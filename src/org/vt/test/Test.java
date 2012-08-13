package org.vt.test;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import flanagan.analysis.Stat;

public class Test {
public static void main(String[] args) throws ParseException, SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException {
	String timeStr = "2010-04-22 14:55:09.0";
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
	Timestamp ts = new Timestamp(sdf.parse(timeStr).getTime());
	System.out.println(ts.toString());
	int i = 10;
	if(i>2)
	{
		System.out.println(i);
	}
	else if(i>3)
	{
		System.out.println("--");
	}
	
	double[] ds = {1,1,2,3};
	double sd = Stat.mean(ds);
	System.out.println(sd);
	
	Calendar ca = Calendar.getInstance();
	int duration = 10;
	SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
	while(duration>=0)
	{
		ca.add(Calendar.DAY_OF_MONTH, 1);
		duration--;
		System.out.println(sf.format(ca.getTime()) + "--" +ca.get(Calendar.DAY_OF_WEEK));
	}
	
	Date t = new Date(1342065600000l);
	System.out.println(sf.format(t));
	
	
	String fileName = "d:/Sqlite/embers.db";
	Class.forName("org.sqlite.JDBC").newInstance();
	Connection conn = DriverManager.getConnection("jdbc:sqlite:"+fileName);
	String sql = "select date from t_daily_stockindices where date>date(?)";
	PreparedStatement pst = conn.prepareStatement(sql);
	pst.setString(1, "2002-01-03");
	ResultSet rs = pst.executeQuery();
	while(rs.next())
	{
		System.out.println(rs.getString(1));
	}
}
}
