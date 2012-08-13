package org.vt.entity;
/**
 *This class are used to represent the article entity 
 *
 * @author wei
 *@version 2012-7-12
 */
public class Article {
	
	private String articelId;
	private String author;
	private String relatedCompany;
	private String title;
	private String postTime;
	private String queryTime;
	private String content;
	private String source;
	
	public String getArticelId() {
		return articelId;
	}
	public void setArticelId(String articelId) {
		this.articelId = articelId;
	}
	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	public String getRelatedCompany() {
		return relatedCompany;
	}
	public void setRelatedCompany(String relatedCompany) {
		this.relatedCompany = relatedCompany;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getPostTime() {
		return postTime;
	}
	public void setPostTime(String postTime) {
		this.postTime = postTime;
	}
	public String getQueryTime() {
		return queryTime;
	}
	public void setQueryTime(String queryTime) {
		this.queryTime = queryTime;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	
	

}


