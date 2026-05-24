package com.presence;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.Objects;

public class App extends Application {

    public BorderPane basicSetup(Avatar av, Stage stage) {
        av.setSprite();
        av.getSprite().fitWidthProperty().bind(stage.widthProperty().multiply(0.5));
        av.getSprite().fitHeightProperty().bind(stage.heightProperty().multiply(0.5));


        Button button = new Button("♥");
        button.getStyleClass().add("ping-button");
        button.setOnAction(event -> {
            av.setMessage("You Pressed The Button!");
            av.ping();
        });

        VBox vbox = new VBox(10);
        vbox.getChildren().addAll(av.getSprite(), button);
        vbox.setAlignment(Pos.CENTER);

        Pane dot = new Pane();
        dot.getStyleClass().addAll("status-dot", "status-dot-online");

        Label statusText = new Label(av.getStatus() ? "Online" : "Offline");
        statusText.getStyleClass().add("status-text-online");

        HBox statusBox = new HBox();
        statusBox.getStyleClass().add("status-label");
        statusBox.getChildren().addAll(statusText, dot);

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox topBar = new HBox();
        topBar.setPadding(new Insets(10, 20, 0, 0));
        topBar.getChildren().addAll(spacer, statusBox);

        BorderPane root = new BorderPane();
        root.setTop(topBar);
        root.setCenter(vbox);

        return root;
    }

    public void customTitle(Stage stage, String title, BorderPane root) {
        stage.setTitle(title);

        HBox titleBar =  new HBox();
        titleBar.setPadding(new Insets(8, 12, 8, 12));
        titleBar.setAlignment(Pos.CENTER_LEFT);

        final double[] dragDelta = new double[2];
        titleBar.setOnMousePressed(event -> {
            dragDelta[0] = stage.getX() - event.getScreenX();
            dragDelta[1] = stage.getY() - event.getScreenY();
        });
        titleBar.setOnMouseDragged(event -> {
            stage.setX(event.getScreenX() + dragDelta[0]);
            stage.setY(event.getScreenY() + dragDelta[1]);
        });

        Label titleName = new Label(title);

        Button minimizeBtn = new Button("—");
        minimizeBtn.getStyleClass().addAll("title-bar-button");
        Button closeBtn = new Button("✕");
        closeBtn.getStyleClass().addAll("title-bar-button", "close-button");

        minimizeBtn.setOnAction(event -> {stage.setIconified(true);});
        closeBtn.setOnAction(event -> {stage.close();});

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        titleBar.getChildren().addAll(titleName, spacer, minimizeBtn, closeBtn);
        root.setTop(titleBar);
    }

    @Override
    public void start(Stage stage) {
        stage.initStyle(StageStyle.UNDECORATED);
        Avatar av = new Avatar();

        BorderPane startup = basicSetup(av, stage);
        customTitle(stage, "presence", startup);


        Scene scene = new Scene(startup, 600, 500);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/button.css")).toExternalForm());
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/online.css")).toExternalForm());
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles.css")).toExternalForm());
        stage.setScene(scene);
        stage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
