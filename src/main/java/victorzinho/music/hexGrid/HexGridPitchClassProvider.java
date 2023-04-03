package victorzinho.music.hexGrid;

import victorzinho.music.PitchClass;

/**
 * {@link HexGridValueProvider} for pitches.
 */
public class HexGridPitchClassProvider implements HexGridValueProvider<PitchClass> {
    private final int westSemitones;
    private final int southEastSemitones;
    private final int southWestSemitones;

    /**
     * Creates a new hexagonal grid value provider with pitches.
     *
     * @param westSemitones      The amount of semitones to sum when moving <b>from</b> west.
     * @param southEastSemitones The amount of semitones to sum when moving <b>from</b> south-east.
     * @param southWestSemitones The amount of semitones to sum when moving <b>from</b> south-west.
     */
    public HexGridPitchClassProvider(int westSemitones, int southEastSemitones, int southWestSemitones) {
        this.westSemitones = westSemitones;
        this.southEastSemitones = southEastSemitones;
        this.southWestSemitones = southWestSemitones;
    }

    @Override
    public PitchClass getValueFromWest(PitchClass west) {
        return PitchClass.fromSemitones(west.getSemitones() + this.westSemitones);
    }

    @Override
    public PitchClass getValueFromSouthEast(PitchClass southEast) {
        return PitchClass.fromSemitones(southEast.getSemitones() + this.southEastSemitones);
    }

    @Override
    public PitchClass getValueFromSouthWest(PitchClass southWest) {
        return PitchClass.fromSemitones(southWest.getSemitones() + this.southWestSemitones);
    }

    @Override
    public PitchClass getValueFromEast(PitchClass east) {
        return PitchClass.fromSemitones((12 + east.getSemitones() - this.westSemitones) % 12);
    }

    @Override
    public PitchClass getValueFromNorthEast(PitchClass northEast) {
        return PitchClass.fromSemitones((12 + northEast.getSemitones() - this.southWestSemitones) % 12);
    }

    @Override
    public PitchClass getValueFromNorthWest(PitchClass northWest) {
        return PitchClass.fromSemitones((12 + northWest.getSemitones() - this.southEastSemitones) % 12);
    }

    @Override
    public PitchClass getInitialValue() {
        return PitchClass.C;
    }

    @Override
    public Class<PitchClass> getValueClass() {
        return PitchClass.class;
    }
}
