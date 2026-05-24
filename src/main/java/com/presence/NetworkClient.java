package com.presence;


import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class NetworkClient {
    private static final String WEB_API_KEY = "Insert Here";
    private static final String DB_URL = "https://desktop-ping-default-rtdb.firebaseio.com";

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
        this.userID = response.get("userID").getAsString();
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
        String path = "/alerts/" + userId + ".json?auth=" + idToken;
        sendPost(DB_URL + path, toJsonString(msg));
    }
    public String toJsonString(String message) {
        return "\"" + message + "\"";
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
}
