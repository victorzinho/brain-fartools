package victorzinho.music.pointdata;

import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.feature.collection.BaseSimpleFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.referencing.GeodeticCalculator;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import java.time.Duration;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.geotools.referencing.crs.DefaultGeographicCRS.WGS84;

public class PointDataFeatureCollection<TPointData extends PointData> extends BaseSimpleFeatureCollection {
    public static final String ATTR_GEOM = "geom";
    public static final String ATTR_COURSE = "course";
    public static final String ATTR_SPEED = "speed";

    private static final GeodeticCalculator GEODETIC_CALCULATOR = new GeodeticCalculator();
    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();

    private final Supplier<Iterator<TPointData>> pointDataSupplier;
    private final List<? extends AttributeDescriptor<TPointData, ?>> attributeDescriptors;
    private final SimpleFeatureBuilder builder;

    @SuppressWarnings({"rawtypes", "unchecked"})
    public PointDataFeatureCollection(
            Supplier<Iterator<TPointData>> pointDataSupplier,
            List<? extends AttributeDescriptor<TPointData, ?>> attributeDescriptors
    ) {
        super(buildSchema((List) attributeDescriptors));
        this.pointDataSupplier = pointDataSupplier;
        this.attributeDescriptors = attributeDescriptors;
        this.builder = new SimpleFeatureBuilder(getSchema());
    }

    private static SimpleFeatureType buildSchema(List<AttributeDescriptor<?, ?>> attributeDescriptors) {
        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.setName("instant_data");
        builder.add(ATTR_GEOM, Point.class);
        builder.add(ATTR_COURSE, Integer.class); // radians
        builder.add(ATTR_SPEED, Double.class); // m/s
        for (AttributeDescriptor<?, ?> descriptor : attributeDescriptors) {
            builder.add(descriptor.attribute, descriptor.getBinding());
        }
        builder.setCRS(WGS84);

        return builder.buildFeatureType();
    }

    @Override
    public SimpleFeatureIterator features() {
        return new CalculatedSpeedAndCourseIterator(pointDataSupplier.get());
    }

    private class CalculatedSpeedAndCourseIterator implements SimpleFeatureIterator {
        private final Iterator<TPointData> pointDataIterator;

        private SimpleFeature nextFeature;
        private PointData previousPointData;

        public CalculatedSpeedAndCourseIterator(Iterator<TPointData> pointDataIterator) {
            this.pointDataIterator = pointDataIterator;
        }

        @Override
        public boolean hasNext() {
            return findNext() != null;
        }

        @Override
        public SimpleFeature next() throws NoSuchElementException {
            findNext();
            SimpleFeature ret = this.nextFeature;
            this.nextFeature = null;
            return ret;
        }

        private SimpleFeature findNext() {
            while (this.nextFeature == null && pointDataIterator.hasNext()) {
                TPointData pointDataNext = pointDataIterator.next();
                setupGeodeticCalculator(pointDataNext);
                builder.set(ATTR_GEOM, GEOMETRY_FACTORY.createPoint(pointDataNext.getPosition()));
                builder.set(ATTR_SPEED, getSpeed(pointDataNext));
                builder.set(ATTR_COURSE, getCourse(pointDataNext));
                for (AttributeDescriptor<TPointData, ?> descriptor : attributeDescriptors) {
                    builder.set(descriptor.attribute, descriptor.getExtractor().apply(pointDataNext));
                }
                this.nextFeature = builder.buildFeature(null);
                this.previousPointData = pointDataNext;
            }

            return this.nextFeature;
        }

        private Float getSpeed(TPointData current) {
            if (current.getSpeed() != null) return current.getSpeed();
            if (previousPointData == null) return null;
            double meters = GEODETIC_CALCULATOR.getOrthodromicDistance();
            double seconds = Duration.between(this.previousPointData.getInstant(), current.getInstant()).getSeconds();
            return (float) (meters / seconds);
        }

        private Float getCourse(TPointData current) {
            if (current.getCourse() != null) return current.getCourse();
            if (previousPointData == null) return null;
            return (float) Math.toRadians((GEODETIC_CALCULATOR.getAzimuth() + 360) % 360);
        }

        private void setupGeodeticCalculator(PointData currentPointData) {
            if (previousPointData == null) return;
            Coordinate previous = previousPointData.getPosition();
            Coordinate current = currentPointData.getPosition();
            GEODETIC_CALCULATOR.setStartingGeographicPoint(previous.x, previous.y);
            GEODETIC_CALCULATOR.setDestinationGeographicPoint(current.x, current.y);
        }

        @Override
        public void close() {
            // do nothing
        }
    }

    public static class AttributeDescriptor<TPointData, TBinding> {
        private String attribute;
        private Class<TBinding> binding;
        private Function<TPointData, TBinding> extractor;

        public String getAttribute() {
            return attribute;
        }

        public AttributeDescriptor<TPointData, TBinding> setAttribute(String attribute) {
            this.attribute = attribute;
            return this;
        }

        public Class<TBinding> getBinding() {
            return binding;
        }

        public AttributeDescriptor<TPointData, TBinding> setBinding(Class<TBinding> binding) {
            this.binding = binding;
            return this;
        }

        public Function<TPointData, TBinding> getExtractor() {
            return extractor;
        }

        public AttributeDescriptor<TPointData, TBinding> setExtractor(Function<TPointData, TBinding> extractor) {
            this.extractor = extractor;
            return this;
        }
    }
}
