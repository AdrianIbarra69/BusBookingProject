package app.busbookingproject;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class BusBookingController {

    // TableView and columns
    @FXML
    private TableView<Booking> bookingTable;

    @FXML
    private TableColumn<Booking, String> nameColumn;

    @FXML
    private TableColumn<Booking, String> contactColumn;

    @FXML
    private TableColumn<Booking, String> fromColumn;

    @FXML
    private TableColumn<Booking, String> toColumn;

    @FXML
    private TableColumn<Booking, String> busNameColumn;

    @FXML
    private TableColumn<Booking, LocalDate> dateColumn;

    @FXML
    private TableColumn<Booking, Integer> seatsColumn;

    @FXML
    private TableColumn<Booking, String> fareColumn; // String for formatted fare

    // Form fields
    @FXML
    private TextField nameField;

    @FXML
    private TextField contactField;

    @FXML
    private ComboBox<String> fromComboBox;

    @FXML
    private ComboBox<String> toComboBox;

    @FXML
    private ComboBox<String> busNameComboBox;

    @FXML
    private ComboBox<Integer> seatComboBox;

    @FXML
    private DatePicker datePicker;

    // Data for the TableView
    private final ObservableList<Booking> bookingList = FXCollections.observableArrayList();

    // Distance map to calculate fare (in kilometers between cities)
    private final Map<String, Integer> distances = new HashMap<>();

    @FXML
    public void initialize() {
        // Initialize TableView columns
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        contactColumn.setCellValueFactory(new PropertyValueFactory<>("contact"));
        fromColumn.setCellValueFactory(new PropertyValueFactory<>("from"));
        toColumn.setCellValueFactory(new PropertyValueFactory<>("to"));
        busNameColumn.setCellValueFactory(new PropertyValueFactory<>("busName"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        seatsColumn.setCellValueFactory(new PropertyValueFactory<>("seats"));
        fareColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getFormattedFare()));

        // Populate ComboBox choices and distances
        populateComboBoxes();
        populateDistances();

        // Bind data to the TableView
        bookingTable.setItems(bookingList);
    }

    private void populateComboBoxes() {
        // Cities in Mindanao (including added locations)
        ObservableList<String> locations = FXCollections.observableArrayList(
                "Davao", "Cagayan de Oro", "Zamboanga", "General Santos", "Butuan",
                "Valencia", "Quezon", "Malaybalay"
        );

        fromComboBox.setItems(locations);
        toComboBox.setItems(locations);

        // Bus names
        ObservableList<String> buses = FXCollections.observableArrayList(
                "Mindanao Express", "Southern Comfort Lines", "Island Transport", "SkyLiner"
        );
        busNameComboBox.setItems(buses);

        // Seat options
        ObservableList<Integer> seatOptions = FXCollections.observableArrayList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        seatComboBox.setItems(seatOptions);
    }

    private void populateDistances() {
        // Populate distances for major cities (in km)
        distances.put("Davao-Cagayan de Oro", 274);
        distances.put("Davao-Zamboanga", 760);
        distances.put("Davao-General Santos", 150);
        distances.put("Davao-Butuan", 283);
        distances.put("Cagayan de Oro-Zamboanga", 475);
        distances.put("Cagayan de Oro-General Santos", 300);
        distances.put("Cagayan de Oro-Butuan", 147);
        distances.put("Zamboanga-General Santos", 610);
        distances.put("Zamboanga-Butuan", 830);
        distances.put("General Santos-Butuan", 430);

        // Added locations: Valencia, Quezon, Malaybalay
        distances.put("Valencia-Cagayan de Oro", 118);
        distances.put("Valencia-Malaybalay", 27);
        distances.put("Valencia-Davao", 280);
        distances.put("Valencia-Zamboanga", 820);
        distances.put("Valencia-General Santos", 295);
        distances.put("Valencia-Butuan", 190);

        // Add Quezon-Valencia and reverse
        distances.put("Quezon-Valencia", 50);  // Add the correct distance
        distances.put("Valencia-Quezon", 50); // Reverse route

        // Other Quezon-related distances
        distances.put("Quezon-Davao", 300);
        distances.put("Quezon-Cagayan de Oro", 140);
        distances.put("Quezon-Malaybalay", 35);
        distances.put("Malaybalay-Cagayan de Oro", 91);
        distances.put("Malaybalay-Davao", 270);
        distances.put("Malaybalay-General Santos", 287);
    }

    @FXML
    private void onAddBooking() {
        try {
            // Get and validate user inputs
            String name = nameField.getText();
            String contact = contactField.getText();
            String from = fromComboBox.getValue();
            String to = toComboBox.getValue();
            String busName = busNameComboBox.getValue();
            LocalDate date = datePicker.getValue();
            Integer seats = seatComboBox.getValue();

            // Validation
            if (name.isEmpty() || contact.isEmpty() || from == null || to == null || busName == null || date == null || seats == null) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "Please fill out all fields!");
                return;
            }

            if (from.equals(to)) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "Origin and destination cannot be the same!");
                return;
            }

            // Calculate the distance-based fare (no multiplication by number of seats)
            String key = from + "-" + to;
            if (!distances.containsKey(key)) {
                key = to + "-" + from; // Check reverse route
            }

            // If no entry is found for the route, display an error.
            if (!distances.containsKey(key)) {
                showAlert(Alert.AlertType.ERROR, "Distance Error", "No valid route found between selected cities!");
                return;
            }

            int distance = distances.get(key);
            double fareRatePerKm = 2.50; // Base fare rate in PHP per kilometer

            // Apply a specific rule for the Quezon to Valencia route
            if ((from.equals("Quezon") && to.equals("Valencia")) || (from.equals("Valencia") && to.equals("Quezon"))) {
                fareRatePerKm = 2.00; // Discounted rate for this route
            }

            double fare = distance * fareRatePerKm; // Calculated fare

            // Create new booking
            Booking booking = new Booking(name, contact, from, to, busName, date, seats, fare);
            bookingList.add(booking);

            // Clear form fields
            onClearForm();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to add booking! Please ensure all fields are filled out correctly.");
        }
    }

    @FXML
    private void onClearForm() {
        // Clear all form fields
        nameField.clear();
        contactField.clear();
        fromComboBox.setValue(null);
        toComboBox.setValue(null);
        busNameComboBox.setValue(null);
        seatComboBox.setValue(null);
        datePicker.setValue(null);
    }

    @FXML
    private void onExit() {
        System.exit(0);
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}