package org.vismayb.tplus.core;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.BorderPane;
import javafx.scene.shape.Line;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

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

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            System.out.println("File selected: " + selectedFile.getAbsolutePath());
        } else {
            System.out.println("File selection cancelled.");
            throw new Exception("No file selected");
        }

        NumberAxis timeAxis = new NumberAxis();
        NumberAxis altitudeAxis = new NumberAxis();
        timeAxis.setLabel("Time");
        altitudeAxis.setLabel("Altitude");
        LineChart<Number, Number> lineChart = new LineChart<>(timeAxis, altitudeAxis);
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
        lineChart.getData().add(series);
        root.setCenter(lineChart);

        // Create the scene
        var scene = new Scene(root);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("chart-style.css")).toExternalForm());
        stage.setScene(scene);
        stage.show();
    }
}
