package com.presence;

import javafx.scene.image.*;

import java.io.IOException;
import java.util.Objects;

public class Avatar {
    private boolean status;
    private String message;
    private ImageView sprite;
    private NetworkClient client;

    public Avatar(NetworkClient client) {
        this.status = false;
        this.message = "";
        this.client = client;
    }

    public void setStatus(boolean status) {
        try {
            this.status = status;
            this.client.setMyStatus(this.status ? "online" : "offline");
        } catch (IOException e) {
            System.err.println("Status write failed: " + e.getMessage());
        }
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
        try {
            this.client.sendEvent("PING");
        } catch (IOException e) {
            System.err.println("Failed to send PING event");
        }
    }

    public void setSprite() {
        try {
            Image spriteImage = new Image(Objects.requireNonNull(getClass().getResource(this.client.getMySprite())).toExternalForm());
            this.sprite = new ImageView(spriteImage);
        } catch (Exception e) {
            System.err.println("Error loading sprite: " + e.getMessage());
        }
    }

    public ImageView getSprite() {
        return this.sprite;
    }

    public Result<String> login(String email, String password) {
        try {
            this.client.login(email, password);
            return Result.ok("Login Successful");
        } catch (IOException e) {
            return Result.fail(e.getMessage());
        }
    }
    public String getRefreshToken() {
        return this.client.getRefreshToken();
    }
    public Result<String> login(String refreshToken) {
        try {
            this.client.loginWithRefreshToken(refreshToken);
            return Result.ok("Login Successful");
        } catch (IOException e) {
            return Result.fail(e.getMessage());
        }
    }
    public Result<String> register(String email, String password) {
        try {
            this.client.register(email, password);
            return Result.ok("Register Successful");
        } catch (IOException e) {
            return Result.fail(e.getMessage());
        }
    }

    public void setClient(NetworkClient client) {
        this.client = client;
    }

    public void reset() {
        this.status = false;
        this.message = "";
        this.sprite = null;
        this.client = null;
    }
}

