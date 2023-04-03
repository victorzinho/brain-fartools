package victorzinho.music.hexGrid;

/**
 * Provides the rules to produce values on a hexagonal grid by calculating values based on neighbours.
 *
 * @param <T> The type of values of the cells.
 */
public interface HexGridValueProvider<T> {
    /**
     * Returns the value of the cell having the given value to the west.
     *
     * @param west The value to the west of the cell to obtain.
     * @return The value of the cell having the given value to the west.
     */
    T getValueFromWest(T west);

    /**
     * Returns the value of the cell having the given value to the south-west.
     *
     * @param southWest The value to the south-west of the cell to obtain.
     * @return The value of the cell having the given value to the south-west.
     */
    T getValueFromSouthWest(T southWest);

    /**
     * Returns the value of the cell having the given value to the south-east.
     *
     * @param southEast The value to the south-east of the cell to obtain.
     * @return The value of the cell having the given value to the south-east.
     */
    T getValueFromSouthEast(T southEast);

    /**
     * Returns the value of the cell having the given value to the east.
     *
     * @param east The value to the east of the cell to obtain.
     * @return The value of the cell having the given value to the east.
     */
    T getValueFromEast(T east);

    /**
     * Returns the value of the cell having the given value to the north-east.
     *
     * @param northEast The value to the north-east of the cell to obtain.
     * @return The value of the cell having the given value to the north-east.
     */
    T getValueFromNorthEast(T northEast);

    /**
     * Returns the value of the cell having the given value to the north-west.
     *
     * @param northWest The value to the north-west of the cell to obtain.
     * @return The value of the cell having the given value to the north-west.
     */
    T getValueFromNorthWest(T northWest);

    /**
     * @return the initial value of any cell. Needed to start generating the grid.
     */
    T getInitialValue();

    /**
     * @return the type of values in the hexagonal grid.
     */
    Class<T> getValueClass();
}
