package com.bravo.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

import com.bravo.model.*;
import com.bravo.utils.Mysql;
import com.bravo.utils.Utils;

public class EventController {
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
    
	public EventController() {
	}

	public void addEvent(Event e, ArrayList<Long> eventUsers, boolean isUpdating, boolean repeatEdit) {
		for (int i = 0; i < eventUsers.size(); i += 1) {
			if (isUpdating && !repeatEdit) { e.repeat = 1;} 
			long timeCounter = e.date.getTime();
			for (int j = 0; j < e.repeat; j += 1) {
				String eventParsedDate = dateFormat.format(timeCounter);
				timeCounter += (86400000*7);
				if (e.type.equals("Static") || (e.type.equals("Dynamic") && isUpdating)) {
					ArrayList<HashMap<String,Object>> currentEvents = Mysql.query("SELECT * FROM timetable WHERE userId='"+eventUsers.get(i)+"' AND start='"+e.start+"' AND date='"+eventParsedDate+"'");
					if (!isUpdating || currentEvents.size() == 0) {
						Mysql.query("INSERT INTO timetable (userId, name, type, start, end, date, location, priority) VALUES ('"+eventUsers.get(i)+"', '"+e.name+"', '"+e.type+"', '"+e.start+"', '"+e.end+"', '"+eventParsedDate+"', '"+e.location+"', '"+e.priority+"')");
					} else {
						HashMap<String,Object> currentEvent = currentEvents.get(0);
						
						//Removes repeat events which are no longer valid
						int oldRepeat = calcRepeating(currentEvent);
						long timeCounterDel = e.date.getTime();
						if (j == 0 && repeatEdit && oldRepeat > 1 && e.repeat < oldRepeat) {
							for (int k = 0; k < oldRepeat; k += 1) {
								if (k >= e.repeat) {
									String eventParsedDateDel = dateFormat.format(timeCounterDel);
									Mysql.query("DELETE FROM timetable WHERE userId='"+eventUsers.get(i)+"' AND name='"+e.name+"' AND type='"+e.type+"' AND start='"+e.start+"' AND end='"+e.end+"' AND date='"+eventParsedDateDel+"' AND location='"+e.location+"' AND priority='"+e.priority+"'");
								}
								timeCounterDel += (86400000*7);
							}
						}
						
						//Handle event update/creation
						if (currentEvent.get("type").equals("Static")) {
							if ((Integer) currentEvent.get("priority") > e.priority) {
								//If type is static with higher priority, notify user that event cannot be overwritten and request new slot (or dynamically allocate)
							} else if ((Integer) currentEvent.get("priority") < e.priority) {
								//If type is static with lower priority, confirm overwrite, find out if user wants to reschedule the replaced event
							} else {
								//If type is static with equal priority, request choice, overwrite and request reschedule
							}
						} else if (currentEvent.get("type").equals("Dynamic")) {
							if ((Integer) currentEvent.get("priority") > e.priority) {
								//[Logically this should never occur]
								//If type is dynamic with higher priority, schedule event for another time (Dynamically, static events will never be overuled by dynamic)
							} else if ((Integer) currentEvent.get("priority") < e.priority) {
								//If type is dynamic with lower priority, schedule event and reschedule any overwritten events dynamically
							} else {
								//[Logically this should never occur]
								//If type is dynamic with equal priority, then schedule event elsewhere
							}
						}
						Mysql.query("UPDATE timetable SET userId='"+eventUsers.get(i)+"', name='"+e.name+"', type='"+e.type+"', start='"+e.start+"', end='"+e.end+"', date='"+eventParsedDate+"', location='"+e.location+"', priority='"+e.priority+"' WHERE eventId='"+currentEvent.get("eventId")+"'");
					}
				} else if (e.type.equals("Dynamic") && !isUpdating) {
					//Dynamic Algorithm to go here
				}
			}
		}
	}
    
    public int calcRepeating(HashMap<String,Object> currentEvent) {
    	int repeatCounter = 0; boolean repeating = true;
    	long timeCounter = Utils.parseDate((String) currentEvent.get("date")).getTime();
    	while (repeating) {
			String eventParsedDate = dateFormat.format(timeCounter);
			timeCounter += (86400000*7);
			ArrayList<HashMap<String,Object>> query = Mysql.query("SELECT * FROM timetable WHERE userId='"+currentEvent.get("userId")+"' AND name='"+currentEvent.get("name")+"' AND start='"+currentEvent.get("start")+"' AND end='"+currentEvent.get("end")+"' AND date='"+eventParsedDate+"'");
			if (query.size() == 0) {
				repeating = false;
			} else {
				repeatCounter += 1;
			}
		}
    	return repeatCounter;
    }
}
