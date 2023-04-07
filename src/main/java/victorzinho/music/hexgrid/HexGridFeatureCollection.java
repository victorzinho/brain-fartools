package victorzinho.music.hexgrid;

import org.geotools.data.DataUtilities;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.collection.AbstractFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.index.strtree.STRtree;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.diffplug.common.base.Errors.rethrow;
import static org.geotools.referencing.crs.DefaultGeographicCRS.WGS84;

public class HexGridFeatureCollection<T> extends AbstractFeatureCollection implements SimpleFeatureCollection {
    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();

    public static final String ATTR_GEOM = "geom";
    public static final String ATTR_VALUE = "value";

    private final float width;
    private final float height;
    private final ReferencedEnvelope bounds;
    private final HexGridValueProvider<T> hexGridValueProvider;

    /**
     * Creates a new feature collection representing a hexagonal grid. The collection is in WGS84 but the hexagons
     * are calculated in meters using UTM for each cell (possibly switching between different CRS and having some overlaps).
     *
     * @param outerCircleCellSize     The size of each cell, defined as the distance to the center to the outer
     *                                circle containing the whole cell (in meters).
     * @param regionOfInterestInWgs84 The region of interest, in WGS84.
     * @param hexGridValueProvider    The provider of values for the hex grid.
     */
    public HexGridFeatureCollection(
            float outerCircleCellSize, Envelope regionOfInterestInWgs84,
            HexGridValueProvider<T> hexGridValueProvider
    ) {
        super(buildSchema(hexGridValueProvider.getValueClass()));
        this.width = (float) Math.sqrt(3) * outerCircleCellSize; // pointy top orientation
        this.height = 1.5f * outerCircleCellSize;
        this.bounds = new ReferencedEnvelope(regionOfInterestInWgs84, WGS84);
        this.hexGridValueProvider = hexGridValueProvider;
    }

    private static SimpleFeatureType buildSchema(Class<?> outputValueBinding) {
        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.setName("hexGrid");
        builder.add(ATTR_GEOM, Polygon.class);
        builder.add(ATTR_VALUE, outputValueBinding);
        builder.setCRS(WGS84);
        return builder.buildFeatureType();
    }

    @Override
    protected Iterator<SimpleFeature> openIterator() {
        return new HexGridIterator();
    }

    @Override
    public int size() {
        return DataUtilities.count(this);
    }

    @Override
    public ReferencedEnvelope getBounds() {
        return this.bounds;
    }

    private class HexGridIterator implements Iterator<SimpleFeature> {
        private final SimpleFeatureBuilder builder;
        private SimpleFeature next;

        private T previousValueWest;
        private T previousValueSouthEast;
        private T previousValueSouthWest;
        private T previousValue;
        private T westernValueInRow;

        private Coordinate nextPositionUtm;
        private CoordinateReferenceSystem currentCrs;
        private Envelope currentCrsEnvelope;

        private boolean lastRowIndented;
        private Coordinate westernPositionInRow;
        private CoordinateReferenceSystem westernCrsInRow;
        private final Map<String, Coordinate> crsToFirstCoordinateInRow = new HashMap<>();
        private final Map<String, STRtree> crsToIndex = new HashMap<>();

        public HexGridIterator() {
            this.builder = new SimpleFeatureBuilder(getSchema());
            updateCrs(new Coordinate(bounds.getMinX(), bounds.getMinY()));
            this.lastRowIndented = false;
            this.westernPositionInRow = this.nextPositionUtm;
            this.westernCrsInRow = this.currentCrs;
            this.previousValueWest = hexGridValueProvider.getInitialValue();
        }

        @Override
        public boolean hasNext() {
            if (next == null) {
                next = findNext();
            }
            return next != null;
        }

        @Override
        public SimpleFeature next() {
            SimpleFeature ret = next;
            next = null;
            return ret;
        }

        private SimpleFeature findNext() {
            T value;

            if (previousValueSouthWest != null) {
                value = hexGridValueProvider.getValueFromSouthWest(previousValueSouthWest);
            } else if (previousValueSouthEast != null) {
                value = hexGridValueProvider.getValueFromSouthEast(previousValueSouthEast);
            } else if (previousValueWest != null) {
                value = hexGridValueProvider.getValueFromWest(previousValueWest);
            } else {
                return null;
            }

            this.previousValue = value;
            if (this.westernValueInRow == null) {
                this.westernValueInRow = value;
            }

            Polygon hexUtm = GEOMETRY_FACTORY.createPolygon(new Coordinate[]{
                    new Coordinate(this.nextPositionUtm.x - 0.5 * width, this.nextPositionUtm.y - 0.25 * height),
                    new Coordinate(this.nextPositionUtm.x, this.nextPositionUtm.y - 0.5 * height),
                    new Coordinate(this.nextPositionUtm.x + 0.5 * width, this.nextPositionUtm.y - 0.25 * height),
                    new Coordinate(this.nextPositionUtm.x + 0.5 * width, this.nextPositionUtm.y + 0.25 * height),
                    new Coordinate(this.nextPositionUtm.x, this.nextPositionUtm.y + 0.5 * height),
                    new Coordinate(this.nextPositionUtm.x - 0.5 * width, this.nextPositionUtm.y + 0.25 * height),
                    new Coordinate(this.nextPositionUtm.x - 0.5 * width, this.nextPositionUtm.y - 0.25 * height)
            });

            builder.set(ATTR_GEOM, toWgs84(hexUtm));
            builder.set(ATTR_VALUE, value);

            String currentCrsName = currentCrs.getName().toString();
            if (!this.crsToIndex.containsKey(currentCrsName)) {
                this.crsToIndex.put(currentCrsName, new STRtree());
            }
            this.crsToIndex.get(currentCrsName).insert(hexUtm.getEnvelopeInternal(), previousValue);

            updateNextPosition();
            return builder.buildFeature(null);
        }

