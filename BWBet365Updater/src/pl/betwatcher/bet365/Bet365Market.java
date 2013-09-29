package pl.betwatcher.bet365;

import java.util.Date;

public class Bet365Market {

	public String categoryName;
	public int categoryId;
	public String name;
	public String id;
	public Date timestamp;
	public GameType type;
	public String bf_marketId;
	public String status;
	public boolean useToPlay;
	
	
	public Bet365Market(String categoryName, int categoryId, String name, String id, Date timestamp, String bf_marketId) {
		this.categoryName = categoryName;
		this.categoryId = categoryId;
		this.name = name;
		this.id = id;
		this.timestamp = timestamp;
		this.bf_marketId = bf_marketId;
		
		if (categoryName == null) {
			this.type = GameType.GameTypeOther;
		} else if (categoryName.equals("Soccer")) {
			this.type = GameType.GameTypeSoccer;
		} else if (categoryName.equals("Basketball")) {
			this.type = GameType.GameTypeBasketball;
		} else if (categoryName.equals("Tennis")) {
			this.type = GameType.GameTypeTennis;
		} else if (categoryName.equals("Ice Hockey")) {
			this.type = GameType.GameTypeIceHockey;
		} else if (categoryName.equals("Table Tennis")) {
			this.type = GameType.GameTypeTableTennis;
		} else if (categoryName.equals("Volleyball")) {
			this.type = GameType.GameTypeVolleyball;
		} else {
			this.type = GameType.GameTypeOther;
		}
	}
	
	public String toString() {
		return "{"+categoryName+" | "+name+" | "+id+"}";
	}
}
