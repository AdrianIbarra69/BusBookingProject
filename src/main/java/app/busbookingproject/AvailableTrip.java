package app.busbookingproject;

public class AvailableTrip {
    private BusInfo busInfo;
    private int availableSeats;
    private String departureTime; // e.g., "10:00 AM", "02:30 PM"
    private int estimatedTravelDurationHours; // e.g., 8 (for 8 hours)

    public AvailableTrip(BusInfo busInfo, int availableSeats, String departureTime, int estimatedTravelDurationHours) {
        this.busInfo = busInfo;
        this.availableSeats = availableSeats;
        this.departureTime = departureTime;
        this.estimatedTravelDurationHours = estimatedTravelDurationHours;
    }

    public BusInfo getBusInfo() {
        return busInfo;
    }

    public int getAvailableSeats() {
        return availableSeats;
    }

    public String getDepartureTime() {
        return departureTime;
    }

    public int getEstimatedTravelDurationHours() {
        return estimatedTravelDurationHours;
    }

    @Override
    public String toString() {
        return busInfo.getName() + " (" + busInfo.getCompany() +
                ", Departs: " + (departureTime != null ? departureTime : "TBD") +
                ", Seats: " + availableSeats + ")";
    }
}
