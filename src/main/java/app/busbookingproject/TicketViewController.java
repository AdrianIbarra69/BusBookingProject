package app.busbookingproject;

import javafx.fxml.FXML;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;         // Import Button
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.control.Alert;          // Import Alert
import javafx.scene.control.Alert.AlertType; // Import AlertType

import javax.imageio.ImageIO;
import javafx.embed.swing.SwingFXUtils;    // For saving image
import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter; // Import DateTimeFormatter

public class TicketViewController {

    @FXML private VBox ticketPane;
    @FXML private Label ticketNumberLabel;
    @FXML private Label passengerNameLabel;
    @FXML private Label routeLabel;
    @FXML private Label dateTimeLabel;
    @FXML private Label busStopLabel;
    @FXML private Label busNumberLabel;
    @FXML private Label seatNumberLabel;
    @FXML private Label totalFareLabelTicket;
    @FXML private Label paymentMethodLabelTicket;
    @FXML private ImageView qrCodeImageView;
    @FXML private Button printButton; // Ensure fx:id="printButton" is in TicketView.fxml

    private Stage dialogStage;
    private Booking currentBooking;

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setTicketDetails(Booking booking, String paymentMethod, Image qrCodeImage) {
        this.currentBooking = booking;

        ticketNumberLabel.setText(booking.getTicketNumber());
        passengerNameLabel.setText(booking.getName());
        routeLabel.setText(booking.getFrom() + " → " + booking.getTo());
        // Corrected date/time formatting
        dateTimeLabel.setText(booking.getDate().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")) + " " + booking.getDepartureTime());
        busStopLabel.setText(booking.getBusStop());
        busNumberLabel.setText(booking.getBusName());
        seatNumberLabel.setText(String.join(", ", booking.getBookedSeatLabels())); // String.join is fine
        totalFareLabelTicket.setText(booking.getFormattedFare()); // Uses corrected Booking.getFormattedFare()
        paymentMethodLabelTicket.setText(paymentMethod);

        if (qrCodeImage != null) {
            qrCodeImageView.setImage(qrCodeImage);
        } else {
            System.err.println("QR Code image was null when setting ticket details.");
            qrCodeImageView.setImage(null);
        }
    }

    @FXML
    private void handlePrintTicket() {
        if (ticketPane == null) {
            Utility.showAlert(AlertType.ERROR, "Snapshot Error", "Cannot take snapshot of the ticket.");
            return;
        }
        WritableImage snapshot = ticketPane.snapshot(new SnapshotParameters(), null);
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Ticket Image");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG Image", "*.png"));
        if (dialogStage == null && printButton != null && printButton.getScene() != null) { // Fallback for owner
            dialogStage = (Stage) printButton.getScene().getWindow();
        }
        File file = fileChooser.showSaveDialog(dialogStage); // dialogStage should be set

        if (file != null) {
            try {
                ImageIO.write(SwingFXUtils.fromFXImage(snapshot, null), "png", file);
                Utility.showAlert(AlertType.INFORMATION, "Ticket Saved", "Ticket image saved to: " + file.getAbsolutePath());
            } catch (IOException ex) {
                ex.printStackTrace();
                Utility.showAlert(AlertType.ERROR, "Save Error", "Could not save ticket image: " + ex.getMessage());
            }
        }
    }

    @FXML
    private void handleCloseTicket() {
        if (dialogStage != null) {
            dialogStage.close();
        }
    }
}