package com.bravo.view;

import com.bravo.utils.*;
import com.bravo.controller.*;
import com.bravo.model.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SpringLayout;
import org.jdesktop.swingx.JXDatePicker;

public class EventDialog extends javax.swing.JDialog {
	private static final long serialVersionUID = -8890905584352089849L;
	private MainWindow mainWindow;
	private EventController eventController;
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
	private long userId;
	private String eS;
	private String eE;
	private String eD;
	private int tS;
	private boolean isUpdating;
	private JTextField eventName;
	private JComboBox<String> eventType;
	private JComboBox<String> eventStart;
	private JComboBox<String> eventEnd;
	private JXDatePicker eventDate;
	private JTextField eventRepeat;
	private JCheckBox eventRepeatEdit;
	private JTextField eventLocation;
	private JComboBox<String> eventPriority;
	private JButton eventButton;
	ArrayList<Long> eventUsers;

    public EventDialog(MainWindow mainWindow, EventController eventController, java.awt.Frame parent, boolean modal, long uId, String eS, String eD, int tS) {
        super(parent, modal);
        this.mainWindow = mainWindow;
        this.eventController = eventController;
        userId = uId;
        this.eS = Utils.minToTime(Integer.parseInt(eS));
        this.eE = Utils.minToTime(Integer.parseInt(eS)+tS);
        this.eD = eD;
        this.tS = tS;
        eventUsers = new ArrayList<Long>();
		eventUsers.add(userId);

        
        initComponents();
        initMyComponents();
        KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        this.getRootPane().registerKeyboardAction(closeActionListener, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
    }
    
    ActionListener closeActionListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            dispose();
        }
    };
    

    private void initMyComponents() {
        this.setTitle(("Event"));
        ArrayList<HashMap<String,Object>> currentEvents = Mysql.query("SELECT * FROM timetable WHERE userId='"+userId+"' AND start='"+eS+"' AND date='"+eD+"'");
        HashMap<String,Object> currentEvent;
        String buttonText = "";
        if (currentEvents.size() > 0) { 
        	buttonText = "Update Event";
        	isUpdating = true;
        	currentEvent = currentEvents.get(0);
        	currentEvent.put("repeat", ""+eventController.calcRepeating(currentEvent));
        } else { 
        	buttonText = "Create Event";
        	isUpdating = false;
        	currentEvent = new HashMap<String,Object>();
        	currentEvent.put("eventId", "");
        	currentEvent.put("userId", "");
        	currentEvent.put("name", "");
        	currentEvent.put("type", "");
        	currentEvent.put("start", eS);
        	currentEvent.put("end", eE);
        	currentEvent.put("date", eD);
        	currentEvent.put("repeat", "1");
        	currentEvent.put("location", "");
        	currentEvent.put("priority", "");
        }
        String[] eventTypes = {"Static", "Dynamic"};  
        String[] priorities = {"Lowest", "Low", "Medium", "High", "Highest"};  
        String[] times = new String[(1440/tS)];
        for (int i = 0; i < 1440; i += tS) {
    		String time = Utils.minToTime(i);
    		times[(i/tS)] = time;
    	}
        
        eventName = new JTextField((String) currentEvent.get("name"));
        eventType = new JComboBox<String>(eventTypes);
        	eventType.setSelectedItem(currentEvent.get("type"));
    	eventStart = new JComboBox<String>(times);
    		eventStart.setSelectedItem(currentEvent.get("start"));
    	eventEnd = new JComboBox<String>(times);
    		eventEnd.setSelectedItem(currentEvent.get("end"));
    	try { eventDate = new JXDatePicker(dateFormat.parse((String) currentEvent.get("date"))); } catch (ParseException e) { eventDate = new JXDatePicker();}
    		eventDate.setFormats(dateFormat);
    	eventRepeat = new JTextField((String) currentEvent.get("repeat"));
    	eventRepeatEdit = new JCheckBox();
    	if (!isUpdating) { eventRepeatEdit.setEnabled(false);} else { eventRepeatEdit.setSelected(true);}
    	eventLocation = new JTextField((String) currentEvent.get("location"));
    	eventPriority = new JComboBox<String>(priorities);
    		int priority;
	    	try { priority = Integer.parseInt((String) currentEvent.get("priority"));} catch (Exception e) { priority = 1;}
			if (currentEvent.get("type").equals("Static")) { priority -= 5;}
			String priorityS = "";
			switch (priority) {
				case 1 : priorityS = "Lowest"; break;
				case 2 : priorityS = "Low"; break;
				case 3 : priorityS = "Medium"; break;
				case 4 : priorityS = "High"; break;
				case 5 : priorityS = "Highest"; break;
			}
    		eventPriority.setSelectedItem(priorityS);
		eventButton = new JButton(buttonText);
			eventButton.addActionListener(eventButtonAL);
        
        JPanel eventPanel = new JPanel(new SpringLayout());
        eventPanel.add(new JLabel("Event Name:"));
        eventPanel.add(eventName);
        eventPanel.add(new JLabel("Event Type:"));
        eventPanel.add(eventType);
        eventPanel.add(new JLabel("Event Time:"));
        	JPanel timePanel = new JPanel(new SpringLayout());
        	timePanel.add(eventStart);
        	timePanel.add(new JLabel("-"));
        	timePanel.add(eventEnd);
        	SpringUtilities.makeCompactGrid(timePanel, 1, 3, 10, 10, 10, 10);
        eventPanel.add(timePanel);
        eventPanel.add(new JLabel("Event Date:"));
        eventPanel.add(eventDate);
        eventPanel.add(new JLabel("Repeat X Weeks:"));
        eventPanel.add(eventRepeat);
        if (isUpdating) {
	        eventPanel.add(new JLabel("Edit Repeats: "));
	        eventPanel.add(eventRepeatEdit);
        } else {
        	eventPanel.add(new JLabel(""));
        	eventPanel.add(new JLabel(""));
        }
        eventPanel.add(new JLabel("Event Location:"));
        eventPanel.add(eventLocation);
        eventPanel.add(new JLabel("Event Priority:"));
        eventPanel.add(eventPriority);
        eventPanel.add(eventButton);
        eventPanel.add(new JLabel(""));
        SpringUtilities.makeCompactGrid(eventPanel, 9, 2, 10, 10, 10, 10);
        this.add(eventPanel);
    }    
    ActionListener eventButtonAL = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
    		try {
    			String name = eventName.getText();
    			String type = (String) eventType.getSelectedItem();
    			String start = (String) eventStart.getSelectedItem();
    			String end = (String) eventEnd.getSelectedItem();
    			Date date = eventDate.getDate();
    			int repeat;
    			try { repeat = Integer.parseInt(eventRepeat.getText());} catch (Exception e) { repeat = -4;}
    			String location = eventLocation.getText();
    			
    			int priority = 0;
    			String check = (String) eventPriority.getSelectedItem();
    			if (check.equals("Lowest")) { priority = 1;}
    			else if (check.equals("Low")) { priority = 2;}
    			else if (check.equals("Medium")) { priority = 3;}
    			else if (check.equals("High")) { priority = 4;}
    			else if (check.equals("Highest")) { priority = 5;}
    			if (type.equals("Static")) { priority += 5;}
    			
    			if (name.equals("") || date.equals("") || repeat == -4) {
    				Utils.error("You didn't fill out a required field");
    			} else if (Integer.parseInt(start.substring(0,2)) > Integer.parseInt(end.substring(0,2)) || (Integer.parseInt(start.substring(0,2)) == Integer.parseInt(end.substring(0,2)) && Integer.parseInt(start.substring(3,5)) >= Integer.parseInt(end.substring(3,5)))) {
					Utils.error("End time must be at least "+tS+" minutes after start time");
				} else if (repeat <= 0){
					Utils.error("Repeat value must be greater than or equal to 1");
				} else {
					eventController.addEvent(new Event(0,0,name,type,start,end,date,repeat,location,priority),eventUsers, isUpdating, eventRepeatEdit.isSelected());
					mainWindow.update();
					dispose();
				}
    		} catch (Exception e) {
    			e.printStackTrace();
    		}
        }
    };

    public void update() {
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setName("Form"); // NOI18N
        getContentPane().setLayout(new javax.swing.BoxLayout(getContentPane(), javax.swing.BoxLayout.PAGE_AXIS));

        pack();
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}