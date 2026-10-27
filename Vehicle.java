/**
 * Abstract Vehicle class demonstrating OOP concepts:
 * - Encapsulation (private fields, public getters)
 * - Abstraction (abstract method getHourlyRate)
 * - Inheritance & Polymorphism (TwoWheeler, FourWheeler subclasses)
 */
public abstract class Vehicle {
    private final String vehicleNumber;
    private final String ownerName;
    private final String vehicleType;

    public Vehicle(String vehicleNumber, String ownerName, String vehicleType) {
        this.vehicleNumber = (vehicleNumber != null) ? vehicleNumber.trim().toUpperCase() : "";
        this.ownerName = (ownerName != null) ? ownerName.trim() : "";
        this.vehicleType = (vehicleType != null) ? vehicleType.trim() : "Car";
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    // Abstract method demonstrating Polymorphism
    public abstract double getHourlyRate();

    // Factory method to create appropriate Vehicle instance (Polymorphism)
    public static Vehicle create(String vehicleNumber, String ownerName, String vehicleType) {
        if ("Bike".equalsIgnoreCase(vehicleType) || "Scooter".equalsIgnoreCase(vehicleType)) {
            return new TwoWheeler(vehicleNumber, ownerName, vehicleType);
        } else {
            return new FourWheeler(vehicleNumber, ownerName, vehicleType);
        }
    }
}

/**
 * TwoWheeler subclass - Bike/Scooter hourly rate ₹10/hr
 */
class TwoWheeler extends Vehicle {
    public TwoWheeler(String vehicleNumber, String ownerName, String vehicleType) {
        super(vehicleNumber, ownerName, vehicleType);
    }

    @Override
    public double getHourlyRate() {
        return 10.0;
    }
}

/**
 * FourWheeler subclass - Car/Truck hourly rate ₹20/hr
 */
class FourWheeler extends Vehicle {
    public FourWheeler(String vehicleNumber, String ownerName, String vehicleType) {
        super(vehicleNumber, ownerName, vehicleType);
    }

    @Override
    public double getHourlyRate() {
        return 20.0;
    }
}
