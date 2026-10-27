SMART PARKING MANAGEMENT SYSTEM - JAVA SWING + MYSQL

FILES
1. ParkingManagementSystem.java - Main Java Swing GUI + JDBC Operations.
2. ParkingSlot.java - OOP Domain Entity representing an individual parking slot (Encapsulation).
3. Vehicle.java - OOP Model hierarchy with polymorphic hourly rate calculations (Inheritance & Polymorphism).
4. database_setup.sql - Creates database, records table, dataset staging table, and master parking_slots table.
5. Smart_Parking_Dataset_100_Records.csv - Converted dataset with 100 vehicle records across slots P01 to P20.
6. lib/mysql-connector-j-8.4.0.jar - Official MySQL JDBC Driver library.
7. .vscode/ - VS Code configuration files for 1-click Run & Debug.

SETUP
A. Install/start MySQL Server and MySQL Workbench.
B. Open database_setup.sql in MySQL Workbench and execute the CREATE DATABASE / CREATE TABLE statements first.
   Important: For the INSERT...SELECT section, first import the CSV into parking_dataset_import using:
   Schemas > smart_parking > Tables > right-click parking_dataset_import > Table Data Import Wizard.
   Choose Smart_Parking_Dataset_100_Records.csv and map its nine columns in order.
   Then execute the INSERT...SELECT section once. If run again, existing record IDs are skipped.
C. Download MySQL Connector/J. Add its JAR to your project's classpath / Libraries.
D. In ParkingManagementSystem.java, replace YOUR_MYSQL_PASSWORD with your MySQL root password.
E. Ensure the JDBC URL uses the right server/port if yours is not localhost:3306.
F. Compile and run ParkingManagementSystem.java.

NOTES
- Dataset Owner_Name is absent, so imported records show a blank owner. New entries capture owner name.
- Imported historical rows retain their given status and fee. New park actions start with status Parked.
- Remove Vehicle marks the selected active record Completed and calculates a fee using example rates:
  Car/Truck = ₹20/hour; Bike/Scooter = ₹10/hour. Change these rates to match your project requirements.
- A completed record is retained as history instead of being deleted. The app checks for another active Parked record in the same slot before inserting.
- For a class submission, create a separate limited-privilege database user rather than keeping a root password in source code.

SLOT AVAILABILITY MODULE FEATURES
- Interactive Visual Grid: Slots P01 through P20 displayed with real-time status.
- Color Coding: Green for AVAILABLE, Red for OCCUPIED (with parked vehicle number).
- Live Counters: Real-time display of Total Slots (20), Available Slots, and Occupied Slots.
- Automatic Sync: Entering or removing a vehicle updates the slot grid and counters immediately.
- Click-to-Park: Clicking any green available slot automatically assigns it to the Entry Form.
- Validation: Rejects invalid slot numbers outside P01-P20, occupied slots, and duplicate vehicle entries.

OOP CONCEPTS USED (FOR COLLEGE VIVA)
1. Encapsulation:
   - ParkingSlot and Vehicle have private fields with public getter and business methods (occupy, vacate, getStatus).
2. Abstraction:
   - Interface ParkingOperations defines parkVehicle, removeVehicle, displayVehicles, updateSlotAvailability.
   - Abstract class Vehicle defines abstract method getHourlyRate().
3. Inheritance:
   - TwoWheeler and FourWheeler inherit from base class Vehicle.
   - ParkingManagementSystem inherits from javax.swing.JFrame.
4. Polymorphism:
   - getHourlyRate() dynamically returns ₹10/hr for bikes/scooters and ₹20/hr for cars/trucks based on vehicle object type.
   - Interface method implementations in ParkingManagementSystem.
