package app.busbookingproject;

import java.util.UUID;
import java.text.SimpleDateFormat; // Required import
import java.util.Date;           // Required import

public class TicketNumberGenerator {
    // Corrected method signature: added space between String and method name
    public static String generateUniqueTicketNumber() {
        String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String uniqueID = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return "TKT-" + timestamp + "-" + uniqueID;
    }
}