package org.vt.finance;

/**
 * This program used to collect the Currency exchange Data from Bloomberg. Taking currency code list from config.property
 * and output the index information to /embers/data-collector. 
 * <p>
 * The output format would be like as
 * currency current_value previous_close open_value update_time insert_time
 * <p>
 * each record for one line and each day's data would be stored in a individual file named as currency_yyyy_mm_dd.txt
 * @author wei
 * @version 2012-7-10
 */
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class CurrencyDataCollector {
	
	private Properties pro;
	private BufferedWriter bw;
	private BufferedReader br;
	
	public CurrencyDataCollector()
	{
		pro = new Properties();
		try{
			pro.load(new FileInputStream("config.property"));
			
			//get the StockIndex List file
			String stockIndexList = pro.getProperty("currencyList");
			br = new BufferedReader(new FileReader(stockIndexList));
			
			//get the OutputPath
			String outputPath = pro.getProperty("outPutPath");
			SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
			String currentDay = sf.format(new Date());
			String outputName = outputPath + "/currency_" + currentDay + ".txt";
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputName,true)));
		}
		catch(IOException ex)
		{
			ex.printStackTrace();
		}
		
	}
	
	public void scrapeStockIndex() throws IOException
	{
		//Get the List of Stock Index
		String stockIndex = "";
		//get insert time
		DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		Date date = new Date();
		String queryTime = dateFormat.format(date);
		while((stockIndex=br.readLine())!=null)
		{
			//Construct the quote url
			String url = "http://www.bloomberg.com/quote/"+stockIndex+":CUR";
			String indexQuote = getIndexQuote(stockIndex,url) + "," + queryTime;
			bw.write(indexQuote + "\n");
		}
		bw.flush();
	}
	
	public String getIndexQuote(String stockIndex,String url) throws IOException
	{
		String indexQuote="";
		String currentValue = "";
		String previousCloseValue = "";
		String updateTime = "";
		
		Document doc = Jsoup.connect(url).timeout(60000).get();
		Elements elements = doc.getElementsByClass("ticker_header_currency");
		for(Element el:elements)
		{
			//get current value 
			Elements els = el.getElementsByClass("price");
			for(Element element:els)
			{
				currentValue = element.ownText().trim().replaceAll(",", "");
			}
			
			//get update time
			Elements timeEls = el.getElementsByClass("fine_print");
			for(Element element:timeEls)
			{
				String content = element.text().trim();
				String time="";
				String date="";
				
				String regEx = "\\d{2}:\\d{2}:\\d{2}";
				Pattern pattern = Pattern.compile(regEx);
				Matcher matcher =  pattern.matcher(content);
				while(matcher.find())
				{
					time = matcher.group();
				}
				
				regEx = "\\d{2}/\\d{2}/\\d{4}";
				pattern = Pattern.compile(regEx);
				matcher =  pattern.matcher(content);
				while(matcher.find())
				{
					date = matcher.group();
				}
				
				updateTime = date + " " + time;
			}
		}
		//get previous close value
		Elements preElements = doc.getElementsMatchingOwnText("Previous Close:");
		for(Element el:preElements)
		{
			previousCloseValue = el.nextElementSibling().text().replaceAll(",", ""); 
		}
		
		indexQuote = stockIndex + "," + currentValue + "," +previousCloseValue + "," + updateTime;
		System.out.println(indexQuote);
		
		return indexQuote;
	}
	
	public void close() throws IOException
	{
		bw.close();
		br.close();
	}
	
	public static void main(String[] args) {
		
		CurrencyDataCollector cdc = new CurrencyDataCollector();
		try{
			cdc.scrapeStockIndex();
			cdc.close();
		}
		catch(IOException ex)
		{
			ex.printStackTrace();
		}
		
	}

}
