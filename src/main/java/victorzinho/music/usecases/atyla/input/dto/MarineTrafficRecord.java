package victorzinho.music.usecases.atyla.input.dto;

import com.fasterxml.jackson.annotation.JsonProperty;


public class MarineTrafficRecord {
    private int mmsi;
    private int status;
    private float speed;
    private float lon;
    private float lat;
    private int course;
    private int heading;
    private String timestamp;
    @JsonProperty("ship_id")
    private int shipId;
    @JsonProperty("wind_angle")
    private int windAngle;
    @JsonProperty("wind_speed")
    private float windSpeed;
    @JsonProperty("wind_temperature")
    private float windTemperature;

    public int getMmsi() {
        return mmsi;
    }

    public void setMmsi(int mmsi) {
        this.mmsi = mmsi;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public float getLon() {
        return lon;
    }

    public void setLon(float lon) {
        this.lon = lon;
    }

    public float getLat() {
        return lat;
    }

    public void setLat(float lat) {
        this.lat = lat;
    }

    public int getCourse() {
        return course;
    }

    public void setCourse(int course) {
        this.course = course;
    }

    public int getHeading() {
        return heading;
    }

    public void setHeading(int heading) {
        this.heading = heading;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public int getShipId() {
        return shipId;
    }

    public void setShipId(int shipId) {
        this.shipId = shipId;
    }

    public int getWindAngle() {
        return windAngle;
    }

    public void setWindAngle(int windAngle) {
        this.windAngle = windAngle;
    }

    public float getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(float windSpeed) {
        this.windSpeed = windSpeed;
    }

    public float getWindTemperature() {
        return windTemperature;
    }

    public void setWindTemperature(float windTemperature) {
        this.windTemperature = windTemperature;
    }
}
