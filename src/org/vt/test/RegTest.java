package org.vt.test;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class RegTest {
	
public static void main(String[] args) throws IOException {
	String s = "/video/the-mayor-of-london-on-barclays-ceo-the-olympics-OAbDPL5VTLOkM0HmWQ8cIg.html";
	String regEx = "video";
	
	Pattern pattern = Pattern.compile(regEx);
	Matcher matcher = pattern.matcher(s);
	if (matcher.find())
	{
		System.out.println(matcher.group());
	}
	
}
}
