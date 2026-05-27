package com.presence;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class PartnerAvatar {
    private NetworkClient client;
    private ImageView sprite;
    private boolean status;

    //Animations
    private Map<String, Image> spriteSheets = new HashMap<>();
    private Timeline currentAnimation;
    private int currentFrame = 0;

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
            spriteSheets.put("idle", spriteImage);
            this.sprite = new ImageView(spriteImage);

            loadSheet("offline", this.client.getPartnerOfflineSheet());
            loadSheet("sleep", this.client.getPartnerSleepSheet());
            //TODO load other sprite sheets here
        } catch (Exception e) {
            System.err.println("Error loading sprite: " + e.getMessage());
        }
    }

    public ImageView getSprite() {
        return this.sprite;
    }

    public void playAnimation(String name, int frameWidth, int frameHeight, int totalFrame,
                              double fps, boolean loop) {
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
                    currentFrame = (currentFrame + 1) % totalFrame;
                })
        );
        currentAnimation.setCycleCount(loop ? Timeline.INDEFINITE : totalFrame);
        currentAnimation.play();
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
    public void stopAnimation() {
        if (this.currentAnimation != null) {
            currentAnimation.stop();
            currentAnimation = null;
        }
        sprite.setViewport(null);
        sprite.setImage(spriteSheets.get("idle"));
        currentFrame = 0;
    }
    public void playOfflineSequence() {
        playAnimation("offline",
                client.getPartnerOfflineFrameW(), client.getPartnerOfflineFrameH(),
                4, 2, false, () -> {
                    playAnimation("sleep",
                            client.getPartnerSleepFrameW(), client.getPartnerSleepFrameH(),
                            8, 3, true);
                });
    }

    private void loadSheet(String name, String path) {
        try {
            Image sheet = new Image(Objects.requireNonNull(getClass().getResource(path)).toExternalForm());
            spriteSheets.put(name, sheet);
        } catch (Exception e) {
            System.err.println("Could not load sheet: " + e.getMessage());
        }
    }

    public boolean getStatus() {
        return this.status;
    }

    public void setStatus(boolean status) {
        this.status = status;
        Platform.runLater(() -> {
            if (client == null) return;
            if (status) {
                stopAnimation();
            } else {
                playOfflineSequence();
            }
        });
    }

    public void onPingReceived() {
        // TODO animate sprite, show notification etc
        System.out.println("Ping received from partner");
    }

    public void startListening() {
        this.client.getPartnerStatus(status -> {
            Platform.runLater(() -> setStatus(status.equals("online")));
        });
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
        stopAnimation();
        this.sprite = null;
        this.status = false;
        this.client = null;
    }
}
