package app.busbookingproject;

public class AvailableTrip {
    private BusInfo busInfo;
    private int availableSeats;

    public AvailableTrip(BusInfo busInfo, int availableSeats) {
        this.busInfo = busInfo;
        this.availableSeats = availableSeats;
    }

    public BusInfo getBusInfo() {
        return busInfo;
    }

    public int getAvailableSeats() {
        return availableSeats;
    }

    @Override
    public String toString() {
        return busInfo.getName() + " (" + busInfo.getCompany() + ", " + availableSeats + " seats available)";
    }
}