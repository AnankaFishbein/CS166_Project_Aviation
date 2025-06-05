/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import java.lang.Math;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */
public class AirlineManagement {

   // reference to physical database connection.
   private Connection _connection = null;

   // handling the keyboard inputs through a BufferedReader
   // This variable can be global for convenience.
   static BufferedReader in = new BufferedReader(
                                new InputStreamReader(System.in));

   /**
    * Creates a new instance of AirlineManagement
    *
    * @param hostname the MySQL or PostgreSQL server hostname
    * @param database the name of the database
    * @param username the user name used to login to the database
    * @param password the user login password
    * @throws java.sql.SQLException when failed to make a connection.
    */
   public AirlineManagement(String dbname, String dbport, String user, String passwd) throws SQLException {

      System.out.print("Connecting to database...");
      try{
         // constructs the connection URL
         String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
         System.out.println ("Connection URL: " + url + "\n");

         // obtain a physical connection
         this._connection = DriverManager.getConnection(url, user, passwd);
         System.out.println("Done");
      }catch (Exception e){
         System.err.println("Error - Unable to Connect to Database: " + e.getMessage() );
         System.out.println("Make sure you started postgres on this machine");
         System.exit(-1);
      }//end catch
   }//end AirlineManagement

   /**
    * Method to execute an update SQL statement.  Update SQL instructions
    * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
    *
    * @param sql the input SQL string
    * @throws java.sql.SQLException when update failed
    */
   public void executeUpdate (String sql) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the update instruction
      stmt.executeUpdate (sql);

