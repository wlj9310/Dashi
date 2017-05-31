package api;

import java.io.BufferedReader;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONArray;
import org.json.JSONObject;


//针对post request 没法直接从url读
public class RpcParser {
	public static JSONObject parseInput(HttpServletRequest request) {
		StringBuffer jb = new StringBuffer();
		// 从request里面读
		String line = null;
		try {
			BufferedReader reader = request.getReader();
			while ((line = reader.readLine()) != null) {
				jb.append(line);
			}
			reader.close();
			return new JSONObject(jb.toString());
			//把string转成jsonobject
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	//StringBuffer 线程安全
	//StringBuilder 快
	

	public static void writeOutput(HttpServletResponse response, JSONObject obj) {
		try {			
			response.setContentType("application/json");
			response.addHeader("Access-Control-Allow-Origin", "*");
			PrintWriter out = response.getWriter();
			out.print(obj);
			out.flush();
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}
	
	public static void writeOutput(HttpServletResponse response, JSONArray array) {
		try {			
			response.setContentType("application/json");
			response.addHeader("Access-Control-Allow-Origin", "*");
			PrintWriter out = response.getWriter();
			out.print(array);
			out.flush();
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}

}
