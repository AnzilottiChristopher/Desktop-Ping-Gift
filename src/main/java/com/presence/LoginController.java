package com.presence;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class LoginController {

    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button loginButton;
    @FXML
    private Button registerButton;
    @FXML
    private Label errorLabel;

    private Avatar av;
    private Result<String> result;
    private Consumer<Result<String>> onAuthResult;

    public void setAv(Avatar av) {
        this.av = av;
    }
    public Result<String> getResult() {
        return this.result;
    }
    public void setOnAuthResult(Consumer<Result<String>> onAuthResult) {
        this.onAuthResult = onAuthResult;
    }

    public Result<String> handleAuth(BiFunction<String, String, Result<String>> authAction) {
        String email = emailField.getText();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            return Result.fail("Email or password is empty");
        }

        if (authAction == null) {
            throw new IllegalArgumentException("Function can't be null");
        }
        return authAction.apply(email, password);
    }
    @FXML
    public void onLoginClicked() {
        this.result = handleAuth(av::login);
        if (!result.isSuccess()) {
            errorLabel.setText("Login failed");
            System.out.println(this.result.getError());
        }
        onAuthResult.accept(this.result);
    }
    @FXML
    public void onRegisterClicked() {
        this.result = handleAuth(av::register);
        if (!result.isSuccess()) {
            errorLabel.setText("Register failed");
            System.out.println(this.result.getError());
        }
        onAuthResult.accept(this.result);
    }

}
