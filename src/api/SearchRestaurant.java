package api;

import java.io.IOException;

import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

import db.DBConnection;
import db.MongoDBConnection;
import db.MySQLDBConnection;

import org.json.JSONArray;

import java.util.*;

/**
 * Servlet implementation class SearchRestaurant
 */
@WebServlet(name = "SearchRestaurants", urlPatterns = { "/restaurants" })
public class SearchRestaurant extends HttpServlet {
	private static final long serialVersionUID = 1L;
    DBConnection connection = new MySQLDBConnection();
	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public SearchRestaurant() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
//		// TODO Auto-generated method stub
//		// response 为什么不写回返回值？
//		//可以让response写多次
//		//写完了发一次 写够了可以直接关掉
//		response.setContentType("application/json");
//		//返回类型 json
//		response.addHeader("Access-Control-Allow-Origin", "*");
//		//允许让什么人访问
//		//Allow all viewers to view this response.
//		String username = "";
//		PrintWriter out = response.getWriter();
//		//Create a PrintWriter from response such that we can add data to response.
//		//response 对应的内容 == out
//		if (request.getParameter("username") != null) {
//			username = request.getParameter("username");
//			//Get the username sent from the client (user)
//			out.print("Hello " + username);
//			//In the output stream, add some magic.
//		}
//		//根据request的username返回hello返回给前端
//		out.flush();
//		//冲到前端
//		out.close();
//		//Close this response for good. 
//		
//		
//		// response.getWriter().append("Served at:
//		// ").append(request.getContextPath());
		
		
//		response.setContentType("application/json");
//		response.addHeader("Access-Control-Allow-Origin", "*");
//
//		String username = "";
//		if (request.getParameter("username") != null) {
//			// 判断是否传入username
//			username = request.getParameter("username");
//		}
//		JSONObject obj = new JSONObject();
//		// 新建一个JSONObject
//		try {
//			obj.put("username", username);
//		} catch (JSONException e) {
//			e.printStackTrace();
//		}
//		// try catch?
//		//try 中出错以后转入catch执行(try中未执行行不再执行) 
//		PrintWriter out = response.getWriter();
//		out.print(obj);
//		// 打印出来
//		out.flush();
//		out.close();
//
//		JSONArray array = new JSONArray();
//		try {
//			if (request.getParameterMap().containsKey("user_id")
//					&& request.getParameterMap().containsKey("lat")
//					&& request.getParameterMap().containsKey("lon")) {
//				String userId = request.getParameter("user_id");
//				double lat = Double.parseDouble(request.getParameter("lat"));
//				double lon = Double.parseDouble(request.getParameter("lon"));
//				// return some fake restaurants
//				array.put(new JSONObject().put("name", "Panda Express"));
//				array.put(new JSONObject().put("name", "Hong Kong Express"));
//			}
//		} catch (JSONException e) {
//			e.printStackTrace();
//		}
//		RpcParser.writeOutput(response, array);
		JSONArray array = new JSONArray();
		if (request.getParameterMap().containsKey("lat")
				&& request.getParameterMap().containsKey("lon")) {
			// term is null or empty by default
			String term = request.getParameter("term");
			//String userId = (String) session.getAttribute("user");
            String userId = "1111";
			double lat = Double.parseDouble(request.getParameter("lat"));
			double lon = Double.parseDouble(request.getParameter("lon"));
			array = connection.searchRestaurants(userId, lat, lon, term);
		}
		RpcParser.writeOutput(response, array);


	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
