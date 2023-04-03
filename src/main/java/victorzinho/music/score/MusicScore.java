package victorzinho.music.score;

import static victorzinho.music.score.AudiverisMusicFactory.createNote;
import static victorzinho.music.score.AudiverisMusicFactory.createPart;
import static victorzinho.music.score.AudiverisMusicFactory.createRest;
import static victorzinho.music.score.AudiverisMusicFactory.createScorePart;
import static org.audiveris.proxymusic.util.Marshalling.marshal;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.audiveris.proxymusic.Note;
import org.audiveris.proxymusic.PartList;
import org.audiveris.proxymusic.ScorePart;
import org.audiveris.proxymusic.ScorePartwise;
import org.audiveris.proxymusic.ScorePartwise.Part;
import org.audiveris.proxymusic.ScorePartwise.Part.Measure;
import org.audiveris.proxymusic.Step;
import org.audiveris.proxymusic.util.Marshalling.MarshallingException;

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

    public void addNote(String partName, Step step, boolean sharp, int octave) {
        Note note = createNote(step, sharp, octave, "whole");
        partToMeasure.get(partName).getNoteOrBackupOrForward().add(note);
        updateNotesPerMeasure(partName);
    }

    public void addRest(String partName) {
        Note rest = createRest("16th");
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
