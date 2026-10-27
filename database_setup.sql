CREATE DATABASE IF NOT EXISTS smart_parking;
USE smart_parking;

-- Main table used by the Java application
CREATE TABLE IF NOT EXISTS parking_records (
    record_id INT PRIMARY KEY AUTO_INCREMENT,
    vehicle_number VARCHAR(20) NOT NULL,
    owner_name VARCHAR(100) NULL,
    vehicle_type VARCHAR(20) NOT NULL,
    slot_number VARCHAR(20) NOT NULL,
    entry_time DATETIME NOT NULL,
    exit_time DATETIME NULL,
    duration_hours DECIMAL(8,2) NULL,
    parking_fee DECIMAL(10,2) NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'Parked'
);

-- Temporary import table matching the nine columns in the Excel dataset.
-- Import the CSV into this table using MySQL Workbench's Table Data Import Wizard.
CREATE TABLE IF NOT EXISTS parking_dataset_import (
    Record_ID INT,
    Vehicle_Number VARCHAR(20),
    Vehicle_Type VARCHAR(20),
    Slot_Number VARCHAR(20),
    Entry_Time VARCHAR(30),
    Exit_Time VARCHAR(30),
    Duration_Hours DECIMAL(8,2),
    Parking_Fee DECIMAL(10,2),
    Status VARCHAR(20)
);

-- Run this INSERT once after importing the CSV into parking_dataset_import.
-- It skips IDs already imported into parking_records.
INSERT INTO parking_records
    (record_id, vehicle_number, owner_name, vehicle_type, slot_number,
     entry_time, exit_time, duration_hours, parking_fee, status)
SELECT
    d.Record_ID,
    d.Vehicle_Number,
    NULL,
    d.Vehicle_Type,
    d.Slot_Number,
    STR_TO_DATE(d.Entry_Time, '%Y-%m-%d %H:%i'),
    CASE WHEN d.Exit_Time IS NULL OR TRIM(d.Exit_Time) = ''
         THEN NULL ELSE STR_TO_DATE(d.Exit_Time, '%Y-%m-%d %H:%i') END,
    d.Duration_Hours,
    d.Parking_Fee,
    d.Status
FROM parking_dataset_import d
WHERE NOT EXISTS (
    SELECT 1 FROM parking_records p WHERE p.record_id = d.Record_ID
);

-- Check imported data
SELECT * FROM parking_records ORDER BY record_id;

-- ========================================================
-- ENHANCEMENT: Master Parking Slots Table (P01 to P20)
-- Safe addition: Will NOT drop or delete existing data.
-- ========================================================
CREATE TABLE IF NOT EXISTS parking_slots (
    slot_id INT PRIMARY KEY AUTO_INCREMENT,
    slot_number VARCHAR(10) NOT NULL UNIQUE,
    slot_type VARCHAR(20) NOT NULL DEFAULT 'Standard',
    status VARCHAR(20) NOT NULL DEFAULT 'Available',
    current_vehicle_number VARCHAR(20) NULL
);

-- Safely insert 20 standard slots (P01 - P20) if they do not already exist
INSERT INTO parking_slots (slot_number, slot_type, status)
SELECT slot_num, 'Standard', 'Available'
FROM (
    SELECT 'P01' AS slot_num UNION ALL SELECT 'P02' UNION ALL SELECT 'P03' UNION ALL SELECT 'P04' UNION ALL SELECT 'P05'
    UNION ALL SELECT 'P06' UNION ALL SELECT 'P07' UNION ALL SELECT 'P08' UNION ALL SELECT 'P09' UNION ALL SELECT 'P10'
    UNION ALL SELECT 'P11' UNION ALL SELECT 'P12' UNION ALL SELECT 'P13' UNION ALL SELECT 'P14' UNION ALL SELECT 'P15'
    UNION ALL SELECT 'P16' UNION ALL SELECT 'P17' UNION ALL SELECT 'P18' UNION ALL SELECT 'P19' UNION ALL SELECT 'P20'
) AS default_slots
WHERE NOT EXISTS (
    SELECT 1 FROM parking_slots s WHERE s.slot_number = default_slots.slot_num
);

-- Synchronize slot status with active 'Parked' vehicles from parking_records
UPDATE parking_slots s
LEFT JOIN parking_records r ON s.slot_number = r.slot_number AND r.status = 'Parked'
SET s.status = CASE WHEN r.record_id IS NOT NULL THEN 'Occupied' ELSE 'Available' END,
    s.current_vehicle_number = r.vehicle_number;

-- Ensure AUTO_INCREMENT starts above imported dataset records
ALTER TABLE parking_records AUTO_INCREMENT = 101;

-- Verification Queries
SELECT 'Total Records' AS Metric, COUNT(*) AS Count FROM parking_records
UNION ALL
SELECT 'Currently Parked', COUNT(*) FROM parking_records WHERE status = 'Parked'
UNION ALL
SELECT 'Available Slots', COUNT(*) FROM parking_slots WHERE status = 'Available'
UNION ALL
SELECT 'Occupied Slots', COUNT(*) FROM parking_slots WHERE status = 'Occupied';

