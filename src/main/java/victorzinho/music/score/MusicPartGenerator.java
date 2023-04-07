package victorzinho.music.score;

import org.opengis.feature.simple.SimpleFeature;

/**
 * Generates music for a score part from {@link SimpleFeature}s.
 */
public interface MusicPartGenerator extends NamedMusicPart {
    /**
     * Processes the next feature, potentially writing to the score.
     *
     * @param feature The next feature to consider for writing music to the part.
     * @param score   The score to write music to.
     */
    void processNextFeature(SimpleFeature feature, MusicScore score);
}
