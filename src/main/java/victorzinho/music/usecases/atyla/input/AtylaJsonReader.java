package victorzinho.music.usecases.atyla.input;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.geotools.referencing.GeodeticCalculator;
import org.locationtech.jts.geom.Coordinate;
import victorzinho.music.usecases.atyla.input.dto.MarineTrafficRecord;
import victorzinho.music.usecases.atyla.input.dto.YellowBrickRecord;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

import static java.util.stream.Collectors.toList;

public class AtylaJsonReader {
    public static final int NAUTIC_MILE_IN_M = 1852;
    private static final GeodeticCalculator GEODETIC_CALCULATOR = new GeodeticCalculator();
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public List<VesselData> readFromMarineTraffic(InputStream inputStream) throws IOException {
        return Arrays.stream(MAPPER.readValue(inputStream, MarineTrafficRecord[].class))
                .map(this::toVesselData)
                .collect(toList());
    }

    private VesselData toVesselData(MarineTrafficRecord marineTrafficRecord) {
        Coordinate position = new Coordinate(marineTrafficRecord.getLon(), marineTrafficRecord.getLat());
        VesselData vesselData = new VesselData(position, Instant.parse(marineTrafficRecord.getTimestamp() + "Z"));
        vesselData
                .setWindAngle(marineTrafficRecord.getWindAngle())
                .setWindSpeed(marineTrafficRecord.getWindSpeed())
                .setWindTemperature(marineTrafficRecord.getWindTemperature())
                .setSource(VesselDataSource.MARINE_TRAFFIC)
                .setSpeed(marineTrafficRecord.getSpeed() / 10f)
                .setCourse((float) Math.toRadians(marineTrafficRecord.getCourse()));
        return vesselData;
    }

    public List<VesselData> readFromYellowBrick(InputStream inputStream) throws IOException {
        YellowBrickRecord[] yellowBrickRecordArray = MAPPER.readValue(inputStream, YellowBrickRecord[].class);
        Arrays.sort(yellowBrickRecordArray, Comparator.comparingLong(YellowBrickRecord::getAt));

        List<VesselData> vesselData = new ArrayList<>();
        for (int i = 1; i < yellowBrickRecordArray.length; i++) {
            YellowBrickRecord previous = yellowBrickRecordArray[i - 1];
            YellowBrickRecord current = yellowBrickRecordArray[i];

            if (previous.getAt() == current.getAt()) {
                continue;
            }

            GEODETIC_CALCULATOR.setStartingGeographicPoint(previous.getLon(), previous.getLat());
            GEODETIC_CALCULATOR.setDestinationGeographicPoint(current.getLon(), current.getLat());
            float course = (float) Math.toRadians((GEODETIC_CALCULATOR.getAzimuth() + 360) % 360);

            double meters = GEODETIC_CALCULATOR.getOrthodromicDistance();
            double seconds = Duration.between(new Date(previous.getAt() * 1000).toInstant(),
                    new Date(current.getAt() * 1000).toInstant()).getSeconds();
            float speed = (float) (meters / seconds);
            if (speed < 0.1) {
                speed = 0;
                course = 0;
            }

            Coordinate currentPosition = new Coordinate(current.getLon(), current.getLat());
            VesselData newVesselData = new VesselData(currentPosition, new Date(current.getAt() * 1000).toInstant());
            newVesselData
                    .setWindAngle(-1)
                    .setWindSpeed(-1)
                    .setWindTemperature(-1)
                    .setSource(VesselDataSource.YELLOW_BRICK)
                    .setSpeed(speed)
                    .setCourse(course);
            vesselData.add(newVesselData);
        }

        return vesselData;
    }
}
