package scan;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import excel.ExcelMethods;
import twitter.RateLimit;
import twitter4j.ResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;

public class TwitterScan {
	public static void main(String args[]) {
		Twitter twitter = getTwitterInstance();
		ExcelMethods excelFile = new ExcelMethods("doctors (1).xlsx");
		Map<Integer, HashSet<TwitterResult>> results = new TreeMap<Integer, HashSet<TwitterResult>>(); 
		
		for (int i = 1; i < 3; i++) { // number of ids_# files
			List<Long> idsFromFile = FileGetter.getIDsFromFile(i);
			
			// Use sublist of size 100 for twitter.lookupUsers()
			int fromIndex = 0, toIndex = 100; 
			
			// toIndex should not exceed size of list
			if (100 > idsFromFile.size()) toIndex = idsFromFile.size();
			else toIndex = 100;
			
			// Iterate through all sublists of List<Long> idsFromFile 
			while (fromIndex < idsFromFile.size()) {
//				System.out.print("At " + Integer.toString(fromIndex) + ", size = ");
				
				long IDsSubset[] = getPrimitives(idsFromFile.subList(fromIndex, toIndex));
				
//				System.out.println(IDsSubset.length);
				
				ResponseList<User> users = lookupUsers(IDsSubset, twitter);
				
				for (User user : users) {
					String[] names = splitUp(user.getName()); // First, Last
					
					for (String name : names) {
						name = name.toLowerCase();
						
						if (excelFile.getByFirstName().containsKey(name)) {
							// Compare twitter account to all profileDr in list
						}
						if (excelFile.getByLastName().containsKey(name)) {
							// Compare twitter account to all profileDr in list
							// if id of user already has twitter username in result list, ignore the profileDr
						}
					}
				}
				
				fromIndex += 100;
				if (fromIndex + 100 >= idsFromFile.size()) toIndex = idsFromFile.size();
				else toIndex = fromIndex + 100;
			}
		}
	}
	// Sets twitter keys and returns twitter object
	static Twitter getTwitterInstance() {
		Twitter twitter = new TwitterFactory().getInstance();
		
		twitter.setOAuthConsumer("hhceDx7pPpmN5d2fdvuAQJrGf", 
				"Q1NYcmQFprf9kSd7Dm8IrAnK807UrzZuOPX8t00BXa6b3kPgzo");
		AccessToken accessToken = new AccessToken("53612356-v5w0OvhsU0TLgQ3975Soc4yUsp9TIbF19NVkNLrEF", 
				"ZXjvPyiadzbbEtyYDVo9Uim4KexcLWjn7msesRcuoyjtH");
		twitter.setOAuthAccessToken(accessToken);
		
		return twitter;
	}
	static long[] getPrimitives(List<Long> array) {
		long primitives[] = new long[array.size()];
		
		for (int i = 0; i < array.size(); i++)
			if (array.get(i) != null) primitives[i] = array.get(i).longValue();
		
		return primitives;
	}
	static ResponseList<User> lookupUsers(long[] ids, Twitter instance) {
		System.out.println("5 ");
		try {
			return instance.lookupUsers(ids);
		} catch (TwitterException e) {
			// Error 429 - Out of requests, sleep then return search result
			if (e.getStatusCode() == 429) {
				try {
					System.err.println("429: Too many Requests. Sleeping.");
					Thread.sleep(RateLimit.getTimeTilReset("lookupUsers"));
					
					return lookupUsers(ids, instance);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		}
		
		System.err.println("Error in static function lookupUsers");
		return null;
	}
	
	static String[] splitUp(String name) {
		return name.split("[^\\w+( \\w+)*$]");
	}
}