        private void updateNextPosition() {
            this.previousValueWest = null;
            this.previousValueSouthWest = null;
            this.previousValueSouthEast = null;

            // Try one more hex to the east
            Coordinate coordinate = new Coordinate(nextPositionUtm.x + width, nextPositionUtm.y);
            if (updatePosition(coordinate)) {
                this.previousValueWest = previousValue;
                return;
            }

            // Not contained in bounds, try next row (western hex to the north)
            this.currentCrs = this.westernCrsInRow;
            coordinate = new Coordinate(westernPositionInRow.x, westernPositionInRow.y + 0.75 * height);
            coordinate.x += (lastRowIndented ? -width / 2 : width / 2);

            if (updatePosition(coordinate)) {
                if (this.lastRowIndented) {
                    this.previousValueSouthEast = this.westernValueInRow;
                } else {
                    this.previousValueSouthWest = this.westernValueInRow;
                }

                this.westernPositionInRow = this.nextPositionUtm;
                this.westernCrsInRow = this.currentCrs;
                this.westernValueInRow = null;
                this.lastRowIndented = !this.lastRowIndented;
                return;
            }

            this.nextPositionUtm = null;
        }

        private boolean updatePosition(Coordinate positionUtm) {
            Coordinate positionWgs84 = toWgs84(GEOMETRY_FACTORY.createPoint(positionUtm)).getCoordinate();
            if (!bounds.contains(positionWgs84)) {
                return false;
            }

            if (this.currentCrsEnvelope.contains(positionUtm)) {
                // contained in both current UTM projection and region of interest
                this.nextPositionUtm = positionUtm;
            } else {
                // contained in region of interest, different UTM zone
                updateCrs(positionWgs84);
            }

            return true;
        }

        @SuppressWarnings("unchecked")
        private void updateCrs(Coordinate currentPositionWgs84) {
            this.currentCrs = getUtmCrs(currentPositionWgs84);
            GeographicBoundingBox geographicBbox = (GeographicBoundingBox) this.currentCrs.getDomainOfValidity()
                    .getGeographicElements().stream().filter(extent -> extent instanceof GeographicBoundingBox).findAny()
                    .orElseThrow(() -> new IllegalStateException("Cannot find bbox for CRS"));
            Envelope crsEnvelope = new Envelope(
                    geographicBbox.getEastBoundLongitude(),
                    geographicBbox.getWestBoundLongitude(),
                    geographicBbox.getSouthBoundLatitude(),
                    geographicBbox.getNorthBoundLatitude());
            MathTransform mathTransform = rethrow().wrap(() -> CRS.findMathTransform(WGS84, currentCrs)).get();
            this.currentCrsEnvelope = rethrow().wrap(() -> JTS.transform(crsEnvelope, mathTransform)).get();

            this.nextPositionUtm = rethrow().wrap(() -> JTS.transform(currentPositionWgs84, null, mathTransform)).get();

            // Adjust to grid
            String crsName = this.currentCrs.getName().toString();
            Coordinate firstCoordinate = this.crsToFirstCoordinateInRow.get(crsName);
            if (firstCoordinate != null) {
                double diffX = (this.nextPositionUtm.x - firstCoordinate.x) % width;
                this.nextPositionUtm.x -= diffX;
                this.nextPositionUtm.x += lastRowIndented ? -width / 2 : width / 2;
                this.nextPositionUtm.y = firstCoordinate.y + 0.75 * height;

                if (this.crsToIndex.containsKey(crsName)) {
                    List<?> values = this.crsToIndex.get(crsName)
                            .query(new Envelope(
                                    nextPositionUtm.x - width / 2,
                                    nextPositionUtm.x - width / 2,
                                    nextPositionUtm.y - 0.75 * height,
                                    nextPositionUtm.y - 0.75 * height));
                    if (!values.isEmpty()) {
                        this.previousValueSouthWest = (T) values.get(0);
                    } else {
                        this.previousValueSouthEast = (T) this.crsToIndex.get(crsName)
                                .query(new Envelope(
                                        nextPositionUtm.x + width / 2,
                                        nextPositionUtm.x + width / 2,
                                        nextPositionUtm.y - 0.75 * height,
                                        nextPositionUtm.y - 0.75 * height))
                                .get(0);
                    }
                }
                this.crsToIndex.remove(crsName);
            }

            this.crsToFirstCoordinateInRow.put(crsName, this.nextPositionUtm);
        }

        private Geometry toWgs84(Geometry g) {
            return rethrow().get(() -> JTS.transform(g, CRS.findMathTransform(currentCrs, WGS84, true)));
        }

        private CoordinateReferenceSystem getUtmCrs(Coordinate coordinateWgs84) {
            // UTM divides the world in 60 zones per hemisphere; 6 degrees each, starting on
            // -180 for zone 1
            int zone = 1 + ((int) coordinateWgs84.x + 180) / 6;
            // EPSG:326xx is for northern hemisphere; EPSG:327xx for southern
            int code = (coordinateWgs84.y > 0 ? 32600 : 32700) + zone;
            return rethrow().get(() -> CRS.decode("EPSG:" + code, true));
        }
    }
}
