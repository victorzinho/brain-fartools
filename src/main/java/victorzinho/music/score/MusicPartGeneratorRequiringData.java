package victorzinho.music.score;

import org.opengis.feature.simple.SimpleFeature;

/**
 * Generates music for a score part from {@link SimpleFeature}s and extra data obtained from the {@link SimpleFeature}
 * that might apply to all part generators (such as the {@link victorzinho.music.pitch.PitchClass} for a given
 * coordinate in the hex grid use case).
 *
 * @param <T> The type of extra data to consider together with the {@link SimpleFeature} to write music to the part.
 */
public interface MusicPartGeneratorRequiringData<T> extends NamedMusicPart {
    /**
     * Processes the next feature and extra data, potentially writing to the score.
     *
     * @param feature The next feature to consider for writing music to the part.
     * @param data    Extra data to consider for writing music to the part.
     * @param score   The score to write music to.
     */
    void processNextFeature(SimpleFeature feature, T data, MusicScore score);
}
