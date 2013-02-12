package com.bravo.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JOptionPane;

public class Utils {
	public static String minToTime(int i) {
		int hourNumber = Math.abs(i/60);
		int minuteNumber = i - (Math.abs(i/60)*60);
		
		String[] timeArray = (hourNumber+":"+minuteNumber).split(":");
		if (timeArray[0].length() == 1) { timeArray[0] = "0"+timeArray[0];}
		if (timeArray[1].length() == 1) { timeArray[1] = "0"+timeArray[1];}
		
		return timeArray[0]+":"+timeArray[1];
	}
	
	public static Date parseDate(String d) {
	    SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
	    try {
	    	return dateFormat.parse(d);
	    } catch (Exception e) {
	    	System.err.println("Unable to parse date"+d);
	    }
	    return null;
	}
	
	public static void error(String message) {
		JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
	}
	
	public static boolean confirm(String message) {
		if (JOptionPane.showConfirmDialog(null, message, "Confirm", JOptionPane.OK_CANCEL_OPTION) == 0) {
			return true;
		} else {
			return false;
		}
	}
}
