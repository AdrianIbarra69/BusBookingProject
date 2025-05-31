package app.busbookingproject;

import java.util.UUID;
import java.text.SimpleDateFormat; // Required
import java.util.Date;           // Required

public class TicketNumberGenerator {
    public static String generateUniqueTicketNumber() { // Corrected: space after String
        String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String uniqueID = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return "TKT-" + timestamp + "-" + uniqueID;
    }
}