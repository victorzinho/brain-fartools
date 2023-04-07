package victorzinho.music.pitch;

import java.util.EnumSet;
import java.util.List;

import static victorzinho.music.pitch.PitchClass.*;

public class PitchClassSet {
    public static final PitchClassSet C_MAJOR = of(C, D, E, F, G, A, B);
    public static final PitchClassSet G_MAJOR = of(G, A, B, C, D, E, F_SHARP);
    public static final PitchClassSet D_MAJOR = of(D, E, F_SHARP, G, A, B, C_SHARP);
    public static final PitchClassSet A_MAJOR = of(A, B, C_SHARP, D, E, F_SHARP, G_SHARP);
    public static final PitchClassSet E_MAJOR = of(E, F_SHARP, G_SHARP, A, B, C_SHARP, D_SHARP);
    public static final PitchClassSet B_MAJOR = of(B, C_SHARP, D_SHARP, E, F_SHARP, G_SHARP, A_SHARP);
    public static final PitchClassSet F_SHARP_MAJOR = of(F_SHARP, G_SHARP, A_SHARP, B, C_SHARP, D_SHARP, E_SHARP);
    public static final PitchClassSet G_FLAT_MAJOR = of(G_FLAT, A_FLAT, B_FLAT, C_FLAT, D_FLAT, E_FLAT, F);
    public static final PitchClassSet D_FLAT_MAJOR = of(D_FLAT, E_FLAT, F, G_FLAT, A_FLAT, B_FLAT, C);
    public static final PitchClassSet A_FLAT_MAJOR = of(A_FLAT, B_FLAT, C, D_FLAT, E_FLAT, F, G);
    public static final PitchClassSet E_FLAT_MAJOR = of(E_FLAT, F, G, A_FLAT, B_FLAT, C, D);
    public static final PitchClassSet B_FLAT_MAJOR = of(B_FLAT, C, D, E_FLAT, F, G, A);
    public static final PitchClassSet F_MAJOR = of(F, G, A, B_FLAT, C, D, E);

    private final EnumSet<PitchClass> pitchClasses;

    public static PitchClassSet of(PitchClass... pitchClasses) {
        return new PitchClassSet(pitchClasses);
    }

    private PitchClassSet(PitchClass... pitchClasses) {
        this.pitchClasses = EnumSet.copyOf(List.of(pitchClasses));
    }

    public boolean hasPitch(PitchClass pitchClass) {
        return this.pitchClasses.contains(pitchClass);
    }
}
