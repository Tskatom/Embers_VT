package org.vt.common;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedList;

public class Calculator {

	public static float calZscore(float xa,LinkedList<Float> ma)
	{
		float sumValue = 0;
		for(Float value:ma)
		{
			sumValue += value;
		}
		float mean = sumValue/ma.size();
		
		float sd = calSD(mean,ma);
		if(sd==0f)
		{
			return 0f;
		}
		float zScore = (xa-mean)/sd;
		return zScore;
	}
	
	private static float calSD(float mean, LinkedList<Float> ll) {
		float sumVal = 0;
		for(Float indexValue:ll)
		{
			sumVal += Math.pow((indexValue-mean),2);
		}
		if(ll.size()==1)
		{
			return 0f;
		}
		float sd = (float)Math.sqrt(sumVal/(ll.size()-1));
		return sd;
	}

}
