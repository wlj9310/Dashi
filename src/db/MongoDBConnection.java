package db;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import model.Restaurant;

import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import yelp.YelpAPI;

import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.regex;

public class MongoDBConnection implements DBConnection {

	private static final int MAX_RECOMMENDED_RESTAURANTS = 10;

	private MongoClient mongoClient;
	private MongoDatabase db;

	public MongoDBConnection() {
		// Connects to local mongodb server.
		mongoClient = new MongoClient();
		db = mongoClient.getDatabase(DBUtil.DB_NAME);

	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		if (mongoClient != null) {
			mongoClient.close();
		}
	}

	@Override
	public void setVisitedRestaurants(String userId, List<String> businessIds) {
		// TODO Auto-generated method stub
		// db.users.update(
		// {
		// user_id:"1111"
		// },
		// {
		// $pushall:{
		// Visited: [“aaaa”, “bbbb”]
		// }
		// }
		// )
		// 一个document对应一个大括号
		// new是对java来说的(相当于在java中实现大括号)
		// pushAll 针对array 如果是单个object可以用push
		db.getCollection("users").updateOne(new Document("user_id", userId),
				new Document("$pushAll", new Document("visited", businessIds)));
	}

	@Override
	public void unsetVisitedRestaurants(String userId, List<String> businessIds) {
		// TODO Auto-generated method stub
		db.getCollection("users").updateOne(new Document("user_id", userId),
				new Document("$pullAll", new Document("visited", businessIds)));
		// pull中如果有不在原来list中的元素直接无视就好
	}

	@Override
	public Set<String> getVisitedRestaurants(String userId) {
		// TODO Auto-generated method stub
		Set<String> set = new HashSet<>();
		// db.users.find({user_id:1111})
		FindIterable<Document> iterable = db.getCollection("users").find(eq("user_id", userId));
		// eq相当于MySQL中的where
		if (iterable.first().containsKey("visited")) {
			// 因为只可能有一个结果 所以这里用if
			List<String> list = (List<String>) iterable.first().get("visited");
			// first()找出iterator的第一个
			set.addAll(list);
		}
		return set;
	}

	@Override
	public JSONObject getRestaurantsById(String businessId, boolean isVisited) {
		// TODO Auto-generated method stub
		// return null;
		FindIterable<Document> iterable = db.getCollection("restaurants").find(eq("business_id", businessId));
		try {
			JSONObject obj = new JSONObject(iterable.first().toJson());
			obj.put("is_visited", isVisited);
			return obj;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public JSONArray recommendRestaurants(String userId) {
		// TODO Auto-generated method stub
		// return null;
		try {

			Set<String> visitedRestaurants = getVisitedRestaurants(userId);// step
																			// 1
			Set<String> allCategories = new HashSet<>();// why hashSet? //step 2
			// 如果考虑权重可以使用map（改进recommendation）
			for (String restaurant : visitedRestaurants) {
				allCategories.addAll(getCategories(restaurant));
			}
			Set<String> allRestaurants = new HashSet<>();// step 3
			for (String category : allCategories) {
				Set<String> set = getBusinessId(category);
				allRestaurants.addAll(set);
			}
			Set<JSONObject> diff = new HashSet<>();// step 4
			int count = 0;
			for (String businessId : allRestaurants) {
				// Perform filtering
				if (!visitedRestaurants.contains(businessId)) {
					diff.add(getRestaurantsById(businessId, false));
					count++;
					if (count >= MAX_RECOMMENDED_RESTAURANTS) {
						break;
					}
				}
			}
			return new JSONArray(diff);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return null;
	}

	@Override
	public Set<String> getCategories(String businessId) {
		// TODO Auto-generated method stub
		Set<String> set = new HashSet<>();
		FindIterable<Document> iterable = db.getCollection("restaurants").find(eq("business_id", businessId));

		if (iterable.first().containsKey("categories")) {
			// 只有一个 所以用if
			String[] categories = iterable.first().getString("categories").split(",");
			for (String category : categories) {
				set.add(category.trim());
			}
		}
		return set;

		// return null;
	}

	@Override
	public Set<String> getBusinessId(String category) {
		// TODO Auto-generated method stub
		// return null;
		Set<String> set = new HashSet<>();
		// similar to LIKE %category% in MySQL
		FindIterable<Document> iterable = db.getCollection("restaurants").find(regex("categories", category));
		iterable.forEach(new Block<Document>() {
			//对iterable里的每一个元素
			@Override
			//匿名类 就在这里用 所以没必要专门写一个class // override apply
			public void apply(final Document document) {
				set.add(document.getString("business_id"));
			}
		});
		return set;
	}

	@Override
	public JSONArray searchRestaurants(String userId, double lat, double lon, String term) {
		// TODO Auto-generated method stub
		// return null;
		try {
			YelpAPI api = new YelpAPI();
			JSONObject response = new JSONObject(api.searchForBusinessesByLocation(lat, lon));
			JSONArray array = (JSONArray) response.get("businesses");

			List<JSONObject> list = new ArrayList<JSONObject>();
			Set<String> visited = getVisitedRestaurants(userId);

			for (int i = 0; i < array.length(); i++) {
				JSONObject object = array.getJSONObject(i);
				Restaurant restaurant = new Restaurant(object);
				String businessId = restaurant.getBusinessId();
				String name = restaurant.getName();
				String categories = restaurant.getCategories();
				String city = restaurant.getCity();
				String state = restaurant.getState();
				String fullAddress = restaurant.getFullAddress();
				double stars = restaurant.getStars();
				double latitude = restaurant.getLatitude();
				double longitude = restaurant.getLongitude();
				String imageUrl = restaurant.getImageUrl();
				String url = restaurant.getUrl();
				JSONObject obj = restaurant.toJSONObject();
				if (visited.contains(businessId)) {
					obj.put("is_visited", true);
				} else {
					obj.put("is_visited", false);
				}
				// Question: why using upsert instead of insert directly?
				UpdateOptions options = new UpdateOptions().upsert(true);
				// upsert:update(有过)或insert(插入)

				db.getCollection("restaurants").updateOne(new Document().append("business_id", businessId),
						new Document("$set", new Document().append("business_id", businessId).append("name", name)
								.append("categories", categories).append("city", city).append("state", state)
								.append("full_address", fullAddress).append("stars", stars).append("latitude", latitude)
								.append("longitude", longitude).append("image_url", imageUrl).append("url", url)),
						options);
				list.add(obj);
			}
			//return new JSONArray(list);
			if (term == null || term.isEmpty()) {
				return new JSONArray(list);
			} else {
				// Use text search to perform better efficiency
				return filterRestaurants(term);
			}

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return null;
	}
	private JSONArray filterRestaurants(String term) {
		try {
			Set<JSONObject> set = new HashSet<JSONObject>();
			FindIterable<Document> iterable = db.getCollection("restaurants").find(Filters.text(term));
 
			iterable.forEach(new Block<Document>() {
				@Override
				public void apply(final Document document) {
					set.add(getRestaurantsById(document.getString("business_id"), false));
				}
			});
			return new JSONArray(set);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return null;
	}

	@Override
	public Boolean verifyLogin(String userId, String password) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getFirstLastName(String userId) {
		// TODO Auto-generated method stub
		return null;
	}

}
