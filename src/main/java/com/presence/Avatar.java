package com.presence;

import javafx.scene.image.*;

import java.util.Objects;

public class Avatar {
    private boolean status;
    private String message;
    private ImageView sprite;

    public Avatar() {
        this.status = true;
        this.message = "";
    }

    public void setStatus() {
        this.status = !this.status;
    }
    public boolean getStatus() {
        return this.status;
    }

    public void setMessage(String message) {
        this.message = message;
    }
    public String getMessage() {
        return this.message;
    }

    public void ping() {
        System.out.println(this.message);
    }

    public void setSprite() {
        try {
            Image spriteImage = new Image(Objects.requireNonNull(getClass().getResource("/assets/me.png")).toExternalForm());
            this.sprite = new ImageView(spriteImage);
        } catch (Exception e) {
            System.err.println("Error loading sprite: " + e.getMessage());
        }
    }

    public ImageView getSprite() {
        return this.sprite;
    }
}

