package pl.betwatcher.betfair;

import java.util.ArrayList;

import pl.betwatcher.bet365.Bet365Market;
import pl.betwatcher.bet365.Bet365NetworkManager;
import pl.betwatcher.bet365.MarketOddsRunnable;
import pl.betwatcher.database.DataManager;

public class BetWatcherAlgorithmOne {

	public static void main(String[] args) {
		// update live markets
		ArrayList<EventType> eventTypesToPlay = DataManager.sharedInstance().getEventTypesToPlay();
		ArrayList<Bet365Market> markets = Bet365NetworkManager.sharedInstance().getLiveMarkets(eventTypesToPlay);
		for (Bet365Market market : markets) {
			DataManager.sharedInstance().saveOrUpdateB365Market(market);
		}
		
		ArrayList<Bet365Market> activeMatchedMarkets = DataManager.sharedInstance().getActiveMatchedFreeMarkets();
		System.out.println("count: " + activeMatchedMarkets.size());
		for (Bet365Market market : activeMatchedMarkets) {
			MarketOddsRunnable marketOdds = new MarketOddsRunnable(market);
			Thread thread = new Thread(marketOdds);
			thread.start();
			break;
		}
	}
}

// market, runner, oddsy, prowizja, 