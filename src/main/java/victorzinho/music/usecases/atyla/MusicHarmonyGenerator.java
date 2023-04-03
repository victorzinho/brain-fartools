package victorzinho.music.usecases.atyla;

import org.geotools.data.DataUtilities;
import org.geotools.data.collection.SpatialIndexFeatureCollection;
import org.geotools.data.shapefile.ShapefileDumper;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.filter.function.RangedClassifier;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Point;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.FilterFactory2;
import victorzinho.music.PitchClass;
import victorzinho.music.hexGrid.HexGridFeatureCollection;
import victorzinho.music.hexGrid.HexGridPitchClassProvider;
import victorzinho.music.pointdata.PointData;
import victorzinho.music.pointdata.PointDataFeatureCollection;
import victorzinho.music.pointdata.PointDataInterpolator;
import victorzinho.music.score.MusicScore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static victorzinho.music.PitchClass.fromSemitones;
import static victorzinho.music.hexGrid.HexGridFeatureCollection.ATTR_GEOM;
import static victorzinho.music.pointdata.PointDataFeatureCollection.ATTR_COURSE;
import static victorzinho.music.pointdata.PointDataFeatureCollection.ATTR_SPEED;

public class MusicHarmonyGenerator<TPointData extends PointData> {
    private static final FilterFactory2 FILTER_FACTORY = CommonFactoryFinder.getFilterFactory2();

    private final boolean writeIntermediateFiles;
    private final File exportDir;

    public MusicHarmonyGenerator(boolean writeIntermediateFiles, File exportDir) {
        this.writeIntermediateFiles = writeIntermediateFiles;
        this.exportDir = exportDir;
    }

    /**
     * Generates art.
     * <p>
     * Creates a harmony from a hexagonal grid and a point collection. 8 + n parts are written to the score, in the
     * directory specified in the constructor.
     *
     * <ul>
     *     <li>First one is the value for the current grid cell</li>
     *     <li>Second one is the value for the directly adjacent grid cell in the course direction. This is,
     *     the cell from which the "entity" will exit the current cell with the current course; the one the "thing"
     *     is facing.</li>
     *     <li>3rd to 8th are all the neighbour cells, from most to least "relevant" (closest to the cell in the course direction).</li>
     *     <li>All other n parts are obtained by the given classifiers provided by <code>extraAttributes</code></li>
     * </ul>
     *
     * @param hexGridPitchClassProvider  Provider for hex grid values.
     * @param hexGridSize                The size of the hex grid cells, defined as the outer circle enclosing the cell (in meters).
     * @param hexGridEnvelope            The region of interest, in WGS84.
     * @param pointData                  The point data to be used for generating the score. WGS84 positions must be contained in the envelope.
     * @param interpolatorAdapter        Adapter to select values for interpolation and rebuild point data.
     * @param interpolationStepInSeconds Number of seconds between each step of the interpolation (each note in the score).
     * @param attributeDescriptors       Extra attribute descriptors to be written (their classified values) as separate score parts.
     * @throws Exception if it explodes
     */
    public void generateScore(
            HexGridPitchClassProvider hexGridPitchClassProvider,
            float hexGridSize, Envelope hexGridEnvelope,
            List<TPointData> pointData, PointDataInterpolator.Adapter<TPointData> interpolatorAdapter, int interpolationStepInSeconds,
            List<AttributeDescriptor<TPointData, ?>> attributeDescriptors
    ) throws Exception {
        HexGridFeatureCollection<PitchClass> hexGrid = getHexGrid(hexGridPitchClassProvider, hexGridSize, hexGridEnvelope);
        PointDataFeatureCollection<TPointData> collection = getInterpolatedPointData(pointData, interpolatorAdapter,
                attributeDescriptors, interpolationStepInSeconds);
        MusicScore score = createScore(hexGrid, hexGridPitchClassProvider, collection, attributeDescriptors);
        try (FileOutputStream output = new FileOutputStream(new File(this.exportDir, "score.xml"))) {
            score.export(output);
        }
    }

    private HexGridFeatureCollection<PitchClass> getHexGrid(
            HexGridPitchClassProvider hexGridProvider, float hexGridSize, Envelope envelope
    ) throws IOException {
        HexGridFeatureCollection<PitchClass> hexGrid = new HexGridFeatureCollection<>(hexGridSize, envelope, hexGridProvider);
        if (writeIntermediateFiles) new ShapefileDumper(this.exportDir).dump(hexGrid);
        return hexGrid;
    }

