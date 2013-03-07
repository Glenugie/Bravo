package com.bravo.view;

import com.bravo.controller.*;
import com.bravo.model.User;
import com.bravo.utils.*;
import com.bravo.App;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SpringLayout;

import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.FrameView;

public final class MainWindow extends FrameView {
    private JTabbedPane tabbedPane;
    private JPanel timetablePanel;
    private JPanel groupPanel;
    private JPanel mapPanel;
    private JTable timetable;
    private EventController eventController;
    private User user;
    private MainWindow mw;
	
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
    
    public JPanel createMapPanel() {
    	mapPanel = new JPanel();
    	
    	/*URLConnection con = new URL("http://maps...").openConnection();
    	InputStream is = con.getInputStream();
    	byte bytes[] = new byte[con.getContentLength()];
    	is.read(bytes);
    	is.close();
    	Toolkit tk = getToolkit();
    	map = tk.createImage(bytes);
    	tk.prepareImage(map, -1, -1, null);*/
    	
    	return mapPanel;
    }
    
    public JPanel createGroupPanel() {
        groupPanel = new JPanel(new SpringLayout());
        groupPanel.add(new JLabel("Groups are an abstract concept that don't truly exist in this universe"));
        JButton groupCreate = new JButton("Create Group");
        groupCreate.addActionListener(groupCreateAL);
        groupPanel.add(groupCreate);
        
        JPanel groupList = new JPanel(new SpringLayout());
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
    	JMenu accountMenu = new JMenu("Account");
    		if (user.getId() == -4) {
		    	JMenuItem loginMenuItem = new JMenuItem("Login");
		    		loginMenuItem.addActionListener(loginMenuItemAL);
		    		accountMenu.add(loginMenuItem);
		    	JMenuItem registerMenuItem = new JMenuItem("Register");
		    		registerMenuItem.addActionListener(registerMenuItemAL);
		    		accountMenu.add(registerMenuItem);
    		} else {
    			JMenuItem logoutMenuItem = new JMenuItem("Logout");
    				logoutMenuItem.addActionListener(logoutMenuItemAL);
	    			accountMenu.add(logoutMenuItem);
    		}
    	menuBar.add(accountMenu);
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
	    	ArrayList<HashMap<String,Object>> userEvents = Mysql.query("SELECT * FROM timetable WHERE userId='"+user.getId()+"'");
	    	for (int i = userEvents.size()-1; i >= 0; i -= 1) {
	    		try {
	    			if (Utils.parseDate((String) userEvents.get(i).get("date")).getTime() < new Date().getTime()-86400000) {
		    			Mysql.query("DELETE FROM timetable WHERE eventId='"+userEvents.get(i).get("eventId")+"'");
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
    	
    	tabbedPane.removeAll();
    	tabbedPane.add("Timetable", new TimetablePanel(this, eventController));
    	tabbedPane.add("Group", createGroupPanel());
        tabbedPane.addTab("Map", createMapPanel());
        if (user.getId() == -4) { tabbedPane.setEnabledAt(1,false); tabbedPane.setEnabledAt(2,false);} else { tabbedPane.setEnabledAt(1,true); tabbedPane.setEnabledAt(2,true);}
    }
    
    public void setUser(long userId) {
    	this.user = new User(userId);
    	eventController.user = new User(userId);
    	update();
    }
    
    public User getUser() {
    	return user;
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
