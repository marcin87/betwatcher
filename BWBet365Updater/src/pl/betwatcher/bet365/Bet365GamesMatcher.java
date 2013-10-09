package pl.betwatcher.bet365;
import java.util.ArrayList;

import pl.betwatcher.betfair.EventType;
import pl.betwatcher.database.DataManager;
import pl.betwatcher.utils.Utils;


public class Bet365GamesMatcher {

	public static void main(String[] args) {
		ArrayList<EventType> eventTypesToPlay = DataManager.sharedInstance().getEventTypesToPlay();
		ArrayList<Bet365Market> markets = Bet365NetworkManager.sharedInstance().getLiveMarkets(eventTypesToPlay);
		Utils.Log("liveMarkets: " + markets.size());
		int matchedCount = 0;
		for (Bet365Market market : markets) {
			boolean newCreated = DataManager.sharedInstance().saveOrUpdateB365Market(market);
//			if (newCreated) {
				matchedCount += DataManager.sharedInstance().matchMarket(market)?1:0;
//			}
		}
		Utils.Log("matchedCount: " + matchedCount);
	}
}