    private PointDataFeatureCollection<TPointData> getInterpolatedPointData(
            List<TPointData> pointData,
            PointDataInterpolator.Adapter<TPointData> adapter,
            List<AttributeDescriptor<TPointData, ?>> attributeDescriptors,
            int timeStepInSeconds
    ) throws IOException {
        List<TPointData> interpolatedData = new PointDataInterpolator<>(adapter).interpolate(pointData, timeStepInSeconds);
        PointDataFeatureCollection<TPointData> interpolatedCollection = new PointDataFeatureCollection<>(interpolatedData::iterator, attributeDescriptors);
        if (writeIntermediateFiles) new ShapefileDumper(this.exportDir).dump(interpolatedCollection);
        return interpolatedCollection;
    }

    private MusicScore createScore(
            HexGridFeatureCollection<PitchClass> hexGrid,
            HexGridPitchClassProvider hexGridPitchClassProvider,
            PointDataFeatureCollection<TPointData> inputData,
            List<AttributeDescriptor<TPointData, ?>> extraAttributes
    ) throws Exception {
        Map<String, RangedClassifier> classifierByName = extraAttributes.stream().collect(Collectors.toMap(
                AttributeDescriptor::getAttribute,
                classifier -> getClassifier(inputData, classifier)));
        SimpleFeatureSource hexGridSource = DataUtilities.source(new SpatialIndexFeatureCollection(hexGrid));

        MusicScore score = new MusicScore();
        score.addPart("bass");
        score.addPart("note1");
        score.addPart("note2");
        score.addPart("note3");
        score.addPart("note4");
        score.addPart("note5");
        score.addPart("note6");
        score.addPart("note7");
        classifierByName.keySet().forEach(score::addPart);

        try (SimpleFeatureIterator iterator = inputData.features()) {
            while (iterator.hasNext()) {
                SimpleFeature next = iterator.next();
                SimpleFeature hex = getHexIntersection(hexGridSource, (Point) next.getDefaultGeometry());

                classifierByName.forEach((attribute, classifier) -> Optional.ofNullable(next.getAttribute(attribute))
                        .map(value -> Double.parseDouble(value.toString()))
                        .map(value -> fromSemitones(classifier.classify(value)))
                        .ifPresent(pitchClass -> score.addNote(attribute, pitchClass.getStep(), pitchClass.isSharp(), 5)));

                PitchClass bassNote = (PitchClass) hex.getAttribute(HexGridFeatureCollection.ATTR_VALUE);

                Double speed = (Double) next.getAttribute(ATTR_SPEED);
                Integer course = (Integer) next.getAttribute(ATTR_COURSE);
                PitchClass[] pitchClasses = null;
                if (speed != null && speed > 0 && course != null) {
                    if (course > 0 && course <= 60) {
                        pitchClasses = new PitchClass[]{
                                bassNote,
                                hexGridPitchClassProvider.getValueFromSouthWest(bassNote),
                                hexGridPitchClassProvider.getValueFromSouthWest(bassNote),
                                hexGridPitchClassProvider.getValueFromSouthEast(bassNote),
                                hexGridPitchClassProvider.getValueFromWest(bassNote),
                                hexGridPitchClassProvider.getValueFromNorthWest(bassNote),
                                hexGridPitchClassProvider.getValueFromEast(bassNote),
                                hexGridPitchClassProvider.getValueFromNorthEast(bassNote)
                        };
                    } else if (course > 60 && course <= 120) {
                        pitchClasses = new PitchClass[]{
                                bassNote,
                                hexGridPitchClassProvider.getValueFromWest(bassNote),
                                hexGridPitchClassProvider.getValueFromWest(bassNote),
                                hexGridPitchClassProvider.getValueFromSouthWest(bassNote),
                                hexGridPitchClassProvider.getValueFromNorthWest(bassNote),
                                hexGridPitchClassProvider.getValueFromSouthEast(bassNote),
                                hexGridPitchClassProvider.getValueFromNorthEast(bassNote),
                                hexGridPitchClassProvider.getValueFromEast(bassNote),
                        };
                    } else if (course > 120 && course <= 180) {
                        pitchClasses = new PitchClass[]{
                                bassNote,
                                hexGridPitchClassProvider.getValueFromNorthWest(bassNote),
                                hexGridPitchClassProvider.getValueFromNorthWest(bassNote),
                                hexGridPitchClassProvider.getValueFromWest(bassNote),
                                hexGridPitchClassProvider.getValueFromNorthEast(bassNote),
                                hexGridPitchClassProvider.getValueFromSouthWest(bassNote),
                                hexGridPitchClassProvider.getValueFromEast(bassNote),
                                hexGridPitchClassProvider.getValueFromSouthEast(bassNote)
                        };
                    } else if (course > 180 && course <= 240) {
                        pitchClasses = new PitchClass[]{
                                bassNote,
                                hexGridPitchClassProvider.getValueFromNorthEast(bassNote),
                                hexGridPitchClassProvider.getValueFromNorthEast(bassNote),
                                hexGridPitchClassProvider.getValueFromNorthWest(bassNote),
                                hexGridPitchClassProvider.getValueFromEast(bassNote),
                                hexGridPitchClassProvider.getValueFromWest(bassNote),
                                hexGridPitchClassProvider.getValueFromSouthEast(bassNote),
                                hexGridPitchClassProvider.getValueFromSouthWest(bassNote)
                        };
                    } else if (course > 240 && course <= 300) {
                        pitchClasses = new PitchClass[]{
                                bassNote,
                                hexGridPitchClassProvider.getValueFromEast(bassNote),
                                hexGridPitchClassProvider.getValueFromEast(bassNote),
                                hexGridPitchClassProvider.getValueFromNorthEast(bassNote),
                                hexGridPitchClassProvider.getValueFromSouthEast(bassNote),
                                hexGridPitchClassProvider.getValueFromNorthWest(bassNote),
                                hexGridPitchClassProvider.getValueFromSouthWest(bassNote),
                                hexGridPitchClassProvider.getValueFromWest(bassNote)
                        };
                    } else if (course > 300 && course <= 360) {
                        pitchClasses = new PitchClass[]{
                                bassNote,
                                hexGridPitchClassProvider.getValueFromSouthEast(bassNote),
                                hexGridPitchClassProvider.getValueFromSouthEast(bassNote),
                                hexGridPitchClassProvider.getValueFromEast(bassNote),
                                hexGridPitchClassProvider.getValueFromSouthWest(bassNote),
                                hexGridPitchClassProvider.getValueFromNorthEast(bassNote),
                                hexGridPitchClassProvider.getValueFromWest(bassNote),
                                hexGridPitchClassProvider.getValueFromNorthWest(bassNote)
                        };
                    }
                }

                if (pitchClasses == null) {
                    pitchClasses = new PitchClass[]{
                            bassNote,
                            bassNote,
                            hexGridPitchClassProvider.getValueFromEast(bassNote),
                            hexGridPitchClassProvider.getValueFromSouthEast(bassNote),
                            hexGridPitchClassProvider.getValueFromSouthWest(bassNote),
                            hexGridPitchClassProvider.getValueFromWest(bassNote),
                            hexGridPitchClassProvider.getValueFromNorthWest(bassNote),
                            hexGridPitchClassProvider.getValueFromNorthEast(bassNote),
                    };
                }

                score.addNote("bass", pitchClasses[0].getStep(), pitchClasses[0].isSharp(), 3);
                score.addNote("note1", pitchClasses[1].getStep(), pitchClasses[1].isSharp(), 4);
                score.addNote("note2", pitchClasses[2].getStep(), pitchClasses[2].isSharp(), 4);
                score.addNote("note3", pitchClasses[3].getStep(), pitchClasses[3].isSharp(), 4);
                score.addNote("note4", pitchClasses[4].getStep(), pitchClasses[4].isSharp(), 4);
                score.addNote("note5", pitchClasses[5].getStep(), pitchClasses[5].isSharp(), 4);
                score.addNote("note6", pitchClasses[6].getStep(), pitchClasses[6].isSharp(), 4);
                score.addNote("note7", pitchClasses[7].getStep(), pitchClasses[7].isSharp(), 4);
            }
        }

        return score;
    }

    private static SimpleFeature getHexIntersection(SimpleFeatureSource hexGridSource, Point position) throws
            IOException {
        SimpleFeatureCollection intersection = hexGridSource.getFeatures(FILTER_FACTORY.intersects(
                FILTER_FACTORY.property(ATTR_GEOM),
                FILTER_FACTORY.literal(position)));
        try (SimpleFeatureIterator iterator = intersection.features()) {
            return iterator.next();
        }
    }

    private RangedClassifier getClassifier(
            SimpleFeatureCollection collection, AttributeDescriptor<TPointData, ?> classifier
    ) {
        return (RangedClassifier) FILTER_FACTORY.function("Jenks",
                        FILTER_FACTORY.property(classifier.getAttribute()),
                        FILTER_FACTORY.literal(classifier.getNClasses()))
                .evaluate(collection);
    }

    public static class AttributeDescriptor<TPointData, TBinding> extends PointDataFeatureCollection.AttributeDescriptor<TPointData, TBinding> {
        private int nClasses;

        public int getNClasses() {
            return nClasses;
        }

        public AttributeDescriptor<TPointData, TBinding> setNClasses(int nClasses) {
            this.nClasses = nClasses;
            return this;
        }
    }
}
