package smartclient;

import java.sql.Connection;
import java.sql.DriverManager;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import utils.HttpServletRequest2JSON;
import utils.JDBC_Connection;

public class RPCManager {		
	private JSONObject joRequest = null;	
	
	public JSONObject getRequest() {
		return joRequest;
	}

	public void setRequest(JSONObject joRequest) {
		this.joRequest = joRequest;
	}
	private HttpServletRequest hsrReq = null;
	
	public JSONObject getJoRequest() {
		return joRequest;
	}

	public void setJoRequest(JSONObject joRequest) {
		this.joRequest = joRequest;
	}

	public HttpServletRequest getHsrReq() {
		return hsrReq;
	}

	public void setHsrReq(HttpServletRequest hsrReq) {
		this.hsrReq = hsrReq;
	}

	public RPCManager(HttpServletRequest hsrRequest)
	{
		JSONObject joR2j =  HttpServletRequest2JSON.convert(hsrRequest);
				
		// if is not wrapped in a transaction then we'll wrap it to make unified handling of the request
		if ( checkTransaction(joR2j) == false )
		{
			JSONObject joTransaction = new JSONObject();
			joTransaction.put("transactionNum", "-1");
			ArrayList<JSONObject> alTemp = new ArrayList<JSONObject>();
			alTemp.add(joR2j);
			joTransaction.put("operations", alTemp);
			
			JSONObject joReq = new JSONObject();
			joReq.put("transaction", joTransaction);
			this.setRequest(joReq);
		} else
		{
			this.setRequest(joR2j);
		}
		
		this.setHsrReq(hsrRequest);		
	}
	/* 
	 * <summary>
     	Returns true if the request has transaction support
       </summary>
       <returns></returns>
     */
	
	public boolean checkTransaction(JSONObject jo)
	{	
		if ( jo.containsKey("transaction"))
			return true;
		else
			return false;
	}	
    /// <summary>
    /// Transforms a object object into a JSONObject. Will setup the serializer with the 
    /// appropriate converters, attributes,etc.
    /// </summary>
    /// <param name="dsresponse">the object object to be transformed to JSONObject</param>
    /// <returns>the created JSONObject object</returns>
	protected JSONObject buildResult(List<DSResponse> lResponse)
	{
		JSONArray jaResponse = new JSONArray();
		for(int i = 0; i < lResponse.size(); i++ )
		{
			DSResponse dsRes = lResponse.get(i);
			JSON json = JSONObject.fromObject(dsRes);
			jaResponse.add(json);
		}
		
		JSONObject joResult = new JSONObject();
		joResult.put("response", jaResponse);
		return joResult;
	}
	 
	/// <summary>
    /// Helper method to decide if request contains an advanced criteria or not
    /// </summary>
    /// <param name="req"></param>
    /// <returns></returns>
	protected boolean checkAdvancedCriteria(JSONObject data)	
	{
		if ( data.containsKey("_constructor") && data.containsKey("operator"))
			return true;
		else
			return false;	
	}
	protected JSONObject parseAdvancedCriterias(JSONObject operation)
	{
		JSONObject data = operation.getJSONObject("data");
		if( this.checkAdvancedCriteria(data))
			return data;
		
		return null;
	}
	/// <summary>
    /// Process the transaction request for which this RPCManager was created for
    /// </summary>
    /// <returns></returns>
	public JSONObject processRequest() throws ClassNotFoundException, SQLException
	{		
		// retrieve the requests with data in form
		JSONObject transaction = this.joRequest.getJSONObject("transaction");
		JSONArray jaOperations = transaction.getJSONArray("operations");
		
		// store transaction num, we'll use it later to see if there was a transaction or not
		int transaction_num = transaction.getInt("transactionNum");
	
		boolean queueFailed = false;
		JSONArray res_list = new JSONArray();
		
		// connecting to the mysql database
		Connection con = null;		
		Class.forName(JDBC_Connection.DRIVER_CLASS);
		
		try
		{
			con = DriverManager.getConnection(JDBC_Connection.URL,JDBC_Connection.USER, JDBC_Connection.PASSWORD);
			con.setAutoCommit(false);
			
			for( int i = 0; i < jaOperations.size(); i++ )
			{
				JSONObject op = jaOperations.getJSONObject(i);//operations.getJSONObject(i);
				
				// parse advanced criterias, if any
				JSONObject advancedCriteria =  this.parseAdvancedCriterias(op);
				
				DSRequest ds_req = new DSRequest( op, hsrReq );
				// add this criteria to the request list
				if(advancedCriteria != null)
				{
					ds_req.setAdvancedCriteria(advancedCriteria);
				}
				
				//execute the request and get the response
				DSResponse ds_res = ds_req.execute();
				// safeguard, if was null, create an empty one with failed status
				if(ds_res == null )					 
				{
					ds_res = new DSResponse();
					ds_res.setStatus(-1);
				}
				
				// if request execution failed, mark the flag variable
				if(ds_res.getStatus() == -1)
				{
					queueFailed = true;
				}
				// store the response for later 
				res_list.add(ds_res);
			}			
			// if there were no errors, commit the transaction
			if (!queueFailed)		
				con.commit();
		} catch(SQLException e )
		{
			 if (con != null) {
		        con.rollback();		        
		      }
		      e.printStackTrace();
		}finally {
	      if (con != null && !con.isClosed()) {
	          con.close();
	        }
	    }
		ArrayList<DSResponse> list = new ArrayList<DSResponse>();
		// if we have only one object, send directly the DSResponse 
		if ( transaction_num == -1 )
		{
			JSONObject first_res = res_list.getJSONObject(0);			
			DSResponse ds_res = new DSResponse(); 
			
		    ds_res.setData( first_res.getJSONArray("data"));
		    ds_res.setStartRow(first_res.getInt("startRow"));
		    ds_res.setEndRow( first_res.getInt("endRow"));
		    ds_res.setTotalRows( first_res.getInt("totalRows"));
		    ds_res.setStatus(0); 
		    
		    list.add(ds_res);
		    return this.buildResult(list);
		}
		
		// iterate over the responses and create a instance of an anonymous class
        // which mimics the required json
		for( int i = 0; i < res_list.size(); i++ )
		{
			JSONObject res_json = res_list.getJSONObject(i);						
			DSResponse ds_res = new DSResponse(); 
			
		    ds_res.setData( res_json.getJSONArray("data"));
		    ds_res.setStartRow(res_json.getInt("startRow"));
		    ds_res.setEndRow( res_json.getInt("endRow"));
		    ds_res.setTotalRows( res_json.getInt("totalRows"));
		    ds_res.setStatus(0);
		    list.add(ds_res);
		}
		return this.buildResult(list);
	} 
}
