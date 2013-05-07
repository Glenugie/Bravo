package com.bravo.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;

import com.bravo.App;
import com.bravo.controller.EventController;
import com.bravo.model.User;
import com.bravo.utils.Mysql;
import com.bravo.utils.Utils;

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
	private Image bglogo; 
	private int priority; // To be used for the colour switch for eventing. *PS

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
			this.setBackground(Color.WHITE);
			/*
			 * try {
			BufferedImage img = ImageIO.read(new File("TeamLogoBG.jpg"));
			ImageIcon icon = new ImageIcon(img);
			JLabel label = new JLabel(icon);
			} catch (IOException e) {
				e.printStackTrace();
				}
				*/
			ImageIcon bgimage = new ImageIcon("TeamLogoBG.jpg");
			JLabel BGlbl = new JLabel();
			BGlbl.setIcon(bgimage);
			BGlbl.setVisible(true);
			this.add(BGlbl);
			this.add(new JLabel("<html><center><font size=18><b>Welcome to Dynacalendar</b>"
					+ "<br>Please sign in to continue.</font></center></html>"));	
			
		} else {
			this.setLayout(new BorderLayout());
			timetable = new JPanel();
			timetable.setLayout(new GridBagLayout());
			c = new GridBagConstraints();
			c.fill = GridBagConstraints.HORIZONTAL;

			int timetableView = mainWindow.getView();
			if (timetableView == 1) {
				addCell("Date", 0, 0);
				for (int i = 0; i < 1440; i += eventController.timeSlot) {
					String time = Utils.minToTime(i);
					addCell(time, (i / eventController.timeSlot) + 1, 0);
				}
				SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
				String date = dateFormat.format(new Date().getTime());
				for (int day = 0; day < 7; day += 1) {
					ArrayList<HashMap<String, Object>> timetableDay = Mysql.query("SELECT * FROM event WHERE userId='" + user.getId() + "' AND date='" + date + "' ORDER BY start ASC");
					HashMap<Integer, Integer> events = new HashMap<Integer, Integer>();
					addCell(date, 0, (day + 1));
					for (int row = 0; row < timetableDay.size(); row += 1) {
						String startTime = (String) timetableDay.get(row).get("start");
						String endTime = (String) timetableDay.get(row).get("end");
						Integer startSlot = (((Integer.parseInt(startTime.substring(0, 2)) * 60) + Integer.parseInt(startTime.substring(3, 5)))/eventController.timeSlot) + 1; 
						Integer endSlot = (((Integer.parseInt(endTime.substring(0, 2)) * 60) + Integer.parseInt(endTime.substring(3, 5)))/eventController.timeSlot) + 1; 
						events.put((startSlot * eventController.timeSlot) - (1 * eventController.timeSlot), endSlot - startSlot);
					}
					int row = 0;
					
					//add Image Icon to the button. ( team Logo small size)
					for (int i = 0; i < 1440; i += eventController.timeSlot) {
						if (events.get(i) != null) {
							priority = (int) timetableDay.get(row).get("priority");
							int eId = (int) timetableDay.get(row).get("eventId");
							addCell((String) timetableDay.get(row).get("name"), ((i / eventController.timeSlot) + 1), (day + 1), events.get(i), date, eId, priority);
							row += 1;
						} else {
							addCell("  ", ((i / eventController.timeSlot) + 1), (day + 1), 1, date, -1, 0);
						}
					}
					date = dateFormat.format((new Date().getTime()) + (86400000 * (day + 1)));
				}
			} else {
				SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
				String date = dateFormat.format(new Date().getTime());
				for (int day = 0; day < 31; day += 1) {
					ArrayList<HashMap<String, Object>> timetableDay = Mysql.query("SELECT * FROM event WHERE userId='" + user.getId() + "' AND date='" + date + "' ORDER BY start ASC");
					addMonthCell(date, (day % 7), (int) Math.floor(day/7), date, timetableDay.size());
					date = dateFormat.format((new Date().getTime()) + (86400000 * (day + 1)));
				}
			}
			JScrollPane scrollPane = new JScrollPane(timetable,
					JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
					JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			scrollPane.getVerticalScrollBar().setUnitIncrement(16);
			scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
			this.add(scrollPane, BorderLayout.CENTER);
		}
	}
	ActionListener eventButtonAL = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent actionEvent) {
			JButton source = (JButton) actionEvent.getSource();
			String[] name = source.getName().split(",");
			JFrame mainFrame = App.getApplication().getMainFrame();
			EventDialog eventDialog = new EventDialog(mainWindow, eventController, mainFrame, true, user.getId(), name[0], name[1], Integer.parseInt(name[2]));
			eventDialog.pack();
			eventDialog.setLocationRelativeTo(null);
			eventDialog.setSize(new Dimension(600, 400));
			eventDialog.setVisible(true);
		}
	};
	ActionListener eventMonthButtonAL = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent actionEvent) {
			JButton source = (JButton) actionEvent.getSource();
			String[] name = source.getName().split(",");
			JFrame mainFrame = App.getApplication().getMainFrame();
			EventDialog eventDialog = new EventDialog(mainWindow, eventController, mainFrame, true, user.getId(), "0", name[1], Integer.parseInt(name[2]));
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
		c.ipadx = 50;
		c.ipady = 50;
		JLabel l = new JLabel(s, SwingConstants.CENTER);
		l.setFont(new Font("Arial Black", Font.BOLD,18));
		l.setBorder(LineBorder.createBlackLineBorder());
		l.setBackground(new Color(173,180,196));
		l.setOpaque(true);
		timetable.add(l, c);
		
	}

	private void addCell(String s, int x, int y, int width, String date, int eId, int priority) {
		c.gridx = x;
		c.gridy = y;
		c.gridwidth = width;
		c.ipadx = 50;
		c.ipady = 50;
		try{
			String osName = System.getProperty("os.name").toLowerCase();
			if (!osName.contains("mac"))
			{
				
				UIManager.setLookAndFeel( UIManager.getCrossPlatformLookAndFeelClassName() ); //CHECK FOR DIFFERENT LOOK AND FEEL
			}
				
		
		JButton b = new JButton(s);
		b.setBorder(LineBorder.createBlackLineBorder());
		b.setName(((x * eventController.timeSlot) - (1 * eventController.timeSlot)) + "," + date + "," + eId);
		b.addActionListener(eventButtonAL);
		b.setOpaque(true);
		//b.setBorderPainted(false);
		ImageIcon ButtonIcon = new ImageIcon("TeamLogoBGForLogin.jpg");
		JLabel BtnLbl = new JLabel();
		BtnLbl.setIcon(ButtonIcon);
		BtnLbl.setVisible(true);
		
		while (priority > 5) { priority -= 5;}
		//System.out.println(priority);
		
		switch(priority) {
			case 1: b.setBackground(Color.WHITE); break;
			case 2: b.setBackground(new Color(191, 169, 142)); break;
			case 3: b.setBackground(new Color(50, 140, 115)); break;
			case 4: b.setBackground(new Color(191, 145, 59)); break; 
			case 5: b.setBackground(new Color(166, 59, 50));break;
			default: b.setBackground(Color.WHITE);
			//b.add(BtnLbl); bad idea to add image unless seriously modified
			break; 
		}
		b.setOpaque(true);
		//b.setVisible(true);
		b.setFont(new Font("Serif",Font.BOLD,18));
		timetable.add(b, c);
		}catch(Exception e){
			e.printStackTrace();	
		}
	}
	
	public void addMonthCell(String s, int x, int y, String date, int eNum) {
		c.gridx = x;
		c.gridy = y;
		c.ipadx = 50;
		c.ipady = 50;
		
		JButton b = new JButton(s.split(" ")[0]+" "+s.split(" ")[1]+" ("+eNum+")");
		b.setBorder(LineBorder.createBlackLineBorder());
		b.setName(((x * eventController.timeSlot) - (1 * eventController.timeSlot)) + "," + date+",0");
		b.addActionListener(eventMonthButtonAL);
		b.setOpaque(true);
		
		ImageIcon ButtonIcon = new ImageIcon("TeamLogoBGForLogin.jpg");
		JLabel BtnLbl = new JLabel();
		BtnLbl.setIcon(ButtonIcon);
		BtnLbl.setVisible(true);
		
		b.setOpaque(true);
		b.setFont(new Font("Serif",Font.BOLD,18));
		
		timetable.add(b, c);
		
	}
	
	public void getImage (){  
		ImageIcon icon = new ImageIcon ("BravoLogo.jpg");
		bglogo =icon.getImage();
		
	}
	public void paint (Graphics g){  //paint bglogo
		g.drawImage(bglogo,0,0,getSize().width,getSize().height,this);
		super.paint(g);
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
