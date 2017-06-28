/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services.stateservices.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
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
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.input.MouseButton;
import javafx.scene.control.Tooltip;
import javafx.scene.control.ComboBox;
import javafx.util.StringConverter;

/**
 *
 * @author Masha
 */
public abstract class InstitutionsController {
    protected String user;
    protected Facade facade = Main.facade;
    protected List<Struct> userChilds = new ArrayList<>();
    protected int institution = 0;
    protected String doctor = "";
    protected String city = "";
    protected String district = "";
    protected boolean isEdu = false;
    protected List<String> cities = new ArrayList<>();
    protected List<String> districts = new ArrayList<>();
    protected ObservableList<Struct> institutions = FXCollections.observableArrayList();
    protected ObservableList<Struct> doctors = FXCollections.observableArrayList();
    protected boolean canAddFeedbacks = true;
    
    @FXML
    protected ComboBox<Struct> selectDoctor; 
    @FXML
    protected Button updateButton;
    @FXML
    protected TabPane tabPane;
    @FXML
    protected Tab institutionsTab;
    @FXML
    protected TableView<Struct> institutionTable;
    @FXML
    protected TableColumn<Struct, String> institutionTitleColumn;
    @FXML
    protected TableColumn<Struct, String> institutionTelephoneColumn;
    @FXML
    protected TableColumn<Struct, String> institutionFaxColumn;
    @FXML
    protected TableColumn<Struct, String> institutionAddressColumn;
    @FXML
    protected Tab informationTab;
    @FXML
    protected TableView<Struct> seatsTable;
    @FXML
    protected TableColumn<Struct, String> seatsClassColumn;
    @FXML
    protected TableColumn<Struct, String> seatsTotalColumn;
    @FXML
    protected TableColumn<Struct, String> freeSeatsColumn;
    @FXML
    protected TableView<Struct> feedbackTable;
    @FXML
    protected TableColumn<Struct, String> feedbackDateColumn;
    @FXML
    protected TableColumn<Struct, String> feedbackUserColumn;
    @FXML
    protected TableColumn<Struct, String> feedbackTextColumn;
    @FXML
    protected TableColumn<Struct, String> feedbackToUserColumn;
    @FXML
    protected TableColumn<Struct, String> feedbackActionColumn;
    @FXML
    protected Label districtLabel;
    @FXML
    protected Label cityLabel;
    @FXML
    protected Label titleLabel;
    @FXML
    protected Label addressLabel;
    @FXML
    protected Label telephoneLabel;
    @FXML
    protected Label faxLabel;
    @FXML
    protected ComboBox<Struct> selectInstitution;
    @FXML
    protected ComboBox<Struct> selectInstitution1;
    @FXML
    protected ComboBox<Struct> selectInstitution2;
    @FXML
    protected Button addFeedbackButton;
    @FXML
    protected Button signOutButton;
    @FXML
    protected Button backButton;
    @FXML
    protected ChoiceBox<String> selectCity;
    @FXML
    protected ChoiceBox<String> selectDistrict;
    @FXML
    protected Tab doctorsTab;
    @FXML
    protected TableView<Struct> doctorsTable;
    @FXML
    protected TableColumn<Struct, String> doctorFullNameColumn;
    @FXML
    protected TableColumn<Struct, String> doctorEmailColumn;
    @FXML
    protected TableColumn<Struct, String> doctorPositionColumn;
    @FXML
    protected TableColumn<Struct, String> doctorSummaryColumn;
    @FXML
    protected TableColumn<Struct, String> doctorActionColumn;
    @FXML
    protected Tab ticketTab;
    @FXML
    protected TableView<Struct> ticketTable;
    @FXML
    protected TableColumn<Struct, String> ticketDateColumn;
    @FXML
    protected TableColumn<Struct, String> ticketDoctorColumn;
    @FXML
    protected TableColumn<Struct, String> ticketCitizenColumn;
    @FXML
    protected TableColumn<Struct, String> ticketChildColumn;
    @FXML
    protected TableColumn<Struct, String> ticketVisitedColumn;
    @FXML
    protected TableColumn<Struct, String> ticketSummary;
    @FXML
    protected TableColumn<Struct, String> ticketActionColumn;
    
    public abstract void onClickUpdateButton();
    
