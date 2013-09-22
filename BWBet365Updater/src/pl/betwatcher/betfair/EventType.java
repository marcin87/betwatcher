package pl.betwatcher.betfair;

public class EventType {
	public String id = null;
	public String name = null;
	
	public EventType(String id, String name) {
		this.id = id;
		this.name = name;
	}
	
	public String toString() {
		return "{" + id + ";" + name + "}"; 
	}
}
