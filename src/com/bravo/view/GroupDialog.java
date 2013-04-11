package com.bravo.view;

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
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SpringLayout;

import com.bravo.model.User;
import com.bravo.utils.Mysql;
import com.bravo.utils.SpringUtilities;
import com.bravo.utils.Utils;

public class GroupDialog extends javax.swing.JDialog {
	private static final long serialVersionUID = -8890905461192089849L;
	private MainWindow mainWindow;
	private String groupName;
	private long groupId;
	private long userId;
	private ArrayList<Long> groupUsers;
	private JPanel userPanel;
    private ArrayList<JCheckBox> userCBs;

    public GroupDialog(MainWindow mw, java.awt.Frame parent, boolean modal, String gN) {
        super(parent, modal);
        mainWindow = mw;
        groupName = gN;
        groupUsers = new ArrayList<Long>();
        groupId = new Integer((int)Mysql.queryTerm("groupId","groups","WHERE groupname='"+gN+"'")).longValue();
        ArrayList<HashMap<String,Object>> members = Mysql.query("SELECT userId FROM group_members WHERE groupID='"+groupId+"'");
        for (HashMap<String,Object> member : members) { groupUsers.add(new Integer((int) member.get("userId")).longValue());}
        userCBs = new ArrayList<JCheckBox>();
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
        this.setTitle(("Add Members to Group"));
        userPanel = new JPanel(new SpringLayout());
    	ArrayList<User> allUsers = new ArrayList<User>();
    	ArrayList<HashMap<String,Object>> users = Mysql.query("SELECT userId FROM users WHERE userId!='"+userId+"'");
    	for (HashMap<String,Object> user : users) { allUsers.add(new User(new Integer((int) user.get("userId")).longValue()));}
    	
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
    	
    	JButton addButton = new JButton("Add Members");
    	addButton.addActionListener(addButtonAL);
    	
    	userPanel.add(new JLabel("Users:"));
    	userPanel.add(userListPanel);
    	userPanel.add(addButton);
        SpringUtilities.makeCompactGrid(userPanel, 3, 1, 10, 10, 10, 10);
        
        this.add(userPanel);
    }    
    ActionListener addButtonAL = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
    		try {
    			for (int i = 0; i < groupUsers.size(); i++) {
    				long groupId = new Integer((int) Mysql.queryTerm("groupId","groups","WHERE groupname='"+groupName+"'")).longValue();
    				if (Mysql.query("SELECT id FROM group_members WHERE groupId='"+groupId+"' AND userId='"+groupUsers.get(i)+"'").size() == 0) {
    					Mysql.query("INSERT INTO group_members (groupId, userId) VALUES ('"+groupId+"', '"+groupUsers.get(i)+"')");
    				}
    			}
    			for (HashMap<String,Object> member : Mysql.query("SELECT userId FROM group_members WHERE groupID='"+groupId+"'")) {
    				boolean contains = false; for (Long uId : groupUsers) { if (uId.equals(((Integer)member.get("userId")).longValue())) {  contains = true;}} 
    				if (!contains) { Mysql.query("DELETE FROM group_members WHERE groupId='"+groupId+"' AND userId='"+member.get("userId")+"'");}
    			}
    			mainWindow.update();
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
