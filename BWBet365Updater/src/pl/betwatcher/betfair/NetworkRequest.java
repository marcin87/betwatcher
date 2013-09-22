package pl.betwatcher.betfair;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;

import org.w3c.dom.Element;

public class NetworkRequest {
	private SOAPMessage message = null;
	private String endpoint = null;
	public SOAPElement requestElement = null;
	
	public NetworkRequest(String method, String anEndpoint) throws SOAPException {
		endpoint = anEndpoint;
		message = MessageFactory.newInstance().createMessage();
		SOAPHeader header = message.getSOAPHeader();
		header.detachNode();

		SOAPBody body = message.getSOAPBody();
		SOAPBodyElement methodElement = body.addBodyElement(new QName(method));
		requestElement = methodElement.addChildElement("request");
		
		SOAPElement localeElement = requestElement.addChildElement("locale");
		localeElement.addTextNode("en");
	}
	
	public void setSessionToken(String sessionToken) throws SOAPException {
		SOAPElement headerElement = requestElement.addChildElement("header");
		SOAPElement clientStampElement = headerElement.addChildElement("clientStamp");
		clientStampElement.addTextNode("0");
		SOAPElement sessionTokenElement = headerElement.addChildElement("sessionToken");
		sessionTokenElement.addTextNode(sessionToken);
	}
	
	public Element call() throws SOAPException, IOException {
		
//		System.out.println(getXmlFromSOAPMessage(message));
		getXmlFromSOAPMessage(message);
		
		SOAPConnection connection = SOAPConnectionFactory.newInstance().createConnection();
		SOAPMessage response = connection.call(message, endpoint);
		connection.close();

//		System.out.println(getXmlFromSOAPMessage(response));
		return response.getSOAPPart().getDocumentElement();
	}
	

	public static String getXmlFromSOAPMessage(SOAPMessage msg) throws SOAPException, IOException {
		ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
		msg.writeTo(byteArrayOS);
		return new String(byteArrayOS.toByteArray());
	}
}
