package app.busbookingproject;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class BookingEntryList {
    private final ObservableList<Booking> bookingList = FXCollections.observableArrayList();

    // Add a new booking to the list
    public void addBooking(Booking booking) {
        bookingList.add(booking);
    }

    // Get all bookings (this is bound to the TableView)
    public ObservableList<Booking> getBookings() {
        return bookingList;
    }
}