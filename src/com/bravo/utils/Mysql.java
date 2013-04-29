package com.bravo.utils;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Mysql {
	private static String proxyServer = "jdbc:mysql://127.0.0.1:3306/shiftout_cs3024b?user=shiftout_cs3024b&password=cs3024bpassword"; //Bypasses proxy when .bashrc is modified
	private static String nonproxyServer = "jdbc:mysql://shiftout.co.uk:3306/shiftout_cs3024b?user=shiftout_cs3024b&password=cs3024bpassword"; //Gets used when no proxy is set
	private static String localServer = "jdbc:mysql://127.0.0.1:3306/timetable?user=root&cs3024";
	private static boolean local = false;
	private static String server;
	
	public static boolean testConnection() {
		server = nonproxyServer;
		if (local) { server = localServer;}
		System.out.println("Testing Mysql Conection...");
		try {
			Class.forName("org.gjt.mm.mysql.Driver");
			DriverManager.getConnection(server);
		} catch (Exception e1) {
        	System.out.println("Mysql Connection Failure using no proxy");
    		System.out.println("Attempting connection with proxy");
    		server = proxyServer;
    		try {
    			Class.forName("org.gjt.mm.mysql.Driver");
    			DriverManager.getConnection(server);
    		} catch (Exception e2) {
            	System.out.println("Mysql Connection Failure using proxy");
            	return false;
    		}
		}
    	System.out.println("Mysql Connection Success");
		return true;
	}
	
	public static ArrayList<HashMap<String, Object>> query(String query) {
		try {
		    Class.forName("com.mysql.jdbc.Driver");
		    Connection connect = DriverManager.getConnection(server);
		    PreparedStatement statement = null;
		    ResultSet resultSet = null;
		    ArrayList<HashMap<String, Object>> result = new ArrayList<HashMap<String, Object>>();
		    
		    //Parses SQL query into a secure prepared statement
		    String[] queryArray = query.split("\'");
		    String newQuery = "";
		    for (int i = 0; i < queryArray.length; i += 2) { newQuery += queryArray[i]+"?";}
		    if (query.charAt(query.length()-1) != "\'".charAt(0)) { newQuery = newQuery.substring(0,newQuery.length()-1);}
		    statement = connect.prepareStatement(newQuery);
		    int j = 1; for (int i = 1; i < queryArray.length; i += 2) { statement.setString(j,queryArray[i]); j += 1;}
		    
		    String queryType = query.split(" ")[0];
		    if (queryType.equals("SELECT")) {
		    	resultSet = statement.executeQuery(query);
		    } else if (queryType.equals("INSERT") || queryType.equals("UPDATE") || queryType.equals("DELETE")) {
		    	 statement.executeUpdate(query);
		    	 resultSet = statement.getGeneratedKeys();
		    }
		    while (resultSet.next()) {
				HashMap<String, Object> row = new HashMap<String, Object>();
				for (int col = 1; col <= resultSet.getMetaData().getColumnCount(); col += 1) {
					row.put(resultSet.getMetaData().getColumnName(col), resultSet.getObject(col));
				}
				result.add(row);
			}

			if (resultSet != null) { resultSet.close();}
			if (statement != null) { statement.close();}
			if (connect != null) { connect.close();}
			return result;
		} catch (Exception e) {
			Utils.error("Unable to execute Mysql Query");
			e.printStackTrace();
		}
		return null;
	}
	
	public static Object queryTerm(String term, String table, String where) {
		try {
			return query("SELECT "+term+" FROM "+table+" "+where).get(0).get(term);
		} catch (Exception e) {
			return null;
		}
	}
}
