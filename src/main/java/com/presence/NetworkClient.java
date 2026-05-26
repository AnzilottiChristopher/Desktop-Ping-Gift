package com.presence;


import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.application.Platform;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;
import java.util.function.Consumer;

public class NetworkClient {
    private static final String WEB_API_KEY;
    private static final String DB_URL;
    private static final String MY_ID;
    private static final String MY_SPRITE;
    private static final String PARTNER_SPRITE;
    private final long startTime = System.currentTimeMillis();


    static{
        try {
            Properties prop = new Properties();
            prop.load(new FileInputStream("config.properties"));
            WEB_API_KEY = prop.getProperty("FIREBASE_API_KEY");
            DB_URL = prop.getProperty("FIREBASE_DB_URL");
            MY_ID = prop.getProperty("MY_ID");
            MY_SPRITE = prop.getProperty("MY_SPRITE");
            PARTNER_SPRITE = prop.getProperty("PARTNER_SPRITE");
        } catch (IOException ex) {
            throw new RuntimeException("Could not load config.properties", ex);
        }
    }

    private String idToken;
    private String userID;
    private String refreshToken;

    public boolean isMe() {
        return userID.equals(MY_ID);
    }

    public String getMySprite() {
        return isMe() ? MY_SPRITE : PARTNER_SPRITE;
    }
    public String getPartnerSprite() {
        return isMe() ? PARTNER_SPRITE : MY_SPRITE;
    }
    public String getPartnerUserID() {
        //TODO When account is created change this to her actual id
        return isMe() ? null : MY_ID;
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
                            String status = JsonParser.parseString(data).getAsString();
                            onStatusChange.accept(status);
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
                                JsonObject events = wrapper.getAsJsonObject("data");
                                if (events == null) return;


                                for (var entry : events.entrySet()) {
                                    JsonObject event = entry.getValue().getAsJsonObject();
                                    if (event.has("to") &&
                                            event.get("to").getAsString().equals(userID)) {

                                        long timestamp = event.get("timestamp").getAsLong();
                                        if (timestamp > startTime) {
                                            onEvent.accept(event);
                                        }

                                        deleteEvent(entry.getKey());
                                    }
                                }
                            } catch (Exception e) {
                                // skip malformed events
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