      // close the instruction
      stmt.close ();
   }//end executeUpdate

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and outputs the results to
    * standard out.
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQueryAndPrintResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and output them to standard out.
      boolean outputHeader = true;
      while (rs.next()){
		 if(outputHeader){
			for(int i = 1; i <= numCol; i++){
			System.out.print(rsmd.getColumnName(i) + "\t");
			}
			System.out.println();
			outputHeader = false;
		 }
         for (int i=1; i<=numCol; ++i)
            System.out.print (rs.getString (i) + "\t");
         System.out.println ();
         ++rowCount;
      }//end while
      stmt.close();
      return rowCount;
   }//end executeQuery

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the results as
    * a list of records. Each record in turn is a list of attribute values
    *
    * @param query the input query string
    * @return the query result as a list of records
    * @throws java.sql.SQLException when failed to execute the query
    */
   public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and saves the data returned by the query.
      boolean outputHeader = false;
      List<List<String>> result  = new ArrayList<List<String>>();
      while (rs.next()){
        List<String> record = new ArrayList<String>();
		for (int i=1; i<=numCol; ++i)
			record.add(rs.getString (i));
        result.add(record);
      }//end while
      stmt.close ();
      return result;
   }//end executeQueryAndReturnResult

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the number of results
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQuery (String query) throws SQLException {
       // creates a statement object
       Statement stmt = this._connection.createStatement ();

       // issues the query instruction
       ResultSet rs = stmt.executeQuery (query);

       int rowCount = 0;

       // iterates through the result set and count nuber of results.
       while (rs.next()){
          rowCount++;
       }//end while
       stmt.close ();
       return rowCount;
   }

   /**
    * Method to fetch the last value from sequence. This
    * method issues the query to the DBMS and returns the current
    * value of sequence used for autogenerated keys
    *
    * @param sequence name of the DB sequence
    * @return current value of a sequence
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int getCurrSeqVal(String sequence) throws SQLException {
	Statement stmt = this._connection.createStatement ();

	ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
	if (rs.next())
		return rs.getInt(1);
	return -1;
   }

   /**
    * Method to close the physical connection if it is open.
    */
   public void cleanup(){
      try{
         if (this._connection != null){
            this._connection.close ();
         }//end if
      }catch (SQLException e){
         // ignored.
      }//end try
   }//end cleanup

   /**
    * The main execution method
    *
    * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
    */
   public static void main(String[] args) {
   if (args.length != 3) {
      System.err.println (
         "Usage: " +
         "java [-classpath <classpath>] " +
         AirlineManagement.class.getName () +
         " <dbname> <port> <user>");
      return;
   }//end if

   Greeting();
   AirlineManagement esql = null;
   try{
      // use postgres JDBC driver.
      Class.forName ("org.postgresql.Driver").newInstance ();
      // instantiate the AirlineManagement object and creates a physical connection.
      String dbname = args[0];
      String dbport = args[1];
      String user = args[2];
      esql = new AirlineManagement (dbname, dbport, user, "");

      // Use modularized menu system
      beforeLogin(esql);

   }catch(Exception e) {
      System.err.println (e.getMessage ());
   }finally{
      // make sure to cleanup the created table and close the connection.
      try{
         if(esql != null) {
            System.out.print("Disconnecting from database...");
            esql.cleanup ();
            System.out.println("Done\n\nBye !");
         }//end if
      }catch (Exception e) {
         // ignored.
      }//end try
      }//end try
   }//end main


   public static void Greeting(){
      System.out.println(
         "\n\n*******************************************************\n" +
         "              User Interface      	               \n" +
         "*******************************************************\n");
   }//end Greeting

   /**
    * This method is called before the user logs in.
    * It displays the main menu and allows the user to create a new user or log in.
    */
   public static void beforeLogin(AirlineManagement esql) {
   boolean keepon = true;
      while(keepon) {
         System.out.println("MAIN MENU");
         System.out.println("---------");
         System.out.println("1. Create user");
         System.out.println("2. Log in");
         System.out.println("9. < EXIT");
         System.out.print("Please make your choice: ");
         int choice = readChoice();
         String authorisedUser = null;
         switch (choice){
            case 1: CreateUser(esql); break;
            case 2: authorisedUser = LogIn(esql); break;
            case 9: keepon = false; break;
            default : System.out.println("Unrecognized choice!"); break;
         }
         if (authorisedUser != null) {
            afterLogin(esql, authorisedUser);
         }
      }
   }//end beforeLogin

   /**
    * This method is called after the user logs in.
    * It displays the user-specific menu based on their role.
    */
   public static void afterLogin(AirlineManagement esql, String authorisedUser) {
   String[] authParts = authorisedUser.split(":");
   String role = authParts[0];
   String id = (authParts.length > 1) ? authParts[1] : null;
   boolean usermenu = true;
   while(usermenu) {
      System.out.println();
      System.out.println("MAIN MENU (" + role + ")");
      System.out.println("----------------------");
      // Role-based menu printing
      if (role.equalsIgnoreCase("Manager")) {
         System.out.println("1. View Flights");
         System.out.println("2. View Flight Seats");
         System.out.println("3. View Flight Status");
         System.out.println("4. View Flights of the day");
         System.out.println("5. View Full Order ID History");
         // ...more management options as needed...
      }
      if (role.equalsIgnoreCase("Customer")) {
         System.out.println("10. Search Flights");
         System.out.println("11. Make Reservation");
         // ...more customer options...
      }
      if (role.equalsIgnoreCase("Pilot")) {
         System.out.println("15. Maintenance Request");
         // ...more pilot options...
      }
      if (role.equalsIgnoreCase("Technician")) {
         System.out.println("16. View Repairs");
         System.out.println("17. Add Repair Record");
         // ...more technician options...
      }
      System.out.println("20. Log out");
      System.out.print("Please make your choice: ");
      int choice = readChoice();

      switch (choice) {
         // Management
         case 1: if (role.equalsIgnoreCase("Manager")) ViewFlights(esql); else notAuthorized(); break;
         case 2: if (role.equalsIgnoreCase("Manager")) ViewFlightSeats(esql); else notAuthorized(); break;
         case 3: if (role.equalsIgnoreCase("Manager")) ViewFlightStatus(esql); else notAuthorized(); break;
         case 4: if (role.equalsIgnoreCase("Manager")) ViewFlightsOfTheDay(esql); else notAuthorized(); break;
         case 5: if (role.equalsIgnoreCase("Manager")) ViewOrderHistory(esql); else notAuthorized(); break;
         // Add more management functions as needed

         // Customer
         case 10: if (role.equalsIgnoreCase("Customer")) SearchFlights(esql); else notAuthorized(); break;
         case 11: if (role.equalsIgnoreCase("Customer")) MakeReservation(esql); else notAuthorized(); break;
         // Add more customer functions as needed

         // Pilot
         case 15: if (role.equalsIgnoreCase("Pilot")) MaintenanceRequest(esql); else notAuthorized(); break;
         // Add more pilot functions as needed

         // Technician
         case 16: if (role.equalsIgnoreCase("Technician")) ViewRepairs(esql); else notAuthorized(); break;
         case 17: if (role.equalsIgnoreCase("Technician")) AddRepairRecord(esql); else notAuthorized(); break;
         // Add more technician functions as needed

         // Log out
         case 20: usermenu = false; break;

         default: System.out.println("Unrecognized choice!"); break;
      }
      }
   }//end afterLogin

   // Helper method for login authorization
   private static void notAuthorized() {
      System.out.println("You are not authorized to use this function.");
   }


   /*
    * Reads the users choice given from the keyboard
    * @int
    **/
   // public static int readChoice() {
   //    int input;
   //    // returns only if a correct value is given.
   //    do {
   //       System.out.print("Please make your choice: ");
   //       try { // read the integer, parse it and break.
   //          input = Integer.parseInt(in.readLine());
   //          break;
   //       }catch (Exception e) {
   //          System.out.println("Your input is invalid!");
   //          continue;
   //       }//end try
   //    }while (true);
   //    return input;
   // }//end readChoice

   public static int readChoice() {
      BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
      try {
         String input = in.readLine();
         return Integer.parseInt(input.trim());
      } catch (Exception e) {
         System.out.println("Your input is invalid!");
         return -1;
      }
   }


   /*
    * Creates a new user
    **/
   public static void CreateUser(AirlineManagement esql) {
     try {
	BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

	System.out.println("Select user type:");
	System.out.println("1. Customer");
	System.out.println("2. Pilot");
	System.out.println("3. Technician");
	System.out.println("Enter choice: ");
	String choice = in.readLine().trim();

	switch(choice) {
	   case "1":
		System.out.print("Enter First Name: ");
		String firstName = in.readLine().trim();
		System.out.print("Enter Last Name: ");
		String lastName = in.readLine().trim();
		System.out.print("Enter Gender: ");
		String gender = in.readLine().trim();
		System.out.print("Enter Date of Birth (YYYY-MM-DD): ");
		String birth = in.readLine().trim();
		System.out.print("Enter Address: ");
		String address = in.readLine().trim();
		System.out.print("Enter Phone #: ");
		String phoneNumber = in.readLine().trim();
		System.out.print("Enter Zip Code: ");
		String zip = in.readLine().trim();

		String insertCustomer = string.format(
		    "INSERT INTO Customer (FirstName, LastName, Gender, DOB, Address, Phone, Zip) ",
		    "VALUES ('%s', '%s', '%s', DATE '%s', '%s', '%s', '%s')",
		    firstName, lastName, gender, birth, address, phoneNumber, zip
		);
		esql.executeUpdate(insertCustomer);
		System.out.println("Customer created.");
		break;

	 case "2":
	      System.out.print("Enter unique Pilot ID: ");
      	      String pilotId = in.readLine().trim();
	      System.out.print("Enter Pilot Name: ");
	      String pilotName = in.readLine().trim();

	      String insertPilot = string.format(
		  "INSERT INTO Pilot (PilotID, Name) VALUES ('%s', '%s')", 
    		  pilotId, pilotName
 	      );
	      esql.executeUpdate(insertPilot);
	      System.out.println("Pilot created.");
	      break;

	case "3":	      
	     System.out.print("Enter unique Technician ID: ");
	     String techId = in.readLine().trim();
	     System.out.print("Enter Technician Name: ");
	     String techName = in.readLine().trim();

	     String insertTechnician = string.format(
	 	 "INSERT INTO Technician (TechnicianID, Name) VALUES ('%s', '%s')",
	         techId, techName
	     );
    	     esql.executeUpdate(insertTechnician);
	     System.out.println("Technician created.");
             break;

	default:
	   System.out.println("Invalid choice. Returning to main menu.");
      }
     } catch (Exception e) {
	  System.err.println("Error creating user: " + e.getMessage());
     }	  

   }//end CreateUser


   /*
    * Check log in credentials for an existing user
    * @return User login or null is the user does not exist
    **/
   /*
 * Check log in credentials for an existing user
 * @return String with role and user id, e.g. "Customer:3" or "Manager" or null if failed
 **/
