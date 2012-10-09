package org.vt.test;

import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;

import org.vt.common.DBConnection;

import com.google.gson.Gson;

public class CreateScatterJson {
	
	public class Event{
		String stockIndex;
		String date;
		float value;
		
		public Event(String stockIndex,String date,float value)
		{
			this.stockIndex = stockIndex;
			this.date = date;
			this.value = value;
		}
	}
	
	public void create() throws SQLException
	{
		Connection con = DBConnection.getDBInstance();
		Statement st = con.createStatement();
//		String sql = "select date,zscore30 from embers.t_daily_stockindices where stock_index='IGBVL' and zscore30 is not null order by date";
		String sql = "select upper(stock_index) stock_index,date,zscore30 zscore from embers.t_daily_stockindices where zscore30 is not null  and abs(zscore30>=4) union select upper(stock_index) stock_index,date,zscore90 zscore from embers.t_daily_stockindices where zscore30 is not null  and (3<=abs(zscore30)<4 and abs(zscore90)>=4)";
		ResultSet rs = st.executeQuery(sql);
		LinkedList<Event> events = new LinkedList<Event>();
		while(rs.next())
		{
			String stockIndex = rs.getString(1);
			Date date = rs.getDate(2);
			Float zscore = rs.getFloat(3);
			Event event = new Event(stockIndex,date.toString(),zscore);
			events.add(event);
		}
		
		Gson gson = new Gson();
		String json = gson.toJson(events);
		System.out.println(json);
		
	}
	public static void main(String[] args) throws SQLException {
		
		CreateScatterJson ci = new CreateScatterJson();
		ci.create();
	}

}
