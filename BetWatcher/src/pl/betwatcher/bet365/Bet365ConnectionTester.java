package pl.betwatcher.bet365;

import java.util.Date;


public class Bet365ConnectionTester {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Bet365Market market = new Bet365Market("soccer", 1, "1-1-5-24880584-2-0-0-1-1-0-0-0-0-0-1-0-0", "", null, null);
		int interval = 90;
		while (interval>0) {
			Date startDate = new Date();
			int i =1;
			while (new Date().getTime() - startDate.getTime() < 600000) {
				System.out.println(new Date() + " ; " + i++ + " ; " + interval);
				Bet365NetworkManager.sharedInstance().getMarketOdds(market);
				try {
					Thread.sleep(interval);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			interval-= 10;
		}
	}
}
