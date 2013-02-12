package com.bravo;

import javax.swing.JFrame;
import com.bravo.view.MainWindow;
import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;

public class App extends SingleFrameApplication {
    @Override
    protected void startup() {
        MainWindow mw = new MainWindow(this);
        show(mw);
        mw.getFrame().setExtendedState(JFrame.MAXIMIZED_BOTH);
    }
    
    @Override
    protected void configureWindow(java.awt.Window root) {
    	//No code required
    }

    public static App getApplication() {
        return Application.getInstance(App.class);
    }

    public static void main(String[] args) {
        launch(App.class, args);
    }
}
