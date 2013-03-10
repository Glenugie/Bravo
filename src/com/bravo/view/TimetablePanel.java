package com.bravo.view;

import com.bravo.App;
import com.bravo.controller.*;
import com.bravo.model.User;
import com.bravo.utils.*;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;

public class TimetablePanel extends javax.swing.JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6681191891565986348L;
	private JPanel timetable;
	private GridBagConstraints c;
	private MainWindow mainWindow;
	private EventController eventController;
	private User user;
	private Image img; //will store background image 
	
	

	public TimetablePanel(MainWindow mainWindow, EventController eventController) {
        super();
        
        this.mainWindow = mainWindow;
        this.eventController = eventController;
        
        this.user = mainWindow.getUser();
        
        initComponents();
        initMyComponents();
        
    }

	private void initMyComponents() {
		if (user.getId() == -4) {
			this.setLayout(new FlowLayout());
			
			/*GuiControl img = new GuiControl(new ImageIcon ("TeamLogoBG.jpg").getImage());
			JFrame frame = new JFrame();
			frame.getContentPane().add(img);
			frame.pack();
			this.setLayout(new BorderLayout());
			this.add(frame, BorderLayout.CENTER);
			frame.setVisible(true); //not working properly yet, will fix it soon
			this.add(frame);*/
			
			
			/*Image image = GenerateImage.toImage(true);
			ImageIcon icon = new ImageIcon("TeamBravoBG.jpg");
			JLabel imgbg = new JLabel();
			imgbg.setIcon(icon);*/
			
			ImageIcon bgicon = new ImageIcon("TeamLogoBG.jpg");
			JLabel bgLabel = new JLabel();
			bgLabel.setIcon(bgicon);
			this.add(bgLabel,BorderLayout.CENTER);
			this.setBackground(Color.WHITE);
			
			this.add(new JLabel("You need to log in to view a timetable"));
			//this.add(frame, BorderLayout.CENTER);
			
		} else {
			
			
			this.setLayout(new BorderLayout());
			timetable = new JPanel();
			
			timetable.setLayout(new GridBagLayout());
			c = new GridBagConstraints();
			c.fill = GridBagConstraints.HORIZONTAL;

			addCell("Date", 0, 0);
			for (int i = 0; i < 1440; i += eventController.timeSlot) {
				String time = Utils.minToTime(i);
				addCell(time, (i / eventController.timeSlot) + 1, 0);
			}
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
			String date = dateFormat.format(new Date().getTime());
			for (int day = 0; day < 7; day += 1) {
				ArrayList<HashMap<String, Object>> timetableDay = Mysql
						.query("SELECT * FROM timetable WHERE userId='"
								+ user.getId() + "' AND date='" + date
								+ "' ORDER BY start ASC");

				HashMap<Integer, Integer> events = new HashMap<Integer, Integer>();
				addCell(date, 0, (day + 1));
				for (int row = 0; row < timetableDay.size(); row += 1) {
					String startTime = (String) timetableDay.get(row).get(
							"start");
					String endTime = (String) timetableDay.get(row).get("end");
					Integer startSlot = (((Integer.parseInt(startTime
							.substring(0, 2)) * 60) + Integer
							.parseInt(startTime.substring(3, 5))) / 15) + 1;
					Integer endSlot = (((Integer.parseInt(endTime.substring(0,
							2)) * 60) + Integer.parseInt(endTime
							.substring(3, 5))) / 15) + 1;
					events.put((startSlot * eventController.timeSlot) - (1 * eventController.timeSlot), endSlot
							- startSlot);
				}

				int row = 0;
				for (int i = 0; i < 1440; i += eventController.timeSlot) {
					if (events.get(i) != null) {
						addCell((String) timetableDay.get(row).get("name"),
								((i / eventController.timeSlot) + 1), (day + 1), events.get(i),
								date);
						row += 1;
					} else {
						addCell("+", ((i / eventController.timeSlot) + 1), (day + 1), 1, date);
					}
				}

				date = dateFormat.format((new Date().getTime())
						+ (86400000 * (day + 1)));
			}
			JScrollPane scrollPane = new JScrollPane(timetable,
					JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
					JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			this.add(scrollPane, BorderLayout.CENTER);
		}
	}

	ActionListener eventButtonAL = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent actionEvent) {
			JButton source = (JButton) actionEvent.getSource();
			String[] name = source.getName().split(",");
			JFrame mainFrame = App.getApplication().getMainFrame();
			EventDialog eventDialog = new EventDialog(mainWindow, eventController, mainFrame, true, user.getId(), name[0], name[1],-1);
			eventDialog.pack();
			eventDialog.setLocationRelativeTo(null);
			eventDialog.setSize(new Dimension(600, 400));
			eventDialog.setVisible(true);
		}
	};

	// Center Cells
	private void addCell(String s, int x, int y) {
		c.gridx = x;
		c.gridy = y;
		c.ipadx = 100;
		c.ipady = 100;
		JLabel l = new JLabel(s, SwingConstants.CENTER);
		l.setBorder(LineBorder.createBlackLineBorder());
		timetable.add(l, c);
	}

	private void addCell(String s, int x, int y, int width, String date) {
		c.gridx = x;
		c.gridy = y;
		c.gridwidth = width;
		c.ipadx = 100;
		c.ipady = 100;
		JButton b = new JButton(s);
		b.setBorder(LineBorder.createBlackLineBorder());
		b.setName(((x * eventController.timeSlot) - (1 * eventController.timeSlot)) + "," + date);
		b.addActionListener(eventButtonAL);
		timetable.add(b, c);
	}
	
	

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */

	// <editor-fold defaultstate="collapsed"
	// desc="Generated Code">//GEN-BEGIN:initComponents
	private void initComponents() {

		setName("Form"); // NOI18N
		setLayout(new java.awt.BorderLayout());
	}// </editor-fold>//GEN-END:initComponents
		// Variables declaration - do not modify//GEN-BEGIN:variables
		// End of variables declaration//GEN-END:variables
}
