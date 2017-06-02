package offline;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JSONObjectTest {
	public static void main( String[] arg) {
		try {
			JSONObject jo = new JSONObject();
			jo.put("aaaa", 1111);
			jo.put("ssss", 1234);
			System.out.println(jo);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}	
}
