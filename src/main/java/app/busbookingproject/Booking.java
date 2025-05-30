package app.busbookingproject;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.text.NumberFormat; // For currency formatting
import java.time.LocalDate;    // For dates
import java.util.ArrayList;    // For creating lists
import java.util.List;         // For using List interface
import java.util.Locale;       // For currency locale (PHP)
// No need for java.util.stream.Collectors for String.join

public class Booking {
    private final String name;
    private final String contact;
    private final String from;
    private final String to;
    private final String busName;
    private final LocalDate date;
    private final List<String> bookedSeatLabels;
    private final double fare;

    // Ticket-specific information (can be set after booking confirmation)
    private final StringProperty ticketNumber;
    private final StringProperty departureTime;
    private final StringProperty busStop;

    // JavaFX Properties
    private final StringProperty formattedFare;
    private final StringProperty paymentStatus;
    private final StringProperty changeGiven;
    private final StringProperty seatsDisplay;

    public Booking(String name, String contact, String from, String to, String busName, LocalDate date,
                   List<String> bookedSeatLabels, double totalFareForBooking) {
        this.name = name;
        this.contact = contact;
        this.from = from;
        this.to = to;
        this.busName = busName;
        this.date = date;
        this.bookedSeatLabels = new ArrayList<>(bookedSeatLabels); // Defensive copy
        this.fare = totalFareForBooking;

        NumberFormat pesoFormat = NumberFormat.getCurrencyInstance(new Locale("en", "PH"));
        this.formattedFare = new SimpleStringProperty(pesoFormat.format(this.fare));
        this.paymentStatus = new SimpleStringProperty("Not Paid");
        this.changeGiven = new SimpleStringProperty("N/A");
        this.seatsDisplay = new SimpleStringProperty(generateSeatsDisplayString());

        this.ticketNumber = new SimpleStringProperty("TBD");
        this.departureTime = new SimpleStringProperty("TBD");
        // Default bus stop to origin city, can be changed via setter
        this.busStop = new SimpleStringProperty(this.from);
    }

    private String generateSeatsDisplayString() {
        if (this.bookedSeatLabels == null || this.bookedSeatLabels.isEmpty()) {
            return "0";
        }
        return this.bookedSeatLabels.size() + " (" + String.join(", ", this.bookedSeatLabels) + ")";
    }

    // Standard Getters
    public String getName() { return name; }
    public String getContact() { return contact; }
    public String getFrom() { return from; }
    public String getTo() { return to; }
    public String getBusName() { return busName; }
    public LocalDate getDate() { return date; }
    public double getFare() { return fare; }
    public List<String> getBookedSeatLabels() { return new ArrayList<>(bookedSeatLabels); } // Defensive copy
    public int getNumberOfSeatsBooked() { return this.bookedSeatLabels != null ? this.bookedSeatLabels.size() : 0; }

    // JavaFX Property Accessors
    public String getFormattedFare() { return formattedFare.get(); } // Corrected
    public StringProperty formattedFareProperty() { return formattedFare; }

    public String getPaymentStatus() { return paymentStatus.get(); } // Corrected
    public void setPaymentStatus(String status) { this.paymentStatus.set(status); }
    public StringProperty paymentStatusProperty() { return paymentStatus; }

    public String getChangeGiven() { return changeGiven.get(); }
    public void setChangeGiven(String change) { this.changeGiven.set(change); }
    public StringProperty changeGivenProperty() { return changeGiven; }

    public String getSeatsDisplay() { return seatsDisplay.get(); }
    public StringProperty seatsDisplayProperty() { return seatsDisplay; }

    // Getters and Setters for Ticket Information
    public String getTicketNumber() { return ticketNumber.get(); }
    public void setTicketNumber(String ticketNumber) { this.ticketNumber.set(ticketNumber); }
    public StringProperty ticketNumberProperty() { return ticketNumber; }

    public String getDepartureTime() { return departureTime.get(); }
    public void setDepartureTime(String departureTime) { this.departureTime.set(departureTime); }
    public StringProperty departureTimeProperty() { return departureTime; }

    public String getBusStop() { return busStop.get(); }
    public void setBusStop(String busStop) { this.busStop.set(busStop); }
    public StringProperty busStopProperty() { return busStop; }

    public boolean processPayment(double amountPaid) {
        if (amountPaid >= this.fare) {
            double calculatedChange = amountPaid - this.fare;
            NumberFormat pesoFormat = NumberFormat.getCurrencyInstance(new Locale("en", "PH"));
            this.setPaymentStatus("Paid");
            this.setChangeGiven(pesoFormat.format(calculatedChange));
            return true;
        }
        return false;
    }
}