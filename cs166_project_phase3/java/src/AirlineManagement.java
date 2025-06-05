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
import java.io.IOException;
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
    * B
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
		String firstName = promptForValidFirstName(in);
		if (firstName == null) return;
		String lastName = promptForValidLastName(in);
		if (lastName == null) return;
		String gender = promptForValidGender(in);
        if (gender == null) return;
		String birth = promptForValidDate(in);
		if (birth == null) return;
		String address = promptForValidAddress(in);
        if (address == null) return;
		String phoneNumber = promptForValidPhone(in);
        if (phoneNumber == null) return;
		String zip = promptForValidZip(in);
        if (zip == null) return;

		String getMaxId = "SELECT COALESCE(MAX(CustomerID), 0) FROM Customer";
		List<List<String>> result = esql.executeQueryAndReturnResult(getMaxId);
		int newId = Integer.parseInt(result.get(0).get(0)) + 1;

		String insertCustomer = String.format(
		   "INSERT INTO Customer (CustomerID, FirstName, LastName, Gender, DOB, Address, Phone, Zip) " + 
		   "VALUES (%d, '%s', '%s', '%s', DATE '%s', '%s', '%s', '%s')",
		    newId, firstName, lastName, gender, birth, address, phoneNumber, zip
		);
		esql.executeUpdate(insertCustomer);
		System.out.println("Customer created with ID: " + newId);
		break;

	 case "2":
	      String getMaxPilot = "SELECT COALESCE(MAX(CAST(SUBSTRING(PilotID, 2) AS INTEGER)), 0) FROM Pilot";
	      List<List<String>> pilotResult = esql.executeQueryAndReturnResult(getMaxPilot);
	      int newPilotID = Integer.parseInt(pilotResult.get(0).get(0)) + 1;
	      String pilotId = String.format("P%03d", newPilotID);

          String pilotName = promptForValidFullName(in, "Pilot");
          if (pilotName == null) return;

	      String insertPilot = String.format(
		  "INSERT INTO Pilot (PilotID, Name) VALUES ('%s', '%s')", 
    		  pilotId, pilotName
 	      );
	      esql.executeUpdate(insertPilot);
	      System.out.println("Pilot created with ID: " + pilotId);
	      break;

	case "3":	      
	     String getMaxTech = "SELECT COALESCE(MAX(CAST(SUBSTRING(TechnicianID, 2) AS INTEGER)), 0) FROM Technician";
	     List<List<String>> techResult = esql.executeQueryAndReturnResult(getMaxTech);
	     int newTechID = Integer.parseInt(techResult.get(0).get(0)) + 1;
	     String techId = String.format("T%03d", newTechID);

         String techName = promptForValidFullName(in, "Technician");
         if (techName == null) return;

	     String insertTechnician = String.format(
	 	 "INSERT INTO Technician (TechnicianID, Name) VALUES ('%s', '%s')",
	         techId, techName
	     );
    	     esql.executeUpdate(insertTechnician);
	     System.out.println("Technician created with ID: " + techId);
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
                // Prompt for first and last name(lengths 2-15 and 2-30 respectively)
                String firstName = promptForValidFirstName(in);
                if (firstName == null) return null; // handle too many invalid attempts
                String lastName = promptForValidLastName(in);
                if (lastName == null) return null;
                // Prompt for customer ID
                String customerId = promptForValidCustomerID(in);
                if (customerId == null) return null;

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
                // Prompt for full name (Pilot: "Holly Wood")
               loginName = promptForValidFullName(in, "Pilot");
                if (loginName == null) return null;

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
                // Prompt for full name (Technician: "Gina Moore")
                loginName = promptForValidFullName(in, "Technician");
                if (loginName == null) return null;

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
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        // Use the robust flight number guard!
        String flightNumber = promptForValidFlightNumber(in);
        if (flightNumber == null) {
            System.out.println("Returning to main menu.");
            return;
        }

        String query = 
            "SELECT s.DayOfWeek, s.DepartureTime, s.ArrivalTime " +
            "FROM Schedule s " +
            "WHERE s.FlightNumber = '" + flightNumber + "' " +
            "ORDER BY " +
            "CASE " +
            "   WHEN s.DayOfWeek = 'Monday' THEN 1 " +
            "   WHEN s.DayOfWeek = 'Tuesday' THEN 2 " +
            "   WHEN s.DayOfWeek = 'Wednesday' THEN 3 " +
            "   WHEN s.DayOfWeek = 'Thursday' THEN 4 " +
            "   WHEN s.DayOfWeek = 'Friday' THEN 5 " +
            "   WHEN s.DayOfWeek = 'Saturday' THEN 6 " +
            "   WHEN s.DayOfWeek = 'Sunday' THEN 7 " +
            "   ELSE 8 END";

        int results = esql.executeQueryAndPrintResult(query);

        if (results == 0) {
            System.out.println("No weekly schedule found for flight " + flightNumber + ".");
        }

    } catch(Exception e) {
        System.err.println(e.getMessage());
    }
}//end ViewFlights


   /*
 * Shows total and sold seats for a given flight and date
 **/
 public static void ViewFlightSeats(AirlineManagement esql) {
    try {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

        // Use robust flight number input guard
        String flightNumber = promptForValidFlightNumber(in);
        if (flightNumber == null) {
            System.out.println("Returning to main menu.");
            return;
        }

        // Use robust date input guard
        String flightDate = promptForValidDate(in);
        if (flightDate == null) {
            System.out.println("Returning to main menu.");
            return;
        }

        String query = "SELECT SeatsTotal, SeatsSold " +
                       "FROM FlightInstance " +
                       "WHERE FlightNumber = '" + flightNumber + "' " +
                       "AND FlightDate = DATE '" + flightDate + "'";

        List<List<String>> result = esql.executeQueryAndReturnResult(query);
        if (result.size() == 0) {
            System.out.println("No seat information found for this flight and date.");
        } else {
            int seatsTotal = Integer.parseInt(result.get(0).get(0));
            int seatsSold = Integer.parseInt(result.get(0).get(1));
            int seatsAvailable = seatsTotal - seatsSold;
            System.out.println("Flight: " + flightNumber + " on " + flightDate);
            System.out.println("Seats Sold: " + seatsSold);
            System.out.println("Seats Still Available: " + seatsAvailable);
        }
    } catch(Exception e) {
        System.err.println(e.getMessage());
    }
}//end ViewFlightSeats


   /*
 * Shows departed/arrived on time for a given flight and date
 **/
   public static void ViewFlightStatus(AirlineManagement esql) {
    try {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        // Input guards
        String flightNumber = promptForValidFlightNumber(in);
        if (flightNumber == null) {
            System.out.println("Returning to main menu.");
            return;
        }

        String flightDate = promptForValidDate(in);
        if (flightDate == null) {
            System.out.println("Returning to main menu.");
            return;
        }

        String query = 
            "SELECT s.DepartureTime, s.ArrivalTime, fi.DepartedOnTime, fi.ArrivedOnTime " +
            "FROM FlightInstance fi " +
            "JOIN Schedule s ON fi.FlightNumber = s.FlightNumber " +
            "AND s.DayOfWeek = TO_CHAR(fi.FlightDate, 'FMDay') " +
            "WHERE fi.FlightNumber = '" + flightNumber + "' " +
            "AND fi.FlightDate = DATE '" + flightDate + "'";

        List<List<String>> result = esql.executeQueryAndReturnResult(query);
        if (result.size() == 0) {
            System.out.println("No flight status information found for this flight and date.");
        } else {
            // Should be at most one result now
            List<String> row = result.get(0);
            String schedDep = row.get(0);
            String schedArr = row.get(1);
            String departed = row.get(2).equals("t") ? "Yes" : "No";
            String arrived = row.get(3).equals("t") ? "Yes" : "No";
            System.out.println("-----------------------------------------");
            System.out.println("Scheduled Departure: " + schedDep);
            System.out.println("Scheduled Arrival:   " + schedArr);
            System.out.println("Departed On Time:    " + departed);
            System.out.println("Arrived On Time:     " + arrived);
            System.out.println("-----------------------------------------");
        }
    } catch(Exception e) {
        System.err.println(e.getMessage());
    }
}//end ViewFlightStatus

   /*
 * Lists all flights scheduled on a given date
 **/
   public static void ViewFlightsOfTheDay(AirlineManagement esql) {
    try {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

        String flightDate = promptForValidDate(in);
        if (flightDate == null) {
            System.out.println("Returning to main menu.");
            return;
        }

        String query =
            "SELECT fi.FlightInstanceID, fi.FlightNumber, f.DepartureCity, f.ArrivalCity, fi.NumOfStops, " +
            "s.DepartureTime, s.ArrivalTime, fi.DepartedOnTime, fi.ArrivedOnTime " +
            "FROM FlightInstance fi " +
            "JOIN Flight f ON fi.FlightNumber = f.FlightNumber " +
            "JOIN Schedule s ON fi.FlightNumber = s.FlightNumber " +
            "AND s.DayOfWeek = TO_CHAR(fi.FlightDate, 'FMDay') " +
            "WHERE fi.FlightDate = DATE '" + flightDate + "' " +
            "ORDER BY fi.FlightNumber, s.DepartureTime";

        List<List<String>> result = esql.executeQueryAndReturnResult(query);
        if (result.size() == 0) {
            System.out.println("No flights scheduled on " + flightDate + ".");
        } else {
            System.out.println("Flights scheduled on " + flightDate + ":");
            System.out.println("InstanceID | FlightNumber | From         | To           | Stops | Departure | Arrival   | DepartedOnTime | ArrivedOnTime");
            for (List<String> row : result) {
                String departed = row.get(7).equals("t") ? "Yes" : "No";
                String arrived  = row.get(8).equals("t") ? "Yes" : "No";
                System.out.printf("%10s | %12s | %-12s | %-12s | %5s | %-9s | %-8s | %-14s | %-13s\n",
                        row.get(0), row.get(1), row.get(2), row.get(3), row.get(4),
                        row.get(5), row.get(6), departed, arrived);
            }
        }
    } catch(Exception e) {
        System.err.println(e.getMessage());
    }
}//end ViewFlightsOfTheDay

   /*
   * Shows all reservations with customer and flight info
   **/
   public static void ViewOrderHistory(AirlineManagement esql) {
    try {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

        String flightNumber = promptForValidFlightNumber(in);
        if (flightNumber == null) {
            System.out.println("Returning to main menu.");
            return;
        }

        String flightDate = promptForValidDate(in);
        if (flightDate == null) {
            System.out.println("Returning to main menu.");
            return;
        }

        // Find FlightInstanceID
        String findInstance = "SELECT FlightInstanceID FROM FlightInstance " +
                              "WHERE FlightNumber = '" + flightNumber + "' " +
                              "AND FlightDate = DATE '" + flightDate + "'";
        List<List<String>> instanceResult = esql.executeQueryAndReturnResult(findInstance);
        if (instanceResult.size() == 0) {
            System.out.println("No flight instance found for " + flightNumber + " on " + flightDate + ".");
            return;
        }
        String instanceId = instanceResult.get(0).get(0);

        // 1. All reservations
        System.out.println("\nPassengers who made a reservation:");
        String queryAll =
            "SELECT c.CustomerID, c.FirstName, c.LastName, r.Status " +
            "FROM Reservation r " +
            "JOIN Customer c ON r.CustomerID = c.CustomerID " +
            "WHERE r.FlightInstanceID = " + instanceId +
            " ORDER BY r.Status";
        List<List<String>> allResults = esql.executeQueryAndReturnResult(queryAll);
        if (allResults.size() == 0) {
            System.out.println("  None.");
        } else {
            System.out.printf("| %-8s | %-15s | %-15s | %-10s |\n", "ID", "First Name", "Last Name", "Status");
            System.out.println("|----------|-----------------|-----------------|------------|");
            for (List<String> row : allResults) {
                System.out.printf("| %-8s | %-15s | %-15s | %-10s |\n", row.get(0), row.get(1), row.get(2), row.get(3));
            }
        }

        // 2. Waitlist only
        System.out.println("\nPassengers on the waiting list:");
        String queryWaitlist =
            "SELECT c.CustomerID, c.FirstName, c.LastName " +
            "FROM Reservation r " +
            "JOIN Customer c ON r.CustomerID = c.CustomerID " +
            "WHERE r.FlightInstanceID = " + instanceId + " AND r.Status = 'waitlist'";
        List<List<String>> waitResults = esql.executeQueryAndReturnResult(queryWaitlist);
        if (waitResults.size() == 0) {
            System.out.println("  None.");
        } else {
            System.out.printf("| %-8s | %-15s | %-15s |\n", "ID", "First Name", "Last Name");
            System.out.println("|----------|-----------------|-----------------|");
            for (List<String> row : waitResults) {
                System.out.printf("| %-8s | %-15s | %-15s |\n", row.get(0), row.get(1), row.get(2));
            }
        }

        // 3. Actually flown
        System.out.println("\nPassengers who actually flew:");
        String queryFlown =
            "SELECT c.CustomerID, c.FirstName, c.LastName " +
            "FROM Reservation r " +
            "JOIN Customer c ON r.CustomerID = c.CustomerID " +
            "WHERE r.FlightInstanceID = " + instanceId + " AND r.Status = 'flown'";
        List<List<String>> flownResults = esql.executeQueryAndReturnResult(queryFlown);
        if (flownResults.size() == 0) {
            System.out.println("  None.");
        } else {
            System.out.printf("| %-8s | %-15s | %-15s |\n", "ID", "First Name", "Last Name");
            System.out.println("|----------|-----------------|-----------------|");
            for (List<String> row : flownResults) {
                System.out.printf("| %-8s | %-15s | %-15s |\n", row.get(0), row.get(1), row.get(2));
            }
        }

    } catch(Exception e) {
        System.err.println(e.getMessage());
    }
}//end ViewOrderHistory

   public static void SearchFlights(AirlineManagement esql) {
    try {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
         // Use the promptForValidCity helper method to ensure valid input
        String depCity = promptForValidCity(in, "Enter Departure City");
         if (depCity == null) {
            System.out.println("Returning to main menu.");
            return;
         }
         String arrCity = promptForValidCity(in, "Enter Arrival City");
         if (arrCity == null) {
            System.out.println("Returning to main menu.");
            return;
         }

        // Use the date validation helper
        String flightDate = promptForValidDate(in);
        if (flightDate == null) {
            // User failed too many times
            System.out.println("Returning to main menu.");
            return;
        }

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

        int results = esql.executeQueryAndPrintResult(query);
        if (results == 0) {
            System.out.println("No flights found for those criteria.");
        }

    } catch(Exception e) {
        System.err.println(e.getMessage());
    }
}

