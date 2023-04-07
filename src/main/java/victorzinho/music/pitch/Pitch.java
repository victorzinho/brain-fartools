package victorzinho.music.pitch;

/**
 * Specific pitch (defined as pitch class and octave).
 */
public class Pitch {
    private final PitchClass pitchClass;
    private final int octave;

    public static Pitch of(PitchClass pitchClass, int octave) {
        return new Pitch(pitchClass, octave);
    }

    Pitch(PitchClass pitchClass, int octave) {
        this.pitchClass = pitchClass;
        this.octave = octave;
    }

    public PitchClass getPitchClass() {
        return pitchClass;
    }

    public int getOctave() {
        return octave;
    }

    @Override
    public String toString() {
        return pitchClass.toString() + octave;
    }
}
