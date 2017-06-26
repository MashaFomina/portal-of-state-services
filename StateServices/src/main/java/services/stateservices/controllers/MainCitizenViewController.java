/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services.stateservices.controllers;

import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ResourceBundle;
import javafx.fxml.Initializable;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseButton;
import services.stateservices.Main;
import services.stateservices.facade.Facade;
import services.stateservices.facade.Struct;
import services.stateservices.entities.*;
import java.util.Optional;
import javafx.scene.input.MouseEvent;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;
import javafx.application.Platform;
import java.util.ArrayList;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback; 
import javafx.scene.layout.FlowPane;

/**
 * FXML Controller class
 *
 * @author Masha
 */
public class MainCitizenViewController {

    private String user;
    private Facade facade = Main.facade;

    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    DateFormat dateFormatBirthDate = new SimpleDateFormat("yyyy-MM-dd");

    @FXML private Label userLabel;
    @FXML private Button updateButton;
    @FXML private Button signOutButton;
    @FXML private TabPane tabs;
    
    @FXML private Tab ticketTab;
    @FXML
    private TableView<Struct> ticketTable;
    @FXML
    private TableColumn<Struct, String> ticketDateColumn;
    @FXML
    private TableColumn<Struct, String> ticketInstitutionColumn;
    @FXML
    private TableColumn<Struct, String> ticketDoctorColumn;
    @FXML
    private TableColumn<Struct, String> ticketChildColumn;
    @FXML
    private TableColumn<Struct, String> ticketVisitedColumn;
    @FXML
    private TableColumn<Struct, String> ticketSummary;
    @FXML
    private TableColumn<Struct, String> ticketActionColumn;

    @FXML
    private TableView<Struct> notificationTable;
    @FXML
    private TableColumn<Struct, String> notificationDateColumn;
    @FXML
    private TableColumn<Struct, String> notificationTextColumn;
    @FXML
    private TableView<Struct> childTable;
    @FXML
    private TableColumn<Struct, String> fullNameColumn;
    @FXML
    private TableColumn<Struct, String> birthCertificateColumn;
    @FXML
    private TableColumn<Struct, String> birthDateColumn;
    @FXML
    private TableColumn<Struct, String> childDeleteColumn;
    @FXML
    private Label policyLabel;
    @FXML
    private Label passportLabel;
    @FXML
    private Label birthDatetLabel;
    @FXML
    private Label emailLabel;
    @FXML
    private Label nameFullColumn;
    @FXML
    private Tab eduRequestsTab;
    @FXML
    private Button addChildButton;
    @FXML
    private TableView<Struct> eduRequestTable;
    @FXML
    private TableColumn<Struct, String> requestCreationDateColumn;
    @FXML
    private TableColumn<Struct, String> requestStatusColumn;
    @FXML
    private TableColumn<Struct, String> requestChildColumn;
    @FXML
    private TableColumn<Struct, String> requestInstitutionColumn;
    @FXML
    private TableColumn<Struct, String> requestClassNumberColumn;
    @FXML
    private TableColumn<Struct, String> requestAppointmentColumn;
    @FXML
    private TableColumn<Struct, String> requestActionsColumn;
    @FXML
    private Button takeTicketButton;
    @FXML
    private Button makeEduRequestButton;


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

