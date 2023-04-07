package victorzinho.music.score;

import java.math.BigDecimal;

import org.audiveris.proxymusic.Note;
import org.audiveris.proxymusic.NoteType;
import org.audiveris.proxymusic.PartName;
import org.audiveris.proxymusic.Pitch;
import org.audiveris.proxymusic.Rest;
import org.audiveris.proxymusic.ScorePart;
import org.audiveris.proxymusic.ScorePartwise.Part;
import org.audiveris.proxymusic.Step;

public class AudiverisMusicFactory {
    public static Part createPart(ScorePart scorePart) {
        Part part = new Part();
        part.setId(scorePart);
        return part;
    }

    public static ScorePart createScorePart(String id, String name) {
        PartName partName = new PartName();
        partName.setValue(name);

        ScorePart scorePart = new ScorePart();
        scorePart.setId(id);
        scorePart.setPartName(partName);
        return scorePart;
    }

    public static Note createNote(victorzinho.music.pitch.Pitch pitch, NoteValue value) {
        NoteType type = new NoteType();
        type.setValue(getValue(value));

        Pitch audiverisPitch = new Pitch();
        audiverisPitch.setOctave(pitch.getOctave());
        audiverisPitch.setAlter(getAlter(pitch));
        audiverisPitch.setStep(getStep(pitch));

        Note note = new Note();
        note.setPitch(audiverisPitch);
        note.setType(type);
        note.setDuration(new BigDecimal(1));

        return note;
    }

    public static Note createRest(NoteValue value) {
        NoteType type = new NoteType();
        type.setValue(getValue(value));

        Note note = new Note();
        note.setRest(new Rest());
        note.setType(type);
        note.setDuration(new BigDecimal(4));

        return note;
    }

    private static Step getStep(victorzinho.music.pitch.Pitch pitch) {
        return switch (pitch.getPitchClass()) {
            case C_FLAT, C, C_SHARP -> Step.C;
            case D_FLAT, D, D_SHARP -> Step.D;
            case E_FLAT, E, E_SHARP -> Step.E;
            case F_FLAT, F, F_SHARP -> Step.F;
            case G_FLAT, G, G_SHARP -> Step.G;
            case A_FLAT, A, A_SHARP -> Step.A;
            case B_FLAT, B, B_SHARP -> Step.B;
        };
    }

    private static BigDecimal getAlter(victorzinho.music.pitch.Pitch pitch) {
        return switch (pitch.getPitchClass()) {
            case C_FLAT, D_FLAT, E_FLAT, F_FLAT, G_FLAT, A_FLAT, B_FLAT -> new BigDecimal(-1);
            case C, D, E, F, G, A, B -> new BigDecimal(0);
            case C_SHARP, D_SHARP, E_SHARP, F_SHARP, G_SHARP, A_SHARP, B_SHARP -> new BigDecimal(1);
        };
    }

    private static String getValue(NoteValue value) {
        return switch (value) {
            case _1024TH -> "1024th";
            case _512TH -> "512th";
            case _256TH -> "256th";
            case _128TH -> "128th";
            case _64TH -> "64th";
            case _32ND -> "32nd";
            case _16TH -> "16th";
            case EIGHTH -> "eighth";
            case QUARTER -> "quarter";
            case HALF -> "half";
            case WHOLE -> "whole";
            case BREVE -> "breve";
            case LONG -> "long";
            case MAXIMA -> "maxima";
        };
    }
}
