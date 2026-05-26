package com.presence;

import java.io.*;
import java.util.Properties;

public class SessionManager {
    public static void saveSession(String refreshToken) {
        try {
            Properties prop = new Properties();
            prop.setProperty("refreshToken", refreshToken);
            prop.store(new FileOutputStream("session.properties"), null);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static String loadSession() {
        try {
            Properties prop = new Properties();
            prop.load(new FileInputStream("session.properties"));
            return prop.getProperty("refreshToken");
        } catch (IOException e) {
            return null;
        }
    }
    public static void clearSession() {
        try {
            new File("session.properties").delete();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
