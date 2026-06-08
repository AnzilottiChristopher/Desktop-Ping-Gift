package com.presence;


import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.application.Platform;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;
import java.util.function.Consumer;

public class NetworkClient {
    private static final String WEB_API_KEY;
    private static final String DB_URL;
    private static final String MY_ID;
    private static final String PARTNER_ID;
    private static final String MY_SPRITE;
    private static final String PARTNER_SPRITE;


    //More Sprites
    private static final String MY_OFFLINE_SHEET;
    private static final String MY_SLEEP_SHEET;
    private static final String PARTNER_OFFLINE_SHEET;
    private static final String PARTNER_SLEEP_SHEET;
    private static final String MY_REACTION_SHEET;
    private static final String MY_SEND_SHEET;
    private static final String PARTNER_REACTION_SHEET;
    private static final String PARTNER_SEND_SHEET;

    //Animation Frame Information "Chris's"
    private static final int MY_OFFLINE_FRAME_W;
    private static final int MY_OFFLINE_FRAME_H;
    private static final int MY_SLEEP_FRAME_W;
    private static final int MY_SLEEP_FRAME_H;
    private static final int MY_REACTION_FRAME_W;
    private static final int MY_REACTION_FRAME_H;
    private static final int MY_SEND_FRAME_W;
    private static final int MY_SEND_FRAME_H;

    //Animation Frame Information "Danielle's"
    private static final int PARTNER_OFFLINE_FRAME_W;
    private static final int PARTNER_OFFLINE_FRAME_H;
    private static final int PARTNER_SLEEP_FRAME_W;
    private static final int PARTNER_SLEEP_FRAME_H;
    private static final int PARTNER_REACTION_FRAME_W;
    private static final int PARTNER_REACTION_FRAME_H;
    private static final int PARTNER_SEND_FRAME_W;
    private static final int PARTNER_SEND_FRAME_H;



    static{
        try {
            String home = System.getProperty("user.home");
            String presencePath = home + "/presence/";
            File presenceDir = new File(presencePath);
            if(!presenceDir.exists()){
                presenceDir.mkdir();
            }

            Properties prop = new Properties();
            prop.load(new FileInputStream(presencePath + "config.properties"));
            WEB_API_KEY = prop.getProperty("FIREBASE_API_KEY");
            DB_URL = prop.getProperty("FIREBASE_DB_URL");
            MY_ID = prop.getProperty("MY_ID");
            PARTNER_ID = prop.getProperty("PARTNER_ID");

            prop.load(new FileInputStream(presencePath + "sprite.properties"));

            MY_SPRITE = prop.getProperty("MY_SPRITE");
            PARTNER_SPRITE = prop.getProperty("PARTNER_SPRITE");
            MY_OFFLINE_SHEET = prop.getProperty("MY_OFFLINE_SHEET");
            MY_SLEEP_SHEET = prop.getProperty("MY_SLEEP_SHEET");
            PARTNER_OFFLINE_SHEET = prop.getProperty("PARTNER_OFFLINE_SHEET");
            PARTNER_SLEEP_SHEET = prop.getProperty("PARTNER_SLEEP_SHEET");
            MY_REACTION_SHEET = prop.getProperty("MY_REACTION_SHEET");
            MY_SEND_SHEET = prop.getProperty("MY_SEND_SHEET");
            PARTNER_REACTION_SHEET = prop.getProperty("PARTNER_REACTION_SHEET");
            PARTNER_SEND_SHEET = prop.getProperty("PARTNER_SEND_SHEET");

            // Frame Information
            MY_OFFLINE_FRAME_W = Integer.parseInt(prop.getProperty("MY_OFFLINE_FRAME_W"));
            MY_OFFLINE_FRAME_H = Integer.parseInt(prop.getProperty("MY_OFFLINE_FRAME_H"));
            MY_SLEEP_FRAME_W = Integer.parseInt(prop.getProperty("MY_SLEEP_FRAME_W"));
            MY_SLEEP_FRAME_H = Integer.parseInt(prop.getProperty("MY_SLEEP_FRAME_H"));
            MY_REACTION_FRAME_W = Integer.parseInt(prop.getProperty("MY_REACTION_FRAME_W"));
            MY_REACTION_FRAME_H = Integer.parseInt(prop.getProperty("MY_REACTION_FRAME_H"));
            MY_SEND_FRAME_W = Integer.parseInt(prop.getProperty("MY_SEND_FRAME_W"));
            MY_SEND_FRAME_H = Integer.parseInt(prop.getProperty("MY_SEND_FRAME_H"));
            PARTNER_OFFLINE_FRAME_W = Integer.parseInt(prop.getProperty("PARTNER_OFFLINE_FRAME_W"));
            PARTNER_OFFLINE_FRAME_H = Integer.parseInt(prop.getProperty("PARTNER_OFFLINE_FRAME_H"));
            PARTNER_SLEEP_FRAME_W = Integer.parseInt(prop.getProperty("PARTNER_SLEEP_FRAME_W"));
            PARTNER_SLEEP_FRAME_H = Integer.parseInt(prop.getProperty("PARTNER_SLEEP_FRAME_H"));
            PARTNER_REACTION_FRAME_W = Integer.parseInt(prop.getProperty("PARTNER_REACTION_FRAME_W"));
            PARTNER_REACTION_FRAME_H = Integer.parseInt(prop.getProperty("PARTNER_REACTION_FRAME_H"));
            PARTNER_SEND_FRAME_W = Integer.parseInt(prop.getProperty("PARTNER_SEND_FRAME_W"));
            PARTNER_SEND_FRAME_H = Integer.parseInt(prop.getProperty("PARTNER_SEND_FRAME_H"));
        } catch (IOException ex) {
            throw new RuntimeException("Could not load config.properties or sprite.properties", ex);
        }
    }

