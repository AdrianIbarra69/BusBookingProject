package app.busbookingproject;

import java.util.ArrayList;
import java.util.List;

public class BusInfo {
    private String id;
    private String name;
    private String company;
    private int totalSeats;
    private int numRows;
    private int seatsPerRow;
    private List<String> seatLabels;

    public BusInfo(String id, String name, String company, int numRows, int seatsPerRow) {
        this.id = id;
        this.name = name;
        this.company = company;
        this.numRows = numRows;
        this.seatsPerRow = seatsPerRow;
        this.totalSeats = numRows * seatsPerRow;
        this.seatLabels = generateSeatLabels();
    }

    private List<String> generateSeatLabels() {
        List<String> labels = new ArrayList<>();
        char rowLabel = 'A';
        for (int i = 0; i < numRows; i++) {
            for (int j = 1; j <= seatsPerRow; j++) {
                labels.add(String.valueOf(rowLabel) + j);
            }
            rowLabel++;
        }
        return labels;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getCompany() { return company; }
    public int getTotalSeats() { return totalSeats; }
    public int getNumRows() { return numRows; }
    public int getSeatsPerRow() { return seatsPerRow; }
    public List<String> getSeatLabels() { return new ArrayList<>(seatLabels); }

    @Override
    public String toString() {
        return name + " (Company: " + company + ", Capacity: " + totalSeats + ")";
    }
}