public static void MakeReservation(AirlineManagement esql) {
    try {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

        String custId = promptForValidCustomerID(in);
        if (custId == null) return;

        String flightNum = promptForValidFlightNumber(in);
        if (flightNum == null) return;

        String flightDate = promptForValidDate(in);
        if (flightDate == null) return;

        // Find the FlightInstanceID and seat info
        String findInstance = "SELECT FlightInstanceID, SeatsTotal, SeatsSold FROM FlightInstance " +
                              "WHERE FlightNumber = '" + flightNum + "' " +
                              "AND FlightDate = DATE '" + flightDate + "'";
        List<List<String>> result = esql.executeQueryAndReturnResult(findInstance);

        if (result.isEmpty()) {
            System.out.println("No flight instance found.");
            return;
        }

        // // Check if flight date is in the past
        // java.sql.Date today = new java.sql.Date(System.currentTimeMillis());
        // java.sql.Date flightSqlDate = java.sql.Date.valueOf(flightDate);

        // if (flightSqlDate.before(today)) {
        //     System.out.println("Sorry, this flight has already flown. No further reservations or waitlist allowed.");
        //     return;
        // }

        // // Optionally, also check DepartedOnTime, if you use that as a flown marker:
        // // (Assuming result.get(0).get(3) is DepartedOnTime)
        // if (result.get(0).size() > 3 && result.get(0).get(3) != null && result.get(0).get(3).equals("t")) {
        //     System.out.println("Sorry, this flight has already departed. No further reservations or waitlist allowed.");
        //     return;
        // }

        String instanceId = result.get(0).get(0);
        int seatsTotal = Integer.parseInt(result.get(0).get(1));
        int seatsSold = Integer.parseInt(result.get(0).get(2));

        // New flown status check:
        String checkFlown = "SELECT 1 FROM Reservation WHERE FlightInstanceID = " + instanceId + " AND Status = 'flown' LIMIT 1";
        List<List<String>> flownCheck = esql.executeQueryAndReturnResult(checkFlown);
        if (flownCheck.size() > 0) {
            System.out.println("Sorry, this flight is already flown. No further reservations or waitlist allowed.");
            return;
        }

        String status = (seatsSold < seatsTotal) ? "reserved" : "waitlist";

       // Generate a new unique ReservationID (assumes format R0001, R0002, etc.)
        String getMaxId = "SELECT COALESCE(MAX(ReservationID), 'R0000') FROM Reservation";
        List<List<String>> maxIdResult = esql.executeQueryAndReturnResult(getMaxId);
        String maxId = maxIdResult.get(0).get(0);
        int newNum = Integer.parseInt(maxId.replaceAll("[^0-9]", "")) + 1;
        if (newNum > 9999) {
            System.out.println("Error: Maximum number of reservations reached (R9999). Cannot create new reservation.");
            return;
        }
        String newReservationId = String.format("R%04d", newNum);


        String insertRes = String.format(
            "INSERT INTO Reservation (ReservationID, CustomerID, FlightInstanceID, Status) " +
            "VALUES ('%s', %s, %s, '%s')",
            newReservationId, custId, instanceId, status
        );
        esql.executeUpdate(insertRes);

        System.out.println("Reservation ID: " + newReservationId +
            (status.equals("reserved") ? " - Confirmed!" : " - Added to waitlist!"));

    } catch(Exception e) {
        System.err.println(e.getMessage());
    }
}


