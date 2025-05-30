## 1. General Modeling Assumptions
| #   | Assumption                                                                                                                  | Rationale                                              |
|-----|-----------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------|
| G-1 | Calendar **dates** (YYYY-MM-DD) and local **times** (HH:MM) are stored separately, not as timestamps.                       | Simplifies queries like “all flights on a date.”       |
| G-2 | We use the **Gregorian calendar** and ignore daylight-saving shifts.                                                       | Out of scope for this phase.                           |
| G-3 | Currency values are in **USD** unless noted.                                                                               | Needed for ticket-cost query.                          |
| G-4 | A **week** starts on **Monday** (ISO-8601).                                                                               | Required for “schedule for the week.”                  |

## 2. Flight & Schedule
| #   | Assumption                                                                                                                  | Rationale                                              |
|-----|-----------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------|
| F-1 | **Flight** ⟹ abstract service (e.g., UA 123) that may run on multiple days.                                                 | Matches requirements #1, #2.                           |
| F-2 | **Flight-Instance** = (Flight #, Date). All operational facts (seat sales, on-time info) are attached to a Flight-Instance. | Lets us answer date-specific queries.                  |
| F-3 | Each Flight-Instance has a single **Origin** and **Destination**, but may include zero-or-more **scheduled stops** (stop #, city, ETA/ETD). | For “number of stops scheduled.”        |
| F-4 | A flight is operated by exactly **one airplane type** on a given date.                                                      | Simplifies “find airplane type for a flight.”          |

## 3. Airplanes
| #   | Assumption                                                                                                                  | Rationale                                              |
|-----|-----------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------|
| P-1 | **PlaneID** uniquely identifies each physical aircraft.                                                                     | Needed for repairs & age.                              |
| P-2 | Plane **make + model** describe the type (e.g., Boeing 737-800).                                                            | Returned to customers.                                 |
| P-3 | **Age** = years since first commercial flight (store DATE_IN_SERVICE to avoid recomputing).                                | Durability & consistency.                              |

## 4. Seating, Tickets & Reservations
| #   | Assumption                                                                                                                  | Rationale                                              |
|-----|-----------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------|
| S-1 | Each plane has a fixed seat map; **SeatCount** is derived from PlaneID.                                                     | Enables sold/unsold counts.                            |
| S-2 | A **Reservation** may cover **1 … N Passengers** (traveler party); all share one reservation #.                              | Requirement #6.                                        |
| S-3 | Reservation **Status** ∈ {Reserved, Waitlist, Flown, Cancelled}.                                                            | Matches query #5 cases 1–3.                            |
| S-4 | Wait-listed records never hold an actual **seat #**; seat assigned only when promoted.                                     | Simplifies management logic.                           |
| S-5 | Tickets are considered **sold** once status ∈ {Reserved, Flown}.                                                            | For statistics/metrics query.                          |
| S-6 | Each **Reservation** is made for exactly one **Flight**; a **Flight** may have zero … N **Reservations**.                    | Supports the Reserves relationship between Flight and Reservation. |

## 5. Passengers / Customers
| #   | Assumption                                                                                                                  | Rationale                                              |
|-----|-----------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------|
| C-1 | **Passenger** = traveler; **Customer** = passenger who booked themself. Model one entity (**Passenger**) and capture booking contact via a flag/table. | Avoids duplication.                                     |
| C-2 | Passenger contact info is stored once; reservation stores only first/last name snapshot for manifest history.              | Normalization + audit.                                 |

## 6. Crew & Maintenance
| #   | Assumption                                                                                                                  | Rationale                                              |
|-----|-----------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------|
| M-1 | **Technician** performs maintenance tasks; identified by TECH_ID.                                                          | Enables query #8.                                      |
| M-2 | **Repair** record = (PlaneID, RepairCode, Date, TECH_ID).                                                                  | Requirement #3.                                        |
| M-3 | **RepairCode** comes from an external standard list; semantics not stored.                                                 | Out of scope.                                          |
| M-4 | **Pilot** may file maintenance **Request** (REQUEST_ID, PilotID, PlaneID, Date, Description).                            | For maintenance-staff query #2.                        |
| M-5 | A **MaintenanceRequest** may result in zero … N **Repairs**; a **Repair** may optionally reference one **MaintenanceRequest**. | Reflects 1 : N link (requests → repairs) and optional repair-only work orders. |

## 7. On-time Performance
| #   | Assumption                                                                                                                  | Rationale                                              |
|-----|-----------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------|
| O-1 | Store **actual departure** and **actual arrival** per Flight-Instance; “on-time” = ≤ 15 min late.                           | Enables queries #2, #3, #10.                           |
| O-2 | Percentage of on-time flights is computed on demand from historical instances.                                             | Avoids redundant storage.                              |

## 8. Further Details
| #   | Assumption                                                                                                                  | Rationale                                              |
|-----|-----------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------|
| X-1 | No code-sharing; Flight # is unique across the system.                                                                     | Simplifies keys.                                       |
| X-2 | No overbooking beyond seat capacity; a full flight ⇒ new reservations go to waitlist.                                    | Requirement note.                                      |
| X-3 | All repairs happen **offline** (plane is grounded) and do not overlap; concurrency not modeled.                           | Phase-1 simplification.                                |
