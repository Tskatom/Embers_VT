package org.vt.finance;

/**
 * This program is used to scrape archive news from bloomberg. The result will be a json format file and each file for per month.
 * The filename will be as yyyy-mm_bloombergnews.txt, the format for json are as below:
 * [{"id":xxx, }]
 * 
 * 
 * 
 */
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.Properties;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.vt.common.ScrapingBloombergTool;
import org.vt.entity.Article;

import com.google.gson.Gson;

import de.l3s.boilerpipe.BoilerpipeProcessingException;

public class BloombergNewsScrape implements Runnable{
	
	private Calendar startPoint;
	private Properties pro;
	
	public BloombergNewsScrape(String start) throws ParseException, FileNotFoundException, IOException
	{
		startPoint = Calendar.getInstance();
		SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
		this.startPoint.setTime(sf.parse(start));
		pro = new Properties();
		pro.load(new FileInputStream("config.property"));
	}
	
	public void getDayNews(String dateStr)
	{
		LinkedList<Article> articles = new LinkedList<Article>();
		//get the day news
		String newsUrl = "http://www.bloomberg.com/archive/news/"+dateStr;
		try{
			Document doc = Jsoup.connect(newsUrl).get();
			Elements content = doc.getElementsByClass("stories");
			//create the daily sequence
			long articleId = Long.valueOf(dateStr.replaceAll("-", ""))*10000;
			
			for(Element el:content)
			{
				Elements links = el.getElementsByTag("a");
				for(Element link:links)
				{
					String linkHref = link.attr("href");
					//To filter the vedio links
					//IF the link is vedio news, then continue
					boolean ifNewsUrl = checkIfNewsUrl(linkHref);
					if(!ifNewsUrl)
					{
						continue;
					}
					String contentUrl = "http://www.bloomberg.com"+linkHref;
					try{
						Article article = ScrapingBloombergTool.getArticle(contentUrl);
						if(article!=null)
						{
							article.setArticelId(String.valueOf(articleId++));
							articles.add(article);
						}
					}catch (BoilerpipeProcessingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						System.out.println("Wrong Url:" + contentUrl);
					}
				}
			}
			
			//output the day news into json files named: yyyy-mm-dd_bloomberg_news.txt
			Gson gson = new Gson();
			String json = gson.toJson(articles);
			String outputPath = pro.getProperty("bloombergNewsOutputPath");
			String fileName = outputPath + "/" + dateStr + "_bloomberNews.txt";
			BufferedWriter bw = new BufferedWriter(new FileWriter(fileName));
			bw.write(json);
			bw.flush();
			bw.close();
		}catch (IOException ex)
		{
			ex.printStackTrace();
			System.out.println("This is a IO Exception:" + dateStr );
		}
		
	}
	
	private boolean checkIfNewsUrl(String url)
	{
		String regEx = "video";
		Pattern pattern = Pattern.compile(regEx);
		if(pattern.matcher(url).find())
		{
			return false;
		}
		return true;
	}
	
	@Override
	public void run() {
		//Iterate to get each day of one month
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		int maxDay = startPoint.getActualMaximum(Calendar.DAY_OF_MONTH);
		System.out.println("Start at " + sdf.format(startPoint.getTime()));
		Calendar currentDay = Calendar.getInstance();
		for(int i=0; i<maxDay;i++)
		{
			//To check if the query day already after current day, then break
			if(startPoint.after(currentDay))
			{
				System.out.println("Break at " + sdf.format(startPoint.getTime()));
				break;
			}
			String dateStr = sdf.format(startPoint.getTime());
			System.out.println(dateStr);
			getDayNews(dateStr);
			startPoint.add(Calendar.DAY_OF_MONTH, 1);
		}
		System.out.println("Finish One Month:" + sdf.format(startPoint.getTime()));
		
	}

	public static void main(String[] args) 
	{
		try{
			Calendar start = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			start.setTime(sdf.parse("2011-10-01"));
			Calendar current = Calendar.getInstance();
			int monthGap = (current.get(Calendar.YEAR) - start.get(Calendar.YEAR))*12 + current.get(Calendar.MONTH) - start.get(Calendar.MONTH);
			//Iterate to get the each month between startpoint and current
			
			for(int i = 0;i<=monthGap;i++)
			{
				BloombergNewsScrape bns;
				bns = new BloombergNewsScrape(sdf.format(start.getTime()));
				Thread thread = new Thread(bns);
				thread.start();
				start.add(Calendar.MONTH, 1);
			}
		}catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
