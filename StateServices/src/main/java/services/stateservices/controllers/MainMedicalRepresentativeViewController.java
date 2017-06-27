/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services.stateservices.controllers;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.util.Callback;
import services.stateservices.Main;
import services.stateservices.facade.Facade;
import services.stateservices.facade.Struct;

/**
 * FXML Controller class
 *
 * @author Masha
 */
public class MainMedicalRepresentativeViewController {
    private Facade facade = Main.facade;
    private String user;
    private int institution;
    @FXML
    private Button addFeedbackButton;
    @FXML
    private Button updateButton;
    @FXML
    private Button addDoctorButton;
    @FXML
    private Label userLabel;
    @FXML
    private Label nameFullColumn;
    @FXML
    private Label emailLabel;
    @FXML
    private Tab informationTab;
    @FXML
    private TextField infoTitleField;
    @FXML
    private TextField infoCityField;
    @FXML
    private TextField infoDistrictField;
    @FXML
    private TextField infoAddressField;
    @FXML
    private TextField infoTelephoneField;
    @FXML
    private TextField infoFaxField;
    @FXML
    private Button saveChangesButton;
    @FXML
    private Tab requestsTab;
    @FXML
    private TableView<Struct> doctorsTable;
    @FXML
    private TableColumn<Struct, String> doctorFullNameColumn;
    @FXML
    private TableColumn<Struct, String> doctorEmailColumn;
    @FXML
    private TableColumn<Struct, String> doctorPositionColumn;
    @FXML
    private TableColumn<Struct, String> doctorSummaryColumn;
    @FXML
    private TableColumn<Struct, String> doctorActionColumn;
    @FXML
    private TableView<Struct> feedbackTable;
    @FXML
    private TableColumn<Struct, String> feedbackDateColumn;
    @FXML
    private TableColumn<Struct, String> feedbackUserColumn;
    @FXML
    private TableColumn<Struct, String> feedbackToUserColumn;
    @FXML
    private TableColumn<Struct, String> feedbackTextColumn;
    @FXML
    private TableColumn<Struct, String> feedbackActionColumn;
    @FXML
    private Tab ticketTab;
    @FXML
    private TableView<Struct> ticketTable;
    @FXML
    private TableColumn<Struct, String> ticketDateColumn;
    @FXML
    private TableColumn<Struct, String> ticketDoctorColumn;
    @FXML
    private TableColumn<Struct, String> ticketCitizenColumn;
    @FXML
    private TableColumn<Struct, String> ticketChildColumn;
    @FXML
    private TableColumn<Struct, String> ticketVisitedColumn;
    @FXML
    private TableColumn<Struct, String> ticketSummary;
    @FXML
    private TableColumn<Struct, String> ticketActionColumn;
    @FXML
    private Button signOutButton;
    @FXML
    private Button removeTicketsButton;
    
    public void setup(String user, int institution) {
        this.user = user;
        this.institution = institution;
        userLabel.setText(this.user);
        emailLabel.setText(facade.getUserEmail(user));
        nameFullColumn.setText(facade.getUserFullName(user));

        infoTitleField.setText(facade.getMedicalInstitutionTitle(institution));
        infoCityField.setText(facade.getMedicalInstitutionCity(institution));
        infoDistrictField.setText(facade.getMedicalInstitutionDistrict(institution));
        infoAddressField.setText(facade.getMedicalInstitutionAddress(institution));
        infoTelephoneField.setText(facade.getMedicalInstitutionTelephone(institution));
        infoFaxField.setText(facade.getMedicalInstitutionFax(institution));
        setUpTicketsTable();
        setUpFeedbackTable();
        setUpDoctorsTable();
        onClickUpdateButton();
    }

        @FXML
    private void onClickUpdateButton() {
        updateTicketsTable();
        updateFeedbackTable();
        updateDoctorsTable();

        infoTitleField.setText(facade.getMedicalInstitutionTitle(institution));
        infoCityField.setText(facade.getMedicalInstitutionCity(institution));
        infoDistrictField.setText(facade.getMedicalInstitutionDistrict(institution));
        infoAddressField.setText(facade.getMedicalInstitutionAddress(institution));
        infoTelephoneField.setText(facade.getMedicalInstitutionTelephone(institution));
        infoFaxField.setText(facade.getMedicalInstitutionFax(institution));
    }

