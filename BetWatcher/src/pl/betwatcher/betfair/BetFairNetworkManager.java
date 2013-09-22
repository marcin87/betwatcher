package pl.betwatcher.betfair;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StringWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.DOMException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import pl.betwatcher.utils.Utils;


public class BetFairNetworkManager {
	private static final String kGlobralServiceURL = "https://api.betfair.com/global/v3/BFGlobalService";
	private static final String kExchangeServiceURL = "https://api.betfair.com/exchange/v5/BFExchangeService";
	private String sessionToken = null;
	private static BetFairNetworkManager sharedInstance = null;

	public static BetFairNetworkManager sharedInstance() {	
		if (sharedInstance == null) {
			sharedInstance = new BetFairNetworkManager();
		}
		return sharedInstance;
	}

	private BetFairNetworkManager() {
		Boolean shouldLogin = true;
		if (deserializeSession()) {
			if (keepAlive()) {
				shouldLogin = false;
				Utils.Log("Session keep alive: " + sessionToken);
			} 
		}
		if (shouldLogin) {
			this.login("wlodarcm", "wlodarcm1");
			Utils.Log("User logged in: " + sessionToken);
			serializeSession();
		}
		nodeToString(null);
	}
	
	private void serializeSession() {
		try {
			FileOutputStream fout = new FileOutputStream("session.obj");
			ObjectOutputStream oos = new ObjectOutputStream(fout);   
			oos.writeObject(sessionToken);
			oos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private boolean deserializeSession() {
		boolean sessionLoaded = false;
		try {
			FileInputStream fin = new FileInputStream("session.obj");
			ObjectInputStream ois = new ObjectInputStream(fin);
			sessionToken = (String) ois.readObject();
			sessionLoaded = true;
			fin.close();
			ois.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return sessionLoaded;
	}

	private Boolean login(String login, String password) {
		try {
			NetworkRequest request = new NetworkRequest("login", kGlobralServiceURL);

			SOAPElement ipAddressElement = request.requestElement.addChildElement("ipAddress");
			ipAddressElement.addTextNode("0");
			SOAPElement locationId = request.requestElement.addChildElement("locationId");
			locationId.addTextNode("0");
			SOAPElement passwordElement = request.requestElement.addChildElement("password");
			passwordElement.addTextNode(password);
			SOAPElement productIdElement = request.requestElement.addChildElement("productId");
			productIdElement.addTextNode("82");
			SOAPElement usernameElement = request.requestElement.addChildElement("username");
			usernameElement.addTextNode(login);
			SOAPElement vendorSoftwareIdElement = request.requestElement.addChildElement("vendorSoftwareId");
			vendorSoftwareIdElement.addTextNode("0");

			Node tokenNode = findNode(request.call(), "sessionToken", true, true);
			System.out.println("sessionToken: " + tokenNode.getTextContent());
			sessionToken = tokenNode.getTextContent();

		} catch (SOAPException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	private Boolean keepAlive() {
		Boolean keepAlive = false;
		try {
			NetworkRequest request = new NetworkRequest("keepAlive", kGlobralServiceURL);
			request.setSessionToken(sessionToken);
			String result = findNode(request.call(), "errorCode", true, true).getTextContent();
			if (result.equals("OK")) {
				keepAlive = true;
			}
		} catch (SOAPException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return keepAlive;
	}
	
	public ArrayList<EventType> getActiveEventTypes() {
		ArrayList<EventType> activeEventTypes = new ArrayList<EventType>();
		try {
			NetworkRequest request = new NetworkRequest("getActiveEventTypes", kGlobralServiceURL);
			request.setSessionToken(sessionToken);
			
			Node resultNode = findNode(request.call(), "eventTypeItems", true, true);
			NodeList list = resultNode.getChildNodes();
			for (int i = 0; i < list.getLength(); i++) {
				EventType eventType = new EventType(
						findNode(list.item(i), "id", true, true).getTextContent(), 
						findNode(list.item(i), "name", true, true).getTextContent());
				activeEventTypes.add(eventType);
			}
		} catch (SOAPException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return activeEventTypes;
	}
	
	public ArrayList<BetFairMarket> getAllMarkets(ArrayList<EventType> eventTypes) {
		ArrayList<BetFairMarket> markets = new ArrayList<BetFairMarket>();
		try {
			NetworkRequest request = new NetworkRequest("getAllMarkets", kExchangeServiceURL);
			request.setSessionToken(sessionToken);
			
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T00:00:00.000Z'");
			Date fromDate = new Date();
			Date toDate = new Date(fromDate.getTime() + (1000*60*60*24*2)); 
			
			SOAPElement fromDateElement = request.requestElement.addChildElement("fromDate");
			fromDateElement.setTextContent(format.format(fromDate));
			SOAPElement toDateElement = request.requestElement.addChildElement("toDate");
			toDateElement.setTextContent(format.format(toDate));

			if (eventTypes != null) {
				SOAPElement eventTypeIdsElement = request.requestElement.addChildElement("eventTypeIds");
				for (EventType eventType : eventTypes) {
					SOAPElement eventTypeIdElement = eventTypeIdsElement.addChildElement("int");
					eventTypeIdElement.setTextContent(eventType.id);
				}
			}
			
			Node resultNode = findNode(request.call(), "marketData", true, true);
			ArrayList<String> marketsId = Utils.AllMatches(resultNode.getTextContent(), ":.*?~.*?~");
			
			for (String marketId : marketsId) {
				String id = marketId.substring(1, marketId.indexOf('~'));
				String name = marketId.substring(marketId.indexOf('~')+1, marketId.length()-1);
				if (name.equals("Match Odds") || name.equals("Moneyline")) {
					BetFairMarket market = new BetFairMarket(id, name); 
					markets.add(market);
				}
			}
		} catch (SOAPException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return markets;
	}

	public BetFairMarket getMarketDetails(BetFairMarket market) {
		try {
			NetworkRequest request = new NetworkRequest("getMarket", kExchangeServiceURL);
			request.setSessionToken(sessionToken);
			
			SOAPElement marketIdElement = request.requestElement.addChildElement("marketId");
			marketIdElement.setTextContent(market.id);
			
			Node resultNode = findNode(request.call(), "market", true, true);
			market.menuPath = findNode(resultNode, "menuPath", true, true).getTextContent();
			
			market.eventTypeId = Integer.parseInt(findNode(resultNode, "eventTypeId", true, true).getTextContent());
			
			String status = findNode(resultNode, "marketStatus", true, true).getTextContent();
			if (!status.equals("CLOSED")) {
				Node runnersNode = findNode(resultNode, "runners", true, true);
				NodeList runners = runnersNode.getChildNodes();
				market.runner1Name = findNode(runners.item(0), "name", true, true).getTextContent();
				market.runner2Name = findNode(runners.item(1), "name", true, true).getTextContent();
				market.runner1Id = Integer.parseInt(findNode(runners.item(0), "selectionId", true, true).getTextContent());
				market.runner2Id = Integer.parseInt(findNode(runners.item(1), "selectionId", true, true).getTextContent());

				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
				format.setTimeZone(TimeZone.getTimeZone("GMT"));
				market.startDate = format.parse(findNode(resultNode, "marketTime", true, true).getTextContent());
			}
 
		} catch (SOAPException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (DOMException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		return market;
	}
	
	public ArrayList<BetFairMarketPrice> getMarketPrices(String marketId) {
		ArrayList<BetFairMarketPrice> prices = new ArrayList<BetFairMarketPrice>();

		try {
			NetworkRequest request = new NetworkRequest("getMarketPrices", kExchangeServiceURL);
			request.setSessionToken(sessionToken);
			
			SOAPElement marketIdElement = request.requestElement.addChildElement("marketId");
			marketIdElement.setTextContent("" + marketId);
			
			SOAPElement currencyElement = request.requestElement.addChildElement("currencyCode");
			currencyElement.setTextContent("EUR");
			
			Node marketPricesNode = findNode(request.call(), "runnerPrices", true, true);
			NodeList marketPrices = marketPricesNode.getChildNodes(); 
			for (int i=0; i<marketPrices.getLength(); ++i) {
				Node marketPriceNode = marketPrices.item(i);
				int runnerId = Integer.parseInt(findNode(marketPriceNode, "selectionId", true, true).getTextContent());

				float priceToBack = -9999;
				float amountToBack = 0;
				Node bestPriceToBackNode = findNode(marketPriceNode, "bestPricesToBack", false, true).getFirstChild();
				if (bestPriceToBackNode!=null) {
					priceToBack = Float.parseFloat(findNode(bestPriceToBackNode, "price", false, true).getTextContent());
					amountToBack = Float.parseFloat(findNode(bestPriceToBackNode, "amountAvailable", false, true).getTextContent());
				}
				
				float priceToLay = 9999;
				float amountToLay = 0;
				Node bestPriceToLayNode = findNode(marketPriceNode, "bestPricesToLay", false, true).getFirstChild();
				if (bestPriceToLayNode!=null) {
					priceToLay = Float.parseFloat(findNode(bestPriceToLayNode, "price", false, true).getTextContent());
					amountToLay = Float.parseFloat(findNode(bestPriceToLayNode, "amountAvailable", false, true).getTextContent());
				}
				
				BetFairMarketPrice marketPrice = new BetFairMarketPrice(marketId, runnerId, priceToBack, amountToBack, priceToLay, amountToLay);
				prices.add(marketPrice);
			}
			
		} catch (SOAPException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return prices;
	}
	
	
	
	private String nodeToString(Node node) {
		if (node==null)
			return null;
		StringWriter sw = new StringWriter();
		try {
			Transformer t = TransformerFactory.newInstance().newTransformer();
			t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			t.setOutputProperty(OutputKeys.INDENT, "yes");
			t.transform(new DOMSource(node), new StreamResult(sw));
		} catch (TransformerException te) {
			System.out.println("nodeToString Transformer Exception");
		}
		return sw.toString();
	}
	
	public static Node findNode(Node root, String elementName, boolean deep, boolean elementsOnly) {
		//Check to see if root has any children if not return null
		if (!(root.hasChildNodes()))
			return null;

		//Root has children, so continue searching for them
		Node matchingNode = null;
		String nodeName = null;
		Node child = null;

		NodeList childNodes = root.getChildNodes();
		int noChildren = childNodes.getLength();
		for (int i = 0; i < noChildren; i++) {
			if (matchingNode == null) {
				child = childNodes.item(i);
				nodeName = child.getNodeName();
				if ((nodeName != null) & (nodeName.equals(elementName)))
					return child;
				if (deep)
					matchingNode = findNode(child, elementName, deep, elementsOnly);
			} else
				break;
		}

		if (!elementsOnly) {
			NamedNodeMap childAttrs = root.getAttributes();
			noChildren = childAttrs.getLength();
			for (int i = 0; i < noChildren; i++) {
				if (matchingNode == null) {
					child = childAttrs.item(i);
					nodeName = child.getNodeName();
					if ((nodeName != null) & (nodeName.equals(elementName)))
						return child;
				} else
					break;
			}
		}
		return matchingNode;
	}

}
