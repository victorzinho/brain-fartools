package victorzinho.music.usecases.atyla;

import org.locationtech.jts.geom.Envelope;
import victorzinho.music.hexGrid.HexGridPitchClassProvider;
import victorzinho.music.usecases.atyla.input.*;

import java.io.*;
import java.util.List;
import java.util.Optional;

public class Main {
    private static final File EXPORT_ROOT = new File("C:\\Users\\vicgonco\\Desktop");
    private static final boolean writeIntermediateFiles = true;

    public static void main(String[] args) throws Exception {
        // define hex grid
        HexGridPitchClassProvider hexGridProvider = new HexGridPitchClassProvider(5, 4, 9);
        float hexGridSize = AtylaJsonReader.NAUTIC_MILE_IN_M * 2;
        Envelope hexGridEnvelope = new Envelope(3, 12.5, 55, 61);

        // obtain your input data somehow
        List<VesselData> pointData = getPointData();

        // define the interpolation
        VesselDataInterpolationAdapter adapter = new VesselDataInterpolationAdapter();
        int interpolationStepInSeconds = 60 * 60; // every hour

        // define extra descriptors if needed
        var descriptors = VesselDataCalculations.getAttributes();

        // run!
        MusicHarmonyGenerator<VesselData> generator = new MusicHarmonyGenerator<>(true, EXPORT_ROOT);
        generator.generateScore(hexGridProvider, hexGridSize, hexGridEnvelope,
                pointData, adapter, interpolationStepInSeconds, descriptors);
    }

    private static List<VesselData> getPointData() throws IOException {
        InputStream marineTrafficData = AtylaJsonReader.class.getResourceAsStream("/atyla_marine_traffic.json");
        InputStream yellowBrickData = AtylaJsonReader.class.getResourceAsStream("/atyla_yellow_brick.json");

        AtylaJsonReader atylaJsonReader = new AtylaJsonReader();
        List<VesselData> allData = atylaJsonReader.readFromMarineTraffic(marineTrafficData);
        allData.addAll(atylaJsonReader.readFromYellowBrick(yellowBrickData));
        writeCsv(allData, new File(EXPORT_ROOT, "atyla_all_points.csv"));
        return allData;

    }

    private static void writeCsv(List<VesselData> data, File file) throws IOException {
        if (!writeIntermediateFiles) return;

        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        writer.write("timestamp,position,speed,course,wind_angle,wind_speed,wind_temperature,source");
        writer.newLine();
        for (VesselData vesselData : data) {
            writer.write(String.join(",",
                    vesselData.getInstant().toString(),
                    vesselData.getPosition().toString(),
                    Optional.ofNullable(vesselData.getSpeed()).map(Object::toString).orElse("null"),
                    Optional.ofNullable(vesselData.getCourse()).map(Object::toString).orElse("null"),
                    Optional.ofNullable(vesselData.getWindAngle()).map(Object::toString).orElse("null"),
                    Optional.ofNullable(vesselData.getWindSpeed()).map(Object::toString).orElse("null"),
                    Optional.ofNullable(vesselData.getWindTemperature()).map(Object::toString).orElse("null"),
                    Optional.ofNullable(vesselData.getSource()).map(VesselDataSource::getValue).orElse("null")));
            writer.newLine();
        }
        writer.close();
    }
}