/*
 * Check log in credentials for an existing user
 * @return String with role and user id, e.g. "Customer:3" or "Manager" or null if failed
 **/
public static String LogIn(AirlineManagement esql) {
    try {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Login as: 1. Customer 2. Pilot 3. Technician 4. Manager");
        String roleChoice = in.readLine().trim();
        String userTable = "";
        String userIdCol = "";
        String loginName = "";
        switch(roleChoice) {
            case "1": // Customer
                userTable = "Customer";
                userIdCol = "CustomerID";
                System.out.print("Customer First Name: ");
                String firstName = in.readLine().trim();
                System.out.print("Customer Last Name: ");
                String lastName = in.readLine().trim();
                System.out.print("Customer ID: ");
                String customerId = in.readLine().trim();
                String query = "SELECT CustomerID FROM Customer " +
                               "WHERE FirstName = '" + firstName + "' " +
                               "AND LastName = '" + lastName + "' " +
                               "AND CustomerID = " + customerId;
                List<List<String>> result = esql.executeQueryAndReturnResult(query);
                if(result.size() > 0) {
                    System.out.println("Login successful as Customer!");
                    return "Customer:" + result.get(0).get(0);
                } else {
                    System.out.println("Login failed! Customer not found.");
                    return null;
                }
            case "2": // Pilot
                userTable = "Pilot";
                userIdCol = "PilotID";
                System.out.print("Pilot Name: ");
                loginName = in.readLine().trim();
                String queryPilot = "SELECT PilotID FROM Pilot WHERE Name = '" + loginName + "'";
                List<List<String>> resultPilot = esql.executeQueryAndReturnResult(queryPilot);
                if(resultPilot.size() > 0) {
                    System.out.println("Login successful as Pilot!");
                    return "Pilot:" + resultPilot.get(0).get(0);
                } else {
                    System.out.println("Login failed! Pilot not found.");
                    return null;
                }
            case "3": // Technician
                userTable = "Technician";
                userIdCol = "TechnicianID";
                System.out.print("Tech Name: ");
                loginName = in.readLine().trim();
                String queryTech = "SELECT TechnicianID FROM Technician WHERE Name = '" + loginName + "'";
                List<List<String>> resultTech = esql.executeQueryAndReturnResult(queryTech);
                if(resultTech.size() > 0) {
                    System.out.println("Login successful as Technician!");
                    return "Technician:" + resultTech.get(0).get(0);
                } else {
                    System.out.println("Login failed! Technician not found.");
                    return null;
                }
            case "4": // Manager
                System.out.print("Enter manager password: ");
                String pw = in.readLine();
                if(pw.equals("24601")) { //"admin123" is a placeholder for the actual password
                    System.out.println("Manager login successful!");
                    return "Manager";
                } else {
                    System.out.println("Incorrect password!");
                    return null;
                }
            default:
                System.out.println("Unrecognized user type.");
                return null;
        }
    } catch(Exception e) {
        System.err.println(e.getMessage());
        return null;
    }
}//end login

