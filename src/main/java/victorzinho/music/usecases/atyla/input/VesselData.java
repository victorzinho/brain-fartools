package victorzinho.music.usecases.atyla.input;

import org.locationtech.jts.geom.Coordinate;
import victorzinho.music.pointdata.PointData;

import java.time.Instant;

/**
 * A class representing all data of the vessel in a given instant.
 */
public class VesselData extends PointData {
    private Integer windAngle; // degrees
    private Float windSpeed; // knots
    private Float windTemperature; // celsius
    private VesselDataSource source;

    public VesselData(Coordinate position, Instant instant) {
        super(position, instant);
    }

    public Integer getWindAngle() {
        return windAngle;
    }

    public VesselData setWindAngle(int windAngle) {
        this.windAngle = windAngle;
        return this;
    }

    public Float getWindSpeed() {
        return windSpeed;
    }

    public VesselData setWindSpeed(float windSpeed) {
        this.windSpeed = windSpeed;
        return this;
    }

    public Float getWindTemperature() {
        return windTemperature;
    }

    public VesselData setWindTemperature(float windTemperature) {
        this.windTemperature = windTemperature;
        return this;
    }

    public VesselDataSource getSource() {
        return source;
    }

    public VesselData setSource(VesselDataSource source) {
        this.source = source;
        return this;
    }
}