public static void MaintenanceRequest(AirlineManagement esql) {
    try {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String pilotId = promptForValidPilotID(in);
        if (pilotId == null) return;

        String planeId = promptForValidPlaneID(in);
        if (planeId == null) return;

        String repairCode = promptForValidRepairCode(in);
        if (repairCode == null) return;

        String requestDate = promptForValidDate(in);
        if (requestDate == null) return;

        // Generate unique RequestID
        String getMaxId = "SELECT COALESCE(MAX(RequestID), 0) FROM MaintenanceRequest";
        List<List<String>> maxIdResult = esql.executeQueryAndReturnResult(getMaxId);
        int newRequestId = 1;
        if (maxIdResult.size() > 0) {
            newRequestId = Integer.parseInt(maxIdResult.get(0).get(0)) + 1;
        }

        String insertReq = String.format(
            "INSERT INTO MaintenanceRequest (RequestID, PlaneID, RepairCode, RequestDate, PilotID) " +
            "VALUES (%d, '%s', '%s', DATE '%s', '%s')",
            newRequestId, planeId, repairCode, requestDate, pilotId
        );
        esql.executeUpdate(insertReq);
        System.out.println("Maintenance request submitted! Request ID: " + newRequestId);

    } catch(Exception e) {
        System.err.println(e.getMessage());
    }
}



