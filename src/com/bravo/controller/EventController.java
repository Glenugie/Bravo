package com.bravo.controller;

import java.awt.Dimension;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.bravo.App;
import com.bravo.model.*;
import com.bravo.utils.Mysql;
import com.bravo.utils.Utils;
import com.bravo.view.EventDialog;
import com.bravo.view.MainWindow;

public class EventController {
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
    private SimpleDateFormat timestamp = new SimpleDateFormat("HH:mm");
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
				//for (int j = 0; j < chainLength; j += 1) {
					String eventParsedDate = dateFormat.format(timeCounter);
					timeCounter += (86400000*7);
					if (!slotFree(e.eventId,e.start,e.end,eventParsedDate,eventUsers.get(i))) {
						clashFree = false;
						if (eventUsers.get(i) != user.getId()) { otherUser = true;}
						//if (chainLength > 1) { chainClash = true;}
					}
				//}
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
					for (int i = 0; i < overwrittenEvents.size(); i += 1) { 
						HashMap<String,Object> overwrittenEvent = Mysql.query("SELECT * FROM timetable WHERE userId='"+user.getId()+"' AND eventId='"+overwrittenEvents.get(i)+"'").get(0);
						EventDialog eventDialog = new EventDialog(mainWindow, this, App.getApplication().getMainFrame(), true, user.getId(), ""+Utils.timeToMin((String)overwrittenEvent.get("start")), (String)overwrittenEvent.get("date"), (int)overwrittenEvent.get("eventId"));
						eventDialog.pack();
						eventDialog.setLocationRelativeTo(null);
						eventDialog.setSize(new Dimension(400, 400));
						eventDialog.setVisible(true);
					}
				} else if (!clashFree) {
					for (int i = 0; i < overwrittenEvents.size(); i += 1) { Mysql.query("DELETE FROM timetable WHERE eventId='"+overwrittenEvents.get(i)+"' AND userId='"+user.getId()+"'");}
				}
			} else {
				if (otherUser) { Utils.error("This event clashes with an event of another user, it is recommended that you use a dynamic event when scheduling for multiple users");}
				successful = false;
			}
		} else {
			String dynamicSchedule = dynamicSchedule(eventUsers,e);
			System.out.println("Slot selected: "+dynamicSchedule);
			if (dynamicSchedule.equals("")) {
				successful = false;
			} else {
				//Schedule event in returned slot
				//Mysql.query("INSERT INTO timetable (eventId, userId, name, type, start, end, date, location, priority, nextChain) " + "VALUES ('"+newId+"', '"+eventUsers.get(i)+"', '"+e.name+"', '"+e.type+"', '"+e.start+"', '"+e.end+"', '"+eventParsedDate+"', '"+e.location+"', '"+e.priority+"', '-1')");
			}
		}
		
		return successful;
	}
	
	public boolean slotFree(int eventId, String start, String end, String date, long userId) {
		int timeSlots = (Utils.timeToMin(end) - Utils.timeToMin(start))/timeSlot;
		boolean free = true;
		for (int i = 0; i < timeSlots; i += 1) {
			String time = Utils.minToTime(Utils.timeToMin(start) + (timeSlot*i));
			String endTime = Utils.minToTime(Utils.timeToMin(start) + (timeSlot*(i+1)));
			Object eventInSlot = Mysql.queryTerm("eventId", "timetable", "WHERE userId='"+userId+"' AND date='"+date+"' AND eventId!='"+eventId+"' AND (start='"+time+"' OR end='"+endTime+"')");
			if (eventInSlot != null) {
				if (userId == user.getId()) { overwrittenEvents.add((Integer)eventInSlot);}
				free= false;
			}
		}
		Object eventInSlot = Mysql.queryTerm("eventId", "timetable", "WHERE userId='"+userId+"' AND start<='"+start+"' AND end>'"+end+"' AND date='"+date+"' AND eventId!='"+eventId+"'");
		if (eventInSlot != null) {
			if (userId == user.getId()) { overwrittenEvents.add((Integer)eventInSlot);}
			free= false;
		}
		return free;
	}
    
	public String dynamicSchedule(ArrayList<Long> eventUsers, Event e) {
		int currentMinute = Utils.timeToMin(timestamp.format(new Date().getTime()));
		String currentDate = dateFormat.format(new Date().getTime());
		
		String returnValue = "";
		int startDay = 0;
		boolean successful = false;
		
		while (!successful) {
			ArrayList<String> allSlots = new ArrayList<String>();
			ArrayList<String> allDates = new ArrayList<String>();
			String date = dateFormat.format(e.date.getTime() + (86400000 * startDay));
			
			//Constructs a list of all slots
			for (int day = startDay; day < (startDay+7); day += 1) {
				allDates.add(date);
				for (int i = 0; i < 1440; i += timeSlot) {
					String slotToAdd = date+" "+Utils.minToTime(i);
					if ((date.equals(currentDate) && i > currentMinute) || !date.equals(currentDate)) {
						allSlots.add(slotToAdd);
					}
				}
				date = dateFormat.format(e.date.getTime() + (86400000 * (day + 1)));
			}

			//Removes slots from the list which are filled by another event
			for (int i = 0; i < eventUsers.size(); i += 1) {
				ArrayList<HashMap<String,Object>> userEvents = Mysql.query("SELECT * FROM timetable WHERE userId='"+eventUsers.get(i)+"' AND date<'"+date+"'");
				for (HashMap<String,Object> event : userEvents) {
					int timeSlots = (Utils.timeToMin((String)event.get("end")) - Utils.timeToMin((String)event.get("start")))/timeSlot;
					for (int j = 0; j < timeSlots; j += 1) {
						allSlots.remove((String)event.get("date")+" "+Utils.minToTime(Utils.timeToMin((String)event.get("start")) + (timeSlot*i)));
					}
				}
			}

			//Remove sets of slots which do not fit event duration
			for (int i = (allSlots.size() - 1); i >= 0; i -= 1) {
				/*=====To Write=====*/
			}

			boolean newDate = true;
			while (newDate && allDates.size() > 0) {
				//Choose a date for the event
				String chosenDate = "";
				int lastCounter = (((24*60)/timeSlot)*eventUsers.size())+1;
				for (String d : allDates) {
					int counter = 0;
					for (int i = 0; i < eventUsers.size(); i += 1) {
						ArrayList<HashMap<String,Object>> userEvents = Mysql.query("SELECT * FROM timetable WHERE userId='"+eventUsers.get(i)+"' AND date='"+d+"'");
						counter += userEvents.size();
					}
					if (counter < lastCounter) { chosenDate = d;}
					lastCounter = counter;
				}
				newDate = false;
				
				//No available slots, get action choice from user
				if (allSlots.size() == 0) {
					Object[] options = {"Check next 7 days", "Find Best Fit", "Cancel Scheduling"};
					int noEvents = JOptionPane.showOptionDialog(null, "There are no slots in the next week that can accommodate all attendees", "No Available Slots", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,null,options,options[0]);
					if (noEvents == JOptionPane.YES_OPTION) {
						startDay += 7;
					} else if (noEvents == JOptionPane.NO_OPTION) {
						//Redefine all slots to find best fit
						/*=====To Write=====*/
					} else {
						successful = true;
					}
				}
				
				//If there are available slots
				if (allSlots.size() > 0) {
					//Add slots to new list that have a free slots before and after
					ArrayList<String> availableSlots = new ArrayList<String>();
					int counter = 0; String lastSlot = chosenDate+" 00:00";
					for (int i = 0; i < allSlots.size(); i += 1) {
						if (allSlots.get(i).startsWith(chosenDate)) {
							if (Utils.timeToMin(allSlots.get(i).split(" ")[3]) == (Utils.timeToMin(lastSlot.split(" ")[3])+timeSlot)) {
								counter += 1;
							} else {
								counter = 0;
							}
							if (counter >= 2) { availableSlots.add(allSlots.get(i));}
							lastSlot = allSlots.get(i);
						}
					}
					
					//If no slots were caught in the above filter, add all free slots
					if (availableSlots.size() == 0) {
						availableSlots.clear();
						for (int i = 0; i < allSlots.size(); i += 1) {
							if (allSlots.get(i).startsWith(chosenDate)) {
								availableSlots.add(allSlots.get(i));
							}
						}
					}
	
					boolean agrees = false;
					String chosenSlot = "";
					while (!agrees) {
						//Select slot which is closest to 2PM (Later this can be middle of working day)
						String startSlot = chosenDate+" 14:00",slotUp = startSlot, slotDown = startSlot;
						boolean slotFound = false;
						if (availableSlots.contains(startSlot)) { 
							chosenSlot = startSlot;
							slotFound = true;
						}
						while (!slotFound) {
							slotUp = chosenDate+" "+Utils.minToTime(Utils.timeToMin(slotUp.split(" ")[3])+timeSlot);
							slotDown = chosenDate+" "+Utils.minToTime(Utils.timeToMin(slotDown.split(" ")[3])-timeSlot);
							int diffUp = Math.abs(Utils.timeToMin(slotUp.split(" ")[3])-Utils.timeToMin(startSlot.split(" ")[3]));
							int diffDown = Math.abs(Utils.timeToMin(slotDown.split(" ")[3])-Utils.timeToMin(startSlot.split(" ")[3]));
							if (availableSlots.contains(slotUp) && diffUp <= diffDown) {
								slotFound = true;
								chosenSlot = slotUp;
								if (availableSlots.contains(slotDown)) { 
									slotDown = chosenDate+" "+Utils.minToTime(Utils.timeToMin(slotDown.split(" ")[3])+timeSlot);
								}
							} else if (availableSlots.contains(slotDown) && diffUp >= diffDown) {
								slotFound = true;
								chosenSlot = slotDown;
							}
							
							if (!slotFound && (slotUp.equals(chosenDate+" 00:00") || slotDown.equals(chosenDate+" "+Utils.minToTime(Utils.timeToMin("24:00")-timeSlot)))) {
								newDate = true;
							}
						}
						
						if (!newDate) {
							//Get user to confirm slot
							Object[] options = {"Schedule Event", "Show Next Slot", "Cancel Scheduling"};
							int confirm = JOptionPane.showOptionDialog(null, "Slot selected: "+chosenSlot, "Slot Confirmation", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,null,options,options[0]);
							if (confirm == JOptionPane.YES_OPTION) {
								agrees = true;
							} else if (confirm == JOptionPane.NO_OPTION) {
								availableSlots.remove(chosenSlot);
							} else {
								successful = true;
								agrees = true;
							}
							
							//If user has rejected all slots on this date, get a new one
							if (availableSlots.size() == 0) { 
								newDate = true;
							}
						}
					}
					
					if (newDate) { for (int i = (allSlots.size() - 1); i >= 0; i -= 1) { if (allSlots.get(i).startsWith(chosenDate)) { allSlots.remove(allSlots.get(i));}}}
					
					//Haven't force quit, therefore continue
					if (!successful) {
						successful = true;
						returnValue = chosenSlot;
					}
				}
			}
			
			//Ran out of dates in current period to suggest to user
			if (allDates.size() == 0) { 
				Utils.error("No more slots left to select in current date period, try setting a later start date");
				successful = true;
			}
		}
		
		return returnValue;
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
