package com.presence;

import java.io.*;
import java.util.Properties;

public class SessionManager {
    public static void saveSession(String refreshToken) {
        String path = System.getProperty("user.home") + "/presence/session.properties";
        try {
            Properties prop = new Properties();
            prop.setProperty("refreshToken", refreshToken);
            prop.store(new FileOutputStream(path), null);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static String loadSession() {
        String path = System.getProperty("user.home") + "/presence/session.properties";
        try {
            Properties prop = new Properties();
            prop.load(new FileInputStream(path));
            return prop.getProperty("refreshToken");
        } catch (IOException e) {
            return null;
        }
    }
    public static void clearSession() {
        String path = System.getProperty("user.home") + "/presence/session.properties";
        try {
            new File(path).delete();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
