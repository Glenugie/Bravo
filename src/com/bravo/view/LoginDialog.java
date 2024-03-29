package com.bravo.view;

import com.bravo.utils.*;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SpringLayout;

public class LoginDialog extends javax.swing.JDialog {
	private static final long serialVersionUID = -8890905461192089849L;
	private MainWindow mainWindow;
    JTextField username;
    JPasswordField password;

    public LoginDialog(MainWindow mw, java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        mainWindow = mw;
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

        JPanel loginPanel = new JPanel(new SpringLayout());
        username = new JTextField();
        password = new JPasswordField();
        JButton loginButton = new JButton("Login to System");
        loginButton.addActionListener(loginButtonAL);
        
        loginPanel.add(new JLabel("Username: "));
        loginPanel.add(username);
        loginPanel.add(new JLabel("Password: "));
        loginPanel.add(password);
        loginPanel.add(loginButton);
        
        getRootPane().setDefaultButton(loginButton);
        
        SpringUtilities.makeCompactGrid(loginPanel, 5, 1, 10, 10, 10, 10);
        this.add(loginPanel);
    }    
    ActionListener loginButtonAL = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
    		try {
    			if (!username.getText().equals("") && !password.equals("")) {
	    			ArrayList<HashMap<String, Object>> userArray = Mysql.query("SELECT * FROM users WHERE username='"+username.getText()+"'");
	    			String encryptedPass = Utils.passEncrypt(password.getPassword());
	    			if (userArray.size() <= 0) {
	    				Utils.error("We couldn't find a user matching that username in our database");
	    			} else if (!userArray.get(0).get("password").equals(encryptedPass)) {
	    				Utils.error("Incorrect password");
	    			} else {
	    				mainWindow.setUser(new Integer((int) userArray.get(0).get("userId")).longValue());
	    				
	    				dispose();
	    			}
    			} else {
    				Utils.error("You didn't fill out a required field");
    			}
    		} catch (Exception e) {
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
