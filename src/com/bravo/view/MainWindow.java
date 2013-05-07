package com.bravo.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SpringLayout;

import org.jdesktop.application.FrameView;
import org.jdesktop.application.SingleFrameApplication;

import com.bravo.App;
import com.bravo.controller.EventController;
import com.bravo.model.User;
import com.bravo.utils.Mysql;
import com.bravo.utils.SpringUtilities;
import com.bravo.utils.Utils;

public final class MainWindow extends FrameView {
    private JTabbedPane tabbedPane;
    private JPanel timetablePanel;
    private JPanel groupPanel;
    private JPanel mapPanel;
    private JTable timetable;
    private EventController eventController;
    private User user;
    private long userID;
    private MainWindow mw;
    private int timetableView;
	
    public MainWindow(SingleFrameApplication app) {
    	
        super(app);
        //can be a system println to start testing a connection in case of MySQL execution error
        this.getFrame().setTitle("Dynamic Timetable");
        
        
        if (!Mysql.testConnection()) {
			Utils.error("Unable to establish connection to MySQL Server");
			System.exit(0);
        }
        
        eventController = new EventController(this);
        mw = this;
        user = new User(-4);
        timetableView = 1;

        initComponents();
        initMyComponents();
    }
    
    private void initMyComponents() {
    	tabbedPane = new JTabbedPane();
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        clearOldEvents();
        update();
        mainPanel.add(tabbedPane);
       
    }    
    
    JPanel sm = new JPanel(); 
    JPanel mm = new JPanel(); 	
    public JPanel createMapPanel() {
  	   
    	mapPanel = new JPanel();
    	mapPanel.setLayout(new BorderLayout());
    						

		try {
			URL link = new URL("http://maps.google.com/maps/api/staticmap?center=57.165736,-2.102185&zoom=4&markers=size:mid|color:black|high+street+aberdeen+uk&size=480x610&sensor=false&key=AIzaSyCzLXZkd3uPevpTSvmV9kQ5Trbts7UldJg");
	    	ImageIcon img1 = new ImageIcon(link);   	
    		JLabel mml1 = new JLabel();	//main map panel
	    	mml1.setIcon(img1);
	    	mml1.setVisible(true);
	    	sm.removeAll();
	    	sm.add(mml1);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		//------------------------------------------------------------							
    	
		try {
			URL link = new URL("http://maps.google.com/maps/api/staticmap?&center=aberdeen+ab253UH&zoom=10&size=1200x610&sensor=false&key=AIzaSyCzLXZkd3uPevpTSvmV9kQ5Trbts7UldJg");
	    	ImageIcon img2 = new ImageIcon(link);   	
    		JLabel mml2 = new JLabel();	//main map panel
	    	mml2.setIcon(img2);
	    	mml2.setVisible(true);
	    	mm.removeAll();
	    	mm.add(mml2);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mm.setPreferredSize(new Dimension(1200,610));
    	JSplitPane maps = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, mm , sm);	//contains 1 big map and 2 small ones
    	maps.setResizeWeight(0.8);   	
    	maps.setContinuousLayout(true);
     	JPanel eP = new JPanel();	//frame for event list
     	eP.setPreferredSize(new Dimension(400, 610));
     	eP.setLayout(new BorderLayout());
     	
     	SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
		String date = dateFormat.format(new Date().getTime());
		ArrayList<HashMap<String, Object>> timetableDay = Mysql
					.query("SELECT * FROM event WHERE userId='"
							+ user.getId() + "' AND date='" + date
							+ "' ORDER BY start ASC");
		String labels[] = new String[timetableDay.size()];
		JButton buttons[] = new JButton[timetableDay.size()];		
		JPanel butPanel = new JPanel(new SpringLayout());
		for (int row = 0; row < timetableDay.size(); row += 1) {
			labels[row]=((String)timetableDay.get(row).get("name"));			
			JButton event = new JButton((String)timetableDay.get(row).get("name"));
			event.setName(""+(int)timetableDay.get(row).get("id"));
			event.addActionListener(eventButtonAL);
			buttons[row]=(event);
			butPanel.add(event);
		}		
		SpringUtilities.makeCompactGrid(butPanel, timetableDay.size(), 1, 10,10,10,10  );
		JScrollPane scrollPane = new JScrollPane(butPanel);
		eP.setMinimumSize(new Dimension(300,610));
        eP.add(scrollPane);

        

     	JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, eP, maps);	//main split will contain event list and maps
     	split.setResizeWeight(0.9);
     	split.setContinuousLayout(true);  	     
    	mapPanel.add(split);  			
    	return mapPanel;
    	
    	
    }
    