    protected  void setupCities() {
        ObservableList<String> tempCities = FXCollections.observableArrayList();
        try {
            cities = facade.getCities(isEdu);
            tempCities = FXCollections.observableArrayList(cities);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        selectCity.setItems(tempCities);
        selectCity.setTooltip(new Tooltip("Select city"));
        selectCityAddListener();
    }
    
    protected void selectCityAddListener() {
        selectCity.getSelectionModel().selectedIndexProperty().addListener(
                new ChangeListener<Number>() {
                    @Override
                    public void changed(ObservableValue ov, Number value, Number newValue) {
                            if (!cities.isEmpty()) {
                                String newCity = cities.get(newValue.intValue());
                                if (!newCity.equals(city)) {
                                    district = "";
                                    city = newCity;
                                    setupDistricts();
                                    onClickUpdateButton();
                                }
                            }
                    }
        });
    }
    
    protected void setupDistricts() {
        if (city.isEmpty()) return;
        ObservableList<String> tempDistricts = FXCollections.observableArrayList();
        try {
            districts = facade.getDistricts(city, isEdu);
            tempDistricts = FXCollections.observableArrayList(districts);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        selectDistrict.setItems(tempDistricts);
        selectDistrict.setTooltip(new Tooltip("Select district"));
        selectDistrictAddListener();
    }
        
    protected void selectDistrictAddListener() {
        selectDistrict.getSelectionModel().selectedIndexProperty().addListener(
                new ChangeListener<Number>() {
                    @Override
                    public void changed(ObservableValue ov, Number value, Number newValue) {
                        if (!districts.isEmpty() && newValue.intValue() >= 0) {
                            district = districts.get(newValue.intValue());
                            selectInstitution.setPromptText("");
                            if (selectInstitution1 != null) selectInstitution1.setPromptText("");
                            if (selectInstitution2 != null) selectInstitution2.setPromptText("");
                            if (selectDoctor != null) selectDoctor.setPromptText("");
                            onClickUpdateButton();
                        }
                    }
        });
    }
    
    protected void setupInstitutionsSelectBox() {
        setupInstitutionsSelectBoxs(selectInstitution);
        if (selectInstitution1 != null) setupInstitutionsSelectBoxs(selectInstitution1);
        if (selectInstitution2 != null) setupInstitutionsSelectBoxs(selectInstitution2);
    }
    
    protected void setupInstitutionsSelectBoxs(ComboBox<Struct> select) {
        select.getItems().clear();
        select.setItems(institutions);
        select.setConverter(new StringConverter<Struct>() {
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
        select.setTooltip(new Tooltip("Select institution"));
        select.getSelectionModel().selectedIndexProperty().addListener(
                new ChangeListener<Number>() {
                    @Override
                    public void changed(ObservableValue ov, Number value, Number newValue) {
                        if (!institutions.isEmpty() & newValue.intValue() >= 0) {
                            Struct struct = institutions.get(newValue.intValue());
                            institution = new Integer(struct.get("id"));
                            setPrompts(struct.get("title"));
                            onClickUpdateButton();
                        }
                    }
        });
    }
    
    protected void setPrompts(String title) {
        selectInstitution.setPromptText(title);
        if (selectInstitution1 != null) selectInstitution1.setPromptText(title);
        if (selectInstitution2 != null) selectInstitution2.setPromptText(title);
    }
    
    protected void updateDoctorsTable() {
        try {
            doctors = FXCollections.observableArrayList(facade.getAllDoctorsForInstitution(institution));
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        doctorsTable.setItems(doctors);
    }
    
    protected void setUpDoctorsTable() {
        doctorFullNameColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().get("fullName")));
        doctorEmailColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().get("email")));
        doctorPositionColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().get("position")));
        doctorSummaryColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().get("summary")));
        addButtonsToDoctorTable();
    }
    
    protected void addButtonsToDoctorTable() {}
        
    protected void updateTicketsTable() {
        ObservableList<Struct> tickets = null;
        try {
            tickets = FXCollections.observableArrayList(facade.getTicketsForMedicalInstitution(institution, doctor));
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        ticketTable.setItems(tickets);
    }
        
    protected void setUpTicketsTable() {
        ticketDateColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().get("date")));
        ticketCitizenColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().get("citizen")));
        ticketDoctorColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().get("doctor")));
        ticketChildColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().get("child")));
        ticketVisitedColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().get("visited")));
        ticketSummary.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().get("summary")));
        addButtonsToTicketTable();
    }
    
    protected void addButtonsToTicketTable() {
        ticketActionColumn.setCellValueFactory(cell -> new SimpleStringProperty(""));
    };
    
    protected void updateSeatsTable() {
        if (institution < 0) return;
        ObservableList<Struct> seats = null;
        try {
            seats = FXCollections.observableArrayList(facade.getSeatsForEducationalInstitution(institution));
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        seatsTable.setItems(seats);
    }

    protected void updateFeedbackTable() {
        if (institution < 0) return;
        ObservableList<Struct> feedbacks = null;
        try {
            feedbacks = FXCollections.observableArrayList(facade.getAllFeedbacksForInstitution(institution, isEdu));
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        feedbackTable.setItems(feedbacks);
    }
    
    protected void updateInstitutionLabels() {
        if (institution < 0) return;
        titleLabel.setText(isEdu ? facade.getEducationalInstitutionTitle(institution) : facade.getMedicalInstitutionTitle(institution));
        cityLabel.setText(isEdu ? facade.getEducationalInstitutionCity(institution) : facade.getMedicalInstitutionCity(institution));
        districtLabel.setText(isEdu ? facade.getEducationalInstitutionDistrict(institution) : facade.getMedicalInstitutionDistrict(institution));
        addressLabel.setText(isEdu ? facade.getEducationalInstitutionAddress(institution) : facade.getMedicalInstitutionAddress(institution));
        telephoneLabel.setText(isEdu ? facade.getEducationalInstitutionTelephone(institution) : facade.getMedicalInstitutionTelephone(institution));
        faxLabel.setText(isEdu ? facade.getEducationalInstitutionFax(institution) : facade.getMedicalInstitutionFax(institution));
    }
    
    protected void updateInstitutionTable() { 
        if (city.length() > 0 && district.length() > 0) {
            try {
                institutions = FXCollections.observableArrayList(facade.getInstitutions(city, district, isEdu));
                setupInstitutionsSelectBox();
            } catch (Exception e) {
                e.printStackTrace();
                return;
            } 
        }
        else {
            institutions = FXCollections.observableArrayList();
            disableTabs();
        }
        institutionTable.setItems(institutions);
    }
    
    protected void setUpInstitutionTable() {
        institutionTitleColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().get("title")));
        institutionTelephoneColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().get("telephone")));
        institutionFaxColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().get("fax")));
        institutionAddressColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().get("address")));
        institutionTitleColumn.setCellFactory(col -> {
            return onClickInstitutionAction();
        });
        institutionTelephoneColumn.setCellFactory(col -> {
            return onClickInstitutionAction();
        });
        institutionFaxColumn.setCellFactory(col -> {
            return onClickInstitutionAction();
        });
        institutionAddressColumn.setCellFactory(col -> {
            return onClickInstitutionAction();
        });
    }
    
    protected TableCell<Struct, String> onClickInstitutionAction() {
        final TableCell<Struct, String> cell = new TableCell<>();
        cell.textProperty().bind(cell.itemProperty());
        cell.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                institution = new Integer(cell.getTableView().getItems().get(cell.getIndex()).get("id"));
                onClickUpdateButton();
                enableTabs();
            }
        });
        return cell;
    }
    
    protected void disableTabs() {
        SingleSelectionModel<Tab> selectionModel = tabPane.getSelectionModel();
        selectionModel.select(institutionsTab);
        informationTab.setDisable(true);
        informationTab.getContent().setVisible(false);
    }
    
    protected void enableTabs() {
        SingleSelectionModel<Tab> selectionModel = tabPane.getSelectionModel();
        selectionModel.select(informationTab);
        informationTab.setDisable(false);
        informationTab.getContent().setVisible(true);
    }
    
    protected void setUpSeatsTable() {
        seatsClassColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().get("classNumber")));
        seatsTotalColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().get("seats")));
        freeSeatsColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().get("freeSeats")));
    }

    protected void setUpFeedbackTable() {
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

                                boolean ret = facade.addFeedbackByUser(user, institution, result.get(), loginUserTo, isEdu);
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
    
    @FXML
    protected void onClickAddFeedback(MouseEvent event) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Enter feedback text");
        dialog.setHeaderText("Enter feedback text");

        Optional<String> result = dialog.showAndWait();
        if (!result.isPresent()) {
            return;
        }

        boolean ret = facade.addFeedbackByUser(user, institution, result.get(), "", isEdu);
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
    private void onClickSignOutButton(MouseEvent event) {
        facade.signOut(user);
        Main.closeStage();
        Main.showSignInView();
    }

    @FXML
    private void onClickBackButton(MouseEvent event) {
        Main.closeStage();
        Main.showMainStage();
    }
}
