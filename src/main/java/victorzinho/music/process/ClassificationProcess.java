package victorzinho.music.process;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.filter.function.RangedClassifier;
import org.geotools.process.classify.ClassificationMethod;
import org.opengis.filter.FilterFactory2;

public class ClassificationProcess {
    private static final FilterFactory2 FILTER_FACTORY = CommonFactoryFinder.getFilterFactory2();

    /**
     * @return a classifier providing classes 0-based, the lowest id means the lowest value
     */
    public RangedClassifier getClassifier(
            SimpleFeatureCollection collection, String attributeName, int nClasses, ClassificationMethod method
    ) {
        String functionName = switch (method) {
            case EQUAL_INTERVAL -> "EqualInterval";
            case QUANTILE -> "Quantile";
            case NATURAL_BREAKS -> "Jenks";
        };
        return  (RangedClassifier) FILTER_FACTORY.function(functionName,
                        FILTER_FACTORY.property(attributeName),
                        FILTER_FACTORY.literal(nClasses))
                .evaluate(collection);
    }
}
