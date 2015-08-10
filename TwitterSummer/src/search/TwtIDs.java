package search;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;

import twitter.RateLimit;
import twitter4j.*;
import twitter4j.auth.AccessToken;
import twitter4j.User;

public class TwtIDs {
	FileInputStream ip;
	String lastTwitterHandle;
	
	public String getLastTwitterHandle() {
		return lastTwitterHandle;
	}

	public TwtIDs() {
		String filename = "handles1.txt";
		File file = new File(filename); // Open file
		
		try {
			if (file.exists()) ip = new FileInputStream(file);
			else {
				System.out.println("Error: could not open file");
				System.exit(-1);
			}
		} catch (FileNotFoundException fe) {
			fe.printStackTrace();
			System.exit(-1);
		}
	}
	
	// Construct input stream to start one name after string lth
	public TwtIDs(String lth) { // last twitter handle
		String filename = "handles1.txt";
		File file = new File(filename); // Open file
		
		try {
			if (file.exists()) ip = new FileInputStream(file);
			else {
				System.out.println("Error: could not open file");
				System.exit(-1);
			}
		} catch (FileNotFoundException fe) {
			fe.printStackTrace();
			System.exit(-1);
		}
		
		try {
			char c = 0;
			while (ip.available() != 0) {
				String line = "";
				
				// Read line from text file for twitter handle
				while ( (c = (char) ip.read()) != '\n') line += c;
				
				if (line.equals(lth)) return;
			}
		} catch (IOException ie) {
			ie.printStackTrace();
		}
		
		// SHOULD NOT BE REACHED
		System.err.println("Error: Name not found");
		System.exit(-1);
	}
	
	public void close() throws IOException {
		ip.close();
	}
	
