package victorzinho.music.score;

public abstract class AbstractNamedMusicPart implements NamedMusicPart {
    private final String partName;

    public AbstractNamedMusicPart(String partName) {
        this.partName = partName;
    }

    @Override
    public String getPartName() {
        return this.partName;
    }
}
