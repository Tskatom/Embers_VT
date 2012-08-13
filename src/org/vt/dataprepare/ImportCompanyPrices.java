package org.vt.dataprepare;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.vt.common.DBConnection;

/**
 * This program is used to import the company member price of stock Index
 * @author wei
 *@version 2012-7-15
 */
public class ImportCompanyPrices {
	private Connection connection;
	private PreparedStatement ps;
	
	public ImportCompanyPrices() throws SQLException
	{
		connection = DBConnection.getDBInstance();
		String insertSql = "insert into t_daily_companyprice (sub_sequence,company_ticker,stock_index,date,last_price,one_day_change) values (?,?,?,?,?,?)";
		ps = connection.prepareStatement(insertSql);
	}

	public void clear() throws SQLException
	{
		String clearSql = "delete from t_daily_companyprice";
		Statement st = connection.createStatement();
		st.execute(clearSql);
		st.close();
	}
	
	public void close() throws SQLException
	{
		ps.close();
		connection.close();
	}
	
	public void importData() throws IOException, ParseException, SQLException
	{
		clear();
		String filePath = "D:/embers/Stock Index/MemPrice/csv";
		File[] files = new File(filePath).listFiles();
		for(File file:files)
		{
			if(file.isFile()&&file.getName().startsWith("COM_"))
			{
				String[] companies;
				//get the stock name
				String fileName = file.getName();
				String stockName = file.getName().substring(4, fileName.indexOf(".csv"));
				System.out.println(stockName);
				//Get the number of companies
				BufferedReader br = new BufferedReader(new FileReader(file));
				String line = "";
				int i = 0;
				while((line=br.readLine())!=null)
				{
					++i;
				}
				companies = new String[i];
				int j = 0;
				br = new BufferedReader(new FileReader(file));
				while((line=br.readLine())!=null)
				{
					companies[j++]=line.split(" ")[0]+":"+line.split(" ")[1];
				}
				
				//Start to read the price files
				String priceFileName = "CP_"+stockName+".csv";
				BufferedReader pbr = new BufferedReader(new FileReader(filePath+"/"+priceFileName));
				int m = 0;
				while(pbr.readLine()!=null)
				{
					m++;
				}
				String[] comPrices = new String[m];
				pbr = new BufferedReader(new FileReader(filePath+"/"+priceFileName));
				int n = 0;
				line = "";
				while((line=pbr.readLine())!=null)
				{
					comPrices[n++] = line;
				}
				
				//insert the Data into Database
				for(i=0;i<companies.length;i++)
				{
					System.out.println(i+"---"+companies[i]);
					//get the date & prices
					String dateLine = comPrices[2*i];
					String priceLine = comPrices[2*i+1];
//					System.out.println(dateLine);
//					System.out.println(priceLine);
					String[] dates = dateLine.split(",");
					String[] prices = priceLine.split(",");
					System.out.println(dates.length);
					System.out.println(prices.length);
					int subSequence = 0;
					for(j=0;j<dates.length;j++)
					{
						SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
						Date currDay = new Date(sdf.parse(dates[j]).getTime());
						String companyTicker = companies[i];
						String stockIndex = stockName;
						float lastPrice = Float.valueOf(prices[j]);
						float oneDayChange = 0f;
						if(j!=0)
						{
							oneDayChange = lastPrice - Float.valueOf(prices[j-1]);
						}
						ps.setInt(1, ++subSequence);
						ps.setString(2, companyTicker);
						ps.setString(3, stockIndex);
						ps.setDate(4, currDay);
						ps.setFloat(5, lastPrice);
						ps.setFloat(6, oneDayChange);
						ps.execute();
					}
					
				}
				
			}
		}
	}
	
	public static void main(String[] args) {
		try {
			ImportCompanyPrices icp = new ImportCompanyPrices();
			icp.importData();
			icp.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}


