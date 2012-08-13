package org.vt.model;

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

import org.vt.common.Calculator;
import org.vt.common.DBConnection;
import org.vt.entity.BloombergEnrichedData;
import org.vt.entity.BloombergRawData;
import org.vt.entity.BloombergSurrogateData;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import flanagan.analysis.Stat;

/**
 * This program used to pre-process the raw data, taking raw data as input and
 * output a Enriched data.
 * 
 * @author wei
 */
public class RawdataProcess {

	//Now we assume that the raw data format for Bloomberg stockindex and currency is
	//{ "message_id":String,"type":"stock|currency","name":name,"last_price":float, "pre_last_price":float,"update_time":String, "insert_time":String}
	//According to the format of rar data format, we set the enriched data format for this is:
	//{"message_id":String,"type":"stock|currency","name":String,"last_price":float, "pre_last_price":float,"update_time":String, "insert_time":String,"zscore30":float,"zscore90":float,"parent_id":String}
	
	private BloombergEnrichedData enrichedData;
	private BloombergRawData brd;
	private Connection connection;
	
	
	public RawdataProcess(String rawData)
	{
		connection = DBConnection.getDBInstance();
		Type rawDataType = new TypeToken<BloombergRawData>(){}.getType();
		Gson gson = new Gson();
		brd = gson.fromJson(rawData, rawDataType);
	}
	
	private float calZscore(String stockIndex,float oneDayChange, int subSequence,int dayPeriod) throws SQLException {
		String sql = "select one_day_change from t_daily_stockindices where sub_sequence >= ? and sub_sequence< ? and stock_index=?";
		PreparedStatement pst = connection.prepareStatement(sql);
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
		rs.close();
		pst.close();
		float zScore = Calculator.calZscore(oneDayChange,ll);
		return zScore;
	}
	
	public int checkIfExistInDB() throws SQLException, ParseException{
		System.out.println("stockIndex=" + brd.getStockIndex());
		String sql = "select max(date) date, max(sub_sequence) sub_sequence from t_daily_stockindices where stock_index=? ";
		PreparedStatement pst = connection.prepareStatement(sql);
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
		String dateString = brd.getUpdateTime().split(" ")[0];
		Date date = new Date(sdf.parse(dateString).getTime());
		String stockIndex = brd.getStockIndex();
		pst.setString(1, stockIndex);
		
		ResultSet rs = pst.executeQuery();
		int subSequence = 0;
		Date maxDate = null;
		while(rs.next())
		{
			SimpleDateFormat sdfSqlite = new SimpleDateFormat("yyyy-MM-dd");
			maxDate = new Date(sdfSqlite.parse(rs.getString(1)).getTime());
			subSequence = rs.getInt(2);
		}
		rs.close();
		pst.close();
		if(maxDate.compareTo(date)>=0)// It means that current day is already in the database, then set subsequence = 0
		{
			subSequence = -1;
		}
		return subSequence+1;
	}
	
	public void insertIntoDB(int subSequence) throws SQLException, ParseException
	{
		String sql = "insert into t_daily_stockindices (sub_sequence,stock_index,date,last_price,one_day_change,zscore30,zscore90) values (?,?,?,?,?,?,?)";
		PreparedStatement ps = connection.prepareStatement(sql);
		ps.setInt(1, subSequence);
		ps.setString(2, brd.getStockIndex());
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
		String dateString = brd.getUpdateTime().split(" ")[0];
		Date date = new Date(sdf.parse(dateString).getTime());
		
		SimpleDateFormat sdfSqlite = new SimpleDateFormat("yyyy-MM-dd");
		ps.setString(3, sdfSqlite.format(date));
		
		ps.setFloat(4, brd.getCurrentValue());
		float oneDayChange = brd.getCurrentValue()-brd.getPreviousCloseValue();
		ps.setFloat(5,oneDayChange );
		float zscore30 = calZscore(brd.getStockIndex(),oneDayChange,subSequence,30);
		float zscore90 = calZscore(brd.getStockIndex(),oneDayChange,subSequence,90);
		ps.setFloat(6,zscore30);
		ps.setFloat(7, zscore90);
		ps.execute();
		ps.close();
	}
	
	public void getInfoFromDB() throws SQLException, ParseException
	{
		String sql = "select zscore30,zscore90 from t_daily_stockindices where stock_index=? and date=date(?)";
		PreparedStatement pst = connection.prepareStatement(sql);
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
		String dateString = brd.getUpdateTime().split(" ")[0];
		Date date = new Date(sdf.parse(dateString).getTime());
		pst.setString(1, brd.getStockIndex());
		
		SimpleDateFormat sdfSqlite = new SimpleDateFormat("yyyy-MM-dd");
		pst.setString(2, sdfSqlite.format(date));
		ResultSet rs = pst.executeQuery();
		float zscore30 = 0;
		float zscore90 = 0;
		while(rs.next())
		{
			zscore30 = rs.getFloat(1);
			zscore90 = rs.getFloat(2);
		}
		rs.close();
		pst.close();
//		enrichedData = new BloombergEnrichedData(brd.message_id, brd.type, brd.name, brd.last_price, brd.pre_last_price, brd.update_time, brd.insert_time, zscore30, zscore90);
	}
	
