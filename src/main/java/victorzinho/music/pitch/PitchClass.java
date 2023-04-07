package victorzinho.music.pitch;

import java.util.Map;

public enum PitchClass {
    C_FLAT(11),
    C(0),
    C_SHARP(1),
    D_FLAT(1),
    D(2),
    D_SHARP(3),
    E_FLAT(3),
    E(4),
    E_SHARP(5),
    F_FLAT(4),
    F(5),
    F_SHARP(6),
    G_FLAT(6),
    G(7),
    G_SHARP(8),
    A_FLAT(8),
    A(9),
    A_SHARP(10),
    B_FLAT(10),
    B(11),
    B_SHARP(0);

    // just the most common historically
    private static final Map<Integer, PitchClass> bySemitones =
            Map.ofEntries(
                    Map.entry(0, C),
                    Map.entry(1, C_SHARP),
                    Map.entry(2, D),
                    Map.entry(3, E_FLAT),
                    Map.entry(4, E),
                    Map.entry(5, F),
                    Map.entry(6, F_SHARP),
                    Map.entry(7, G),
                    Map.entry(8, G_SHARP),
                    Map.entry(9, A),
                    Map.entry(10, B_FLAT),
                    Map.entry(11, B));

    public static PitchClass fromSemitones(int semitones) {
        return bySemitones.get(semitones % 12);
    }

    private final int semitones;

    PitchClass(int semitones) {
        this.semitones = semitones;
    }

    public int getSemitones() {
        return semitones;
    }

    public Pitch toPitch(int octave) {
        return Pitch.of(this, octave);
    }

    @Override
    public String toString() {
        return this.name().replace("_", "")
                .replace("SHARP", "#")
                .replace("FLAT", "b");
    }

    public PitchClass sum(PitchClass other) {
        return PitchClass.fromSemitones((this.semitones + other.semitones) % 12);
    }
}
