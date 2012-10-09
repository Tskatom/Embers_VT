package org.vt.test;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;

import org.vt.common.DBConnection;

import com.google.gson.Gson;

public class CreateJsonForPosNegZscoreIndex {
	
	private Connection con;
	public CreateJsonForPosNegZscoreIndex()
	{
		 con = DBConnection.getDBInstance();
	}
	public class Price{
		String date;
		float value;
		float pos;
		float neg;
		
		public Price(String date,float value,float pos,float neg)
		{
			this.date = date;
			this.value = value;
			this.pos = pos;
			this.neg = neg;
		}
	}
	
	public String create(String index) throws SQLException
	{
		
		Statement st = con.createStatement();
		String sql = "select post_date,zscore30,pos_value,neg_value from embers.time_series_stock_analysis where stock_index='"+index+"' order by post_date asc";
//		String sql = "select date,zscore30 from embers.t_daily_companyprice where stock_index='BVPSBVPS' and company_ticker='MHCH:PP' and zscore30 is not null order by date";
		ResultSet rs = st.executeQuery(sql);
		LinkedList<Price> prices = new LinkedList<Price>();
		while(rs.next())
		{
			Date date = rs.getDate(1);
			Float zscore30 = rs.getFloat(2);
			float pos = rs.getFloat(3);
			float neg = rs.getFloat(4);
			Price price = new Price(date.toString(),zscore30,pos,neg);
			prices.add(price);
		}
		
		Gson gson = new Gson();
		String json = gson.toJson(prices);
		st.close();
		return json;
	}
	public static void main(String[] args) throws SQLException, IOException {
		
		CreateJsonForPosNegZscoreIndex ci = new CreateJsonForPosNegZscoreIndex();
		String[] stockIndices = new String[9];
		stockIndices[0]="BVPSBVPS";
		stockIndices[1]="CHILE65";
		stockIndices[2]="COLCAP";
		stockIndices[3]="CRSMBCT";
		stockIndices[4]="IBOV";
		stockIndices[5]="IBVC";
		stockIndices[6]="MEXBOL";
		stockIndices[7]="MERVAL";
		stockIndices[8]="IGBVL";
		for(String stock:stockIndices)
		{
			String fileName = "d:/" + stock + "_Sen.json";
			BufferedWriter bw = new BufferedWriter(new FileWriter(fileName));
			bw.write(ci.create(stock));
			bw.flush();
			bw.close();
		}
		
	}

}
