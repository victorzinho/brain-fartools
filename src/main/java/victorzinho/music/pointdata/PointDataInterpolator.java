package victorzinho.music.pointdata;

import org.apache.commons.math3.analysis.interpolation.LinearInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class PointDataInterpolator<TPointData extends PointData> {
    private final Adapter<TPointData> adapter;

    /**
     * @param adapter An adapter to obtain all relevant Y values, filter them if needed and rebuild the object
     *                for interpolated data (as double[]).
     */
    public PointDataInterpolator(Adapter<TPointData> adapter) {
        this.adapter = adapter;
    }

    /**
     * Interpolates point data in the given time steps.
     *
     * @param data              The list with the elements to interpolate.
     * @param timeStepInSeconds The increment of X for each interpolated value (time in seconds).
     * @return The interpolated data from the first to the last instant in the given data, at the specified time steps.
     */
    public List<TPointData> interpolate(List<TPointData> data, double timeStepInSeconds) {
        // one spline for each Y function
        List<PolynomialSplineFunction> splines = adapter.getYFunctions().stream()
                .map(yFunction -> interpolate(data, yFunction))
                .toList();

        Collections.sort(data);
        List<TPointData> ret = new ArrayList<>();
        double firstInstant = data.get(0).getEpochSecond();
        double lastInstant = data.get(data.size() - 1).getEpochSecond();
        for (double time = firstInstant; time < lastInstant; time += timeStepInSeconds) {
            ret.add(adapter.build(getValuesForInstant(time, splines)));
        }

        return ret;
    }

    private List<Double> getValuesForInstant(double time, List<PolynomialSplineFunction> splines) {
        List<Double> values = new ArrayList<>();
        values.add(time);
        values.addAll(splines.stream().map(spline -> spline.value(time)).toList());
        return values;
    }

    /**
     * Interpolate the values of the elements in the list.
     *
     * @param list      The elements with XY values to interpolate.
     * @param yFunction The function to obtain Y from an element in the list.
     * @return The interpolated spline function
     */
    private PolynomialSplineFunction interpolate(
            List<TPointData> list, Function<TPointData, ? extends Number> yFunction
    ) {
        LinearInterpolator interpolator = new LinearInterpolator();

        List<TPointData> uniqueValuesByX = list.stream()
                // if there are multiple elements with same X, we just pick any and discard the others
                .collect(toMap(PointData::getEpochSecond, Function.identity(), (value1, value2) -> value1))
                .values().stream().sorted().toList();
        List<Long> x = uniqueValuesByX.stream()
                .filter(data -> adapter.filter(yFunction.apply(data)))
                .map(PointData::getEpochSecond).collect(toList());
        List<Number> y = uniqueValuesByX.stream()
                .filter(data -> adapter.filter(yFunction.apply(data)))
                .map(yFunction).collect(toList());

        long firstX = uniqueValuesByX.get(0).getEpochSecond();
        long lastX = uniqueValuesByX.get(uniqueValuesByX.size() - 1).getEpochSecond();
        if (x.get(0) != firstX) {
            x.add(0, firstX);
            y.add(0, y.get(0));
        }
        if (x.get(x.size() - 1) != lastX) {
            x.add(lastX);
            y.add(y.get(y.size() - 1));
        }
        return interpolator.interpolate(
                x.stream().mapToDouble(Long::doubleValue).toArray(),
                y.stream().mapToDouble(Number::doubleValue).toArray());
    }

    public interface Adapter<TPointData extends PointData> {
        /**
         * @return all Y values relevant for the interpolation for a given point data. The order is relevant since the
         * interpolated values will be passed afterwards to the {@link #build(List)} method.
         */
        List<Function<TPointData, ? extends Number>> getYFunctions();

        /**
         * Determines whether a given yValue must be taken into consideration for interpolation or not
         * (such as discarding negative values, etc.)
         *
         * @param yValue The yValue to filter.
         * @return <code>true</code> if the value is valid, <code>false</code> otherwise.
         */
        boolean filter(Number yValue);

        /**
         * Builds a point data object from the given interpolated values.
         *
         * @param interpolatedData A list of interpolated values. The first element will be the X value (instant in seconds).
         *                         The rest of the elements will be the Y values interpolated, in the same order as provided
         *                         by {@link #getYFunctions()}
         * @return The interpolated point data.
         */
        TPointData build(List<Double> interpolatedData);
    }
}
