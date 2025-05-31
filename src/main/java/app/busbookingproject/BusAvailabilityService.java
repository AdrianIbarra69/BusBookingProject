package app.busbookingproject;

import javafx.collections.ObservableList;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

public class BusAvailabilityService {

    private List<BusInfo> allConfiguredBuses;
    private ObservableList<Booking> existingBookings;
    private Random random = new Random();
    // DateTimeFormatter for parsing/formatting is better placed in BusBookingController
    // if that's where the final string formatting for display happens.
    // Here, we primarily deal with the string representations for departure.

    // Example departure times and durations
    private String[] sampleDepartureTimes = {"08:00 AM", "09:30 AM", "11:00 AM", "01:30 PM", "03:00 PM", "05:30 PM", "07:00 PM", "08:30 PM"};
    private int[] sampleDurations = {2, 3, 4, 5, 6, 7, 8, 9, 10}; // in hours, expanded for more variety

    public BusAvailabilityService(ObservableList<Booking> existingBookings) {
        this.existingBookings = existingBookings;
        this.allConfiguredBuses = new ArrayList<>();
        initializeBuses();
    }

    private void initializeBuses() {
        allConfiguredBuses.clear();
        String[] prefixes = {"DLX-", "EXP-", "ACE-", "REG-", "PREM-", "SUP-"};
        String[] companies = {
                "RTMI", "Super 5", "Pabama", "Bachelor Express",
                "Philtranco", "Metro Star", "Bagong Lipunan", "TOMAWIS", "Super Six"
        };
        // Generate a decent number of base buses
        int numberOfBusesToGenerate = 20 + random.nextInt(10); // 20 to 29 buses

        for (int i = 0; i < numberOfBusesToGenerate; i++) {
            String prefix = prefixes[random.nextInt(prefixes.length)];
            String busIdSuffix = String.format("%03d", random.nextInt(1000));
            String busName = prefix + busIdSuffix;
            String internalId = "BUS" + i + busIdSuffix;
            String assignedCompany = companies[random.nextInt(companies.length)];

            int configType = random.nextInt(4);
            int numRows, seatsPerRow;
            switch (configType) {
                case 0: numRows = 11; seatsPerRow = 4; break; // 44 seats
                case 1: numRows = 12; seatsPerRow = 4; break; // 48 seats
                case 2: numRows = 10; seatsPerRow = 5; break; // 50 seats
                default: numRows = 9; seatsPerRow = 4; break;  // 36 seats
            }
            allConfiguredBuses.add(new BusInfo(internalId, busName, assignedCompany, numRows, seatsPerRow));
        }
    }

    public List<AvailableTrip> getAvailableTrips(String fromLocation, String toLocation, LocalDate travelDate) {
        List<AvailableTrip> availableTrips = new ArrayList<>();
        if (fromLocation == null || toLocation == null || travelDate == null) {
            return availableTrips;
        }

        // For each configured bus, try to generate trips for multiple departure times.
        for (BusInfo bus : allConfiguredBuses) {
            // Simulate that a bus might run this route (e.g., 2 out of 3 buses might)
            if (random.nextInt(3) < 2) { // Increased probability: 2/3 chance

                // For each bus considered, iterate through some sample departure times
                for (String currentDepartureTime : sampleDepartureTimes) {
                    // Further randomize if this specific bus runs at this specific departure time
                    if (random.nextBoolean()) { // 50% chance for this specific time slot
                        Set<String> allBookedSeatLabelsForThisBusTrip = new HashSet<>();
                        for (Booking booking : existingBookings) {
                            // Match existing bookings for this specific bus, route, date, AND departure time
                            if (booking.getBusName().equals(bus.getName()) &&
                                    booking.getFrom().equals(fromLocation) &&
                                    booking.getTo().equals(toLocation) &&
                                    booking.getDate().equals(travelDate) &&
                                    currentDepartureTime.equals(booking.getDepartureTime())) {
                                allBookedSeatLabelsForThisBusTrip.addAll(booking.getBookedSeatLabels());
                            }
                        }
                        int currentAvailableSeatCount = bus.getTotalSeats() - allBookedSeatLabelsForThisBusTrip.size();

                        if (currentAvailableSeatCount > 0) {
                            int duration = sampleDurations[random.nextInt(sampleDurations.length)];
                            availableTrips.add(new AvailableTrip(bus, currentAvailableSeatCount, currentDepartureTime, duration));
                        }
                    }
                }
            }
        }

        Collections.shuffle(availableTrips); // Shuffle to make the list appear more dynamic
        // Limit the number of trips shown in the ComboBox to avoid overwhelming the user
        return availableTrips.stream().limit(random.nextInt(3) + 3).collect(Collectors.toList()); // Show 3 to 5 trips
    }

    // This method signature should match what BusBookingController expects (5 arguments)
    public List<String> getAvailableSeatLabelsForTrip(BusInfo busInfo, String fromLocation, String toLocation, LocalDate travelDate, String departureTime) {
        if (busInfo == null || fromLocation == null || toLocation == null || travelDate == null || departureTime == null) {
            System.err.println("WARN BusAvailabilityService: getAvailableSeatLabelsForTrip called with null parameters.");
            return new ArrayList<>();
        }
        Set<String> bookedSeatLabelsOnThisTrip = new HashSet<>();
        for (Booking booking : existingBookings) {
            if (booking.getBusName().equals(busInfo.getName()) &&
                    booking.getFrom().equals(fromLocation) &&
                    booking.getTo().equals(toLocation) &&
                    booking.getDate().equals(travelDate) &&
                    departureTime.equals(booking.getDepartureTime())) {
                bookedSeatLabelsOnThisTrip.addAll(booking.getBookedSeatLabels());
            }
        }
        return busInfo.getSeatLabels().stream()
                .filter(label -> !bookedSeatLabelsOnThisTrip.contains(label))
                .collect(Collectors.toList());
    }
}