    private void updateDoctorsTable() {
        ObservableList<Struct> doctors = null;
        try {
            doctors = FXCollections.observableArrayList(facade.getAllDoctorsForInstitution(institution));
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        doctorsTable.setItems(doctors);
    }

    private void updateTicketsTable() {
        ObservableList<Struct> tickets = null;
        try {
            tickets = FXCollections.observableArrayList(facade.getTicketsForMedicalInstitution(institution));
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        ticketTable.setItems(tickets);
    }

    private void updateFeedbackTable() {
        ObservableList<Struct> feedbacks = null;
        try {
            feedbacks = FXCollections.observableArrayList(facade.getAllFeedbacksForInstitution(institution, false));
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        feedbackTable.setItems(feedbacks);
    }

    @FXML
    private void onClickSignOutButton() {
        facade.signOut(user);
        Main.showSignInView();
    }

    private void setUpTicketsTable() {
        ticketDateColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().get("date")));
        ticketCitizenColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().get("citizen")));
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

    private void setUpFeedbackTable() {
        feedbackDateColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().get("date")));
        feedbackUserColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().get("user")));
        feedbackToUserColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().get("toUser")));
        feedbackTextColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().get("text")));

        feedbackActionColumn.setCellValueFactory(new PropertyValueFactory<>("DUMMY"));

        Callback<TableColumn<Struct, String>, TableCell<Struct, String>> cellFactory;
        cellFactory = new Callback<TableColumn<Struct, String>, TableCell<Struct, String>>() {
            @Override
            public TableCell call(final TableColumn<Struct, String> param) {
                final TableCell<Struct, String> cell;
                cell = new TableCell<Struct, String>() {

                    final Button btnAddFeedback = new Button("Add feedback");
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
                        if (empty || fields.get("userLogin").equals(user)) {
                            setGraphic(null);
                            setText(null);
                        } else {
                            btnAddFeedback.setOnAction(event -> {
                                String loginUserTo = fields.get("userLogin");
                                TextInputDialog dialog = new TextInputDialog();
                                dialog.setTitle("Enter feedback text");
                                dialog.setHeaderText("Enter feedback text");

                                Optional<String> result = dialog.showAndWait();
                                if (!result.isPresent()) {
                                    return;
                                }

                                boolean ret = facade.addFeedbackByRepresentative(user, institution, result.get(), loginUserTo);
                                if (ret) {
                                    onClickUpdateButton();
                                } else {
                                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                                    alert.setTitle("Error");
                                    alert.setHeaderText("Error occured during saving feedback!");
                                    alert.showAndWait();
                                }
                                loginUserTo = "";
                            });
                            pane.getChildren().add(btnAddFeedback);
                            setGraphic(pane);
                            setText(null);
                        }
                    }
                };
                return cell;
            }
        };

        feedbackActionColumn.setCellFactory(cellFactory);
    }

    private void setUpDoctorsTable() {
        doctorFullNameColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().get("fullName")));
        doctorEmailColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().get("email")));
        doctorPositionColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().get("position")));
        doctorSummaryColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().get("summary")));

        doctorActionColumn.setCellValueFactory(new PropertyValueFactory<>("DUMMY"));

        Callback<TableColumn<Struct, String>, TableCell<Struct, String>> cellFactory;
        cellFactory = new Callback<TableColumn<Struct, String>, TableCell<Struct, String>>() {
            @Override
            public TableCell call(final TableColumn<Struct, String> param) {
                final TableCell<Struct, String> cell;
                cell = new TableCell<Struct, String>() {

                    final Button btnRemove = new Button("Remove");
                    final Button btnAddTicket = new Button("Add ticket");
                    final Button btnAddTickets = new Button("Add tickets");

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
                        if (empty) {
                            setGraphic(null);
                            setText(null);
                        } else {
                            btnRemove.setOnAction(event -> {
                                boolean ret = facade.removeDoctor(user, fields.get("login"));
                                if (ret) {
                                    onClickUpdateButton();
                                } else {
                                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                                    alert.setTitle("Error");
                                    alert.setHeaderText("Error ocured during removing the doctor!");
                                    alert.showAndWait();
                                }
                            });
                            pane.getChildren().add(btnRemove);
                            setGraphic(pane);
                            setText(null);

                            btnAddTicket.setOnAction(event -> {
                                TextInputDialog dialog = new TextInputDialog();
                                dialog.setTitle("Enter ticket date");
                                dialog.setHeaderText("Enter ticket date (format - 2017-06-26 17:45:55)");

                                Optional<String> result = dialog.showAndWait();
                                if (!result.isPresent()) {
                                    return;
                                }

                                String date = result.get();

                                boolean ret = facade.addTicket(user, fields.get("login"), date);
                                if (ret) {
                                    onClickUpdateButton();
                                } else {
                                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                                    alert.setTitle("Error");
                                    alert.setHeaderText("Invalid date or format!");
                                    alert.showAndWait();
                                }
                            });
                            pane.getChildren().add(btnAddTicket);
                            setGraphic(pane);
                            setText(null);

                            btnAddTickets.setOnAction(event -> {
                                Dialog<Struct> dialog = new Dialog<>();
                                dialog.setTitle("Enter start and end date (format - 2017-06-26 17:45:55) and interval in minutes!");

                                // Set the button types.
                                ButtonType loginButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
                                dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

                                GridPane gridPane = new GridPane();
                                gridPane.setHgap(10);
                                gridPane.setVgap(10);
                                gridPane.setPadding(new Insets(20, 150, 10, 10));

                                TextField start = new TextField();
                                start.setPromptText("Start date");
                                TextField end = new TextField();
                                end.setPromptText("End date");
                                TextField interval = new TextField();
                                interval.setPromptText("Interval in minutes");

                                gridPane.add(new Label("Start date:"), 0, 0);
                                gridPane.add(start, 1, 0);
                                gridPane.add(new Label("End date:"), 2, 0);
                                gridPane.add(end, 3, 0);
                                gridPane.add(new Label("Interval in minutes:"), 4, 0);
                                gridPane.add(interval, 5, 0);

                                dialog.getDialogPane().setContent(gridPane);

                                // Request focus on the full name field by default.
                                Platform.runLater(() -> start.requestFocus());

                                // Convert the result when the login button is clicked.
                                dialog.setResultConverter(dialogButton -> {
                                    if (dialogButton == loginButtonType) {
                                        Struct struct = new Struct();
                                        struct.add("start", start.getText());
                                        struct.add("end", end.getText());
                                        struct.add("interval", interval.getText());
                                        return struct;
                                    }
                                    return null;
                                });

                                Optional<Struct> result = dialog.showAndWait();

                                result.ifPresent(formFields -> {
                                    if (formFields.get("start").length() > 0 && formFields.get("end").length() > 0 && formFields.get("interval").length() > 0) {
                                        boolean ret = facade.addTickets(user, fields.get("login"), formFields.get("start"), formFields.get("end"), formFields.get("interval"));
                                        if (ret) {
                                            onClickUpdateButton();
                                        } else {
                                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                                            alert.setTitle("Error");
                                            alert.setHeaderText("Invalid dates or interval or connect error!");
                                            alert.showAndWait();
                                        }
                                    } else {
                                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                                        alert.setTitle("Error");
                                        alert.setHeaderText("You must fill all fields!");
                                        alert.showAndWait();
                                    }
                                });
                            });
                            pane.getChildren().add(btnAddTickets);
                            setGraphic(pane);
                            setText(null);
                        }
                    }
                };
                return cell;
            }
        };

        doctorActionColumn.setCellFactory(cellFactory);
    }

    @FXML
    private void onClickInfoSaveChanges(MouseEvent event) {
        String title = infoTitleField.getText();
        String city = infoCityField.getText();
        String district = infoDistrictField.getText();
        String telephone = infoTelephoneField.getText();
        String fax = infoFaxField.getText();
        String address = infoAddressField.getText();
        boolean ret = false;
        if (title.length() > 0 && city.length() > 0 && district.length() > 0 && telephone.length() > 0 && fax.length() > 0 && address.length() > 0) {
            ret = facade.editInstitutionInformation(user, title, city, district, telephone, fax, address, institution);
        }
        if (ret) {
            onClickUpdateButton();
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText("Changes successfully saved!");
            alert.showAndWait();
        } else {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Error");
            alert.setHeaderText("Error ocured during saving information about institution! All fields must be filled");
            alert.showAndWait();
        }
    }

    @FXML
    private void onClickAddDoctorButton(MouseEvent event) {
        Dialog<Struct> dialog = new Dialog<>();
        dialog.setTitle("Enter information about doctor");

        // Set the button types.
        ButtonType loginButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(20, 150, 10, 10));

        TextField fullName = new TextField();
        fullName.setPromptText("Full name");
        TextField login = new TextField();
        login.setPromptText("Login");
        TextField email = new TextField();
        email.setPromptText("Email");
        TextField password = new TextField();
        password.setPromptText("Password");
        TextField position = new TextField();
        position.setPromptText("Position");
        TextField summary = new TextField();
        summary.setPromptText("Summary");

        gridPane.add(new Label("Full name:"), 0, 0);
        gridPane.add(fullName, 0, 0);
        gridPane.add(new Label("Login:"), 0, 1);
        gridPane.add(login, 0, 1);
        gridPane.add(new Label("Email:"), 0, 2);
        gridPane.add(email, 0, 2);
        gridPane.add(new Label("Password:"), 0, 3);
        gridPane.add(password, 0, 3);
        gridPane.add(new Label("Position:"), 0, 4);
        gridPane.add(position, 0, 4);
        gridPane.add(new Label("Summary:"), 0, 5);
        gridPane.add(summary, 0, 5);

        dialog.getDialogPane().setContent(gridPane);

        // Request focus on the full name field by default.
        Platform.runLater(() -> fullName.requestFocus());

        // Convert the result when the login button is clicked.
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                Struct fields = new Struct();
                fields.add("fullName", fullName.getText());
                fields.add("email", email.getText());
                fields.add("password", password.getText());
                fields.add("login", login.getText());
                fields.add("position", position.getText());
                fields.add("summary", summary.getText());
                return fields;
            }
            return null;
        });

        Optional<Struct> result = dialog.showAndWait();

        result.ifPresent(fields -> {
            if (fields.get("fullName").length() > 0 && fields.get("login").length() > 0 && fields.get("email").length() > 0 && fields.get("password").length() > 0 && fields.get("position").length() > 0 && fields.get("summary").length() > 0) {
                boolean ret = facade.saveDoctorToMedicalInstitution(user, fields.get("login"), fields.get("password"), fields.get("fullName"), fields.get("email"), fields.get("position"), fields.get("summary"));
                if (ret) {
                    onClickUpdateButton();
                } else {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Error");
                    alert.setHeaderText("Error during saving information about doctor! Try enter other doctor login!");
                    alert.showAndWait();
                }
            } else {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Error");
                alert.setHeaderText("You must fill all fields!");
                alert.showAndWait();
            }
        });
    }

    @FXML
    private void onClickAddFeedback(MouseEvent event) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Enter feedback text");
        dialog.setHeaderText("Enter feedback text");

        Optional<String> result = dialog.showAndWait();
        if (!result.isPresent()) {
            return;
        }

        boolean ret = facade.addFeedbackByRepresentative(user, institution, result.get(), "");
        if (ret) {
            onClickUpdateButton();
        } else {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Error");
            alert.setHeaderText("Error occured during saving feedback!");
            alert.showAndWait();
        }
    }



    @FXML
    private void onClickRemoveTicketsButton(MouseEvent event) {
    }
}
