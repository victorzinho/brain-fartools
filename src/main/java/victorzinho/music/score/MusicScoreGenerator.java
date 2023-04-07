package victorzinho.music.score;

import org.audiveris.proxymusic.util.Marshalling;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.opengis.feature.simple.SimpleFeature;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.stream.Stream;

/**
 * Generates a score from a SimpleFeatureCollection and a bunch of part generators.
 *
 * @param <T> The type of extra data to obtain for each feature, to be used with part generators
 *            implementing {@link MusicPartGeneratorRequiringData}.
 */
public abstract class MusicScoreGenerator<T> {
    protected final File outputDir;

    /**
     * @param outputDir The output directory to write files to (such as the MusicXML file with the score).
     */
    public MusicScoreGenerator(File outputDir) {
        this.outputDir = outputDir;
    }

    /**
     * Generates a score from a {@link SimpleFeatureCollection} and some part generators, and writes it to
     * the directory in the constructor in MusicXML format.
     *
     * @param collection                  The collection to generate music from. Each feature will be passed to all
     *                                    part generators iteratively.
     * @param partGenerators              The part generators that don't require extra data to generate music for the part.
     * @param partGeneratorsRequiringData The part generators that require extra data to generate music for the part.
     * @throws IOException if the score cannot be written to the output directory.
     */
    public void generateScore(
            SimpleFeatureCollection collection,
            Collection<? extends MusicPartGenerator> partGenerators,
            Collection<? extends MusicPartGeneratorRequiringData<T>> partGeneratorsRequiringData
    ) throws IOException {
        MusicScore score = new MusicScore();

        // preprocess, add parts to score
        Stream.concat(partGenerators.stream(), partGeneratorsRequiringData.stream())
                .forEach(partGenerator -> score.addPart(partGenerator.getPartName()));

        // process all coordinates for all part generators
        try (SimpleFeatureIterator iterator = collection.features()) {
            while (iterator.hasNext()) {
                SimpleFeature next = iterator.next();
                T data = getData(next);
                partGenerators.forEach(partGenerator -> partGenerator.processNextFeature(next, score));
                partGeneratorsRequiringData.forEach(partGenerator -> partGenerator.processNextFeature(next, data, score));
            }
        }

        try (FileOutputStream output = new FileOutputStream(new File(this.outputDir, "score.xml"))) {
            score.export(output);
        } catch (Marshalling.MarshallingException e) {
            throw new IOException(e);
        }
    }

    /**
     * @return the data to be passed to each {@link MusicPartGeneratorRequiringData} when generating music from
     * simple features.
     */
    protected abstract T getData(SimpleFeature feature);

    /**
     * @return the first coordinate of the feature geometry or null if the feature or geometry are not there.
     */
    public static Coordinate getCoordinate(SimpleFeature feature) {
        if (feature == null) return null;
        if (feature.getDefaultGeometry() instanceof Geometry geometry) {
            return geometry.getCoordinate();
        }
        return null;
    }
}
