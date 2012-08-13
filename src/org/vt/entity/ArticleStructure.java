package org.vt.entity;

import java.util.LinkedList;

public class ArticleStructure {
	public String articleId;
	public LinkedList<String> parts;
	
	public ArticleStructure(String articleId)
	{
		this.articleId = articleId;
		parts = new LinkedList<String>();
	}
	
	

}
