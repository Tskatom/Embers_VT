package org.vt.finance;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import de.l3s.boilerpipe.BoilerpipeProcessingException;
import de.l3s.boilerpipe.extractors.ArticleExtractor;

/**
 *This program is used to collect the current news related to indicated companies. It scrapes articles from Bloomberg Website
 *and then store into local files. The articles will be stored as XML format with the following style:  
 *<article>
 *<articleID></articleID>
 *<author></author>
 *<company></company>
 *<title></title>
 *<postTime></postTime>
 *<queryTime><queryTime>
 *<content><content> 
 *</article>
 * @author wei
 * @version 2012-7-11
 */

public class GetCurrentNewsRelatedCompany {
	private BufferedReader br;
	private BufferedWriter bw;
	private String queryTime;
	private String companyName;
	private Properties pro;
	private long articleId;
	
	public GetCurrentNewsRelatedCompany() throws FileNotFoundException, IOException
	{
		SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		queryTime = sf.format(new Date());
		companyName = "ALL3";
		pro = new Properties();
		pro.load(new FileInputStream("D:/eclipse/workspace/Embers/config.property"));
		String outputPath = pro.getProperty("outPutPath");
		String fileName = outputPath + "/" + companyName + "_" + queryTime.split(" ")[0] + ".txt";
		bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName,true)));
		articleId = 10000000;
	}
	
	public String getArticle(String url) throws BoilerpipeProcessingException, IOException
	{
		//get title
		Document doc = Jsoup.connect(url).timeout(60000).get();
		Element titleEle = doc.getElementById("disqus_title");
		String title = titleEle.text();
		
		//get posttime
		String postTime = "";
		Elements postTimeEles = doc.getElementsByClass("datestamp");
		for(Element postTimeEle:postTimeEles)
		{
			String longTime = postTimeEle.attr("epoch");
			postTime = new Timestamp(Long.valueOf(longTime)).toString();
		}
		
		//get author
		String author = "";
		Elements authorElements = doc.getElementsByClass("byline");
		for(Element authEle:authorElements)
		{
			author =  authEle.ownText().replaceAll("By", "").replaceAll("-","").replaceAll("and", ",");
		}
		
		String content = ArticleExtractor.INSTANCE.getText(new URL(url));
		
		String article = "<article>\n" + 
				"<articleId>" + articleId++ +"</articleId>\n" +
				"<author>" + author + "</author>\n" +
				"<company>" + this.companyName + "</company>\n" +
				"<title>" + title + "</title>\n" +
				"<postTime>" + postTime + "</postTime>\n" +
				"<queryTime>" + queryTime + "</queryTime>\n" +
				"<content>" + content + "</content>\n" +
				"</article>\n";
		
		return article;
	}
	
	/**
	 * Through input URL, get all the news in that webpages
	 * @param url
	 * @throws IOException
	 * @throws BoilerpipeProcessingException 
	 */
	public void getAllRelatedArticles(String url) throws IOException, BoilerpipeProcessingException
	{
		Document urlListDoc = Jsoup.connect(url).timeout(60000).get();
		Element urlElement = urlListDoc.getElementById("news_tab_company_news_panel"); 
		Elements urls = urlElement.getElementsByTag("a");
		for(Element newsUrl:urls)
		{
			String absUrl = "http://www.bloomberg.com" + newsUrl.attr("href");
			String aritcle = getArticle(absUrl);
			bw.write(aritcle);
			bw.flush();
		}
	}
	
	public void close() throws IOException
	{
		bw.flush();
		bw.close();
	}
	
	public static void main(String[] args) {
		try{
			GetCurrentNewsRelatedCompany gnrc = new GetCurrentNewsRelatedCompany();
			String url = "http://www.bloomberg.com/quote/ALLL3:BS/news#news_tab_company_news";
			gnrc.getAllRelatedArticles(url);
			gnrc.close();
		}
		catch(FileNotFoundException ff)
		{
			ff.printStackTrace();
		}catch( IOException ex)
		{
			ex.printStackTrace();
		} catch (BoilerpipeProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
