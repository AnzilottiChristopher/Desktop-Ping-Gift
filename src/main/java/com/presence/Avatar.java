package com.presence;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.*;
import javafx.util.Duration;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Avatar {
    private boolean status;
    private String message;
    private ImageView sprite;
    private NetworkClient client;

    //Animations
    private Map<String, Image> spriteSheets = new HashMap<>();
    private Timeline currentAnimation;
    private int currentFrame = 0;

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

    public void setMessage(String message) {
        this.message = message;
    }
    public String getMessage() {
        return this.message;
    }

    public NetworkClient getClient() {
        return client;
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
            spriteSheets.put("idle", spriteImage);
            this.sprite = new ImageView(spriteImage);

            loadSheet("send", this.client.getMySendSheet());
            loadSheet("reaction", this.client.getMyReactionSheet());
        } catch (Exception e) {
            System.err.println("Error loading sprite: " + e.getMessage());
        }
    }

    public ImageView getSprite() {
        return this.sprite;
    }

    public void playAnimation(String name, int frameWidth, int frameHeight, int totalFrame, double fps, boolean loop) {
        if (this.currentAnimation != null) {
            this.currentAnimation.stop();
        }

        Image sheet = spriteSheets.get(name);
        if (sheet == null) {
            System.err.println("No sheet loaded for: " + name);
            return;
        }

        sprite.setImage(sheet);
        currentFrame = 0;

        currentAnimation = new Timeline(
                new KeyFrame(Duration.millis(1000.0 / fps), event -> {
                    int x = currentFrame * frameWidth;
                    sprite.setViewport(new Rectangle2D(x, 0, frameWidth, frameHeight));
                    currentFrame = (currentFrame + 1) % totalFrame;
                })
        );
        currentAnimation.setCycleCount(loop ? Timeline.INDEFINITE : totalFrame);
        currentAnimation.play();
    }
    public void stopAnimation() {
        if (this.currentAnimation != null) {
            currentAnimation.stop();
            currentAnimation = null;
        }
        sprite.setViewport(null);
        sprite.setImage(spriteSheets.get("idle"));
        currentFrame = 0;
    }

    public void playAnimation(String name, int frameWidth, int frameHeight, int totalFrame,
                              double fps, boolean loop, Runnable onFinish) {
        if (this.currentAnimation != null) {
            this.currentAnimation.stop();
        }

        Image sheet = spriteSheets.get(name);
        if (sheet == null) {
            System.err.println("No sheet loaded for: " + name);
            return;
        }

        sprite.setImage(sheet);
        currentFrame = 0;

        sprite.setViewport(new Rectangle2D(0, 0, frameWidth, frameHeight));

        currentAnimation = new Timeline(
                new KeyFrame(Duration.millis(1000.0 / fps), event -> {
                    if (sprite == null) {
                        currentAnimation.stop();
                        return;
                    }
                    int x = currentFrame * frameWidth;
                    sprite.setViewport(new Rectangle2D(x, 0, frameWidth, frameHeight));
                    currentFrame++;
                    if (currentFrame >= totalFrame) {
                        if (loop) {
                            currentFrame = 0;
                        } else {
                            currentAnimation.stop();
                            if (onFinish != null) {
                                onFinish.run();
                            }
                        }
                    }
                })
        );
        currentAnimation.setCycleCount(loop ? Timeline.INDEFINITE : totalFrame);
        currentAnimation.play();
    }
    public void playSendAnimation() {
        playAnimation("send",
                client.getMySendFrameW(), client.getMySendFrameH(),
                6, 4, false, this::stopAnimation);
    }
    public void playReactionAnimation() {
        playAnimation("reaction",
                client.getMyReactionFrameW(), client.getMyReactionFrameH(),
                6, 4, false, this::stopAnimation);
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

    private void loadSheet(String name, String path) {
        try {
            Image sheet = new Image(Objects.requireNonNull(getClass().getResource(path)).toExternalForm());
            spriteSheets.put(name, sheet);
        } catch (Exception e) {
            System.err.println("Could not load sheet: " + e.getMessage());
        }
    }
}