// Rest of the functions definition go in here

   /*
 * Lists all flights with details
 **/
   public static void ViewFlights(AirlineManagement esql) {
      try {
         String query = "SELECT f.FlightNumber, f.DepartureCity, f.ArrivalCity, f.PlaneID, " +
                        "s.DayOfWeek, s.DepartureTime, s.ArrivalTime " +
                        "FROM Flight f JOIN Schedule s ON f.ScheduleID = s.ScheduleID";
         esql.executeQueryAndPrintResult(query);
      } catch(Exception e) {
         System.err.println(e.getMessage());
      }
   }//end ViewFlights

   /*
 * Shows total and sold seats for a given flight and date
 **/
   public static void ViewFlightSeats(AirlineManagement esql) {
      try {
         System.out.print("Enter flight number: ");
         int flightNumber = Integer.parseInt(in.readLine());
         System.out.print("Enter flight date (yyyy-mm-dd): ");
         String dateStr = in.readLine();
         String query = "SELECT SeatsTotal, SeatsSold FROM FlightInstance " +
                        "WHERE FlightNumber = " + flightNumber + 
                        " AND FlightDate = DATE '" + dateStr + "'";
         esql.executeQueryAndPrintResult(query);
      } catch(Exception e) {
         System.err.println(e.getMessage());
      }
   }//end ViewFlightSeats

   /*
 * Shows departed/arrived on time for a given flight and date
 **/
   public static void ViewFlightStatus(AirlineManagement esql) {
      try {
         System.out.print("Enter flight number: ");
         int flightNumber = Integer.parseInt(in.readLine());
         System.out.print("Enter flight date (yyyy-mm-dd): ");
         String dateStr = in.readLine();
         String query = "SELECT DepartedOnTime, ArrivedOnTime FROM FlightInstance " +
                        "WHERE FlightNumber = " + flightNumber + 
                        " AND FlightDate = DATE '" + dateStr + "'";
         esql.executeQueryAndPrintResult(query);
      } catch(Exception e) {
         System.err.println(e.getMessage());
      }
   }//end ViewFlightStatus

   /*
 * Lists all flights scheduled on a given date
 **/
   public static void ViewFlightsOfTheDay(AirlineManagement esql) {
      try {
         System.out.print("Enter date (yyyy-mm-dd): ");
         String dateStr = in.readLine();
         String query = "SELECT fi.FlightInstanceID, fi.FlightNumber, f.DepartureCity, f.ArrivalCity " +
                        "FROM FlightInstance fi JOIN Flight f ON fi.FlightNumber = f.FlightNumber " +
                        "WHERE fi.FlightDate = DATE '" + dateStr + "'";
         esql.executeQueryAndPrintResult(query);
      } catch(Exception e) {
         System.err.println(e.getMessage());
      }
   }//end ViewFlightsOfTheDay

   /*
   * Shows all reservations with customer and flight info
   **/
   public static void ViewOrderHistory(AirlineManagement esql) {
      try {
         String query = "SELECT r.ReservationID, r.Status, c.FirstName, c.LastName, fi.FlightNumber, fi.FlightDate " +
                        "FROM Reservation r " +
                        "JOIN Customer c ON r.CustomerID = c.CustomerID " +
                        "JOIN FlightInstance fi ON r.FlightInstanceID = fi.FlightInstanceID";
         esql.executeQueryAndPrintResult(query);
      } catch(Exception e) {
         System.err.println(e.getMessage());
      }
   }//end ViewOrderHistory

   public static void SearchFlights(AirlineManagement esql) {
    try {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("Enter Departure City: ");
        String depCity = in.readLine().trim();
        System.out.print("Enter Arrival City: ");
        String arrCity = in.readLine().trim();
        System.out.print("Enter Date (yyyy-mm-dd): ");
        String flightDate = in.readLine().trim();

        // Find matching flights for the date
        String query = 
            "SELECT f.FlightNumber, fi.FlightDate, s.DepartureTime, s.ArrivalTime, fi.NumOfStops, " +
            "ROUND(100.0 * SUM(CASE WHEN fi.DepartedOnTime THEN 1 ELSE 0 END) / COUNT(*), 2) AS OnTimePercent " +
            "FROM Flight f " +
            "JOIN FlightInstance fi ON f.FlightNumber = fi.FlightNumber " +
            "JOIN Schedule s ON s.FlightNumber = f.FlightNumber " +
            "WHERE f.DepartureCity = '" + depCity + "' " +
            "AND f.ArrivalCity = '" + arrCity + "' " +
            "AND fi.FlightDate = DATE '" + flightDate + "' " +
            "GROUP BY f.FlightNumber, fi.FlightDate, s.DepartureTime, s.ArrivalTime, fi.NumOfStops";

        esql.executeQueryAndPrintResult(query);

    } catch(Exception e) {
        System.err.println(e.getMessage());
    }
}

