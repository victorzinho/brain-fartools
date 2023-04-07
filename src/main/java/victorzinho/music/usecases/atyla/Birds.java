package victorzinho.music.usecases.atyla;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.opengis.feature.simple.SimpleFeature;
import victorzinho.music.hexgrid.HexGridPitchClassProvider;
import victorzinho.music.io.ShpIO;
import victorzinho.music.pointdata.PointData;
import victorzinho.music.pointdata.PointDataInterpolator;
import victorzinho.music.usecases.atyla.input.SpeedAndCourseCalculator;

import java.io.File;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class Birds {
    private static final File EXPORT_DIR = new File("C:\\Users\\vicgonco\\Desktop");
    private static final boolean writeIntermediateFiles = true;

    public static void main(String[] args) throws Exception {
        // define hex grid
        HexGridPitchClassProvider hexGridProvider = new HexGridPitchClassProvider(2, 3, 5);
        float hexGridSize = 1000;

        // obtain your input data somehow
        List<PointData> pointData = getPointData();

        Envelope hexGridEnvelope = new Envelope();
        pointData.forEach(p -> hexGridEnvelope.expandToInclude(p.getPosition()));
        hexGridEnvelope.expandBy(1e-2, 1e-2);

        // define the interpolation
        PointDataInterpolator.DefaultAdapter adapter = new PointDataInterpolator.DefaultAdapter() {
            @Override
            public boolean filter(Number yValue) {
                return yValue != null && yValue.doubleValue() > 0.0;
            }
        };
        int interpolationStepInSeconds = 10 * 60;

        // run!
        MusicHarmonyGenerator<PointData> generator = new MusicHarmonyGenerator<>(hexGridProvider,
                hexGridSize, hexGridEnvelope, EXPORT_DIR, writeIntermediateFiles);
        generator.generateScore(pointData, adapter, interpolationStepInSeconds, null);
    }

    private static List<PointData> getPointData() {
        SimpleFeatureCollection birdPositions = ShpIO.read(new File(EXPORT_DIR, "Satloc test 2.shp"));
        List<PointData> pointData = new ArrayList<>();
        try (SimpleFeatureIterator iterator = birdPositions.features()) {
            while (iterator.hasNext()) {
                SimpleFeature next = iterator.next();
                Coordinate position = ((Geometry) next.getDefaultGeometry()).getCoordinate();
                Instant instant = Instant.parse(next.getAttribute("timestamp").toString().replace(" ", "T") + "Z");
                pointData.add(new PointData(position, instant));
            }
        }
        return SpeedAndCourseCalculator.updateSpeedAndCourseIfNeeded(pointData);
    }
}
