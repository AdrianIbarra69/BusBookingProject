package app.busbookingproject;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.Locale;

public class Booking {
    private final String name;
    private final String contact;
    private final String from;
    private final String to;
    private final String busName;
    private final LocalDate date;
    private final int seats;
    private final double fare;

    public Booking(String name, String contact, String from, String to, String busName, LocalDate date, int seats, double fare) {
        this.name = name;
        this.contact = contact;
        this.from = from;
        this.to = to;
        this.busName = busName;
        this.date = date;
        this.seats = seats;
        this.fare = fare;
    }

    public String getName() {
        return name;
    }

    public String getContact() {
        return contact;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public String getBusName() {
        return busName;
    }

    public LocalDate getDate() {
        return date;
    }

    public int getSeats() {
        return seats;
    }

    public double getFare() {
        return fare;
    }

    // Added method to return formatted fare as Philippine Peso
    public String getFormattedFare() {
        NumberFormat pesoFormat = NumberFormat.getCurrencyInstance(new Locale("en", "PH"));
        return pesoFormat.format(fare);
    }
}