public static void ViewRepairs(AirlineManagement esql) {
    try {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String planeId = promptForValidPlaneID(in);
        if (planeId == null) {
            System.out.println("Returning to main menu.");
            return;
        }

        System.out.println("Enter start date of range:");
        String startDate = promptForValidDate(in);
        if (startDate == null) {
            System.out.println("Returning to main menu.");
            return;
        }

        System.out.println("Enter end date of range:");
        String endDate = promptForValidDate(in);
        if (endDate == null) {
            System.out.println("Returning to main menu.");
            return;
        }

        // Query all repairs for this plane in the given date range
        String query =
            "SELECT RepairDate, RepairCode, TechnicianID " +
            "FROM Repair " +
            "WHERE PlaneID = '" + planeId + "' " +
            "AND RepairDate BETWEEN DATE '" + startDate + "' AND DATE '" + endDate + "' " +
            "ORDER BY RepairDate";

        List<List<String>> result = esql.executeQueryAndReturnResult(query);
        if (result.size() == 0) {
            System.out.println("No repairs found for plane " + planeId + " between " + startDate + " and " + endDate + ".");
        } else {
            System.out.println("Repairs for " + planeId + " from " + startDate + " to " + endDate + ":");
            System.out.println("| RepairDate  | RepairCode | TechnicianID |");
            System.out.println("|-------------|------------|--------------|");
            for (List<String> row : result) {
                System.out.printf("| %-11s | %-10s | %-12s |\n", row.get(0), row.get(1), row.get(2));
            }
        }

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
  
public static String promptForValidDate(BufferedReader in) throws IOException {
    int maxTries = 5;
    for (int attempt = 1; attempt <= maxTries; attempt++) {
        System.out.print("Enter Date (yyyy-mm-dd) [example: 2025-05-05]: ");
        String input = in.readLine().trim();
        // Regex: 2025-05-05 or 2026-12-31 etc.
        if (input.matches("\\d{4}-\\d{2}-\\d{2}")) {
            String[] parts = input.split("-");
            int year = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]);
            int day = Integer.parseInt(parts[2]);
            if (year >= 2025 && year <= 2026) {
                if (month >= 1 && month <= 12) {
                    int[] daysInMonth = {31,28,31,30,31,30,31,31,30,31,30,31};
                    // leap year check for February
                    if (month == 2 && ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0)) {
                        daysInMonth[1] = 29;
                    }
                    if (day >= 1 && day <= daysInMonth[month-1]) {
                        return input; // Valid date!
                    }
                }
            }
        }
        System.out.println("Invalid date! Please use yyyy-mm-dd and a year between 2025 and 2026. Example: 2025-05-05");
        if (attempt == maxTries) {
            System.out.println("Too many invalid attempts. Logging out.");
            return null;
        }
    }
    return null; // Should never reach here.
}

  public static String promptForValidCity(BufferedReader in, String prompt) throws IOException {
    int maxTries = 5;
    for (int attempt = 1; attempt <= maxTries; attempt++) {
        System.out.print(prompt + " (letters and spaces only, e.g. 'New York'): ");
        String input = in.readLine().trim();
        if (input.matches("[A-Za-z ]{2,15}")) {
            // Optional: capitalize first letter of each word for consistency
            String[] words = input.split("\\s+");
            StringBuilder sb = new StringBuilder();
            for (String w : words) {
                if (!w.isEmpty()) {
                    sb.append(Character.toUpperCase(w.charAt(0)));
                    if (w.length() > 1) sb.append(w.substring(1).toLowerCase());
                    sb.append(" ");
                }
            }
            return sb.toString().trim();
        }
        System.out.println("Invalid city! Example: 'New York'. Please use only letters and spaces (2-50 chars).");
        if (attempt == maxTries) {
            System.out.println("Too many invalid attempts. Logging out.");
            return null;
        }
    }
    return null;
}

