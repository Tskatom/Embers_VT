package org.vt.test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;


import com.google.gson.Gson;

public class ConvertCurrencyCsv2Json {
	
	public class Currency{
		public String name;
		public LinkedList<Price> prices;
		
		public Currency(String name)
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
		File filePath = new File("D:/embers/Currency/csv");
		String storePath = "D:/embers/Currency/Json";
		File[] files = filePath.listFiles();
		for(File file:files)
		{
			if(file.isFile())
			{
				String fileName = file.getName();
				String currencyName = fileName.substring(0, fileName.indexOf(".csv"));
				Currency currency = new Currency(currencyName);
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
					if(info.length==0)
					{
						i++;
						continue;
					}
					String date = info[0];
					if(info[1].equals("#N/A N/A")||info[2].equals("#N/A N/A"))
					{
						i++;
						continue;
					}
					float lastPrice = Float.valueOf(info[1]);
					float previousLastPrice = Float.valueOf(info[2]);
					
					Price price = new Price(date,lastPrice,previousLastPrice);
					
					currency.prices.add(price);
					i++;
				}
				
				Gson gson = new Gson();
				String storeFileName = storePath + "/" + currencyName;
				BufferedWriter bw = new BufferedWriter(new FileWriter(storeFileName));
				String json = gson.toJson(currency);
				bw.write(json);
				bw.flush();
				bw.close();
			}
		}
	}
	
	public static void main(String[] args) throws IOException {
		
		ConvertCurrencyCsv2Json cc2j = new ConvertCurrencyCsv2Json();
		cc2j.transfer();
	
	}

}
