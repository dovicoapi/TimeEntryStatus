package com.dovico.timeentrystatus;

import java.text.*;
import java.util.Date;

public class CTimeEntry {
	protected String m_sID = ""; // Could be a Guid or a Long. I don't need the actual value right now so I left it as a string
	protected Date m_dtDate = null;
	protected String m_sStatus = "";
	protected String m_sRejectedReason = "";
	
	// Overloaded constructor
	public CTimeEntry(String sID, String sDate, String sStatus, String sRejectedReason) {
		m_sID = sID;
		m_sStatus = sStatus;
		m_sRejectedReason = sRejectedReason;
		
		Format fFormatter = new SimpleDateFormat(Constants.XML_DATE_FORMAT);
		try { m_dtDate = (Date)fFormatter.parseObject(sDate); } 
		catch (ParseException e) { e.printStackTrace(); }
	}
	
	
	// Returns the current object's date
	public Date getDate() { return m_dtDate; } 	
	
	public String getStatus() { return m_sStatus; }
}
