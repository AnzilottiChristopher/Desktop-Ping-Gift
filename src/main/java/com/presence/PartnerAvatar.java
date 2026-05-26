package com.presence;

import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.IOException;
import java.util.Objects;

public class PartnerAvatar {
    private NetworkClient client;
    private ImageView sprite;
    private boolean status;

    public PartnerAvatar(NetworkClient client) {
        this.client = client;
        this.status = false; // starts offline until we know they're connected
    }

    public void setClient(NetworkClient client) {
        this.client = client;
    }

    public void setSprite() {
        try {
            Image spriteImage = new Image(Objects.requireNonNull(
                    getClass().getResource(this.client.getPartnerSprite())).toExternalForm());
            this.sprite = new ImageView(spriteImage);
        } catch (Exception e) {
            System.err.println("Error loading sprite: " + e.getMessage());
        }
    }

    public ImageView getSprite() {
        return this.sprite;
    }

    public boolean getStatus() {
        return this.status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public void onPingReceived() {
        // TODO animate sprite, show notification etc
        System.out.println("Ping received from partner");
    }

    public void startListening() {
        this.client.listenForEvents(event -> {
            String type = event.get("type").getAsString();
            Platform.runLater(() -> {
                if (type.equals("PING")) {
                    onPingReceived();
                }
            });
        });
        this.client.listenForPartnerStatus(status -> {
            Platform.runLater(() -> {
                setStatus(status.equals("online"));
            });
        });
    }

    public void reset() {
        this.sprite = null;
        this.status = false;
        this.client = null;
    }
}
