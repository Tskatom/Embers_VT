package org.vt.datapreprocess;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;

import org.vt.common.Calculator;
import org.vt.common.DBConnection;



public class ZscoreStockIndex {
	private Connection con;
	
	public ZscoreStockIndex()
	{
		con = DBConnection.getDBInstance();
	}
	public void run()
	{
		try {
				
				String sql = "select sequence_id,stock_index,sub_sequence,one_day_change from t_daily_stockindices where date>='2002-5-1' order by date asc";
				PreparedStatement pst = con.prepareStatement(sql);
				
				ResultSet rs = pst.executeQuery();
				while(rs.next())
				{
					int sequenceId = rs.getInt(1);
					String stockIndex = rs.getString(2);
					int subSequence = rs.getInt(3);
					float oneDayChange = rs.getFloat(4);
					//Calculate zScore30
					float zScore30 = calZscore(stockIndex,oneDayChange,subSequence,30);
					//Calculate zScore90
					float zScore90 = calZscore(stockIndex,oneDayChange,subSequence,90);
					String updateSql = "update t_daily_stockindices set zscore30 = ?,zscore90=? where sequence_id=?";
					
					PreparedStatement upPst = con.prepareStatement(updateSql);
					upPst.setFloat(1, zScore30);
					upPst.setFloat(2, zScore90);
					upPst.setInt(3, sequenceId);
					upPst.executeUpdate();
					
					upPst.close();
				}
				
				rs.close();
				pst.close();
				
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private float calZscore(String stockIndex,float oneDayChange, int subSequence,int dayPeriod) throws SQLException {
		String sql = "select one_day_change from t_daily_stockindices where sub_sequence >= ? and sub_sequence< ? and stock_index=?";
		PreparedStatement pst = con.prepareStatement(sql);
		int beforeAccpet = subSequence-dayPeriod;
		pst.setInt(1, beforeAccpet);
		pst.setInt(2, subSequence);
		pst.setString(3, stockIndex);
		ResultSet rs = pst.executeQuery();
		LinkedList<Float> ll = new LinkedList<Float>();
		
		float sumValue = 0;
		while(rs.next())
		{
			ll.add(new Float(rs.getFloat(1)));
		}
		
		float zScore = Calculator.calZscore(oneDayChange,ll);
		return zScore;
	}
	

	public static void main(String[] args) throws SQLException {
			ZscoreStockIndex sic = new ZscoreStockIndex();
			sic.run();
	}

}
