package pl.betwatcher.bet365;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pl.betwatcher.betfair.EventType;


public class Bet365NetworkManager {
	private static Bet365NetworkManager sharedInstance = null;

	public static Bet365NetworkManager sharedInstance() {	
		if (sharedInstance == null) {
			sharedInstance = new Bet365NetworkManager();
		}
		return sharedInstance;
	}

	private Bet365NetworkManager() {

	}

	public ArrayList<Bet365Market> getLiveMarkets(ArrayList<EventType> eventTypes) {
		ArrayList<Bet365Market> liveGames = new ArrayList<Bet365Market>();
		HashMap<String, Integer> types = new HashMap<String, Integer>();
		for (EventType eventType : eventTypes) {
			types.put(eventType.name, Integer.parseInt(eventType.id));
		}

		String response = getPath("http://www.bet365.com/Lite/cache/api/?clt=9994&op=22&cid=9999&cpid=&cf=N&lng=1&cty=152&fm=1&tzi=4&oty=2&hd=N");
		ArrayList<String> categories = allMatches(response, "<div id=.*? class=\'expItem lv. open.*? <\\/tr> <\\/table> <\\/div>");
		for (String categoryData : categories) {
			String categoryName = allMatches(categoryData, "<h3 .*? <\\/h3>").get(0);
			Integer firstIndex = categoryName.indexOf('>')+1;
			Integer lastIndex = categoryName.lastIndexOf('<');
			categoryName = categoryName.substring(firstIndex, lastIndex).trim();
			Integer categoryId = types.get(categoryName);
			if (categoryId == null) {
				continue;
			}
			
			ArrayList<String> markets = allMatches(categoryData, "<td class=\"lv2\".*?<\\/td>");
			for (String marketData : markets) {
				String marketName = allMatches(marketData, "class=\"splitem.*?<").get(0);
				firstIndex = marketName.indexOf('>')+1;
				lastIndex = marketName.lastIndexOf('<');
				marketName = marketName.substring(firstIndex, lastIndex).trim();

				String marketId = allMatches(marketData, "id=\'.*?\'").get(0);
				firstIndex = marketId.lastIndexOf('#')+1;
				lastIndex = marketId.lastIndexOf('\'');
				marketId = marketId.substring(firstIndex, lastIndex).trim();

				Bet365Market market = new Bet365Market(categoryName, categoryId, marketName, marketId, new Date(), null);
				liveGames.add(market);
			}
		}
		return liveGames;
	}

	public ArrayList<Bet365MarketOdd> getMarketOdds(Bet365Market market) {
		String request = "http://www.bet365.com/Lite/cache/api/?clt=9994&op=14&cid=9998&cpid=" + market.id;
		ArrayList<Bet365MarketOdd> ratings = null;
		String response = getPath(request);
		if (response == null) {
			return null;
		}
		switch (market.type) {
		case GameTypeSoccer:
			ratings = this.parseGame(market, response, "Fulltime Result");
			break;
		case GameTypeBasketball:
			ratings = this.parseGame(market, response, "To Win Match");
			break;
		case GameTypeTennis:
			ratings = this.parseGame(market, response, "Match Winner");
			break;
		case GameTypeIceHockey:
			ratings = this.parseGame(market, response, "Puck Line");
			break;
		case GameTypeTableTennis:
			ratings = this.parseGame(market, response, "Match Winner");
			break;
		case GameTypeVolleyball:
			ratings = this.parseGame(market, response, "Match Winner");
			break;

		default:
			break;
		}

		return ratings;
	}

	private ArrayList<Bet365MarketOdd> parseGame(Bet365Market game, String response, String eventType) {
		ArrayList<Bet365MarketOdd> ratings = new ArrayList<Bet365MarketOdd>();
		ArrayList<String> results = allMatches(response, ">" + eventType + ".*?</table>");
		if (results.isEmpty()) {
			return ratings;
		}
		String data = results.get(0);
		ArrayList<String> events = allMatches(data, "<a.*?</a>");
		for (String event : events) {
			String eventName = allMatches(event, "left.*?<").get(0);
			eventName = eventName.substring(6, eventName.length()-1);
			String rating = allMatches(event, "right.*?<").get(0);
			rating = rating.substring(7, rating.length()-1);
			Bet365MarketOdd gameRating = new Bet365MarketOdd(game, eventType, eventName, rating);
			ratings.add(gameRating);
		}
		Float margin = new Float(0);
		for (Bet365MarketOdd gameRating : ratings) {
			margin += (1/gameRating.getOdd());
		}
		margin -= 1;
		for (Bet365MarketOdd gameRating : ratings) {
			gameRating.setOdd(gameRating.getOdd()/(1-margin));
		}
		return ratings;
	}
	


	/* private regular expression processing */

	private ArrayList<String> allMatches(String string, String regex) {
		ArrayList<String> matches = new ArrayList<String>();
		Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
		Matcher matcher = pattern.matcher(string);
		while (matcher.find()) {
			matches.add(matcher.group());
		}
		return matches;
	}


	/* private communication methods */		
	private String getPath(String path) {
		String serverResponse = null;
		URL requestURL = null;
		String proxyURL = "81.196.156.93";
		int proxyPort = 80;
		boolean useProxy = false;

		try {
			requestURL = new URL(path);
			HttpURLConnection connection;
			if (useProxy) {
				connection = (HttpURLConnection)requestURL.openConnection(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyURL, proxyPort)));
			} else {
				connection = (HttpURLConnection)requestURL.openConnection();
			}
			connection.setRequestMethod("GET");

			if (connection.getResponseCode()>=400) {
				return null;
			}
			
			//Get Response			
			InputStream is = connection.getInputStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			String line;
			StringBuffer response = new StringBuffer(); 
			while((line = rd.readLine()) != null) {
				response.append(line);
				response.append('\r');
			}
			rd.close();
			serverResponse = response.toString();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (ProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return serverResponse;
	}
}
