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
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.util.Callback;
import services.stateservices.Main;
import services.stateservices.facade.Facade;
import services.stateservices.facade.Struct;
import javafx.scene.control.TextInputDialog;
import java.util.Optional;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.layout.GridPane;

/**
 * FXML Controller class
 *
 * @author Masha
 */
public class MainEducationalRepresentativeViewController {

    private Facade facade = Main.facade;
    private String user;
    private int institution;
    @FXML
    private Button signOutButton;
    @FXML
    private Tab informationTab;
    @FXML
    private TableView<Struct> seatsTable;
    @FXML
    private TableColumn<Struct, String> seatsClassColumn;
    @FXML
    private TableColumn<Struct, String> seatsTotalColumn;
    @FXML
    private TableColumn<Struct, String> freeSeatsColumn;
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
    private TableView<Struct> requestTable;
    @FXML
    private TableColumn<Struct, String> requestDateColumn;
    @FXML
    private TableColumn<Struct, String> requestAppointmentColumn;
    @FXML
    private TableColumn<Struct, String> requestStatusColumn;
    @FXML
    private TableColumn<Struct, String> requestChildColumn;
    @FXML
    private TableColumn<Struct, String> requestClassColumn;
    @FXML
    private TableColumn<Struct, String> requestBirthDateColumn;
    @FXML
    private TableColumn<Struct, String> requestActionColumn;
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
    private Label emailLabel;
    @FXML
    private Label nameFullColumn;
    @FXML
    private Label userLabel;
    @FXML
    private Button changeSeatsButton;
    @FXML
    private Button updateButton;
    @FXML
    private Button addFeedbackButton;

    public void setup(String user, int institution) {
        this.user = user;
        this.institution = institution;
        userLabel.setText(this.user);
        emailLabel.setText(facade.getUserEmail(user));
        nameFullColumn.setText(facade.getUserFullName(user));

        infoTitleField.setText(facade.getEducationalInstitutionTitle(institution));
        infoCityField.setText(facade.getEducationalInstitutionCity(institution));
        infoDistrictField.setText(facade.getEducationalInstitutionDistrict(institution));
        infoAddressField.setText(facade.getEducationalInstitutionAddress(institution));
        infoTelephoneField.setText(facade.getEducationalInstitutionTelephone(institution));
        infoFaxField.setText(facade.getEducationalInstitutionFax(institution));
        setUpSeatsTable();
        setUpFeedbackTable();
        setUpEduRequestTable();
        onClickUpdateButton();
    }

    @FXML
    private void onClickUpdateButton() {
        updateSeatsTable();
        updateFeedbackTable();
        updateEduRequestsTable();

        infoTitleField.setText(facade.getEducationalInstitutionTitle(institution));
        infoCityField.setText(facade.getEducationalInstitutionCity(institution));
        infoDistrictField.setText(facade.getEducationalInstitutionDistrict(institution));
        infoAddressField.setText(facade.getEducationalInstitutionAddress(institution));
        infoTelephoneField.setText(facade.getEducationalInstitutionTelephone(institution));
        infoFaxField.setText(facade.getEducationalInstitutionFax(institution));
    }

