package com.dovico.timeentrystatus;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.border.TitledBorder;
import javax.swing.event.*;

import com.dovico.commonlibrary.CPanel_About;

//
// We now have two classes 'Form_EmployeeTimeStatus', which is for standard Desktop type applications, and 'Applet_EmployeeTimeStatus' which is for web-based 
// applications  
public class CCommonUILogic {
	// Listener from the UI class that gets called when the settings data has changed so that it can update its UI as need be
	private ActionListener m_alSettingsChanged = null; 
		
	private Date m_dtDateRangeStart = null;
	private Date m_dtDateRangeEnd = null;
	private CEmployeeListModel m_lstEmployeeData = null;
	
	private JTabbedPane m_pTabControl = null;
	
	// Controls on the Main tab (the ones we want to manipulate)
	private CPanel_TimeEntry m_pTimelinePanel = null;
	private JList m_lstEmployees = null;
	private JButton m_btnNextPage = null;
	private JButton m_btnPrevPage = null;
	
	// Settings tab
	private CPanel_SettingsEx m_pSettingsTab =  null;
	private CPanel_About m_pAboutTab = null;
	private int m_iPreviousTabIndex = -1;
	private int m_iSettingsTabIndex = 1;
		
	// Variables needed for logging into the REST API (NOTE: I usually use 'protected' but that seems to allow any other class to access the variables too)
	private String m_sConsumerSecret = ""; // also known as the 3rd party developer key
	private String m_sDataAccessToken = "";
	
