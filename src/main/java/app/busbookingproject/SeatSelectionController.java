package app.busbookingproject;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.FlowPane; // Ensure this import is for FlowPane
import javafx.stage.Stage;
import java.util.ArrayList;
import java.util.List;

public class SeatSelectionController {
    @FXML private Label busInfoLabel;
    @FXML private FlowPane seatGridPane; // CRITICAL: This must be FlowPane
    @FXML private Label selectedSeatsInfoLabel;

    private Stage dialogStage;
    private BusInfo currentBusInfo;
    private List<String> availableSeatLabelsForTrip;
    private List<String> currentlySelectedSeats = new ArrayList<>();

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setSeatData(BusInfo busInfo, List<String> availableSeatLabels) {
        this.currentBusInfo = busInfo;
        this.availableSeatLabelsForTrip = availableSeatLabels;

        // Update busInfoLabel text
        if (busInfoLabel != null) { // Null check for safety
            if (busInfo != null) {
                busInfoLabel.setText("Select Seats for " + busInfo.getName() + " (Capacity: " + busInfo.getTotalSeats() + ")");
            } else {
                busInfoLabel.setText("Bus information unavailable.");
            }
        } else {
            System.err.println("ERROR: busInfoLabel is null in setSeatData. Check FXML injection.");
        }
        renderSeats();
    }

    private void renderSeats() {
        if (seatGridPane == null) {
            System.err.println("ERROR: seatGridPane is null in renderSeats. Check FXML injection and controller linkage.");
            if (busInfoLabel != null) busInfoLabel.setText("Error: Seat layout pane not loaded.");
            return;
        }
        seatGridPane.getChildren().clear();

        if (currentBusInfo == null || availableSeatLabelsForTrip == null) {
            Label infoLabel = new Label("Seat information is currently unavailable.");
            infoLabel.getStyleClass().add("form-label"); // Use existing style class
            seatGridPane.getChildren().add(infoLabel);
            return;
        }

        List<String> allPossibleSeats = currentBusInfo.getSeatLabels();

        for (String seatLabel : allPossibleSeats) {
            ToggleButton seatButton = new ToggleButton(seatLabel);
            // Apply consistent styling via CSS class
            seatButton.getStyleClass().add("seat-button");
            // It's better to control prefWidth/Height via CSS for .seat-button if possible,
            // but can be set here if dynamic sizing is complex.
            // Example: seatButton.setPrefSize(50, 50);
            seatButton.setWrapText(true);


            if (availableSeatLabelsForTrip.contains(seatLabel)) {
                seatButton.setDisable(false);
                seatButton.setOnAction(e -> handleSeatToggle(seatButton, seatLabel));
                if (currentlySelectedSeats.contains(seatLabel)) {
                    seatButton.setSelected(true);
                }
            } else {
                seatButton.setDisable(true);
                // The .seat-button:disabled CSS should handle the visual distinction.
                // Adding "(X)" can be done if preferred, but ensure it fits.
                // seatButton.setText(seatLabel + " (X)");
            }
            seatGridPane.getChildren().add(seatButton);
        }
        updateSelectedSeatsInfo();
    }

    private void handleSeatToggle(ToggleButton seatButton, String seatLabel) {
        if (seatButton.isSelected()) {
            currentlySelectedSeats.add(seatLabel);
        } else {
            currentlySelectedSeats.remove(seatLabel);
        }
        updateSelectedSeatsInfo();
    }

    private void updateSelectedSeatsInfo() {
        if (selectedSeatsInfoLabel == null) {
            System.err.println("ERROR: selectedSeatsInfoLabel is null. Check FXML injection.");
            return;
        }
        if (currentlySelectedSeats.isEmpty()) {
            selectedSeatsInfoLabel.setText("Selected: None");
        } else {
            selectedSeatsInfoLabel.setText("Selected: " + String.join(", ", currentlySelectedSeats) + " (" + currentlySelectedSeats.size() + " seat(s))");
        }
    }

    public List<String> getSelectedSeats() {
        return new ArrayList<>(currentlySelectedSeats);
    }

    @FXML
    private void handleConfirmSeats() {
        if(currentlySelectedSeats.isEmpty()){
            Utility.showAlert(Alert.AlertType.WARNING, "No Seats Selected", "Please select at least one seat.");
            return;
        }
        if (dialogStage != null) {
            dialogStage.close();
        } else {
            System.err.println("ERROR: dialogStage is null in handleConfirmSeats.");
        }
    }

    @FXML
    private void handleCancel() {
        currentlySelectedSeats.clear();
        if (dialogStage != null) {
            dialogStage.close();
        } else {
            System.err.println("ERROR: dialogStage is null in handleCancel.");
        }
    }

    @FXML
    public void initialize() {
        // Optional: Add a print statement to confirm initialization and FXML injection
        System.out.println("SeatSelectionController initialized.");
        if (seatGridPane == null) {
            System.err.println("CRITICAL ERROR: seatGridPane is null after FXML loading in initialize(). Check fx:id and controller in FXML.");
        }
        if (busInfoLabel == null) {
            System.err.println("CRITICAL ERROR: busInfoLabel is null after FXML loading in initialize().");
        }
        if (selectedSeatsInfoLabel == null) {
            System.err.println("CRITICAL ERROR: selectedSeatsInfoLabel is null after FXML loading in initialize().");
        }
    }
}