    ActionListener eventButtonAL = new ActionListener() {
    	@Override
    	public void actionPerformed(ActionEvent actionEvent) {
    		JButton source = (JButton) actionEvent.getSource();
    		HashMap<String, Object> event = Mysql.query("SELECT * FROM event WHERE id='"+source.getName()+"'").get(0);
    		try {
    				HashMap<String, Object> address = Mysql.query("SELECT * FROM address WHERE addressID='"+event.get("addressID")+"'").get(0);    				  				
    				System.out.println(event.get("name")+ " " + address.get("city")+ " "+ address.get("street"));
    				sm.removeAll();
    				mm.removeAll();
    				System.out.println(constructURL((String)address.get("postcode"), (String)address.get("city"), (String)address.get("street"),1));
    				try {
    					URL link = new URL(constructURL((String)address.get("postcode"), (String)address.get("city"), (String)address.get("street"),1));
    			    	ImageIcon img1 = new ImageIcon(link);   	
    		    		JLabel mml1 = new JLabel();	//main map panel
    			    	mml1.setIcon(img1);
    			    	mml1.setVisible(true);
    			    	sm.add(mml1);
	
    			    	link = new URL(constructURL((String)address.get("postcode"), (String)address.get("city"), (String)address.get("street"),0));
    			    	ImageIcon img2 = new ImageIcon(link);   	
    		    		JLabel mml2 = new JLabel();	
    			    	mml2.setIcon(img2);
    			    	mml2.setVisible(true);
    			    	mm.add(mml2);
    				} catch (MalformedURLException e) {
    					// TODO Auto-generated catch block
    					e.printStackTrace();
    				}
    				
    		} catch (Exception e){
    			//No address
    		}		
    	}
    };

    /*
     * mapType 1 - far, 0 - close
     */
    private String constructURL(String city, String street, String postCode, int mapType){
    	String link;
    	link = "http://maps.google.com/maps/api/staticmap?&center="+city+"+"+street+"+"+postCode;
    	if(mapType == 1 ){
    		link+="&zoom=13&size=480x610&";
    	}
    	else{
    		link+="&zoom=16&size=1200x610&";
    	}
    	link+="markers=size:mid|color:red&sensor=false&key=AIzaSyCzLXZkd3uPevpTSvmV9kQ5Trbts7UldJg";
    	return link;
    }
    
    
    public JPanel createGroupPanel() {
        groupPanel = new JPanel(new SpringLayout());
        groupPanel.add(new JLabel("<html><font size=20><b><u>Groups</u></b></font></html>"));
        
        JButton groupCreate = new JButton("Create Group");
        groupCreate.addActionListener(groupCreateAL);
        groupPanel.add(groupCreate);
        
        JPanel groupList = new JPanel(new SpringLayout());
        groupList.add(new JLabel("<html><font size=12><b><u>You are a member of the following groups:</u></b></font></html>"));
        groupList.add(new JLabel(""));
        int i = 0;
        for (HashMap<String, Object> group : Mysql.query("SELECT * FROM groups")) {
        	groupList.add(new JLabel((String)group.get("groupname")));
        	JButton addMembers = new JButton("Add Members");
        	addMembers.setName((String)group.get("groupname"));
        	addMembers.addActionListener(addMembersAL);
        	if (((Integer)group.get("groupleader")).longValue() == user.getId()) {
        		groupList.add(addMembers);
        	} else {
        		groupList.add(new JLabel(""));
        	}
        	i += 1;
        }
        SpringUtilities.makeCompactGrid(groupList, i, 2, 10, 10, 10, 10);
        groupPanel.add(groupList);

        SpringUtilities.makeCompactGrid(groupPanel, 3, 1, 10, 10, 10, 10);
        return groupPanel;
    }
    ActionListener groupCreateAL = new ActionListener() {
    	@Override
    	public void actionPerformed(ActionEvent actionEvent) {
    		JFrame mainFrame = App.getApplication().getMainFrame();
            GroupCreateDialog groupDialog = new GroupCreateDialog(mw, mainFrame, true);
            groupDialog.pack();
            groupDialog.setLocationRelativeTo(null);
            groupDialog.setBackground(Color.black); //changes bg color of login panel
            groupDialog.setSize(new Dimension(320,275));
            groupDialog.setVisible(true);
    	}
    };
    ActionListener addMembersAL = new ActionListener() {
    	@Override
    	public void actionPerformed(ActionEvent actionEvent) {
    		JFrame mainFrame = App.getApplication().getMainFrame();
            GroupDialog groupDialog = new GroupDialog(mw, mainFrame, true, ((JButton)actionEvent.getSource()).getName());
            groupDialog.pack();
            groupDialog.setLocationRelativeTo(null);
            groupDialog.setBackground(Color.black); //changes bg color of login panel
            groupDialog.setSize(new Dimension(320,275));
            groupDialog.setVisible(true);
    	}
    };
    
