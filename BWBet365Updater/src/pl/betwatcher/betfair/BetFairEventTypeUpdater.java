package pl.betwatcher.betfair;

import java.util.ArrayList;

import pl.betwatcher.database.DataManager;

public class BetFairEventTypeUpdater {

	public static void main(String[] args) {
		ArrayList<EventType> eventTypes =  BetFairNetworkManager.sharedInstance().getActiveEventTypes();
		DataManager.sharedInstance().saveBFActiveEventTypes(eventTypes);
	}

}
