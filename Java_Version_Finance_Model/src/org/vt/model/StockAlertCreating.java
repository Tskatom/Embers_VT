package org.vt.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;

public class StockAlertCreating {
	
	public String process(String rawData) throws SQLException, ParseException
	{
		RawdataProcess rp = new RawdataProcess(rawData);
		String enrichedData = rp.process();
		
		EnrichdataProcess ep = new EnrichdataProcess(enrichedData);
		String surrogate = ep.process();
		
		LeastSquaresModel lsm = new LeastSquaresModel(surrogate);
		String warning = lsm.process();
		
		return warning;
	}
	
	public static void main(String[] args) {
		//Read the daily stock index value from file
		String fileName = args[0];
		File file = new File(fileName);
		String dailyIndex = "";
		StockAlertCreating stockAlertCre = new StockAlertCreating();
		try 
		{
			BufferedReader br = new BufferedReader(new FileReader(file));
			while((dailyIndex=br.readLine())!=null)
			{
				String warning = stockAlertCre.process(dailyIndex);
				System.out.println("Warning=" + warning);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
