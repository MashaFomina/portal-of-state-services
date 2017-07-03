/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services.stateservices.controllers;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.util.Callback;
import services.stateservices.Main;
import services.stateservices.facade.Facade;
import services.stateservices.facade.Struct;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.control.ListView;
import javafx.scene.control.ListCell;
import javafx.util.StringConverter;
import javafx.scene.layout.StackPane;

/**
 * FXML Controller class
 *
 * @author Masha
 */
public class EducationalInstitutionCitizenViewController extends InstitutionsController {
    List<Integer> classesWithFreeSeats = new ArrayList<Integer>();
    
    @FXML
    private Button makeRequestButton;
        
    public void setup(String user) {
        this.user = user;
        this.isEdu = true;
        setupCities();
        setUpSeatsTable();
        setUpFeedbackTable();
        setUpInstitutionTable();
        onClickUpdateButton();
    }

    @FXML
    public void onClickUpdateButton() {
        userChilds = facade.getAllChildsForUser(user);
        if (institution > 0) {
            addFeedbackButton.setVisible(true);
            updateSeatsTable();
            updateFeedbackTable();
            updateInstitutionLabels();
            classesWithFreeSeats = facade.getClassesWithFreeSeatsForEducationalInstitution(institution);
        }
        else {
            addFeedbackButton.setVisible(false);
            SingleSelectionModel<Tab> selectionModel = tabPane.getSelectionModel();
            selectionModel.select(institutionsTab);
            informationTab.setDisable(true);
            informationTab.getContent().setVisible(false);
        }
        
        if (userChilds.isEmpty() || classesWithFreeSeats.isEmpty()) {
            makeRequestButton.setVisible(false);
        } else {
            makeRequestButton.setVisible(true);
        }
        
        updateInstitutionTable();
        
        InstitutionsController.setFocusRefresh(seatsTable);
        InstitutionsController.setFocusRefresh(feedbackTable);
        InstitutionsController.setFocusRefresh(institutionTable);
    }
    
    @FXML
    private void onClickMakeRequestButton(MouseEvent event) {
        if (institution < 1 || userChilds.isEmpty() || classesWithFreeSeats.isEmpty()) return;
        
        Dialog<List<Integer>> dialog = new Dialog<>();
        dialog.setTitle("Add educational request");

        // Set the button types.
        ButtonType buttonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(buttonType, ButtonType.CANCEL);

        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(20, 150, 10, 10));

        ChoiceBox<Struct> selectChild = new ChoiceBox<Struct>();
        ObservableList<Struct> childs = null;
        try {
            childs = FXCollections.observableArrayList(userChilds);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        selectChild.setItems(childs);
        selectChild.setConverter(new StringConverter<Struct>() {
              @Override
              public String toString(Struct struct) {
                if (struct == null){
                  return null;
                } else {
                  return struct.get("fullName");
                }
              }

            @Override
            public Struct fromString(String fullName) {
                return null;
            }
        });
        selectChild.setTooltip(new Tooltip("Select child"));
        
        ChoiceBox<Integer> selectClass = new ChoiceBox<>();
        ObservableList<Integer> classes = null;
        try {
            classes = FXCollections.observableArrayList(classesWithFreeSeats);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        selectClass.setItems(classes);
        selectClass.setTooltip(new Tooltip("Select class"));
        
   
        gridPane.add(new Label("Select child:"), 0, 0);
        gridPane.add(selectChild, 1, 0);
        gridPane.add(new Label("Select class:"), 0, 1);
        gridPane.add(selectClass, 1, 1);

        dialog.getDialogPane().setContent(gridPane);

        // Request focus on the full name field by default.
        Platform.runLater(() -> selectChild.requestFocus());

        // Convert the result when the login button is clicked.
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == buttonType) {
                List<Integer> fields = new ArrayList<>();
                fields.add((selectChild.getValue() != null) ? new Integer(selectChild.getValue().get("id")) : 0);
                fields.add((selectClass.getValue() != null) ? new Integer(selectClass.getValue().toString()) : 0);
                return fields;
            }
            return null;
        });

        Optional<List<Integer>> result = dialog.showAndWait();

        result.ifPresent(fields -> {
            if (fields.get(0) > 0 && fields.get(1) > 0) {
                boolean ret = facade.addEduRequest(user, institution, fields.get(0), fields.get(1));
                if (ret) {
                    onClickUpdateButton();
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Request successfully added");
                    alert.setHeaderText("Educational request successfully added!");
                    alert.showAndWait();
                } else {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Error");
                    alert.setHeaderText("Error during saving educational request or connect error! May be all seats in class are busy! Or request in institution already exists or child already enrolled in some institution! Child can be enrolled just in one institution!");
                    alert.getDialogPane().setMaxWidth(600);
                    alert.showAndWait();
                }
            } else {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Error");
                alert.setHeaderText("You must select child and class number!");
                alert.showAndWait();
            }
        });
    }
}
