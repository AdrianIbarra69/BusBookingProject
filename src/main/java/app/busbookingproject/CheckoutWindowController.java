package app.busbookingproject;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.image.ImageView; // Only need ImageView here, Image is in PaymentMethodDisplay
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CheckoutWindowController {

    @FXML private Label nameLabel;
    @FXML private Label contactLabel;
    @FXML private Label fromLabel;
    @FXML private Label toLabel;
    @FXML private Label busNameLabel;
    @FXML private Label dateLabel;
    @FXML private Label seatsLabel;
    @FXML private Label totalFareLabel;
    @FXML private TextField paymentField;
    @FXML private Label paymentStatusLabel;
    @FXML private Button processPaymentButton;

    @FXML
    private ComboBox<PaymentMethodDisplay> paymentMethodComboBox;

    private Booking currentBooking;
    private Stage stage;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setBooking(Booking booking) {
        this.currentBooking = booking;
        populateBookingDetails();
        populatePaymentMethods();

        if (currentBooking != null) {
            boolean isPaid = "Paid".equalsIgnoreCase(currentBooking.getPaymentStatus());
            paymentField.setDisable(isPaid);
            if (paymentMethodComboBox != null) paymentMethodComboBox.setDisable(isPaid);
            if (isPaid) paymentField.setText("PAID"); else paymentField.setText("");
            if (processPaymentButton != null) processPaymentButton.setDisable(isPaid);
            paymentStatusLabel.setText(isPaid ? "Status: Paid. Change: " + currentBooking.getChangeGiven() : "Status: Not Paid");
            paymentStatusLabel.setStyle(isPaid ? "-fx-text-fill: green;" : "-fx-text-fill: red;");
        }
    }

    private void populateBookingDetails() {
        if (currentBooking != null) {
            nameLabel.setText(currentBooking.getName());
            contactLabel.setText(currentBooking.getContact());
            fromLabel.setText(currentBooking.getFrom());
            toLabel.setText(currentBooking.getTo());
            busNameLabel.setText(currentBooking.getBusName());
            dateLabel.setText(currentBooking.getDate().toString());
            seatsLabel.setText(String.valueOf(currentBooking.getNumberOfSeatsBooked()));
            totalFareLabel.setText(currentBooking.getFormattedFare());
        }
    }

    private void populatePaymentMethods() {
        if (paymentMethodComboBox != null) {
            List<PaymentMethodDisplay> methods = new ArrayList<>();
            System.out.println("[CheckoutCtrl DEBUG] Populating payment methods...");

            // Ensure these filenames (e.g., "gcash_logo.png") EXACTLY match your files
            // in src/main/resources/app/busbookingproject/images/
            // Also ensure your folder is named "images" (plural) or adjust path in PaymentMethodDisplay.
            methods.add(new PaymentMethodDisplay("GCash", "gcash_logo.png"));
            methods.add(new PaymentMethodDisplay("Maya", "maya_logo.png"));
            // If your card logo is card_logo.jpg, use that name here:
            methods.add(new PaymentMethodDisplay("Credit/Debit Card", "card_logo.png")); // Or "card_logo.jpg"
            methods.add(new PaymentMethodDisplay("Online Bank Transfer", "bank_logo.png"));

            paymentMethodComboBox.setItems(FXCollections.observableArrayList(methods));

            Callback<ListView<PaymentMethodDisplay>, ListCell<PaymentMethodDisplay>> cellFactory = lv -> new ListCell<>() {
                @Override
                protected void updateItem(PaymentMethodDisplay item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setGraphic(null);
                        setText(null);
                    } else {
                        HBox hbox = new HBox(5); // 5px spacing
                        Label label = new Label(item.name());
                        ImageView logoView = item.getLogoView(); // Calls the method in your record

                        if (logoView != null && logoView.getImage() != null && !logoView.getImage().isError()) {
                            // System.out.println("[CheckoutCtrl DEBUG] CellFactory: Logo is valid for " + item.name());
                            hbox.getChildren().addAll(logoView, label);
                        } else {
                            // System.out.println("[CheckoutCtrl DEBUG] CellFactory: Logo NOT valid or image error for " + item.name());
                            hbox.getChildren().add(label); // Only display text if logo is invalid
                        }
                        setGraphic(hbox);
                        setText(null); // We are using a graphic, so clear the default text
                    }
                }
            };

            paymentMethodComboBox.setCellFactory(cellFactory);
            paymentMethodComboBox.setButtonCell(cellFactory.call(null)); // For the displayed selected item
            System.out.println("[CheckoutCtrl DEBUG] Payment methods ComboBox populated and cell factories set.");
        } else {
            System.err.println("[CheckoutCtrl ERROR] paymentMethodComboBox is null in populatePaymentMethods.");
        }
    }

    @FXML
    private void onProcessPayment() {
        if (currentBooking == null) { Utility.showAlert(Alert.AlertType.ERROR, "Error", "No booking selected."); return; }
        if ("Paid".equalsIgnoreCase(currentBooking.getPaymentStatus())) { Utility.showAlert(Alert.AlertType.INFORMATION, "Already Paid", "This booking is already paid."); return; }

        PaymentMethodDisplay selectedMethodDisplay = paymentMethodComboBox.getValue();
        if (selectedMethodDisplay == null) {
            Utility.showAlert(Alert.AlertType.WARNING, "Payment Method", "Please select a payment method.");
            return;
        }
        String selectedPaymentMethodName = selectedMethodDisplay.name();

        String paymentText = paymentField.getText();
        if (paymentText == null || paymentText.trim().isEmpty()) { Utility.showAlert(Alert.AlertType.ERROR, "Payment Error", "Payment amount empty."); return; }

        try {
            double paymentAmount = Double.parseDouble(paymentText.trim());
            if (currentBooking.processPayment(paymentAmount)) {
                paymentStatusLabel.setStyle("-fx-text-fill: green;");
                paymentStatusLabel.setText("Payment successful via " + selectedPaymentMethodName + "! Change: " + currentBooking.getChangeGiven());
                Utility.showAlert(Alert.AlertType.INFORMATION, "Payment Success", "Payment successful via " + selectedPaymentMethodName + "! Change: " + currentBooking.getChangeGiven());
                paymentField.setDisable(true);
                if (processPaymentButton != null) processPaymentButton.setDisable(true);
                if (paymentMethodComboBox != null) paymentMethodComboBox.setDisable(true);
            } else {
                paymentStatusLabel.setStyle("-fx-text-fill: red;");
                paymentStatusLabel.setText("Insufficient payment. Amount due: " + currentBooking.getFormattedFare());
                Utility.showAlert(Alert.AlertType.WARNING, "Payment Status", "Insufficient payment. Provide at least " + currentBooking.getFormattedFare());
            }
        } catch (NumberFormatException e) {
            paymentStatusLabel.setStyle("-fx-text-fill: red;");
            paymentStatusLabel.setText("Invalid payment amount.");
            Utility.showAlert(Alert.AlertType.ERROR, "Input Error", "Invalid payment amount. Enter numeric value.");
        }
    }

    @FXML
    private void onClose() {
        if (stage != null) {
            stage.close();
        }
    }

    @FXML
    public void initialize() {
        // ComboBox is populated when setBooking() is called.
    }
}