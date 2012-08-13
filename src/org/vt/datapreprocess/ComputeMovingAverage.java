package org.vt.datapreprocess;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.vt.common.DBConnection;

import flanagan.analysis.Stat;

public class ComputeMovingAverage {
	
	private Connection connection;
	private PreparedStatement ps;
	private boolean isAbs;
	
	public ComputeMovingAverage(boolean isAbs) throws SQLException
	{
		connection = DBConnection.getDBInstance();
		String insertSql;
		this.isAbs = isAbs;
		if(isAbs)
		{
			insertSql = "insert into embers.t_stockindices_abs_moving( sequence_id, stock_index,date,zscore30,zscore90,mov_3avg,mov_5avg,mov_7avg,duration) values (?,?,?,?,?,?,?,?,?)";
		}
		else
		{
			insertSql = "insert into embers.t_stockindices_moving( sequence_id, stock_index,date,zscore30,zscore90,mov_3avg,mov_5avg,mov_7avg,duration) values (?,?,?,?,?,?,?,?,?)";
		}
		
		ps = connection.prepareStatement(insertSql);
	}

	public void compute() throws SQLException
	{
		String querySql = "SELECT sequence_id,sub_sequence,stock_index,date,zscore30,zscore90 FROM embers.t_daily_stockindices where zscore30 is not null order by stock_index";
		Statement st = connection.createStatement();
		ResultSet rs = st.executeQuery(querySql);
		while(rs.next())
		{
			int sequenceId = rs.getInt(1);
			int subSequence = rs.getInt(2);
			String stockIndex = rs.getString(3);
			Date date = rs.getDate(4);
			float z30 = rs.getFloat(5);
			float z90 = rs.getFloat(6);
			float m3 = computeMoving(subSequence,stockIndex,3);
			float m5 = computeMoving(subSequence,stockIndex,5);
			float m7 = computeMoving(subSequence,stockIndex,7);
			int duration = 0;
			
			if(Math.abs(z30)>=4||Math.abs(z90)>=3)
			{
				//Computing the Duration
				duration = getLastSigmaEvent(sequenceId,stockIndex,date);
			}
			
			ps.setInt(1, sequenceId);
			ps.setString(2, stockIndex);
			ps.setDate(3, date);
			ps.setFloat(4, z30);
			ps.setFloat(5, z90);
			ps.setFloat(6, m7);
			ps.setFloat(7, m5);
			ps.setFloat(8, m7);
			ps.setInt(9, duration);
			ps.execute();
		}
		rs.close();
	}
	
	public int getLastSigmaEvent(int sequenceId,String stockIndex,Date date) throws SQLException
	{
		String sql = "select max(date) from embers.t_daily_stockindices where (abs(zscore30)>=4 or abs(zscore90)>=3) and stock_index=? and sequence_id<?";
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
		
		int duration = (int)(date.getTime() - retDate.getTime())/(1000*60*60*24);
		return duration;
	}
	
	public void close() throws SQLException{
		ps.close();
		connection.close();
	}
	
	
	public float computeMoving(int subSeq,String stockIndex,int num) throws SQLException
	{
		String sql;
		if(isAbs)
		{
			sql = "select abs(ln(last_price/(last_price-one_day_change))) from embers.t_daily_stockindices where stock_index = ? and sub_sequence >=? and sub_sequence< ?";
		}
		else
		{
			sql = "select ln(last_price/(last_price-one_day_change)) from embers.t_daily_stockindices where stock_index = ? and sub_sequence >=? and sub_sequence< ?";
		}
		int beginSeq = subSeq - num;
		PreparedStatement psTemp = connection.prepareStatement(sql);
		psTemp.setString(1, stockIndex);
		psTemp.setInt(2, beginSeq);
		psTemp.setInt(3, subSeq);
		
		float[] changes = new float[num];
		int i = 0;
		ResultSet rs = psTemp.executeQuery();
		while(rs.next())
		{
			changes[i++] = rs.getFloat(1);
		}
		rs.close();
		float movingAvg = Stat.mean(changes);
		return movingAvg;
	}
	
	public static void main(String[] args) throws SQLException {
		ComputeMovingAverage cma = new ComputeMovingAverage(false);
		cma.compute();
		cma.close();
	}
}