    public void createMenuBar() {
    	 
    	//UIManager.put("logoutMenuItem.selectionBackground", new Color(199,66,35)); change color of Jmenu when you drag mouse over it, Doesn't work
    	JMenu accountMenu = new JMenu("Account");
    	JMenu TimetableView = new JMenu("Timetable View");
    	//TimetableView.setLocation(p);
    	
    		if (user.getId() == -4) {
		    	JMenuItem loginMenuItem = new JMenuItem("Login");
		    		loginMenuItem.addActionListener(loginMenuItemAL);
		    		accountMenu.add(loginMenuItem);
		    	JMenuItem registerMenuItem = new JMenuItem("Register");
		    		registerMenuItem.addActionListener(registerMenuItemAL);
		    		accountMenu.add(registerMenuItem);
		    	JMenuItem dummyMenuItem = new JMenuItem("Create 25 Dummy Accounts");
		    		dummyMenuItem.addActionListener(dummyMenuItemAL);
		    		accountMenu.add(dummyMenuItem);
    		} else {
    			JMenuItem ProfileMenuItem = new JMenuItem("Profile");
    			ProfileMenuItem.addActionListener(ProfileMenuItemAL);//need to add action listener
	    		accountMenu.add(ProfileMenuItem);
	    		
    			JMenuItem logoutMenuItem = new JMenuItem("Logout");
    			logoutMenuItem.setForeground(new Color(199,66,35));	
    			
    			JMenuItem monthView = new JMenuItem ("Month View");
    			monthView.addActionListener(mViewAL);
    			if (timetableView == 2) { monthView.setEnabled(false);}
    			TimetableView.add(monthView);
    			JMenuItem weekView = new JMenuItem ("Week View");
    			weekView.addActionListener(wViewAL);
    			if (timetableView == 1) { weekView.setEnabled(false);}
    			TimetableView.add(weekView);
    			
				//UIManager.put("logoutMenuItem.selectionBackground", new Color(199,66,35));
				logoutMenuItem.addActionListener(logoutMenuItemAL);
    			accountMenu.add(logoutMenuItem);		
    		}
    	menuBar.add(accountMenu);
    	menuBar.add(TimetableView);
    }
    ActionListener loginMenuItemAL = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
        	JFrame mainFrame = App.getApplication().getMainFrame();
            LoginDialog loginDialog = new LoginDialog(mw, mainFrame, true);
            loginDialog.pack();
            loginDialog.setLocationRelativeTo(null);
            loginDialog.setBackground(Color.black); //changes bg color of login panel
            loginDialog.setVisible(true);
            clearOldEvents();
        }
    };
    ActionListener dummyMenuItemAL = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
        	String lastDummy = (String) Mysql.query("SELECT username FROM users WHERE username LIKE 'DummyAccount%' ORDER BY userId DESC LIMIT 1").get(0).get("username");
        	int dummyStart = Integer.parseInt(lastDummy.substring(12));
        	for (int aC = 1; aC <= 25; aC += 1) {
        		Mysql.query("INSERT INTO users (username, password, email, workday) VALUES ('DummyAccount"+(aC+dummyStart)+"', '"+Utils.passEncrypt("Test".toCharArray())+"', 'Test', '09:00-17:00')");
        	}
        }
    };
    ActionListener mViewAL = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
        	timetableView = 2;
        	update();
        }
    };
    ActionListener wViewAL = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
        	timetableView = 1;
        	update();
        }
    };
    ActionListener ProfileMenuItemAL = new ActionListener(){
    	@Override
    	public void actionPerformed (ActionEvent actionEvent){
    		JFrame mainFrame = App.getApplication().getMainFrame();
    		ProfileDialog profileDialog = new ProfileDialog(mw,mainFrame, true, userID);
    		profileDialog.pack();
    		profileDialog.setLocationRelativeTo(null);
    		profileDialog.setSize(new Dimension(320,275));
    		profileDialog.setVisible(true);
    		
    	}
    };
    ActionListener logoutMenuItemAL = new ActionListener() {
    	@Override
    	public void actionPerformed(ActionEvent actionEvent) {
    		if (Utils.confirm("Logout?")) {
    			user = new User(-4);
    			update();
    		}
    	}
    };
    ActionListener registerMenuItemAL = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            JFrame mainFrame = App.getApplication().getMainFrame();
            RegisterDialog registerDialog = new RegisterDialog(mw, mainFrame, true);
            registerDialog.pack();
            registerDialog.setLocationRelativeTo(null);
            registerDialog.setSize(new Dimension(320,275));
            registerDialog.setVisible(true);
        }
    };
    /*ActionListener eventItemAL = new ActionListener() {
    	@Override
    	public void actionPerformed(ActionEvent actionEvent) {
    		JFrame mainFrame = App.getApplication().getMainFrame();
            EventDialog eventDialog = new EventDialog(mw, mainFrame, true, user.getId(), "", "");
            eventDialog.pack();
            eventDialog.setLocationRelativeTo(null);
            eventDialog.setSize(new Dimension(640,480));
            eventDialog.setVisible(true);
    	}
    };*/
    
    public void clearOldEvents() {
    	//Delete all events from < Yesterday
    	if (user.getId() != -4) {
	    	ArrayList<HashMap<String,Object>> userEvents = Mysql.query("SELECT * FROM event WHERE userId='"+user.getId()+"'");
	    	for (int i = userEvents.size()-1; i >= 0; i -= 1) {
	    		try {
	    			if (Utils.parseDate((String) userEvents.get(i).get("date")).getTime() < new Date().getTime()-86400000) {
		    			Mysql.query("DELETE FROM event WHERE eventId='"+userEvents.get(i).get("eventId")+"'");
		    			userEvents.remove(i);
		    		}
	    		} catch (Exception e) {
	    			System.err.println("There was a problem clearing up old events");
	    		}
	    	}
    	}
    }

    public final void update() {
    	menuBar.removeAll();
    	createMenuBar();
    	int index = tabbedPane.getSelectedIndex();
    	if (index == -1) { index = 0;}
    	tabbedPane.removeAll();
    	tabbedPane.add("Timetable", new TimetablePanel(this, eventController));
    	tabbedPane.add("Group", createGroupPanel());
    	tabbedPane.addTab("Map", createMapPanel());
        if (user.getId() == -4) { tabbedPane.setEnabledAt(1,false); tabbedPane.setEnabledAt(2,false);} else { tabbedPane.setEnabledAt(1,true); tabbedPane.setEnabledAt(2,true);}
    	tabbedPane.setSelectedIndex(index);
    }
    
    public void setUser(long userId) {
    	this.user = new User(userId);
    	this.userID = userId;
    	eventController.user = new User(userId);
    	
    	update();
    }
    
    public User getUser() {
    	return user;
    }
    
    public int getView() {
    	return timetableView;
    }
    
    /** This method is called from within the constructor to
     * initialise the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        menuBar = new javax.swing.JMenuBar();
        statusPanel = new javax.swing.JPanel();
        javax.swing.JSeparator statusPanelSeparator = new javax.swing.JSeparator();
        statusMessageLabel = new javax.swing.JLabel();
        statusAnimationLabel = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();

        mainPanel.setName("mainPanel"); // NOI18N
        mainPanel.setLayout(new java.awt.BorderLayout());

        menuBar.setName("menuBar"); // NOI18N

        statusPanel.setName("statusPanel"); // NOI18N

        statusPanelSeparator.setName("statusPanelSeparator"); // NOI18N

        statusMessageLabel.setName("statusMessageLabel"); // NOI18N

        statusAnimationLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        statusAnimationLabel.setName("statusAnimationLabel"); // NOI18N

        progressBar.setName("progressBar"); // NOI18N

        javax.swing.GroupLayout statusPanelLayout = new javax.swing.GroupLayout(statusPanel);
        statusPanel.setLayout(statusPanelLayout);
        statusPanelLayout.setHorizontalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(statusPanelSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(statusMessageLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 230, Short.MAX_VALUE)
                .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(statusAnimationLabel)
                .addContainerGap())
        );
        statusPanelLayout.setVerticalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addComponent(statusPanelSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(statusMessageLabel)
                    .addComponent(statusAnimationLabel)
                    .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(3, 3, 3))
        );

        setComponent(mainPanel);
        setMenuBar(menuBar);
        setStatusBar(statusPanel);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel mainPanel;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JLabel statusAnimationLabel;
    private javax.swing.JLabel statusMessageLabel;
    private javax.swing.JPanel statusPanel;
    // End of variables declaration//GEN-END:variables

	

	
}
