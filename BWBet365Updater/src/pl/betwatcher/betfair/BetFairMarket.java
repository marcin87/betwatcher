package pl.betwatcher.betfair;

import java.util.Date;

public class BetFairMarket {
	public String id = null;
	public String name = null;
	public Date startDate = null;
	public int runner1Id = 0;
	public int runner2Id = 0;
	public String runner1Name = null;
	public String runner2Name = null;
	public String menuPath = null;
	public int baseRate = 0;
	public int eventTypeId = 0;
	
	public BetFairMarket(String id, String name) {
		this.id = id;
		this.name = name;
	}
	
	public String toString() {
		return "{" + id + ";" + name + ";" + startDate + ";" + runner1Name + ";" + runner2Name + "}";
	}
}
