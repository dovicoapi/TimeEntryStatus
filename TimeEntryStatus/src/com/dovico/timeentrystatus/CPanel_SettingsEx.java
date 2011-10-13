package com.dovico.timeentrystatus;

import java.awt.*;
import javax.swing.*;

import com.dovico.commonlibrary.CPanel_Settings;


public class CPanel_SettingsEx extends CPanel_Settings {
	private static final long serialVersionUID = 1L;

	private ButtonGroup m_btnGroup = null;
	private JRadioButton m_optManagerView = null;
	private JRadioButton m_optUserView = null;
	
	// Default constructor
	public CPanel_SettingsEx() {
		// Call the parent class's constructor (handles setting up the controls) 
		super();		
		
		
		// Employee List Mode label
		JLabel lblEmployeeListMode = new JLabel("Employee List Mode:");
		lblEmployeeListMode.setBounds(10, 73, 117, 20);
		lblEmployeeListMode.setFont(new Font("Arial", Font.PLAIN, 11));
		this.add(lblEmployeeListMode);
		
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
	public void setSettingsData(String sConsumerSecret, String sDataAccessToken, boolean bEmployeeListModeIsManagerView){
		// Pass the Consumer Secret and Data Access Token to the parent class
		super.setSettingsData(sConsumerSecret, sDataAccessToken);
		
		
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
