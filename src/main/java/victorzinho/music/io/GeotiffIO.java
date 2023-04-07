package victorzinho.music.io;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.geotools.gce.geotiff.GeoTiffWriter;

import java.io.File;
import java.io.IOException;

public class GeotiffIO {
    public static GridCoverage2D read(String path) {
        try {
            return new GeoTiffReader(new File(path)).read(null);
        } catch (IOException e) {
            throw new RuntimeException("Cannot read geotiff", e);
        }
    }

    public static void write(GridCoverage2D coverage, String path) {
        try {
            new GeoTiffWriter(new File(path)).write(coverage, null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
