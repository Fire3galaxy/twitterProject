package twitter;

import java.util.Map;

import twitter4j.RateLimitStatus;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;

public class RateLimit {
	static public int getTimeTilReset(String s) {
		Twitter twitter = new TwitterFactory().getInstance();
		
		twitter.setOAuthConsumer("KOmMvDbWuOkg7IzGhn5DT6NLH", 
				"dalWrw35UOFAD2N2um4pxpj7C04QUzYSvEO1nrM0CU8xwxxznB");
		AccessToken accessToken = new AccessToken("53612356-eWirp6zij2dvHe4KS88bWnMLnMMdU1lUxKjCf5HJN", 
				"BQBSk6p9Jm3aWhRydfK0hgj0TD8GnZYzZy3Mvfsa7Oen0");
		twitter.setOAuthAccessToken(accessToken);
		
		try {
			if (s.equals("showUser")) {
				Map<String, RateLimitStatus> rateLimitStatus = twitter.getRateLimitStatus("users");
				RateLimitStatus showUsersRL = rateLimitStatus.get("/users/show/:id");
				
				return showUsersRL.getSecondsUntilReset() * 1000 + 60 * 1000;
			}
			else if (s.equals("lookupUsers")) {
				Map<String, RateLimitStatus> rateLimitStatus = twitter.getRateLimitStatus("users");
				RateLimitStatus lookupUsersRL = rateLimitStatus.get("/users/lookup");
				
				return lookupUsersRL.getSecondsUntilReset() * 1000 + 60 * 1000;
			}
			else if (s.equals("getFollowerIDs")) {
				Map<String, RateLimitStatus> rateLimitStatus = twitter.getRateLimitStatus("followers");
				RateLimitStatus getFollowIDsRL = rateLimitStatus.get("/followers/ids");
				
				return getFollowIDsRL.getSecondsUntilReset() * 1000 + 60 * 1000;
			}
			else {
				System.out.println("Time Reset-String not recognized.");
				System.exit(-1);
			}
		} catch(TwitterException e) {
			System.out.println("Error is in Time Reset function");
			e.printStackTrace();
			System.exit(-1);
		}
		
		return 0;
	}
}
