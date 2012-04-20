package com.dovico.timeentrystatus;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import netscape.javascript.*; // Needed for JavaScript communication (found in plugin.jar of 'C:\Program Files (x86)\Java\jre6\lib\')
import javax.swing.*;


// Class that lets us embed this application in a web page :)
public class Applet_EmployeeTimeStatus extends JApplet {
	private static final long serialVersionUID = 1L;
	
	// Class that handles the logic of this application (creating the controls for the main form, responding to load and click events, etc)
	private CCommonUILogic m_UILogic = null;

	
	// Default constructor
	public Applet_EmployeeTimeStatus(){
		// Have the tab control, and related controls, created (NOTE: If you pass the content pane in as a parameter, it works at run-time but you can't use the
		// Google WindowBuilder Design tab - nothing shows up. When you return the root control, the JTabbedPane in this case, add add it to the content pane then
		// everything shows up but you still can't edit it using the Design tab.)
		m_UILogic = new CCommonUILogic(getContentPane(), GetActionListenerForSettingsChange());
	}
	
	
	// Action Listener for when the settings are changed (callback function from the CommonUILogic class)
	private ActionListener GetActionListenerForSettingsChange(){
		return new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {  
				// Grab the current Consumer Secret we have. If it matches our constant then clear the variable so that we don't save the value to a cookie potentially
				// exposing sensitive information
				String sConsumerSecretToSave = m_UILogic.getConsumerSecret();
				if(sConsumerSecretToSave.equals(Constants.CONSUMER_SECRET_API_TOKEN)){ sConsumerSecretToSave = ""; }
				
				
				// Call our Save Settings function
				saveSettings(sConsumerSecretToSave, m_UILogic.getDataAccessToken(), m_UILogic.getEmployeeListModeIsManagerView());
			}
		};
	}	
	
	
	@Override
	public void init() {
		// I'm not sure what's the deal but this throws an exception when run from the IDE. When run from a web page it works fine.		
		try{
			JSObject winMain = JSObject.getWindow(this);
			if(winMain != null){  winMain.call("passUsTheSettingsData", null); }	
		}
		catch(Throwable e){}
	}
	
	
	// Called by the JavaScript to tell us what the settings are from the cookie (without signing this app we don't have permission to access Preferences so we're 
	// doing a workaround instead) 
	public void JSCallBackReturningSettingsData(String sRootURI, String sConsumerSecret, String sUserToken, String sEmployeeListModeIsManagerView){
		m_UILogic.handlePageLoad(sConsumerSecret, sUserToken, (sEmployeeListModeIsManagerView == "T"));
	}
	
	
	// Called when the user clicks on the Main tab after having been on the Settings tab and everything validated OK 
	public void saveSettings(String sConsumerSecret, String sUserToken, boolean bEmployeeListModeIsManagerView) {
		try{
			JSObject winMain = JSObject.getWindow(this);
			if(winMain != null){  winMain.call("saveTheSettingsData", new String[] { sConsumerSecret, sUserToken, (bEmployeeListModeIsManagerView ? "T": "F") }); }	
		}
		catch(Throwable e){}
	}
}
