package victorzinho.music.usecases.atyla.input.dto;

public class YellowBrickRecord {
    private int dtf;
    private float lon;
    private float lat;
    private long at;
    private String pc;

    public int getDtf() {
        return dtf;
    }

    public void setDtf(int dtf) {
        this.dtf = dtf;
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

    public long getAt() {
        return at;
    }

    public void setAt(long at) {
        this.at = at;
    }

    public String getPc() {
        return pc;
    }

    public void setPc(String pc) {
        this.pc = pc;
    }
}
