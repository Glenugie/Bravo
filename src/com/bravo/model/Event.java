package com.bravo.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import com.bravo.utils.*;

public class Event {
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
	public int eventId;
	public long userId;
	public String name;
	public String type;
	public String start;
	public String end;
	public Date date;
	public int chain;
	public String location;
	public int priority;
	
	public Event(int eId, long uId, String n, String t, String s, String e, Date d, String l, int p) {
		eventId = eId;
		userId = uId;
		name = n;
		type = t;
		start = s;
		end = e;
		date = d;
		chain = -1;
		location = l;
		priority = p;
	}
	
	public Event(int eId) {
		HashMap<String,Object> eventQuery = Mysql.query("SELECT * FROM timetable WHERE id='"+eId+"'").get(0);
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
		chain = (int) eventQuery.get("nextChain");
		location = (String) eventQuery.get("location");
		priority = (int) eventQuery.get("priority");
	}
}
