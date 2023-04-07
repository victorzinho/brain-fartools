package victorzinho.music.score.generators;

import org.opengis.feature.simple.SimpleFeature;
import victorzinho.music.hexgrid.HexGridPitchClassProvider;
import victorzinho.music.pitch.PitchClass;
import victorzinho.music.score.AbstractNamedMusicPart;
import victorzinho.music.score.MusicPartGeneratorRequiringData;
import victorzinho.music.score.MusicScore;
import victorzinho.music.score.NoteValue;

import java.util.List;

import static victorzinho.music.pointdata.PointDataFeatureCollection.ATTR_COURSE;
import static victorzinho.music.pointdata.PointDataFeatureCollection.ATTR_SPEED;

/**
 * Generates score parts from a hex grid. The features passed to {@link #processNextFeature(SimpleFeature, PitchClass, MusicScore)}
 * are assumed to be {@link victorzinho.music.pointdata.PointDataFeatureCollection},
 * with attributes {@link victorzinho.music.pointdata.PointDataFeatureCollection#ATTR_COURSE}
 * and {@link victorzinho.music.pointdata.PointDataFeatureCollection#ATTR_SPEED}.
 */
public class HexPitchPartGenerator
        extends AbstractNamedMusicPart
        implements MusicPartGeneratorRequiringData<PitchClass> {

    private static final int OCTAVE_CENTER = 2;
    private static final int LOWER_OCTAVE_AROUND = 3;

    /**
     * Obtains all the part generators for the hex grid.
     *
     * <ul>
     *     <li>First one is the value for the current grid cell.</li>
     *     <li>Second one is the value for the directly adjacent grid cell in the course direction. This is,
     *     the cell from which the "entity" will exit the current cell with the current course; the one the "thing"
     *     is facing.</li>
     *     <li>3rd to 8th are all the neighbour cells, from most to least "relevant" (closest to the cell in the course direction).</li>
     *     <li>All other n parts are obtained by the given classifiers provided by <code>extraAttributes</code></li>
     * </ul>
     *
     * @param hexGridPitchClassProvider The provider of hex grid values.
     * @return The list of part generators
     */
    public static List<HexPitchPartGenerator> newPartGenerators(
            HexGridPitchClassProvider hexGridPitchClassProvider
    ) {
        return List.of(
                new HexPitchPartGenerator("bass", hexGridPitchClassProvider, 0, OCTAVE_CENTER),
                new HexPitchPartGenerator("violin1", hexGridPitchClassProvider, 1, LOWER_OCTAVE_AROUND + 2),
                new HexPitchPartGenerator("violin2", hexGridPitchClassProvider, 2, LOWER_OCTAVE_AROUND + 2),
                new HexPitchPartGenerator("violin3", hexGridPitchClassProvider, 3, LOWER_OCTAVE_AROUND + 1),
                new HexPitchPartGenerator("violin4", hexGridPitchClassProvider, 4, LOWER_OCTAVE_AROUND),
                new HexPitchPartGenerator("violin5", hexGridPitchClassProvider, 5, LOWER_OCTAVE_AROUND + 2),
                new HexPitchPartGenerator("violin6", hexGridPitchClassProvider, 6, LOWER_OCTAVE_AROUND + 1),
                new HexPitchPartGenerator("violin7", hexGridPitchClassProvider, 7, LOWER_OCTAVE_AROUND)
        );
    }

    private final HexGridPitchClassProvider hexGridPitchClassProvider;
    private final int pitchIndex;
    private final int octave;

    private HexPitchPartGenerator(
            String name, HexGridPitchClassProvider hexGridPitchClassProvider, int pitchIndex, int octave
    ) {
        super(name);
        this.hexGridPitchClassProvider = hexGridPitchClassProvider;
        this.pitchIndex = pitchIndex;
        this.octave = octave;
    }

    @Override
    public void processNextFeature(SimpleFeature feature, PitchClass bassNote, MusicScore score) {
        Double speed = (Double) feature.getAttribute(ATTR_SPEED);
        Double course = (Double) feature.getAttribute(ATTR_COURSE);
        PitchClass[] pitchClasses = null;
        if (speed != null && speed > 0 && course != null) {
            if (course > 0 && course <= Math.PI * 1 / 3) {
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
            } else if (course > Math.PI * 1 / 3 && course <= Math.PI * 2 / 3) {
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
            } else if (course > Math.PI * 2 / 3 && course <= Math.PI) {
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
            } else if (course > Math.PI && course <= Math.PI * 4 / 3) {
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
            } else if (course > Math.PI * 4 / 3 && course <= Math.PI * 5 / 3) {
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
            } else if (course > Math.PI * 5 / 3 && course <= Math.PI * 2) {
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

        score.addNote(getPartName(), pitchClasses[this.pitchIndex].toPitch(this.octave), NoteValue.WHOLE);
    }
}
