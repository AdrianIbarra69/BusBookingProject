module app.busbookingproject {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    // For ZXing QR Code generation and SwingFXUtils
    requires com.google.zxing;       // Correct automatic module name for core ZXing library
    requires com.google.zxing.javase; // This should be the correct automatic module name

    requires java.desktop;           // For BufferedImage, Color, Graphics2D (used by ZXing's javase)
    requires javafx.swing;           // For SwingFXUtils

    // Other dependencies from your project
    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;

    opens app.busbookingproject to javafx.fxml;
    exports app.busbookingproject;
}