package app.busbookingproject;

import javafx.fxml.FXML;
import javafx.scene.control.Alert; // Correct import
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import java.util.ArrayList;
import java.util.List;

public class SeatSelectionController {
    @FXML private Label busInfoLabel; @FXML private GridPane seatGridPane; @FXML private Label selectedSeatsInfoLabel;
    private Stage dialogStage; private BusInfo currentBusInfo; private List<String> availableSeatLabelsForTrip;
    private List<String> currentlySelectedSeats = new ArrayList<>();

    public void setDialogStage(Stage dialogStage) { this.dialogStage = dialogStage; }
    public void setSeatData(BusInfo busInfo, List<String> availableSeatLabels) {
        this.currentBusInfo = busInfo; this.availableSeatLabelsForTrip = availableSeatLabels;
        if (busInfo != null) busInfoLabel.setText("Layout for " + busInfo.getName() + " (Rows: " + busInfo.getNumRows() + ", Seats/Row: " + busInfo.getSeatsPerRow() +")");
        else busInfoLabel.setText("Bus info unavailable.");
        renderSeats();
    }
    private void renderSeats() {
        seatGridPane.getChildren().clear();
        if (currentBusInfo == null || availableSeatLabelsForTrip == null) { seatGridPane.add(new Label("Seat info unavailable."), 0, 0); return; }
        List<String> allPossibleSeats = currentBusInfo.getSeatLabels(); int col = 0; int row = 0;
        for (String seatLabel : allPossibleSeats) {
            ToggleButton seatButton = new ToggleButton(seatLabel); seatButton.setPrefWidth(50); seatButton.setPrefHeight(50); seatButton.setWrapText(true);
            if (availableSeatLabelsForTrip.contains(seatLabel)) {
                seatButton.setDisable(false); seatButton.setOnAction(e -> handleSeatToggle(seatButton, seatLabel));
                if (currentlySelectedSeats.contains(seatLabel)) seatButton.setSelected(true);
            } else {
                seatButton.setDisable(true); seatButton.setText(seatLabel + "\n(X)"); seatButton.setStyle("-fx-base: lightcoral; -fx-opacity: 0.7;");
            }
            seatGridPane.add(seatButton, col, row); col++;
            if (col >= currentBusInfo.getSeatsPerRow()) { col = 0; row++; }
        }
        updateSelectedSeatsInfo();
    }
    private void handleSeatToggle(ToggleButton seatButton, String seatLabel) {
        if (seatButton.isSelected()) currentlySelectedSeats.add(seatLabel); else currentlySelectedSeats.remove(seatLabel);
        updateSelectedSeatsInfo();
    }
    private void updateSelectedSeatsInfo() {
        if (currentlySelectedSeats.isEmpty()) selectedSeatsInfoLabel.setText("Selected: None");
        else selectedSeatsInfoLabel.setText("Selected: " + String.join(", ", currentlySelectedSeats) + " (" + currentlySelectedSeats.size() + " seat(s))");
    }
    public List<String> getSelectedSeats() { return new ArrayList<>(currentlySelectedSeats); }
    @FXML private void handleConfirmSeats() {
        if(currentlySelectedSeats.isEmpty()){ Utility.showAlert(Alert.AlertType.WARNING, "No Seats Selected", "Please select at least one seat."); return; }
        dialogStage.close();
    }
    @FXML private void handleCancel() { currentlySelectedSeats.clear(); dialogStage.close(); }
}