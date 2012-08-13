package org.vt.dataprepare;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Properties;

import org.vt.common.DBConnection;

/**
 * This tool is used to import the GSR event
 * 
 * @author wei
 * @version 2012-7-24
 */
public class ImportGSR {
	
	private Connection connection;
	private Properties properties;
	
	public ImportGSR() throws FileNotFoundException, IOException
	{
		connection = DBConnection.getDBInstance();
		properties = new Properties();
		properties.load(new FileInputStream("config.property"));
	}
	
	public void clearOldData() throws SQLException
	{
		String clearSql = "delete from gsr";
		Statement st = connection.createStatement();
		st.execute(clearSql);
		st.close();
	}
	
	public void importData(String fileName) throws IOException, SQLException, ParseException
	{
		clearOldData();
		
		String gsrPath = properties.getProperty("gsrDir");
		File gsr = new File(gsrPath+fileName);
		BufferedReader br = new BufferedReader(new FileReader(gsr));
		String line = "";
		String sql = "insert into gsr (event_id,event_type,country,event_code,population,date) values (?,?,?,?,?,?)";
		PreparedStatement ps = connection.prepareStatement(sql);
		int i = 0;
		while((line=br.readLine())!=null)
		{
			if(i==0)
			{
				i++;
				continue;
			}
			String[] infos = line.split(",");
			int eventId = Integer.valueOf(infos[0]);
			String country = infos[1];
			String eventCode = infos[2];
			String population = infos[3];
			SimpleDateFormat dsf = new SimpleDateFormat("MM/dd/yyyy");
			Date date = new Date(dsf.parse(infos[4]).getTime());
			String eventType = "";
			if(eventCode.startsWith("41"))
			{
				eventType = "STOCK";
			}
			else if(eventCode.startsWith("42"))
			{
				eventType = "CURRENCY";
			}
			ps.setInt(1, eventId);
			ps.setString(2, eventType);
			ps.setString(3, country);
			ps.setString(4, eventCode);
			ps.setString(5, population);
			ps.setDate(6, date);
			
			ps.execute();
		}
		
		ps.close();
	}
	
	
	public void close() throws SQLException
	{
		connection.close();
	}
	public static void main(String[] args) {
		
		try {
			ImportGSR igsr = new ImportGSR();
			igsr.importData("GSR-2012-7-14.csv");
			igsr.close();
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
