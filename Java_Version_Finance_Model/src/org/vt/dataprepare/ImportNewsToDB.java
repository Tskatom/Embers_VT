package org.vt.dataprepare;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.Properties;

import org.vt.common.DBConnection;
import org.vt.entity.Article;
import org.vt.entity.CompanyMatchedArticles;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

/**
 * This program used to import articles to the database 
 * @author wei
 * @version 2012-7-26
 */
public class ImportNewsToDB {
	private Connection connection;
	private PreparedStatement ps;
	private Properties properties;
	
	public ImportNewsToDB() throws SQLException, FileNotFoundException, IOException
	{
		connection = DBConnection.getDBInstance();
		String insertSql = "insert into t_company_news (article_id,company_ticker,company_name,stock_index,title,author,post_time,post_date,source) values (?,?,?,?,?,?,?,?,?)";
		ps = connection.prepareStatement(insertSql);
		properties = new Properties();
		properties.load(new FileInputStream("config.property"));
		
	}
	
	public void clear() throws SQLException
	{
		String clearSql = "delete from t_company_news";
		Statement st = connection.createStatement();
		st.execute(clearSql);
		st.close();
	}
	
	public void close() throws SQLException
	{
		ps.close();
		connection.close();
	}
	
	public void importNews() throws JsonIOException, JsonSyntaxException, FileNotFoundException, ParseException, SQLException
	{
		Type listType = new TypeToken<LinkedList<CompanyMatchedArticles>>(){}.getType();
		String filePath = properties.getProperty("newsPath");
		Gson gson = new Gson();
		LinkedList<CompanyMatchedArticles> cmArticles = gson.fromJson(new FileReader(filePath), listType);
		for(CompanyMatchedArticles cma:cmArticles)
		{
			String companyTicker = cma.getCompanyTicker();
			String companyName = cma.getCompanyName();
			String stockIndex = cma.getStockIndex();
			for(Article article:cma.getArticles())
			{
				String articleId = article.getArticelId();
				String title = article.getTitle();
				String author = article.getAuthor();
				String source = article.getSource();
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
				Timestamp postTime = new Timestamp(dateFormat.parse(article.getPostTime()).getTime());
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				Date postDate = new Date(sdf.parse(article.getPostTime().split(" ")[0]).getTime());
				
				//Insert into Database
				ps.setString(1, articleId);
				ps.setString(2, companyTicker);
				ps.setString(3,companyName);
				ps.setString(4, stockIndex);
				ps.setString(5, title);
				ps.setString(6, author);
				ps.setTimestamp(7, postTime);
				ps.setDate(8,postDate);
				ps.setString(9, source);
				
				ps.execute();
			}
		}
	}
	
	public static void main(String[] args) {
		try {
			ImportNewsToDB intb = new ImportNewsToDB();
			intb.clear();
			intb.importNews();
			intb.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonIOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonSyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
