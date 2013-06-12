package smartclient;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;

import utils.ResultSetConverter;
import utils.JDBC_Connection;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class DataSource {
	
	private JSONObject joDataSource = null;
	private HttpServletRequest hsr;
	

	public DataSource(String strDataSource, HttpServletRequest hsr)
	{
		this.hsr = hsr;
		joDataSource = this.getDataSource(strDataSource);		
	}
	
	private String getPrimaryKey()
	{
		JSONArray jaFields = joDataSource.getJSONArray("fields");
		String strPK = "";
		for( int i = 0; i < jaFields.size(); i++ )
		{
			JSONObject joField = jaFields.getJSONObject(i);
			
			if ( joField.containsKey("primaryKey") == true )
			{				
				strPK = joField.getString("name");
			}				 
		}
		
		return strPK;
	}
	
	/// <summary>
    /// Load a DataSource specified by it's ID
    /// </summary>
    /// <param name="datasourceId">the ID of the DataSource to be loaded</param>
    /// <returns>the DataSource object corresponding to the DataSource with the specified ID or null if not found</returns>
	private JSONObject getDataSource(String strDSName)
	{
		BufferedReader br = null;
		JSONObject jsDataSource = new JSONObject();
		try
		{
			String strCurrentLine = "";
			String strDSTemp = "";
						
			String strFileName =  hsr. getServletContext().getRealPath("") + "\\resource\\ds\\" + strDSName.toString() + ".js";
			
			br = new BufferedReader(new FileReader(strFileName));
			while ((strCurrentLine = br.readLine()) != null) {
				strDSTemp += strCurrentLine;
			}

			// replace process
			String strDS = strDSTemp.replace("isc.RestDataSource.create(","");
			strDS = strDS.replace(");","");
			strDS = strDS.replaceAll("\r|\n|\t", "");
			
			jsDataSource = JSONObject.fromObject(strDS.toString());
			
		}catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		// get data source from the file
		return jsDataSource;
	}
	public DSResponse execute(DSRequest dsRequest) throws SQLException
	{		
		String strOperationType = dsRequest.getOperationType();
		if ( strOperationType.equals("fetch") )
			return this.fetch(dsRequest);
		else if ( strOperationType.equals("add") )
			return this.add(dsRequest);
		else if ( strOperationType.equals("remove") )
			return this.remove(dsRequest);
		else if ( strOperationType.equals("update") )
			return this.update(dsRequest);
		
		return null;
	}
	
	protected DSResponse update( DSRequest dsRequest ) throws SQLException
	{
		JSONObject joData = dsRequest.getData();
		JSONObject joOldData = dsRequest.getOldValues();
		
		if ( dsRequest.checktDataKeys() == true )
		{
			Iterator<?> iKeys = joData.keys();
			while( iKeys.hasNext() ){
	            String strKey = (String)iKeys.next();	            
	            String strValue = joData.getString(strKey);
	            
	        	joOldData.put(strKey, strValue);
	        }
		}
		// get the primary key
		String strPK = this.getPrimaryKey();
		String strTblName = joDataSource.getString("ID");
		int nValue = joData.getInt(strPK);
		
		Iterator<?> iKeys = joOldData.keys();
		String strSetCondition = "";
		while( iKeys.hasNext() ){
            String strKey = (String)iKeys.next();	            
            String strValue = joOldData.getString(strKey);
            if ( strValue == "true" )
            	strValue = "1";
            else if ( strValue == "false")
            	strValue = "0";
            
            strSetCondition += strKey + " = '" + strValue + "',";        	
        }
		
		strSetCondition = strSetCondition.substring(0, strSetCondition.length()-1);
		String strQuery = "UPDATE " + strTblName + " SET " + strSetCondition + " WHERE " + strPK + " = " + String.valueOf(nValue);
		Connection con = DriverManager.getConnection(JDBC_Connection.URL,JDBC_Connection.USER, JDBC_Connection.PASSWORD);
		Statement stmt = null;
		stmt = con.createStatement();
		stmt.executeUpdate(strQuery);
		con.close();
		stmt.close();
		DSResponse dsResponse = new DSResponse();
		dsResponse.setStatus(0);
		return dsResponse;
	}
	
	protected DSResponse remove(DSRequest dsRequest) throws SQLException
	{
		// get the table name from data source		
		String strTblName = joDataSource.getString("ID");		
		
		// get primary key
		String strPK = this.getPrimaryKey();
		JSONObject joData = dsRequest.getData();
		int nValue = joData.getInt(strPK);
		
		String strQuery = "DELETE FROM " + strTblName + " WHERE " + strPK + " = " + String.valueOf(nValue);
		Connection con = DriverManager.getConnection(JDBC_Connection.URL,JDBC_Connection.USER, JDBC_Connection.PASSWORD);
		Statement stmt = null;
		stmt = con.createStatement();
		stmt.executeUpdate(strQuery);
		con.close();
		stmt.close();
		
		DSResponse dsResponse = new DSResponse();
		dsResponse.setStatus(0);
		return dsResponse;
	}
	
	protected DSResponse add(DSRequest dsRequest) throws SQLException
	{		
		// get the table name
		String strTblName = joDataSource.getString("ID");
		
		String strQuery = "INSERT INTO " + strTblName;
		String strColumns = "";
		String strValues = "";
		JSONObject joData = dsRequest.getData();		
		// make the query for the insert
		Iterator<?> iKeys = joData.keys();
		while( iKeys.hasNext() ){
            String strKey = (String)iKeys.next();
            strColumns += strKey + ",";
            String strValue = joData.getString(strKey);
            if ( strValue == "true" )
            	strValue = "1";
            else if ( strValue == "false")
            	strValue = "0";
            
        	strValues += "'" + strValue + "',";
        }
		strColumns = "(" + strColumns.substring(0, strColumns.length() - 1) + ")";
		strValues = "(" + strValues.substring(0, strValues.length() - 1) + ")";
		
		strQuery += strColumns + " VALUES " + strValues;
		// get the connection
		Connection con = DriverManager.getConnection(JDBC_Connection.URL,JDBC_Connection.USER, JDBC_Connection.PASSWORD);
		Statement stmt = null;
		stmt = con.createStatement();
		stmt.executeUpdate(strQuery);
		con.close();
		stmt.close();
				
		DSResponse dsResponse = new DSResponse();	    	 
	    dsResponse.setStatus(0);
		return dsResponse;
	}
	
	
	protected DSResponse fetch(DSRequest dsRequest) throws SQLException
	{
		// get the DataSource
		String strDataSource = dsRequest.getDataSource();
		JSONObject jsQueryResult = new JSONObject();
		if ( dsRequest.getAdvancedCriteria() == null)
		{
			jsQueryResult = this.buildStandardCriteria(dsRequest);
		} else
		{
			jsQueryResult = this.buildAdvancedCriteria(dsRequest);
		}
		
		String strWhere = "";
		String strQuery = "";
		
		if ( jsQueryResult.containsKey("query"))
			strWhere = jsQueryResult.getString("query");
		else
			strWhere = "";
		
		if ( strWhere != "" )
			strQuery = "SELECT * FROM " + strDataSource.toString() + " WHERE " + strWhere;
		else
			strQuery = "SELECT * FROM " + strDataSource.toString();
		
		// sort by
		if ( dsRequest.getSortBy() != null )
		{			
			strQuery = strQuery + " ORDER BY ";
			String strSortBy = dsRequest.getSortBy().getString("0");
			
			if ( strSortBy.contains("-"))
			{
				strQuery += strSortBy + " ASC";
			}else
			{
				strQuery += strSortBy.substring(1) + " DESC";
			}
		}
		Statement stmt = null;
		ResultSet rs = null;
		Connection con = DriverManager.getConnection(JDBC_Connection.URL,JDBC_Connection.USER, JDBC_Connection.PASSWORD);
		
		if ( jsQueryResult.containsKey("values") && jsQueryResult.getJSONArray("values").size() > 0 )
		{
			PreparedStatement psWhere = con.prepareStatement(strQuery);
			JSONArray jaValues = jsQueryResult.getJSONArray("values");
			
			for ( int i = 0; i < jaValues.size(); i++ )
			{
				int nIndex = i + 1;
				String strVal = jaValues.getString(i);
				
				psWhere.setString(nIndex,  strVal);
			}
			
			rs = psWhere.executeQuery();
		}else
		{ 	 
			stmt = con.createStatement();
			rs = stmt.executeQuery(strQuery);
		}
		// DSResponse
		JSONArray ja = new JSONArray();
		ja = ResultSetConverter.convert(rs); 
	    rs.last();
	    int rowCount = rs.getRow();
	    int end_row = 0;
	    
	    if( rowCount > 0 )
	    	end_row = rowCount - 1;
	    else
	    	end_row = 0;
	     
	    DSResponse dsResponse = new DSResponse();
	    dsResponse.setData(ja);
	    dsResponse.setStartRow(0);
	    dsResponse.setEndRow( end_row);
	    dsResponse.setTotalRows( rowCount );
	    dsResponse.setStatus(0);
		
	    // close connection and statement
	    con.close();
	    stmt.close();
	    rs.close();

	    return dsResponse;
		
	}
	
	private JSONObject buildAdvancedCriteria(DSRequest dsRequest)
	{
		// get the advanced criteria
		JSONObject joAdvancedCriterias = dsRequest.getAdvancedCriteria();
		JSONObject joCriteras = new JSONObject();
		if ( joAdvancedCriterias != null )
		{
			joCriteras = this.buildCriterion(joAdvancedCriterias);			
		
		}
		return joCriteras;		
	}
	
	private JSONObject buildCriterion( JSONObject joCriterias )
	{
		JSONArray jaCriteria = joCriterias.getJSONArray("criteria");
		String strOperator = joCriterias.getString("operator");
		ArrayList<String> laValues = new ArrayList<String>();
		String strResultQuery = "";
		
		for( int i = 0; i < jaCriteria.size(); i++ )
		{
			JSONObject joC = jaCriteria.getJSONObject(i);
			
			String strFieldName = "", strVal= "", strOp = "", strStart= "", strEnd= "", strQuery = "";
			JSONArray jaSubCritera;
			// fieldName 
			if ( joC.containsKey("fieldName"))
				strFieldName = joC.getString("fieldName");
			// operator
			if ( joC.containsKey("operator"))
				strOp = joC.getString("operator");
			// value
			if ( joC.containsKey("value") )
			{
				strVal = joC.getString("value");
				
				if ( strVal.equals("TRUE"))
					strVal = "1";
				else if ( strVal.equals("FALSE"))
					strVal = "0";				
			}
			// start
			if ( joC.containsKey("start"))
				strStart = joC.getString("start");			
			// end
			if ( joC.containsKey("end"))
				strEnd = joC.getString("end");
			// criteria
			if ( joC.containsKey("criteria"))
			{
				jaSubCritera = joC.getJSONArray("criteria");
			} else
			{
				jaSubCritera = null;
			}
			
			if ( jaSubCritera == null )
			{
				if ( strOp.equals("equals") )
				{
					strQuery = strFieldName + " = ?";
					laValues.add(strVal);
				}else if ( strOp.equals("notEqual") )
				{
					strQuery = strFieldName + " != ?";
					laValues.add(strVal);
				}else if ( strOp.equals("iEquals") )
				{
					strQuery = "UPPER(" + strFieldName + ") = ?";
					laValues.add( "UPPER('" + strVal + "')" );
				}else if ( strOp.equals("iNotEqual") )
				{
					strQuery = "UPPER(" + strFieldName + ") != ?";
					laValues.add( "UPPER('" + strVal + "')" );
				}else if ( strOp.equals("greaterThan") )
				{
					strQuery = strFieldName + " > ?";
					laValues.add(strVal);
				}else if ( strOp.equals("lessThan") )
				{
					strQuery = strFieldName + " < ?";
					laValues.add(strVal);
				}else if ( strOp.equals("greaterOrEqual") )
				{
					strQuery = strFieldName + " >= ?";
					laValues.add(strVal);
				}else if ( strOp.equals("lessOrEqual") )
				{
					strQuery = strFieldName + " <= ?";
					laValues.add(strVal);
				}else if ( strOp.equals("contains") )
				{
					strQuery = strFieldName + " LIKE ?";
					laValues.add("%" + strVal + "%");
				}else if ( strOp.equals("startsWith") )
				{
					strQuery = strFieldName + " LIKE ?";
					laValues.add( "'" + strVal + "%");
				}else if ( strOp.equals("endsWith") )
				{
					strQuery = strFieldName + " LIKE ?";
					laValues.add( "%" + strVal + "'" );
				}else if ( strOp.equals("iContains") )
				{
					strQuery = strFieldName + " LIKE ?";
					laValues.add( "%" + strVal+ "%" );
				}else if ( strOp.equals("iStartsWith") )
				{
					strQuery = "UPPER(" + strFieldName + ") LIKE ?";
					laValues.add( "UPPER('" + strVal + "%)" );
				}else if ( strOp.equals("iEndsWith") )
				{
					strQuery = "UPPER(" + strFieldName + ") LIKE ?";
					laValues.add( "UPPER(%" + strVal + "')" );
				}else if ( strOp.equals("notContains") )
				{
					strQuery = strFieldName + " NOT LIKE ?";
					laValues.add("%" + strVal + "%");
				}else if ( strOp.equals("notStartsWith") )
				{
					strQuery = strFieldName + " NOT LIKE ?";
					laValues.add( "'" + strVal + "%");
				}else if ( strOp.equals("notEndsWith") )
				{
					strQuery = strFieldName + " NOT LIKE ?";
					laValues.add( "%" + strVal + "'" );
				}else if ( strOp.equals("iNotContains") )
				{
					strQuery = "UPPER(" + strFieldName + ") NOT LIKE ?";
					laValues.add( "UPPER(%" + strVal + "%)" );
				}else if ( strOp.equals("iNotStartsWith") )
				{
					strQuery = "UPPER(" + strFieldName + ") NOT LIKE ?";
					laValues.add( "UPPER('" + strVal + "%)" );
				}else if ( strOp.equals("iNotEndsWith") )
				{
					strQuery = "UPPER(" + strFieldName + ") NOT LIKE ?";
					laValues.add( "UPPER(%" + strVal + "')" );
				}else if ( strOp.equals("isNull") )
				{
					strQuery = strFieldName + " IS NULL";
				}else if ( strOp.equals("notNull") )
				{
					strQuery = strFieldName + " IS NOT NULL";
				}else if ( strOp.equals("equalsField") )
				{
					strQuery = strFieldName + " LIKE ?";
					laValues.add( "CONCAT('" + strVal + "', %)" );
				}else if ( strOp.equals("iEqualsField") )
				{
					strQuery = "UPPER(" + strFieldName + ") LIKE ?";
					laValues.add( "UPPER(CONCAT('" + strVal + "', %))" );
				}else if ( strOp.equals("iNotEqualField") )
				{
					strQuery = "UPPER(" + strFieldName + ") NOT LIKE ?";
					laValues.add( "UPPER(CONCAT('" + strVal + "', %))" );
				}else if ( strOp.equals("notEqualField") )
				{
					strQuery = strFieldName + " NOT LIKE ?";
					laValues.add( "CONCAT('" + strVal + "', %)" );
				}else if (strOp.equals("greaterThanField"))
				{
					strQuery = strFieldName + " > ?";
					laValues.add( "CONCAT('" + strVal + "', %)" );
				}else if (strOp.equals("lessThanField"))
				{
					strQuery = strFieldName + " < ?";
					laValues.add( "CONCAT('" + strVal + "', %)" );
				}else if ( strOp.equals("greaterOrEqualField"))
				{
					strQuery = strFieldName + " >= ?";
					laValues.add( "CONCAT('" + strVal + "', %)" );
				}else if ( strOp.equals("lessOrEqualField"))
				{
					strQuery = strFieldName + " <= ?";
					laValues.add( "CONCAT('" + strVal + "', %)" );
				}else if ( strOp.equals("iBetweenInclusive"))
				{
					strQuery = strFieldName + " BETWEEN ? AND ?";
					laValues.add(strStart);
					laValues.add(strEnd);
				}else if ( strOp.equals("iBetweenInclusive"))
				{
					strQuery = strFieldName + " BETWEEN ? AND ?";
					laValues.add(strStart);
					laValues.add(strEnd);
				}
				strResultQuery += strQuery + " " +  strOperator + " ";
			} else
			{
				String strTemp = strResultQuery;
				
				JSONObject joTemp = this.buildCriterion(joC);
				strResultQuery = strTemp + " ( " + joTemp.getString("query") + " ) " + strOperator + " ";
				JSONArray jaValues = joTemp.getJSONArray("values");
				
				for ( int j = 0; j < jaValues.size(); j++ )
				{
					String strTempVal = jaValues.getString(i);
					laValues.add(strTempVal);
				} 
			}			
		}
		String strResult = strResultQuery.substring( 0, strResultQuery.lastIndexOf(strOperator) );
		
		JSONObject joAdvancedCriteria = new JSONObject();
		joAdvancedCriteria.put( "query", strResult );
		joAdvancedCriteria.put( "values", laValues );
		return joAdvancedCriteria;
	}
	
	private JSONObject buildStandardCriteria(DSRequest dsRequest)
	{
		JSONObject jsResult = new JSONObject();
		String strQuery = null;	
		String strResultQuery = null;
		ArrayList<String> alValues = new ArrayList<String>();		 
		if ( dsRequest.checktDataKeys() == false )
		{
			strQuery = "";
			JSONObject joData = dsRequest.getData();
			Iterator<?> iKeys = joData.keys();
			while( iKeys.hasNext() ){
	            String strKey = (String)iKeys.next();	             
	            if( joData.get(strKey) instanceof JSONObject ){
	            	String strValue = joData.getString("strKey");
	            	strQuery += " s." + strKey + " = ? AND ";	
	            	alValues.add(strValue);
	            }
	        }
			if( strQuery.contains("AND"))
				strResultQuery = strQuery.substring(0, strQuery.indexOf("AND"));
		}
		
		jsResult.put("query", strResultQuery);		
		jsResult.put("values", alValues);
		return jsResult;
	}
	 
}
