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

    public static Note createNote(Step step, boolean sharp, int octave, String value) {
        NoteType type = new NoteType();
        type.setValue(value);

        Pitch pitch = new Pitch();
        pitch.setOctave(octave);
        if (sharp) pitch.setAlter(new BigDecimal(1));
        pitch.setStep(step);

        Note note = new Note();
        note.setPitch(pitch);
        note.setType(type);
        note.setDuration(new BigDecimal(1));

        return note;
    }

    public static Note createRest(String value) {
        NoteType type = new NoteType();
        type.setValue(value);

        Note note = new Note();
        note.setRest(new Rest());
        note.setType(type);
        note.setDuration(new BigDecimal(4));

        return note;
    }
}
