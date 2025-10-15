package org.vismayb.tplus.core;

import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.input.MouseButton;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.shape.Line;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.w3c.dom.html.HTMLImageElement;

import java.io.*;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * The main class of the application.
 */
public class App extends Application {
    /**
     * The entry point of the application gui.
     * @param stage The primary stage for the application.
     * @throws Exception If an error occurs.
     */
    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("Application");
        stage.setWidth(800);
        stage.setHeight(640);

        // Create the root layout
        var root = new BorderPane();

        /*FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File selectedFile = fileChooser.showOpenDialog(stage);*/
        File selectedFile = new File("D:\\Downloads\\FL0.csv");

        if (selectedFile != null) {
            System.out.println("File selected: " + selectedFile.getAbsolutePath());
        } else {
            System.out.println("File selection cancelled.");
            throw new Exception("No file selected");
        }

        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Time");
        yAxis.setLabel("Altitude");
        LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setLegendVisible(false);
        Map<Double, Double> data = new HashMap<>();

        try (Reader reader = new FileReader(selectedFile)) {
            Iterable<CSVRecord> records = CSVFormat.DEFAULT.builder()
                    .setHeader(
                            "timE",
                            "vertAccel",
                            "vertVelo",
                            "altitude",
                            "apogeeVelo",
                            "apogee",
                            "isFlying",
                            "isCoasting",
                            "accelGravityVertical",
                            "netDrag",
                            "vertDrag",
                            "pos",
                            "extensionPerm"
                    )
                    .setIgnoreEmptyLines(true)
                    .setSkipHeaderRecord(true)
                    .setTrim(true)
                    .get()
                    .parse(reader);


            for (CSVRecord record : records) {
                data.put(Double.parseDouble(record.get("timE")), Double.parseDouble(record.get("altitude")));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        data.forEach((x, y) -> series.getData().add(new XYChart.Data<>(x, y)));

        System.out.println(series.getData().size());
        double dataMinX = series.getData().getFirst().getXValue().doubleValue();
        double dataMaxX = series.getData().getLast().getXValue().doubleValue();

        xAxis.setAutoRanging(true);
        xAxis.setForceZeroInRange(false);
        xAxis.setLowerBound(dataMinX);
        xAxis.setUpperBound(dataMaxX);

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
        plotArea.setOnMouseReleased(e -> lastMouseX[0] = Double.NaN);

        lineChart.getData().add(series);
        lineChart.setCreateSymbols(false);
        root.setCenter(lineChart);

        // Create the scene
        var scene = new Scene(root);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("chart-style.css")).toExternalForm());
        stage.setScene(scene);
        stage.show();
    }
}
