package com.bravo.model;

import java.util.Date;

public class Event {
	public int eventId;
	public int userId;
	public String name;
	public String type;
	public String start;
	public String end;
	public Date date;
	public int repeat;
	public String location;
	public int priority;
	
	public Event(int eId, int uId, String n, String t, String s, String e, Date d, int r, String l, int p) {
		eventId = eId;
		userId = uId;
		name = n;
		type = t;
		start = s;
		end = e;
		date = d;
		repeat = r;
		location = l;
		priority = p;
	}
}
