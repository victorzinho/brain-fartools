package victorzinho.music.io;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.opengis.filter.Filter;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Map;

public class ShpIO {
    public static SimpleFeatureCollection read(String path) {
        try {
            URL url = new File(path).toURI().toURL();
            DataStore dataStore = DataStoreFinder.getDataStore(Map.of("url", url));
            return dataStore.getFeatureSource(dataStore.getTypeNames()[0]).getFeatures(Filter.INCLUDE);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
