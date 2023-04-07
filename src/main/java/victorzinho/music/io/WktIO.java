package victorzinho.music.io;

import org.apache.commons.io.IOUtils;
import org.geotools.geometry.jts.WKTReader2;
import org.locationtech.jts.geom.Geometry;

import java.io.FileReader;

public class WktIO {
    private static final WKTReader2 WKT = new WKTReader2();

    @SuppressWarnings({"unchecked", "unused"})
    public static <T> T read(String path, Class<? extends Geometry> geometryType) {
        try {
            return (T) WKT.read(IOUtils.toString(new FileReader(path)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
