package com.bravo.view;

import com.bravo.model.User;
import com.bravo.utils.*;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SpringLayout;

public class GroupDialog extends javax.swing.JDialog {
	private static final long serialVersionUID = -8890905461192089849L;
	private MainWindow mainWindow;
	private long userId;
	private ArrayList<Long> groupUsers;
	private JPanel userPanel;
    private ArrayList<JCheckBox> userCBs;

    public GroupDialog(MainWindow mw, java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        mainWindow = mw;
        userId = mw.getUser().getId();
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
        this.setTitle(("Login"));
        userPanel = new JPanel(new SpringLayout());
    	ArrayList<User> allUsers = new ArrayList<User>();
    	for (HashMap<String,Object> user : Mysql.query("SELECT userId FROM users WHERE userId!='"+userId+"'")) {
    		allUsers.add(new User(new Integer((int) user.get("userId")).longValue()));
    	}
    	
    	JPanel subPanel = new JPanel(new SpringLayout());
    	for (User user : allUsers) {
            JCheckBox selectUser = new JCheckBox();
    		selectUser.setName(new Long(user.getId()).toString());
    		selectUser.setSelected(groupUsers.contains(new Long(user.getId())));
            selectUser.addActionListener(selectUserAL);
            userCBs.add(selectUser);
            subPanel.add(selectUser);
    		subPanel.add(new JLabel(user.getName()));
    	}
        SpringUtilities.makeCompactGrid(subPanel, allUsers.size(), 2, 10, 10, 10, 10);
    	JScrollPane userListPanel = new JScrollPane(subPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    	
    	userPanel.add(new JLabel("Users:"));
    	userPanel.add(userListPanel);    	
        SpringUtilities.makeCompactGrid(userPanel, 2, 1, 10, 10, 10, 10);
    }    
    ActionListener loginButtonAL = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
    		try {
    			dispose();
    		} catch (Exception e) {
    			e.printStackTrace();
    			Utils.error("Misc Error");
    		}
        }
    };
    ActionListener selectUserAL = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
        	JCheckBox cb = (JCheckBox) actionEvent.getSource();
        	if (cb.isSelected()) {
        		groupUsers.add(Long.parseLong(cb.getName()));
        	} else {
        		groupUsers.remove(Long.parseLong(cb.getName()));
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
