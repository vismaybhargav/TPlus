package org.vismayb.tplus.core;

import com.interactivemesh.jfx.importer.obj.ObjModelImporter;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.input.MouseButton;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.shape.Mesh;
import javafx.scene.shape.MeshView;
import javafx.stage.Stage;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.fxyz3d.importers.Importer3D;
import org.fxyz3d.utils.CameraTransformer;
import org.vismayb.tplus.views.GraphView;

import java.io.*;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * The main class of the application.
 */
public class App extends Application {
    Map<Double, Double> data = new TreeMap<>();
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

        if (State.getInstance().file != null) {
            System.out.println("File selected: " + State.getInstance().file.getAbsolutePath());
            loadCSV();
        } else {
            System.out.println("File selection cancelled.");
            throw new Exception("No file selected");
        }

        var series = new XYChart.Series<Number, Number>();
        data.forEach((x, y) -> System.out.println(x + " " + y));
        data.forEach((x, y) -> series.getData().add(new XYChart.Data<>(x, y)));


        //var graphView = new GraphView();
        //graphView.addSeries(series);
        //root.setCenter(graphView.getLineChart());
        ObjModelImporter importer = new ObjModelImporter();

        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setNearClip(0.1);
        camera.setFarClip(10000.0);
        camera.setTranslateZ(-500);
        camera.setFieldOfView(20);

        CameraTransformer cameraTransformer = new CameraTransformer();
        cameraTransformer.getChildren().add(camera);
        cameraTransformer.ry.setAngle(-30.0);
        cameraTransformer.rx.setAngle(-15.0);

        // Create the scene
        var scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    private void loadCSV() {
        try (Reader reader = new FileReader(State.getInstance().file)) {
            State.getInstance().logRecords = CSVFormat.DEFAULT.builder()
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

            for (CSVRecord record : State.getInstance().logRecords) {
                var timE = Double.parseDouble(record.get("timE"));
                var altitude = Double.parseDouble(record.get("altitude"));
                System.out.println(timE + " " + altitude);
                data.put(timE, altitude);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
