package services.stateservices.controllers;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;
import javafx.util.StringConverter;
import services.stateservices.facade.Struct;

/**
 * FXML Controller class
 *
 * @author Masha
 */
public class MedicalInstitutionCitizenViewController extends InstitutionsController {       
    public void setup(String user) {
        this.user = user;
        this.isEdu = false;
        setupCities();
        setUpFeedbackTable();
        setUpInstitutionTable();
        setUpDoctorsTable();
        setUpTicketsTable();
        onClickUpdateButton();
    }

    @FXML
    public void onClickUpdateButton() {
        userChilds = facade.getAllChildsForUser(user);
        if (institution > 0) {  
            canAddFeedbacks = facade.canAddFeedbackToMedicalInstitution (user, institution);
            updateDoctorsTable();
            updateTicketsTable();
            updateFeedbackTable();
            updateInstitutionLabels();
            setupDoctorsSelectBoxs();
        }
        else {
            canAddFeedbacks = false;
            disableTabs();
        }
        
        if (institution > 0 && canAddFeedbacks == true) {
            addFeedbackButton.setVisible(true);
        }
        else {
            addFeedbackButton.setVisible(false);
        }
        
        updateInstitutionTable();
    }

    protected void setupDoctorsSelectBoxs() {
        selectDoctor.getItems().clear();
        selectDoctor.setItems(doctors);
        selectDoctor.setConverter(new StringConverter<Struct>() {
              @Override
              public String toString(Struct struct) {
                if (struct == null){
                    return null;
                } else {
                    return struct.get("title");
                }
              }

            @Override
            public Struct fromString(String title) {
                return null;
            }
        });
        selectDoctor.setTooltip(new Tooltip("Select institution"));
        selectDoctor.getSelectionModel().selectedIndexProperty().addListener(
                new ChangeListener<Number>() {
                    @Override
                    public void changed(ObservableValue ov, Number value, Number newValue) {
                        if (!doctors.isEmpty() & newValue.intValue() >= 0) {
                            Struct struct = institutions.get(newValue.intValue());
                            doctor = struct.get("doctorLogin");
                            selectDoctor.setPromptText(struct.get("fullName") + " (" + struct.get("position") + ")");
                            onClickUpdateButton();
                        }
                    }
        });
    }
        
    protected void addButtonsToTicketTable1() {
        ticketActionColumn.setCellValueFactory(new PropertyValueFactory<>("DUMMY"));

        Callback<TableColumn<Struct, String>, TableCell<Struct, String>> cellFactory
                = new Callback<TableColumn<Struct, String>, TableCell<Struct, String>>() {
            @Override
            public TableCell call(final TableColumn<Struct, String> param) {
                final TableCell<Struct, String> cell = new TableCell<Struct, String>() {

                    final Button btnCancel = new Button("Cancel");
                    final Button btnVisited = new Button("Visited");

                    @Override
                    public void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        int index = getIndex();
                        if (index == -1 || getTableView().getItems().size() <= index) {
                            setGraphic(null);
                            setText(null);
                            return;
                        }
                        Struct fields = getTableView().getItems().get(index);
                        if (empty) {
                            setGraphic(null);
                            setText(null);
                        } else if (fields.get("canRefuse").equals("yes")) {
                            btnCancel.setOnAction(event -> {
                                boolean ret = facade.cancelTicketByRepresentative(user, fields.get("id"));
                                if (ret) {
                                    onClickUpdateButton();
                                } else {
                                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                                    alert.setTitle("Error");
                                    alert.setHeaderText("Error ocured during cancelling ticket!");
                                    alert.showAndWait();
                                }
                            });
                            setGraphic(btnCancel);
                            setText(null);
                        } else if (fields.get("canSetVisited").equals("yes") && fields.get("visited").equals("no")) {
                            btnVisited.setOnAction(event -> {
                                TextInputDialog dialog = new TextInputDialog();
                                dialog.setTitle("Enter visit summary");
                                dialog.setHeaderText("Enter visit summary");

                                Optional<String> result = dialog.showAndWait();
                                if (!result.isPresent()) {
                                    return;
                                }

                                boolean ret = facade.setTicketIsVisited(user, fields.get("id"), result.get());
                                if (ret) {
                                    onClickUpdateButton();
                                } else {
                                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                                    alert.setTitle("Error");
                                    alert.setHeaderText("Error ocured during setting visited ticket!");
                                    alert.showAndWait();
                                }
                            });
                            setGraphic(btnVisited);
                            setText(null);
                        }
                    }
                };
                return cell;
            }
        };

        ticketActionColumn.setCellFactory(cellFactory);
    }
        
    @Override
    protected void disableTabs() {
        SingleSelectionModel<Tab> selectionModel = tabPane.getSelectionModel();
        selectionModel.select(institutionsTab);
        informationTab.setDisable(true);
        informationTab.getContent().setVisible(false);
        doctorsTab.setDisable(true);
        doctorsTab.getContent().setVisible(false);
        ticketTab.setDisable(true);
        ticketTab.getContent().setVisible(false);
    }
    
    @Override
    protected void enableTabs() {
        SingleSelectionModel<Tab> selectionModel = tabPane.getSelectionModel();
        selectionModel.select(informationTab);
        informationTab.setDisable(false);
        informationTab.getContent().setVisible(true);
        doctorsTab.setDisable(false);
        doctorsTab.getContent().setVisible(true);
        ticketTab.setDisable(false);
        ticketTab.getContent().setVisible(true);
    }
}
