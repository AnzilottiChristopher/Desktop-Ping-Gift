package com.presence;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

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

    public void setSprite(String imagePath) {
        try {
            Image spriteImage = new Image(Objects.requireNonNull(
                    getClass().getResource(imagePath)).toExternalForm());
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

    public void onPingReceived(String message) {
        // TODO animate sprite, show notification etc
        System.out.println("Partner sent: " + message);
    }

//    public void startListening(String partnerUserId) {
//        client.listenForPing(partnerUserId, message -> {
//            Platform.runLater(() -> onPingReceived(message));
//        });
//    }

    public void reset() {
        this.sprite = null;
        this.status = false;
        this.client = null;
    }
}
