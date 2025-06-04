-- Flight
CREATE INDEX idx_flight_planeid ON Flight(PlaneID);
CREATE INDEX idx_flight_departurecity ON Flight(DepartureCity);
CREATE INDEX idx_flight_arrivalcity ON Flight(ArrivalCity);

-- FlightInstance
CREATE INDEX idx_flightinstance_flightnumber ON FlightInstance(FlightNumber);
CREATE INDEX idx_flightinstance_flightdate ON FlightInstance(FlightDate);

-- Schedule
CREATE INDEX idx_schedule_flightnumber ON Schedule(FlightNumber);
CREATE INDEX idx_schedule_dayofweek ON Schedule(DayOfWeek);

-- Reservation
CREATE INDEX idx_reservation_customerid ON Reservation(CustomerID);
CREATE INDEX idx_reservation_flightinstanceid ON Reservation(FlightInstanceID);
CREATE INDEX idx_reservation_status ON Reservation(Status);

-- Repair
CREATE INDEX idx_repair_planeid ON Repair(PlaneID);
CREATE INDEX idx_repair_technicianid ON Repair(TechnicianID);
CREATE INDEX idx_repair_repairdate ON Repair(RepairDate);

-- MaintenanceRequest
CREATE INDEX idx_maintreq_planeid ON MaintenanceRequest(PlaneID);
CREATE INDEX idx_maintreq_pilotid ON MaintenanceRequest(PilotID);
CREATE INDEX idx_maintreq_requestdate ON MaintenanceRequest(RequestDate);
