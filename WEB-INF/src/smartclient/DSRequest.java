package smartclient;

import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONObject;

public class DSRequest {
	private String strDataSource;
	private String strOperationType;
	private int nStartRow;
	private int nEndRow;
	private String strTextMatchStyle;
	private String strComponentId;
	private JSONObject joData;
	private JSONObject joSortBy;
	private JSONObject joOldValues;
	private JSONObject advancedCriteria;
	private HttpServletRequest hsr;
	 
	public DSRequest(JSONObject jsonObject, HttpServletRequest hsr )
	{
		// Parse the JSON request parameter
		if ( jsonObject.containsKey("dataSource") == true )
		{
			String strDs = jsonObject.getString("dataSource");
			this.setDataSource(strDs);
		}
		
		if ( jsonObject.containsKey("operationType") == true )
		{
			String strOperationType = jsonObject.getString("operationType");
			this.setOperationType(strOperationType);
		}
		
		if ( jsonObject.containsKey("startRow") == true )
		{
			int nStartRow = jsonObject.getInt("startRow");
			this.setStartRow(nStartRow);
		}
		
		if ( jsonObject.containsKey("endRow") == true )
		{
			int nEndRow = jsonObject.getInt("endRow");
			this.setEndRow(nEndRow);
		}
		
		if ( jsonObject.containsKey("data") == true )
		{
			String strData = jsonObject.getString("data");
			JSONObject joData = JSONObject.fromObject(strData);
			this.setData(joData);
		}
			
		
		if ( jsonObject.containsKey("textMatchStyle") == true )
		{
			String strTextMatchStyle = jsonObject.getString("textMatchStyle");
			this.setTextMatchStyle(strTextMatchStyle);
		}
		
		if ( jsonObject.containsKey("componentId") == true )
		{
			String strComponentId = jsonObject.getString("componentId");
			this.setComponentId(strComponentId);
		}
		// check to exist the "sortBy" key
		if ( jsonObject.containsKey("sortBy") == true )
		{
			
			String strSortBy = jsonObject.getString("sortBy");
			JSONObject joSortBy = JSONObject.fromObject(strSortBy);
			this.setSortBy(joSortBy);
		}
		// check to exist the "advancedCriteria" key
		if ( jsonObject.containsKey("advancedCriteria") == true )
		{
			JSONObject advancedCriteria = jsonObject.getJSONObject("advancedCriteria");
			this.setAdvancedCriteria(advancedCriteria);
		}
		
		if ( jsonObject.containsKey("oldValues") == true )
		{
			JSONObject joOldValues = jsonObject.getJSONObject("oldValues");		 
			this.setOldValues(joOldValues);
		}
				

		this.hsr = hsr;
	}
	
	public DSResponse execute() throws SQLException
	{
		DataSource ds = new DataSource(this.strDataSource, hsr);
		if ( ds == null) 
			return null;
		
		return ds.execute(this);
	}
	public boolean checktDataKeys()
	{
		JSONObject joData = this.getData();
		int nKeyCount = joData.size();
		
		if (nKeyCount > 0) 
			return true;
		else
			return false;
	}
		 
	public String getDataSource() {
		return strDataSource;
	}

	public void setDataSource(String strDataSource) {
		this.strDataSource = strDataSource;
	}
 
	public String getOperationType() {
		return strOperationType;
	}
	public void setOperationType(String strOperationType) {
		this.strOperationType = strOperationType;
	}
	public int getStartRow() {
		return nStartRow;
	}
	public void setStartRow(int nStartRow) {
		this.nStartRow = nStartRow;
	}
	public int getEndRow() {
		return nEndRow;
	}
	public void setEndRow(int nEndRow) {
		this.nEndRow = nEndRow;
	}
	public String getTextMatchStyle() {
		return strTextMatchStyle;
	}
	public void setTextMatchStyle(String strTextMatchStyle) {
		this.strTextMatchStyle = strTextMatchStyle;
	}
	public String getComponentId() {
		return strComponentId;
	}
	public void setComponentId(String strComponentId) {
		this.strComponentId = strComponentId;
	}
	public JSONObject getData() {
		return joData;
	}
	public void setData(JSONObject joData) {
		this.joData = joData;
	}
	public JSONObject getSortBy() {
		return joSortBy;
	}
	public void setSortBy(JSONObject joSortBy) {
		this.joSortBy = joSortBy;
	}
	public JSONObject getOldValues() {
		return joOldValues;
	}
	public void setOldValues(JSONObject joOldValues) {
		this.joOldValues = joOldValues;
	}
	public JSONObject getAdvancedCriteria() {
		return advancedCriteria;
	}
	public void setAdvancedCriteria(JSONObject advancedCriteria) {
		this.advancedCriteria = advancedCriteria;
	}
	
}
