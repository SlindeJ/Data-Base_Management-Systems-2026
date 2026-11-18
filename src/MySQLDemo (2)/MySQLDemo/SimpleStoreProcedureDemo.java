import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.CallableStatement;
import java.sql.Types;

public class SimpleStoreProcedureDemo {

	static final String databasePrefix ="cs366-2267_slindejs08";                // need to change info to my database info (ID, pass, DBname)
	static final String netID ="slindejs08"; // Please enter your netId    need to use eduRoam too
	static final String hostName ="washington.uww.edu"; //140.146.23.39 or washington.uww.edu
	static final String databaseURL ="jdbc:mariadb://"+hostName+"/"+databasePrefix; // need to use this in mySQL
	static final String password="js6398"; // please enter your own password
     		    
	private Connection connection = null;
	private Statement statement = null;
	private ResultSet resultSet = null;

	public void Connection(){

		try {
			Class.forName("org.mariadb.jdbc.Driver");
		    System.out.println("databaseURL"+ databaseURL);
			connection = DriverManager.getConnection(databaseURL, netID, password);
			System.out.println("Successfully connected to the database");
		}
		catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
	} // end of Connection

	public void simpleStoreProcedure(String spName) {


	try {
		statement = connection.createStatement();
		int total =0;		// set @total =0; in putty SQL
		CallableStatement myCallStmt = connection.prepareCall("{call "+spName+"(?)}");	// ? is the parameter, if you have multiple, you have to put multiple ? (?, ?,...)
        myCallStmt.registerOutParameter(1,Types.BIGINT);	// change data type to what you're calling
        myCallStmt.execute();
        total = myCallStmt.getInt(1);	//.get[Data type]
        System.out.println("The total student ="+ total);

	}
	catch (SQLException e) {
		e.printStackTrace();
	}
} // end of simpleQuery method

/*
 					// store procedure she wants us to use
delimiter $$
drop procedure if exists getTotalStudent;
create procedure getTotalStudent(INOUT total int) 
begin 
      select count(*) into total
      from Student;
end $$
delimiter ; 

*/
	
	
public static void main(String args[]) {

	SimpleStoreProcedureDemo demoObj = new SimpleStoreProcedureDemo();
	demoObj.Connection();
	String spName ="getTotalStudent";
	demoObj.simpleStoreProcedure(spName);
}



}

