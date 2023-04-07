package victorzinho.music.usecases.raster;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.opengis.feature.simple.SimpleFeature;
import victorzinho.music.io.GeotiffIO;
import victorzinho.music.io.ShpIO;
import victorzinho.music.score.MusicScoreGenerator;
import victorzinho.music.score.generators.RasterMusicPartGenerator;

import java.io.File;
import java.util.Collections;
import java.util.List;

import static org.geotools.process.classify.ClassificationMethod.NATURAL_BREAKS;
import static victorzinho.music.score.generators.RasterMusicPartGenerator.ALL_PITCH_CLASSES;

public class Main {
    private static final File EXPORT_DIR = new File(System.getProperty("user.home") + "\\Desktop");

    public static void main(String[] args) throws Exception {
        // inputs
        SimpleFeatureCollection points = ShpIO.read(System.getProperty("user.home") + "\\Desktop\\waypoints.shp");
        GridCoverage2D coverage = GeotiffIO.read(System.getProperty("user.home") + "\\Desktop\\eu_dem_v11_E20N20\\eu_dem_v11_E20N20.TIF");

        // part generators
        RasterMusicPartGenerator partGenerator = new RasterMusicPartGenerator("melody", points,
                ALL_PITCH_CLASSES, coverage, NATURAL_BREAKS, null);

        // generate
        MusicScoreGenerator<Void> generator = new MusicScoreGenerator<>(EXPORT_DIR) {
            @Override
            protected Void getData(SimpleFeature feature) {
                return null;
            }
        };
        generator.generateScore(points, List.of(partGenerator), Collections.emptyList());
    }
}
