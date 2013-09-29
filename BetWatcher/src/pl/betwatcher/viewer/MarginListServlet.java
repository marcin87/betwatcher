package pl.betwatcher.viewer;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import pl.betwatcher.database.*;
import pl.betwatcher.betfair.*;

/**
 * Servlet implementation class MarginListServlet
 */
@WebServlet("/MarginListServlet")
public class MarginListServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public MarginListServlet() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String bf_marketId = request.getParameter("bf_marketId");
		String marginString = request.getParameter("margin");
		if (marginString == null)
			marginString = "0.1";
		float margin = new Float(marginString);
		ArrayList<HashMap<String, String>> prices = DataManager.sharedInstance().getMarketPrices(bf_marketId, margin);
		
		String json = null;
		json = new Gson().toJson(prices);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(json);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
