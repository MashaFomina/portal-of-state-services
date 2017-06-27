/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services.stateservices.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import services.stateservices.facade.Facade;
import services.stateservices.Main;

import java.io.IOException;
import java.util.Optional;

public class SignInController {

    private Facade facade = Main.facade;

    @FXML private Label loginLabel;
    @FXML private Label passwordLabel;
    @FXML private Label errorLabel;
    @FXML private Label registerLabel;
    @FXML private TextField loginField;
    @FXML private PasswordField passwordField;
    @FXML private Button signInButton;

    @FXML
    private void initialize() {}

    @FXML
    private void onClickSignInButton() {
        String login = loginField.getText();
        String password = passwordField.getText();
        if (login.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Enter login and password");
            return;
        }
        try {
            if (facade.authenticate(login, password)) {
                if(facade.isCitizen(login)) {
                    Main.showMainCitizenView(login);
                }
                if(facade.isEducationalRepresentative(login)) {
                    Main.showEducationalRepresentativeView(login);
                }
                if(facade.isMedicalRepresentative(login)) {
                    Main.showMedicalRepresentativeView(login);
                }
            } else {
                errorLabel.setText("Incorrect login or password");
            }
        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Error connecting to database");
        }
    }

    @FXML
    private void onCLickRegister() throws IOException {
        //Main.showRegisterView();
    }
}
