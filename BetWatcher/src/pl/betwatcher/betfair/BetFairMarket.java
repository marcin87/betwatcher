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

	public BetFairMarket(String id, String name, Date startDate, int runner1Id,
			int runner2Id, String runner1Name, String runner2Name,
			String menuPath, int baseRate, int eventTypeId) {
		this.id = id;
		this.name = name;
		this.startDate = startDate;
		this.runner1Id = runner1Id;
		this.runner2Id = runner2Id;
		this.runner1Name = runner1Name;
		this.runner2Name = runner2Name;
		this.menuPath = menuPath;
		this.baseRate = baseRate;
		this.eventTypeId = eventTypeId;
	}

	public BetFairMarket(String id, String name) {
		this.id = id;
		this.name = name;
	}

	public String toString() {
		return "{" + id + ";" + name + ";" + startDate + ";" + runner1Name
				+ ";" + runner2Name + "}";
	}
}
