package org.vt.model;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.sql.Connection;

import org.vt.common.DBConnection;
import org.vt.entity.BloombergEnrichedData;
import org.vt.entity.BloombergSurrogateData;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

/**
 * This program is used to process the Enriched Data and output a surrogate data
 * @author wei
 *
 */
public class EnrichdataProcess {
	
	private BloombergEnrichedData bed;
	private BloombergSurrogateData bsd;
	private Connection connection;
	
	public EnrichdataProcess(String enrichedData)
	{
		connection = DBConnection.getDBInstance();
		Type enrichDataType = new TypeToken<BloombergEnrichedData>(){}.getType();
		Gson gson = new Gson();
		bed = gson.fromJson(enrichedData, enrichDataType);
	}
	
	public String process()
	{
		String surrogateData = "";

		//Setup the surrogate parameters
		bsd = new BloombergSurrogateData();
		String date = bed.date;
		String[] derivedFrom = {String.valueOf(bed.sequenceId)};
		String model = "Test Model";
		String location = bed.location;
		String population = bed.stockIndex;
		int confidence =  60;
		boolean confidenceIsProbability = false;
		String shiftType = "Trend";
		String valueSpectrum = "Zscore Sigma";
		String direction;
		String strength = String.valueOf("Z30:"+bed.zscore30 + " Z90:" + bed.zscore90);
		String shiftDate = bed.date;
				
		//If the zsocre30 is bigger than 4 or zscore90 is bigger than 3, then we set the direction as positive
		//Else if the |zscore30| <=4 and |zscore90|<=3,then we set the dirction as calm
		//Else zsocre30 is less than -4 or zscore90 is less than -3, then we set the direction negative
		if(bed.zscore30>=4 || bed.zscore90 >=3)
		{
			direction = "positive";
		}
		else if(bed.zscore30<=-4 || bed.zscore90<=-3)
		{
			direction = "negative";
		}
		else
		{
			direction = "calm";
		}
		
		bsd.setConfidence(confidence);
		bsd.setConfidenceIsProbability(confidenceIsProbability);
		bsd.setDate(date);
		bsd.setDerovedFrom(derivedFrom);
		bsd.setDirection(direction);
		bsd.setLocation(location);
		bsd.setModel(model);
		bsd.setPopulation(population);
		bsd.setShiftDate(shiftDate);
		bsd.setShiftType(shiftType);
		bsd.setStrength(strength);
		bsd.setValueSpectrum(valueSpectrum);
		
		//Output surrogate data as json format
		Gson gson = new Gson();
		surrogateData = gson.toJson(bsd);
		return surrogateData;
	}
	
	//TODO: Test the Function
	public static void main(String[] args) throws JsonIOException, JsonSyntaxException, FileNotFoundException {
		Type enrichDataType = new TypeToken<BloombergEnrichedData>(){}.getType();
		Gson gson = new Gson();
		BloombergEnrichedData bed = gson.fromJson(new FileReader("TestEnrichedData.json"), enrichDataType);
		
		EnrichdataProcess ep = new EnrichdataProcess(gson.toJson(bed));
		String surrogate = ep.process();
		
		System.out.println("surrogate= " + surrogate);
	}
	

}
