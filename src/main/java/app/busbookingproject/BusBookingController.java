package app.busbookingproject;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections; // Added for sorting
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale; // For time parsing

public class BusBookingController {

    @FXML private TableView<Booking> bookingTable;
    @FXML private TableColumn<Booking, String> nameColumn;
    @FXML private TableColumn<Booking, String> contactColumn;
    @FXML private TableColumn<Booking, String> fromColumn;
    @FXML private TableColumn<Booking, String> toColumn;
    @FXML private TableColumn<Booking, String> busNameColumn;
    @FXML private TableColumn<Booking, LocalDate> dateColumn;
    @FXML private TableColumn<Booking, String> seatsColumn;
    @FXML private TableColumn<Booking, String> fareColumn;
    @FXML private TableColumn<Booking, String> paymentStatusColumn;
    @FXML private TableColumn<Booking, String> changeColumn;
    @FXML private TableColumn<Booking, Void> actionColumn;
    @FXML private TableColumn<Booking, Void> viewTicketColumn;

    @FXML private TextField nameField;
    @FXML private TextField contactField;
    @FXML private ComboBox<String> fromComboBox;
    @FXML private ComboBox<String> toComboBox;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<AvailableTrip> availableTripsComboBox;
    @FXML private Button selectSeatsButton;

    @FXML private Label selectedFromLabel;
    @FXML private Label selectedToLabel;

