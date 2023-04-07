package victorzinho.music.process;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.data.DataUtilities;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.feature.collection.DecoratingSimpleFeatureCollection;
import org.geotools.feature.collection.DecoratingSimpleFeatureIterator;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Coordinate;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.geometry.DirectPosition;
import org.opengis.referencing.operation.MathTransform;

import static com.diffplug.common.base.Errors.rethrow;
import static victorzinho.music.score.MusicScoreGenerator.getCoordinate;

public class WithCoverageValueCollection extends DecoratingSimpleFeatureCollection {
    public static final String ATTR_COVERAGE_VALUE = "value_from_coverage";

    private final SimpleFeatureType schema;
    private final GridCoverage2D coverage;
    private final MathTransform mathTransform;

    public WithCoverageValueCollection(SimpleFeatureCollection delegate, GridCoverage2D coverage) {
        super(delegate);

        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.init(delegate.getSchema());
        builder.add(ATTR_COVERAGE_VALUE, Double.class);
        this.schema = builder.buildFeatureType();

        this.coverage = coverage;
        this.mathTransform = rethrow().get(() -> CRS.findMathTransform(getSchema().getCoordinateReferenceSystem(),
                coverage.getCoordinateReferenceSystem()));
    }

    @Override
    public SimpleFeatureType getSchema() {
        return schema;
    }

    @Override
    public SimpleFeatureIterator features() {
        return new WithCoverageValueIterator(super.features());
    }

    private class WithCoverageValueIterator extends DecoratingSimpleFeatureIterator {
        public WithCoverageValueIterator(SimpleFeatureIterator iterator) {
            super(iterator);
        }

        @Override
        public SimpleFeature next() {
            SimpleFeature next = super.next();

            SimpleFeature retyped = DataUtilities.reType(schema, next);

            Coordinate coordinate = getCoordinate(retyped);
            if (coordinate == null) return retyped;

            Coordinate coordinateInCoverageCrs = rethrow().get(() -> JTS.transform(coordinate, null, mathTransform));
            DirectPosition position = new DirectPosition2D(coordinateInCoverageCrs.x, coordinateInCoverageCrs.y);
            retyped.setAttribute(ATTR_COVERAGE_VALUE, coverage.evaluate(position, (double[]) null)[0]);
            return retyped;
        }
    }
}
