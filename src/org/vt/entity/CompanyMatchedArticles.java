package org.vt.entity;

import java.util.LinkedList;

public class CompanyMatchedArticles {
	private String stockIndex;
	private String companyTicker;
	private String companyName;
	private LinkedList<Article> articles;
	
	
	public String getCompanyName() {
		return companyName;
	}
	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}
	
	public CompanyMatchedArticles()
	{
		articles = new LinkedList<Article>();
	}
	public String getStockIndex() {
		return stockIndex;
	}
	public void setStockIndex(String stockIndex) {
		this.stockIndex = stockIndex;
	}
	public String getCompanyTicker() {
		return companyTicker;
	}
	public void setCompanyTicker(String companyTicker) {
		this.companyTicker = companyTicker;
	}
	public LinkedList<Article> getArticles() {
		return articles;
	}
	public void setArticles(LinkedList<Article> articles) {
		this.articles = articles;
	}
	
	public void addArticle(Article article)
	{
		articles.add(article);
	}
	
}
