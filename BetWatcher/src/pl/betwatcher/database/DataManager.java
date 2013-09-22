package pl.betwatcher.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import pl.betwatcher.bet365.Bet365Market;
import pl.betwatcher.bet365.Bet365MarketOdd;
import pl.betwatcher.betfair.BetFairMarketPrice;
import pl.betwatcher.betfair.EventType;
import pl.betwatcher.betfair.BetFairMarket;
import pl.betwatcher.utils.Utils;

public class DataManager {
	private static DataManager sharedInstance = null;

	private DataSource ds;

	public static DataManager sharedInstance() {
		if (sharedInstance == null) {
			sharedInstance = new DataManager();
		}
		return sharedInstance;
	}

	private DataManager() {
		Context initCtx;
		try {
			initCtx = new InitialContext();
			Context envCtx = (Context) initCtx.lookup("java:comp/env");
			ds = (DataSource) envCtx.lookup("jdbc/BetWatcher");
		} catch (NamingException e) {
			e.printStackTrace();
		}

	}

	// BetFair

	public int saveBFActiveEventTypes(ArrayList<EventType> eventTypes) {
		int savedEventsCount = 0;
		try {
			Connection connection = ds.getConnection();
			for (EventType eventType : eventTypes) {
				Statement statement = connection.createStatement();
				ResultSet resultSet = statement
						.executeQuery("SELECT * FROM bf_eventtype WHERE id='"
								+ eventType.id + "'");
				if (!resultSet.next()) {
					savedEventsCount++;
					PreparedStatement preparedStatement = connection
							.prepareStatement("INSERT INTO bf_eventtype(id,name) VALUES (?,?)");
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
			Connection connection = ds.getConnection();
			for (BetFairMarket market : markets) {
				Statement statement = connection.createStatement();
				ResultSet resultSet = statement
						.executeQuery("SELECT * FROM bf_market WHERE id='"
								+ market.id + "'");
				if (!resultSet.next()) {
					savedMarketsCount++;
					PreparedStatement preparedStatement = connection
							.prepareStatement("INSERT INTO bf_market(id,name) VALUES (?,?)");
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
				Connection connection = ds.getConnection();
				PreparedStatement preparedStatement = connection
						.prepareStatement("DELETE from bf_market WHERE id="
								+ market.id);
				preparedStatement.execute();

			} else {
				Connection connection = ds.getConnection();
				String query = "UPDATE bf_market SET startDate=?, runner1Name=?, runner2Name=?, menuPath=?, baseRate=?, runner1Id=?, runner2Id=?, eventTypeId=? WHERE id="
						+ market.id;
				PreparedStatement preparedStatement = connection
						.prepareStatement(query);
				preparedStatement.setTimestamp(1, new Timestamp(
						market.startDate.getTime()));
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
			Connection connection = ds.getConnection();
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement
					.executeQuery("SELECT * FROM bf_market WHERE startDate IS NULL");
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
			Connection connection = ds.getConnection();
			Statement statement = connection.createStatement();
			String query = "SELECT * FROM b365_marketoddOdd WHERE bf_marketid=\""
					+ marketOdd.market.id
					+ "\" and runner=\""
					+ marketOdd.eventName
					+ "\" group by bf_marketid,runner,timestamp order by timestamp desc";
			// System.out.println(query);
			ResultSet resultSet = statement.executeQuery(query);
			if (resultSet.next()) {
				Float odd = resultSet.getFloat("odd");
				if (odd.equals(marketOdd.getOdd())) {
					return false;
				}
			}
			PreparedStatement preparedStatement = connection
					.prepareStatement("INSERT INTO b365_marketodd(bf_marketid,timestamp,runner,odd) VALUES (?,?,?,?)");
			preparedStatement.setString(1, marketOdd.market.id);
			preparedStatement.setTimestamp(2,
					new Timestamp(marketOdd.timestamp.getTime()));
			preparedStatement.setString(3, marketOdd.eventName);
			preparedStatement.setFloat(4, marketOdd.getOdd());
			preparedStatement.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return true;
	}

	public void setMarketInactive(Bet365Market market) {
		try {
			Connection connection = ds.getConnection();
			PreparedStatement preparedStatement = connection
					.prepareStatement("UPDATE b365_market SET status=?, useToPlay=? WHERE id=?");
			preparedStatement.setString(1, "INACTIVE");
			preparedStatement.setBoolean(2, false);
			preparedStatement.setString(3, market.id);
			preparedStatement.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public ArrayList<Bet365Market> getActiveMatchedFreeMarkets() {
		ArrayList<Bet365Market> activeMatchedMarkets = new ArrayList<Bet365Market>();
		try {
			Connection connection = ds.getConnection();
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement
					.executeQuery("SELECT * FROM b365_market "
							+ "WHERE status='ACTIVE' "
							+ "and bf_marketid is not null "
							+ "and (algorithm is null or algorithm='') ");
			while (resultSet.next()) {
				String id = resultSet.getString("id");
				String categoryName = resultSet.getString("category");
				String bf_marketId = resultSet.getString("bf_marketId");
				Bet365Market market = new Bet365Market(categoryName, 0, null,
						id, null, bf_marketId);
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
			Connection connection = ds.getConnection();
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement
					.executeQuery("SELECT * FROM b365_market WHERE id='"
							+ market.id + "'");
			if (!resultSet.next()) {
				newCreated = true;
				PreparedStatement preparedStatement = connection
						.prepareStatement("INSERT INTO b365_market(id, name, category, eventTypeId, createDate, status) VALUES (?,?,?,?,?,?)");
				preparedStatement.setString(1, market.id);
				preparedStatement.setString(2, market.name);
				preparedStatement.setString(3, market.categoryName);
				preparedStatement.setInt(4, market.categoryId);
				preparedStatement.setTimestamp(5, new Timestamp(
						market.timestamp.getTime()));
				preparedStatement.setString(6, "ACTIVE");
				preparedStatement.execute();
			} else {
				PreparedStatement preparedStatement = connection
						.prepareStatement("UPDATE b365_market SET status=? WHERE id=?");
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
			connection = ds.getConnection();
			Statement statement = connection.createStatement();
			SimpleDateFormat format = new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss");
			String query = "select * " + "from bf_market "
					+ "where eventTypeId = " + market.categoryId + " "
					+ "and ABS(TIMESTAMPDIFF(MINUTE, startDate, '"
					+ format.format(market.timestamp) + "'))<100 ";
			ResultSet resultSet = statement.executeQuery(query);
			while (resultSet.next()) {
				String id = "" + resultSet.getInt("id");
				String bf_runner1 = DataManager.cleanupString(" "
						+ resultSet.getString("runner1Name") + " ");
				String bf_runner2 = DataManager.cleanupString(" "
						+ resultSet.getString("runner2Name") + " ");

				String[] bf_runner1Elements = bf_runner1.trim().split(" ");
				String[] bf_runner2Elements = bf_runner2.trim().split(" ");

				float ratio = 0;
				for (String string : bf_runner1Elements) {
					string.trim();
					ratio += Utils.AllMatches(runners, " " + string + " ")
							.size();
				}
				for (String string : bf_runner2Elements) {
					string.trim();
					ratio += Utils.AllMatches(runners, " " + string + " ")
							.size();
				}
				if (ratio > 0) {
					ratio /= (bf_runner1Elements.length + bf_runner2Elements.length);
				}
				if (ratio > 0.6) {
					matched = true;
					connection = ds.getConnection();
					query = "UPDATE b365_market SET BF_MARKETID=" + id
							+ " WHERE id='" + market.id + "'";
					PreparedStatement preparedStatement = connection
							.prepareStatement(query);
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
			Connection connection = ds.getConnection();
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement
					.executeQuery("SELECT * FROM bf_eventtype WHERE useToPlay is true");
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

	public void updateB365MarketForAlgorithm(Bet365Market market,
			String algorithmName) {
		try {
			Connection connection = ds.getConnection();
			PreparedStatement preparedStatement = connection
					.prepareStatement("UPDATE b365_market SET algorithm=? WHERE id=?");
			preparedStatement.setString(1, algorithmName);
			preparedStatement.setString(2, market.id);
			preparedStatement.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void saveBFMarketPrice(BetFairMarketPrice price) {
		try {
			Connection connection = ds.getConnection();
			PreparedStatement preparedStatement = connection
					.prepareStatement("INSERT INTO bf_marketprice(bf_marketid, runnerid, pricetoback, amounttoback, pricetolay, amounttolay, b365_odd, margintoback, margintolay, timestamp) VALUES(?,?,?,?,?,?,?,?,?,?)");
			preparedStatement.setString(1, price.bf_marketId);
			preparedStatement.setInt(2, price.runnerId);
			preparedStatement.setFloat(3, price.priceToBack);
			preparedStatement.setFloat(4, price.amountToBack);
			preparedStatement.setFloat(5, price.priceToLay);
			preparedStatement.setFloat(6, price.amountToLay);
			preparedStatement.setFloat(7, price.b365_odd);
			preparedStatement.setFloat(8, price.marginToBack);
			preparedStatement.setFloat(9, price.marginToLay);
			preparedStatement.setTimestamp(10,
					new Timestamp(new Date().getTime()));
			preparedStatement.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static String cleanupString(String string) {
		return string.toLowerCase().replace("/", " ").replace("-", " ")
				.replace(" fc ", " ").replace(" sc ", " ")
				.replace(" MIA ", " Miami ").replace("MIL", " Milwaukee ");
	}

	public ArrayList<HashMap<String, String>> getMarketPricesWithMargin(
			float margin) {
		ArrayList<HashMap<String, String>> prices = new ArrayList<HashMap<String, String>>();
		try {
			Connection connection = ds.getConnection();
			PreparedStatement preparedStatement = connection
					.prepareStatement("SELECT m.menuPath, p.marginToBack, p.marginToLay, p.timestamp "
							+ "FROM bf_marketprice p "
							+ "JOIN bf_market m on m.id=p.bf_marketId "
							+ "WHERE p.marginToBack >= ? or p.marginToLay >= ? "
							+ "ORDER BY m.id, p.timestamp desc");
			preparedStatement.setFloat(1, margin);
			preparedStatement.setFloat(2, margin);
			ResultSet resultSet = preparedStatement.executeQuery();
			while (resultSet.next()) {
				String menuPath = resultSet.getString("menuPath");
				String timestamp = "" + resultSet.getTimestamp("timestamp");
				String marginToBack = "" + resultSet.getFloat("marginToBack");
				String marginToLay = "" + resultSet.getFloat("marginToLay");
				HashMap<String, String> result = new HashMap<String, String>();
				result.put("menuPath", menuPath);
				result.put("timestamp", timestamp);
				result.put("marginToBack", marginToBack);
				result.put("marginToLay", marginToLay);
				prices.add(result);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return prices;
	}

	public BetFairMarket getMarketWithId(String bf_marketId) {
		BetFairMarket market = null;
		try {
			Connection connection = ds.getConnection();
			PreparedStatement preparedStatement = connection
					.prepareStatement("SELECT * FROM bf_market WHERE id = ?");
			preparedStatement.setString(1, bf_marketId);
			ResultSet resultSet = preparedStatement.executeQuery();
			while (resultSet.next()) {
				String name = resultSet.getString("name");
				Date startDate = resultSet.getDate("startDate");
				int runner1Id = resultSet.getInt("runner1Id");
				int runner2Id = resultSet.getInt("runner2Id");
				String runner1Name = resultSet.getString("runner1Name");
				String runner2Name = resultSet.getString("runner2Name");
				String menuPath = resultSet.getString("menuPath");
				int baseRate = resultSet.getInt("baseRate");
				int eventTypeId = resultSet.getInt("eventTypeId");
				market = new BetFairMarket(bf_marketId, name, startDate,
						runner1Id, runner2Id, runner1Name, runner2Name,
						menuPath, baseRate, eventTypeId);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return market;
	}

	public void saveScore(String market, String team, int score,
			String timestamp) {
		try {
			Context initCtx = new InitialContext();
			Context envCtx = (Context) initCtx.lookup("java:comp/env");
			DataSource ds = (DataSource) envCtx.lookup("jdbc/BetWatcher");
			Connection connection = ds.getConnection();
			PreparedStatement preparedStatement = connection
					.prepareStatement("INSERT INTO score(market, team, score, timestamp) VALUES(?,?,?,?)");
			preparedStatement.setString(1, market);
			preparedStatement.setString(2, team);
			preparedStatement.setInt(3, score);
			preparedStatement.setString(4, timestamp);
			preparedStatement.execute();
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (NamingException e) {
			e.printStackTrace();
		}
	}

	public ArrayList<String> getScoresForMarketSinceDate(String market,
			String lastId) {
		ArrayList<String> scores = new ArrayList<String>();
		try {
			Context initCtx = new InitialContext();
			Context envCtx = (Context) initCtx.lookup("java:comp/env");
			DataSource ds = (DataSource) envCtx.lookup("jdbc/BetWatcher");
			Connection connection = ds.getConnection();
			PreparedStatement preparedStatement = connection
					.prepareStatement("SELECT * FROM score WHERE id > ? and market = ?");
			preparedStatement.setString(1, lastId);
			preparedStatement.setString(2, market);
			ResultSet resultSet = preparedStatement.executeQuery();
			while (resultSet.next()) {
				int id = resultSet.getInt("id");
				String team = resultSet.getString("team");
				int score = resultSet.getInt("score");
				Date timestamp = resultSet.getTimestamp("timestamp");
				scores.add(id + " | " + timestamp + " | " + team + " | "
						+ score);
			}
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (NamingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return scores;
	}

	public ArrayList<HashMap<String, String>> get365Markets() {
		ArrayList<HashMap<String, String>> markets = new ArrayList<HashMap<String, String>>();
		try {
			Context initCtx = new InitialContext();
			Context envCtx = (Context) initCtx.lookup("java:comp/env");
			DataSource ds = (DataSource) envCtx.lookup("jdbc/BetWatcher");
			Connection connection = ds.getConnection();
			PreparedStatement preparedStatement = connection
					.prepareStatement("" 
							+ "SELECT * " + "FROM b365_market "
							+ "WHERE bf_marketId is not null "
							+ "AND createDate > DATE(NOW()-INTERVAL 1 DAY) "
							+ "ORDER BY createDate desc");
			ResultSet resultSet = preparedStatement.executeQuery();
			while (resultSet.next()) {
				String name = resultSet.getString("name");
				String category = resultSet.getString("category");
				String createDate = "" + resultSet.getTimestamp("createDate");
				String status = resultSet.getString("status");
				String marketId = resultSet.getString("bf_marketId");
				String algorithm = resultSet.getString("algorithm");
				String useToPlay = resultSet.getBoolean("useToPlay")?"YES":"NO";
				HashMap<String, String> market = new HashMap<String, String>();
				market.put("name", name);
				market.put("category", category);
				market.put("createDate", createDate);
				market.put("status", status);
				market.put("bf_marketId", marketId);
				market.put("algorithm", algorithm);
				market.put("useToPlay", useToPlay);
				markets.add(market);
			}
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (NamingException e) {
			e.printStackTrace();
		}
		return markets;
	}

	public void set365MarketToPlay(String id, boolean useToPlay) {
		try {
			Context initCtx = new InitialContext();
			Context envCtx = (Context) initCtx.lookup("java:comp/env");
			DataSource ds = (DataSource) envCtx.lookup("jdbc/BetWatcher");
			Connection connection = ds.getConnection();
			PreparedStatement preparedStatement = connection
					.prepareStatement("" 
							+ "UPDATE b365_market "
							+ "SET useToPlay = ? " 
							+ "WHERE bf_marketid = ?");
			preparedStatement.setBoolean(1, useToPlay);
			preparedStatement.setString(2, id);
			preparedStatement.execute();
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (NamingException e) {
			e.printStackTrace();
		}
	}
}
