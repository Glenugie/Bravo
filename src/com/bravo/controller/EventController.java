package com.bravo.controller;

import java.awt.Dimension;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JFrame;

import com.bravo.App;
import com.bravo.model.*;
import com.bravo.utils.Mysql;
import com.bravo.utils.Utils;
import com.bravo.view.EventDialog;
import com.bravo.view.MainWindow;

public class EventController {
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
	public final int timeSlot = 15; // In minutes
	public User user = new User(-4);
	private MainWindow mainWindow;
	private ArrayList<Integer> overwrittenEvents;
    
	public EventController(MainWindow mw) {
		mainWindow = mw;
	}

	public boolean addEvent(Event e, int chainLength, ArrayList<Long> eventUsers, boolean isUpdating, boolean repeatEdit) {
		boolean successful = true;
		overwrittenEvents = new ArrayList<Integer>();
		if (isUpdating) { chainLength = calcRepeating(e);}
		if (!repeatEdit) { chainLength = 1;}
		
		if (e.type.equals("Static") || (e.type.equals("Dynamic") && isUpdating)) {
			boolean clashFree = true;
			boolean otherUser = false;
			boolean chainClash = false;
			for (int i = 0; i < eventUsers.size(); i += 1) {
				long timeCounter = e.date.getTime();
				for (int j = 0; j < chainLength; j += 1) {
					String eventParsedDate = dateFormat.format(timeCounter);
					timeCounter += (86400000*7);
					if (!slotFree(e.eventId,e.start,e.end,eventParsedDate,eventUsers.get(i))) {
						clashFree = false;
						if (eventUsers.get(i) != user.getId()) { otherUser = true;}
						if (chainLength > 1) { chainClash = true;}
					}
				}
			}
			
			if (clashFree || (!clashFree && !otherUser && !chainClash && Utils.question("This event clashes with a pre-existing event, would you like to overwrite?")) || (!clashFree && !otherUser && chainClash && Utils.question("This event chain clashes with a pre-existing event, would you like to overwrite? (You will not be able to reschedule the overwritten event)"))) {
				int newId; try{ newId = (int)Mysql.queryTerm("id","timetable","ORDER BY id DESC LIMIT 1")+1;} catch (Exception ex) { newId = 1;}
				//int oldId = newId;
				for (int i = 0; i < eventUsers.size(); i += 1) {
					long timeCounter = e.date.getTime();
					//for (int j = 0; j < chainLength; j += 1) {
						String eventParsedDate = dateFormat.format(timeCounter);
						timeCounter += (86400000*7);
						//if (j >= calcRepeating(e)) { isUpdating = false;}
						if (isUpdating) {
							newId = e.eventId;
							Mysql.query("UPDATE timetable SET name='"+e.name+"', type='"+e.type+"', start='"+e.start+"', end='"+e.end+"', " +
								"date='"+eventParsedDate+"', location='"+e.location+"', priority='"+e.priority+"' WHERE eventId='"+newId+"' AND userId='"+eventUsers.get(i)+"'");
						} else {
							Mysql.query("INSERT INTO timetable (eventId, userId, name, type, start, end, date, location, priority, nextChain) " +
								"VALUES ('"+newId+"', '"+eventUsers.get(i)+"', '"+e.name+"', '"+e.type+"', '"+e.start+"', '"+e.end+"', '"+eventParsedDate+"', '"+e.location+"', '"+e.priority+"', '-1')");
							/*if (j != 0) {
								int id = (int)Mysql.queryTerm("id", "timetable", "WHERE start='"+e.start+"' AND date='"+eventParsedDate+"'AND eventId='"+newId+"' AND userId='"+eventUsers.get(i)+"'");
								Mysql.query("UPDATE timetable SET nextChain='"+id+"' WHERE id='"+oldId+"'");
								oldId = id;
							}*/
						}
					//}
				}
				if (!clashFree && !chainClash && Utils.question("Do you want to reschedule the overwritten event?")) {
					/*HashMap<String,Object> overwrittenEvent = Mysql.query("SELECT * FROM timetable WHERE userId='"+user.getId()+"' AND name='"+e.name+"' AND type='"+e.type+"' AND start='"+e.start+"' AND end='"+e.end+"', date='"+e.date+"', location='"+e.location+"', priority='"+e.priority+"'").get(0);
					EventDialog eventDialog = new EventDialog(mainWindow, this, App.getApplication().getMainFrame(), true, user.getId(), (String)overwrittenEvent.get("start"), (String)overwrittenEvent.get("date"), (int)overwrittenEvent.get("eventId"));
					eventDialog.pack();
					eventDialog.setLocationRelativeTo(null);
					eventDialog.setSize(new Dimension(400, 400));
					eventDialog.setVisible(true);*/
					Utils.error("Currently unable to reschedule events");
					for (int i = 0; i < overwrittenEvents.size(); i += 1) { Mysql.query("DELETE FROM timetable WHERE eventId='"+overwrittenEvents.get(i)+"' AND userId='"+user.getId()+"'");}
				} else if (!clashFree) {
					for (int i = 0; i < overwrittenEvents.size(); i += 1) { Mysql.query("DELETE FROM timetable WHERE eventId='"+overwrittenEvents.get(i)+"' AND userId='"+user.getId()+"'");}
				}
			} else {
				if (otherUser) { Utils.error("This event clashes with an event of another user, it is recommended that you use a dynamic event when scheduling for multiple users");}
				successful = false;
			}
		} else {
			//Dynamic algorithm
		}
		
		return successful;
	}
	
	public boolean slotFree(int eventId, String start, String end, String date, long userId) {
		int timeSlots = (Utils.timeToMin(end) - Utils.timeToMin(start))/timeSlot;
		boolean free = true;
		for (int i = 0; i < timeSlots; i += 1) {
			String time = Utils.minToTime(Utils.timeToMin(start) + (timeSlot*i));
			Object eventInSlot = Mysql.queryTerm("eventId", "timetable", "WHERE userId='"+userId+"' AND start='"+time+"' AND date='"+date+"'");
			if (eventInSlot != null && (Integer) eventInSlot != eventId) {
				if (userId == user.getId()) { overwrittenEvents.add((Integer)eventInSlot);}
				free= false;
			}
		}
		return free;
	}
    
    public int calcRepeating(Event e) {
    	int repeatCounter = 1;
    	while (e.chain != -1) {
    		repeatCounter += 1;
    		e = new Event(e.chain);
    	}
    	return repeatCounter;
    }
}
