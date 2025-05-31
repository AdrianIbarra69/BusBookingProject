package app.busbookingproject;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginScreenController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Label statusLabel;

    @FXML
    public void initialize() {
        // Initialization code if needed
        statusLabel.setText(""); // Clear status on init
    }

    @FXML
    private void handleLoginButtonAction() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        // Simple hardcoded validation for demonstration
        // Replace this with actual authentication logic (e.g., database check)
        if ("admin".equals(username) && "password123".equals(password)) {
            statusLabel.setText("Login Successful!");
            statusLabel.setTextFill(Color.GREEN);
            statusLabel.setVisible(true);
            statusLabel.setManaged(true);
            System.out.println("Login successful");

            // Proceed to the next screen (e.g., loading screen or dashboard)
            proceedToNextScreen();

        } else if ("user".equals(username) && "pass".equals(password)) {
            statusLabel.setText("Login Successful!");
            statusLabel.setTextFill(Color.GREEN);
            statusLabel.setVisible(true);
            statusLabel.setManaged(true);
            System.out.println("Login successful");
            proceedToNextScreen();
        }
        else {
            statusLabel.setText("Invalid username or password.");
            statusLabel.setTextFill(Color.RED);
            statusLabel.setVisible(true);
            statusLabel.setManaged(true);
            System.out.println("Login failed");
        }
    }

    private void proceedToNextScreen() {
        try {
            // Get the current stage (window)
            Stage stage = (Stage) loginButton.getScene().getWindow();

            // Load the loading screen (or directly the dashboard if you prefer)
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("loading_screen.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 700, 600); // Adjust size as needed

            stage.setScene(scene);
            stage.setTitle("Bus Booking Application - Loading"); // Update title if needed
            // stage.centerOnScreen(); // Optional: re-center if window size changes

        } catch (IOException e) {
            e.printStackTrace();
            statusLabel.setText("Error loading next screen.");
            statusLabel.setTextFill(Color.RED);
            statusLabel.setVisible(true);
            statusLabel.setManaged(true);
        }
    }
}
