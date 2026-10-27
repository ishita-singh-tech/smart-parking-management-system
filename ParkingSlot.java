/**
 * ParkingSlot entity demonstrating OOP Encapsulation.
 * Manages the state, assignment, and availability of an individual parking slot.
 */
public class ParkingSlot {
    private final String slotNumber;
    private final String slotType;
    private boolean occupied;
    private String currentVehicleNumber;

    public ParkingSlot(String slotNumber, String slotType, boolean occupied, String currentVehicleNumber) {
        this.slotNumber = (slotNumber != null) ? slotNumber.trim().toUpperCase() : "";
        this.slotType = (slotType != null) ? slotType.trim() : "Standard";
        this.occupied = occupied;
        this.currentVehicleNumber = currentVehicleNumber;
    }

    public ParkingSlot(String slotNumber) {
        this(slotNumber, "Standard", false, null);
    }

    // Encapsulation - Getters & Setters
    public String getSlotNumber() {
        return slotNumber;
    }

    public String getSlotType() {
        return slotType;
    }

    public boolean isOccupied() {
        return occupied;
    }

    public void setOccupied(boolean occupied) {
        this.occupied = occupied;
    }

    public String getCurrentVehicleNumber() {
        return currentVehicleNumber;
    }

    public void setCurrentVehicleNumber(String currentVehicleNumber) {
        this.currentVehicleNumber = currentVehicleNumber;
    }

    // Domain business methods
    public void occupy(String vehicleNumber) {
        this.occupied = true;
        this.currentVehicleNumber = vehicleNumber;
    }

    public void vacate() {
        this.occupied = false;
        this.currentVehicleNumber = null;
    }

    public String getStatus() {
        return occupied ? "Occupied" : "Available";
    }

    @Override
    public String toString() {
        return slotNumber + " (" + getStatus() + (occupied ? " - " + currentVehicleNumber : "") + ")";
    }
}
