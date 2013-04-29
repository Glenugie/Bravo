package com.bravo.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import com.bravo.utils.Mysql;

public class Event {
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
	public int eventId;
	public long userId;
	public String name;
	public String type;
	public String start;
	public String end;
	public Date date;
	public int location;
	public int priority;
	
	public Event(int eId, long uId, String n, String t, String s, String e, Date d, String l, int p) {
		eventId = eId;
		userId = uId;
		name = n;
		type = t;
		start = s;
		end = e;
		date = d;
		//City, Street, Postcode
		try {
			String[] temp = l.split(",");
			HashMap<String,Object> result = Mysql.query("INSERT INTO address (city, street, postcode) VALUES ('"+temp[0]+"', '"+temp[1]+"', '"+temp[2]+"')").get(0);
			location = (int) result.get("addressID");
		} catch (Exception ex) {
			location = -1;
		}
		priority = p;
	}
	
	public Event(int eId, long uId) {
		HashMap<String,Object> eventQuery = Mysql.query("SELECT * FROM event WHERE eventId='"+eId+"' AND userId='"+uId+"'").get(0);
		eventId = (int) eventQuery.get("eventId");
		userId = (int) eventQuery.get("userId");
		name = (String) eventQuery.get("name");
		type = (String) eventQuery.get("type");
		start = (String) eventQuery.get("start");
		end = (String) eventQuery.get("end");
		try {
			date = dateFormat.parse((String) eventQuery.get("date"));
		} catch (ParseException e) {
			System.err.println("Unable to parse date");
		}
		location = (int) eventQuery.get("addressID");
		priority = (int) eventQuery.get("priority");
	}
}
