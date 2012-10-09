package org.vt.test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;

import com.google.gson.Gson;

public class ConvertStockCSV2Json {
	
	public class StockPrice{
		public String name;
		public LinkedList<Price> prices;
		
		public StockPrice(String name)
		{
			this.name = name;
			prices = new LinkedList<Price>();
		}
	}
	
	public class Price{
		public String date;
		public float lastPrice;
		public float previousLastPrice;
		
		public Price(String date,float lastPrice,float previousLastPrice)
		{
			this.date = date;
			this.lastPrice = lastPrice;
			this.previousLastPrice = previousLastPrice;
		}
	}
	
	public void transfer() throws NumberFormatException, IOException
	{
		File filePath = new File("D:/embers/Stock Index/Index Value/csv");
		String storePath = "D:/embers/Stock Index/Json";
		File[] files = filePath.listFiles();
		for(File file:files)
		{
			if(file.isFile())
			{
				String fileName = file.getName();
				String stockName = fileName.substring(0, fileName.indexOf(".csv"));
				StockPrice sp = new StockPrice(stockName);
				BufferedReader br = new BufferedReader(new FileReader(file));
				String line = "";
				System.out.println(file.getName());
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
					if(info[1].equals("#N/A N/A")||info[3].equals("#N/A N/A"))
					{
						i++;
						continue;
					}
					float lastPrice = Float.valueOf(info[1]);
					float previousLastPrice = Float.valueOf(info[3]);
					
					Price price = new Price(date,lastPrice,previousLastPrice);
					
					sp.prices.add(price);
					i++;
				}
				
				Gson gson = new Gson();
				String storeFileName = storePath + "/" + stockName;
				BufferedWriter bw = new BufferedWriter(new FileWriter(storeFileName));
				String json = gson.toJson(sp);
				bw.write(json);
				bw.flush();
				bw.close();
			}
		}
	}
	
	public static void main(String[] args) throws IOException {
		
		ConvertStockCSV2Json cc2j = new ConvertStockCSV2Json();
		cc2j.transfer();
	
	}

}