	// We display all Employees by default (in User view, we only display the logged in user - useful when the logged in user does not have Read access to the
	// Employees view)
	private boolean m_bEmployeeListModeIsManagerView = true;
	
	
	// Default constructor
	public CCommonUILogic(Container cContainer, ActionListener alSettingsChanged){
		// Change the look from the Metal UI which I find kind of ugly
		try {
			// Loop through the various LookAndFeel items to see if 'Nimbus' exists. If yes then...
		    for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
		        if ("Nimbus".equals(info.getName())) {
		            UIManager.setLookAndFeel(info.getClassName());
		            break;
		        } // End if ("Nimbus".equals(info.getName()))
		    } // End of the for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) loop.
		}catch (Exception e) {
			// Switch the look to the system default
			try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } 
			catch (UnsupportedLookAndFeelException e2) { }
			catch (ClassNotFoundException e2) { }
			catch (InstantiationException e2) { }
			catch (IllegalAccessException e2) { }
		} // End of the catch (Exception e) statement.

		
		
		// Determine the End date that we will display	
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.set(Calendar.HOUR_OF_DAY, 0); // Make sure the Hour, Minute, and Second fields are 0 (otherwise, it throws off date comparisons)
	    cal.set(Calendar.MINUTE, 0);
	    cal.set(Calendar.SECOND, 0);
	    cal.set(Calendar.MILLISECOND, 0);	    
		m_dtDateRangeEnd = cal.getTime();
		
		// The start date is 30 days before the End date
		cal.add(Calendar.DATE, -Constants.DATE_RANGE_DAYS);
		m_dtDateRangeStart = cal.getTime(); // 30 days before the start date which makes the range 31 days including the end date
			
		// The model for the list of employees displayed 
		m_lstEmployeeData = new CEmployeeListModel();
		
		// Remember the action listener for when the settings are changed (so that we can tell the proper class that the settings have changed and they need to be
		// saved
		m_alSettingsChanged = alSettingsChanged;
		
		
		// Cause the controls to be created
		initializeControls(cContainer);
	}

	
	// Called when UI is ready to have the controls created
	private void initializeControls(Container cContainer) {
		// Create the tabs themselves and add in pane 1 and 2 (set up a listener for when the tab selection is changed)
		m_pTabControl = new JTabbedPane();
		m_pTabControl.addChangeListener(new ChangeListener() {		    
		    public void stateChanged(ChangeEvent evt) { handleTabsChanged(); }
		});
				
		// Add the tab control to the container passed in
		cContainer.add(m_pTabControl);
				
		
		// Create the panel that will hold the controls for the main tab and then create the controls
		GridBagLayout gblLayoutTab1 = new GridBagLayout();
		gblLayoutTab1.rowHeights = new int[]{0, 0, 150, 0}; // Only way I could figure out how to keep the list from resizing once data was added (only happens when we use the JTabbedPane. If we added this pane directly to the panel passed to this function there is no issue) 
		JPanel pTab1 = new JPanel(gblLayoutTab1);
		createControlsForMainTab(pTab1);
		m_pTabControl.addTab("Main", null, pTab1, null);
		
		// Create our Settings Tab panel and add it to our tab control
		m_pSettingsTab = new CPanel_SettingsEx(); 
		m_pTabControl.addTab("Settings", null, m_pSettingsTab, null);
		
		// Create our About Tab panel and add it to our tab control
		m_pAboutTab = new CPanel_About("Time Entry Status", "1.2"); 
		m_pTabControl.addTab("About", null, m_pAboutTab, null);
	}
	
	
	// Helper to create the controls for the Main tab
	protected void createControlsForMainTab(JPanel pTab1) {
		// Create an object to help with the positioning of controls within the grid
		GridBagConstraints gbcConstraints = new GridBagConstraints();
		int iGridColumns = 2;// The number of columns in our grid
				
		// Status panel (1st row - spans all columns)
		m_pTimelinePanel = new CPanel_TimeEntry(m_dtDateRangeStart, m_dtDateRangeEnd);		
		adjustGridBagConstraints(GridBagConstraints.HORIZONTAL, 582, 136, iGridColumns, 1, 0, 0, 0, 0, 10, 0, gbcConstraints);
		pTab1.add(m_pTimelinePanel, gbcConstraints);
				
		
		// Employees List label (2nd row - spans all columns)
		JLabel lblNewLabel = new JLabel("Employees:");
		lblNewLabel.setFont(new Font("Arial", Font.PLAIN, 11));		
		adjustGridBagConstraints(GridBagConstraints.HORIZONTAL, 0, 0, iGridColumns, 1, 0, 1, 0, 2, 0, 0, gbcConstraints);
		pTab1.add(lblNewLabel, gbcConstraints);
				
				
		// Employees List (will be added as part of the JScrollPane below)
		m_lstEmployees = new JList(m_lstEmployeeData);		
		m_lstEmployees.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent arg0) { 
				// Only call our list selection handler if the list is done adjusting (this event fires multiple times when you make a selection)
				if(!arg0.getValueIsAdjusting()) { handleListSelection(); }
			}
		});
		m_lstEmployees.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		m_lstEmployees.setFont(new Font("Arial", Font.PLAIN, 11));
		m_lstEmployees.setBackground(Color.WHITE);
		m_lstEmployees.setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		
		
		// Scroll pane so that we can scroll our list of Employees (3rd row - spans all columns)
		JScrollPane scrollPane = new JScrollPane(m_lstEmployees);
		adjustGridBagConstraints(GridBagConstraints.BOTH, 0, 0, iGridColumns, 1, 0, 2, 0, 2, 10, 2, gbcConstraints);
		pTab1.add(scrollPane, gbcConstraints);
	
		

		// A panel allowing the Previous and Next button to be horizontally centered (colspan of 2)
		JPanel pPrevNextPanel = new JPanel(new GridBagLayout()); 
		adjustGridBagConstraints(GridBagConstraints.HORIZONTAL, 0, 0, 2, 1, 1, 3, 0, 0, 0, 0, gbcConstraints);
		pTab1.add(pPrevNextPanel, gbcConstraints);
		
		
		// Previous Page button (4th row - 2nd column)
		m_btnPrevPage = new JButton("< Previous");
		m_btnPrevPage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {  handlePrevPageClick(); }
		});
		m_btnPrevPage.setFont(new Font("Arial", Font.PLAIN, 11));
		m_btnPrevPage.setEnabled(false);
		adjustGridBagConstraints(GridBagConstraints.NONE, 50, 0, 1, 1, 0, 0, 0, 0, 6, 4, gbcConstraints);
		pPrevNextPanel.add(m_btnPrevPage, gbcConstraints);
	
	
		// Next Page button (4th row - 3rd column)
		m_btnNextPage = new JButton("Next >");
		m_btnNextPage.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent arg0) { handleNextPageClick(); } 
		});
		m_btnNextPage.setFont(new Font("Arial", Font.PLAIN, 11));
		m_btnNextPage.setEnabled(false);
		adjustGridBagConstraints(GridBagConstraints.NONE, 50, 0, 1, 1, 1, 0, 0, 0, 6, 0, gbcConstraints);		
		pPrevNextPanel.add(m_btnNextPage, gbcConstraints);	
	}

			
	// Helper to make setting the GridBagContraint values easier
	protected void adjustGridBagConstraints(int iFill, int iPadX, int iPadY, int iColSpan, int iRowSpan, int iGridX, int iGridY, int iInsetTop, int iInsetLeft, 
			int iInsetBottom, int iInsetRight, GridBagConstraints gbcConstraints)
	{
		gbcConstraints.fill = iFill; // If the control should be stretched or not if the control is smaller than the cell
		gbcConstraints.ipadx = iPadX; // Internal padding of the control (width)
		gbcConstraints.ipady = iPadY; // Internal padding of the control (height)
		gbcConstraints.gridwidth = iColSpan; // How many columns the control is to take up (a column span - this is a one-based value!)
		gbcConstraints.gridheight = iRowSpan; // How many rows the control is to take up (a row span - this is a one-based value!)
		gbcConstraints.gridx = iGridX; // Which cell the control is to be placed in on the X-Axis (this is a zero-based value!) 
		gbcConstraints.gridy = iGridY; // Which cell the control is to be placed in on the Y-Axis (this is a zero-based value!)
		
		// Margins between this control and those around it (default is 0)
		gbcConstraints.insets = new Insets(iInsetTop, iInsetLeft, iInsetBottom, iInsetRight);
	}
	
	
	
	// Called when the form is first displayed (windowOpened event). Load in the first page of Employee data.
	public void handlePageLoad(String sConsumerSecret, String sDataAccessToken, boolean bEmployeeListModeIsManagerView) 
	{ 
		// Remember the preferences specified
		m_sConsumerSecret = sConsumerSecret;
		m_sDataAccessToken = sDataAccessToken;
		m_bEmployeeListModeIsManagerView = bEmployeeListModeIsManagerView;
				
		// Make sure the Settings pane has the necessary data
		m_pSettingsTab.setSettingsData(sConsumerSecret, sDataAccessToken, bEmployeeListModeIsManagerView);
		
		
		// If either value is empty then...
		if(m_sConsumerSecret.isEmpty() || m_sDataAccessToken.isEmpty()) {
			// Make sure the Settings tab is selected
			m_iSettingsTabIndex = 1;
			m_pTabControl.setSelectedIndex(m_iSettingsTabIndex);
		} 
		else // The necessary settings are present... 
		{  
			// Load in the first page of Employee data if in Manager view. Otherwise, just load in the logged in employee's Employee object 
			String sURI = (m_bEmployeeListModeIsManagerView ? m_lstEmployeeData.getURIForFirstPage() : m_lstEmployeeData.getURIForLoggedInUser());
			loadEmployeeList(sURI);
		} // End if(m_sConsumerSecret.isEmpty() || m_sDataAccessToken.isEmpty())
	}	
	
	// User clicked on the Previous Page button
	protected void handlePrevPageClick() {
		// Disable both paging buttons and make sure the list selection is removed
		m_btnPrevPage.setEnabled(false);
		m_btnNextPage.setEnabled(false);
		m_lstEmployees.clearSelection();
		
		// Load the Previous page of Employee data
		loadEmployeeList(m_lstEmployeeData.getPrevPageURI());
	}	
	
	// User clicked on the Next Page button
	protected void handleNextPageClick() {
		// Disable both paging buttons and make sure the list selection is removed
		m_btnPrevPage.setEnabled(false);
		m_btnNextPage.setEnabled(false);
		m_lstEmployees.clearSelection();
				
		// Load the Next page of Employee data
		loadEmployeeList(m_lstEmployeeData.getNextPageURI());	
	}
	
	
	// Called by handlePageLoad, handlePrevPageClick, and handleNextPageClick to actually load in the requested page of data into the Employee list
	public void loadEmployeeList(String sURI) {
		// Indicate that processing is going on
		m_pTimelinePanel.clearStatusesAndIndicateProcessing();
		 		
		// Clear the current data (sets Previous/Next page URIs to 'N/A'). If we have a URI to call then load in the requested page of Employee data.
		m_lstEmployeeData.removeAllElements();
		if(!sURI.isEmpty()) { m_lstEmployeeData.loadEmployeeData(sURI, getConsumerSecret(), getDataAccessToken()); }
	
		// Update the Previous/Next Page buttons based on if there is a Previous/Next page of data to go to
		m_btnPrevPage.setEnabled(m_lstEmployeeData.hasPrevPageURI());
		m_btnNextPage.setEnabled(m_lstEmployeeData.hasNextPageURI());
				
		// Hide the loading message
		m_pTimelinePanel.indicateProcessing(false);
	}
	
	
	// Called when the user clicks on an item in the list control
	public void handleListSelection() {
		// Clear the contents of the panel
		m_pTimelinePanel.clearStatusesAndIndicateProcessing();
		
		// Grab the selected item. If nothing is selected then exit now (Ctrl + Click can remove the selection altogether)
		Object objSelectedItem = m_lstEmployees.getSelectedValue();
		if(objSelectedItem == null) { m_pTimelinePanel.indicateProcessing(false); return; }
		
		// Cast the selected object into our CEmployee object and then cause it to load in the time entries
		CEmployee eEmployee = (CEmployee)objSelectedItem;
		eEmployee.getTimeEntries(getConsumerSecret(), getDataAccessToken(), m_dtDateRangeStart, m_dtDateRangeEnd);
		
		// Pass the employee object off to the panel so that the Statuses can be rendered
		m_pTimelinePanel.displayEmployeeStatuses(eEmployee);
	}
	

	// Tab's selection has been changed (NOTE: This gets called when the control is initially displayed and when the tab's selection is changed via code)
	private void handleTabsChanged(){
		// If the previous tab was the Settings tab then...(user just tabbed off of the settings tab
	    if(m_iPreviousTabIndex == m_iSettingsTabIndex)
	    {
	    	// If everything validates OK for the Settings tab then...
	    	if(m_pSettingsTab.validateSettingsData()) 
	    	{
	    		// Make sure nothing is selected in the Employee list and then reload the page now that we have some new settings (the ButtonGroup object
	    		// returns the radio button's 'Model' rather than the button itself which is why we do a 'getModel' call on the ManagerView radio button to 
	    		// find out which radio button is selected)
	    		m_lstEmployees.clearSelection();
	    		handlePageLoad(m_pSettingsTab.getConsumerSecret(), m_pSettingsTab.getDataAccessToken(), m_pSettingsTab.isEmployeListModeManagerView());
	    		
	    		// Update the UI class telling it that the settings have been changed (so that the settings can be saved and data reloaded - we need to do it this
	    		// way rather than handling the load/save in the Setting panel because if the settings panel is used by an Applet, having a reference to 
	    		// 'java.util.prefs.Preferences' will throw an exception)
	    		m_alSettingsChanged.actionPerformed(null);
	    	} 
	    	else // Validation failed... 
	    	{ 		        	
	    		// Reselect the Settings tab (indicate that the previous index is not the settings tab so that the validation is not hit again) 
	    		m_iPreviousTabIndex = -1; 
	    		m_pTabControl.setSelectedIndex(m_iSettingsTabIndex);
	    	} // End if(m_pSettingsTab.validateSettingsData())
	    } // End if(m_iPreviousTabIndex == m_iSettingsTabIndex)
	    
	    
	    // Remember the selected tab index
	    m_iPreviousTabIndex = m_pTabControl.getSelectedIndex();
	}
	
		
	// Methods allowing other areas of this application to gain access to the Login values
	public String getConsumerSecret() { return m_sConsumerSecret; }
	public String getDataAccessToken() { return m_sDataAccessToken; }
	public boolean getEmployeeListModeIsManagerView(){ return m_bEmployeeListModeIsManagerView; }
}