    private void updateEduRequestsTable() {
        ObservableList<Struct> requests = null;
        try {
            requests = FXCollections.observableArrayList(facade.getAllEduRequestsForInstitution(institution));
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        requestTable.setItems(requests);
    }

    private void updateSeatsTable() {
        ObservableList<Struct> seats = null;
        try {
            seats = FXCollections.observableArrayList(facade.getSeatsForEducationalInstitution(institution));
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        seatsTable.setItems(seats);
    }

    private void updateFeedbackTable() {
        ObservableList<Struct> feedbacks = null;
        try {
            feedbacks = FXCollections.observableArrayList(facade.getAllFeedbacksForInstitution(institution, true));
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

    private void setUpSeatsTable() {
        seatsClassColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().get("classNumber")));
        seatsTotalColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().get("seats")));
        freeSeatsColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().get("freeSeats")));
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

    private void setUpEduRequestTable() {
        requestDateColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().get("creationDate")));
        requestStatusColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().get("status")));
        requestChildColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().get("child")));
        requestClassColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().get("classNumber")));
        requestBirthDateColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().get("childBirthDate")));
        requestAppointmentColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().get("appointment")));

        requestActionColumn.setCellValueFactory(new PropertyValueFactory<>("DUMMY"));

        Callback<TableColumn<Struct, String>, TableCell<Struct, String>> cellFactory;
        cellFactory = new Callback<TableColumn<Struct, String>, TableCell<Struct, String>>() {
            @Override
            public TableCell call(final TableColumn<Struct, String> param) {
                final TableCell<Struct, String> cell;
                cell = new TableCell<Struct, String>() {

                    final Button btnAccept = new Button("Accept");
                    final Button btnRefuse = new Button("Refuse");
                    final Button btnMakeAppointment = new Button("Appointment");
                    final Button btnEnroll = new Button("Enroll");

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
                        if (empty || (fields.get("mustAccept").equals("no") && fields.get("mustDecide").equals("no") && fields.get("mustMakeAppointment").equals("no"))) {
                            setGraphic(null);
                            setText(null);
                        } else if (fields.get("mustAccept").equals("yes")) {
                            btnAccept.setOnAction(event -> {
                                boolean ret = facade.acceptEduRequestByInstitution(user, fields.get("id"));
                                if (ret) {
                                    onClickUpdateButton();
                                } else {
                                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                                    alert.setTitle("Error");
                                    alert.setHeaderText("Error ocured during accepting request by institution!");
                                    alert.showAndWait();
                                }
                            });
                            pane.getChildren().add(btnAccept);
                            setGraphic(pane);
                            setText(null);
                        }
                        if (fields.get("mustMakeAppointment").equals("yes")) {
                            btnMakeAppointment.setOnAction(event -> {
                                TextInputDialog dialog = new TextInputDialog();
                                dialog.setTitle("Enter appointment date");
                                dialog.setHeaderText("Enter appointment date (format - 2017-06-26 17:45:55)");

                                Optional<String> result = dialog.showAndWait();
                                if (!result.isPresent()) {
                                    return;
                                }

                                String date = result.get();

                                boolean ret = facade.makeAppointment(user, fields.get("id"), date);
                                if (ret) {
                                    onClickUpdateButton();
                                } else {
                                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                                    alert.setTitle("Error");
                                    alert.setHeaderText("Invalid date or format!");
                                    alert.showAndWait();
                                }
                            });
                            pane.getChildren().add(btnMakeAppointment);
                            setGraphic(pane);
                            setText(null);
                        }
                        if (fields.get("mustDecide").equals("yes")) {
                            btnEnroll.setOnAction(event -> {
                                boolean ret = facade.enrollChildInInstitution(user, fields.get("id"));
                                if (ret) {
                                    onClickUpdateButton();
                                } else {
                                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                                    alert.setTitle("Error");
                                    alert.setHeaderText("Error ocured during enrolling child! May be no free seats!");
                                    alert.showAndWait();
                                }
                            });
                            btnRefuse.setOnAction(event -> {
                                boolean ret = facade.refuseEduRequestByInstitution(user, fields.get("id"));
                                if (ret) {
                                    onClickUpdateButton();
                                } else {
                                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                                    alert.setTitle("Error");
                                    alert.setHeaderText("Error ocured during refusing request!");
                                    alert.showAndWait();
                                }
                            });
                            pane.getChildren().add(btnEnroll);
                            pane.getChildren().add(btnRefuse);
                            setGraphic(pane);
                            setText(null);
                        }
                    }
                };
                return cell;
            }
        };

        requestActionColumn.setCellFactory(cellFactory);
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
    private void onClickChangeSeatsButton(MouseEvent event) {
        Dialog<Struct> dialog = new Dialog<>();
        dialog.setTitle("Enter class number, total and free seats");

        // Set the button types.
        ButtonType loginButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(20, 150, 10, 10));

        TextField classNumber = new TextField();
        classNumber.setPromptText("Class number");
        TextField seats = new TextField();
        seats.setPromptText("Total seats");
        TextField busySeats = new TextField();
        busySeats.setPromptText("Busy seats");

        gridPane.add(new Label("Class number:"), 0, 0);
        gridPane.add(classNumber, 1, 0);
        gridPane.add(new Label("Total seats:"), 2, 0);
        gridPane.add(seats, 3, 0);
        gridPane.add(new Label("Busy seats:"), 4, 0);
        gridPane.add(busySeats, 5, 0);

        dialog.getDialogPane().setContent(gridPane);

        // Request focus on the full name field by default.
        Platform.runLater(() -> classNumber.requestFocus());

        // Convert the result when the login button is clicked.
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                Struct fields = new Struct();
                fields.add("classNumber", classNumber.getText());
                fields.add("seats", seats.getText());
                fields.add("busySeats", busySeats.getText());
                return fields;
            }
            return null;
        });

        Optional<Struct> result = dialog.showAndWait();

        result.ifPresent(fields -> {
            if (fields.get("classNumber").length() > 0 && fields.get("seats").length() > 0 && fields.get("busySeats").length() > 0) {
                boolean ret = facade.saveSeatsForEducationalInstitution(user, new Integer(fields.get("classNumber")), new Integer(fields.get("seats")), new Integer(fields.get("busySeats")));
                if (ret) {
                    onClickUpdateButton();
                } else {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Error");
                    alert.setHeaderText("Error during saving seats or connect error!");
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
}
