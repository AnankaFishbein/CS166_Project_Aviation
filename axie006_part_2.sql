-- Drop tables (from most dependent to least)
DROP TABLE IF EXISTS Reservation CASCADE;
DROP TABLE IF EXISTS Uses CASCADE;
DROP TABLE IF EXISTS Repairs CASCADE;
DROP TABLE IF EXISTS MaintenanceRequest CASCADE;
DROP TABLE IF EXISTS FlightInstance CASCADE;
DROP TABLE IF EXISTS Flight CASCADE;
DROP TABLE IF EXISTS Customer CASCADE;
DROP TABLE IF EXISTS Technician CASCADE;
DROP TABLE IF EXISTS Pilot CASCADE;
DROP TABLE IF EXISTS Plane CASCADE;
DROP TABLE IF EXISTS Schedule CASCADE;


-- Plane
CREATE TABLE Plane (
    PlaneID SERIAL PRIMARY KEY,
    Make CHAR(50) NOT NULL,
    Model CHAR(50) NOT NULL,
    Year INT NOT NULL,
    LastRepairDate DATE
);

-- Technician
CREATE TABLE Technician (
    TechnicianID SERIAL PRIMARY KEY,
    Name CHAR(50) NOT NULL
);

-- Pilot
CREATE TABLE Pilot (
    PilotID SERIAL PRIMARY KEY,
    Name CHAR(50) NOT NULL
);

-- Customer
CREATE TABLE Customer (
    CustomerID SERIAL PRIMARY KEY,
    FirstName CHAR(50) NOT NULL,
    LastName CHAR(50) NOT NULL,
    Gender CHAR(10),
    DOB DATE,
    Phone CHAR(20),
    Address CHAR(100),
    Zip CHAR(10)
);

-- Schedule
CREATE TABLE Schedule (
    ScheduleID SERIAL PRIMARY KEY,
    DayOfWeek CHAR(10) NOT NULL,
    DepartureTime TIME NOT NULL,
    ArrivalTime TIME NOT NULL
);

-- Flight
CREATE TABLE Flight (
    FlightNumber SERIAL PRIMARY KEY,
    DepartureCity CHAR(50) NOT NULL,
    ArrivalCity CHAR(50) NOT NULL,
    ScheduleID INT NOT NULL,
    PlaneID INT NOT NULL,
    FOREIGN KEY (ScheduleID) REFERENCES Schedule(ScheduleID) ON DELETE SET NULL,
    FOREIGN KEY (PlaneID) REFERENCES Plane(PlaneID) ON DELETE CASCADE
);

-- FlightInstance
CREATE TABLE FlightInstance (
    FlightInstanceID SERIAL PRIMARY KEY,
    FlightNumber INT NOT NULL,
    FlightDate DATE NOT NULL,
    DepartedOnTime BOOLEAN,
    ArrivedOnTime BOOLEAN,
    SeatsTotal INT,
    SeatsSold INT,
    NumOfStops INT,
    TicketCost NUMERIC(10,2),
    FOREIGN KEY (FlightNumber) REFERENCES Flight(FlightNumber) ON DELETE CASCADE
);

-- Repairs (M:N)
CREATE TABLE Repairs (
    RepairID SERIAL PRIMARY KEY,
    PlaneID INT NOT NULL,
    TechnicianID INT NOT NULL,
    RepairCode CHAR(20) NOT NULL,
    RepairDate DATE NOT NULL,
    FOREIGN KEY (PlaneID) REFERENCES Plane(PlaneID) ON DELETE CASCADE,
    FOREIGN KEY (TechnicianID) REFERENCES Technician(TechnicianID) ON DELETE SET NULL
);

-- MaintenanceRequest (M:N)
CREATE TABLE MaintenanceRequest (
    RequestID SERIAL PRIMARY KEY,
    PlaneID INT NOT NULL,
    PilotID INT NOT NULL,
    RepairCode CHAR(20) NOT NULL,
    RequestDate DATE NOT NULL,
    FOREIGN KEY (PlaneID) REFERENCES Plane(PlaneID) ON DELETE CASCADE,
    FOREIGN KEY (PilotID) REFERENCES Pilot(PilotID) ON DELETE SET NULL
);

-- Reservation (M:N)
CREATE TABLE Reservation (
    ReservationID SERIAL PRIMARY KEY,
    CustomerID INT NOT NULL,
    FlightInstanceID INT NOT NULL,
    Status CHAR(20) NOT NULL,
    FOREIGN KEY (CustomerID) REFERENCES Customer(CustomerID) ON DELETE CASCADE,
    FOREIGN KEY (FlightInstanceID) REFERENCES FlightInstance(FlightInstanceID) ON DELETE CASCADE
);
