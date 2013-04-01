package com.bravo.view;
import com.bravo.model.User;
import com.bravo.utils.*;


import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SpringLayout;
import javax.swing.border.EmptyBorder;
import com.bravo.controller.*;
public class ProfileDialog extends javax.swing.JDialog{
	private static final long serialVersionUID = 5427903396379849829L;
	private MainWindow mainWindow;
    JTextField username;
    JPasswordField password;
    JPasswordField newPassword;
    JTextField email;
    JTextField confirmEmail;
    private EventController eventController;
    long userid;
    
    public ProfileDialog (MainWindow mw,java.awt.Frame parent, boolean modal, long userID){
    	super(parent, modal);
    	this.userid=userID;
        mainWindow = mw;
        initComponents();
        initMyComponents();
        KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        this.getRootPane().registerKeyboardAction(closeActionListener, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
    }
    
    ActionListener closeActionListener = new ActionListener(){
    	@Override
    	public void actionPerformed (ActionEvent actionEvent){
    		dispose();
    		
    	}
    };
    public void initMyComponents(){
    	this.setTitle("Profile");
    	
    	JPanel ProfilePanel = new JPanel (new GridLayout(8,7));
    	
    	username=new JTextField(10);
    	username.setEditable(false);
    	/*User user=mainWindow.getUser();
    	username.setText(user.toString());*/
    	//ArrayList<HashMap<String, Object>> userArray = Mysql.query("SELECT * FROM users WHERE username='"+username.getText()+"'");    	
    	//username.setText(user.getText()+ "," + user);
    	/*String user = User.class.getName();
    	username.setText(user);
    	System.out.println(user);*/
    	//username.setText(String.valueOf(userid)); Just for testing that correct user ID is displayed
    	//ArrayList<HashMap<String, Object>> userArray = Mysql.query("SELECT username FROM users WHERE userId='"+String.valueOf(userid)+"'");
    	User user = new User(userid);
    	
    	username.setText(user.getName());
    	password = new JPasswordField();
    	newPassword = new JPasswordField();
    	email= new JTextField();
    	email.setText(user.getEmail());
    	
    	JButton UpdateProfile = new JButton("Update Profile");
    	UpdateProfile.addActionListener(UpdateProfileAL);
    	
    	
    	ProfilePanel.add(new JLabel("Username: "));
    	ProfilePanel.add(username);
    	this.add(ProfilePanel);
    	
       
    	ProfilePanel.add(new JLabel("Existing password: "));
    	ProfilePanel.add(password);
    	ProfilePanel.add(new JLabel("New password: "));
    	ProfilePanel.add(newPassword);
    	ProfilePanel.add(new JLabel("Email"));
    	ProfilePanel.add(email);
    	//SpringUtilities.makeCompactGrid(ProfilePanel, 5, 2, 10, 10, 10, 10);
        
        JPanel ProfileButton = new JPanel();
        ProfileButton.add(UpdateProfile);
        this.add(ProfileButton);
        
        
    	
    }
    ActionListener UpdateProfileAL = new ActionListener(){
    	@Override
    	public void actionPerformed (ActionEvent actionEvent){
    		try{
    			if(!username.getText().equals("") &&!password.equals("")){
    				ArrayList<HashMap<String, Object>> userArray = Mysql.query("SELECT * FROM users WHERE username='" + username.getText() + "'");
    				String encryptedPass = Utils.passEncrypt(password.getPassword());
    				if (!userArray.get(0).get("password").equals(encryptedPass)) {
	    				Utils.error("Incorrect password");
    			}else if (password.getText().equals( newPassword.getText())){
	    					Utils.error ("New password is the same as old password");
	    				}else if(!email.getText().contains("@")){
	    					Utils.error("Enter valid email address");
	    					
    				}else{
    					if (Utils.confirm("Confirm Update")){{ 
    						//Mysql.query("UPDATE users (username, password, email)SET VALUES ('"+username.getText()+"', '"+Utils.passEncrypt(newPassword.getPassword())+"', '"+email.getText()+"')WHERE username='" +username.getText()+"')");
    						Mysql.query("UPDATE users SET password='"+Utils.passEncrypt(newPassword.getPassword())+"',email='"+email.getText()+"' WHERE username ='"+username.getText()+"'");
    						
    						dispose();
    					}
    					
    					
    					}
    				}
    			}
    		}catch (Exception e) {
    			e.printStackTrace();
    			Utils.error("Misc Error");
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
private void initComponents() {

    setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
    setName("Form"); // NOI18N
    getContentPane().setLayout(new javax.swing.BoxLayout(getContentPane(), javax.swing.BoxLayout.PAGE_AXIS));

    pack();
}// </editor-fold>//GEN-END:initComponents
// Variables declaration - do not modify//GEN-BEGIN:variables
// End of variables declaration//GEN-END:variables
}
