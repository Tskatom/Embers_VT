package org.vt.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

public class CompanyCalculator {
	public static  void calZ30(HashMap<Integer,Float> value)
	{
		for(int j=32; j<value.size()+2;j++)
		{
			LinkedList<Float> ll = new LinkedList<Float>();
			int beginValue = j - 30;
			int endValue = j -1;
			float sumValue= 0f;
			for(int m = beginValue;m<=endValue;m++)
			{
				ll.add(value.get(m));
				sumValue+=value.get(m);
			}
			float mean = sumValue/ll.size();
			float sd = calSD(mean,ll);
			float zscore = (value.get(j)-mean)/sd;
			if((j==393)||j==352||j==391||j==573)
			{
				System.out.println(j+"--:"+zscore);
			}
		}
	}
	
	private static float calSD(float mean,LinkedList ll)
	{
		Iterator it = ll.iterator();
		float sumVal = 0.00f;
		while(it.hasNext())
		{
			float indexValue =(Float)it.next();
			sumVal += Math.pow((indexValue-mean), 2);
		}
		float sd = (float)Math.sqrt(sumVal/(ll.size()-1));
		return sd;
	}
	
	public static void main(String[] args) throws IOException {
		File file = new File("d:/MP_MERVAL.csv");
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line = "";
		int i = 0;
		HashMap<String,Integer> dateMap = new HashMap<String,Integer>();
		HashMap<Integer,Float> BMA = new HashMap<Integer,Float>();
		HashMap<Integer,Float> COME = new HashMap<Integer,Float>();
		HashMap<Integer,Float> EDN = new HashMap<Integer,Float>();
		HashMap<Integer,Float> ERAR = new HashMap<Integer,Float>();
		HashMap<Integer,Float> FRAN = new HashMap<Integer,Float>();
		HashMap<Integer,Float> GGAL = new HashMap<Integer,Float>();
		HashMap<Integer,Float> PAMP = new HashMap<Integer,Float>();
		HashMap<Integer,Float> PESA = new HashMap<Integer,Float>();
		HashMap<Integer,Float> TECO2 = new HashMap<Integer,Float>();
		HashMap<Integer,Float> TS = new HashMap<Integer,Float>();
		HashMap<Integer,Float> YPFD = new HashMap<Integer,Float>();
		
		
		
		while((line=br.readLine())!=null)
		{
			System.out.println(line);
			String[] info = line.split(",");
			if(i==0)
			{
				for(int j=2;j<info.length;j++)
				{
					dateMap.put(info[j]==null?"0":info[j], j);
				}
			}
			else if(i==1)
			{
				for(int j=2;j<info.length;j++)
				{
					BMA.put(j,Float.valueOf(info[j]==null?"0":info[j])-Float.valueOf(info[j-1]==null?"0":info[j-1]));
				}
			}
			else if(i==2)
			{
				for(int j=2;j<info.length;j++)
				{
					COME.put(j,Float.valueOf(info[j]==null?"0":info[j])-Float.valueOf(info[j-1]==null?"0":info[j-1]));
				}
			}
			else if(i==3)
			{
				for(int j=2;j<info.length;j++)
				{
					EDN.put(j,Float.valueOf(info[j]==null?"0":info[j])-Float.valueOf(info[j-1]==null?"0":info[j-1]));
				}
			}
			else if(i==4)
			{
				for(int j=2;j<info.length;j++)
				{
					ERAR.put(j,Float.valueOf(info[j]==null?"0":info[j])-Float.valueOf(info[j-1]==null?"0":info[j-1]));
				}
			}else if(i==5)
			{
				for(int j=2;j<info.length;j++)
				{
					FRAN.put(j,Float.valueOf(info[j]==null?"0":info[j])-Float.valueOf(info[j-1]==null?"0":info[j-1]));
				}
			}else if(i==6)
			{
				for(int j=2;j<info.length;j++)
				{
					GGAL.put(j,Float.valueOf(info[j]==null?"0":info[j])-Float.valueOf(info[j-1]==null?"0":info[j-1]));
				}
			}else if(i==7)
			{
				for(int j=2;j<info.length;j++)
				{
					PAMP.put(j,Float.valueOf(info[j]==null?"0":info[j])-Float.valueOf(info[j-1]==null?"0":info[j-1]));
				}
			}else if(i==8)
			{
				for(int j=2;j<info.length;j++)
				{
					PESA.put(j,Float.valueOf(info[j]==null?"0":info[j])-Float.valueOf(info[j-1]==null?"0":info[j-1]));
				}
			}else if(i==9)
			{
				for(int j=2;j<info.length;j++)
				{
					TECO2.put(j,Float.valueOf(info[j]==null?"0":info[j])-Float.valueOf(info[j-1]==null?"0":info[j-1]));
				}
			}else if(i==10)
			{
				for(int j=2;j<info.length;j++)
				{
					TS.put(j,Float.valueOf(info[j]==null?"0":info[j])-Float.valueOf(info[j-1]==null?"0":info[j-1]));
				}
			}else if(i==11)
			{
				for(int j=2;j<info.length;j++)
				{
					YPFD.put(j,Float.valueOf(info[j]==null?"0":info[j])-Float.valueOf(info[j-1]==null?"0":info[j-1]));
				}
			}
			i++;
		}
		br.close();
		for(String key:dateMap.keySet())
		{
			if(key.equals("6/9/2011")||key.equals("8/4/2011")||key.equals("8/8/2011")||key.equals("5/8/2012"))
			{
				System.out.print(key+":"+dateMap.get(key));
				System.out.println();
			}
		}
		
		System.out.println("BMA");
		CompanyCalculator.calZ30(BMA);
		System.out.println("COME");
		CompanyCalculator.calZ30(COME);
		System.out.println("EDN");
		CompanyCalculator.calZ30(EDN);
		System.out.println("ERAR");
		CompanyCalculator.calZ30(ERAR);
		System.out.println("FRAN");
		CompanyCalculator.calZ30(FRAN);
		System.out.println("GGAL");
		CompanyCalculator.calZ30(GGAL);
		System.out.println("PAMP");
		CompanyCalculator.calZ30(PAMP);
		System.out.println("PESA");
		CompanyCalculator.calZ30(PESA);
		System.out.println("TECO2");
		CompanyCalculator.calZ30(TECO2);
		System.out.println("TS");
		CompanyCalculator.calZ30(TS);
		System.out.println("YPFD");
		CompanyCalculator.calZ30(YPFD);
	}

}
