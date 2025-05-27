package app.busbookingproject;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;
import javafx.stage.Stage;

import java.io.IOException;

public class LoadingScreenController {

    @FXML
    private ProgressBar progressBar;

    public void initialize() {
        // Start the loading simulation in a background thread
        new Thread(() -> {
            try {
                // Gradually update progress from 0 to 1
                for (int i = 0; i <= 100; i++) {
                    double progress = i / 100.0; // Calculate progress as a percentage

                    // Update the ProgressBar on the JavaFX Application thread
                    Platform.runLater(() -> progressBar.setProgress(progress));

                    Thread.sleep(50); // Pause for 50ms to simulate work
                }

                // After progress completes, switch to dashboard
                Platform.runLater(() -> {
                    try {
                        switchToDashboard();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void switchToDashboard() throws IOException {
        // Get the current stage from the ProgressBar's scene
        Stage stage = (Stage) progressBar.getScene().getWindow();

        // Load the Dashboard FXML file
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("dashboard.fxml"));
        Scene dashboardScene = new Scene(fxmlLoader.load());

        // Replace the current scene with the dashboard scene
        stage.setScene(dashboardScene);
        stage.show();
    }
}