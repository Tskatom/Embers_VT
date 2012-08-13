package org.vt.dataprepare;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.LinkedList;

import org.vt.common.DBConnection;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

public class ImportNewAnalysis {
	
	private Connection connection;
	private PreparedStatement ps;
	
	public ImportNewAnalysis() throws SQLException{
		connection = DBConnection.getDBInstance();
		String insertSql = "insert into company_news_analysis (company_ticker,stock_index,post_date,article_id,positive_fre,negative_fre) values (?,?,?,?,?,?)";
		ps = connection.prepareStatement(insertSql);
	}
	
	public class Analysis{
		public LinkedList<PNAritcle> articles = new LinkedList<PNAritcle>();
		public String stockIndex;
		public String companyTicker;
		public String companyName;
	}
	
	public class PNAritcle
	{
		public String articelId;
		public int positive;
		public int negative;
		public int neutral;
		public String postTime;
	}
	
	public Date getPostDate(String articleId,String stockIndex) throws SQLException
	{
		String sql = "select post_date from embers.t_company_news where article_id=? and stock_index=?";
		PreparedStatement ps2 = connection.prepareStatement(sql);
		ps2.setString(1, articleId);
		ps2.setString(2, stockIndex);
		ResultSet rs = ps2.executeQuery();
		Date postDate = null;
		while(rs.next())
		{
			postDate = rs.getDate(1);
		}
		ps2.close();
		return postDate;
		
	}
	
	public Date getPostDate(String dateStr) throws ParseException
	{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		return new Date(sdf.parse(dateStr).getTime());
	}
	
	public void clear() throws SQLException
	{
		String sql = "delete from embers.company_news_analysis";
		Statement st = connection.createStatement();
		st.execute(sql);
		st.close();
	}
	
	
	public void importData() throws JsonIOException, JsonSyntaxException, FileNotFoundException, SQLException, ParseException
	{
		clear();
		File file = new File("D:/embers/Bloomberg/analysis/sentiment-result.txt");
		Gson gson = new Gson();
		Type listType = new TypeToken<LinkedList<Analysis>>(){}.getType();
		LinkedList<Analysis> analysises = gson.fromJson(new FileReader(file), listType);
		for(Analysis al:analysises)
		{
			String stockIndex = al.stockIndex;
			String companyTicker = al.companyTicker;
			for(PNAritcle pna:al.articles)
			{
				int positive = pna.positive;
				int negative = pna.negative;
				String articleId = pna.articelId;
				Date postDate = getPostDate(pna.postTime.split(" ")[0]);
				
				System.out.println(postDate + ":" + articleId);
				ps.setString(1, companyTicker);
				ps.setString(2, stockIndex);
				ps.setDate(3,postDate);
				ps.setString(4, articleId);
				ps.setInt(5, positive);
				ps.setInt(6, negative);
				
				ps.execute();
			}
		}
	}
	
	public void close() throws SQLException
	{
		ps.close();
		connection.close();
	}

	public static void main(String[] args) throws JsonIOException, JsonSyntaxException, FileNotFoundException, SQLException, ParseException {
		ImportNewAnalysis ina = new ImportNewAnalysis();
		ina.importData();
		ina.close();
	}
}
