package app.busbookingproject;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime; // Import LocalDateTime
import java.time.format.DateTimeFormatter; // Import DateTimeFormatter
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Booking {
    private final String name;
    private final String contact;
    private final String from;
    private final String to;
    private final String busName;
    private final LocalDate date;
    private final List<String> bookedSeatLabels;
    private final double fare;

    private final StringProperty ticketNumber;
    private final StringProperty departureTime; // This is likely the bus departure time
    private final StringProperty busStop;

    private final StringProperty formattedFare;
    private final StringProperty paymentStatus;
    private final StringProperty changeGiven;
    private final StringProperty seatsDisplay;

    // New fields
    private final LocalDateTime bookingCreationTime;
    private final StringProperty busArrivalTime;

    public Booking(String name, String contact, String from, String to, String busName, LocalDate date,
                   List<String> bookedSeatLabels, double totalFareForBooking) {
        this.name = name;
        this.contact = contact;
        this.from = from;
        this.to = to;
        this.busName = busName;
        this.date = date;
        this.bookedSeatLabels = new ArrayList<>(bookedSeatLabels);
        this.fare = totalFareForBooking;

        NumberFormat pesoFormat = NumberFormat.getCurrencyInstance(new Locale("en", "PH"));
        this.formattedFare = new SimpleStringProperty(pesoFormat.format(this.fare));
        this.paymentStatus = new SimpleStringProperty("Not Paid");
        this.changeGiven = new SimpleStringProperty("N/A");
        this.seatsDisplay = new SimpleStringProperty(generateSeatsDisplayString());

        this.ticketNumber = new SimpleStringProperty("TBD");
        this.departureTime = new SimpleStringProperty("TBD"); // Example: "10:00 AM", should be set based on trip
        this.busStop = new SimpleStringProperty(this.from); // Or specific terminal

        // Initialize new fields
        this.bookingCreationTime = LocalDateTime.now(); // Set booking creation time automatically
        this.busArrivalTime = new SimpleStringProperty("TBD"); // Default to "To Be Determined"
    }

    private String generateSeatsDisplayString() {
        if (this.bookedSeatLabels == null || this.bookedSeatLabels.isEmpty()) {
            return "0";
        }
        return this.bookedSeatLabels.size() + " (" + String.join(", ", this.bookedSeatLabels) + ")";
    }

    // Existing getters and property methods...
    public String getName() { return name; }
    public String getContact() { return contact; }
    public String getFrom() { return from; }
    public String getTo() { return to; }
    public String getBusName() { return busName; }
    public LocalDate getDate() { return date; }
    public double getFare() { return fare; }
    public List<String> getBookedSeatLabels() { return new ArrayList<>(bookedSeatLabels); }
    public int getNumberOfSeatsBooked() { return this.bookedSeatLabels != null ? this.bookedSeatLabels.size() : 0; }

    public String getFormattedFare() { return formattedFare.get(); }
    public StringProperty formattedFareProperty() { return formattedFare; }

    public String getPaymentStatus() { return paymentStatus.get(); }
    public void setPaymentStatus(String status) { this.paymentStatus.set(status); }
    public StringProperty paymentStatusProperty() { return paymentStatus; }

    public String getChangeGiven() { return changeGiven.get(); }
    public void setChangeGiven(String change) { this.changeGiven.set(change); }
    public StringProperty changeGivenProperty() { return changeGiven; }

    public String getSeatsDisplay() { return seatsDisplay.get(); }
    public StringProperty seatsDisplayProperty() { return seatsDisplay; }

    public String getTicketNumber() { return ticketNumber.get(); }
    public void setTicketNumber(String ticketNumber) { this.ticketNumber.set(ticketNumber); }
    public StringProperty ticketNumberProperty() { return ticketNumber; }

    public String getDepartureTime() { return departureTime.get(); }
    public void setDepartureTime(String departureTime) { this.departureTime.set(departureTime); }
    public StringProperty departureTimeProperty() { return departureTime; }

    public String getBusStop() { return busStop.get(); }
    public void setBusStop(String busStop) { this.busStop.set(busStop); }
    public StringProperty busStopProperty() { return busStop; }

    // New getters and property methods
    public LocalDateTime getBookingCreationTime() { return bookingCreationTime; }

    // Formatted booking creation time
    public String getFormattedBookingCreationTime() {
        if (this.bookingCreationTime != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm:ss");
            return this.bookingCreationTime.format(formatter);
        }
        return "N/A";
    }

    public String getBusArrivalTime() { return busArrivalTime.get(); }
    public void setBusArrivalTime(String time) { this.busArrivalTime.set(time); } // Allow setting it later
    public StringProperty busArrivalTimeProperty() { return busArrivalTime; }


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
