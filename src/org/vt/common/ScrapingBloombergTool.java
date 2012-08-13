package org.vt.common;

import java.io.IOException;
import java.net.URL;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.vt.entity.Article;

import de.l3s.boilerpipe.BoilerpipeProcessingException;
import de.l3s.boilerpipe.extractors.ArticleExtractor;

/**
 * 
 * This program is used to scrape one news infromation from input webpage.
 * @author wei
 *@version 2012-7-12
 */

public class ScrapingBloombergTool {
	
	public static Article getArticle(String url) throws BoilerpipeProcessingException
	{
		Article article = new Article();
		try{
			//get Query Time
			SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String queryTime = sf.format(new Date());
			
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
			
			//get content
			String content = ArticleExtractor.INSTANCE.getText(new URL(url));
			
			article.setAuthor(author);
			article.setContent(content);
			article.setPostTime(postTime);
			article.setQueryTime(queryTime);
			article.setSource("Bloomberg");
			article.setRelatedCompany("");
			article.setTitle(title);
			return article;
		}
		catch(IOException ex)
		{
			ex.printStackTrace();
			System.out.println("Error URL Here: " + url);
		}
		return null;
	}

}