	/**
	 * Computing the absolute value of Moving Average before indicated days
	 * @param stockIndex
	 * @param subSequence
	 * @param duration
	 * @param isPositive
	 * @return
	 * @throws SQLException
	 */
	public float computingMaBefore(String stockIndex, int subSequence, int duration,boolean isPositive) throws SQLException
	{
		int beginSubSeq = subSequence - duration;
		int endSubSeq = subSequence;
		String sql  = "";
		if(isPositive)
		{
			 sql = "select last_price/(last_price-one_day_change) day_change from t_daily_stockindices where stock_index = ? and sub_sequence >=? and sub_sequence< ? and last_price/(last_price-one_day_change)>1 ";
		}
		else
		{
			 sql = "select last_price/(last_price-one_day_change) day_change from t_daily_stockindices where stock_index = ? and sub_sequence >=? and sub_sequence< ? and last_price/(last_price-one_day_change)<1 ";
		}
		
		PreparedStatement psTemp = connection.prepareStatement(sql);
		psTemp.setString(1, stockIndex);
		psTemp.setInt(2, beginSubSeq);
		psTemp.setInt(3, endSubSeq);
		
		LinkedList<Float> nums = new LinkedList<Float>();
		
		ResultSet rs = psTemp.executeQuery();
		while(rs.next())
		{
			
			nums.add((float)Math.abs(Math.log(rs.getFloat(1))));
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
	
	/**
	 * Computing the absolute value of Moving Average after indicated days
	 * @param stockIndex
	 * @param subSequence
	 * @param duration
	 * @param isPositive
	 * @return
	 * @throws SQLException
	 */
	public float computingMaAfter(String stockIndex, int subSequence, int duration,boolean isPositive) throws SQLException
	{
		int beginSubSeq = subSequence;
		int endSubSeq = subSequence  + duration;
		String sql  = "";
		if(isPositive)
		{
			 sql = "select last_price/(last_price-one_day_change) day_change from t_daily_stockindices where stock_index = ? and sub_sequence >? and sub_sequence<= ? and last_price/(last_price-one_day_change)>1 ";
		}
		else
		{
			 sql = "select last_price/(last_price-one_day_change) day_change from t_daily_stockindices where stock_index = ? and sub_sequence >? and sub_sequence<= ? and last_price/(last_price-one_day_change)<1 ";
		}
		
		PreparedStatement psTemp = connection.prepareStatement(sql);
		psTemp.setString(1, stockIndex);
		psTemp.setInt(2, beginSubSeq);
		psTemp.setInt(3, endSubSeq);
		
		LinkedList<Float> nums = new LinkedList<Float>();
		
		ResultSet rs = psTemp.executeQuery();
		while(rs.next())
		{
			nums.add((float)Math.abs(Math.log(rs.getFloat(1))));
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
	
	/**
	 * get the subsequence for currenday's stockIndex
	 * @param stockIndex
	 * @param date
	 * @return
	 * @throws SQLException 
	 * @throws ParseException 
	 */
	public int getSubSequence(String stockIndex,String date) throws SQLException, ParseException
	{
		String sql = "select sub_sequence from t_daily_stockindices where stock_index=? and date = ?";
		System.out.println("stockIndex=" + stockIndex + " date=" + date);
		PreparedStatement psst = connection.prepareStatement(sql);
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
		Date curDay = new Date(sdf.parse(date).getTime());
		psst.setString(1, stockIndex);
		
		SimpleDateFormat sdfSqlite = new SimpleDateFormat("yyyy-MM-dd");
		psst.setString(2, sdfSqlite.format(curDay));
		
		ResultSet rs = psst.executeQuery();
		int subSequence = 0;
		while(rs.next())
		{
			subSequence = rs.getInt(1);
		}
		rs.close();
		psst.close();
		
		return subSequence;
	}
	
	public String process() throws SQLException, ParseException
	{
		int subSequence = checkIfExistInDB();
		if(subSequence>0)
		{
			insertIntoDB(subSequence);
		}
		else
		{
			getInfoFromDB();
		}
		
		//Get currenday's subsequence and try to calculate the enriched data for three day's before
		String dateString = brd.getUpdateTime().split(" ")[0];
		int currentSubSeq = getSubSequence(brd.getStockIndex(),dateString);
		int targetSubSeq = currentSubSeq - 3;
		String stockIndex = brd.getStockIndex();
		
		float posMa3Before = computingMaBefore(stockIndex, targetSubSeq, 3, true);
		float negMa3Before = computingMaBefore(stockIndex, targetSubSeq, 3, false);
		float posMa3After = computingMaAfter(stockIndex, targetSubSeq, 3, true);
		float negMa3After = computingMaAfter(stockIndex, targetSubSeq, 3, false);
		
		float zscore30 = 0;
		float zscore90 = 0;
		float curDayChange = 0;
		int sequenceId = 0;
		Date date = null;
		int endSubSequence = currentSubSeq;
		
		String sql = "select sequence_id, date,last_price/(last_price-one_day_change) day_change,zscore30,zscore90 from t_daily_stockindices where stock_index=? and sub_sequence=?";
		PreparedStatement pss = connection.prepareStatement(sql);
		pss.setString(1, stockIndex);
		pss.setInt(2, targetSubSeq);
		System.out.println("StockIndex=" + stockIndex + " targetSubSeq=" + targetSubSeq);
		ResultSet rs = pss.executeQuery();
		while(rs.next())
		{
			sequenceId = rs.getInt(1);
			SimpleDateFormat sdfSqlite = new SimpleDateFormat("yyyy-MM-dd");
			date = new Date(sdfSqlite.parse(rs.getString(2)).getTime());
			curDayChange = (float)Math.abs(Math.log(rs.getFloat(3)));
			zscore30 = rs.getFloat(4);
			zscore90 = rs.getFloat(5);
		}
		rs.close();
		pss.close();
		
		//Get the locationinfo
		String location = getLocation(stockIndex);
		
		
		//Insert into Enriched Data
		//IF the enriched Data has already in the database, then does not need to insert otherwise insert
		String checkSql = "select count(*) from t_daily_enriched_stockindices where sequence_id=?";
		PreparedStatement checkPtt = connection.prepareStatement(checkSql);
		checkPtt.setInt(1, sequenceId);
		ResultSet checkRs = checkPtt.executeQuery();
		int checkCount = 0;
		while(checkRs.next())
		{
			checkCount = checkRs.getInt(1);
		}
		
		if(checkCount==0)
		{
			String sqlInser = "insert into t_daily_enriched_stockindices (sequence_id,sub_sequence,end_sub_sequence,stock_index,date,zscore30,zscore90,pos_ma_3before,neg_ma_3before,day_change,pos_ma_3after,neg_ma_3after,location) values (?,?,?,?,?,?,?,?,?,?,?,?,?)";
			PreparedStatement ptt = connection.prepareStatement(sqlInser);
			ptt.setInt(1, sequenceId);
			ptt.setInt(2, targetSubSeq);
			ptt.setInt(3, currentSubSeq);
			ptt.setString(4, stockIndex);
			SimpleDateFormat sdfSqlite = new SimpleDateFormat("yyyy-MM-dd");
			ptt.setString(5, sdfSqlite.format(date));
			ptt.setFloat(6, zscore30);
			ptt.setFloat(7, zscore90);
			ptt.setFloat(8, posMa3Before);
			ptt.setFloat(9, negMa3Before);
			ptt.setFloat(10, curDayChange);
			ptt.setFloat(11, posMa3After);
			ptt.setFloat(12, negMa3After);
			ptt.setString(13, location);
			ptt.execute();
			ptt.close();
		}
		
		//create the enrichedData
		enrichedData = new BloombergEnrichedData();
		
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
		enrichedData.setDate(sdf.format(date));
		enrichedData.setDayChange(curDayChange);
		enrichedData.setEndSubSequence(endSubSequence);
		enrichedData.setNegMa3After(negMa3After);
		enrichedData.setNegMa3before(negMa3Before);
		enrichedData.setPosMa3After(posMa3After);
		enrichedData.setPosMa3Before(posMa3Before);
		enrichedData.setSequenceId(sequenceId);
		enrichedData.setStockIndex(stockIndex);
		enrichedData.setSubSequence(currentSubSeq);
		enrichedData.setZscore30(zscore30);
		enrichedData.setZscore90(zscore90);
		enrichedData.setLocation(location);
		
		Gson gson = new Gson();
		return gson.toJson(enrichedData);
	}
	
	public String getLocation(String stockIndex) throws SQLException
	{
		String sql = "select country from s_stock_country where stock_index = '" + stockIndex + "'";
		Statement st = connection.createStatement();
		ResultSet rs = st.executeQuery(sql);
		String country = "";
		while(rs.next())
		{
			country = rs.getString(1);
		}
		rs.close();
		st.close();
		return country;
	}
	
	
	//TODO: Just for Test 
	public static void main(String[] args) throws SQLException, ParseException, JsonIOException, JsonSyntaxException, FileNotFoundException {
		Type rawDataType = new TypeToken<BloombergRawData>(){}.getType();
		Gson gson = new Gson();
		BloombergRawData brd = gson.fromJson(new FileReader("TestRawData.json"), rawDataType);
		String rawData = new Gson().toJson(brd);
		System.out.println("rawData=" + rawData);
		
		RawdataProcess rp = new RawdataProcess(rawData);
		String enrichedData = rp.process();
		System.out.println("enrichedData= " + enrichedData);
		
		EnrichdataProcess ep = new EnrichdataProcess(enrichedData);
		String surrogate = ep.process();
		System.out.println("surrogate= " + surrogate);
		
		LeastSquaresModel lsm = new LeastSquaresModel(surrogate);
		String warning = lsm.process();
		
		System.out.println("Warning=" + warning);
	}
}
