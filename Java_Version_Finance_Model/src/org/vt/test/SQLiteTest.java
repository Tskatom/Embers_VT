package org.vt.test;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;

import org.vt.common.DBConnection;

public class SQLiteTest {
public static void main(String[] args) {
try {
		// The SQLite (3.3.8) Database File
		// This database has one table (pmp_countries) with 3 columns (country_id, country_code, country_name)
		// It has like 237 records of all the countries I could think of.
		String fileName = "d:/Sqlite/embers.db";
		// Driver to Use
		// http://www.zentus.com/sqlitejdbc/index.html　　Class.forName("org.sqlite.JDBC");
		// Create Connection Object to SQLite Database
		// If you want to only create a database in memory, exclude the +fileName
		Class.forName("org.sqlite.JDBC").newInstance();
		Connection conn = DriverManager.getConnection("jdbc:sqlite:"+fileName);
		// Create a Statement object for the database connection, dunno what this stuff does though.
		int i = 0;
		String sql = "insert into t_daily_stockindices (sub_sequence,stock_index,date,last_price,one_day_change,zscore30,zscore90) values (?,?,?,?,?,?,?)";
		PreparedStatement ps = conn.prepareStatement(sql);
		
		Connection mysqlConn = DBConnection.getDBInstance();
		Statement st = mysqlConn.createStatement();
		String sqlQuery = "select sub_sequence,stock_index,date,last_price,one_day_change,zscore30,zscore90 from t_daily_stockindices";
		SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
		ResultSet rs = st.executeQuery(sqlQuery);
		while(rs.next())
		{
			ps.setInt(1, rs.getInt(1));
			ps.setString(2, rs.getString(2));
			ps.setString(3, sf.format(rs.getDate(3)));
			ps.setFloat(4, rs.getFloat(4));
			ps.setFloat(5,rs.getFloat(5) );
			ps.setFloat(6,rs.getFloat(6));
			ps.setFloat(7, rs.getFloat(7));
			ps.execute();
			System.out.println(i++);
		}
		rs.close();
		ps.close();
		conn.close();
		mysqlConn.close();
}
catch (Exception e) {
// Print some generic debug info
System.out.println(e.getMessage());
System.out.println(e.toString());
}
}
}