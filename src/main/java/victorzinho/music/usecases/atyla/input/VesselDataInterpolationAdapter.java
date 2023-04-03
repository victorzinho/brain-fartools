package victorzinho.music.usecases.atyla.input;

import org.locationtech.jts.geom.Coordinate;
import victorzinho.music.pointdata.PointDataInterpolator;

import java.util.Date;
import java.util.List;
import java.util.function.Function;

public class VesselDataInterpolationAdapter implements PointDataInterpolator.Adapter<VesselData> {
    @Override
    public List<Function<VesselData, ? extends Number>> getYFunctions() {
        return List.of(
                vesselData -> vesselData.getPosition().x,
                vesselData -> vesselData.getPosition().y,
                VesselData::getSpeed,
                VesselData::getCourse,
                VesselData::getWindAngle,
                VesselData::getWindSpeed,
                VesselData::getWindTemperature
        );
    }

    @Override
    public VesselData build(List<Double> values) {
        VesselData vesselData = new VesselData(new Coordinate(values.get(1), values.get(2)), new Date(values.get(0).longValue() * 1000).toInstant());
        vesselData
                .setWindAngle(values.get(5).intValue())
                .setWindSpeed(values.get(6).floatValue())
                .setWindTemperature(values.get(7).floatValue())
                .setSpeed(values.get(3).floatValue())
                .setCourse(values.get(4).floatValue());
        return vesselData;
    }

    @Override
    public boolean filter(Number yValue) {
        return yValue.doubleValue() >= 0.0;
    }
}
