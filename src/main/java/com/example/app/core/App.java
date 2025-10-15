package com.example.app.core;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

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

        // Create the label and button
        var label = new Label("");
        var button = new Button("Click me!");

        // Add an action to the button
        button.setOnAction(e -> {
            label.setText("Hello, World!");
        });

        // Add the label and button to the root layout
        root.setCenter(new VBox(label, button));

        // Create the scene
        var scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
}
