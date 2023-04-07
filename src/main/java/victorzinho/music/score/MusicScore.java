package victorzinho.music.score;

import org.audiveris.proxymusic.*;
import org.audiveris.proxymusic.ScorePartwise.Part;
import org.audiveris.proxymusic.ScorePartwise.Part.Measure;
import org.audiveris.proxymusic.util.Marshalling.MarshallingException;
import victorzinho.music.pitch.Pitch;

import java.io.OutputStream;
import java.lang.String;
import java.util.HashMap;
import java.util.Map;

import static org.audiveris.proxymusic.util.Marshalling.marshal;
import static victorzinho.music.score.AudiverisMusicFactory.*;

public class MusicScore {
    private final Map<String, Measure> partToMeasure;
    private final Map<String, Part> nameToPart;
    private final ScorePartwise score;
    private int nPart = 1;
    private final Map<String, Integer> partToNotesPerMeasure;

    public MusicScore() {
        this.score = new ScorePartwise();
        this.score.setPartList(new PartList());
        this.partToMeasure = new HashMap<>();
        this.nameToPart = new HashMap<>();
        this.partToNotesPerMeasure = new HashMap<>();
    }

    public void addPart(String name) {
        ScorePart scorePart = createScorePart("p" + nPart++, name);
        score.getPartList().getPartGroupOrScorePart().add(scorePart);

        Part part = createPart(scorePart);
        score.getPart().add(part);

        Measure measure = new Measure();
        part.getMeasure().add(measure);
        partToNotesPerMeasure.put(name, 0);

        nameToPart.put(name, part);
        partToMeasure.put(name, measure);
    }

    public void addNote(String partName, Pitch pitch, NoteValue value) {
        Note note = createNote(pitch, value);
        partToMeasure.get(partName).getNoteOrBackupOrForward().add(note);
        updateNotesPerMeasure(partName);
    }

    public void addRest(String partName, NoteValue value) {
        Note rest = createRest(value);
        partToMeasure.get(partName).getNoteOrBackupOrForward().add(rest);
        updateNotesPerMeasure(partName);
    }

    private void updateNotesPerMeasure(String partName) {
        int notesPerMeasure = partToNotesPerMeasure.get(partName) + 1;
        partToNotesPerMeasure.put(partName, notesPerMeasure);
        if (notesPerMeasure == 1) {
            Measure measure = new Measure();
            nameToPart.get(partName).getMeasure().add(measure);
            partToMeasure.put(partName, measure);
            partToNotesPerMeasure.put(partName, 0);
        }

    }

    public void export(OutputStream output) throws MarshallingException {
        marshal(score, output, false, 2);
    }
}
