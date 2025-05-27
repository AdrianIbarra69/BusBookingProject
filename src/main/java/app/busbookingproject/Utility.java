package app.busbookingproject;

import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;

public class Utility {
    // Show an alert dialog
    public static void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Clear all TextField input fields
    public static void clearFields(TextField... fields) {
        for (TextField field : fields) {
            field.clear();
        }
    }

    // Clear a DatePicker
    public static void clearDatePicker(DatePicker datePicker) {
        datePicker.setValue(null);
    }

    // Validate if a TextField is not empty
    public static boolean validateField(TextField field, String fieldName) {
        if (field.getText() == null || field.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", fieldName + " cannot be empty!");
            return false;
        }
        return true;
    }

    // Validate if a TextField contains a valid number
    public static boolean validateNumericField(TextField field, String fieldName) {
        try {
            Double.parseDouble(field.getText());
            return true;
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", fieldName + " must be a valid number!");
            return false;
        }
    }
}