public static String promptForValidFirstName(BufferedReader in) throws IOException {
    int maxTries = 5;
    for (int attempt = 1; attempt <= maxTries; attempt++) {
        System.out.print("Customer First Name: ");
        String input = in.readLine().trim();
        if (input.matches("[A-Za-z]{2,15}")) {
            // Capitalize first letter
            return Character.toUpperCase(input.charAt(0)) + input.substring(1).toLowerCase();
        }
        System.out.println("Invalid first name! Example: 'Kevin'. Use only letters (2-30 characters).");
        if (attempt == maxTries) {
            System.out.println("Too many invalid attempts. Logging out.");
            return null;
        }
    }
    return null;
}

public static String promptForValidLastName(BufferedReader in) throws IOException {
    int maxTries = 5;
    for (int attempt = 1; attempt <= maxTries; attempt++) {
        System.out.print("Customer Last Name: ");
        String input = in.readLine().trim();
        if (input.matches("[A-Za-z]{2,30}")) {
            // Capitalize first letter
            return Character.toUpperCase(input.charAt(0)) + input.substring(1).toLowerCase();
        }
        System.out.println("Invalid last name! Example: 'Hall'. Use only letters (2-30 characters).");
        if (attempt == maxTries) {
            System.out.println("Too many invalid attempts. Logging out.");
            return null;
        }
    }
    return null;
}

