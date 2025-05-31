package app.busbookingproject;

import javafx.fxml.FXML;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

import javax.imageio.ImageIO;
import javafx.embed.swing.SwingFXUtils;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.format.DateTimeFormatter; // For formatting LocalDate & LocalDateTime

public class TicketViewController {

    @FXML private VBox ticketPane;
    @FXML private Button closeButtonTicket;
    @FXML private ImageView passengerPhotoPlaceholder;
    @FXML private Label passengerNameLabel;
    @FXML private Label originLabel;
    @FXML private Label originDetailLabel;
    @FXML private ImageView busIconView;
    @FXML private Label destinationLabel;
    @FXML private Label destinationDetailLabel;
    @FXML private ImageView calendarIconView;
    @FXML private Label dateLabelTicket;
    @FXML private ImageView timeIconView;
    @FXML private Label timeLabelTicket; // This is for Departure Time
    @FXML private Label busStopLabel;
    @FXML private Label busNumberLabel;
    @FXML private Label ticketNumberLabel;
    @FXML private Label passengerCountLabel;
    @FXML private Label seatNumberLabel;
    @FXML private Label totalFareLabelTicket;
    @FXML private Label paymentMethodLabelTicket;
    @FXML private ImageView qrCodeImageView;
    @FXML private Button printButton;

    // New FXML Injections
    @FXML private Label bookingTimeLabel;
    @FXML private Label arrivalTimeLabel;


    private Stage dialogStage;

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    @FXML
    public void initialize() {
        System.out.println("DEBUG TicketViewController: Initializing...");
        loadIcons();
        System.out.println("DEBUG TicketViewController: Initialization complete.");
    }

