package org.vt.datapreprocess;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Properties;

import org.vt.entity.Article;
import org.vt.entity.CompanyMatchedArticles;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

public class GetMemberCompany {
	private LatinMarket latinMarket;

	public class Top{
		public String name="Latin American Stock Market";
		public LinkedList<Words> children;
		public Top()
		{
			children = new LinkedList<Words>();
		}
	}
	
	public class Words{
		public String name;
		public LinkedList<Word> children;
		public Words()
		{
			children =  new LinkedList<Word>();
		}
		
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		
	}
	
	
	public class Word{
		public String name;
		public int size;
		
		public Word(String name,int size)
		{
			this.name = name;
			this.size = size;
		}
	
	}
	
	public class LatinMarket{
		private String name="S.A Stock Market";
		private LinkedList<StockMarket> children = new LinkedList<StockMarket>();
	}
	public class StockMarket{
		private String name;
		private LinkedList<Company> children;
		public StockMarket(String name)
		{
			children  = new LinkedList<Company>() ;
			this.name = name;
		}
	}
	public class Company{
		private String name;
		private LinkedList<News> children;
		public Company(String name)
		{
			this.name = name;
			children = new LinkedList<News>();
		}
	}
	public class News{
		private String name;
		public News(String name)
		{
			this.name = name;
		}
	}
	public void iniMarket()
	{
		latinMarket = new LatinMarket();
	}
	
	public  void transferLine2json(File file) throws IOException
	{
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line = "";
		Top top = new Top();
		int i=0;
		Words words = new Words();
		words.setName("0");
		top.children.add(words);
		while((line=br.readLine())!=null)
		{
			if(i!=0 &&i%30==0)
			{
				words = new Words();
				top.children.add(words);
				words.setName(String.valueOf(i));
			}
			System.out.println(line);
			String[] infos = line.split(" ");
			String keyword = infos[0];
			String fre = infos[1];
			Word word = new Word(keyword,Integer.valueOf(fre));
			words.children.add(word);
			i++;
		}
		Gson gson = new Gson();
		String json = gson.toJson(top);
		System.out.println(json);
	}
	
	//Transfer the Stock_Mem Hierarchy to Json file
	public void transferStockMem(File articleFile) throws JsonIOException, JsonSyntaxException, FileNotFoundException
	{
		iniMarket();
		StockMarket merval = new StockMarket("MERVAL");
		StockMarket ibov = new StockMarket("IBOV");
		StockMarket chile65 = new StockMarket("CHILE65");
		StockMarket colcap = new StockMarket("COLCAP");
		StockMarket crsmbct = new StockMarket("CRSMBCT");
		StockMarket mexbol = new StockMarket("MEXBOL");
		StockMarket bvpsbvps = new StockMarket("BVPSBVPS");
		StockMarket igbvl = new StockMarket("IGBVL");
		StockMarket ibvc = new StockMarket("IBVC");
		latinMarket.children.add(ibvc);
		latinMarket.children.add(mexbol);
		latinMarket.children.add(chile65);
		latinMarket.children.add(merval);
		latinMarket.children.add(colcap);
		latinMarket.children.add(crsmbct);
		latinMarket.children.add(bvpsbvps);
		latinMarket.children.add(igbvl);
		latinMarket.children.add(ibov);
		
		HashMap<String,StockMarket> stockMap = new HashMap<String,StockMarket>();
		stockMap.put("MERVAL", merval);
		stockMap.put("IBOV", ibov);
		stockMap.put("CHILE65", chile65);
		stockMap.put("COLCAP", colcap);
		stockMap.put("CRSMBCT", crsmbct);
		stockMap.put("MEXBOL", mexbol);
		stockMap.put("BVPSBVPS", bvpsbvps);
		stockMap.put("IGBVL", igbvl);
		stockMap.put("IBVC", ibvc);
		
		Type listType = new TypeToken<LinkedList<CompanyMatchedArticles>>(){}.getType();
		Gson gson = new Gson();
		LinkedList<CompanyMatchedArticles> cmArticles = gson.fromJson(new FileReader(articleFile), listType);
		for(CompanyMatchedArticles article:cmArticles)
		{
			Company company = new Company(article.getCompanyTicker());
			String stockIndex = article.getStockIndex();
			int i=0;
			for(Article at:article.getArticles())
			{
				if(i>=10)
				{
					break;
				}
				News news = new News(at.getTitle());
				if(stockIndex.equalsIgnoreCase("chile65") && company.name.equalsIgnoreCase("RIPLEY"))
				{
					System.out.println(company.name + "---(" + at.getPostTime().substring(0, 10)+ ")" +at.getTitle());
				}
			}
			for(String key:stockMap.keySet())
			{
				if(key.equalsIgnoreCase(stockIndex))
				{
					stockMap.get(key).children.add(company);
				}
			}
			
		}
		
		Gson gs2 = new Gson();
		String json = gs2.toJson(latinMarket);
		System.out.println(json);
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Properties pro = new Properties();
		try{
			 pro.load(new FileInputStream("config.property"));
			 String topwordFile = pro.getProperty("bloombergNewsOutputPath")+"/filterBloombergArray.txt";
			 File file = new File(topwordFile);
			 GetMemberCompany ttt = new GetMemberCompany();
//			 ttt.transferLine2json(file);
			 ttt.transferStockMem(file);
		}catch(FileNotFoundException ex)
		{
			ex.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
