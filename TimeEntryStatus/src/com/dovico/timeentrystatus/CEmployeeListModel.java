package com.dovico.timeentrystatus;

import java.util.ArrayList;

import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.dovico.commonlibrary.APIRequestResult;
import com.dovico.commonlibrary.CRESTAPIHelper;
import com.dovico.commonlibrary.CXMLHelper;



// We create a subclass of ListModel so that this object can be used directly in the JList object
public class CEmployeeListModel implements ListModel {
	// Holds the list of listeners (the JList control only redraws if something changes. this listener is how we tell the list that something changed)
	protected ArrayList<ListDataListener> m_lstListeners = null;
		
	// Holds the list of Employees that are currently loaded
	protected ArrayList<CEmployee> m_lstEmployees = null;
	
	// WARNING: I had forgot about this and wondered why things weren't working right...Strings in Java are 'Objects'...doing a '==' on two objects does not give the
	// 			desired result. You need to call the object's 'equals' method to test for equality in Java.
	//
	// Previous Page
	protected String m_sPrevPageURI = Constants.URI_NOT_AVAILABLE;
	public boolean hasPrevPageURI() { return (m_sPrevPageURI.equals(Constants.URI_NOT_AVAILABLE) ? false : true); }
	public String getPrevPageURI() { return m_sPrevPageURI; }
	public void setPrevPageURI(String sPrevPageURI) { m_sPrevPageURI = sPrevPageURI; }

	// Next Page
	protected String m_sNextPageURI = Constants.URI_NOT_AVAILABLE;
	public boolean hasNextPageURI() { return (m_sNextPageURI.equals(Constants.URI_NOT_AVAILABLE) ? false : true); }
	public String getNextPageURI() { return m_sNextPageURI; }
	public void setNextPageURI(String sNextPageURI) { m_sNextPageURI = sNextPageURI; }
	
	
	// Constructor
	public CEmployeeListModel() {
		m_lstListeners = new ArrayList<ListDataListener>();
		m_lstEmployees = new ArrayList<CEmployee>(); 
	}
	
	
	// Returns the URI needed for the first page of data. After this, the Previous/Next Page URIs will be used.
	public String getURIForFirstPage() { return CRESTAPIHelper.buildURI("Employees/", "", "1"); }
	
	// Returns the URI needed for the request of Employee information for the logged in user
	public String getURIForLoggedInUser() { return CRESTAPIHelper.buildURI("Employees/Me/", "", "1"); }
	
	
	// Main method to load in the requested page of Employee data from the REST API	
	public void loadEmployeeData(String sURI, String sConsumerSecret, String sDataAccessToken) {
		// Clear the Previous/Next Page URIs in the event we have an error when trying to pull the data
		setPrevPageURI(Constants.URI_NOT_AVAILABLE);
		setNextPageURI(Constants.URI_NOT_AVAILABLE);
		
		
		// Ask the REST API for the page of data and grab the Previous/Next Page URIs from the root node. If no data was returned then exit now.
		APIRequestResult arResult = CRESTAPIHelper.makeAPIRequest(sURI, "GET", null, sConsumerSecret, sDataAccessToken);
		Document xdDoc = arResult.getResultDocument();
		if(xdDoc == null) { return; }
		
		
		Element xeEmployee = null;
		String sLastName = "", sFirstName = "", sWorkDays = "", sExpectedHoursPerDay = "";
		long lID = 0;
		
		// Grab the root element and get the Previous/Next Page URIs from it (when requesting a specific employee when in 'Employee List Mode - User view' there will
		// be no paging information returned since a single record is all that is ever returned. If that's the case we want our Previous/Next Page URIs to hold 'N/A'
		// rather than "")
		Element xeDocElement = xdDoc.getDocumentElement();
		m_sPrevPageURI = CXMLHelper.getChildNodeValue(xeDocElement, Constants.PREV_PAGE_URI, Constants.URI_NOT_AVAILABLE);
		m_sNextPageURI = CXMLHelper.getChildNodeValue(xeDocElement, Constants.NEXT_PAGE_URI, Constants.URI_NOT_AVAILABLE);
				
		// Grab the list of Employee nodes and loop through the employees...
		NodeList xnlEmployees = xeDocElement.getElementsByTagName("Employee");
		int iEmployeeCount = xnlEmployees.getLength();
		for(int iIndex = 0; iIndex < iEmployeeCount; iIndex++) {
			// Grab the current Employee element
			xeEmployee = (Element)xnlEmployees.item(iIndex);
			
			// Grab the values that we're interested in
			lID = Long.valueOf(CXMLHelper.getChildNodeValue(xeEmployee, "ID"));
			sLastName = CXMLHelper.getChildNodeValue(xeEmployee, "LastName");
			sFirstName = CXMLHelper.getChildNodeValue(xeEmployee, "FirstName");
			sWorkDays = CXMLHelper.getChildNodeValue(xeEmployee, "WorkDays");
			sExpectedHoursPerDay = CXMLHelper.getChildNodeValue(xeEmployee, "Hours");
			
			// Add the current employee item to our list
			m_lstEmployees.add(new CEmployee(lID, sLastName, sFirstName, sWorkDays, sExpectedHoursPerDay));
		} // End of the for(int iIndex = 0; iIndex < iEmployeeCount; iIndex++) loop.
		
		
		// Tell the subscribed listeners that the content of the list has changed.
		updateListenersAboutContentChange();
	}
	
	
	// Tells the subscribed listeners that the content of the list has changed.
	protected void updateListenersAboutContentChange() {
		// Loop through the list of listeners telling them that the contents of the list have changed (the following is the Java version of a foreach loop)
		for(ListDataListener ldlListener : m_lstListeners) { 
			ldlListener.contentsChanged(new ListDataEvent(m_lstEmployees, ListDataEvent.CONTENTS_CHANGED, 0, getSize())); 
		} // End of the for(ListDataListener ldlListener : m_lstListeners) loop.
	}
	
	
	// Clear our employee list and resets the Previous/Next Page URIs
	public void removeAllElements() {
		m_sPrevPageURI = Constants.URI_NOT_AVAILABLE;
		m_sNextPageURI = Constants.URI_NOT_AVAILABLE;
		m_lstEmployees.clear();
		
		// Tell the subscribed listeners that the content of the list has changed.
		updateListenersAboutContentChange();
	}
	
	
	// Necessary if we wish to subclass the ListModel object
	public Object getElementAt(int iIndex) { return m_lstEmployees.get(iIndex); }
	public int getSize() { return m_lstEmployees.size(); }	
	public void addListDataListener(ListDataListener l) { m_lstListeners.add(l); }
	public void removeListDataListener(ListDataListener l) { m_lstListeners.remove(l); }
}
