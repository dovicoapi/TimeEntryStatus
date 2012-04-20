package com.dovico.timeentrystatus;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.util.prefs.Preferences; // Placed here rather than in EmployeeTimeStatus_CommonLogic because it causes an error when run as an Applet


// When used as a desktop application, this is the main class 
public class Form_EmployeeTimeStatus {

	// The main window that will be displayed to the user
	protected JFrame m_frmTimeEntryStatus = null;

	
	// Class that handles the logic of this application (creating the controls for the main form, responding to load and click events, etc)
	private CCommonUILogic m_UILogic = null;	

		
	// Launch the application.
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Form_EmployeeTimeStatus window = new Form_EmployeeTimeStatus();
					window.m_frmTimeEntryStatus.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	
	// Default constructor (triggered by the 'main' method when it creates the new Form_EmployeeTimeStatus class instance)
	public Form_EmployeeTimeStatus() {
		// Now create the main window frame of our application
		m_frmTimeEntryStatus = new JFrame();
		m_frmTimeEntryStatus.setResizable(false);
		m_frmTimeEntryStatus.addWindowListener(new WindowAdapter() {
			@Override
			public void windowOpened(WindowEvent arg0) 
			{
				// Grab the Preferences values and pass the values to the handlePageLoad function
				Preferences prefs = Preferences.userNodeForPackage(Form_EmployeeTimeStatus.class);
				m_UILogic.handlePageLoad(prefs.get(Constants.PREFS_KEY_CONSUMER_SECRET, ""), prefs.get(Constants.PREFS_KEY_USER_TOKEN, ""), 
						prefs.getBoolean(Constants.PREFS_KEY_EMPLOYEE_LIST_MODE, true)); 
			}
		});
		m_frmTimeEntryStatus.setTitle("Time Entry Status");
		m_frmTimeEntryStatus.setBounds(100, 100, 609, 408);
		m_frmTimeEntryStatus.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		
		// Have the tab control, and related controls, created (NOTE: If you pass the content pane in as a parameter, it works at run-time but you can't use the
		// Google WindowBuilder Design tab - nothing shows up. When you return the root control, the JTabbedPane in this case, then everything works OK. Weird.)
		m_UILogic = new CCommonUILogic(m_frmTimeEntryStatus.getContentPane(), GetActionListenerForSettingsChange());
	}
	
	
	// Action Listener for when the settings are changed (callback function from the CommonUILogic class)
	private ActionListener GetActionListenerForSettingsChange(){
		return new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {  
				// Grab the current Consumer Secret we have. If it matches our constant then clear the variable so that we don't save the value potentially
				// exposing sensitive information
				String sConsumerSecretToSave = m_UILogic.getConsumerSecret();
				if(sConsumerSecretToSave.equals(Constants.CONSUMER_SECRET_API_TOKEN)){ sConsumerSecretToSave = ""; }
				
				
				// Save the settings
				Preferences prefs = Preferences.userNodeForPackage(Form_EmployeeTimeStatus.class);
				prefs.put(Constants.PREFS_KEY_CONSUMER_SECRET, sConsumerSecretToSave);
				prefs.put(Constants.PREFS_KEY_USER_TOKEN, m_UILogic.getDataAccessToken());
				prefs.putBoolean(Constants.PREFS_KEY_EMPLOYEE_LIST_MODE, m_UILogic.getEmployeeListModeIsManagerView());
			}
		};
	}
}

