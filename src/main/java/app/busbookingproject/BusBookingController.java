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
    private TableColumn<Booking, String> fareColumn;

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

    // Checkout and Payment fields
    @FXML
    private Label totalFareLabel;
    @FXML
    private TextField paymentField;
    @FXML
    private Label paymentStatusLabel;

    // Dynamic selection output
    @FXML
    private Label selectedFromLabel;
    @FXML
    private Label selectedToLabel;
    @FXML
    private Label selectedBusLabel;
    @FXML
    private Label selectedSeatsLabel;

    // Data for the TableView
    private final ObservableList<Booking> bookingList = FXCollections.observableArrayList();

    // Distance map for route distances
    private final Map<String, Double> distances = new HashMap<>();

    @FXML
    public void initialize() {
        populateComboBoxes();

        // Bind booking data to the TableView
        bookingTable.setItems(bookingList);

        // Set TableView column mappings
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        contactColumn.setCellValueFactory(new PropertyValueFactory<>("contact"));
        fromColumn.setCellValueFactory(new PropertyValueFactory<>("from"));
        toColumn.setCellValueFactory(new PropertyValueFactory<>("to"));
        busNameColumn.setCellValueFactory(new PropertyValueFactory<>("busName"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        seatsColumn.setCellValueFactory(new PropertyValueFactory<>("seats"));
        fareColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getFormattedFare()));

        // Update output labels dynamically
        fromComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            selectedFromLabel.setText(newVal != null ? newVal : "N/A");
        });

        toComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            selectedToLabel.setText(newVal != null ? newVal : "N/A");
        });

        busNameComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            selectedBusLabel.setText(newVal != null ? newVal : "N/A");
        });

        seatComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            selectedSeatsLabel.setText(newVal != null ? newVal.toString() : "0");
        });

        calculateTotalFare(); // Initialize fare label
    }

    private void populateComboBoxes() {
        // Adding destinations to ComboBoxes
        fromComboBox.setItems(FXCollections.observableArrayList(
                "Davao City", "Cagayan de Oro", "General Santos", "Zamboanga City",
                "Butuan", "Iligan", "Maramag", "Malaybalay", "Quezon", "Valencia"
        ));
        toComboBox.setItems(FXCollections.observableArrayList(
                "Davao City", "Cagayan de Oro", "General Santos", "Zamboanga City",
                "Butuan", "Iligan", "Maramag", "Malaybalay", "Quezon", "Valencia"
        ));
        busNameComboBox.setItems(FXCollections.observableArrayList("Bus 101", "Bus 202", "Bus 303"));
        seatComboBox.setItems(FXCollections.observableArrayList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20)); // Available seats

        // Define distances between destinations (in km)
        distances.put("Davao City-Cagayan de Oro", 233.0);
        distances.put("Davao City-General Santos", 145.0);
        distances.put("Davao City-Zamboanga City", 626.0);
        distances.put("Davao City-Cagayan de Oro", 233.0); // Add this if not already included
        distances.put("Cagayan de Oro-Davao City", 233.0); // Add reverse route
        distances.put("Davao City-Butuan", 230.0);
        distances.put("Davao City-Iligan", 289.0);
        distances.put("Davao City-Maramag", 172.0);
        distances.put("Davao City-Malaybalay", 192.0);
        distances.put("Davao City-Quezon", 160.0);
        distances.put("Davao City-Valencia", 180.0);

        distances.put("Cagayan de Oro-Malaybalay", 112.0);
        distances.put("Cagayan de Oro-Iligan", 88.0);
        distances.put("Cagayan de Oro-Valencia", 102.0); // Added route (Cagayan to Valencia)
        distances.put("Valencia-Cagayan de Oro", 102.0); // Added reverse route (Valencia to Cagayan)
        distances.put("Malaybalay-Valencia", 25.0);
        distances.put("Valencia-Malaybalay", 25.0); // Reverse route

        distances.put("Quezon-Valencia", 38.0);
        distances.put("Maramag-Valencia", 15.0);
    }

    @FXML
    private void onAddBooking() {
        try {
            // Validate input fields
            String name = nameField.getText();
            String contact = contactField.getText();
            String from = fromComboBox.getValue();
            String to = toComboBox.getValue();
            String busName = busNameComboBox.getValue();
            Integer seats = seatComboBox.getValue();
            LocalDate date = datePicker.getValue();

            if (name.isEmpty() || contact.isEmpty() || from == null || to == null || busName == null || seats == null || date == null) {
                showAlert(Alert.AlertType.WARNING, "Form Error", "All fields must be filled out!");
                return;
            }

            if (from.equals(to)) {
                showAlert(Alert.AlertType.WARNING, "Form Error", "Origin and Destination cannot be the same!");
                return;
            }

            // Calculate fare based on distance
            String route = from + "-" + to;
            if (!distances.containsKey(route)) {
                showAlert(Alert.AlertType.ERROR, "Invalid Route", "This route is not available or defined.");
                return;
            }
            double farePerKm = 5.0; // Reduced fare per km
            double totalFare = distances.get(route) * farePerKm;

            // Add booking to the table
            Booking booking = new Booking(name, contact, from, to, busName, date, seats, totalFare);
            bookingList.add(booking);

            // Update total fare
            calculateTotalFare();

            // Clear the form
            onClearForm();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "An unexpected error occurred: " + e.getMessage());
        }
    }

    @FXML
    private void onClearForm() {
        nameField.clear();
        contactField.clear();
        fromComboBox.setValue(null);
        toComboBox.setValue(null);
        busNameComboBox.setValue(null);
        seatComboBox.setValue(null);
        datePicker.setValue(null);

        // Reset labels
        selectedFromLabel.setText("N/A");
        selectedToLabel.setText("N/A");
        selectedBusLabel.setText("N/A");
        selectedSeatsLabel.setText("0");
    }

    @FXML
    private void onCheckout() {
        try {
            double payment = Double.parseDouble(paymentField.getText().trim());
            double totalFare = calculateTotalFare();

            if (payment < totalFare) {
                paymentStatusLabel.setStyle("-fx-text-fill: red;");
                paymentStatusLabel.setText("Insufficient payment. Please provide more.");
            } else {
                double change = payment - totalFare;
                paymentStatusLabel.setStyle("-fx-text-fill: green;");
                paymentStatusLabel.setText("Payment successful! Change: ₱" + String.format("%.2f", change));
                paymentField.clear();
            }
        } catch (NumberFormatException e) {
            paymentStatusLabel.setStyle("-fx-text-fill: red;");
            paymentStatusLabel.setText("Invalid payment amount. Please enter a numeric value.");
        }
    }

    private double calculateTotalFare() {
        double totalFare = 0;

        for (Booking booking : bookingList) {
            totalFare += booking.getFare();
        }

        totalFareLabel.setText("₱" + String.format("%.2f", totalFare));
        return totalFare;
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void onExit() {
        System.exit(0);
    }
}