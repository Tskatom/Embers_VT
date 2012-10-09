package org.vt.datapreprocess;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;

import org.vt.common.DBConnection;

import flanagan.analysis.Stat;

public class ComputingTrainingData {
	
	private Connection connection;
	private PreparedStatement ps;
	
	public ComputingTrainingData() throws SQLException
	{
		connection = DBConnection.getDBInstance();
		String insertSql = "insert into embers.t_prediction_training_stock_index (sequence_id,stock_index,date,zscore30,zscore90,moving3Before,moving3after,currentChange,duration) values (?,?,?,?,?,?,?,?,?)";
		ps = connection.prepareStatement(insertSql);
	}
	
	public float get3MaBefore(boolean isPos, String stockIndex, int subSequence) throws SQLException
	{
		String sql  = "";
		if(isPos)
		{
			 sql = "select abs(ln(last_price/(last_price-one_day_change))) day_change from embers.t_daily_stockindices where stock_index = ? and sub_sequence >=? and sub_sequence< ? and ln(last_price/(last_price-one_day_change))>0 ";
		}
		else
		{
			 sql = "select abs(ln(last_price/(last_price-one_day_change))) day_change from embers.t_daily_stockindices where stock_index = ? and sub_sequence >=? and sub_sequence< ? and ln(last_price/(last_price-one_day_change))<0 ";
		}
		
		int beginSeq = subSequence - 3;
		PreparedStatement psTemp = connection.prepareStatement(sql);
		psTemp.setString(1, stockIndex);
		psTemp.setInt(2, beginSeq);
		psTemp.setInt(3, subSequence);
		
		LinkedList<Float> nums = new LinkedList<Float>();
		
		ResultSet rs = psTemp.executeQuery();
		while(rs.next())
		{
			nums.add(rs.getFloat(1));
		}
		rs.close();
		int i = 0;
		float[] changes = new float[nums.size()];
		for(Float fl:nums)
		{
			changes[i++] = fl;
		}
		float movingAvg = Stat.mean(changes);
		if(nums.size()==0)
		{
			movingAvg = 0;
		}
		return movingAvg;
	}
	
	public float get3MaAfter(boolean isPos, String stockIndex, int subSequence) throws SQLException
	{
		String sql  = "";
		if(isPos)
		{
			 sql = "select abs(ln(last_price/(last_price-one_day_change))) day_change from embers.t_daily_stockindices where stock_index = ? and sub_sequence >? and sub_sequence<= ? and ln(last_price/(last_price-one_day_change))>0 ";
		}
		else
		{
			 sql = "select abs(ln(last_price/(last_price-one_day_change))) day_change from embers.t_daily_stockindices where stock_index = ? and sub_sequence >? and sub_sequence<= ? and ln(last_price/(last_price-one_day_change))<0 ";
		}
		
		int endSeq = subSequence + 3;
		PreparedStatement psTemp = connection.prepareStatement(sql);
		psTemp.setString(1, stockIndex);
		psTemp.setInt(2, subSequence);
		psTemp.setInt(3, endSeq);
		
		LinkedList<Float> nums = new LinkedList<Float>();
		
		ResultSet rs = psTemp.executeQuery();
		while(rs.next())
		{
			nums.add(rs.getFloat(1));
		}
		rs.close();
		int i = 0;
		float[] changes = new float[nums.size()];
		for(Float fl:nums)
		{
			changes[i++] = fl;
		}
		float movingAvg = Stat.mean(changes);
		if(nums.size()==0)
		{
			movingAvg = 0;
		}
		return movingAvg;
	}
	
	public int getDuration(boolean isPos, String stockIndex, int sequenceId,Date date) throws SQLException
	{
		String sql = "";
		if(isPos)
		{
			sql = "select min(date) from embers.t_daily_stockindices where (zscore30>=4 or zscore90>=3) and stock_index=? and sequence_id>?";
		}
		else
		{
			sql = "select min(date) from embers.t_daily_stockindices where (zscore30<=-4 or zscore90<=-3) and stock_index=? and sequence_id>?";
		}
		PreparedStatement ps3 = connection.prepareStatement(sql);
		ps3.setString(1, stockIndex);
		ps3.setInt(2, sequenceId);
		
		ResultSet rs = ps3.executeQuery();
		Date retDate = null;
		while(rs.next())
		{
			retDate = rs.getDate(1);
		}
		if(retDate==null)
		{
			retDate = date;
		}
		
		long duration = (retDate.getTime() - date.getTime())/(1000*60*60*24);
		return (int)duration;
	}

	public void compute() throws SQLException
	{
		//TODO: Test computing the date after 2011-1-1, the training data is before 2011-1-1
		String queSql = "select sequence_id,sub_sequence,stock_index,date,abs(ln(last_price/(last_price-one_day_change))) day_change,zscore30,zscore90 from embers.t_daily_stockindices where (abs(zscore30)>=4 or abs(zscore90)>=3)  and date>='2011-1-1'  order by stock_index,date;";
		Statement st = connection.createStatement();
		ResultSet rs = st.executeQuery(queSql);
		while(rs.next())
		{
			int sequenceId = rs.getInt(1);
			int subSequence = rs.getInt(2);
			String stockIndex = rs.getString(3);
			Date date = rs.getDate(4);
			float dayChange = rs.getFloat(5);
			float zscore30 = rs.getFloat(6);
			float zscore90 = rs.getFloat(7);
			
			boolean isPos = true;
			//Computing the positive sigma event and negative event separately
			if(zscore30<0)
			{
				isPos =  false;
			}
			
			float moving3Before =  get3MaBefore(isPos, stockIndex, subSequence);
			float moving3After = get3MaAfter(isPos, stockIndex, subSequence);
			int duration = getDuration(isPos, stockIndex, sequenceId, date);
			
		    
			ps.setInt(1, sequenceId);
			ps.setString(2, stockIndex);
			ps.setDate(3, date);
			ps.setFloat(4, zscore30);
			ps.setFloat(5, zscore90);
			ps.setFloat(6, moving3Before);
			ps.setFloat(7, moving3After);
			ps.setFloat(8,dayChange);
			ps.setInt(9, duration);
//			
//			System.out.println("sequenceId="+sequenceId );
//			System.out.println("stockIndex="+stockIndex );
//			System.out.println("date="+date );
//			System.out.println("zscore30="+zscore30 );
//			System.out.println("zscore90="+zscore90 );
//			System.out.println("moving3Before="+moving3Before );
//			System.out.println("moving3After="+moving3After );
//			System.out.println("dayChange="+dayChange );
//			System.out.println("duration="+duration );
//			
			ps.execute();
		}
	}
	
	public void close() throws SQLException
	{
		ps.close();
		connection.close();
	}
	
	public static void main(String[] args) {
		try 
		{
			ComputingTrainingData ctd = new ComputingTrainingData();
			ctd.compute();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
