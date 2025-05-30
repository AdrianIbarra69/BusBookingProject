package app.busbookingproject;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BusBookingController {

    // TableView and Columns
    @FXML private TableView<Booking> bookingTable;
    @FXML private TableColumn<Booking, String> nameColumn;
    @FXML private TableColumn<Booking, String> contactColumn;
    @FXML private TableColumn<Booking, String> fromColumn;
    @FXML private TableColumn<Booking, String> toColumn;
    @FXML private TableColumn<Booking, String> busNameColumn;
    @FXML private TableColumn<Booking, LocalDate> dateColumn;
    @FXML private TableColumn<Booking, String> seatsColumn; // Displays "Count (Label1, Label2)"
    @FXML private TableColumn<Booking, String> fareColumn;
    @FXML private TableColumn<Booking, String> paymentStatusColumn;
    @FXML private TableColumn<Booking, String> changeColumn;
    @FXML private TableColumn<Booking, Void> actionColumn;

    // Form Fields
    @FXML private TextField nameField;
    @FXML private TextField contactField;
    @FXML private ComboBox<String> fromComboBox;
    @FXML private ComboBox<String> toComboBox;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<AvailableTrip> availableTripsComboBox;
    @FXML private Button selectSeatsButton;

    // Labels for Displaying Selected Info
    @FXML private Label selectedFromLabel;
    @FXML private Label selectedToLabel;

    // Data Structures
    private final ObservableList<Booking> bookingList = FXCollections.observableArrayList();
    private final Map<String, Map<String, Double>> routeCompanyFares = new HashMap<>(); // For fixed fares
    private final Map<String, Double> fallbackDistances = new HashMap<>(); // For distance-based fallback fares
    private BusAvailabilityService availabilityService;

    @FXML
    public void initialize() {
        availabilityService = new BusAvailabilityService(bookingList);
        populateRouteAndFareData();
        setupTableColumns();

        fromComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            selectedFromLabel.setText(newVal != null ? newVal : "N/A");
            updateAvailableTripsAndSeatButton(); // This method must be defined
        });
        toComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            selectedToLabel.setText(newVal != null ? newVal : "N/A");
            updateAvailableTripsAndSeatButton(); // This method must be defined
        });
        datePicker.valueProperty().addListener((obs, oldVal, newVal) -> updateAvailableTripsAndSeatButton()); // This method must be defined
        availableTripsComboBox.valueProperty().addListener((obs, oldVal, newVal) -> updateSeatButtonState()); // This method must be defined

        availableTripsComboBox.setItems(FXCollections.observableArrayList());
        availableTripsComboBox.setPromptText("Select route and date first");
        if (selectSeatsButton != null) { // Defensive null check
            selectSeatsButton.setDisable(true);
        }
    }

    private void setupTableColumns() {
        bookingTable.setItems(bookingList);
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        contactColumn.setCellValueFactory(new PropertyValueFactory<>("contact"));
        fromColumn.setCellValueFactory(new PropertyValueFactory<>("from"));
        toColumn.setCellValueFactory(new PropertyValueFactory<>("to"));
        busNameColumn.setCellValueFactory(new PropertyValueFactory<>("busName"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        seatsColumn.setCellValueFactory(cellData -> cellData.getValue().seatsDisplayProperty());
        fareColumn.setCellValueFactory(cellData -> cellData.getValue().formattedFareProperty());

        paymentStatusColumn.setCellValueFactory(cellData -> cellData.getValue().paymentStatusProperty());
        paymentStatusColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); }
                else { setText(item); setStyle("-fx-font-weight: bold; " + ("Paid".equalsIgnoreCase(item) ? "-fx-text-fill: green;" : "-fx-text-fill: red;")); }
            }
        });

        changeColumn.setCellValueFactory(cellData -> cellData.getValue().changeGivenProperty());

        Callback<TableColumn<Booking, Void>, TableCell<Booking, Void>> actionCellFactory = param -> {
            final TableCell<Booking, Void> cell = new TableCell<>() {
                private final Button btn = new Button("Checkout");
                {
                    btn.setOnAction(event -> {
                        Booking booking = null;
                        if (getIndex() >= 0 && getIndex() < getTableView().getItems().size()) {
                            booking = getTableView().getItems().get(getIndex());
                        }
                        if (booking != null) {
                            if ("Not Paid".equalsIgnoreCase(booking.getPaymentStatus())) {
                                handleRowSelectForCheckout(booking); // This method must be defined
                            } else {
                                Utility.showAlert(Alert.AlertType.INFORMATION, "Booking Paid", "This booking has already been paid.");
                            }
                        }
                    });
                }
                @Override
                public void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                        setGraphic(null);
                    } else {
                        Booking booking = (Booking) getTableRow().getItem();
                        btn.setDisable("Paid".equalsIgnoreCase(booking.getPaymentStatus()));
                        btn.setText("Paid".equalsIgnoreCase(booking.getPaymentStatus()) ? "Paid" : "Checkout");
                        setGraphic(btn);
                        setAlignment(Pos.CENTER);
                    }
                }
            };
            return cell;
        };
        actionColumn.setCellFactory(actionCellFactory);
    }

    private void populateRouteAndFareData() {
        ObservableList<String> locations = FXCollections.observableArrayList(
                "Cagayan de Oro", "Davao City", "General Santos", "Zamboanga City", "Iligan", "Butuan",
                "Pagadian City", "Dipolog City", "Oroquieta City", "Ozamiz City",
                "Maramag", "Valencia", "Malaybalay", "Tacurong City",
                "Gingoog City", "Balingoan", "Kibawe", "Don Carlos", "Kadingilan", "Quezon",
                "Laguindingan", "Metro Manila"
        );
        if (fromComboBox != null) fromComboBox.setItems(locations);
        if (toComboBox != null) toComboBox.setItems(locations);

        // --- Populate Fixed Fares (Key: "Origin-Destination", Value: Map<"Company", Fare_Per_Seat>) ---
        // (This section should contain all your addFare(...) calls based on the provided fare text)
        // Example:
        addFare("Cagayan de Oro", "Iligan", "RTMI", 100.00);
        addFare("Cagayan de Oro", "Iligan", "Super 5", 115.00);
        // ... many more addFare calls based on the extensive list you provided ...
        // Make sure this section is complete as per the previous response where I transcribed the fares.
        // FROM CAGAYAN DE ORO
        addFare("Cagayan de Oro", "Zamboanga City", "RTMI", 1000.00);
        addFare("Cagayan de Oro", "Iligan", "TOMAWIS", 100.00);
        addFare("Cagayan de Oro", "Pagadian City", "RTMI", 410.00);
        addFare("Cagayan de Oro", "Pagadian City", "Super 5", 345.00);
        addFare("Cagayan de Oro", "Dipolog City", "RTMI", 450.00);
        addFare("Cagayan de Oro", "Dipolog City", "Super 5", 375.00);
        addFare("Cagayan de Oro", "Oroquieta City", "RTMI", 320.00);
        addFare("Cagayan de Oro", "Oroquieta City", "Super 5", 255.00);
        addFare("Cagayan de Oro", "Ozamiz City", "RTMI", 240.00);
        addFare("Cagayan de Oro", "Ozamiz City", "Super 5", 190.00);
        addFare("Cagayan de Oro", "Laguindingan", "Super 5", 100.00);
        addFare("Cagayan de Oro", "Davao City", "RTMI", 700.00);
        addFare("Cagayan de Oro", "Maramag", "RTMI", 240.00);
        addFare("Cagayan de Oro", "Maramag", "Pabama", 220.00);
        addFare("Cagayan de Oro", "Maramag", "Super 5", 190.00);
        addFare("Cagayan de Oro", "Valencia", "RTMI", 200.00);
        addFare("Cagayan de Oro", "Valencia", "Pabama", 180.00);
        addFare("Cagayan de Oro", "Valencia", "Super 5", 160.00);
        addFare("Cagayan de Oro", "Malaybalay", "RTMI", 150.00);
        addFare("Cagayan de Oro", "Malaybalay", "Pabama", 150.00);
        addFare("Cagayan de Oro", "Malaybalay", "Super 5", 120.00);
        addFare("Cagayan de Oro", "General Santos", "RTMI", 840.00);
        addFare("Cagayan de Oro", "Tacurong City", "RTMI", 700.00);
        addFare("Cagayan de Oro", "Butuan", "Bachelor Express", 400.00);
        addFare("Cagayan de Oro", "Gingoog City", "Bachelor Express", 245.00);
        addFare("Cagayan de Oro", "Gingoog City", "Pabama", 160.00);
        addFare("Cagayan de Oro", "Gingoog City", "Metro Star", 160.00);
        addFare("Cagayan de Oro", "Gingoog City", "Bagong Lipunan", 130.00);
        addFare("Cagayan de Oro", "Balingoan", "Bachelor Express", 125.00);
        addFare("Cagayan de Oro", "Balingoan", "Pabama", 100.00);
        addFare("Cagayan de Oro", "Balingoan", "Metro Star", 100.00);
        addFare("Cagayan de Oro", "Balingoan", "Bagong Lipunan", 80.00);
        addFare("Cagayan de Oro", "Metro Manila", "Philtranco", 2600.00);
        addFare("Cagayan de Oro", "Kibawe", "Pabama", 320.00);

        addFare("Iligan", "Zamboanga City", "RTMI", 840.00);
        addFare("Iligan", "Ozamiz City", "RTMI", 140.00);
        addFare("Iligan", "Ozamiz City", "Super 5", 95.00);
        addFare("Iligan", "Dipolog City", "RTMI", 350.00);
        addFare("Iligan", "Dipolog City", "Super 5", 330.00);
        addFare("Iligan", "Pagadian City", "RTMI", 310.00);
        addFare("Iligan", "Pagadian City", "Super 5", 200.00);

        addFare("Ozamiz City", "Dipolog City", "RTMI", 210.00);
        addFare("Ozamiz City", "Dipolog City", "Super 5", 190.00);
        addFare("Ozamiz City", "Oroquieta City", "RTMI", 80.00);
        addFare("Ozamiz City", "Oroquieta City", "Super 5", 70.00);
        addFare("Ozamiz City", "Pagadian City", "RTMI", 150.00);
        addFare("Ozamiz City", "Pagadian City", "Super 5", 120.00);

        addFare("Oroquieta City", "Dipolog City", "RTMI", 130.00);
        addFare("Oroquieta City", "Dipolog City", "Super 5", 120.00);

        addFare("Malaybalay", "Davao City", "RTMI", 330.00);
        addFare("Malaybalay", "Valencia", "RTMI", 50.00);
        addFare("Malaybalay", "Valencia", "Pabama", 40.00);
        addFare("Malaybalay", "Valencia", "Super 5", 40.00);
        addFare("Malaybalay", "Valencia", "Super Six", 35.00);
        addFare("Malaybalay", "Maramag", "RTMI", 90.00);
        addFare("Malaybalay", "Maramag", "Pabama", 70.00);
        addFare("Malaybalay", "Maramag", "Super 5", 70.00);
        addFare("Malaybalay", "Maramag", "Super Six", 55.00);
        addFare("Malaybalay", "Don Carlos", "Super Six", 70.00);
        addFare("Malaybalay", "Kadingilan", "Super Six", 100.00);

        addFare("Valencia", "Davao City", "RTMI", 280.00);
        addFare("Valencia", "Don Carlos", "Super Six", 35.00);
        addFare("Valencia", "Kadingilan", "Super Six", 65.00);

        addFare("Maramag", "Davao City", "RTMI", 330.00);
        addFare("Maramag", "General Santos", "RTMI", 470.00);
        addFare("Maramag", "Kibawe", "Pabama", 100.00);
        addFare("Maramag", "Don Carlos", "Pabama", 20.00);
        addFare("Maramag", "Don Carlos", "Super Six", 20.00);
        addFare("Maramag", "Kadingilan", "Super Six", 45.00);

        addFare("Gingoog City", "Balingoan", "Pabama", 60.00);
        addFare("Gingoog City", "Balingoan", "Metro Star", 60.00);
        addFare("Gingoog City", "Balingoan", "Bagong Lipunan", 50.00);

        // --- Populate Fallback Distances ---
        fallbackDistances.put("Malaybalay-Quezon", 65.0);
        fallbackDistances.put("Quezon-Malaybalay", 65.0);
        fallbackDistances.put("Maramag-Quezon", 28.0);
        fallbackDistances.put("Quezon-Maramag", 28.0);
        fallbackDistances.put("Davao City-General Santos", 145.0); // Only if not covered by a fixed fare for chosen company
        // Add any other routes that are selectable but might not always have a fixed fare listed for ALL companies
    }

    // Helper method to add fares and their reverses
    private void addFare(String origin, String destination, String company, double fare) {
        routeCompanyFares.computeIfAbsent(origin + "-" + destination, k -> new HashMap<>()).put(company, fare);
        routeCompanyFares.computeIfAbsent(destination + "-" + origin, k -> new HashMap<>()).put(company, fare);
    }

    private void updateAvailableTrips() {
        String from = fromComboBox.getValue(); String to = toComboBox.getValue(); LocalDate date = datePicker.getValue();
        if (from != null && to != null && date != null && !from.equals(to)) {
            List<AvailableTrip> trips = availabilityService.getAvailableTrips(from, to, date);
            availableTripsComboBox.setItems(FXCollections.observableArrayList(trips)); // This is correct
            availableTripsComboBox.setPromptText(trips.isEmpty() ? "No trips available" : "Select an available trip");
        } else {
            if (availableTripsComboBox.getItems() != null) availableTripsComboBox.getItems().clear();
            availableTripsComboBox.setPromptText("Select route and date");
        }
    }

    private void updateAvailableTripsAndSeatButton() {
        updateAvailableTrips();
        updateSeatButtonState();
    }

    private void updateSeatButtonState() {
        AvailableTrip selectedTrip = availableTripsComboBox.getValue();
        if (selectSeatsButton != null) {
            selectSeatsButton.setDisable(selectedTrip == null || selectedTrip.getAvailableSeats() == 0);
        }
    }

    @FXML
    private void onSelectSeatsAction() {
        AvailableTrip selectedTrip = availableTripsComboBox.getValue(); String from = fromComboBox.getValue(); String to = toComboBox.getValue(); LocalDate date = datePicker.getValue();
        if (selectedTrip == null || from == null || to == null || date == null) { Utility.showAlert(Alert.AlertType.WARNING, "Selection Missing", "Please select a trip, route, and date first."); return; }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("SeatSelectionDialog.fxml"));
            Stage dialogStage = new Stage(); dialogStage.setTitle("Select Seats for " + selectedTrip.getBusInfo().getName() + " (" + selectedTrip.getBusInfo().getCompany() + ")");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            if (nameField.getScene() != null && nameField.getScene().getWindow() != null) dialogStage.initOwner(nameField.getScene().getWindow());
            Scene scene = new Scene(loader.load()); dialogStage.setScene(scene);
            SeatSelectionController controller = loader.getController();
            List<String> availableSeatLabels = availabilityService.getAvailableSeatLabelsForTrip(selectedTrip.getBusInfo(), from, to, date);
            controller.setDialogStage(dialogStage); controller.setSeatData(selectedTrip.getBusInfo(), availableSeatLabels);
            dialogStage.showAndWait();
            List<String> chosenSeats = controller.getSelectedSeats();
            if (chosenSeats != null && !chosenSeats.isEmpty()) processBookingWithSelectedSeats(chosenSeats, selectedTrip);
            else System.out.println("Seat selection cancelled or no seats selected.");
        } catch (Exception e) { e.printStackTrace(); Utility.showAlert(Alert.AlertType.ERROR, "Error", "Could not open seat selection dialog or an error occurred: " + e.getMessage());}
    }

    private void processBookingWithSelectedSeats(List<String> selectedSeatLabels, AvailableTrip trip) {
        String name = nameField.getText(); String contact = contactField.getText(); String from = fromComboBox.getValue(); String to = toComboBox.getValue(); LocalDate date = datePicker.getValue();
        BusInfo busInfo = trip.getBusInfo(); String busName = busInfo.getName(); String busCompany = busInfo.getCompany();

        if (name.isEmpty() || contact.isEmpty() || from == null || to == null || date == null || selectedSeatLabels.isEmpty()) { Utility.showAlert(Alert.AlertType.WARNING, "Form Error", "All fields and seat selection are required."); return; }
        if (from.equals(to)) { Utility.showAlert(Alert.AlertType.WARNING, "Form Error", "Origin and Destination cannot be the same."); return; }

        int numSeatsBooked = selectedSeatLabels.size();
        double singleSeatFare = 0.0;
        String routeKey = from + "-" + to; String reverseRouteKey = to + "-" + from;
        boolean fareFound = false;

        Map<String, Double> companyFaresForRoute = routeCompanyFares.get(routeKey);
        if (companyFaresForRoute == null) companyFaresForRoute = routeCompanyFares.get(reverseRouteKey);

        if (companyFaresForRoute != null) {
            if (companyFaresForRoute.containsKey(busCompany)) {
                singleSeatFare = companyFaresForRoute.get(busCompany); fareFound = true;
            } else if (!companyFaresForRoute.isEmpty()) {
                singleSeatFare = companyFaresForRoute.values().iterator().next(); fareFound = true;
                System.out.println("INFO: Fare for company '" + busCompany + "' on route '" + routeKey + "' not found. Using general route fare for this route: " + singleSeatFare);
            }
        }

        if (!fareFound) {
            System.out.println("WARN: No fixed fare for route '" + routeKey + "' (or specific company '" + busCompany + "'). Attempting fallback to distance-based calculation for bus '" + busName + "'.");
            Double distance = fallbackDistances.get(routeKey);
            if (distance == null) distance = fallbackDistances.get(reverseRouteKey);
            if (distance == null) { Utility.showAlert(Alert.AlertType.ERROR, "Fare Error", "Route fare or fallback distance is not defined for '" + routeKey + "'. Please check configuration in populateRouteAndFareData()."); return; }
            double farePerSeatPerKm; double baseFarePerSeat = 20.0;
            if (busName.startsWith("DLX-") || busName.startsWith("PREM-") || busName.startsWith("VIP-")) farePerSeatPerKm = 3.0;
            else if (busName.startsWith("EXP-") || busName.startsWith("ACE-")) farePerSeatPerKm = 2.5;
            else farePerSeatPerKm = 2.0;
            singleSeatFare = baseFarePerSeat + (distance * farePerSeatPerKm);
            System.out.println("WARN: Calculated fallback fare per seat using distance: " + singleSeatFare);
        }

        double totalFareForBooking = singleSeatFare * numSeatsBooked;
        Booking booking = new Booking(name, contact, from, to, busName, date, selectedSeatLabels, totalFareForBooking);
        bookingList.add(booking);
        Utility.showAlert(Alert.AlertType.INFORMATION, "Booking Successful", numSeatsBooked + " seat(s) booked on " + busName + ".\nTotal Fare: ₱" + String.format("%.2f", totalFareForBooking));
        onClearForm();
    }

    @FXML
    private void onAddBooking() {
        if (availableTripsComboBox.getValue() != null) {
            onSelectSeatsAction();
        } else {
            Utility.showAlert(Alert.AlertType.INFORMATION, "Select Trip First", "Please select an available trip from the list, then click 'View/Select Seats'.");
        }
    }

    private void handleRowSelectForCheckout(Booking selectedBooking) {
        if (selectedBooking == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("CheckoutWindow.fxml"));
            Stage checkoutStage = new Stage(); checkoutStage.initModality(Modality.APPLICATION_MODAL);
            checkoutStage.setTitle("Checkout Booking for: " + selectedBooking.getName());
            if (bookingTable.getScene() != null && bookingTable.getScene().getWindow() != null) checkoutStage.initOwner(bookingTable.getScene().getWindow());
            Scene scene = new Scene(loader.load()); checkoutStage.setScene(scene);
            CheckoutWindowController controller = loader.getController();
            controller.setStage(checkoutStage); controller.setBooking(selectedBooking);
            checkoutStage.showAndWait();
        } catch (Exception e) { e.printStackTrace(); Utility.showAlert(Alert.AlertType.ERROR, "Error", "Could not open checkout window: " + e.getMessage());}
    }

    @FXML
    private void onClearForm() {
        Utility.clearFields(nameField, contactField);
        fromComboBox.setValue(null); toComboBox.setValue(null); Utility.clearDatePicker(datePicker);
        availableTripsComboBox.setValue(null);
        if(availableTripsComboBox.getItems() != null) availableTripsComboBox.getItems().clear();
        availableTripsComboBox.setPromptText("Select route and date first");
        if(selectSeatsButton != null) selectSeatsButton.setDisable(true); // defensive null check
        if(selectedFromLabel != null) selectedFromLabel.setText("N/A");
        if(selectedToLabel != null) selectedToLabel.setText("N/A");
        updateAvailableTripsAndSeatButton();
    }

    @FXML
    private void onExit() { System.exit(0); }
}