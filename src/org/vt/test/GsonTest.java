package org.vt.test;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.LinkedList;

import org.vt.entity.CompanyMatchedArticles;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

public class GsonTest {
	public static void main(String[] args) throws JsonIOException, JsonSyntaxException, IOException {
		Gson gson = new Gson();
		Type listType = new TypeToken<LinkedList<CompanyMatchedArticles>>(){}.getType();
		LinkedList<CompanyMatchedArticles> articles = gson.fromJson(new FileReader("d:/embers/data-collector/bloombergNews/filterBloombergArray.txt"), listType);
		System.out.println(articles.size());
		LinkedList<CompanyMatchedArticles> newList = new LinkedList<CompanyMatchedArticles>();
		for(CompanyMatchedArticles article:articles)
		{
			newList.add(article);
			if(newList.size()>5)
			{
				break;
			}
		}
		
		BufferedWriter bw = new BufferedWriter(new FileWriter("d:/newsList.txt"));
		Gson gs2 = new Gson();
		String json = gs2.toJson(newList);
		bw.write(json);
		bw.flush();
		bw.close();
	}
	
}
