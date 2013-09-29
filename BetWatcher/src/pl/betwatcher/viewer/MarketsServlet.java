package pl.betwatcher.viewer;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import pl.betwatcher.bet365.Bet365Market;
import pl.betwatcher.database.DataManager;

/**
 * Servlet implementation class MarketsServlet
 */
@WebServlet("/MarketsServlet")
public class MarketsServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public MarketsServlet() {
		super();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		if (request.getParameter("bf_marketId") != null) {
			Bet365Market market = DataManager.sharedInstance()
					.getB365MarketWithBFMarketId(request.getParameter("bf_marketId"));
			String json = new Gson().toJson(market);
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			response.getWriter().write(json);
		} else {
			ArrayList<HashMap<String, String>> prices = DataManager
					.sharedInstance().get365Markets();
			String json = new Gson().toJson(prices);
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			response.getWriter().write(json);
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		String action = request.getParameter("action");
		if (action.equals("useToPlay")) {
			String id = request.getParameter("id");
			boolean useToPlay = Boolean.parseBoolean(request
					.getParameter("useToPlay"));
			DataManager.sharedInstance().set365MarketToPlay(id, useToPlay);
		} else if (action.equals("runProcess")) {
			ProcessBuilder pb = new ProcessBuilder("java", "-jar",
					"/root/betwatcher/BetWatcherAlgorithmOne.jar");
			pb.directory(new File("/usr/lib/jvm/java-1.6.0-openjdk-amd64/bin"));
			pb.redirectErrorStream(true);
			Process process = pb.start();
			BufferedReader br = new BufferedReader(new InputStreamReader(
					process.getInputStream()));
			int ch;
			String responseString = new String();
			while ((ch = br.read()) != -1)
				responseString = responseString + (char) ch;
			br.close();
			System.out.print(responseString);
			try {
				int exitVal = process.waitFor();
				System.out.println("Exit Value: " + exitVal);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} else if (action.equals("activate")) {
			String id = request.getParameter("id");
			String activateString = request.getParameter("activate");
			boolean active = activateString.equals("true") ? true : false;
			DataManager.sharedInstance().setMarketActiveState(id, active);
		} else {
			System.out.println("other");
		}
	}

}
