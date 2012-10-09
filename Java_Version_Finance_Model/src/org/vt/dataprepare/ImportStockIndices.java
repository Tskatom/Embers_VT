package org.vt.dataprepare;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.vt.common.DBConnection;

/**
 * This tool is used to import the historical stock index data
 * 
 * @author wei
 * @version 2012-7-25
 */
public class ImportStockIndices {
	
	private Connection connection;
	private PreparedStatement ps;
	
	public ImportStockIndices() throws SQLException
	{
		connection = DBConnection.getDBInstance();
		String sql = "insert into t_daily_stockindices (sub_sequence,stock_index,date,last_price,one_day_change) values (?,?,?,?,?)";
		ps = connection.prepareStatement(sql);
	}
	
	public void clear() throws SQLException
	{
		String sql = "delete from t_daily_stockindices";
		PreparedStatement ps2 = connection.prepareStatement(sql);
		ps2.execute();
		ps2.close();
	}
	public void close() throws SQLException
	{
		ps.close();
		connection.close();
	}
	
	public void importData() throws NumberFormatException, IOException, SQLException, ParseException
	{
		clear();
		
		File filePath = new File("D:/embers/Stock Index/Index Value/csv");
		File[] files = filePath.listFiles();
		for(File file:files)
		{
			if(file.isFile())
			{
				String fileName = file.getName();
				String stockName = fileName.substring(0, fileName.indexOf(".csv"));
				BufferedReader br = new BufferedReader(new FileReader(file));
				String line = "";
				System.out.println(file.getName());
				int subSequence = 0;
				int i =0;
				while((line=br.readLine())!=null)
				{
					if(i==0||i==1)
					{
						System.out.println(line);	
						i++;
						continue;
					}
					String[] info = line.split(",");
					String date = info[0];
					if(info[1].equals("#N/A N/A")||info[2].equals("#N/A N/A"))
					{
						i++;
						continue;
					}
					float lastPrice = Float.valueOf(info[1]);
					float previousLastPrice = Float.valueOf(info[2]);
					float oneDayChange = lastPrice - previousLastPrice;
					ps.setInt(1, ++subSequence);
					ps.setString(2, stockName);
					SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
					Date currentDay = new Date(sdf.parse(date).getTime());
					ps.setDate(3, currentDay);
					ps.setFloat(4, lastPrice);
					ps.setFloat(5, oneDayChange);
					
					ps.execute();
					i++;
					
				}
				
			}
		}
	}
	
	public static void main(String[] args) throws ParseException {
		try {
			 ImportStockIndices isi = new ImportStockIndices();
			 isi.importData();
			 isi.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
