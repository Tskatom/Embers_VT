package org.vt.datapreprocess;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.vt.common.DBConnection;

public class ComputeTimeSeriesStock {
	public class T1{
		public Date postDate;
		public String stockIndex;
		public float z30;
		public float z90;
		
		public T1(Date postDate,String stockIndex,float z30,float z90)
		{
			this.postDate = postDate;
			this.stockIndex = stockIndex;
			this.z30 = z30;
			this.z90 = z90;
		}
	}
	
	public class T2{
		public String stockIndex;
		public Date postDate;
		public float pos;
		public float neg;
		
		public T2(Date postDate,String stockIndex,float pos,float neg)
		{
			this.pos=pos;
			this.neg=neg;
			this.stockIndex=stockIndex;
			this.postDate = postDate;
		}
	}
	
	
	private Connection connection;
	private PreparedStatement ps;
	private T1[] t1s;
	private T2[] t2s;
	
	public ComputeTimeSeriesStock() throws SQLException
	{
		connection = DBConnection.getDBInstance();
		String inSql = "insert into time_series_stock_analysis (post_date,stock_index,zscore30,zscore90,pos_value,neg_value) values (?,?,?,?,?,?)";
		ps = connection.prepareStatement(inSql);
	}
	
	public void getT1(String stockIndex) throws SQLException
	{
		Statement st = connection.createStatement();
		String sql = "select stock_index,date,zscore30,zscore90 from embers.t_daily_stockindices where stock_index='"+stockIndex+"' and  zscore30 is not null order by date asc";
		String countSql = "select count(*) count from embers.t_daily_stockindices where stock_index='"+stockIndex+"' and  zscore30 is not null order by date asc";
		ResultSet crs = st.executeQuery(countSql);
		int count = 0;
		while(crs.next())
		{
			count = crs.getInt(1);
		}
		t1s = new T1[count];
		ResultSet rs = st.executeQuery(sql);
		int i = 0;
		while(rs.next())
		{
			Date date = rs.getDate(2);
			float z30 = rs.getFloat(3);
			float z90 = rs.getFloat(4);
			t1s[i++] = new T1(date,stockIndex,z30,z90);
		}
		st.close();
	}
	
	
	public void getT2(String stockIndex) throws SQLException
	{
		Statement st = connection.createStatement();
		String sql = "select stock_index, post_date,sum(positive_fre) pos,sum(negative_fre) neg from embers.company_news_analysis where stock_index='" + stockIndex + "' GROUP BY POST_DATE,stock_index order by post_date asc";
		String countSql = "select count(*) count from (select count(*) from embers.company_news_analysis where stock_index='"+stockIndex+"' GROUP BY POST_DATE,stock_index) a";
		ResultSet crs = st.executeQuery(countSql);
		int count = 0;
		while(crs.next())
		{
			count = crs.getInt(1);
		}
		t2s = new T2[count];
		ResultSet rs = st.executeQuery(sql);
		int i = 0;
		while(rs.next())
		{
			Date date = rs.getDate(2);
			float pos = rs.getInt(3);
			float neg = rs.getInt(4);
			t2s[i++] = new T2(date,stockIndex,pos,neg);
		}
		
		float maxPos = 0;
		float minPos = 0;
		float maxNeg = 0;
		float minNeg = 0;
		
		String tSql ="select max(pos),min(pos),max(neg),min(neg) from (select stock_index, post_date,sum(positive_fre) pos,sum(negative_fre) neg from embers.company_news_analysis group by stock_index, post_date) a";
		ResultSet trs = st.executeQuery(tSql);
		while(trs.next())
		{
			maxPos = trs.getInt(1);
			minPos = trs.getInt(2);
			maxNeg = trs.getInt(3);
			minNeg = trs.getInt(4);
		}
		System.out.println(maxPos + "-" + minPos + "-" +maxNeg +"-" +minNeg);
		for(T2 t2:t2s)
		{
			t2.pos = (t2.pos-minPos)/(maxPos-minPos);
			t2.neg = (t2.neg - minNeg)/(maxNeg - minNeg);
		}
		st.close();
	}
	public void clear() throws SQLException
	{
		Statement st = connection.createStatement();
		String sql = "delete from time_series_stock_analysis";
		st.execute(sql);
		st.close();
	}
	public void compute(String stockIndex) throws ParseException, SQLException
	{
//		clear();
		getT1(stockIndex);
		getT2(stockIndex);
		
		System.out.println(t1s.length);
		System.out.println(t2s.length);
		Calendar start = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		start.setTime(sdf.parse("2010-03-01"));
		
		Calendar end = Calendar.getInstance();
		end.setTime(sdf.parse("2012-07-16"));
		int j = 0;
		int m=0;
		int n=0;
		while(start.compareTo(end)<=0)
		{
			Date curDay = new Date(start.getTimeInMillis());
			Date postDate=curDay;
			float z30=0;
			float z90=0;
			float pos=0;
			float neg=0;
			boolean isSkip = true;
//			System.out.println("m="+m+" t1s.length="+t1s.length + "--" +stockIndex);
			if(curDay.equals(t1s[m>=t1s.length?t1s.length-1:m].postDate))
			{
				z30 = t1s[m].z30;
				z90 = t1s[m].z90;
				m++;
				isSkip = false;
			}
			if(t2s.length>0 && curDay.equals(t2s[n>=t2s.length?t2s.length-1:n].postDate))
			{
				pos = t2s[n].pos;
				neg = t2s[n].neg;
				n++;
				isSkip = false;
			}
			
			if(isSkip)
			{
				start.add(Calendar.DAY_OF_MONTH, 1);
				continue;
			}
//			System.out.println(postDate.toString());
			ps.setDate(1, postDate);
			ps.setString(2, stockIndex);
			ps.setFloat(3, z30);
			ps.setFloat(4, z90);
			ps.setFloat(5, pos);
			ps.setFloat(6, neg);
			ps.execute();
			start.add(Calendar.DAY_OF_MONTH, 1);
		}
		
	}
	
	public void close() throws SQLException
	{
		ps.close();
		connection.close();
	}
	public static void main(String[] args) throws ParseException, SQLException {
		ComputeTimeSeriesStock css = new ComputeTimeSeriesStock();
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
			css.compute(stock);
		}
		css.close();
	}
}
