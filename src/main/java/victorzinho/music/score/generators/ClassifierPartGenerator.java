package victorzinho.music.score.generators;

import org.geotools.filter.function.RangedClassifier;
import org.geotools.process.classify.ClassificationMethod;
import org.opengis.feature.simple.SimpleFeature;
import victorzinho.music.pointdata.PointData;
import victorzinho.music.pointdata.PointDataFeatureCollection;
import victorzinho.music.process.ClassificationProcess;
import victorzinho.music.score.AbstractNamedMusicPart;
import victorzinho.music.score.MusicPartGenerator;
import victorzinho.music.score.MusicScore;
import victorzinho.music.score.NoteValue;
import victorzinho.music.usecases.atyla.MusicHarmonyGenerator.AttributeDescriptor;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static victorzinho.music.pitch.PitchClass.fromSemitones;

/**
 * Generates score parts from ranged classifiers, assuming the passed feature has the attribute in the classifier.
 * The value obtained from the classifier for each feature will be interpreted as the number of semitones to obtain
 * the {@link victorzinho.music.pitch.PitchClass} (0-based, the lowest values in the collection for the attribute
 * will be assigned to the {@link victorzinho.music.pitch.PitchClass} with 0 semitones).
 */
public class ClassifierPartGenerator extends AbstractNamedMusicPart implements MusicPartGenerator {
    private static final int OCTAVE = 5;

    public static <TPointData extends PointData> List<ClassifierPartGenerator> newPartGenerators(
            PointDataFeatureCollection<TPointData> collection,
            List<AttributeDescriptor<TPointData, ?>> descriptors,
            ClassificationMethod method
    ) {
        if (descriptors == null) return Collections.emptyList();

        return descriptors.stream()
                .map(descriptor -> new ClassifierPartGenerator(
                        descriptor.getAttribute(),
                        descriptor.getAttribute(),
                        new ClassificationProcess().getClassifier(collection,
                                descriptor.getAttribute(), descriptor.getNClasses(), method)))
                .toList();
    }

    private final String attribute;
    private final RangedClassifier classifier;

    public ClassifierPartGenerator(String partName, String attribute, RangedClassifier classifier) {
        super(partName);
        this.attribute = attribute;
        this.classifier = classifier;
    }

    @Override
    public void processNextFeature(SimpleFeature feature, MusicScore score) {
        Optional.ofNullable(feature.getAttribute(attribute))
                .map(value -> Double.parseDouble(value.toString()))
                .map(value -> fromSemitones(classifier.classify(value)))
                .ifPresent(pitchClass -> score.addNote(attribute, pitchClass.toPitch(OCTAVE), NoteValue.WHOLE));
    }
}
