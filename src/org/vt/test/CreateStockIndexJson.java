package org.vt.test;

import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;

import org.vt.common.DBConnection;

import com.google.gson.Gson;

public class CreateStockIndexJson {
	
	public class Price{
		String date;
		float value;
		
		public Price(String date,float value)
		{
			this.date = date;
			this.value = value;
		}
	}
	
	public void create() throws SQLException
	{
		Connection con = DBConnection.getDBInstance();
		Statement st = con.createStatement();
//		String sql = "select date,zscore30 from embers.t_daily_stockindices where stock_index='IGBVL' and zscore30 is not null order by date";
		String sql = "select date,zscore30 from embers.t_daily_companyprice where stock_index='IGBVL' and company_ticker='MHCH:PP' and zscore30 is not null order by date";
		ResultSet rs = st.executeQuery(sql);
		LinkedList<Price> prices = new LinkedList<Price>();
		while(rs.next())
		{
			Date date = rs.getDate(1);
			Float zscore30 = rs.getFloat(2);
			Price price = new Price(date.toString(),zscore30);
			prices.add(price);
		}
		
		Gson gson = new Gson();
		String json = gson.toJson(prices);
		System.out.println(json);
		
	}
	public static void main(String[] args) throws SQLException {
		
		CreateStockIndexJson ci = new CreateStockIndexJson();
		ci.create();
	}

}
