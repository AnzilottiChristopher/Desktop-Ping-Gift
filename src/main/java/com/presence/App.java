package com.presence;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.util.Objects;

public class App extends Application {

    public Result<String> login(Avatar av) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
            Parent root = loader.load();

            LoginController controller = loader.getController();
            controller.setAv(av);

            Stage loginStage = new Stage();
            controller.setOnAuthResult(result -> {
                if (result.isSuccess())
                    loginStage.close();
            });

            loginStage.setScene(new Scene(root));
            loginStage.showAndWait();

            return controller.getResult();
        } catch (IOException e) {
            return Result.fail(e.getMessage());
        }
    }

    public BorderPane basicSetup(Avatar av, Stage stage, PartnerAvatar pav) {
        av.setSprite();
        av.getSprite().fitWidthProperty().bind(stage.widthProperty().multiply(0.5));
        av.getSprite().fitHeightProperty().bind(stage.heightProperty().multiply(0.5));

        pav.setSprite();
        pav.getSprite().fitWidthProperty().bind(stage.widthProperty().multiply(0.5));
        pav.getSprite().fitHeightProperty().bind(stage.heightProperty().multiply(0.5));

        HBox spriteBox = new  HBox(5);
        spriteBox.getChildren().addAll(av.getSprite(), pav.getSprite());
        spriteBox.setAlignment(Pos.CENTER);


        Button button = new Button("♥");
        button.getStyleClass().add("ping-button");
        button.setOnAction(event -> {
            av.setMessage("You Pressed The Button!");
            av.ping();
        });

        VBox vbox = new VBox(10);
        vbox.getChildren().addAll(spriteBox, button);
        vbox.setAlignment(Pos.CENTER);

        Pane dot = new Pane();
        dot.getStyleClass().addAll("status-dot", "status-dot-online");


        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        BorderPane root = new BorderPane();
        root.setCenter(vbox);

        return root;
    }

    public void customTitle(Stage stage, String title, BorderPane root, Avatar av, PartnerAvatar pav) {
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
        closeBtn.setOnAction(event -> {
            av.setStatus(false);
            stage.close();
        });

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button logoutButton = new Button("Logout");
        logoutButton.getStyleClass().addAll("title-bar-button");
        logoutButton.setOnAction(event -> {
            SessionManager.clearSession();
            av.reset();
            av.setStatus(false);
            pav.reset();
            resetClients(stage, av, pav);
        });

        titleBar.getChildren().addAll(titleName, logoutButton, spacer, minimizeBtn, closeBtn);
        root.setTop(titleBar);
    }

    private void resetClients(Stage stage, Avatar av, PartnerAvatar pav) {
        Result<String> loginResult = login(av);
        if (loginResult != null && !loginResult.isSuccess()) {
            NetworkClient client = new NetworkClient();
            av.setClient(client);
            av.setStatus(true);
            pav.setClient(client);
            showMainScreen(stage, av, pav);
        } else {
            Platform.exit();
        }
    }

    private void showMainScreen(Stage stage, Avatar av, PartnerAvatar pav) {
        BorderPane startup = basicSetup(av, stage, pav);
        customTitle(stage, "", startup, av, pav);

        Scene scene = new Scene(startup, 600, 500);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/button.css")).toExternalForm());
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/online.css")).toExternalForm());
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles.css")).toExternalForm());
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void start(Stage stage) {
        stage.initStyle(StageStyle.UNDECORATED);
        NetworkClient client = new NetworkClient();
        Avatar av = new Avatar(client);
        PartnerAvatar pav = new PartnerAvatar(client);

        String refreshToken = SessionManager.loadSession();
        if (refreshToken != null) {
            Result<String> result = av.login(refreshToken);
            if (result != null && result.isSuccess()) {
                av.setStatus(true);
                pav.startListening();
                showMainScreen(stage, av, pav);
                return;
            }
        }

        Result<String> result = login(av);
        if (result != null && result.isSuccess()) {
            av.setStatus(true);
            pav.startListening();
            showMainScreen(stage, av, pav);
        }
    }



    public static void main(String[] args) {
        launch(args);
    }
}