    private String idToken;
    private String userID;
    private String refreshToken;

    public boolean isMe() {
        return userID.equals(MY_ID);
    }



    //////////////////////////// ANIMATION STUFF THAT SHOULD BE ITS OWN CLASS //////////////////////////////////////////
    public String getMyReactionSheet() {
        return isMe() ? MY_REACTION_SHEET : PARTNER_REACTION_SHEET;
    }
    public String getMySendSheet() {
        return isMe() ? MY_SEND_SHEET : PARTNER_SEND_SHEET;
    }
    public String getPartnerReactionSheet() {
        return isMe() ? PARTNER_REACTION_SHEET : MY_REACTION_SHEET;
    }
    public String getPartnerSendSheet() {
        return isMe() ? PARTNER_SEND_SHEET : MY_SEND_SHEET;
    }
    public int getMyReactionFrameW() {
        return isMe() ? MY_REACTION_FRAME_W : PARTNER_REACTION_FRAME_W;
    }
    public int getMyReactionFrameH() {
        return isMe() ? MY_REACTION_FRAME_H : PARTNER_REACTION_FRAME_H;
    }
    public int getMySendFrameW() {
        return isMe() ? MY_SEND_FRAME_W : PARTNER_SEND_FRAME_W;
    }
    public int getMySendFrameH() {
        return isMe() ? MY_SEND_FRAME_H : PARTNER_SEND_FRAME_H;
    }
    public int getPartnerReactionFrameW() {
        return isMe() ? PARTNER_REACTION_FRAME_W : MY_REACTION_FRAME_W;
    }
    public int getPartnerReactionFrameH() {
        return isMe() ? PARTNER_REACTION_FRAME_H : MY_REACTION_FRAME_H;
    }
    public int getPartnerSendFrameW() {
        return isMe() ? PARTNER_SEND_FRAME_W : MY_SEND_FRAME_W;
    }
    public int getPartnerSendFrameH() {
        return isMe() ? PARTNER_SEND_FRAME_H : MY_SEND_FRAME_H;
    }

    public String getMySprite() {
        return isMe() ? MY_SPRITE : PARTNER_SPRITE;
    }
    public String getPartnerSprite() {
        return isMe() ? PARTNER_SPRITE : MY_SPRITE;
    }
    public String getPartnerOfflineSheet() {
        return isMe() ? PARTNER_OFFLINE_SHEET : MY_OFFLINE_SHEET;
    }
    public String getPartnerSleepSheet() {
        return isMe() ? PARTNER_SLEEP_SHEET : MY_SLEEP_SHEET;
    }
    public int getPartnerOfflineFrameW() {
        return isMe() ? PARTNER_OFFLINE_FRAME_W : MY_OFFLINE_FRAME_W;
    }

    public int getPartnerOfflineFrameH() {
        return isMe() ? PARTNER_OFFLINE_FRAME_H : MY_OFFLINE_FRAME_H;
    }

    public int getPartnerSleepFrameW() {
        return isMe() ? PARTNER_SLEEP_FRAME_W : MY_SLEEP_FRAME_W;
    }

    public int getPartnerSleepFrameH() {
        return isMe() ? PARTNER_SLEEP_FRAME_H : MY_SLEEP_FRAME_H;
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////


    public String getPartnerUserID() {
        return isMe() ? PARTNER_ID : MY_ID;
    }
    public String getUserID() {
        return userID;
    }
    public String getRefreshToken() {
        return this.refreshToken;
    }
    public void register(String email, String password) throws IOException {
        String url = "https://identitytoolkit.googleapis.com/v1/accounts:signUp?key=" + WEB_API_KEY;
        String body = String.format(
                "{\"email\":\"%s\",\"password\":\"%s\",\"returnSecureToken\":true}",
                email, password
        );
        JsonObject response = sendPost(url, body);
        this.idToken = response.get("idToken").getAsString();
        this.userID = response.get("localId").getAsString();
        this.refreshToken = response.get("refreshToken").getAsString();
    }
    public void login(String email, String password) throws IOException {
        String url = "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=" + WEB_API_KEY;
        String body = String.format(
                "{\"email\":\"%s\",\"password\":\"%s\",\"returnSecureToken\":true}",
                email, password
        );
        JsonObject response = sendPost(url, body);
        this.idToken = response.get("idToken").getAsString();
        this.userID = response.get("localId").getAsString();
        this.refreshToken = response.get("refreshToken").getAsString();
    }

    public void loginWithRefreshToken(String refreshToken) throws IOException {
        String url = "https://securetoken.googleapis.com/v1/token?key=" + WEB_API_KEY;
        String body = "grant_type=refresh_token&refresh_token=" + refreshToken;

        URL urlObj = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) urlObj.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setDoOutput(true);
        conn.getOutputStream().write(body.getBytes());

        String response = new String(conn.getInputStream().readAllBytes());
        JsonObject json = JsonParser.parseString(response).getAsJsonObject();
        this.idToken = json.get("id_token").getAsString();
        this.userID = json.get("user_id").getAsString();
    }

