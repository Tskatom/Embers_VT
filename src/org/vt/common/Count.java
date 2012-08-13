package org.vt.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.Properties;

import org.vt.entity.Article;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

public class Count {
	
	public static int getArticles(File file) throws JsonIOException, JsonSyntaxException, FileNotFoundException
	{
		Gson gson = new Gson();
		Type listType = new TypeToken<LinkedList<Article>>(){}.getType();
		LinkedList<Article> articles = gson.fromJson(new FileReader(file), listType);
		return articles.size();
	}
	public static void main(String[] args) throws FileNotFoundException, IOException {
		Properties pro = new Properties();
		pro.load(new FileInputStream("config.property"));
		String fileDir = pro.getProperty("bloombergNewsOutputPath");
		File[] files = new File(fileDir).listFiles();
		int totalArticles = 0;
		for(File file:files)
		{
			if(!file.isDirectory())
			{
				totalArticles += getArticles(file);
			}
		}
		System.out.println("TotalArticles: " + totalArticles);
		
		
		//Get Companies
		String memDir = pro.getProperty("companyList");
		File[] memFiles = new File(memDir).listFiles();
		int memCounts = 0;
		for(File file:memFiles)
		{
			if(!file.isDirectory())
			{
				String line = "";
				BufferedReader br = new BufferedReader(new FileReader(file));
				while((line = br.readLine())!=null)
				{
					memCounts++;
				}
			}
		}
		System.out.println("memCounts: " + memCounts);
		
		//Get filterNews
		
	}

}
