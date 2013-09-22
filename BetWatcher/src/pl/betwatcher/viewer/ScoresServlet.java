package pl.betwatcher.viewer;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import pl.betwatcher.database.DataManager;

import com.google.gson.Gson;

/**
 * Servlet implementation class Scores
 */
@WebServlet("/ScoresServlet")
public class ScoresServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public ScoresServlet() {
		super();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		String id = request.getParameter("id");
		String market = request.getParameter("market");
		ArrayList<String> scores = DataManager.sharedInstance().getScoresForMarketSinceDate(market, id);

		String json = "";
		if (scores.size() > 0) {
			json = scores.toString();
		}
		response.setContentType("text/plain; charset=ISO-8859-2");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(json);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		String score = request.getParameter("score");
		String market = request.getParameter("market");
		String diff = request.getParameter("diff");
		System.out.println("diff " + diff);
		String[] teams = market.split(" vs. ");
		String[] scoreParts = score.split("_");
		String team = teams[Integer.parseInt(scoreParts[0]) - 1];
		
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String timestamp = df.format(new Date());
		DataManager.sharedInstance().saveScore(market, team, Integer.parseInt(scoreParts[1]), timestamp);
	}

}
