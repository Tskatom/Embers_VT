package org.vt.datapreprocess;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.vt.entity.Article;
import org.vt.entity.CompanyMatchedArticles;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;


public class BloombergNewsFilter {
	private Properties pro;
	private HashMap<String,CompanyMatchedArticles> comArticles;
	private String matchingRules = "";
	
	public BloombergNewsFilter()
	{
		pro = new Properties();
		try {
				pro.load(new FileInputStream("config.property"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		comArticles = new HashMap<String,CompanyMatchedArticles>();
		
	}
	
	public void filterArticlesByCompany(File file) throws JsonSyntaxException, JsonIOException, FileNotFoundException
	{
		
		Type listType = new TypeToken<LinkedList<Article>>(){}.getType();
		Gson gson = new Gson();
		LinkedList<Article> articles =  gson.fromJson(new BufferedReader(new FileReader(file)),listType);
		for(Article article:articles)
		{
			String content = article.getContent();
			Pattern pattern = Pattern.compile(matchingRules,Pattern.CASE_INSENSITIVE);
			if(content==null)
			{
				continue;
			}
			Matcher matcher = pattern.matcher(content);
			String articleId = article.getArticelId();
			HashMap<String,String> matchedString = new HashMap<String,String>();
			while(matcher.find())
			{
				matchedString.put(matcher.group().toLowerCase(), articleId);
			}
			
			for(String key:matchedString.keySet())
			{
//				System.out.println(matchedString.get(key) + ": " + key);
				comArticles.get(key).addArticle(article);
			}
			
		}

	}
	
	public void scanAllNewsFiles() throws JsonSyntaxException, JsonIOException, IOException
	{
		getAllFiles();
		
		String filePath = "d:/embers/data-collector/bloombergNews";
		File firDir = new File(filePath);
		File[] newsFiles = firDir.listFiles();
		for(File newsFile:newsFiles)
		{
			if(!newsFile.isDirectory())
			{
				System.out.println(newsFile.getName());
				filterArticlesByCompany(newsFile);
			}
		}
		
		//write to file
		String fileName = filePath + "/filterBloombergArray.txt";
		BufferedWriter bw = new BufferedWriter(new FileWriter(fileName));
		Gson gson = new Gson();
		LinkedList<CompanyMatchedArticles> results = new LinkedList<CompanyMatchedArticles>();
		for(String key:comArticles.keySet())
		{
			results.add(comArticles.get(key));
		}
		String json = gson.toJson(results);
		bw.write(json);
		bw.flush();
		bw.close();
	}
	
	public void getAllFiles() throws IOException
	{
		String filePath = pro.getProperty("companyList");
		File[] files = new File(filePath).listFiles();
		for(File file:files)
		{
			if(!file.isDirectory())
			{
				System.out.println(file.getName());
				BufferedReader br = new BufferedReader(new FileReader(file));
				String line = "";
				while((line=br.readLine())!=null)
				{
					String[] comInfo = line.split("\\|");
					String stockIndex = comInfo[0].trim();
					String companyTicker = comInfo[1].trim();
					String companyName = comInfo[2].trim().toLowerCase();
					
					CompanyMatchedArticles cma = new CompanyMatchedArticles();
					cma.setStockIndex(stockIndex);
					cma.setCompanyTicker(companyTicker);
					cma.setCompanyName(companyName);
					comArticles.put(companyName, cma);
				}
				br.close();
			}
		}
		
		int i = 1;
		for(String key:comArticles.keySet())
		{
			String eachRule = "(" + key.replaceFirst("\\.", "\\\\.") + ")";
			if(i!=comArticles.keySet().size())
			{
				eachRule += "|";
			}
			matchingRules += eachRule;
			i++;
		}
		System.out.println("matchingRules=" + matchingRules);
		
	}
	
	public static void main(String[] args) 
	{
		BloombergNewsFilter bnf = new BloombergNewsFilter();
		try {
				bnf.scanAllNewsFiles();
		} catch (JsonSyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonIOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
