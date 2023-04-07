package victorzinho.music.score.generators;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.filter.function.RangedClassifier;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.jts.JTS;
import org.geotools.process.classify.ClassificationMethod;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Coordinate;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.geometry.DirectPosition;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import victorzinho.music.pitch.PitchClass;
import victorzinho.music.pitch.PitchClassSet;
import victorzinho.music.process.ClassificationProcess;
import victorzinho.music.process.WithCoverageValueCollection;
import victorzinho.music.score.AbstractNamedMusicPart;
import victorzinho.music.score.MusicPartGenerator;
import victorzinho.music.score.MusicScore;
import victorzinho.music.score.NoteValue;

import java.util.*;
import java.util.stream.IntStream;

import static com.diffplug.common.base.Errors.rethrow;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static victorzinho.music.pitch.PitchClassSet.*;
import static victorzinho.music.process.WithCoverageValueCollection.ATTR_COVERAGE_VALUE;
import static victorzinho.music.score.MusicScoreGenerator.getCoordinate;

/**
 * Generates a score part by classifying the values of a raster and mapping them to pitch classes,
 * potentially restricting to pitch class sets (either a fixed pitch class set or a list of them,
 * obtained in a similar way by classifying and obtaining values for each coordinate from a separate raster).
 */
public class RasterMusicPartGenerator extends AbstractNamedMusicPart implements MusicPartGenerator {
    public static final List<PitchClassSet> KNOWN_KEY_SIGNATURES = List.of(
            C_MAJOR, G_MAJOR, D_MAJOR, A_MAJOR, E_MAJOR, B_MAJOR,
            F_SHARP_MAJOR, D_FLAT_MAJOR, A_FLAT_MAJOR, E_FLAT_MAJOR, B_FLAT_MAJOR
    );

    public static final EnumSet<PitchClass> ALL_PITCH_CLASSES = EnumSet.copyOf(IntStream.range(0, 12)
            .mapToObj(PitchClass::fromSemitones)
            .toList());

    private final GridCoverage2D pitchClassCoverage;
    private final GridCoverage2D pitchClassSetCoverage;
    private final RangedClassifier pitchClassClassifier;
    private final RangedClassifier pitchClassSetClassifier;
    private final Map<Integer, PitchClass> pitchClassByCoverageClassId;
    private final Map<Integer, PitchClassSet> pitchClassSetByCoverageClassId;


    /**
     * @param partName              Name of the score part
     * @param collection            The collection that will be traversed when generating the parts. It is needed
     *                              to obtain only the relevant values from the coverages.
     * @param availablePitchClasses All the available pitch classes. The classified raster will be mapped to these pitch
     *                              classes, order by {@link PitchClass#getSemitones()}.
     * @param pitchClassCoverage    The coverage to use to classify and obtain values to map to pitch classes.
     * @param pitchClassMethod      The classification method to use for the coverage values.
     * @param pitchClassSet         The pitch class set to restrict the availablePitchClasses. If the obtained value
     *                              is not in the pitch class set, a rest will be written.
     */
    public RasterMusicPartGenerator(
            String partName,
            SimpleFeatureCollection collection,
            EnumSet<PitchClass> availablePitchClasses,
            GridCoverage2D pitchClassCoverage, ClassificationMethod pitchClassMethod,
            PitchClassSet pitchClassSet
    ) {
        super(partName);

        this.pitchClassCoverage = pitchClassCoverage;
        this.pitchClassClassifier = getClassifierFromCoverageValues(collection, pitchClassCoverage,
                availablePitchClasses.size(), pitchClassMethod);
        this.pitchClassByCoverageClassId = getPitchClassByCoverageClassId(availablePitchClasses);

        this.pitchClassSetClassifier = null;
        this.pitchClassSetCoverage = null;
        this.pitchClassSetByCoverageClassId = pitchClassSet != null ? Map.of(0, pitchClassSet) : null;
    }

