package org.vt.model;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.UUID;

import org.vt.common.DBConnection;
import org.vt.entity.BloombergEnrichedData;
import org.vt.entity.BloombergSurrogateData;
import org.vt.entity.BloombergWarning;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

public class LeastSquaresModel {
	
	private BloombergSurrogateData bsd;
	private BloombergEnrichedData bed;
	private Connection connection;
	
	public LeastSquaresModel(String surrogate)
	{
		connection = DBConnection.getDBInstance();
		Type type = new TypeToken<BloombergSurrogateData>(){}.getType();
		Gson gson = new Gson();
		bsd = gson.fromJson(surrogate, type);
	}
	
	public void initialEnrichedData(int sequenceId) throws SQLException
	{
		String sql = "select stock_index,date,zscore30,zscore90,pos_ma_3before,neg_ma_3before,day_change,pos_ma_3after,neg_ma_3after,location from t_daily_enriched_stockindices where sequence_id=?";
		PreparedStatement ps = connection.prepareStatement(sql);
		ps.setInt(1, sequenceId);
		bed = new BloombergEnrichedData();
		ResultSet rs = ps.executeQuery();
		while(rs.next())
		{
			String stockIndex = rs.getString(1);
			String date = rs.getString(2);
			float zscore30 = rs.getFloat(3);
			float zscore90 = rs.getFloat(4);
			float posMa3Before = rs.getFloat(5);
			float negMa3Before = rs.getFloat(6);
			float dayChange = rs.getFloat(7);
			float posMa3After = rs.getFloat(8);
			float negMa3After = rs.getFloat(9);
			String location = rs.getString(10);
			
			//Setpu the content of EnrichedData
			bed.setSequenceId(sequenceId);
			bed.setStockIndex(stockIndex);
			bed.setDate(date);
			bed.setZscore30(zscore30);
			bed.setZscore90(zscore90);
			bed.setPosMa3Before(posMa3Before);
			bed.setNegMa3before(negMa3Before);
			bed.setDayChange(dayChange);
			bed.setPosMa3After(posMa3After);
			bed.setNegMa3After(negMa3After);
			bed.setLocation(location);
		}
		rs.close();
		ps.close();
	}
	
	public String process() throws SQLException, ParseException
	{

		//Get the EnrichedData through surrogate data
		int enrichedDataId = Integer.valueOf(bsd.getDerovedFrom()[0]);
		initialEnrichedData(enrichedDataId);
		String stockIndex = bed.getStockIndex();
		float zscore30 = bed.getZscore30();
		float zscore90 = bed.getZscore90();
		float posMa3Before = bed.getPosMa3Before();
		float negMa3Before = bed.getNegMa3before();
		float dayChange = bed.getDayChange();
		float posMa3After = bed.getPosMa3After();
		float negMa3After = bed.getNegMa3After();
		String date = bed.getDate();
		int sequenceId = bed.sequenceId;
		
		
		//Check If need to throw a warning message
		//If the direction property of surrogate message is positive or negative, then throw a warning
		//Otherwise not
		String direction = bsd.getDirection();
		if(direction.equals("calm"))////Currenday surrogate is not a sigma event, do not need to be processed
		{
			System.out.println("No warning need to be thrown!");
			return null;
		}
		else
		{
			int duration = 0;
			if(direction.equals("positive"))
			{
				//This is positive event
				String sql = "select coe_ma3_before,coe_ma3_after,coe_day_change,intercept from least_squire_model_coefficient where stock_index=? and direction= ? ";
				PreparedStatement ps = connection.prepareStatement(sql);
				ps.setString(1, stockIndex);
				ps.setString(2, "positive");
				float coeMa3B = 0;
				float coeMa3A = 0;
				float coeDay = 0;
				float intercept = 0;
				
				ResultSet rs = ps.executeQuery();
				while(rs.next())
				{
					coeMa3B = rs.getFloat(1);
					coeMa3A = rs.getFloat(2);
					coeDay = rs.getFloat(3);
					intercept = rs.getFloat(4);
				}
				
				rs.close();
				ps.close();
				duration = (int) Math.ceil(Math.exp(coeMa3B*posMa3Before + coeMa3A*posMa3After + coeDay*dayChange + intercept));
			}
			else if(direction.equals("negative"))
			{
				//This is positive event
				String sql = "select coe_ma3_before,coe_ma3_after,coe_day_change,intercept from least_squire_model_coefficient where stock_index=? and direction= ? ";
				PreparedStatement ps = connection.prepareStatement(sql);
				ps.setString(1, stockIndex);
				ps.setString(2, "negative");
				float coeMa3B = 0;
				float coeMa3A = 0;
				float coeDay = 0;
				float intercept = 0;
				
				ResultSet rs = ps.executeQuery();
				while(rs.next())
				{
					coeMa3B = rs.getFloat(1);
					coeMa3A = rs.getFloat(2);
					coeDay = rs.getFloat(3);
					intercept = rs.getFloat(4);
				}
				
				rs.close();
				ps.close();
				duration = (int) Math.ceil(Math.exp(coeMa3B*negMa3Before + coeMa3A*negMa3After + coeDay*dayChange + intercept));
			}
			
			//Computing the next event date,need to exclude the weekends
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Calendar startDay = Calendar.getInstance();
			startDay.setTime(sdf.parse(date));
			while(duration>=0)
			{
				startDay.add(Calendar.DAY_OF_YEAR, 1);
				if(startDay.get(Calendar.DAY_OF_WEEK)!=7 || startDay.get(Calendar.DAY_OF_WEEK)!=1)
				{
					duration--;
				}
			}
			String predictDate = sdf.format(startDay.getTime());
			
			BloombergWarning blWaring = new BloombergWarning();
			//TODO: Because the model can not output a probability of the prediction, so we use a fix value here
			blWaring.setConfidence(80);
			blWaring.setConfidenceIsProbability(true);
			blWaring.setDate(date);
			blWaring.setDerivedFrom(new String[]{String.valueOf(sequenceId)});
			blWaring.setErmbers_id(UUID.randomUUID().toString());
			blWaring.setEventDate(predictDate);
			blWaring.setModel("TestModel");
			blWaring.setPopulation(stockIndex);
			
			Gson gson = new Gson();
			String warning = gson.toJson(blWaring);
			return warning;
		}
	}
	
	//TODO: Test
	public static void main(String[] args) throws JsonIOException, JsonSyntaxException, FileNotFoundException, SQLException, ParseException {
		Type surrogateType = new TypeToken<BloombergSurrogateData>(){}.getType();
		Gson gson = new Gson();
		BloombergSurrogateData bsd = gson.fromJson(new FileReader("TestSurrogateData.json"), surrogateType);
		
		LeastSquaresModel lsm = new LeastSquaresModel(gson.toJson(bsd));
		String Warning = lsm.process();
		
		System.out.println("Warning= " + Warning);
	}
	
	
}
