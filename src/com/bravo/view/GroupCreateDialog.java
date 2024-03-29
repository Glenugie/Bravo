package com.bravo.view;

import com.bravo.model.User;
import com.bravo.utils.*;

import java.awt.BorderLayout;
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

public class GroupCreateDialog extends javax.swing.JDialog {
	private static final long serialVersionUID = -8890905461192089849L;
	private MainWindow mainWindow;
	private long userId;
	private JTextField groupName;

    public GroupCreateDialog(MainWindow mw, java.awt.Frame parent, boolean modal) {
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
    
    public boolean groupNameCheck() {
    	ArrayList<String> allGroups = new ArrayList<String>();
    	for (HashMap<String,Object> group : Mysql.query("SELECT groupname FROM groups")) {
    		allGroups.add( (String) group.get("groupname"));
    	} 
    	for (String i: allGroups){
    		if (i.equals(groupName.getText())){
    			this.setTitle(("Group Already Exists"));
    			JButton groupOK = new JButton ("Ok");
    			groupOK.addActionListener(groupButtonAL);
    			return true;
    		}
    	}
    	return false;
    }

    private void initMyComponents() {
    	this.setTitle(("Create Group"));
    	JPanel groupPanel = new JPanel(new SpringLayout());
    	groupName = new JTextField();
    	groupName.setMaximumSize( groupName.getPreferredSize());
    	
    	groupPanel.add(groupName);
    	groupPanel.setBackground(Color.WHITE);
    	JButton groupCreate = new JButton("Create Group");
    	groupCreate.addActionListener(groupButtonAL);
    	groupPanel.add(groupCreate);
    	
    	SpringUtilities.makeCompactGrid(groupPanel, 2, 1, 10, 10, 10, 10);
    	this.add(groupPanel);
    	
    }    
    ActionListener groupButtonAL = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
    		try {
    			if (!groupNameCheck()){
    				Mysql.query("INSERT INTO groups (groupname, groupleader) VALUES ('"+groupName.getText()+"', '"+userId+"')");
    				long groupId = new Integer((int) Mysql.queryTerm("groupId","groups","WHERE groupname='"+groupName.getText()+"'")).longValue();
    				Mysql.query("INSERT INTO group_members (groupId, userId) VALUES ('"+groupId+"', '"+userId+"')");
    				update();
    			}
    			mainWindow.update();
    			dispose();
    		} catch (Exception e) {
    			e.printStackTrace();
    			Utils.error("Misc Error");
    		}
        }
    };

    public void update() {
    	initMyComponents();
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
