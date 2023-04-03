package victorzinho.music.usecases.atyla.input;

import victorzinho.music.usecases.atyla.MusicHarmonyGenerator.AttributeDescriptor;

import java.util.List;

public class VesselDataCalculations {
    public static final String ATTR_WIND_SPEED = "wind_speed";
    public static final String ATTR_WIND_TEMPERATURE = "wind_temperature";

    public static Float getWindSpeed(VesselData vesselData) {
        if (vesselData.getSpeed() == null && vesselData.getWindSpeed() == null) return null;
        return vesselData.getWindSpeed() - (float) Math.sin(Math.toRadians(vesselData.getWindAngle())) * vesselData.getSpeed();
    }

    public static Float getWindTemperature(VesselData vesselData) {
        return vesselData.getWindTemperature();
    }

    public static List<AttributeDescriptor<VesselData, ?>> getAttributes() {
        AttributeDescriptor<VesselData, Float> windSpeed = new AttributeDescriptor<>();
        windSpeed.setNClasses(8)
                .setAttribute(ATTR_WIND_SPEED)
                .setBinding(Float.class)
                .setExtractor(VesselDataCalculations::getWindSpeed);

        AttributeDescriptor<VesselData, Float> windTemperature = new AttributeDescriptor<>();
        windTemperature.setNClasses(10)
                .setAttribute(ATTR_WIND_TEMPERATURE)
                .setBinding(Float.class)
                .setExtractor(VesselDataCalculations::getWindTemperature);
        return List.of(windSpeed, windTemperature);
    }
}
