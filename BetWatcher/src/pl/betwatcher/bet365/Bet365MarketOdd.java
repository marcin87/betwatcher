package pl.betwatcher.bet365;

import java.util.Date;

public class Bet365MarketOdd {
	
	public Bet365Market market;
	public String eventType;
	public Date timestamp;
	public String eventName;
	private Float odd;
	
	public Bet365MarketOdd(Bet365Market game, String eventType, String eventName, String odd) {
		this.market = game;
		this.eventType = eventType;
		this.eventName = eventName;
		String[] values = odd.split("/");
		this.odd = 1 + Float.parseFloat(values[0])/Float.parseFloat(values[1]);
		this.timestamp = new Date();
	}
	
	public void setOdd(Float odd) {
		this.odd = (float) (Math.round(odd*1000.0)/1000.0);
	}
	
	public Float getOdd() {
		return this.odd;
	}
	
	public String toString() {
		return "{" + this.market.id + " | " + this.eventName + " | " + this.timestamp + " | " + this.odd + "}";
	}
}
