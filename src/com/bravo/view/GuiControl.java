package com.bravo.view;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;

import javax.swing.JPanel;

public class GuiControl extends JPanel {
	
	private Image img;
	public GuiControl(Image img) { //method for background image in main window
		this.img = img;
		Dimension size = new Dimension(img.getWidth(null), img.getHeight(null));
		setPreferredSize(size);
		setMinimumSize(size);
		setMaximumSize(size);
		setSize(size);
		setLayout(null);
		
		
	}
	public void paintComponent(Graphics g){ //painting image in main menu
		g.drawImage(img,0,0,null);
		
	}
}
