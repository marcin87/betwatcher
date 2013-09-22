package pl.betwatcher.utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

	public static ArrayList<String> AllMatches(String string, String regex) {
		ArrayList<String> matches = new ArrayList<String>();
		Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
		Matcher matcher = pattern.matcher(string);
		while (matcher.find()) {
			matches.add(matcher.group());
		}
		return matches;
	}
	
	public static void Log(String string) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		System.out.println(format.format(new Date()) + " > " + string);
	}
}