public static void MakeReservation(AirlineManagement esql) {
    try {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("Enter your CustomerID: ");
        String custId = in.readLine().trim();
        System.out.print("Enter Flight Number: ");
        String flightNum = in.readLine().trim();
        System.out.print("Enter Flight Date (yyyy-mm-dd): ");
        String flightDate = in.readLine().trim();

        // Find the FlightInstanceID
        String findInstance = "SELECT FlightInstanceID, SeatsTotal, SeatsSold FROM FlightInstance " +
                              "WHERE FlightNumber = '" + flightNum + "' " +
                              "AND FlightDate = DATE '" + flightDate + "'";
        List<List<String>> result = esql.executeQueryAndReturnResult(findInstance);

        if(result.isEmpty()) {
            System.out.println("No flight instance found.");
            return;
        }
        String instanceId = result.get(0).get(0);
        int seatsTotal = Integer.parseInt(result.get(0).get(1));
        int seatsSold = Integer.parseInt(result.get(0).get(2));

        String status = (seatsSold < seatsTotal) ? "reserved" : "waitlist";

        // Insert into Reservation (ReservationID can be generated or auto)
        String insertRes = "INSERT INTO Reservation (CustomerID, FlightInstanceID, Status) " +
                           "VALUES (" + custId + ", " + instanceId + ", '" + status + "')";
        esql.executeUpdate(insertRes);

        System.out.println("Reservation " + (status.equals("reserved") ? "confirmed!" : "added to waitlist!"));

    } catch(Exception e) {
        System.err.println(e.getMessage());
    }
}

