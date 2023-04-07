package victorzinho.music.usecases.atyla.input;

import org.geotools.referencing.GeodeticCalculator;
import victorzinho.music.pointdata.PointData;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public class SpeedAndCourseCalculator {
    private static final GeodeticCalculator GEODETIC_CALCULATOR = new GeodeticCalculator();

    public static <TPointData extends PointData> List<TPointData> updateSpeedAndCourseIfNeeded(Collection<TPointData> pointData) {
        List<TPointData> pointDataList = new ArrayList<>(pointData);
        pointDataList.sort(Comparator.comparing(PointData::getInstant));

        for (int i = 1; i < pointDataList.size(); i++) {
            TPointData previous = pointDataList.get(i - 1);
            TPointData current = pointDataList.get(i);

            double seconds = Duration.between(previous.getInstant(), current.getInstant()).getSeconds();
            if (seconds <= 0) {
                continue;
            }

            GEODETIC_CALCULATOR.setStartingGeographicPoint(previous.getPosition().x, previous.getPosition().y);
            GEODETIC_CALCULATOR.setDestinationGeographicPoint(current.getPosition().x, current.getPosition().y);
            float course = (float) Math.toRadians((GEODETIC_CALCULATOR.getAzimuth() + 360) % 360);
            double meters = GEODETIC_CALCULATOR.getOrthodromicDistance();
            float speed = (float) (meters / seconds);
            current.setSpeed(speed < 0.1 ? 0f : speed);
            current.setCourse(speed < 0.1 ? 0f : course);
        }

        return pointDataList;
    }
}