    private void loadIconToView(ImageView imageView, String iconPath) {
        if (imageView == null) {
            System.err.println("DEBUG TicketViewController: ImageView for " + iconPath + " is null (check FXML fx:id).");
            return;
        }
        try (InputStream iconStream = getClass().getResourceAsStream(iconPath)) {
            if (iconStream != null) {
                Image icon = new Image(iconStream);
                if (!icon.isError()) {
                    imageView.setImage(icon);
                } else {
                    System.err.println("ERROR TicketViewController: Error loading icon image: " + iconPath + (icon.getException() != null ? " - " + icon.getException().getMessage() : ""));
                }
            } else {
                System.err.println("ERROR TicketViewController: Icon resource not found: " + iconPath);
            }
        } catch (Exception e) {
            System.err.println("ERROR TicketViewController: Exception loading icon " + iconPath + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadIcons() {
        System.out.println("DEBUG TicketViewController: Loading icons...");
        loadIconToView(busIconView, "/app/busbookingproject/images/bus_icon.png");
        // You might want to add icons for the new time fields if desired
        System.out.println("DEBUG TicketViewController: Icon loading attempted.");
    }

    public void setTicketDetails(Booking booking, String paymentMethod, Image qrCodeImage) {
        System.out.println("DEBUG TicketViewController: Setting ticket details for: " + (booking != null ? booking.getName() : "null booking"));
        if (booking == null) {
            System.err.println("ERROR TicketViewController: Booking object is null in setTicketDetails.");
            Utility.showAlert(AlertType.ERROR, "Internal Error", "Booking data was not provided to the ticket view.");
            return;
        }

        // Existing details
        if (ticketNumberLabel != null) ticketNumberLabel.setText(booking.getTicketNumber() != null ? booking.getTicketNumber() : "N/A");
        if (passengerNameLabel != null) passengerNameLabel.setText(booking.getName());
        if (originLabel != null) originLabel.setText(booking.getFrom());
        if (originDetailLabel != null) originDetailLabel.setText(booking.getBusStop());
        if (destinationLabel != null) destinationLabel.setText(booking.getTo());
        if (destinationDetailLabel != null) destinationDetailLabel.setText(booking.getTo());

        if (dateLabelTicket != null) {
            if (booking.getDate() != null) {
                dateLabelTicket.setText(booking.getDate().format(DateTimeFormatter.ofPattern("MMMM dd, uuuu")));
            } else {
                dateLabelTicket.setText("N/A");
            }
        }
        // Assuming timeLabelTicket is for Departure Time
        if (timeLabelTicket != null) timeLabelTicket.setText(booking.getDepartureTime() != null ? booking.getDepartureTime() : "N/A");

        if (busStopLabel != null) busStopLabel.setText(booking.getBusStop());
        if (busNumberLabel != null) busNumberLabel.setText(booking.getBusName());
        if (seatNumberLabel != null) seatNumberLabel.setText(booking.getBookedSeatLabels() != null ? String.join(", ", booking.getBookedSeatLabels()) : "N/A");

        if (passengerCountLabel != null) {
            int numPassengers = booking.getNumberOfSeatsBooked();
            passengerCountLabel.setText(numPassengers + (numPassengers > 1 ? " Adults" : " Adult"));
        }

        if (totalFareLabelTicket != null) totalFareLabelTicket.setText(booking.getFormattedFare());
        if (paymentMethodLabelTicket != null) paymentMethodLabelTicket.setText(paymentMethod != null ? paymentMethod : "N/A");

        // Set new details
        if (bookingTimeLabel != null) {
            bookingTimeLabel.setText(booking.getFormattedBookingCreationTime());
        } else {
            System.err.println("ERROR TicketViewController: bookingTimeLabel is null. Check fx:id.");
        }

        if (arrivalTimeLabel != null) {
            arrivalTimeLabel.setText(booking.getBusArrivalTime()); // This will show "TBD" by default
        } else {
            System.err.println("ERROR TicketViewController: arrivalTimeLabel is null. Check fx:id.");
        }


        if (qrCodeImageView != null) {
            if (qrCodeImage != null && !qrCodeImage.isError()) {
                qrCodeImageView.setImage(qrCodeImage);
            } else {
                qrCodeImageView.setImage(null);
                System.err.println("ERROR TicketViewController: QR Code image was null or in error state in setTicketDetails.");
            }
        } else {
            System.err.println("ERROR TicketViewController: qrCodeImageView is null. Check fx:id.");
        }
        System.out.println("DEBUG TicketViewController: Ticket details setting complete.");
    }

    @FXML
    private void handlePrintTicket() {
        // ... (Your existing handlePrintTicket method) ...
        System.out.println("DEBUG TicketViewController: handlePrintTicket called.");
        if (ticketPane == null) {
            System.err.println("ERROR TicketViewController: ticketPane is null in handlePrintTicket.");
            Utility.showAlert(AlertType.ERROR, "Snapshot Error", "Cannot take snapshot because ticketPane is not available.");
            return;
        }
        WritableImage snapshot = ticketPane.snapshot(new SnapshotParameters(), null);
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Ticket As Image");
        String initialFileName = "BusTicket.png";
        if (ticketNumberLabel != null && ticketNumberLabel.getText() != null && !ticketNumberLabel.getText().equals("N/A")) {
            initialFileName = "BusTicket_" + ticketNumberLabel.getText().replace(":", "_").replace("/", "_") + ".png";
        }
        fileChooser.setInitialFileName(initialFileName);
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG Image", "*.png"));
        Stage currentStage = dialogStage != null ? dialogStage : (printButton != null && printButton.getScene() != null ? (Stage) printButton.getScene().getWindow() : null);
        if (currentStage == null) {
            System.err.println("ERROR TicketViewController: Could not determine current stage for FileChooser in handlePrintTicket.");
            Utility.showAlert(AlertType.ERROR, "Error", "Window not found to show save dialog."); return;
        }
        File file = fileChooser.showSaveDialog(currentStage);
        if (file != null) {
            try {
                ImageIO.write(SwingFXUtils.fromFXImage(snapshot, null), "png", file);
                Utility.showAlert(AlertType.INFORMATION, "Ticket Saved", "Saved to: " + file.getAbsolutePath());
            } catch (IOException ex) {
                ex.printStackTrace();
                Utility.showAlert(AlertType.ERROR, "Save Error", "Could not save image: " + ex.getMessage());
            }
        } else {
            System.out.println("DEBUG TicketViewController: Save ticket cancelled by user.");
        }
    }

    @FXML
    private void handleCloseTicket() {
        // ... (Your existing handleCloseTicket method) ...
        System.out.println("DEBUG TicketViewController: handleCloseTicket called.");
        Stage stageToClose = dialogStage != null ? dialogStage : (closeButtonTicket != null && closeButtonTicket.getScene() != null ? (Stage) closeButtonTicket.getScene().getWindow() : null);
        if (stageToClose != null) {
            stageToClose.close();
            System.out.println("DEBUG TicketViewController: Ticket window closed.");
        } else {
            System.err.println("ERROR TicketViewController: Could not close ticket window; stage not found.");
        }
    }
}