    private final ObservableList<Booking> bookingList = FXCollections.observableArrayList();
    private final Map<String, Map<String, Double>> routeCompanyFares = new HashMap<>();
    private final Map<String, Double> fallbackDistances = new HashMap<>();
    private BusAvailabilityService availabilityService;
    private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH);


    @FXML
    public void initialize() {
        System.out.println("DEBUG BusBookingController: Is viewTicketColumn null in initialize()? " + (viewTicketColumn == null));

        availabilityService = new BusAvailabilityService(bookingList);
        populateRouteAndFareData();
        setupTableColumns();

        fromComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if(selectedFromLabel != null) selectedFromLabel.setText(newVal != null ? newVal : "N/A");
            updateAvailableTripsAndSeatButton();
        });
        toComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if(selectedToLabel != null) selectedToLabel.setText(newVal != null ? newVal : "N/A");
            updateAvailableTripsAndSeatButton();
        });
        datePicker.valueProperty().addListener((obs, oldVal, newVal) -> updateAvailableTripsAndSeatButton());
        availableTripsComboBox.valueProperty().addListener((obs, oldVal, newVal) -> updateSeatButtonState());

        availableTripsComboBox.setItems(FXCollections.observableArrayList());
        availableTripsComboBox.setPromptText("Select route and date first");
        if (selectSeatsButton != null) selectSeatsButton.setDisable(true);
    }

    private void setupTableColumns() {
        if (bookingTable != null) {
            bookingTable.setItems(bookingList);
        }
        if (nameColumn != null) nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        if (contactColumn != null) contactColumn.setCellValueFactory(new PropertyValueFactory<>("contact"));
        if (fromColumn != null) fromColumn.setCellValueFactory(new PropertyValueFactory<>("from"));
        if (toColumn != null) toColumn.setCellValueFactory(new PropertyValueFactory<>("to"));
        if (busNameColumn != null) busNameColumn.setCellValueFactory(new PropertyValueFactory<>("busName"));
        if (dateColumn != null) dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        if (seatsColumn != null) seatsColumn.setCellValueFactory(cellData -> cellData.getValue().seatsDisplayProperty());
        if (fareColumn != null) fareColumn.setCellValueFactory(cellData -> cellData.getValue().formattedFareProperty());

        if (paymentStatusColumn != null) {
            paymentStatusColumn.setCellValueFactory(cellData -> cellData.getValue().paymentStatusProperty());
            paymentStatusColumn.setCellFactory(column -> new TableCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) { setText(null); setStyle(""); }
                    else { setText(item); setStyle("-fx-font-weight: bold; " + ("Paid".equalsIgnoreCase(item) ? "-fx-text-fill: green;" : "-fx-text-fill: red;")); }
                }
            });
        }

        if (changeColumn != null) changeColumn.setCellValueFactory(cellData -> cellData.getValue().changeGivenProperty());

        if (actionColumn != null) {
            Callback<TableColumn<Booking, Void>, TableCell<Booking, Void>> actionCellFactory = param -> {
                final TableCell<Booking, Void> cell = new TableCell<>() {
                    private final Button btn = new Button("Checkout");
                    {
                        btn.setOnAction(event -> {
                            Booking booking = null;
                            if (getIndex() >= 0 && getIndex() < getTableView().getItems().size()) booking = getTableView().getItems().get(getIndex());
                            if (booking != null) {
                                if ("Not Paid".equalsIgnoreCase(booking.getPaymentStatus())) handleRowSelectForCheckout(booking);
                                else Utility.showAlert(Alert.AlertType.INFORMATION, "Booking Paid", "This booking has already been paid.");
                            }
                        });
                    }
                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || getTableRow() == null || getTableRow().getItem() == null) setGraphic(null);
                        else {
                            Booking booking = (Booking) getTableRow().getItem();
                            btn.setDisable("Paid".equalsIgnoreCase(booking.getPaymentStatus()));
                            btn.setText("Paid".equalsIgnoreCase(booking.getPaymentStatus()) ? "Paid" : "Checkout");
                            setGraphic(btn); setAlignment(Pos.CENTER);
                        }
                    }
                };
                return cell;
            };
            actionColumn.setCellFactory(actionCellFactory);
        }

        if (viewTicketColumn != null) {
            Callback<TableColumn<Booking, Void>, TableCell<Booking, Void>> viewTicketCellFactory = param -> {
                final TableCell<Booking, Void> cell = new TableCell<>() {
                    private final Button btn = new Button("View Ticket");
                    {
                        btn.setOnAction(event -> {
                            Booking booking = null;
                            if (getIndex() >= 0 && getIndex() < getTableView().getItems().size()) booking = getTableView().getItems().get(getIndex());

                            if (booking != null && "Paid".equalsIgnoreCase(booking.getPaymentStatus()) &&
                                    booking.getTicketNumber() != null && !booking.getTicketNumber().equals("TBD") && !booking.getTicketNumber().isEmpty()) {

                                System.out.println("DEBUG BusBookingController: 'View Ticket' button clicked for: " + booking.getName());
                                String qrCodeText = "Ticket: " + booking.getTicketNumber() + "\nPassenger: " + booking.getName() + "\nRoute: " + booking.getFrom() + " to " + booking.getTo() + "\nDate: " + booking.getDate().toString() + " " + booking.getDepartureTime() + "\nBus: " + booking.getBusName() + "\nSeats: " + String.join(", ", booking.getBookedSeatLabels());
                                Image qrImage = QRCodeGenerator.generateQRCodeImage(qrCodeText, 120, 120);
                                String paymentMethodUsed = "Paid";
                                showTicketView(booking, paymentMethodUsed, qrImage);
                            } else {
                                System.out.println("DEBUG BusBookingController: 'View Ticket' button clicked, but conditions not met for booking: " + (booking != null ? booking.getName() : "null booking"));
                                if (booking != null) {
                                    System.out.println("  Status: " + booking.getPaymentStatus() + ", TicketNo: " + booking.getTicketNumber());
                                }
                                Utility.showAlert(Alert.AlertType.INFORMATION, "Ticket Not Ready", "Ticket can only be viewed after successful payment and ticket generation.");
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
                            boolean isPaid = "Paid".equalsIgnoreCase(booking.getPaymentStatus());
                            boolean isTicketNumberValid = booking.getTicketNumber() != null && !booking.getTicketNumber().equals("TBD") && !booking.getTicketNumber().isEmpty();
                            if (isPaid && isTicketNumberValid) {
                                setGraphic(btn);
                                setAlignment(Pos.CENTER);
                            } else {
                                setGraphic(null);
                            }
                        }
                    }
                };
                return cell;
            };
            viewTicketColumn.setCellFactory(viewTicketCellFactory);
        } else {
            System.err.println("ERROR BusBookingController: viewTicketColumn is NULL in setupTableColumns. Check fx:id in dashboard.fxml and fx:controller.");
        }
    }

    private void populateRouteAndFareData() {
        ObservableList<String> locations = FXCollections.observableArrayList(
                "Cagayan de Oro", "Davao City", "General Santos", "Zamboanga City", "Iligan", "Butuan",
                "Pagadian City", "Dipolog City", "Oroquieta City", "Ozamiz City",
                "Maramag", "Valencia", "Malaybalay", "Tacurong City",
                "Gingoog City", "Balingoan", "Kibawe", "Don Carlos", "Kadingilan", "Quezon",
                "Laguindingan"
        );

        // Sort the locations alphabetically
        Collections.sort(locations);

        if (fromComboBox != null) fromComboBox.setItems(locations);
        if (toComboBox != null) toComboBox.setItems(locations);

        // Existing Fares
        addFare("Cagayan de Oro", "Zamboanga City", "RTMI", 1000.00);
        addFare("Cagayan de Oro", "Iligan", "RTMI", 100.00);
        addFare("Cagayan de Oro", "Iligan", "Super 5", 115.00);
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
        // ... (ensure all your addFare calls are complete) ...


        // Fallback Distances
        fallbackDistances.put("Malaybalay-Quezon", 65.0);
        fallbackDistances.put("Quezon-Malaybalay", 65.0);
        fallbackDistances.put("Maramag-Quezon", 28.0);
        fallbackDistances.put("Quezon-Maramag", 28.0);
        fallbackDistances.put("Davao City-Valencia", 180.0);
        fallbackDistances.put("Valencia-Davao City", 180.0);
        fallbackDistances.put("Davao City-Quezon", 160.0);
        fallbackDistances.put("Quezon-Davao City", 160.0);
        fallbackDistances.put("Butuan-Davao City", 230.0);
        fallbackDistances.put("Iligan-Davao City", 289.0);

        fallbackDistances.put("Davao City-Cagayan de Oro", 250.0);
        fallbackDistances.put("Cagayan de Oro-Davao City", 250.0);

        for (Map.Entry<String, Double> entry : new HashMap<>(fallbackDistances).entrySet()) {
            String[] parts = entry.getKey().split("-");
            if (parts.length == 2) {
                fallbackDistances.putIfAbsent(parts[1] + "-" + parts[0], entry.getValue());
            }
        }
    }

    private void addFare(String origin, String destination, String company, double fare) {
        routeCompanyFares.computeIfAbsent(origin + "-" + destination, k -> new HashMap<>()).put(company, fare);
        routeCompanyFares.computeIfAbsent(destination + "-" + origin, k -> new HashMap<>()).put(company, fare);
    }

    private void updateAvailableTrips() {
        String from = fromComboBox.getValue();
        String to = toComboBox.getValue();
        LocalDate date = datePicker.getValue();
        if (from != null && to != null && date != null && !from.equals(to)) {
            List<AvailableTrip> trips = availabilityService.getAvailableTrips(from, to, date);
            availableTripsComboBox.setItems(FXCollections.observableArrayList(trips));
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
        AvailableTrip selectedTrip = availableTripsComboBox.getValue();
        String from = fromComboBox.getValue();
        String to = toComboBox.getValue();
        LocalDate date = datePicker.getValue();

        if (selectedTrip == null || from == null || to == null || date == null) {
            Utility.showAlert(Alert.AlertType.WARNING, "Selection Missing", "Please select a trip, route, and date first.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("SeatSelectionDialog.fxml"));
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Select Seats for " + selectedTrip.getBusInfo().getName() + " (" + selectedTrip.getBusInfo().getCompany() + ")");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            if (nameField != null && nameField.getScene() != null && nameField.getScene().getWindow() != null) {
                dialogStage.initOwner(nameField.getScene().getWindow());
            }
            Scene scene = new Scene(loader.load());
            dialogStage.setScene(scene);
            SeatSelectionController controller = loader.getController();

            List<String> availableSeatLabels = availabilityService.getAvailableSeatLabelsForTrip(
                    selectedTrip.getBusInfo(),
                    from,
                    to,
                    date,
                    selectedTrip.getDepartureTime()
            );

            controller.setDialogStage(dialogStage);
            controller.setSeatData(selectedTrip.getBusInfo(), availableSeatLabels);
            dialogStage.showAndWait();
            List<String> chosenSeats = controller.getSelectedSeats();
            if (chosenSeats != null && !chosenSeats.isEmpty()) {
                processBookingWithSelectedSeats(chosenSeats, selectedTrip);
            } else {
                System.out.println("Seat selection cancelled or no seats selected.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Utility.showAlert(Alert.AlertType.ERROR, "Error", "Could not open seat selection dialog: " + e.getMessage());
        }
    }

    private void processBookingWithSelectedSeats(List<String> selectedSeatLabels, AvailableTrip trip) {
        String name = nameField.getText();
        String contact = contactField.getText();
        String from = fromComboBox.getValue();
        String to = toComboBox.getValue();
        LocalDate date = datePicker.getValue();
        BusInfo busInfo = trip.getBusInfo();
        String busName = busInfo.getName();
        String busCompany = busInfo.getCompany();

        if (name.isEmpty() || contact.isEmpty() || from == null || to == null || date == null || selectedSeatLabels.isEmpty()) {
            Utility.showAlert(Alert.AlertType.WARNING, "Form Error", "All fields and seat selection are required.");
            return;
        }
        if (from.equals(to)) {
            Utility.showAlert(Alert.AlertType.WARNING, "Form Error", "Origin and Destination cannot be the same.");
            return;
        }

        int numSeatsBooked = selectedSeatLabels.size();
        double singleSeatFare = 0.0;
        String routeKey = from + "-" + to;
        String reverseRouteKey = to + "-" + from;
        boolean fareFound = false;
        Map<String, Double> companyFaresForRoute = routeCompanyFares.get(routeKey);
        if (companyFaresForRoute == null) companyFaresForRoute = routeCompanyFares.get(reverseRouteKey);

        if (companyFaresForRoute != null) {
            if (companyFaresForRoute.containsKey(busCompany)) {
                singleSeatFare = companyFaresForRoute.get(busCompany);
                fareFound = true;
            } else if (!companyFaresForRoute.isEmpty()) {
                singleSeatFare = companyFaresForRoute.values().iterator().next();
                fareFound = true;
                System.out.println("INFO: Fare for company '" + busCompany + "' on route '" + routeKey + "' not found. Using general route fare: " + singleSeatFare);
            }
        }

        if (!fareFound) {
            System.out.println("WARN: No fixed fare for route '" + routeKey + "' for company '" + busCompany + "'. Falling back to distance for bus '" + busName + "'.");
            Double distance = fallbackDistances.get(routeKey);
            if (distance == null) {
                Utility.showAlert(Alert.AlertType.ERROR, "Fare Error", "Route fare or fallback distance not defined for '" + routeKey + "'. Check populateRouteAndFareData().");
                return;
            }
            double farePerSeatPerKm;
            double baseFarePerSeat = 20.0;
            if (busName.startsWith("DLX-") || busName.startsWith("PREM-") || busName.startsWith("VIP-")) farePerSeatPerKm = 3.0;
            else if (busName.startsWith("EXP-") || busName.startsWith("ACE-")) farePerSeatPerKm = 2.5;
            else farePerSeatPerKm = 2.0;
            singleSeatFare = baseFarePerSeat + (distance * farePerSeatPerKm);
            System.out.println("INFO: Calculated fallback fare per seat using distance " + distance + "km: " + singleSeatFare);
        }

        double totalFareForBooking = singleSeatFare * numSeatsBooked;
        Booking booking = new Booking(name, contact, from, to, busName, date, selectedSeatLabels, totalFareForBooking);

        booking.setDepartureTime(trip.getDepartureTime());

        try {
            LocalTime departureLocalTime = LocalTime.parse(trip.getDepartureTime(), timeFormatter);
            LocalTime arrivalLocalTime = departureLocalTime.plusHours(trip.getEstimatedTravelDurationHours());
            booking.setBusArrivalTime(arrivalLocalTime.format(timeFormatter));
        } catch (DateTimeParseException e) {
            System.err.println("Error parsing departure time for arrival calculation: " + trip.getDepartureTime() + " - " + e.getMessage());
            booking.setBusArrivalTime("Calc. Error");
        }

        bookingList.add(booking);
        Utility.showAlert(Alert.AlertType.INFORMATION, "Booking Successful", numSeatsBooked + " seat(s) booked on " + busName + ".\nTotal Fare: ₱" + String.format("%.2f", totalFareForBooking) + "\nDeparture: " + booking.getDepartureTime() + "\nEst. Arrival: " + booking.getBusArrivalTime());
        onClearForm();
    }

    @FXML
    private void onAddBooking() {
        if (availableTripsComboBox.getValue() != null) {
            onSelectSeatsAction();
        } else {
            Utility.showAlert(Alert.AlertType.INFORMATION, "Select Trip First", "Please select an available trip, then click 'View/Select Seats'.");
        }
    }

    private void handleRowSelectForCheckout(Booking selectedBooking) {
        if (selectedBooking == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("CheckoutWindow.fxml"));
            Stage checkoutStage = new Stage();
            checkoutStage.initModality(Modality.APPLICATION_MODAL);
            checkoutStage.setTitle("Checkout Booking for: " + selectedBooking.getName());
            if (bookingTable != null && bookingTable.getScene() != null && bookingTable.getScene().getWindow() != null) {
                checkoutStage.initOwner(bookingTable.getScene().getWindow());
            }
            Scene scene = new Scene(loader.load());
            checkoutStage.setScene(scene);
            CheckoutWindowController controller = loader.getController();
            controller.setStage(checkoutStage);
            controller.setBooking(selectedBooking);
            checkoutStage.showAndWait();

            if (bookingTable != null) {
                bookingTable.refresh();
            }

        } catch (Exception e) {
            e.printStackTrace();
            Utility.showAlert(Alert.AlertType.ERROR, "Error", "Could not open checkout window: " + e.getMessage());
        }
    }

    private void showTicketView(Booking booking, String paymentMethod, Image qrCodeImage) {
        System.out.println("DEBUG BusBookingController: Attempting to show ticket for: " + (booking != null ? booking.getTicketNumber() : "null booking"));
        if (booking == null) {
            Utility.showAlert(Alert.AlertType.ERROR, "Ticket Error", "Booking data is null.");
            return;
        }

        try {
            java.net.URL fxmlUrl = getClass().getResource("TicketView.fxml");
            if (fxmlUrl == null) {
                System.err.println("ERROR BusBookingController: TicketView.fxml NOT FOUND in resources path!");
                Utility.showAlert(Alert.AlertType.ERROR, "FXML Error", "TicketView.fxml not found. Check file location and build path.");
                return;
            }
            System.out.println("DEBUG BusBookingController: TicketView.fxml found at: " + fxmlUrl.toExternalForm());

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Stage ticketStage = new Stage();
            ticketStage.initModality(Modality.WINDOW_MODAL);

            if (bookingTable != null && bookingTable.getScene() != null && bookingTable.getScene().getWindow() != null) {
                ticketStage.initOwner(bookingTable.getScene().getWindow());
            } else {
                System.out.println("DEBUG BusBookingController: Cannot set owner for ticketStage, bookingTable or its window is null.");
            }
            ticketStage.setTitle("Bus Ticket - " + booking.getTicketNumber());

            Scene scene = new Scene(loader.load());
            System.out.println("DEBUG BusBookingController: TicketView.fxml loaded into Scene successfully.");
            ticketStage.setScene(scene);

            TicketViewController controller = loader.getController();
            if (controller == null) {
                System.err.println("ERROR BusBookingController: TicketViewController is NULL after loading FXML. Check fx:controller in TicketView.fxml.");
                Utility.showAlert(Alert.AlertType.ERROR, "Controller Error", "Could not get controller for TicketView.");
                return;
            }
            System.out.println("DEBUG BusBookingController: TicketViewController obtained successfully.");

            controller.setDialogStage(ticketStage);
            System.out.println("DEBUG BusBookingController: Calling setTicketDetails on TicketViewController.");
            controller.setTicketDetails(booking, paymentMethod, qrCodeImage);
            System.out.println("DEBUG BusBookingController: setTicketDetails finished.");

            ticketStage.show();
            System.out.println("DEBUG BusBookingController: Ticket stage shown.");

        } catch (IOException e) {
            System.err.println("CRITICAL ERROR BusBookingController: IOException while loading or setting up TicketView.fxml!");
            e.printStackTrace();
            Utility.showAlert(Alert.AlertType.ERROR, "Ticket Load Error", "Could not display the ticket due to an IO Error: " + e.getMessage() + "\n(Check console for details)");
        } catch (Exception e) {
            System.err.println("CRITICAL ERROR BusBookingController: Unexpected exception in showTicketView!");
            e.printStackTrace();
            Utility.showAlert(Alert.AlertType.ERROR, "Unexpected Ticket Error", "An unexpected error occurred: " + e.getMessage() + "\n(Check console for details)");
        }
    }

    @FXML
    private void onClearForm() {
        Utility.clearFields(nameField, contactField);
        if (fromComboBox != null) fromComboBox.setValue(null);
        if (toComboBox != null) toComboBox.setValue(null);
        if (datePicker != null) Utility.clearDatePicker(datePicker);
        if (availableTripsComboBox != null ) {
            availableTripsComboBox.setValue(null);
            if(availableTripsComboBox.getItems() != null) availableTripsComboBox.getItems().clear();
            availableTripsComboBox.setPromptText("Select route and date first");
        }
        if(selectSeatsButton != null) selectSeatsButton.setDisable(true);
        if(selectedFromLabel != null) selectedFromLabel.setText("N/A");
        if(selectedToLabel != null) selectedToLabel.setText("N/A");
        updateAvailableTripsAndSeatButton();
    }

    @FXML
    private void onExit() { System.exit(0); }
}
