package org.vt.datapreprocess;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import org.vt.entity.Company;

public class CreateCompanyMemberJson {
	public void createMemJson(File filePath) throws IOException
	{
		File[] files = filePath.listFiles();
		for(File file:files)
		{
			//Read all the Csv Files
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line = "";
			String indexName = "";
			int i = 0;
			int companyNum=0;
			while((line=br.readLine())!=null)
			{
				//Skip the first title line
				if(i==0)
				{
					i++;
					continue;
				}
				else if(i==1)
				{
					indexName = line.split(",")[1].split(" ")[0];
					i++;
				}
				else
				{
					String[] infos =  line.split(",");
					String ticker = infos[1].split(" ")[0] + ":" + infos[1].split(" ")[1];
					String companyName = infos[2];
					
					Company company = new Company(ticker,companyName,indexName);
					
					companyNum++;
				}
			}
			System.out.println(indexName + "---" + companyNum);
		}
	}
	
	public static void main(String[] args) {
		CreateCompanyMemberJson ccmj = new CreateCompanyMemberJson();
		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream("config.property"));
			File filePath = new File(properties.getProperty("companyMemberDir"));
			ccmj.createMemJson(filePath);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
