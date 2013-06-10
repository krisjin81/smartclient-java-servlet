package utils;

import java.io.BufferedReader;
import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONObject;

public class HttpServletRequest2JSON {
	public static JSONObject convert(HttpServletRequest req)  
	{
		StringBuffer jb = new StringBuffer();
		String line = null;
		try {
		    BufferedReader reader = req.getReader();
		    while ((line = reader.readLine()) != null)
		      jb.append(line);
		}catch (Exception e) { /*report an error*/ }

		JSONObject jsonObject = JSONObject.fromObject(jb.toString());
		
		return jsonObject;		
	}
}
