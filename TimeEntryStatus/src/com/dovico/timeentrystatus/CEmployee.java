package com.dovico.timeentrystatus;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.dovico.commonlibrary.APIRequestResult;
import com.dovico.commonlibrary.CRESTAPIHelper;
import com.dovico.commonlibrary.CXMLHelper;


public class CEmployee {
	private Long m_lID = null;
	private String m_sLastName = ""; // for display purposes, etc
	private String m_sFirstName = ""; // for display purposes, etc
	private boolean[] m_arrWorkDays = { false, true, true, true, true, true, false }; // Array of boolean values representing each week day and if it's a standard working day for the employee 
		
	private ArrayList<CTimeEntry> m_lstTimeEntries = null;
	

	// Overloaded constructor
	public CEmployee(Long lID, String sLastName, String sFirstName, String sWorkDays, String sExpectedHoursPerDay) {
		m_lID = lID;
		m_sLastName = sLastName;
		m_sFirstName = sFirstName;
		
		// Set the boolean values in the Work Days array (m_arrWorkDays) to match the Work Days string received 
		setWorkDays(sWorkDays);
		
		m_lstTimeEntries = new ArrayList<CTimeEntry>();
	}
	
	
	// Sets the boolean values in the Work Days array (m_arrWorkDays) to match the Work Days string received 
	protected void setWorkDays(String sWorkDays)
	{
		// Loop through each day of the week (the sWorkDays string is SMTWTFS - Sunday, Monday, Tuesday, Wednesday, Thursday, Friday, and Saturday. If a '.' is present
		// it means the day is not a standard work day for the employee)
		for(int iIndex = 0; iIndex < 7; iIndex++) {
			// Set the boolean value of the current array item to 'false' if the current day is not a standard work day for this employee. Otherwise, set the value to
			// 'true'.			
			m_arrWorkDays[iIndex] = (sWorkDays.charAt(iIndex) == '.' ? false : true);
		} // End of the for(int iIndex = 0; iIndex < 7; iIndex++) loop.
	}
	
	
	// Returns if the date specified falls on one of this employee's non-working days
	// iDayOftheWeek comes from a call to calCurrentDate.get(Calendar.DAY_OF_WEEK) and the result is a one-based value with 1 being Sunday and 7 being Saturday
	public boolean isDateANonWorkingDay(int iDayOfTheWeek) {
		// Return the value at the index specified by iDayOfTheWeek (we subtract 1 because the index is one-based and the array is zero-based). The array holds 
		// booleans that indicate if the day is a standard working day or not. Because we are looking for Non-Working days, we not the array value returned.
		return !m_arrWorkDays[(iDayOfTheWeek - 1)];
	}
	
		
	// Function that loads in the time entries of the current employee for a given date range and for specific statuses
	// Returns 'true' to the caller if time was loaded and 'false' if not (user might not have any time entries for the date range specified 
	public void getTimeEntries(String sConsumerSecret, String sDataAccessToken, Date dtStart, Date dtEnd) {
		// Make sure any previous time entries are removed
		m_lstTimeEntries.clear();
		
		
		// We want the list of time entries for a specific employee so we will use the Employee filter
		String sURIPart = ("TimeEntries/Employee/" + m_lID.toString() + "/");
		
		// Create our Date Range query string to restrict the time entries returned to just those within the date range (I manually set the
		// URI encoding for a space below - probably not the best idea)
		Format fFormatter = new SimpleDateFormat(Constants.XML_DATE_FORMAT);
		String sDateRangeQueryString = ("daterange=" + fFormatter.format(dtStart) + "%20" + fFormatter.format(dtEnd));
		
		// Build up the full URI for the request and then ask the REST API for the data (the function will call itself again if necessary based on if there is a next
		// page of data or not)
		String sURI = CRESTAPIHelper.buildURI(sURIPart, sDateRangeQueryString, "1");
		loadTimeEntryData(sURI, sConsumerSecret, sDataAccessToken);

		
		// Sort the list of time entries by date (time entries are returned with TempTrans first and then Trans. now that we have all of the time entries in one
		// list, we want to make future code easier for ourselves by having everything sorted by date)
		Collections.sort(m_lstTimeEntries, new Comparator<CTimeEntry>() { 
			public int compare(CTimeEntry t1, CTimeEntry t2) { return t1.getDate().compareTo(t2.getDate()); } 
		});
	}
	
	
	// Causes the Time Entry data, for the current employee and date range, to be loaded in
	/// <history>
    /// <modified author="C. Gerard Gallant" date="2011-12-05" reason="With the change to how the CXMLHelper.getChildNodeValue function works, this function broke. Modified this function to grab the Sheet element and from that element grab the Status and RejectedReason values"/>
    /// </history>
	protected void loadTimeEntryData(String sURI, String sConsumerSecret, String sDataAccessToken) {
		// Load in the current page of data. If there is data returned then...
		APIRequestResult arResult = CRESTAPIHelper.makeAPIRequest(sURI, "GET", null, sConsumerSecret, sDataAccessToken);
		Document xdDoc = arResult.getResultDocument();
		if(xdDoc != null) {
			// Grab the root element and grab the Next Page URI (in case we need to call this function again to load the next page of data)
			Element xeDocElement = xdDoc.getDocumentElement();
			String sNextPageURI = CXMLHelper.getChildNodeValue(xeDocElement, Constants.NEXT_PAGE_URI);

			Element xeTimeEntry = null, xeSheet = null;			
			String sID = "", sDate = "", sStatus = "", sRejectedReason = "";
			
			// Grab the list of TimeEntry nodes
			NodeList xnlTimeEntries = xeDocElement.getElementsByTagName("TimeEntry");
			int iTimeEntryCount = xnlTimeEntries.getLength();
			for(int iIndex = 0; iIndex < iTimeEntryCount; iIndex++) {
				// Grab the current TimeEntry element and the Sheet element
				xeTimeEntry = (Element)xnlTimeEntries.item(iIndex);
				xeSheet = (Element)xeTimeEntry.getElementsByTagName("Sheet").item(0);
							
				// Grab the ID (NOTE: The ID will have a prefix 'T' or 'M'. Items with the 'T' prefix will be Guids. Items with the 'M' prefix will be 'long'), Date,
				// Status, and Rejected Reason (will be an empty string unless the Status is 'R' - Rejected)
				sID = CXMLHelper.getChildNodeValue(xeTimeEntry, "ID");
				sDate = CXMLHelper.getChildNodeValue(xeTimeEntry, "Date");
				sStatus = CXMLHelper.getChildNodeValue(xeSheet, "Status");
				sRejectedReason = CXMLHelper.getChildNodeValue(xeSheet, "RejectedReason");
				
				// Add the current Time Entry item to our list
				m_lstTimeEntries.add(new CTimeEntry(sID, sDate, sStatus, sRejectedReason));
			} // End of the for(int iIndex = 0; iIndex < iTimeEntryCount; iIndex++) loop.
			
			
			// If there is yet another page of time entry data to load then load it in too.
			if(!sNextPageURI.equals(Constants.URI_NOT_AVAILABLE)) { loadTimeEntryData(sNextPageURI, sConsumerSecret, sDataAccessToken); }		
		} // End if(xeDocElement != null)
	}
		
	
	// Returns the statuses of the time for the date specified
	public void getStatusesForDate(Date dtDate, CDayStatuses dsStatuses) {
		Date dtCurrent = null;
		String sStatus = "";
		Boolean bHaveRejected = false, bHaveNotSubmitted = false, bHaveUnderReview = false, bHaveApproved = false;
		
		// Loop through the time entries in our list until we find the date requested....
		for(CTimeEntry tTimeEntry : m_lstTimeEntries) {
			// If we found a time entry with a date that matches the date we're looking for then...
			dtCurrent = tTimeEntry.getDate(); 
			if(dtCurrent.equals(dtDate)) {
				// Grab the time entry's status
				sStatus = tTimeEntry.getStatus();
				
				// If the Status is Rejected and we have not yet set that status then...
				if(sStatus.equals(Constants.STATUS_REJECTED) && !bHaveRejected) { 
					dsStatuses.setStatusRejected(); 
					bHaveRejected = true;
				}
				// If the Status is Non-Submitted and we have not yet set that status then...
				else if(sStatus.equals(Constants.STATUS_NOT_SUBMITTED) && !bHaveNotSubmitted) {
					dsStatuses.setStatusNotSubmitted();
					bHaveNotSubmitted = true;
				}
				// If the Status is Under Review and we have not yet set that status then...
				else if(sStatus.equals(Constants.STATUS_UNDER_REVIEW) && !bHaveUnderReview) {
					dsStatuses.setStatusUnderReview();
					bHaveUnderReview = true;
				}
				// If the Status is Approved and we have not yet set that status then...
				else if(sStatus.equals(Constants.STATUS_APPROVED) && !bHaveApproved) {
					dsStatuses.setStatusApproved();
					bHaveApproved = true;
				} // End if
								
				// If we've found time entries with all four possible statuses then exit the loop now (no sense looping longer if we've gathered what we've needed)
				if(bHaveRejected && bHaveNotSubmitted && bHaveUnderReview && bHaveApproved) { break; }				
			}
			// We've passed the date we're looking for. No need to keep looping.
			else if(dtCurrent.after(dtDate)) { break; }		
		} // End of the for(CTimeEntry tTimeEntry : m_lstTimeEntries) loop.
	}
	
	
	@Override
	public String toString() { return (m_sLastName + ", " + m_sFirstName); }
}
