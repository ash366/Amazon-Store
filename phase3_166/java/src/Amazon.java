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

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;  

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */
public class Amazon {
   private static String loggedInUserName;
   private static String loggedInUserPassword;
   // reference to physical database connection.
   private Connection _connection = null;

   // handling the keyboard inputs through a BufferedReader
   // This variable can be global for convenience.
   static BufferedReader in = new BufferedReader(
                                new InputStreamReader(System.in));

   /**
    * Creates a new instance of Amazon store
    *
    * @param hostname the MySQL or PostgreSQL server hostname
    * @param database the name of the database
    * @param username the user name used to login to the database
    * @param password the user login password
    * @throws java.sql.SQLException when failed to make a connection.
    */
   public Amazon(String dbname, String dbport, String user, String passwd) throws SQLException {

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
   }//end Amazon

   // Method to calculate euclidean distance between two latitude, longitude pairs. 
   public double calculateDistance (double lat1, double long1, double lat2, double long2){
      double t1 = (lat1 - lat2) * (lat1 - lat2);
      double t2 = (long1 - long2) * (long1 - long2);
      return Math.sqrt(t1 + t2); 
   }
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
      stmt.close ();
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
   public static void main (String[] args) {
      if (args.length != 3) {
         System.err.println (
            "Usage: " +
            "java [-classpath <classpath>] " +
            Amazon.class.getName () +
            " <dbname> <port> <user>");
         return;
      }//end if

      Greeting();
      Amazon esql = null;
      try{
         // use postgres JDBC driver.
         Class.forName ("org.postgresql.Driver").newInstance ();
         // instantiate the Amazon object and creates a physical
         // connection.
         String dbname = args[0];
         String dbport = args[1];
         String user = args[2];
         esql = new Amazon (dbname, dbport, user, "");

         boolean keepon = true;
         while(keepon) {
            // These are sample SQL statements
            System.out.println("MAIN MENU");
            System.out.println("---------");
            System.out.println("1. Create user");
            System.out.println("2. Log in");
            System.out.println("9. < EXIT");
            String authorisedUser = null;
            switch (readChoice()){
               case 1: CreateUser(esql); break;

               //LogIn() returns 1
               case 2: authorisedUser = LogIn(esql); break;
               case 9: keepon = false; break;
               default : System.out.println("Unrecognized choice!"); break;
            }//end switch
            if (authorisedUser != null) {
              boolean usermenu = true;
              while(usermenu) {

               
                System.out.println("MAIN MENU");
                System.out.println("---------");
                System.out.println("1. View Stores within 30 miles");
                System.out.println("2. View Product List");
                System.out.println("3. Place a Order");
                System.out.println("4. View 5 recent orders");

   //IF ADMIN OR MANAGER
               if (esql.checkPerms(esql)==1){
                //the following functionalities basically used by managers
                  System.out.println("5. Update Product");
                  System.out.println("6. View 5 recent Product Updates Info");
                  System.out.println("7. View 5 Popular Items");
                  System.out.println("8. View 5 Popular Customers");
                  System.out.println("9. Place Product Supply Request to Warehouse");
               }
                System.out.println(".........................");
                System.out.println("20. Log out");
                switch (readChoice()){
                   case 1: viewStores(esql); break;
                   case 2: viewProducts(esql); break;
                   case 3: placeOrder(esql); break;
                   case 4: viewRecentOrders(esql); break;                   
                   case 5: updateProduct(esql); break;
                   case 6: viewRecentUpdates(esql); break;
                   case 7: viewPopularProducts(esql); break;
                   case 8: viewPopularCustomers(esql); break;
                   case 9: placeProductSupplyRequests(esql); break;

                   case 20: usermenu = false; break;
                   default : System.out.println("Unrecognized choice!"); break;
                }
              }
            }
         }//end while
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

   /*
    * Reads the users choice given from the keyboard
    * @int
    **/
   public static int readChoice() {
      int input;
      // returns only if a correct value is given.
      do {
         System.out.print("Please make your choice: ");
         try { // read the integer, parse it and break.
            input = Integer.parseInt(in.readLine());
            break;
         }catch (Exception e) {
            System.out.println("Your input is invalid!");
            continue;
         }//end try
      }while (true);
      return input;
   }//end readChoice

   /*
    * Creates a new user
    **/


//
//HELPER FUNCTION
//Grab count of rows from ANY table.
public static int GetNextUniqueNumber(Amazon esql, String table){
   try{
      String query = String.format("SELECT COUNT(*) FROM "+ table);
       List <List<String>> count= esql.executeQueryAndReturnResult(query);
      int next_count = Integer.parseInt(count.get(0).get(0))+1;
      return next_count;
   }
   catch(Exception e){
         System.err.println (e.getMessage ());
         return -1;
      }
}

//is manager or admin?
public static int checkPerms(Amazon esql){
try{
        String queryManagerCheck = String.format("SELECT * FROM Users WHERE name = '%s' AND password = '%s' AND (type = 'manager' OR type= 'admin')", loggedInUserName, loggedInUserPassword);
        int rowCountManagerCheck = esql.executeQuery(queryManagerCheck);

        return rowCountManagerCheck;

}
//helper
   catch(Exception e){
         System.err.println (e.getMessage ());
         return -1;
      }
   }

public static int isAdmin(Amazon esql){
try{
        String queryManagerCheck = String.format("SELECT * FROM Users WHERE name = '%s' AND password = '%s' AND (type ='admin')", loggedInUserName, loggedInUserPassword);
        int rowCountManagerCheck = esql.executeQuery(queryManagerCheck);

        return rowCountManagerCheck;

}
   catch(Exception e){
         System.err.println (e.getMessage ());
         return -1;
      }

}

public static int isManager(Amazon esql){
try{
        String queryManagerCheck = String.format("SELECT * FROM Users WHERE name = '%s' AND password = '%s' AND (type ='manager')", loggedInUserName, loggedInUserPassword);
        int rowCountManagerCheck = esql.executeQuery(queryManagerCheck);

        return rowCountManagerCheck;

}

//helper
   catch(Exception e){
         System.err.println (e.getMessage ());
         return -1;
      }
   }

public static String getUserType(Amazon esql){
   try{
         String type_query = String.format("SELECT u.type FROM Users u WHERE u.name = '%s' AND u.password = '%s'", loggedInUserName, loggedInUserPassword);
         List<List<String>> u_types = esql.executeQueryAndReturnResult(type_query);
         String u_type = u_types.get(0).get(0);
         return u_type;
   }
   catch(Exception e){
         System.err.println (e.getMessage ());
         return null;
      }
   }
   //helper
public static int getUserID(Amazon esql){
   try{
         String id_query = String.format("SELECT u.userID FROM Users u WHERE u.name = '%s' AND u.password = '%s'", loggedInUserName, loggedInUserPassword);
         List<List<String>> u_ids = esql.executeQueryAndReturnResult(id_query);
         int u_id = Integer.parseInt(u_ids.get(0).get(0));
         return u_id;
   }
   catch(Exception e){
         System.err.println (e.getMessage ());
         return -1;
      }
   }
//helper function 
   public static void CreateUser(Amazon esql){
      try{
         System.out.print("\tEnter name: ");
         String name = in.readLine();
         System.out.print("\tEnter password: ");
         String password = in.readLine();
         System.out.print("\tEnter latitude: ");   
         String latitude = in.readLine();       //enter lat value between [0.0, 100.0]
         System.out.print("\tEnter longitude: ");  //enter long value between [0.0, 100.0]
         String longitude = in.readLine();
         
         String type="Customer";

         //int userID = esql.CreateNewUserID(esql);
         

			String query = String.format("INSERT INTO USERS (name, password, latitude, longitude, type) VALUES ('%s','%s', %s, %s,'%s')", name, password, latitude, longitude, type);

         esql.executeUpdate(query);
         System.out.println ("User successfully created!");

         //CREATE ID
      }catch(Exception e){
         System.err.println (e.getMessage ());
      }
   }//end CreateUser


   /*
    * Check log in credentials for an existing user
    * @return User login or null is the user does not exist
    **/
   public static String LogIn(Amazon esql){
      try{
         System.out.print("\tEnter name: ");
         String name = in.readLine();
         System.out.print("\tEnter password: ");
         String password = in.readLine();

         String query = String.format("SELECT * FROM USERS WHERE name = '%s' AND password = '%s'", name, password);
         int userNum = esql.executeQuery(query);
         loggedInUserName=name;
         loggedInUserPassword= password;
	 if (userNum > 0)
		return name;
         return null;
       
      }
      catch(Exception e){
         System.err.println (e.getMessage ());
         return null;
      }
   }//end

// Rest of the functions definition go in here

   public static void viewStores(Amazon esql) {
      try {
         String query1 = String.format("SELECT latitude FROM USERS WHERE name = '%s' AND password = '%s'", loggedInUserName, loggedInUserPassword);
         List<List<String>> lats = esql.executeQueryAndReturnResult(query1);
         double userLatitude = Double.valueOf(lats.get(0).get(0));

         String query2 = String.format("SELECT longitude FROM USERS WHERE name = '%s' AND password = '%s'", loggedInUserName, loggedInUserPassword);
         List<List<String>>  longs = esql.executeQueryAndReturnResult(query2);
         double userLongitude = Double.valueOf(longs.get(0).get(0));


         String query = String.format("SELECT storeID, latitude, longitude FROM Store WHERE " +"(SQRT(POW(latitude - '%s', 2) + POW(longitude - '%s', 2)) < 30)", userLatitude, userLongitude);
         int rowCount = esql.executeQueryAndPrintResult(query);

    } catch (Exception e) {
        System.err.println(e.getMessage());
    }
   }


   public static void viewProducts(Amazon esql) {
   try{
         System.out.print("\tEnter StoreID: ");
         String storeID = in.readLine();
			String query = String.format("SELECT p.productName, p.numberOFUnits, p.pricePerUnit  FROM PRODUCT p, Store s WHERE p.storeID = s.storeID AND s.storeID = '%s'", storeID);
         //String query = "SELECT * FROM Store";
         //esql.executeUpdate(query);
         esql.executeQueryAndPrintResult(query);
         //System.out.print(row);
      }catch(Exception e){
         System.err.println (e.getMessage ());
      }
   }

   //helper function
   public static String getTime(){
      String datetime = (ZonedDateTime.now().format(DateTimeFormatter.RFC_1123_DATE_TIME));
      return datetime;
   }

   public static void placeOrder(Amazon esql) {
         try{
            //FIRST CHECK if store is within 30 miles.
         System.out.print("\tEnter StoreID: ");
         String storeID = in.readLine();
         System.out.print("\tEnter Product Name: ");
         String productName = in.readLine();
         System.out.print("\tEnter Number of Units: ");
         String numberOfUnits = in.readLine();

         //get store longitude
         String query1 = String.format("SELECT longitude FROM Store Where storeID = '%s' ", storeID);
         List<List<String>> s_longitudes = esql.executeQueryAndReturnResult(query1); 
         double store_longitude = Double.valueOf(s_longitudes.get(0).get(0));

      //store latitude
         String query2 = String.format("SELECT latitude FROM Store Where storeID = '%s'", storeID);
         esql.executeQuery(query2);

         List<List<String>> s_latitudes = esql.executeQueryAndReturnResult(query2); 
         double store_latitude = Double.valueOf(s_latitudes.get(0).get(0));

         //user longitude
         String query3 = String.format("SELECT longitude FROM Users Where name = '%s' AND password = '%s'", loggedInUserName, loggedInUserPassword);
         esql.executeQuery(query3); 
         List<List<String>> u_longitudes = esql.executeQueryAndReturnResult(query3); 
         double user_longitude = Double.valueOf(u_longitudes.get(0).get(0));

         //user latitude
         String query4 = String.format("SELECT latitude FROM Users Where name = '%s' AND password = '%s'", loggedInUserName, loggedInUserPassword);
         List<List<String>> u_latitudes = esql.executeQueryAndReturnResult(query4); 
         double user_latitude = Double.valueOf(u_latitudes.get(0).get(0));


         double distance = esql.calculateDistance(user_latitude, user_longitude, store_latitude, store_longitude); 
   
         //System.out.println(new_order_number);

         if (distance > 30){
            System.out.println("That store is too far from you! (Must be within 30 miles from your location.)");
            return;
         }

         int new_order_number = esql.GetNextUniqueNumber(esql, "Orders");
         
         //query the selected prpoduct.
         String product_from_store = String.format("SELECT * FROM Product p WHERE p.storeID = '%s' AND p.productName ='%s' and P.numberOfUnits >= '%s'", storeID, productName, numberOfUnits);
         List<List<String>> product = esql.executeQueryAndReturnResult(product_from_store);
         
         if (product.isEmpty()){
            System.out.println("Product doesn't exist or you ordered too many.");
            return;
         } 

         //PRODUCT EXISTS AND YOU CAN ORDER.
         //if (distance<=30){
         //CREATE ORDER.
         int u_id = esql.getUserID(esql);
         String datetime = getTime();
         String query5 = String.format("INSERT INTO Orders (orderNumber, customerID, storeID, productName, unitsOrdered, orderTime) VALUES ('%s', '%s', '%s', '%s', '%s', '%s')", new_order_number, u_id, storeID, productName, numberOfUnits, datetime);
         esql.executeUpdate(query5);

         //now edit chosen store's product and decrase by numberofunits.
         String updatequery = String.format("UPDATE Product SET numberOfUnits = numberOfUnits - '%s' WHERE storeID = '%s' AND productName ='%s' and numberOfUnits >= '%s'", numberOfUnits,storeID, productName, numberOfUnits);
         esql.executeUpdate(updatequery);

         System.out.println("Product ordered!");
         
      } catch(Exception e){
         System.err.println (e.getMessage ());
      }
   }

//users can use this
   public static void viewRecentOrders(Amazon esql) {
      try{


         
         int u_id = esql.getUserID(esql);
         //System.out.println(u_id);

         if (esql.isManager(esql) == 1 ) {

            String query = String.format("SELECT o.orderNumber, u.name AS customerName, o.storeID, o.productName, o.orderTime " +
                             "FROM Orders o " +
                             "JOIN Users u ON o.customerID = u.userID " +
                             "JOIN Store s ON o.storeID = s.storeID " +
                             "WHERE s.managerID = '%s' " +
                             "ORDER BY o.orderTime DESC " +
                             "LIMIT 5", u_id);

            esql.executeQueryAndPrintResult(query);
         }
         else if (esql.isAdmin(esql) ==1){
            String query = String.format("SELECT * FROM Orders o ORDER BY orderNumber DESC LIMIT 5", u_id);
            esql.executeQueryAndPrintResult(query);
      }
         else{
            String query = String.format("SELECT * FROM Orders o WHERE o.customerID = '%s' ORDER BY orderNumber DESC LIMIT 5", u_id);
            esql.executeQueryAndPrintResult(query);
         }
         //view 5 recent orders.
         
      }
      catch(Exception e){
         System.err.println(e.getMessage());
      }
   }

   

   

      //manager or admin
   public static void updateProduct(Amazon esql) {
      try{
         //CHECK IF MANAGER
      
       if (esql.checkPerms(esql) <1){
            System.out.println("User does not have permissions. Access denied.");
            return;

       }

         System.out.print("\tEnter StoreID: ");
         String storeID = in.readLine();

         //grab id
         int user_id = esql.getUserID(esql);

         String user_type = esql.getUserType(esql);
         //verify if its owned by manager.
         String storeCheck = String.format("SELECT s.storeID FROM Store s WHERE s.storeID = '%s' AND s.managerID = '%s'", storeID, user_id);
         int store_manager_check = esql.checkPerms(esql);
         
         
         //if manager is not assigned to store AND IS NOT ADMIN. if admin, skip.
         int user_perms =esql.isAdmin(esql);

         //if NOT admin AND not assigned to store
         if (user_perms < 1 && store_manager_check < 1){
            System.out.println("You are not a verified manager for this store.");
            return;
         }
         
         System.out.print("\tEnter Product Name: ");
         String productName = in.readLine();

         String productquery = String.format("SELECT * FROM product p WHERE p.storeID = '%s'", storeID);
         if (esql.executeQuery(productquery) < 1){
            System.out.println("This product is not available at this location.");
            return;
         }

         System.out.println("\tUpdate price? y/n: ");
         String response1 =in.readLine();
         

         if (response1.equals("y")){
            System.out.print("\tAssign new price: ");
            String newPrice = in.readLine();

            String updatePrice = String.format("UPDATE Product SET pricePerUnit = '%s' WHERE storeID = '%s' AND productName = '%s'", newPrice, storeID, productName);
            esql.executeUpdate(updatePrice);
         }
         
         System.out.print("\tUpdate stock? y/n: ");
         String response2 = in.readLine();

         if (response2.equals("y")){
            System.out.print("\tAssign new numberofUnits:  ");
            String newUnits = in.readLine();

            String updateUnits = String.format("UPDATE Product SET numberOfUnits = '%s' WHERE storeID = '%s' AND productName = '%s'", newUnits, storeID, productName);
            esql.executeUpdate(updateUnits);

         }

         int updateNumber = esql.GetNextUniqueNumber(esql, "ProductUpdates");
         if (response1.equals("y") || response2.equals("y")){
            String timestamp = esql.getTime();

            String productUpdatequery = String.format("INSERT INTO ProductUpdates (updateNumber, managerID, storeID, productName, updatedOn) VALUES ('%s','%s','%s','%s', '%s')", updateNumber, user_id, storeID, productName, timestamp);
             esql.executeUpdate(productUpdatequery);

         }
         //INSERT ProductUpdate log
      }
      catch(Exception e){
         System.err.println(e.getMessage());
      }
   }
   public static void viewRecentUpdates(Amazon esql) {
         try{
         //grab ID
         //String debug  = String.format("SELECT * FROM USERS LIMIT 5");
         //esql.executeQueryAndPrintResult(debug);
         int u_id = esql.getUserID(esql);
      
         int store_manager_check = esql.checkPerms(esql);
         
         //if manager is not assigned to store AND IS NOT ADMIN. if admin, skip.
         int user_perms =esql.isAdmin(esql);

         
         //if NOT admin AND not assigned to store
         if (user_perms < 1 && store_manager_check < 1){
            System.out.println("You are not a verified manager for this store.");
            return;
         }

         String query;
         //
         if (user_perms < 1){

         query = String.format("SELECT * FROM ProductUpdates pu WHERE pu.managerID = '%s' ORDER BY updateNumber DESC LIMIT 5", u_id);

         }
         else{
            query = String.format("SELECT * FROM ProductUpdates pu ORDER BY updateNumber DESC LIMIT 5", u_id);
         }
         esql.executeQueryAndPrintResult(query);

         
      }
      catch(Exception e){
         System.err.println(e.getMessage());
      }
   }

public static void viewPopularProducts(Amazon esql) {
    try {
      /*
        String queryManagerCheck = String.format("SELECT * FROM Users WHERE name = '%s' AND password = '%s' AND type = 'manager'", loggedInUserName, loggedInUserPassword);
        int rowCountManagerCheck = esql.executeQuery(queryManagerCheck);

      */
     //CHECK PERMS (is manager? is admin?)
        if (esql.checkPerms(esql) < 1) {
            System.out.println("User is not a manager. Access denied.");
            return;
        }
        
       

         int manager_id = esql.getUserID(esql);

         int user_perms =esql.isAdmin(esql);

         String query;
         if (user_perms < 1){
            query = String.format(
            "SELECT o.productname, COUNT(o.unitsOrdered) as orderCount " +
            "FROM orders o " +
            "WHERE o.storeid IN "+
            "(SELECT s.storeid FROM store s WHERE s.managerid = '%s') GROUP BY o.productname ORDER BY orderCount DESC LIMIT 5"
            , manager_id

         );

         }
         else{
            query = String.format(
            "SELECT o.productname, COUNT(o.unitsOrdered) as orderCount " +
            "FROM orders o " +
            "GROUP BY o.productname ORDER BY orderCount DESC LIMIT 5"
            , manager_id

         );

         }
         
         esql.executeQueryAndPrintResult(query);

         //select o.productname, count() from orders o group by o.productname
         

    } catch (Exception e) {
        System.err.println(e.getMessage());
    }
}


public static void viewPopularCustomers(Amazon esql) {
    try {
        // Check if the user is a manager
        /*
        String queryManagerCheck = String.format("SELECT * FROM Users WHERE name = '%s' AND password = '%s' AND type = 'manager'", loggedInUserName, loggedInUserPassword);
        int rowCountManagerCheck = esql.executeQuery(queryManagerCheck);

         */
        if (esql.checkPerms(esql) < 1) {
            System.out.println("User is not a manager. Access denied.");
            return;
        }

        int user_perms =esql.isAdmin(esql);

        // Retrieve information about popular customers

        //NOT ADMIN
         String queryPopularCustomers;
        if (user_perms < 1){
        queryPopularCustomers = String.format("SELECT u.name AS customerName, COUNT(*) AS orderCount " +
                "FROM Users u " +
                "JOIN Orders o ON u.userID = o.customerID " +
                "JOIN Store s ON o.storeID = s.storeID " +
                "WHERE s.managerID = (SELECT userID FROM Users WHERE name = '%s' AND password = '%s' AND type = 'manager') " +
                "GROUP BY u.name " +
                "ORDER BY orderCount DESC " +
                "LIMIT 5;", loggedInUserName, loggedInUserPassword);

        }

        else{ //IS ADMIN
        queryPopularCustomers = String.format("SELECT u.name AS customerName, COUNT(*) AS orderCount " +
                "FROM Users u " +
                "JOIN Orders o ON u.userID = o.customerID " +
                "JOIN Store s ON o.storeID = s.storeID " +
                "GROUP BY u.name " +
                "ORDER BY orderCount DESC " +
                "LIMIT 5;", loggedInUserName, loggedInUserPassword);


         
        }
        int rowCountPopularCustomers = esql.executeQueryAndPrintResult(queryPopularCustomers);


        if (rowCountPopularCustomers < 1) {
            System.out.println("No popular customers found.");
        }

    } catch (Exception e) {
        System.err.println(e.getMessage());
    }
}


   public static void placeProductSupplyRequests(Amazon esql) {
      try{
         /*
         String queryManagerCheck = String.format("SELECT * FROM Users WHERE name = '%s' AND password = '%s' AND type = 'manager'", loggedInUserName, loggedInUserPassword);
        int rowCountManagerCheck = esql.executeQuery(queryManagerCheck);

      */
        if (esql.checkPerms(esql) < 1) {
            System.out.println("User is not a manager. Access denied.");
            return;
        }

      //storeID, productName, number of units needed, and warehouseID
         System.out.print("\tEnter StoreID: ");
         String storeID = in.readLine();
         
         int user_id = esql.getUserID(esql);

      
         //check if storeid and manager is correct.
         String storeCheck = String.format("SELECT s.storeID FROM Store s WHERE s.storeID = '%s' AND s.managerID = '%s'", storeID, user_id);
         int store_manager_check = esql.executeQuery(storeCheck);
         int user_perms =esql.isAdmin(esql);

         //if NOT admin AND not assigned to store
         if (user_perms < 1 && store_manager_check < 1){
            System.out.println("You are not a verified manager for this store.");
            return;
         }
         
      
         //

         System.out.print("\tEnter Product Name: ");
         String productName = in.readLine();

         System.out.print("\tEnter warehouse ID: ");
         String warehouseID = in.readLine();

         System.out.print("\tRequest how many units?: ");
         String unitsRequested = in.readLine();

         String productUpdate = String.format("UPDATE Product SET numberOfUnits = numberOfUnits + '%s' WHERE storeID = '%s' AND productName = '%s'", unitsRequested,storeID, productName);
         esql.executeUpdate(productUpdate);
         
      
         int requestnumber = GetNextUniqueNumber(esql, "ProductSupplyRequests");
         int manager_id = esql.getUserID(esql);
         String requestUpdate = String.format("INSERT INTO ProductSupplyRequests (requestNumber, managerID, warehouseID, storeID, productName, unitsRequested) VALUES ('%s','%s','%s','%s','%s', '%s')", requestnumber, manager_id, warehouseID, storeID, productName, unitsRequested );
         esql.executeUpdate(requestUpdate);
      }

      catch(Exception e){
         System.err.println (e.getMessage ());
      }
   
   }


}//end Amazon