public static String promptForValidFullName(BufferedReader in, String role) throws IOException {
    int maxTries = 5;
    for (int attempt = 1; attempt <= maxTries; attempt++) {
        System.out.print(role + " Full Name (e.g. 'Jessica Wang'): ");
        String input = in.readLine().trim();
        // Accepts two words, each 2-30 letters, separated by space
        if (input.matches("[A-Za-z]{2,30}\\s+[A-Za-z]{2,30}")) {
            String[] parts = input.split("\\s+");
            StringBuilder formatted = new StringBuilder();
            for (String name : parts) {
                formatted.append(Character.toUpperCase(name.charAt(0)));
                if (name.length() > 1) formatted.append(name.substring(1).toLowerCase());
                formatted.append(" ");
            }
            return formatted.toString().trim();
        }
        System.out.println("Invalid name! Please enter a first and last name, each 2–30 letters. Example: 'Gina Moore'");
        if (attempt == maxTries) {
            System.out.println("Too many invalid attempts. Logging out.");
            return null;
        }
    }
    return null;
}

public static String promptForValidCustomerID(BufferedReader in) throws IOException {
    int maxTries = 5;
    for (int attempt = 1; attempt <= maxTries; attempt++) {
        System.out.print("Customer ID (1-999): ");
        String input = in.readLine().trim();
        if (input.matches("\\d{1,3}")) {
            int id = Integer.parseInt(input);
            if (id >= 1 && id <= 999) {
                return String.valueOf(id);
            }
        }
        System.out.println("Invalid Customer ID! Enter a positive integer between 1 and 999 (e.g., 4).");
        if (attempt == maxTries) {
            System.out.println("Too many invalid attempts. Logging out.");
            return null;
        }
    }
    return null;
}

