package pl.betwatcher.bet365;

import java.util.ArrayList;


import pl.betwatcher.betfair.BetFairMarketPrice;
import pl.betwatcher.betfair.BetFairNetworkManager;
import pl.betwatcher.database.DataManager;
import pl.betwatcher.utils.Utils;

public class MarketOddsRunnable implements Runnable {
	public Bet365Market market;

	public MarketOddsRunnable(Bet365Market market) {
		this.market = market;
	}

	public void run() {
		Utils.Log("Thread started");
		boolean shouldStop = false;
		
		// mark market for algorithm one
		DataManager.sharedInstance().updateB365MarketForAlgorithm(market, "algorithmOne");

		while (!shouldStop) {
			ArrayList<Bet365MarketOdd> marketOdds =  Bet365NetworkManager.sharedInstance().getMarketOdds(market);
			boolean useToPlay = DataManager.sharedInstance().getB365MarketWithBFMarketId(market.bf_marketId).useToPlay;
			if (marketOdds == null || marketOdds.size() == 0 || !useToPlay) {
				DataManager.sharedInstance().setMarketInactive(market);
				shouldStop = true;
			} else {
				boolean oddChanged = false;
				for (Bet365MarketOdd marketOdd : marketOdds) {
					oddChanged = DataManager.sharedInstance().saveBet365MarketOdd(marketOdd);
					if (oddChanged) {
					} else {
					}
				}
				// sprawdz czy na bf sa korzystne kursy
				if (oddChanged) {
					ArrayList<BetFairMarketPrice> prices = BetFairNetworkManager.sharedInstance().getMarketPrices(market.bf_marketId);
					for (int i=0; i<prices.size(); ++i) {
						BetFairMarketPrice price = prices.get(i);
						Float b365_odd = marketOdds.get(i).getOdd();
						Float marginToBack = price.priceToBack/b365_odd - 1;
						Float marginToLay = 1- (price.priceToLay/b365_odd);
						price.b365_odd = b365_odd;
						price.marginToBack = marginToBack;
						price.marginToLay = marginToLay;
						DataManager.sharedInstance().saveBFMarketPrice(price);
						Utils.Log("market price saved");
					}
				}
			}
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		Utils.Log("Thread stopped");
		// mark market for algorithm one
		DataManager.sharedInstance().updateB365MarketForAlgorithm(market, null);
		
	}
}
