package victorzinho.music.usecases.atyla;

import org.geotools.data.DataUtilities;
import org.geotools.data.collection.SpatialIndexFeatureCollection;
import org.geotools.data.shapefile.ShapefileDumper;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.factory.CommonFactoryFinder;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.GeometryFactory;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import victorzinho.music.hexgrid.HexGridFeatureCollection;
import victorzinho.music.hexgrid.HexGridPitchClassProvider;
import victorzinho.music.pitch.PitchClass;
import victorzinho.music.pointdata.PointData;
import victorzinho.music.pointdata.PointDataFeatureCollection;
import victorzinho.music.pointdata.PointDataInterpolator;
import victorzinho.music.score.MusicScoreGenerator;
import victorzinho.music.score.generators.ClassifierPartGenerator;
import victorzinho.music.score.generators.HexPitchPartGenerator;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static com.diffplug.common.base.Errors.rethrow;
import static org.geotools.process.classify.ClassificationMethod.NATURAL_BREAKS;
import static victorzinho.music.hexgrid.HexGridFeatureCollection.ATTR_GEOM;
import static victorzinho.music.score.generators.ClassifierPartGenerator.newPartGenerators;
import static victorzinho.music.score.generators.HexPitchPartGenerator.newPartGenerators;

public class MusicHarmonyGenerator<TPointData extends PointData>
        extends MusicScoreGenerator<PitchClass> {
    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();
    private static final FilterFactory2 FILTER_FACTORY = CommonFactoryFinder.getFilterFactory2();

    private final HexGridPitchClassProvider hexGridPitchClassProvider;
    private final SimpleFeatureSource hexGridSource;
    private final boolean writeIntermediateFiles;

    /**
     * @param hexGridPitchClassProvider Provider for hex grid values.
     * @param hexGridSize               The size of the hex grid cells, defined as the outer circle enclosing the cell (in meters).
     * @param hexGridEnvelope           The region of interest, in WGS84.
     * @param exportDir                 The directory to export files.
     * @param writeIntermediateFiles    Flag to determine whether to export intermediate results (such as the hex grid) or not.
     */
    public MusicHarmonyGenerator(
            HexGridPitchClassProvider hexGridPitchClassProvider,
            float hexGridSize, Envelope hexGridEnvelope,
            File exportDir, boolean writeIntermediateFiles
    ) {
        super(exportDir);
        this.hexGridPitchClassProvider = hexGridPitchClassProvider;
        this.writeIntermediateFiles = writeIntermediateFiles;
        this.hexGridSource = getHexGridSource(hexGridPitchClassProvider, hexGridSize, hexGridEnvelope);
    }

    private SimpleFeatureSource getHexGridSource(
            HexGridPitchClassProvider hexGridProvider, float hexGridSize, Envelope envelope
    ) {
        try {
            HexGridFeatureCollection<PitchClass> hexGrid = new HexGridFeatureCollection<>(hexGridSize, envelope, hexGridProvider);
            if (writeIntermediateFiles) new ShapefileDumper(this.outputDir).dump(hexGrid);
            return DataUtilities.source(new SpatialIndexFeatureCollection(hexGrid));
        } catch (IOException e) {
            throw new RuntimeException("Cannot get hex grid", e);
        }
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
     * @param pointData                  The point data to be used for generating the score. WGS84 positions must be contained in the envelope.
     * @param interpolatorAdapter        Adapter to select values for interpolation and rebuild point data.
     * @param interpolationStepInSeconds Number of seconds between each step of the interpolation (each note in the score).
     * @param attributeDescriptors       Extra attribute descriptors to be written (their classified values) as separate score parts.
     * @throws Exception if it explodes
     */
    public void generateScore(
            List<TPointData> pointData,
            PointDataInterpolator.Adapter<TPointData> interpolatorAdapter, int interpolationStepInSeconds,
            List<AttributeDescriptor<TPointData, ?>> attributeDescriptors
    ) throws Exception {
        // get interpolated data
        List<TPointData> interpolatedData = new PointDataInterpolator<>(interpolatorAdapter)
                .interpolate(pointData, interpolationStepInSeconds);
        PointDataFeatureCollection<TPointData> collection = new PointDataFeatureCollection<>(interpolatedData::iterator, attributeDescriptors);
        if (writeIntermediateFiles) new ShapefileDumper(this.outputDir).dump(collection);

        // get generators
        List<HexPitchPartGenerator> hexPitchPartGenerators = newPartGenerators(hexGridPitchClassProvider);
        List<ClassifierPartGenerator> generators = newPartGenerators(collection, attributeDescriptors, NATURAL_BREAKS);

        // generate
        generateScore(collection, generators, hexPitchPartGenerators);
    }

    @Override
    protected PitchClass getData(SimpleFeature feature) {
        Coordinate coordinate = getCoordinate(feature);
        Filter intersects = FILTER_FACTORY.intersects(
                FILTER_FACTORY.property(ATTR_GEOM),
                FILTER_FACTORY.literal(GEOMETRY_FACTORY.createPoint(coordinate)));
        SimpleFeatureCollection intersection = rethrow().get(() -> hexGridSource.getFeatures(intersects));
        try (SimpleFeatureIterator iterator = intersection.features()) {
            SimpleFeature hex = iterator.next();
            return (PitchClass) hex.getAttribute(HexGridFeatureCollection.ATTR_VALUE);
        }
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
