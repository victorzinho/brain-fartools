package victorzinho.music.pointdata;

import org.locationtech.jts.geom.Coordinate;

import java.time.Instant;

public class PointData implements Comparable<PointData> {
    private final Coordinate position;
    private final Instant instant;

    private Float speed;
    private Float course;

    /**
     * @param position In wgs84
     * @param instant  Instant for the point data.
     */
    public PointData(Coordinate position, Instant instant) {
        this.position = position;
        this.instant = instant;
    }

    public Coordinate getPosition() {
        return position;
    }

    public Instant getInstant() {
        return instant;
    }

    public long getEpochSecond() {
        return instant.getEpochSecond();
    }

    public Float getSpeed() {
        return this.speed;
    }

    public PointData setSpeed(float speed) {
        this.speed = speed;
        return this;
    }

    public Float getCourse() {
        return this.course;
    }

    public PointData setCourse(float course) {
        this.course = course;
        return this;
    }

    @Override
    public int compareTo(PointData pointData) {
        return instant.compareTo(pointData.instant);
    }
}
