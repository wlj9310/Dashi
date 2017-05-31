package yelp;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
////import java.util.concurrent.TimeUnit;/////////




/*////
import java.sql.Connection;

///*/




import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import db.DBUtil;
import model.Restaurant;

public class YelpAPI {
	private static final String API_HOST = "https://api.yelp.com";
	private static final String DEFAULT_TERM = "dinner";
	//Optional. Search term (e.g. "food", "restaurants"). 
	//If term isn’t included we search everything. 
	//The term keyword also accepts business names such as "Starbucks".
	private static final int SEARCH_LIMIT = 20;/////////////////
	private static final String SEARCH_PATH = "/v3/businesses/search";
	private static final String TOKEN_HOST = "https://api.yelp.com/oauth2/token";
	private static final String CLIENT_ID = "-A9QHDvvPCNk_EhOOg4zmQ";
	private static final String CLIENT_SECRET = "DLOhjmvSEvPqQ93NBV7VY5KO5kuNhVbaXxOegOAWUxW69w9XofbfLGspPJrbCcxX";
	private static final String GRANT_TYPE = "client_credentials";
	private static final String TOKEN_TYPE = "Bearer";

	public YelpAPI() {}

	/**
	 * Create and send a request to Yelp Token Host and return the access token
	 */	
	private JSONObject obtainAccessToken() {
		try {
			String query = String.format("grant_type=%s&client_id=%s&client_secret=%s",
					GRANT_TYPE,
					CLIENT_ID,
					CLIENT_SECRET);
			
			HttpURLConnection connection = (HttpURLConnection) new URL(TOKEN_HOST).openConnection();

			connection.setDoOutput(true);
			// 设置是否向httpUrlConnection输出，因为这个是post请求，参数要放在    
			// http正文内，因此需要设为true, 默认情况下是false; 
			connection.setRequestMethod("POST");
			//默认request method 为GET

			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			//application/x-www-form-urlencoded 最常见的 POST 提交数据的方式
			DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
			// 发request （Output）
			wr.write(query.getBytes());

			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			// get response
			// 把string强制转换为InputStreamReader
			int responseCode = connection.getResponseCode();
			System.out.println("\nSending 'POST' request to URL : " + TOKEN_HOST);
			System.out.println("Response Code : " + responseCode);
			//看返回的状态码 200-->正常
			String inputLine;
			StringBuffer response = new StringBuffer();
			// 返回的东西存到stringbuffer里
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			return new JSONObject(response.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	//得到token 因为token是新建出来的东西 所以使用POST


	/**
	 * Creates and sends a request to the Search API by term and location.
	 */
	//向search发请求 得到返回信息
	public String searchForBusinessesByLocation(double lat, double lon) {
		//servlet要调用的函数
		String latitude = lat + "";
		String longitude = lon + "";
		
		String query = String.format("term=%s&latitude=%s&longitude=%s&limit=%s",
				DEFAULT_TERM, latitude, longitude, SEARCH_LIMIT);
		/*/////////
		String query = String.format("term=%s&latitude=%s&longitude=%s&sort_by=%s&limit=50&offset=50",
				DEFAULT_TERM, latitude, longitude, "distance");
		//////////////////*/
		String url = API_HOST + SEARCH_PATH;
		try {
			String access_token = obtainAccessToken().getString("access_token");
			HttpURLConnection connection = (HttpURLConnection) new URL(url + "?" + query).openConnection();
			// 建一个机器与yelp之间的联系
			//token如果有效会一直使用原来的token 
			// optional default is GET
			connection.setRequestMethod("GET");
			//改成POST以后可能没法用(Yelp可能不支持) 
			//不设也可以 默认get
			connection.setRequestProperty("Authorization",  TOKEN_TYPE + " " + access_token);
			//设好以后就可以发请求辣
			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			//把从yelp读到的信息存在local的buffer里
			int responseCode = connection.getResponseCode();
			System.out.println("\nSending 'GET' request to URL : " + url);
			System.out.println("Response Code : " + responseCode);
			
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();

			// print result
			return response.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;

	}

	/**
	 * Queries the Search API based on the command line arguments and takes the
	 * first result to query the Business API.
	 */
	private static void queryAPI(YelpAPI yelpApi, double lat, double lon) {
		String searchResponseJSON = yelpApi.searchForBusinessesByLocation(lat, lon);
		JSONObject response = null;
		try {
			response = new JSONObject(searchResponseJSON);
			JSONArray businesses = (JSONArray) response.get("businesses");
			for (int i = 0; i < businesses.length(); i++) {
				JSONObject business = (JSONObject) businesses.get(i);
				System.out.println(business);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Main entry for sample Yelp API requests.
	 */
	public static void main(String[] args) {
		YelpAPI yelpApi = new YelpAPI();
		queryAPI(yelpApi, 37.38, -122.08);
//		try {
//			Class.forName("com.mysql.jdbc.Driver").newInstance();
//		} catch (Exception e1) {
//			e1.printStackTrace();
//		}
//		Connection conn = null;
//		try {
//			conn = DriverManager.getConnection(DBUtil.URL);
//		} catch (SQLException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
//		for (double lat = 41.64; lat <=42.02; lat = lat + 0.01) {
//			for (double lon = -87.93; lon <= -87.52; lon = lon + 0.01) {
//				try {
//					JSONObject response = new JSONObject(api.searchForBusinessesByLocation(lat, lon));
//					JSONArray array = (JSONArray) response.get("businesses");
//
//					List<JSONObject> list = new ArrayList<JSONObject>();
//					//Set<String> visited = getVisitedRestaurants(userId);
//					System.out.println(lat + "" + lon);
//					System.out.println(array.length());
//					for (int i = 0; i < array.length(); i++) {
//						JSONObject object = array.getJSONObject(i);
//						Restaurant restaurant = new Restaurant(object);
//						String businessId = restaurant.getBusinessId();
//						String name = restaurant.getName();
//						String categories = restaurant.getCategories();
//						String city = restaurant.getCity();
//						String state = restaurant.getState();
//						String fullAddress = restaurant.getFullAddress();
//						double stars = restaurant.getStars();
//						double latitude = restaurant.getLatitude();
//						double longitude = restaurant.getLongitude();
//						String imageUrl = restaurant.getImageUrl();
//						String url = restaurant.getUrl();
//						JSONObject obj = restaurant.toJSONObject();
////						if (visited.contains(businessId)) {
////							obj.put("is_visited", true);
////						} else {
////							obj.put("is_visited", false);
////						}
//
//						String sql = "INSERT IGNORE INTO restaurants VALUES (?,?,?,?,?,?,?,?,?,?,?)";
//						PreparedStatement statement = conn.prepareStatement(sql);
//						statement.setString(1, businessId);
//						// 第一个？代替成businessId
//						statement.setString(2, name);
//						statement.setString(3, categories);
//						statement.setString(4, city);
//						statement.setString(5, state);
//						statement.setDouble(6, stars);
//						statement.setString(7, fullAddress);
//						statement.setDouble(8, latitude);
//						statement.setDouble(9, longitude);
//						statement.setString(10, imageUrl);
//						statement.setString(11, url);
//						statement.execute();
//						//System.out.println("asdadas" + i);
//						//System.out.println(lat + "" + lon);
//					}
//				} catch (Exception e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//				//TimeUnit.SECONDS.sleep(1);
//				try {
//				    Thread.sleep(1);
//				} catch(InterruptedException ex) {
//				    Thread.currentThread().interrupt();
//				}
//			}
//		}
	}
}


//重点要记住流程
//具体实现没有辣么重要
// 为什么不是servlet：面向自己内部功能 所以用class