package com.presence;


import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.application.Platform;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;

public class NetworkClient {
    private static final String WEB_API_KEY;
    private static final String DB_URL;

    static{
        try {
            Properties prop = new Properties();
            prop.load(new FileInputStream("config.properties"));
            WEB_API_KEY = prop.getProperty("FIREBASE_API_KEY");
            DB_URL = prop.getProperty("FIREBASE_DB_URL");
        } catch (IOException ex) {
            throw new RuntimeException("Could not load config.properties", ex);
        }
    }

    private String idToken;
    private String userID;

    public void register(String email, String password) throws IOException {
        String url = "https://identitytoolkit.googleapis.com/v1/accounts:signUp?key=" + WEB_API_KEY;
        String body = String.format(
                "{\"email\":\"%s\",\"password\":\"%s\",\"returnSecureToken\":true}",
                email, password
        );
        JsonObject response = sendPost(url, body);
        this.idToken = response.get("idToken").getAsString();
        this.userID = response.get("localId").getAsString();
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
    }

    public void sendPing(String msg) throws IOException {
        String path = "/alerts/" + userID + ".json?auth=" + idToken;
        sendPost(DB_URL + path, toJsonString(msg));
    }
    public String toJsonString(String message) {
        return "\"" + message + "\"";
    }

    public void listenForPing(String otherUserId) {
        Thread listenerThread = new Thread(() -> {
            while(true) {
                try {
                    String path = "/alerts/" + otherUserId + ".json?auth=" + idToken;
                    URL url = new URL(path);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setRequestProperty("Accept", "text/event-stream");
                    connection.setDoInput(true);

                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                    String line;
                    while((line = reader.readLine()) != null) {
                        if (line.startsWith("data:")) {
                            String data = line.substring(5).trim();
                            if (!data.equals("null")) {
                                String message = JsonParser.parseString(data).getAsString();
                                Platform.runLater(() -> {
                                    //TODO Update UI
                                    System.out.println("Ping received: " + message);
                                });
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
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
}
