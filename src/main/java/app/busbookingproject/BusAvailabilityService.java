package app.busbookingproject;

import javafx.collections.ObservableList;
import java.time.LocalDate;
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

    public BusAvailabilityService(ObservableList<Booking> existingBookings) {
        this.existingBookings = existingBookings;
        this.allConfiguredBuses = new ArrayList<>();
        initializeBuses();
    }

    private void initializeBuses() {
        allConfiguredBuses.clear();
        String[] prefixes = {"DLX-", "EXP-", "ACE-", "REG-", "PREM-", "SUP-"}; // Example prefixes
        // Expanded list of companies based on your fare data
        String[] companies = {
                "RTMI", "Super 5", "Pabama", "Bachelor Express",
                "Philtranco", "Metro Star", "Bagong Lipunan", "TOMAWIS", "Super Six" // Shortened names for consistency
        };
        int numberOfBusesToGenerate = 18 + random.nextInt(10); // Generate 18 to 27 buses for more variety

        for (int i = 0; i < numberOfBusesToGenerate; i++) {
            String prefix = prefixes[random.nextInt(prefixes.length)];
            String busIdSuffix = String.format("%03d", random.nextInt(1000));
            String busName = prefix + busIdSuffix; // This is the displayed bus number/ID
            String internalId = "BUS" + i + busIdSuffix;
            String assignedCompany = companies[random.nextInt(companies.length)];

            int configType = random.nextInt(4);
            int numRows, seatsPerRow;
            switch (configType) {
                case 0: numRows = 11; seatsPerRow = 4; break; // 44 seats (2x2)
                case 1: numRows = 12; seatsPerRow = 4; break; // 48 seats (2x2)
                case 2: numRows = 10; seatsPerRow = 5; break; // 50 seats (e.g., 2x3 like, for BusInfo generation)
                default: numRows = 9; seatsPerRow = 4; break;  // 36 seats (2x2)
            }
            allConfiguredBuses.add(new BusInfo(internalId, busName, assignedCompany, numRows, seatsPerRow));
            // System.out.println("Initialized Bus: " + busName + " (Company: " + assignedCompany + ") with " + (numRows * seatsPerRow) + " seats");
        }
    }

    public List<AvailableTrip> getAvailableTrips(String fromLocation, String toLocation, LocalDate travelDate) {
        List<AvailableTrip> availableTrips = new ArrayList<>();
        if (fromLocation == null || toLocation == null || travelDate == null) {
            return availableTrips;
        }
        for (BusInfo bus : allConfiguredBuses) {
            Set<String> allBookedSeatLabelsForThisBusTrip = new HashSet<>();
            for (Booking booking : existingBookings) {
                if (booking.getBusName().equals(bus.getName()) && // Could also match by bus.getId() if busName is not unique enough
                        booking.getFrom().equals(fromLocation) &&
                        booking.getTo().equals(toLocation) &&
                        booking.getDate().equals(travelDate)) {
                    allBookedSeatLabelsForThisBusTrip.addAll(booking.getBookedSeatLabels());
                }
            }
            int currentAvailableSeatCount = bus.getTotalSeats() - allBookedSeatLabelsForThisBusTrip.size();
            if (currentAvailableSeatCount > 0) {
                availableTrips.add(new AvailableTrip(bus, currentAvailableSeatCount));
            }
        }
        Collections.shuffle(availableTrips);
        return availableTrips;
    }

    public List<String> getAvailableSeatLabelsForTrip(BusInfo busInfo, String fromLocation, String toLocation, LocalDate travelDate) {
        if (busInfo == null || fromLocation == null || toLocation == null || travelDate == null) {
            return new ArrayList<>();
        }
        Set<String> bookedSeatLabelsOnThisTrip = new HashSet<>();
        for (Booking booking : existingBookings) {
            if (booking.getBusName().equals(busInfo.getName()) && // Could also match by bus.getId()
                    booking.getFrom().equals(fromLocation) &&
                    booking.getTo().equals(toLocation) &&
                    booking.getDate().equals(travelDate)) {
                bookedSeatLabelsOnThisTrip.addAll(booking.getBookedSeatLabels());
            }
        }
        return busInfo.getSeatLabels().stream()
                .filter(label -> !bookedSeatLabelsOnThisTrip.contains(label))
                .collect(Collectors.toList());
    }
}