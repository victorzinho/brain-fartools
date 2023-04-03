package victorzinho.music;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

import java.util.Arrays;
import java.util.Map;

import org.audiveris.proxymusic.Step;

public enum PitchClass {
    C(0, Step.C, false),
    C_SHARP(1, Step.C, true),
    D(2, Step.D, false),
    E_FLAT(3, Step.D, true),
    E(4, Step.E, false),
    F(5, Step.F, false),
    F_SHARP(6, Step.F, true),
    G(7, Step.G, false),
    G_SHARP(8, Step.G, true),
    A(9, Step.A, false),
    B_FLAT(10, Step.A, true),
    B(11, Step.B, false);

    private static final Map<Integer, PitchClass> bySemitones = Arrays.stream(PitchClass.values())
            .collect(toMap(PitchClass::getSemitones, identity()));

    public static PitchClass fromSemitones(int semitones) {
        return bySemitones.get(semitones % 12);
    }

    private final int semitones;
    private final Step step;
    private final boolean isSharp;

    PitchClass(int semitones, Step step, boolean isSharp) {
        this.semitones = semitones;
        this.step = step;
        this.isSharp = isSharp;
    }

    public int getSemitones() {
        return semitones;
    }

    public Step getStep() {
        return this.step;
    }

    public boolean isSharp() {
        return this.isSharp;
    }

    @Override
    public String toString() {
        return this.step + (isSharp ? "#" : "");
    }
}
