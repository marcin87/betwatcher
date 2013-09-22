package pl.betwatcher.betfair;

public class BetFairMarketPrice {
	
	public String bf_marketId;
	public int runnerId;
	public float priceToBack;
	public float amountToBack;
	public float priceToLay;
	public float amountToLay;
	public float b365_odd;
	public float marginToBack;
	public float marginToLay;
	
	public BetFairMarketPrice(String bf_marketId, int runnerId, float priceToBack, float amountToBack, float priceToLay, float amountToLay) {
		this.bf_marketId = bf_marketId;
		this.runnerId = runnerId;
		this.priceToBack = priceToBack;
		this.amountToBack = amountToBack;
		this.priceToLay = priceToLay;
		this.amountToLay = amountToLay;
	}
	
	public BetFairMarketPrice(String bf_marketId, int runnerId, float priceToBack, float amountToBack, float priceToLay, float amountToLay, float b365_odd, float marginToBack, float marginToLay) {
		this.bf_marketId = bf_marketId;
		this.runnerId = runnerId;
		this.priceToBack = priceToBack;
		this.amountToBack = amountToBack;
		this.priceToLay = priceToLay;
		this.amountToLay = amountToLay;
		this.b365_odd = b365_odd;
		this.marginToBack = marginToBack;
		this.marginToLay = marginToLay;
	}
	
	public String toString() {
		return "{" + bf_marketId + ", " + runnerId + ", " + priceToBack + ", " + amountToBack + ", " + priceToLay + ", " + amountToLay +
				", " + b365_odd + ", " + marginToBack + ", " + marginToLay + "}";
	}
}
