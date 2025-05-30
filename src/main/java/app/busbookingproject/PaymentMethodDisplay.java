package app.busbookingproject;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public record PaymentMethodDisplay(String name, String logoPath) {

    public ImageView getLogoView() {
        ImageView logoView = null;
        if (logoPath != null && !logoPath.isEmpty()) {
            // Ensure your images folder is correctly named (e.g., "images")
            String fullPath = "/app/busbookingproject/images/" + logoPath;
            System.out.println("[PaymentMethodDisplay DEBUG] Attempting to load logo from classpath: " + fullPath);
            try {
                Image img = new Image(getClass().getResourceAsStream(fullPath));
                if (img.isError()) {
                    System.err.println("[PaymentMethodDisplay ERROR] Failed to load image (img.isError() is true): " + fullPath);
                    if (img.getException() != null) {
                        System.err.println("    Image loading error details: " + img.getException().getMessage());
                    }
                } else {
                    System.out.println("[PaymentMethodDisplay DEBUG] Successfully loaded image: " + fullPath);
                    logoView = new ImageView(img);
                    logoView.setFitHeight(20);
                    logoView.setFitWidth(20);
                    logoView.setPreserveRatio(true);
                }
            } catch (NullPointerException npe){
                System.err.println("[PaymentMethodDisplay ERROR] NullPointerException: Resource not found at path (check build, path, and if file exists in target/classes): " + fullPath);
            }
            catch (Exception e) {
                System.err.println("[PaymentMethodDisplay ERROR] General exception loading image " + fullPath + ": " + e.getMessage());
            }
        }
        return logoView != null ? logoView : new ImageView();
    }

    @Override
    public String toString() {
        return name;
    }
}