public static String promptForValidPilotID(BufferedReader in) throws IOException {
    int maxTries = 5;
    for (int attempt = 1; attempt <= maxTries; attempt++) {
        System.out.print("Pilot ID (P001–P999): ");
        String input = in.readLine().trim().toUpperCase();
        if (input.matches("P\\d{3}")) {
            int num = Integer.parseInt(input.substring(1));
            if (num >= 1 && num <= 999) {
                // Optionally pad with leading zeros
                return "P" + String.format("%03d", num);
            }
        }
        System.out.println("Invalid Pilot ID! Use format P001–P999, e.g., P010.");
        if (attempt == maxTries) {
            System.out.println("Too many invalid attempts. Logging out.");
            return null;
        }
    }
    return null;
}

public static String promptForValidTechnicianID(BufferedReader in) throws IOException {
    int maxTries = 5;
    for (int attempt = 1; attempt <= maxTries; attempt++) {
        System.out.print("Technician ID (T001–T999): ");
        String input = in.readLine().trim().toUpperCase();
        if (input.matches("T\\d{3}")) {
            int num = Integer.parseInt(input.substring(1));
            if (num >= 1 && num <= 999) {
                // Optionally pad with leading zeros
                return "T" + String.format("%03d", num);
            }
        }
        System.out.println("Invalid Technician ID! Use format T001–T999, e.g., T101.");
        if (attempt == maxTries) {
            System.out.println("Too many invalid attempts. Logging out.");
            return null;
        }
    }
    return null;
}

