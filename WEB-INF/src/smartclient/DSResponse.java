package smartclient;
import net.sf.json.JSONArray;

public class DSResponse {
	 private JSONArray jaData;
     private int nStartRow;
     private int nEndRow;
     private int nTotalRows;
     private int nStatus;
		
		public JSONArray getData() {
			return jaData;
		}
	
		public void setData(JSONArray jaData) {
			this.jaData = jaData;
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
		public int getTotalRows() {
			return nTotalRows;
			
		}
		
		public void setTotalRows(int nTotalRows) {
			this.nTotalRows = nTotalRows;
		}
		
		public int getStatus() {
			return nStatus;
		}
		
		public void setStatus(int nStatus) {
			this.nStatus = nStatus;
		}
}
