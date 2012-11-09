package com.dovico.timeentrystatus;

import java.awt.*;
import javax.swing.*;

import com.dovico.commonlibrary.CPanel_Settings;


public class CPanel_SettingsEx extends CPanel_Settings {
	private static final long serialVersionUID = 1L;

	private JLabel m_lblEmployeeListMode = null;
	private ButtonGroup m_btnGroup = null;
	private JRadioButton m_optManagerView = null;
	private JRadioButton m_optUserView = null;
	
	// Default constructor
	public CPanel_SettingsEx() {
		// Call the parent class's constructor (handles setting up the controls) 
		super();		
		
		
		// Employee List Mode label
		m_lblEmployeeListMode = new JLabel("Employee List Mode:");
		m_lblEmployeeListMode.setBounds(10, 73, 117, 20);
		m_lblEmployeeListMode.setFont(new Font("Arial", Font.PLAIN, 11));
		this.add(m_lblEmployeeListMode);
		
		// Employee List Mode - Manager view
		m_optManagerView = new JRadioButton("Manager view");
		m_optManagerView.setBounds(133, 73, 305, 20);
		m_optManagerView.setFont(new Font("Arial", Font.PLAIN, 11));
		m_optManagerView.setSelected(true);
		this.add(m_optManagerView);
		
		// Employee List Mode - User view
		m_optUserView = new JRadioButton("User view");
		m_optUserView.setBounds(133, 103, 305, 20);
		m_optUserView.setFont(new Font("Arial", Font.PLAIN, 11));
		this.add(m_optUserView);
		
		// Group the two radio buttons together
		m_btnGroup = new ButtonGroup();
		m_btnGroup.add(m_optManagerView);
		m_btnGroup.add(m_optUserView);
	}
	
	
	// Populate our extra fields
	/// <history>
    /// <modified author="C. Gerard Gallant" date="2011-12-15" reason="Due to recent changes to the CPanel_Settings constructor, had to modify the constructor call by passing in dummy values for the employee information (this app does not need the employee information at this time)"/>
    /// </history>
	public void setSettingsData(String sConsumerSecret, String sDataAccessToken, boolean bEmployeeListModeIsManagerView){
		// We will hide the Consumer Secret field if the constant for the token is not an empty string. Pass the proper consumer secret value to our parent class
		// if the constant was specified. If not, use the token that was last saved by the user.
		boolean bHideConsumerSecretField = !Constants.CONSUMER_SECRET_API_TOKEN.isEmpty();
		String sConsumerSecretToUse = (bHideConsumerSecretField ? Constants.CONSUMER_SECRET_API_TOKEN : sConsumerSecret);
		
		// Pass the Consumer Secret and Data Access Token to the parent class. Just pass in default values for the employee information (not used by this app)
		super.setSettingsData(sConsumerSecretToUse, sDataAccessToken, Constants.API_VERSION_TARGETED, 0L, "", "", bHideConsumerSecretField);
		
		
		// The vertical offset we want to bump the Access Token labels down by
		int iAccessTokenLabelVerticalOffset = 80;
		
		// If we hid the Consumer Secret field then...
		if(bHideConsumerSecretField){
			m_lblEmployeeListMode.setBounds(10, (73 - 30), 117, 20);
			m_optManagerView.setBounds(133, (73 - 30), 305, 20);
			m_optUserView.setBounds(133, (103 - 30), 305, 20);
			iAccessTokenLabelVerticalOffset -= 30;
		} // End if(bHideConsumerSecretField)

		// Bump the Access Token labels down some so that they are below the controls on our settings dialog
		super.adjustAccessTokenLabelVerticalPositions(iAccessTokenLabelVerticalOffset);

		
		// Cause the radio buttons to be selected based on if we're in Manager view or User view 
		m_optManagerView.setSelected(bEmployeeListModeIsManagerView);
		m_optUserView.setSelected(!bEmployeeListModeIsManagerView);
	}
	
	
	@Override
	public boolean validateSettingsData() {
		// If the parent class validation fails then...(checks the URI, Consumer Secret, and Data Access Token values)
		if(!super.validateSettingsData()) { return false; }
		
		// Return true (there is nothing else for us to validate but if there was, you would do it here and return 'false' if there is an issue)
		return true;
	}
	
	
	// Returns if the Manager or User view is selected for the Employee List Mode
	public boolean isEmployeListModeManagerView() { return (m_btnGroup.getSelection() == m_optManagerView.getModel()); }
}
