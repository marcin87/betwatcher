package pl.betwatcher.betfair;

import java.util.ArrayList;

import pl.betwatcher.database.DataManager;
import pl.betwatcher.utils.Utils;

public class BetFairMarketsUpdater {

	public static void main(String[] args) {
		ArrayList<EventType> eventTypesToPlay = DataManager.sharedInstance().getEventTypesToPlay();
		ArrayList<BetFairMarket> markets = BetFairNetworkManager.sharedInstance().getAllMarkets(eventTypesToPlay);
		int saved = DataManager.sharedInstance().saveBFMarkets(markets);
		ArrayList<BetFairMarket> newMarkets = DataManager.sharedInstance().getBFNewMarkets(); 
		for (BetFairMarket market : newMarkets) {
			market = BetFairNetworkManager.sharedInstance().getMarketDetails(market);
			DataManager.sharedInstance().updateBFMarket(market);
			try {
				Thread.sleep(12000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		Utils.Log("new markets saved = " + saved);
	}
}
