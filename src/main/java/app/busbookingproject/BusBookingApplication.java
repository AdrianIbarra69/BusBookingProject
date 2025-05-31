package app.busbookingproject;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class BusBookingApplication extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load the login screen first
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("login_screen.fxml"));
        // Adjust preferred size for the login screen if different from loading/dashboard
        Scene scene = new Scene(fxmlLoader.load(), 900, 600);

        primaryStage.setTitle("Bus Booking System - Login");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
