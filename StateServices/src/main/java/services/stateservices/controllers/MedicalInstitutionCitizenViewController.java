package services.stateservices.controllers;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.application.Platform;
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
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
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
        canAddFeedbacks = false;
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
            disableTabs(null);
        }
        
        if (canAddFeedbacks == true) {
            addFeedbackButton.setVisible(true);
        }
        else {
            addFeedbackButton.setVisible(false);
        }
        
        updateInstitutionTable();
    }
        
    @Override
    protected void addButtonsToTicketTable() {
        ticketActionColumn.setCellValueFactory(new PropertyValueFactory<>("DUMMY"));

        Callback<TableColumn<Struct, String>, TableCell<Struct, String>> cellFactory
                = new Callback<TableColumn<Struct, String>, TableCell<Struct, String>>() {
            @Override
            public TableCell call(final TableColumn<Struct, String> param) {
                final TableCell<Struct, String> cell = new TableCell<Struct, String>() {

                    final Button btnRefuse = new Button("Refuse");
                    final Button btnTake = new Button("Take");

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
                        } else if (fields.get("canRefuse").equals("yes") && fields.get("citizen").equals(facade.getUserFullName(user))) {
                            btnRefuse.setOnAction(event -> {
                                boolean ret = facade.refuseTicketByCitizen(user, fields.get("id"));
                                if (ret) {
                                    onClickUpdateButton();
                                } else {
                                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                                    alert.setTitle("Error");
                                    alert.setHeaderText("Error ocured during refusing the ticket!");
                                    alert.showAndWait();
                                }
                            });
                            setGraphic(btnRefuse);
                            setText(null);
                        } else if (fields.get("canRefuse").equals("yes") && fields.get("visited").equals("no")) {
                            btnTake.setOnAction(event -> {
                                if (userChilds.isEmpty()) {
                                    facade.takeTicket(user, fields.get("id"), 0);
                                    onClickUpdateButton();
                                    return;
                                }
                                Dialog<Integer> dialog = new Dialog<>();
                                dialog.setTitle("Take ticket");

                                // Set the button types.
                                ButtonType buttonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
                                dialog.getDialogPane().getButtonTypes().addAll(buttonType, ButtonType.CANCEL);

                                GridPane gridPane = new GridPane();
                                gridPane.setHgap(10);
                                gridPane.setVgap(10);
                                gridPane.setPadding(new Insets(20, 150, 10, 10));

                                ChoiceBox<Struct> select = new ChoiceBox<Struct>();
                                ObservableList<Struct> names = null;
                                try {
                                    names = FXCollections.observableArrayList(userChilds);
                                    Struct userStruct = new Struct();
                                    userStruct.add("fullName", "me (" + facade.getUserFullName(user) + ")");
                                    userStruct.add("id", "0");
                                    names.add(userStruct);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    return;
                                }
                                select.setItems(names);
                                select.setConverter(new StringConverter<Struct>() {
                                    @Override
                                    public String toString(Struct struct) {
                                        if (struct == null) {
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
                                select.setTooltip(new Tooltip("Select child or you"));

                                gridPane.add(new Label("Select child or you:"), 0, 0);
                                gridPane.add(select, 1, 0);

                                dialog.getDialogPane().setContent(gridPane);

                                // Request focus on the full name field by default.
                                Platform.runLater(() -> select.requestFocus());

                                // Convert the result when the login button is clicked.
                                dialog.setResultConverter(dialogButton -> {
                                    if (dialogButton == buttonType) {
                                        return (select.getValue() != null) ? new Integer(select.getValue().get("id")) : 0;
                                    }
                                    return null;
                                });

                                Optional<Integer> result = dialog.showAndWait();

                                result.ifPresent(childId -> {
                                    boolean ret = facade.takeTicket(user, fields.get("id"), childId);
                                    if (ret) {
                                        onClickUpdateButton();
                                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                                        alert.setTitle("Ticket successfully taken");
                                        alert.setHeaderText("Ticket successfully taken!");
                                        alert.showAndWait();
                                    } else {
                                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                                        alert.setTitle("Error");
                                        alert.setHeaderText("Error during taking ticket!");
                                        alert.showAndWait();
                                    }
                                });
                            });
                            setGraphic(btnTake);
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
    protected void disableTabs(Tab selected) {
        SingleSelectionModel<Tab> selectionModel = tabPane.getSelectionModel();
        selectionModel.select(selected != null ? selected : institutionsTab);
        informationTab.setDisable(true);
        informationTab.getContent().setVisible(false);
        doctorsTab.setDisable(true);
        doctorsTab.getContent().setVisible(false);
        ticketTab.setDisable(true);
        ticketTab.getContent().setVisible(false);
    }
    
    @Override
    protected void enableTabs(Tab selected) {
        SingleSelectionModel<Tab> selectionModel = tabPane.getSelectionModel();
        selectionModel.select(selected != null ? selected : informationTab);
        informationTab.setDisable(false);
        informationTab.getContent().setVisible(true);
        doctorsTab.setDisable(false);
        doctorsTab.getContent().setVisible(true);
        ticketTab.setDisable(false);
        ticketTab.getContent().setVisible(true);
    }
}