    public void setMyStatus(String status) throws IOException {
        String path = "/users/" + userID + "/status.json?auth=" + idToken;
        sendPut(DB_URL + path, toJsonString(status));
    }
    public String toJsonString(String message) {
        return "\"" + message + "\"";
    }

    public void listenForPartnerStatus(Consumer<String> onStatusChange) {
        Thread listenerThread = new Thread(() -> {
            try {
                String partnerID = getPartnerUserID();
                if(partnerID == null) return;

                String path = DB_URL + "/users/" + partnerID + "/status.json?auth=" + idToken;
                URL url = new URL(path);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "text/event-stream");

                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                String line;
                while ((line = in.readLine()) != null) {
                    if (line.startsWith("data:")) {
                        String data = line.substring(5).trim();
                        if (!data.equals("null")) {
                            JsonObject wrapper = JsonParser.parseString(data).getAsJsonObject();
                            JsonElement dataElement = wrapper.get("data");
                            if (dataElement != null && dataElement.isJsonPrimitive()) {
                                String status = dataElement.getAsString();
                                onStatusChange.accept(status);
                            }
                        }
                    }
                }
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        listenerThread.setDaemon(true);
        listenerThread.start();
    }
    public void getPartnerStatus(Consumer<String> onStatus) {
        new Thread(() -> {
            try {
                String path = DB_URL + "/users/" + getPartnerUserID() + "/status.json?auth=" + idToken;
                String response = sendGet(path);
                String status = JsonParser.parseString(response).getAsString();
                onStatus.accept(status);
            } catch (Exception e) {
                onStatus.accept("offline");
            }
        }).start();
    }

    public void sendEvent(String type) throws IOException {
        String eventId = "event_" + System.currentTimeMillis();
        String path = "/events/" + eventId + ".json?auth=" + idToken;
        String body = String.format(
                "{\"type\":\"%s\",\"from\":\"%s\",\"to\":\"%s\",\"timestamp\":%d}",
                type, userID, getPartnerUserID(), System.currentTimeMillis()
        );
        sendPut(DB_URL + path, body);
    }

    public void listenForEvents(Consumer<JsonObject> onEvent) {
        Thread listenerThread = new Thread(() -> {
            try {
                String path = DB_URL + "/events.json?auth=" + idToken;
                URL url = new URL(path);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "text/event-stream");
                conn.setDoInput(true);
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("data:")) {
                        String data = line.substring(5).trim();
                        if (!data.equals("null")) {
                            try {
                                JsonObject wrapper = JsonParser.parseString(data).getAsJsonObject();
                                String eventPath = wrapper.get("path").getAsString();
                                JsonElement dataElement = wrapper.get("data");

                                if (dataElement == null || dataElement.isJsonNull()) continue;

                                if (eventPath.equals("/")) {
                                    JsonObject events = dataElement.getAsJsonObject();
                                    for (var entry : events.entrySet()) {
                                        JsonObject event = entry.getValue().getAsJsonObject();
                                        if (event.has("to") &&
                                                event.get("to").getAsString().equals(userID)) {
                                            onEvent.accept(event);
                                            deleteEvent(entry.getKey());
                                        }
                                    }
                                } else {
                                    String eventId = eventPath.substring(1);
                                    JsonObject event = dataElement.getAsJsonObject();
                                    if (event.has("to") &&
                                            event.get("to").getAsString().equals(userID)) {
                                        onEvent.accept(event);
                                        deleteEvent(eventId);
                                    }
                                }
                            } catch (Exception e) {
                                System.err.println("Parse error: " + e.getMessage());
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    private JsonObject sendPost(String urlString, String body) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        conn.getOutputStream().write(body.getBytes());

        String response = new String(conn.getInputStream().readAllBytes());
        return JsonParser.parseString(response).getAsJsonObject();
    }
    private void sendPut(String urlString, String body) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("PUT");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        conn.getOutputStream().write(body.getBytes());
        conn.getInputStream().readAllBytes();
    }
    private String sendGet(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        return new String(conn.getInputStream().readAllBytes());
    }
    private void deleteEvent(String eventId) {
        try {
            String path = DB_URL + "/events/" + eventId + ".json?auth=" + idToken;
            URL url = new URL(path);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("DELETE");
            conn.getResponseCode();
            conn.disconnect();
        } catch (IOException e) {
            System.err.println("Failed to delete event: " + e.getMessage());
        }
    }
}