    /**
     * <b>WARNING: untested</b>
     *
     * @param partName                Name of the score part
     * @param collection              The collection that will be traversed when generating the parts. It is needed
     *                                to obtain only the relevant values from the coverages.
     * @param availablePitchClasses   All the available pitch classes. The classified raster will be mapped to these pitch
     *                                classes, order by {@link PitchClass#getSemitones()}.
     * @param pitchClassCoverage      The coverage to classify and obtain values to map to pitch classes.
     * @param pitchClassMethod        The classification method to use for the pitch classes coverage values.
     * @param availablePitchClassSets All the available pitch class sets. With this constructor it will work the same way
     *                                as the pitch classes: the classified raster will be mapped to these, in the same
     *                                order as provided. If the pitch class set obtained from corresponding coverage
     *                                does not contain the pitch class from the other coverage, a rest will be written.
     * @param pitchClassSetCoverage   The coverage to classify and obtain values to map to pitch class sets.
     * @param pitchClassSetMethod     The classification method to use for the pitch class set coverage values.
     */
    public RasterMusicPartGenerator(
            String partName,
            SimpleFeatureCollection collection,
            EnumSet<PitchClass> availablePitchClasses, GridCoverage2D pitchClassCoverage, ClassificationMethod pitchClassMethod,
            List<PitchClassSet> availablePitchClassSets, GridCoverage2D pitchClassSetCoverage, ClassificationMethod pitchClassSetMethod
    ) {
        super(partName);

        this.pitchClassCoverage = pitchClassCoverage;
        this.pitchClassClassifier = getClassifierFromCoverageValues(collection, pitchClassCoverage,
                availablePitchClasses.size(), pitchClassMethod);
        this.pitchClassByCoverageClassId = getPitchClassByCoverageClassId(availablePitchClasses);

        this.pitchClassSetCoverage = pitchClassSetCoverage;
        if (pitchClassSetCoverage != null) {
            if (availablePitchClassSets.size() == 1) {
                throw new IllegalArgumentException("Please use the other constructor :)");
            }

            this.pitchClassSetClassifier = getClassifierFromCoverageValues(collection, pitchClassSetCoverage,
                    availablePitchClassSets.size(), pitchClassSetMethod);
            this.pitchClassSetByCoverageClassId = IntStream.range(0, availablePitchClassSets.size()).boxed()
                    .collect(toMap(identity(), availablePitchClassSets::get));
        } else {
            this.pitchClassSetClassifier = null;
            this.pitchClassSetByCoverageClassId = null;
        }
    }

    private static RangedClassifier getClassifierFromCoverageValues(
            SimpleFeatureCollection collection, GridCoverage2D coverage, int nClasses, ClassificationMethod method
    ) {
        return new ClassificationProcess().getClassifier(
                new WithCoverageValueCollection(collection, coverage), ATTR_COVERAGE_VALUE,
                nClasses, method);
    }

    private static Map<Integer, PitchClass> getPitchClassByCoverageClassId(Collection<PitchClass> pitchClasses) {
        List<PitchClass> orderedPitchClasses = pitchClasses.stream()
                .sorted(Comparator.comparing(PitchClass::getSemitones))
                .toList();
        return IntStream.range(0, orderedPitchClasses.size()).boxed()
                .collect(toMap(identity(), orderedPitchClasses::get));
    }

    @Override
    public void processNextFeature(SimpleFeature feature, MusicScore score) {
        Coordinate coordinate = getCoordinate(feature);
        if (coordinate == null) return;

        CoordinateReferenceSystem crs = feature.getFeatureType().getCoordinateReferenceSystem();
        PitchClass pitchClass = getPitchClass(coordinate, crs);

        if (!inKeySignature(pitchClass, coordinate, crs)) {
            score.addRest(getPartName(), NoteValue.WHOLE);
        } else {
            score.addNote(getPartName(), pitchClass.toPitch(4), NoteValue.WHOLE);
        }
    }

    private PitchClass getPitchClass(Coordinate coordinate, CoordinateReferenceSystem crs) {
        double coverageValue = getValue(pitchClassCoverage, coordinate, crs);
        int classId = pitchClassClassifier.classify(coverageValue);
        return this.pitchClassByCoverageClassId.get(classId);
    }

    private boolean inKeySignature(PitchClass pitchClass, Coordinate coordinate, CoordinateReferenceSystem crs) {
        if (pitchClassSetByCoverageClassId == null) return true;

        if (pitchClassSetByCoverageClassId.size() == 1) {
            return pitchClassSetByCoverageClassId.values().iterator().next().hasPitch(pitchClass);
        }

        double coverageValue = getValue(pitchClassSetCoverage, coordinate, crs);
        int classId = pitchClassSetClassifier.classify(coverageValue);
        return pitchClassSetByCoverageClassId.get(classId).hasPitch(pitchClass);
    }

    private static double getValue(GridCoverage2D coverage, Coordinate coordinate, CoordinateReferenceSystem coordinateCrs) {
        Coordinate coordinateInCoverageCrs = rethrow().get(() -> JTS.transform(coordinate, null,
                CRS.findMathTransform(coordinateCrs, coverage.getCoordinateReferenceSystem())));
        DirectPosition position = new DirectPosition2D(coordinateInCoverageCrs.x, coordinateInCoverageCrs.y);
        return coverage.evaluate(position, (double[]) null)[0];
    }
}