public static String promptForValidFlightNumber(BufferedReader in) throws IOException {
    int maxTries = 5;
    for (int attempt = 1; attempt <= maxTries; attempt++) {
        System.out.print("Flight Number (F100–F120): ");
        String input = in.readLine().trim().toUpperCase();
        if (input.matches("F\\d{3}")) {
            int num = Integer.parseInt(input.substring(1));
            if (num >= 100 && num <= 120) {
                return "F" + String.format("%03d", num);
            }
        }
        System.out.println("Invalid Flight Number! Use format F100–F120, e.g., F105.");
        if (attempt == maxTries) {
            System.out.println("Too many invalid attempts. Logging out.");
            return null;
        }
    }
    return null;
}

public static String promptForValidGender(BufferedReader in) throws IOException {
    int maxTries = 5;
    for (int attempt = 1; attempt <= maxTries; attempt++) {
        System.out.print("Gender (M/F/O): ");
        String input = in.readLine().trim().toUpperCase();
        if (input.equals("M") || input.equals("F") || input.equals("O")) {
            return input;
        }
        System.out.println("Invalid gender! Enter M, F, or O only.");
        if (attempt == maxTries) {
            System.out.println("Too many invalid attempts. Logging out.");
            return null;
        }
    }
    return null;
}

public static String promptForValidPhone(BufferedReader in) throws IOException {
    int maxTries = 5;
    for (int attempt = 1; attempt <= maxTries; attempt++) {
        System.out.print("Phone # (format: 123-456-7890): ");
        String input = in.readLine().trim();
        if (input.matches("\\d{3}-\\d{3}-\\d{4}")) {
            return input;
        }
        System.out.println("Invalid phone number! Use format: 123-456-7890.");
        if (attempt == maxTries) {
            System.out.println("Too many invalid attempts. Logging out.");
            return null;
        }
    }
    return null;
}

public static String promptForValidZip(BufferedReader in) throws IOException {
    int maxTries = 5;
    for (int attempt = 1; attempt <= maxTries; attempt++) {
        System.out.print("Zip Code (5 digits): ");
        String input = in.readLine().trim();
        if (input.matches("\\d{5}")) {
            return input;
        }
        System.out.println("Invalid zip code! Use exactly 5 digits, e.g., 92507.");
        if (attempt == maxTries) {
            System.out.println("Too many invalid attempts. Logging out.");
            return null;
        }
    }
    return null;
}

public static String promptForValidAddress(BufferedReader in) throws IOException {
    int maxTries = 5;
    for (int attempt = 1; attempt <= maxTries; attempt++) {
        System.out.print("Address: ");
        String input = in.readLine().trim();
        if (input.length() >= 5 && input.length() <= 100 &&
            input.matches("[A-Za-z0-9.,'\\- ]+")) {
            return input;
        }
        System.out.println("Invalid address! Use letters, numbers, comma, dot, dash, and spaces. 5–100 chars.");
        if (attempt == maxTries) {
            System.out.println("Too many invalid attempts. Logging out.");
            return null;
        }
    }
    return null;
}

public static String promptForValidPlaneID(BufferedReader in) throws IOException {
    int maxTries = 5;
    for (int attempt = 1; attempt <= maxTries; attempt++) {
        System.out.print("Plane ID (e.g., PL001): ");
        String input = in.readLine().trim().toUpperCase();
        if (input.matches("PL\\d{3}")) {
            return input;
        }
        System.out.println("Invalid Plane ID! Use format PLXXX, e.g., PL001.");
        if (attempt == maxTries) {
            System.out.println("Too many invalid attempts. Logging out.");
            return null;
        }
    }
    return null;
}

public static String promptForValidRepairCode(BufferedReader in) throws IOException {
    int maxTries = 5;
    for (int attempt = 1; attempt <= maxTries; attempt++) {
        System.out.print("Repair Code (RC001–RC999): ");
        String input = in.readLine().trim().toUpperCase();
        if (input.matches("RC\\d{3}")) {
            int num = Integer.parseInt(input.substring(2));
            if (num >= 1 && num <= 999) {
                return "RC" + String.format("%03d", num);
            }
        }
        System.out.println("Invalid Repair Code! Use format RCXXX, e.g., RC004.");
        if (attempt == maxTries) {
            System.out.println("Too many invalid attempts. Logging out.");
            return null;
        }
    }
    return null;
}




}//end AirlineManagement