public static void MaintenanceRequest(AirlineManagement esql) {
    try {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("Enter your PilotID: ");
        String pilotId = in.readLine().trim();
        System.out.print("Enter PlaneID: ");
        String planeId = in.readLine().trim();
        System.out.print("Enter Repair Code: ");
        String repairCode = in.readLine().trim();
        System.out.print("Enter Request Date (yyyy-mm-dd): ");
        String requestDate = in.readLine().trim();

        String insertReq = "INSERT INTO MaintenanceRequest (PlaneID, RepairCode, RequestDate, PilotID) " +
                           "VALUES ('" + planeId + "', '" + repairCode + "', DATE '" + requestDate + "', '" + pilotId + "')";
        esql.executeUpdate(insertReq);
        System.out.println("Maintenance request submitted!");

    } catch(Exception e) {
        System.err.println(e.getMessage());
    }
}

public static void ViewRepairs(AirlineManagement esql) {
    try {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("Enter your TechnicianID: ");
        String techId = in.readLine().trim();

        String query = "SELECT RepairID, PlaneID, RepairCode, RepairDate " +
                       "FROM Repair WHERE TechnicianID = '" + techId + "'";
        esql.executeQueryAndPrintResult(query);

    } catch(Exception e) {
        System.err.println(e.getMessage());
    }
}

public static void AddRepairRecord(AirlineManagement esql) {
    try {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("Enter your TechnicianID: ");
        String techId = in.readLine().trim();
        System.out.print("Enter PlaneID: ");
        String planeId = in.readLine().trim();
        System.out.print("Enter Repair Code: ");
        String repairCode = in.readLine().trim();
        System.out.print("Enter Repair Date (yyyy-mm-dd): ");
        String repairDate = in.readLine().trim();

        String insertRepair = "INSERT INTO Repair (PlaneID, RepairCode, RepairDate, TechnicianID) " +
                              "VALUES ('" + planeId + "', '" + repairCode + "', DATE '" + repairDate + "', '" + techId + "')";
        esql.executeUpdate(insertRepair);
        System.out.println("Repair record added!");

    } catch(Exception e) {
        System.err.println(e.getMessage());
    }
}

  


}//end AirlineManagement