    @FXML
    private void onClickAddChildButton() {
        Dialog<Struct> dialog = new Dialog<>();
        dialog.setTitle("Enter information about child");

        // Set the button types.
        ButtonType loginButtonType = new ButtonType("OK", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(20, 150, 10, 10));

        TextField fullName = new TextField();
        fullName.setPromptText("Full name");
        TextField birthCertificate = new TextField();
        birthCertificate.setPromptText("Birth certificate");
        TextField birthDate = new TextField();
        birthCertificate.setPromptText("Birth date");

        gridPane.add(new Label("Full name:"), 0, 0);
        gridPane.add(fullName, 1, 0);
        gridPane.add(new Label("Birth certificate:"), 2, 0);
        gridPane.add(birthCertificate, 3, 0);
        gridPane.add(new Label("Birth date:"), 4, 0);
        gridPane.add(birthDate, 5, 0);

        dialog.getDialogPane().setContent(gridPane);

        // Request focus on the full name field by default.
        Platform.runLater(() -> fullName.requestFocus());

        // Convert the result when the login button is clicked.
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                Struct fields = new Struct();
                fields.add("fullName", fullName.getText());
                fields.add("birthCertificate", birthCertificate.getText());
                fields.add("birthDate", birthDate.getText());    
                return fields;
            }
            return null;
        });

        Optional<Struct> result = dialog.showAndWait();

        result.ifPresent(fields -> {
            System.out.println(fields.get("fullName") + " " + fields.get("birthCertificate") + " " + fields.get("birthDate"));
            if (fields.get("fullName").length() > 0 && fields.get("birthCertificate").length() > 0 && fields.get("birthDate").length() > 0) {
                boolean ret = facade.addChild(user, fields.get("fullName"), fields.get("birthCertificate"), fields.get("birthDate"));
                if (ret) {
                    onClickUpdateButton();
                } else {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Error");
                    alert.setHeaderText("Child with birth certificate \"" + fields.get("birthCertificate") + "\" may be already exists or invalid birth date or connect error!");
                    alert.showAndWait();
                }
            }
            else {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Error");
                alert.setHeaderText("You must fill all fields!");
                alert.showAndWait();
            }
        });
        /*TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Enter information");
        dialog.setHeaderText("Enter full name");

        Optional<String> result = dialog.showAndWait();
        if (!result.isPresent()) return;

        boolean added = false;*/
        /*try {
            added = facade.createProject(user, result.get());
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Error");
            alert.setHeaderText(e.getMessage());
            alert.showAndWait();
        }*/

        /*if (added) {
            onClickUpdateButton();
        } else {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Error");
            alert.setHeaderText("Project with name \"" + result.get() + "\" already exists");
            alert.showAndWait();
        }*/
    }


    private void setUpNotificationTable() {
        notificationDateColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().get("date")));
        notificationTextColumn.setCellValueFactory(cell -> {
            try {
                return new SimpleStringProperty(cell.getValue().get("notification"));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return new SimpleStringProperty("");
        });
    }

    private void setUpChildTable() {
        fullNameColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().get("fullName")));
        birthCertificateColumn.setCellValueFactory((TableColumn.CellDataFeatures<Struct, String> cell) -> {
            try {
                return new SimpleStringProperty(cell.getValue().get("birthCertificate"));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return new SimpleStringProperty("");
        });
        birthDateColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().get("birthDate")));
        fullNameColumn.setCellFactory(col -> {
            return onClickChildAction();
        });
        birthCertificateColumn.setCellFactory(col -> {
            return onClickChildAction();
        });
        birthDateColumn.setCellFactory(col -> {
            return onClickChildAction();
        });
        
        //TableColumn deleteCol = new TableColumn("Delete");
        childDeleteColumn.setCellValueFactory(new PropertyValueFactory<>("DUMMY"));

        Callback<TableColumn<Struct, String>, TableCell<Struct, String>> cellFactory
                = new Callback<TableColumn<Struct, String>, TableCell<Struct, String>>() {
            @Override
            public TableCell call(final TableColumn<Struct, String> param) {
                final TableCell<Struct, String> cell = new TableCell<Struct, String>() {

                    final Button btn = new Button("Delete");

                    @Override
                    public void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                            setText(null);
                        } else {
                            btn.setOnAction(event -> {
                                Struct fields = getTableView().getItems().get(getIndex());
                                System.out.println("Child id: " + fields.get("id"));
                                boolean ret = facade.deleteChild(user, fields.get("id"));
                                if (ret) {
                                    onClickUpdateButton();
                                } else {
                                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                                    alert.setTitle("Error");
                                    alert.setHeaderText("Error ocured during deleting child!");
                                    alert.showAndWait();
                                }
                            });
                            setGraphic(btn);
                            setText(null);
                        }
                    }
                };
                return cell;
            }
        };

        childDeleteColumn.setCellFactory(cellFactory);
    }

    private void setUpTicketTable() {
        ticketDateColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().get("date")));
        ticketInstitutionColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().get("institution")));
        ticketDoctorColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().get("doctor")));
        ticketChildColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().get("child")));
        ticketVisitedColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().get("visited")));
        ticketSummary.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().get("summary")));

        ticketActionColumn.setCellValueFactory(new PropertyValueFactory<>("DUMMY"));

        Callback<TableColumn<Struct, String>, TableCell<Struct, String>> cellFactory
                = new Callback<TableColumn<Struct, String>, TableCell<Struct, String>>() {
            @Override
            public TableCell call(final TableColumn<Struct, String> param) {
                final TableCell<Struct, String> cell = new TableCell<Struct, String>() {

                    final Button btn = new Button("Refuse");

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
                        if (empty || fields.get("canRefuse").equals("no")) {
                            setGraphic(null);
                            setText(null);
                        } else {
                            btn.setOnAction(event -> {
                                boolean ret = facade.refuseTicket(user, fields.get("id"));
                                if (ret) {
                                    onClickUpdateButton();
                                } else {
                                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                                    alert.setTitle("Error");
                                    alert.setHeaderText("Error ocured during refusing ticket!");
                                    alert.showAndWait();
                                }
                            });
                            setGraphic(btn);
                            setText(null);
                        }
                    }
                };
                return cell;
            }
        };

        ticketActionColumn.setCellFactory(cellFactory);
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
        
    private TableCell<Struct, String> onClickChildAction() {
        final TableCell<Struct, String> cell = new TableCell<>();
        cell.textProperty().bind(cell.itemProperty());
        cell.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                try {
                    Dialog<Boolean> dialog = new Dialog<>();
                    dialog.setTitle("Do you want delete child with name \"" + cell.getTableView().getItems().get(cell.getIndex()).get("fullName") + "\"?");

                    // Set the button types.
                    ButtonType deleteButtonType = new ButtonType("Delete", ButtonData.OK_DONE);
                    dialog.getDialogPane().getButtonTypes().addAll(deleteButtonType, ButtonType.CANCEL);

                    // Convert the result to a username-password-pair when the login button is clicked.
                    dialog.setResultConverter(dialogButton -> {
                        if (dialogButton == deleteButtonType) {
                            return true;
                        }
                        return false;
                    });

                    Optional<Boolean> result = dialog.showAndWait();

                    result.ifPresent(delete -> {
                        if (delete) {
                            boolean ret = facade.deleteChild(user, cell.getTableView().getItems().get(cell.getIndex()).get("id"));
                            if (ret) {
                                onClickUpdateButton();
                            } else {
                                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                                alert.setTitle("Error");
                                alert.setHeaderText("Error ocured during deleting child!");
                                alert.showAndWait();
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        return cell;
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

    private void updateChildTable() {
        ObservableList<Struct> childs = null;
        try {
            childs = FXCollections.observableArrayList(facade.getAllChildsForUser(user));
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        childTable.setItems(childs);
    }
    
    private void updateTicketTable() {
        ObservableList<Struct> tickets = null;
        try {
            tickets = FXCollections.observableArrayList(facade.getAllTicketsForUser(user));
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        ticketTable.setItems(tickets);
    }
    
    private void updateEduRequestsTable() {
        ObservableList<Struct> tickets = null;
        try {
            tickets = FXCollections.observableArrayList(facade.getAllEduRequestsForUser(user));
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        eduRequestTable.setItems(tickets); 
    }
}