	// Gets twitter handles in txt file and saves ids of user and followers to file
	// NOTE: 20 Twitter handles per call, then returns ids
	public ArrayList<Long> getIDs() { 
		String line = "";
		ArrayList<Long> returnIds = new ArrayList<Long>();
		
		try {
			char c = 0;
			for (int i = 0; ip.available() != 0 && i < 5; i++) {
				line = "";
				
				// Read line from text file for twitter handle
				while ( (c = (char) ip.read()) != '\n') line += c;
				
				if (!line.isEmpty()) {
					long id = getID(line); // Add twitter handle's id
					
					// If user exists, get followers
					if (id != -1) {
						returnIds.add(id);

						this.lastTwitterHandle = line;
						
						// Add twitter handle's followers' ids
						for (long[] followerBlock : getFollowerIDs(id, -1)) {
							System.out.print("-fb-");
							for (long followerID : followerBlock) {
								returnIds.add(followerID);
							}
						}
						
						System.out.println(line); // Output should be: # of blocks in "-fb-" + Username
					}
				}
			}
		} catch (IOException ie) {
			ie.printStackTrace();
		}
		
		return returnIds;
	}
	private long getID(String twitterHandle) {
		// For if twitter.showUser() causes an exception and can't get the id
		// returns ID of user or -1
		Twitter twitter = new TwitterFactory().getInstance();
		
		twitter.setOAuthConsumer("hhceDx7pPpmN5d2fdvuAQJrGf", 
				"Q1NYcmQFprf9kSd7Dm8IrAnK807UrzZuOPX8t00BXa6b3kPgzo");
		AccessToken accessToken = new AccessToken("53612356-v5w0OvhsU0TLgQ3975Soc4yUsp9TIbF19NVkNLrEF", 
				"ZXjvPyiadzbbEtyYDVo9Uim4KexcLWjn7msesRcuoyjtH");
		twitter.setOAuthAccessToken(accessToken);
		
		long id;
		
		try {
			User u = twitter.showUser(twitterHandle);
			id = u.getId();
		} catch (TwitterException e) {
			// Catches errors that may happen while getting user account info and ID
			
			// Error 429 - Out of requests, sleep then return search result
			if (e.getStatusCode() == 429) {
				try {
					System.err.println("429: Too many Requests. Sleeping.");
					Thread.sleep(RateLimit.getTimeTilReset("showUser"));
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				return getID(twitterHandle);
			}
			
			// Error -1 - No internet
			else if (e.getStatusCode() == -1) System.err.println("-1: Internet is offline.");
			
			// Error 403 - User account banned
			// Error 404 - User not found
			else System.err.println(Integer.toString(e.getStatusCode()) + ": " + twitterHandle);
			
			return -1;
		}
		
		return id;
	}
	private LinkedList<long[]> getFollowerIDs(long userID, long c) { // userId for follower search, c for cursor
		// Gets followers from specific user and returns list of followers
		// CURSOR PARAMETER: to call function recursively if rate limit exceeded
		
		Twitter twitter = new TwitterFactory().getInstance();
		
		twitter.setOAuthConsumer("hhceDx7pPpmN5d2fdvuAQJrGf", 
				"Q1NYcmQFprf9kSd7Dm8IrAnK807UrzZuOPX8t00BXa6b3kPgzo");
		AccessToken accessToken = new AccessToken("53612356-v5w0OvhsU0TLgQ3975Soc4yUsp9TIbF19NVkNLrEF", 
				"ZXjvPyiadzbbEtyYDVo9Uim4KexcLWjn7msesRcuoyjtH");
		twitter.setOAuthAccessToken(accessToken);
		
		// if rate limit for getFollowerIDs is exceeded, sleep and recursively call this function to add
		//   results to followers
		LinkedList<long[]> followers = new LinkedList<long[]>();
		long cursor = c; 
		
		try {
			IDs followerIDs = twitter.getFollowersIDs(userID, cursor, 5000); // initialize
			boolean hasNextCursor = true;
			// followers paged in numbers of 5000 each
			for (cursor = c; hasNextCursor;) {
				// add each follower array of IDs
				followers.add(followerIDs.getIDs());
				
				// go to next cursor, if exists
				if (followerIDs.hasNext()) {
					cursor = followerIDs.getNextCursor();
					followerIDs = twitter.getFollowersIDs(userID, cursor, 5000);
					
					hasNextCursor = true;
				} else {
					hasNextCursor = false;
				}
			}
		} catch (TwitterException e) {
			if (e.getStatusCode() == 429) {
				try {
					System.err.println("429: Too many Requests. Sleeping.");
					Thread.sleep(RateLimit.getTimeTilReset("getFollowerIDs"));
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				
				// Continue getting followers with recursive call
				followers.addAll(getFollowerIDs(userID, cursor));				
			}
			// Unauthorized to see followers - return empty list
			// should occur on first call to getFollowerIDs
			else if (e.getStatusCode() == 401) {
				System.err.println("401: Unauthorized. ");
			}
			else {
				e.printStackTrace();
				System.exit(-1);
			}
		}
		
		return followers;
	}
	
	public String writeIDsToFile(ArrayList<Long> ids, int fileNum) {
		// Name of file: ids_#.txt
        File logFile = new File("ids_" + Integer.toString(fileNum) + ".txt");
        
        // Writes ids to file
        BufferedWriter writer = null;

        try {
            writer = new BufferedWriter(new FileWriter(logFile));
			
			for (long id : ids) 
	        	writer.write(Long.toString(id) + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
        
        try {
			return logFile.getCanonicalPath();
		} catch (IOException e) {
			e.printStackTrace();
			
			return "Error: file name getCanonicalPath() failed";
		}
	}
	public static void main(String args[]) {
		// Authorization test
//		Twitter twitter = new TwitterFactory().getInstance();
//		
//		twitter.setOAuthConsumer("hhceDx7pPpmN5d2fdvuAQJrGf", 
//				"Q1NYcmQFprf9kSd7Dm8IrAnK807UrzZuOPX8t00BXa6b3kPgzo");
//		AccessToken accessToken = new AccessToken("53612356-v5w0OvhsU0TLgQ3975Soc4yUsp9TIbF19NVkNLrEF", 
//				"ZXjvPyiadzbbEtyYDVo9Uim4KexcLWjn7msesRcuoyjtH");
//		twitter.setOAuthAccessToken(accessToken);
//		
//		System.out.println("Made twitter object and token, now trying request.");
//		User u;
//		try {
//			u = twitter.showUser("skygazerpkform");
//			System.out.println("User: " + u.getScreenName());
//		} catch (TwitterException e) {
//			e.printStackTrace();
//		}
//		
//		System.exit(0);
		
		System.out.println("Now getting IDs and outputting to files!");
		
		// gets ids of all followers and writes to file
		TwtIDs ti = new TwtIDs("Stolib");
		ArrayList<Long> ids = ti.getIDs(); // First 20 ids
		for (int i = 90; !ids.isEmpty(); i++, ids = ti.getIDs()) { // Creates files in incrementing index with IDs filled in
			String filename = ti.writeIDsToFile(ids, i); // Writes ids array to file
			System.out.println(filename + " is complete!");
		}
		
		// Testing getFollowerIDs
//		{
//			System.out.println("Testing getFollowerIDs()");
//			Twitter twitter = new TwitterFactory().getInstance();
//			
//			twitter.setOAuthConsumer("hhceDx7pPpmN5d2fdvuAQJrGf", 
//					"Q1NYcmQFprf9kSd7Dm8IrAnK807UrzZuOPX8t00BXa6b3kPgzo");
//			AccessToken accessToken = new AccessToken("53612356-v5w0OvhsU0TLgQ3975Soc4yUsp9TIbF19NVkNLrEF", 
//					"ZXjvPyiadzbbEtyYDVo9Uim4KexcLWjn7msesRcuoyjtH");
//			twitter.setOAuthAccessToken(accessToken);
//			
//			try {
//				TwtIDs ti_test = new TwtIDs();
//				User u = twitter.showUser("CarePractice");
//				List<long[]> results = ti_test.getFollowerIDs(u.getId(), -1);
//				
//				 for (int i = 0; i < 50; i++) {
//					 System.out.println(results.get(0)[i]);
//				 }
//			} catch (TwitterException e) {
//				e.printStackTrace();
//			}
//			
//			System.exit(0);
//		}
	}
}
