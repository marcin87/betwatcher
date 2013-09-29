package pl.betwatcher.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import pl.betwatcher.bet365.Bet365Market;
import pl.betwatcher.bet365.Bet365MarketOdd;
import pl.betwatcher.betfair.BetFairMarketPrice;
import pl.betwatcher.betfair.EventType;
import pl.betwatcher.betfair.BetFairMarket;
import pl.betwatcher.utils.Utils;

public class DataManager {
	private static DataManager sharedInstance = null;
	private static String url = "jdbc:mysql://vps31242.ovh.net:3306/Betwatcher";
	private static String login = "remote";
	private static String password = "admin";

	public static DataManager sharedInstance() {	
		if (sharedInstance == null) {
			sharedInstance = new DataManager();
		}
		return sharedInstance;
	}

	private DataManager() {
	}

	// BetFair	

	public int saveBFActiveEventTypes(ArrayList<EventType> eventTypes) {
		int savedEventsCount = 0;
		try {
			Connection connection = DriverManager.getConnection(url, login, password);
			for (EventType eventType : eventTypes) {
				Statement statement = connection.createStatement();
				ResultSet resultSet = statement.executeQuery("SELECT * FROM bf_eventtype WHERE id='" + eventType.id + "'");
				if (!resultSet.next()) {
					savedEventsCount++;
					PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO bf_eventtype(id,name) VALUES (?,?)");
					preparedStatement.setInt(1, Integer.parseInt(eventType.id));
					preparedStatement.setString(2, eventType.name);
					preparedStatement.execute();
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return savedEventsCount;
	}

	public int saveBFMarkets(ArrayList<BetFairMarket> markets) {
		int savedMarketsCount = 0;
		try {
			Connection connection = DriverManager.getConnection(url, login, password);
			for (BetFairMarket market : markets) {
				Statement statement = connection.createStatement();
				ResultSet resultSet = statement.executeQuery("SELECT * FROM bf_market WHERE id='" + market.id + "'");
				if (!resultSet.next()) {
					savedMarketsCount++;
					PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO bf_market(id,name) VALUES (?,?)");
					preparedStatement.setInt(1, Integer.parseInt(market.id));
					preparedStatement.setString(2, market.name);
					preparedStatement.execute();
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return savedMarketsCount;
	}

	public void updateBFMarket(BetFairMarket market) {
		try {
			if (market.startDate == null) {
				Connection connection = DriverManager.getConnection(url, login, password);
				PreparedStatement preparedStatement = connection.prepareStatement("DELETE from bf_market WHERE id=" + market.id);
				preparedStatement.execute();

			} else {
				Connection connection = DriverManager.getConnection(url, login, password);
				String query = "UPDATE bf_market SET startDate=?, runner1Name=?, runner2Name=?, menuPath=?, baseRate=?, runner1Id=?, runner2Id=?, eventTypeId=? WHERE id=" + market.id;
				PreparedStatement preparedStatement = connection.prepareStatement(query);
				preparedStatement.setTimestamp(1, new Timestamp(market.startDate.getTime()));
				preparedStatement.setString(2, market.runner1Name);
				preparedStatement.setString(3, market.runner2Name);
				preparedStatement.setString(4, market.menuPath);
				preparedStatement.setInt(5, market.baseRate);
				preparedStatement.setInt(6, market.runner1Id);
				preparedStatement.setInt(7, market.runner2Id);
				preparedStatement.setInt(8, market.eventTypeId);
				preparedStatement.execute();
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public ArrayList<BetFairMarket> getBFNewMarkets() {
		ArrayList<BetFairMarket> markets = new ArrayList<BetFairMarket>();
		try {
			Connection connection = DriverManager.getConnection(url, login, password);
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery("SELECT * FROM bf_market WHERE startDate IS NULL");
			while (resultSet.next()) {
				String id = "" + resultSet.getInt("id");
				String name = resultSet.getString("name");
				BetFairMarket market = new BetFairMarket(id, name);
				markets.add(market);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return markets;
	}

	// Bet365	

	public boolean saveBet365MarketOdd(Bet365MarketOdd marketOdd) {
		try {
			Connection connection = DriverManager.getConnection(url, login, password);
			Statement statement = connection.createStatement();
			String query = "SELECT * FROM b365_marketodd WHERE bf_marketid=\"" + marketOdd.market.id + "\" and runner=\"" + marketOdd.eventName + "\" group by bf_marketid,runner,timestamp order by timestamp desc";
			ResultSet resultSet = statement.executeQuery(query);
			if (resultSet.next()) {
				Float odd = resultSet.getFloat("odd");
				if (odd.equals(marketOdd.getOdd())) {
					return false;
				}
			}
			PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO b365_marketodd(bf_marketid,timestamp,runner,odd) VALUES (?,?,?,?)");
			preparedStatement.setString(1, marketOdd.market.id);
			preparedStatement.setTimestamp(2, new Timestamp(marketOdd.timestamp.getTime()));
			preparedStatement.setString(3, marketOdd.eventName);
			preparedStatement.setFloat(4, marketOdd.getOdd());
			preparedStatement.execute();
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return true;
	}

	public void setMarketInactive(Bet365Market market) {
		System.out.println("Set inactive");
		try {
			Connection connection = DriverManager.getConnection(url, login, password);
			PreparedStatement preparedStatement = connection.prepareStatement("UPDATE b365_market SET status=?, useToPlay=? WHERE id=?");
			preparedStatement.setString(1, "INACTIVE");
			preparedStatement.setBoolean(2, false);
			preparedStatement.setString(3, market.id);
			preparedStatement.execute();
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public ArrayList<Bet365Market> getActiveMatchedFreeMarkets() {
		ArrayList<Bet365Market> activeMatchedMarkets = new ArrayList<Bet365Market>();
		try {
			Connection connection = DriverManager.getConnection(url, login, password);
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery("SELECT * FROM b365_market " +
					"WHERE status='ACTIVE' " +
					"and bf_marketid is not null " +
					"and (algorithm is null or algorithm='') " +
					"and useToPlay = true");
			while (resultSet.next()) {
				String id = resultSet.getString("id");
				String categoryName = resultSet.getString("category");
				String bf_marketId = resultSet.getString("bf_marketId");
				Bet365Market market = new Bet365Market(categoryName, 0, null, id, null, bf_marketId);
				activeMatchedMarkets.add(market);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return activeMatchedMarkets;
	}

	public boolean saveOrUpdateB365Market(Bet365Market market) {
		boolean newCreated = false;
		try {
			Connection connection = DriverManager.getConnection(url, login, password);
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery("SELECT * FROM b365_market WHERE id='" + market.id + "'");
			if (!resultSet.next()) {
				newCreated = true;
				PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO b365_market(id, name, category, eventTypeId, createDate, status, useToPlay) VALUES (?,?,?,?,?,?,?)");
				preparedStatement.setString(1, market.id);
				preparedStatement.setString(2, market.name);
				preparedStatement.setString(3, market.categoryName);
				preparedStatement.setInt(4, market.categoryId);
				preparedStatement.setTimestamp(5, new Timestamp(market.timestamp.getTime()));
				preparedStatement.setString(6, "ACTIVE");
				preparedStatement.setBoolean(7, false);
				preparedStatement.execute();
			} else {
				PreparedStatement preparedStatement = connection.prepareStatement("UPDATE b365_market SET status=? WHERE id=?");
				preparedStatement.setString(1, "ACTIVE");
				preparedStatement.setString(2, market.id);
				preparedStatement.execute();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return newCreated;
	}

	// Mixed	

	public boolean matchMarket(Bet365Market market) {
		boolean matched = false;
		String runners = DataManager.cleanupString(" " + market.name + " ");
		Connection connection;
		try {
			connection = DriverManager.getConnection(url, login, password);
			Statement statement = connection.createStatement();
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String query = "select * " + 
					"from bf_market " +
					"where eventTypeId = " + market.categoryId + " " + 
					"and ABS(TIMESTAMPDIFF(MINUTE, startDate, '" + format.format(market.timestamp) + "'))<100 ";	
			ResultSet resultSet = statement.executeQuery(query);
			while (resultSet.next()) {
				String id = "" + resultSet.getInt("id");
				String bf_runner1 = DataManager.cleanupString(" " + resultSet.getString("runner1Name") + " ");
				String bf_runner2 = DataManager.cleanupString(" " + resultSet.getString("runner2Name") + " ");

				String[] bf_runner1Elements = bf_runner1.trim().split(" ");
				String[] bf_runner2Elements = bf_runner2.trim().split(" ");

				float ratio = 0;
				for (String string : bf_runner1Elements) {
					string.trim();
					ratio += Utils.AllMatches(runners, " " + string + " ").size();
				}
				for (String string : bf_runner2Elements) {
					string.trim();
					ratio += Utils.AllMatches(runners, " " + string + " ").size();
				}
				if (ratio>0) {
					ratio/=(bf_runner1Elements.length + bf_runner2Elements.length);
				}
				if (ratio>0.6) {
					matched = true;
					connection = DriverManager.getConnection(url, login, password);
					query = "UPDATE b365_market SET BF_MARKETID=" + id + " WHERE id='" + market.id + "'";
					PreparedStatement preparedStatement = connection.prepareStatement(query);
					preparedStatement.execute();
				}
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return matched;
	}

	public ArrayList<EventType> getEventTypesToPlay() {
		ArrayList<EventType> eventTypes = new ArrayList<EventType>();
		try {
			Connection connection = DriverManager.getConnection(url, login, password);
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery("SELECT * FROM bf_eventtype WHERE useToPlay is true");
			while (resultSet.next()) {
				String id = "" + resultSet.getInt("id");
				String name = resultSet.getString("name");
				EventType eventType = new EventType(id, name);
				eventTypes.add(eventType);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return eventTypes;
	}
	
	public void updateB365MarketForAlgorithm(Bet365Market market, String algorithmName) {
		try {
			Connection connection = DriverManager.getConnection(url, login, password);
			PreparedStatement preparedStatement = connection.prepareStatement("UPDATE b365_market SET algorithm=? WHERE id=?");
			preparedStatement.setString(1, algorithmName);
			preparedStatement.setString(2, market.id);
			preparedStatement.execute();
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void saveBFMarketPrice(BetFairMarketPrice price) {
		try {
			Connection connection = DriverManager.getConnection(url, login, password);
			PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO bf_marketprice(bf_marketid, runnerid, pricetoback, amounttoback, pricetolay, amounttolay, b365_odd, margintoback, margintolay, timestamp) VALUES(?,?,?,?,?,?,?,?,?,?)");
			preparedStatement.setString(1, price.bf_marketId);
			preparedStatement.setInt(2, price.runnerId);
			preparedStatement.setFloat(3, price.priceToBack);
			preparedStatement.setFloat(4, price.amountToBack);
			preparedStatement.setFloat(5, price.priceToLay);
			preparedStatement.setFloat(6, price.amountToLay);
			preparedStatement.setFloat(7, price.b365_odd);
			preparedStatement.setFloat(8, price.marginToBack);
			preparedStatement.setFloat(9, price.marginToLay);
			preparedStatement.setTimestamp(10, new Timestamp(new Date().getTime()));
			preparedStatement.execute();
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static String cleanupString(String string) {
		return string.toLowerCase().replace("/", " ").replace("-", " ").replace(" fc ", " ").replace(" sc ", " ")
				.replace(" MIA ", " Miami ").replace("MIL", " Milwaukee ");
	}
	
	public ArrayList<BetFairMarketPrice> getMarketPricesWithMargin(float margin) {
		ArrayList<BetFairMarketPrice> prices = new ArrayList<BetFairMarketPrice>();
		try {
			Connection connection = DriverManager.getConnection(url, login, password);
			PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM bf_marketprice WHERE marginToBack >= ? or marginToLay >= ?");
			preparedStatement.setFloat(1, margin);
			preparedStatement.setFloat(2, margin);
			ResultSet resultSet = preparedStatement.executeQuery();			
			while (resultSet.next()) {
				String bf_marketId = resultSet.getString("bf_marketId");
				int runnerId = resultSet.getInt("runnerId");
				float priceToBack = resultSet.getFloat("priceToBack");
				float amountToBack = resultSet.getFloat("amountToBack");
				float priceToLay = resultSet.getFloat("priceToLay");
				float amountToLay = resultSet.getFloat("amountToLay");
				float b365_odd = resultSet.getFloat("b365_odd");
				float marginToBack = resultSet.getFloat("marginToBack");
				float marginToLay = resultSet.getFloat("marginToLay");
				BetFairMarketPrice marketPrice = new BetFairMarketPrice(bf_marketId, runnerId, priceToBack, amountToBack, priceToLay, amountToLay, b365_odd, marginToBack, marginToLay);
				prices.add(marketPrice);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return prices;
	}
	
	public Bet365Market getB365MarketWithBFMarketId(String bf_marketId) {
		Bet365Market market = null;
		try {
			Connection connection = DriverManager.getConnection(url, login, password);
			PreparedStatement preparedStatement = connection
					.prepareStatement("SELECT * FROM b365_market WHERE bf_marketId = ?");
			preparedStatement.setString(1, bf_marketId);
			ResultSet resultSet = preparedStatement.executeQuery();
			while (resultSet.next()) {
				String categoryName = resultSet.getString("category");
				String name = resultSet.getString("name");
				Date createDate = resultSet.getDate("createDate");
				String id = resultSet.getString("id");
				boolean useToPlay = resultSet.getBoolean("useToPlay");
				String status = resultSet.getString("status");
				
				market = new Bet365Market(categoryName, 0, name, id, createDate, bf_marketId);
				market.useToPlay = useToPlay;
				market.status = status;
			}
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return market;
	}
	
	
	
	
	
}
