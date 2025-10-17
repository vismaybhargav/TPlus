package org.vismayb.tplus.views;

import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.input.MouseButton;
import javafx.scene.input.ScrollEvent;

public class GraphView {
    private LineChart<Number, Number> lineChart;

    private NumberAxis xAxis = new NumberAxis();
    private NumberAxis yAxis = new NumberAxis();

    private Node plotArea;
    double dataMinX = 0;
    double dataMaxX = 0;
    public GraphView() {
        lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setLegendVisible(false);
        lineChart.setCreateSymbols(false);

        xAxis.setAutoRanging(false);
        xAxis.setForceZeroInRange(false);

        Node plotArea = lineChart.lookup(".chart-plot-background");

        plotArea.addEventFilter(ScrollEvent.SCROLL, e -> {
            if (e.getDeltaY() == 0) return;

            // Zoom factor: >1 zoom in, <1 zoom out
            double zoomFactor = Math.pow(1.0015, e.getDeltaY()); // smooth & fast
            double mouseXInAxis = xAxis.sceneToLocal(e.getSceneX(), 0).getX();
            Number xValN = xAxis.getValueForDisplay(mouseXInAxis);
            if (xValN == null) return;
            double anchorX = xValN.doubleValue();

            double min = xAxis.getLowerBound();
            double max = xAxis.getUpperBound();

            // Interpolate bounds toward/away from anchor (cursor)
            double newMin = anchorX + (min - anchorX) / zoomFactor;
            double newMax = anchorX + (max - anchorX) / zoomFactor;

            // Keep some minimum span and clamp to data domain
            double minSpan = 1e-9; // avoid collapse
            if (newMax - newMin < minSpan) return;
            newMin = Math.max(newMin, dataMinX);
            newMax = Math.min(newMax, dataMaxX);

            // If clamping inverted, bail
            if (newMax - newMin < minSpan) return;

            xAxis.setLowerBound(newMin);
            xAxis.setUpperBound(newMax);

            e.consume();
        });

        final double[] lastMouseX = {Double.NaN};
        plotArea.setOnMousePressed(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                lastMouseX[0] = e.getX();
            }
        });

        plotArea.setOnMouseDragged(e -> {
            if (e.getButton() != MouseButton.PRIMARY) return;
            if (Double.isNaN(lastMouseX[0])) return;

            double dx = e.getX() - lastMouseX[0];
            lastMouseX[0] = e.getX();

            // Convert dx (pixels) → value delta using current scale
            double pixelToValue = (xAxis.getUpperBound() - xAxis.getLowerBound())
                    / xAxis.getWidth();
            double delta = -dx * pixelToValue; // drag right → move window right

            double newMin = xAxis.getLowerBound() + delta;
            double newMax = xAxis.getUpperBound() + delta;

            // Clamp to data domain
            double span = xAxis.getUpperBound() - xAxis.getLowerBound();
            if (newMin < dataMinX) {
                newMin = dataMinX;
                newMax = dataMinX + span;
            }
            if (newMax > dataMaxX) {
                newMax = dataMaxX;
                newMin = dataMaxX - span;
            }

            xAxis.setLowerBound(newMin);
            xAxis.setUpperBound(newMax);
        });
    }

    public void addSeries(XYChart.Series<Number, Number> series) {
        lineChart.getData().add(series);

        var data = series.getData();
        dataMinX = Math.min(data.getFirst().getXValue().doubleValue(), dataMinX);
        dataMaxX = Math.max(data.getLast().getXValue().doubleValue(), dataMaxX);

        xAxis.setLowerBound(dataMinX);
        xAxis.setUpperBound(dataMaxX);
    }

    public LineChart<Number, Number> getLineChart() {
        return lineChart;
    }
}
