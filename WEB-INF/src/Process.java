
import java.io.*;

// Java Servlet Library Import
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


// MySQL Library
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

//Smartclient Library
import smartclient.DSRequest;
import smartclient.DSResponse;
import smartclient.RPCManager;

// JSON Library
import net.sf.json.JSON;
import net.sf.json.JSONObject;
import net.sf.json.JSONArray;

// Utils Lib
import utils.ResultSetConverter;
public class Process extends HttpServlet {
	 
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
	{	 		
		res.setContentType("application/json");
		res.setCharacterEncoding("UTF-8");
		PrintWriter out = res.getWriter();
		RPCManager rpc_manager = new RPCManager(req);
		JSONObject result;
		try {
			result = rpc_manager.processRequest();
			out.print(result.toString());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}	    
	    	 		
		out.close();		
	} 
}
