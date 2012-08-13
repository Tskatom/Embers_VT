package org.vt.common;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

/**
 * This Classs provide a Static Method to get the Connection to the MySql DataBase
 * @author wei
 *
 */
public class DBConnection {
	public static Connection connection;
	
	public static Connection getDBInstance()
	{
		if(connection!=null)
		{
			return connection;
		}
		else
		{
			try{
				Properties properties = new Properties();
				properties.load(new FileInputStream("stock.config.property"));
//				Mysql Database
//				String url = properties.getProperty("DBUrl");
//				String dbName = properties.getProperty("DBName");
//				String driver = properties.getProperty("driver");
//				String userName = properties.getProperty("userName");
//				String password = properties.getProperty("passwd");
//				Class.forName(driver).newInstance();
//				connection = DriverManager.getConnection(url+dbName,userName,password);
				
//				SQLite Database
				String dbName = properties.getProperty("SqliteDB");
				String driver =  properties.getProperty("SqliteDriver");
				Class.forName(driver).newInstance();
				connection = DriverManager.getConnection("jdbc:sqlite:"+dbName);
				
				
				System.out.println("Connect to the database!");
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		return connection;
	}
	
	public static void main(String[] args) {
		DBConnection.getDBInstance();
	}

}
