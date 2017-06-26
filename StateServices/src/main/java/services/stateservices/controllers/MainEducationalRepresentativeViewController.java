/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services.stateservices.controllers;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.FlowPane;
import javafx.util.Callback;
import services.stateservices.Main;
import services.stateservices.facade.Facade;
import services.stateservices.facade.Struct;

/**
 * FXML Controller class
 *
 * @author Masha
 */
public class MainEducationalRepresentativeViewController {

    private Facade facade = Main.facade;
    
    public void setup(String user) {
        this.user = user;
        userLabel.setText(this.user);
        policyLabel.setText(facade.getCitizenPolicy(user));
        passportLabel.setText(facade.getCitizenPassport(user));
        birthDatetLabel.setText(facade.getCitizenBirthDate(user));
        emailLabel.setText(facade.getUserEmail(user));
        nameFullColumn.setText(facade.getUserFullName(user));
        /*userLabel.setOnMouseClicked(mouseEvent -> {
            try {
                //Main.showUserView(user);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });*/
        setUpNotificationTable();
        setUpChildTable();
        setUpTicketTable();
        setUpEduRequestTable();
        onClickUpdateButton();
    }

    private void updateNotificationTable() {
        ObservableList<Struct> notifications = null;
        try {
            notifications = FXCollections.observableArrayList(facade.getAllNotificationsForUser(user));
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        notificationTable.setItems(notifications);
    }
        
    @FXML
    private void onClickSignOutButton() {
        facade.signOut(user);
        Main.showSignInView();
    }

    @FXML
    private void onClickUpdateButton() {
        updateNotificationTable();
        updateChildTable();
        updateTicketTable();
        updateEduRequestsTable();
    }
    
    private void setUpEduRequestTable() {
        requestCreationDateColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().get("creationDate")));
        requestStatusColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().get("status")));
        requestChildColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().get("child")));
        requestInstitutionColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().get("institution")));
        requestClassNumberColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().get("classNumber")));
        requestAppointmentColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().get("appointment")));

        requestActionsColumn.setCellValueFactory(new PropertyValueFactory<>("DUMMY"));

        Callback<TableColumn<Struct, String>, TableCell<Struct, String>> cellFactory;
        cellFactory = new Callback<TableColumn<Struct, String>, TableCell<Struct, String>>() {
            @Override
            public TableCell call(final TableColumn<Struct, String> param) {
                final TableCell<Struct, String> cell;
                cell = new TableCell<Struct, String>() {
                    
                    final Button btnAccept = new Button("Accept");
                    final Button btnRemove = new Button("Remove");
                    FlowPane pane = new FlowPane();

                    @Override
                    public void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        pane.getChildren().clear();
                        int index = getIndex();
                        if (index == -1 || getTableView().getItems().size() <= index) {
                            setGraphic(null);
                            setText(null);
                            return;
                        }
                        Struct fields = getTableView().getItems().get(index);
                        if (empty || (fields.get("mustAccept").equals("no") && fields.get("canRemove").equals("no"))) {
                            setGraphic(null);
                            setText(null);
                        } else if (fields.get("mustAccept").equals("yes")) {
                            btnAccept.setOnAction(event -> {
                                boolean ret = facade.acceptEduRequest(user, fields.get("id"));
                                if (ret) {
                                    onClickUpdateButton();
                                } else {
                                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                                    alert.setTitle("Error");
                                    alert.setHeaderText("Error ocured during refusing ticket!");
                                    alert.showAndWait();
                                }
                            });
                            pane.getChildren().add(btnAccept);
                            setGraphic(pane);
                            setText(null);
                        }
                        if (fields.get("canRemove").equals("yes")) {
                            btnRemove.setOnAction(event -> {
                                boolean ret = facade.removeEduRequest(user, fields.get("id"));
                                if (ret) {
                                    onClickUpdateButton();
                                } else {
                                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                                    alert.setTitle("Error");
                                    alert.setHeaderText("Error ocured during refusing ticket!");
                                    alert.showAndWait();
                                }
                            });
                            pane.getChildren().add(btnRemove);
                            setGraphic(pane);
                            setText(null);
                        }
                    }
                };
                return cell;
            }
        };

        requestActionsColumn.setCellFactory(cellFactory);
    }
}
