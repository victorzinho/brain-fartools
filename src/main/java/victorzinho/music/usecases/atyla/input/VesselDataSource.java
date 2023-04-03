package victorzinho.music.usecases.atyla.input;

public enum VesselDataSource {
    MARINE_TRAFFIC("marine_traffic"),
    YELLOW_BRICK("yellow_brick");

    private final String value;

    VesselDataSource(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
