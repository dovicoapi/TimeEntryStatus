package com.dovico.timeentrystatus;

import com.dovico.timeentrystatus.CDayStatuses.Status;
import java.util.*;
import java.text.*;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;


public class CPanel_TimeEntry extends JPanel {
	private static final long serialVersionUID = 1L;
	
	protected Date m_dtDateRangeStart = null;
	protected Date m_dtDateRangeEnd = null;
	protected boolean m_bIndicateProcessing = true; // Processing by default so that we indicate processing when the form is loading
	protected CEmployee m_eCurrentEmployee = null;
	
	// Overloaded constructor (when the form is initially displayed no data will be present so only the ruler will show)
	public CPanel_TimeEntry(Date dtDateRangeStart, Date dtDateRangeEnd) {
		m_dtDateRangeStart = dtDateRangeStart;
		m_dtDateRangeEnd = dtDateRangeEnd;
	}
	
	
	// Clears the status area and displays the processing indicator
	public void clearStatusesAndIndicateProcessing() {
		m_eCurrentEmployee = null;
		indicateProcessing(true);
	}
	
	
	// Causes a new employee's statuses to be displayed
	public void displayEmployeeStatuses(CEmployee eEmployee) {
		m_eCurrentEmployee = eEmployee;
		indicateProcessing(false);
	}
		
	
	// Show/hide the processing indicator
	public void indicateProcessing(boolean bIndicateProcessing) { 
		m_bIndicateProcessing = bIndicateProcessing;
		
		// They say you should use 'repaint()' but I don't find it fires properly due to processing and I don't want to get into async threads so I'm forcing the
		// repaint now.
		paintImmediately(super.getVisibleRect());
	}
				
	
	// The actual drawing engine of the JPanel
	public void paintComponent(Graphics g) {
		// Get the drawing surface size of our panel		
		Rectangle rcMaxSize = super.getVisibleRect();
		Graphics2D g2d = (Graphics2D)g;
		
		// Paint the panel's background white
		g2d.setColor(Color.white);
		g2d.fillRect(0, 0, rcMaxSize.width, rcMaxSize.height);

		// Create the fonts to use for text
		Font fArial11 = new Font("Arial", Font.PLAIN, 11);
		Font fArial09 = new Font("Arial", Font.PLAIN, 9);
		
		// Ruler calculations
		int iRulerHeight = Constants.RULER_HEIGHT;
		int iRulerBorderThickness = 2;
		int iRulerTop = ((rcMaxSize.height - iRulerHeight) / 2); // Center vertically
		
		// Day (cell) calculations (Date Range is 30 days 'before' today's date which is 31 days in total. We also don't want the right edge of the time line stuck 
		// against the right edge of the panel so we give our panel an extra day's width to accommodate)
		int iDayWidth = (rcMaxSize.width / (Constants.DATE_RANGE_DAYS + 2)); // The width permitted for each day's cell
		int iDayHeight = (Constants.DAY_QUARTER_HEIGHT * 4);// (if multiple entries for a day there could be Non-Submitted, Approved, Under Review, and Rejected time all on the same day. the vertical split will allow us to correctly reflect the state of each day)
		int iDayTop = ((rcMaxSize.height - iDayHeight) / 2); // Center vertically
		
		
		// We want ruler to have a darker blue border and then the time line portion itself to be a lighter blue
		// 
		// Paint the time line portion first
		g2d.setColor(new Color(153, 217, 234));
		g2d.fillRect(0, iRulerTop, rcMaxSize.width, iRulerHeight);
		//
		// Paint the time line border
		Color colBorder = new Color(112, 146, 190);
		g2d.setColor(colBorder);
		g2d.setStroke(new BasicStroke(2));
		g2d.drawRect(-iRulerBorderThickness, iRulerTop, (rcMaxSize.width + (iRulerBorderThickness * 2)), iRulerHeight);// x 2 for width because we started at (x - border thickness)
		
		// Put the thin stroke back in
		g2d.setStroke(new BasicStroke(1));		
						
		
		// Calendar object to help us determine if the date in the following loop is the first day of the month or not
		Calendar calFirstDayOfMonth = Calendar.getInstance();
		
		// Create our Calendar object that will let us step through the days
		Calendar calCurrentDate = Calendar.getInstance();
		calCurrentDate.setTime(m_dtDateRangeStart);
		int iCurrentDayIndex = 0, iX = 0, iY = 0, iMonthX = 0, iMonthRightX = 0;
		
		// Loop from the Start to the End of the date range...
		Date dtCurrent = calCurrentDate.getTime();
		while(dtCurrent.before(m_dtDateRangeEnd) || dtCurrent.equals(m_dtDateRangeEnd)) {
			// Determine what the first day of the current month is (we increment dtCurrent by 1 day each loop so there's a chance that our 30 day range actually spans
			// multiple months. if we start a new month, we want the new month indicated on the ruler)
			calFirstDayOfMonth.setTime(dtCurrent);
			calFirstDayOfMonth.set(Calendar.DAY_OF_MONTH, 1);
			
			
			// If this is the first time through the loop then draw the current month's abbreviation (WARNING: The X/Y of the drawString method specifies the
			// lower-left corner of the text! I was expecting it to be the top-left!)
			if(iCurrentDayIndex == 0) { iMonthRightX = drawMonthAbbreviation(g2d, fArial11, (iX + 4), (iRulerTop - 4), dtCurrent); } 
			
			// If the current date is the first day of the month then...
			if(dtCurrent.equals(calFirstDayOfMonth.getTime())) {
				// Change the color to that of the border and then draw the month vertical separator
				g2d.setColor(colBorder);
				g2d.drawLine(iX, iRulerTop, iX, (iRulerTop + iRulerHeight));
				
				// If we're not the first time through the loop then...(if this is the first time through the loop, the month abbreviation was already drawn above)
				if(iCurrentDayIndex > 0) {
					// Determine the X position of the text. If the position we want to use will overlap an existing abbreviation then adjust the X position to 
					// accommodate the previous abbreviation. 
					iMonthX = (iX + 4);
					if((iX + 4) <= iMonthRightX) { iMonthX = (iMonthRightX + 4); }
					
					// Draw the current month's abbreviation (get the X position of the right-edge of the text so that we can make sure the next abbreviation does not
					// overlap it)					
					iMonthRightX = drawMonthAbbreviation(g2d, fArial11, iMonthX, (iRulerTop - 4), dtCurrent);
				} // End if(iCurrentDayIndex > 0)
			} // End if(dtCurrent.equals(calFirstDayOfMonth.getTime()))
			

			// Only try to draw the status if we have an Employee's status to draw
			if(m_eCurrentEmployee != null) { 
				// Draw an opaque background over the ruler for the current day if today is a non-working day for the current employee
				drawNonWorkingDayBackground(g2d, iX, iRulerTop, iDayWidth, iRulerHeight, calCurrentDate.get(Calendar.DAY_OF_WEEK));
				
				// Draw the Status indicators (is there Rejected, Non-Submitted, Under Review, or Approved time for the current date)
				drawQuarterCellStatuses(g2d, iX, iDayTop, iDayWidth, dtCurrent); 
			} // End if(m_eCurrentEmployee != null)
			
			
			// Draw the day of the month text (I had it here for testing purposes and it grew on me so I left it in)
			iY = iRulerTop;
			drawDayOfMonth(g2d, fArial09, iX, iDayWidth, iY, dtCurrent);

			
			// Increment the date by 1 day
			iCurrentDayIndex++;
			calCurrentDate.add(Calendar.DATE, 1);
			dtCurrent = calCurrentDate.getTime();
			
			// Determine the X position for the current day's data (we increment at the end of the loop rather than the beginning so that following the loop, the
			// value can be used to indicate the current date on the ruler at the proper position - the code following the loop will not have to increment this value
			// again)
			iX = (iCurrentDayIndex * iDayWidth);
		} // End of the while(dtCurrent.before(m_dtDateRangeEnd)) loop.
		

		// Draw the vertical line that indicates where today's date is on the panel
		iY = (iRulerTop + iRulerHeight + 5);
		g2d.setColor(Color.black);
		g2d.drawLine(iX, (iRulerTop + (iRulerHeight / 2)) , iX, iY);
		
		// Draw Today's date (e.g. 'Today (July 15, 2010)')
		drawTodayString(g2d, fArial11, iX, iY, m_dtDateRangeEnd);
		
		
		// Draw the 'Processing' text if we've been asked for it (we do this at the end because I want the ruler drawn below the processing indicator)
		drawProcessing(g2d, rcMaxSize.width, rcMaxSize.height);		
	}
		
	
	// Helper that draws the Month's abbreviation
	protected int drawMonthAbbreviation(Graphics2D g2d, Font fFont, int iBottomLeftX, int iBottomLeftY, Date dtDate) {
		// Get the Month's abbreviation from the date object passed in
		Format fFormatter = new SimpleDateFormat(Constants.MONTH_ABBREVIATION_FORMAT);
		String sMonthAbbreviation = fFormatter.format(dtDate);
		
		// Set the font and color to use for the current text 
		g2d.setFont(fFont);
		g2d.setColor(Color.black);
				
		// Determine the size of the text that will be drawn
		FontMetrics fmMetrics = g2d.getFontMetrics();
		Rectangle2D rcBounds = fmMetrics.getStringBounds(sMonthAbbreviation, g2d);
		
		// Draw the current month's abbreviation (WARNING: The X/Y of the drawString method specifies the lower-left corner of the text! I was expecting it to
		// be the top-left!)
		g2d.setColor(Color.black);
		g2d.drawString(sMonthAbbreviation, iBottomLeftX, iBottomLeftY);
				
		// Return the X position of the right edge of the text
		return (iBottomLeftX + (int)rcBounds.getWidth());
	}
	
	
	// Helper to draw the day of the month text
	protected void drawDayOfMonth(Graphics2D g2d, Font fFont, int iX, int iMaxWidth, int iY, Date dtDate) {
		// Get the Day of the Month string
		Format fFormatter = new SimpleDateFormat(Constants.DAY_OF_MONTH_FORMAT );		
		String sDayOfMonth = fFormatter.format(dtDate);
				
		// Set the font and color to use for the current text 
		g2d.setFont(fFont);
		g2d.setColor(Color.gray);
		
		// Determine the size of the text that will be drawn
		FontMetrics fmMetrics = g2d.getFontMetrics();
		Rectangle2D rcBounds = fmMetrics.getStringBounds(sDayOfMonth, g2d);
	
		// Draw the text horizontally centered
		g2d.drawString(sDayOfMonth, (iX + ((iMaxWidth - (int)rcBounds.getWidth()) / 2)), (iY + (int)rcBounds.getHeight()));
	}
	
	
	// Helper that draw's today's date
	protected void drawTodayString(Graphics2D g2d, Font fFont, int iX, int iY, Date dtDate) {
		// Grab the text for today's date		
		String sToday = getTodayString(m_dtDateRangeEnd);
		
		// Set the font and color to use for the current text 
		g2d.setFont(fFont);
		g2d.setColor(Color.black);
		
		// Determine the size of the text and based on that size position the text on the panel appropriately		
		FontMetrics fmMetrics = g2d.getFontMetrics();
		Rectangle2D rcBounds = fmMetrics.getStringBounds(sToday, g2d);
		g2d.drawString(sToday, (iX - (int)rcBounds.getWidth()), (iY + (int)rcBounds.getHeight()));
	}
	
	
	// Returns the date as: 'Today (July 15, 2010)'
	protected String getTodayString(Date dtDate) { return new SimpleDateFormat(Constants.TODAY_STRING_FORMAT).format(dtDate); }
	

	// Draw an opaque background over the ruler for the current day if today is a non-working day for the current employee
	protected void drawNonWorkingDayBackground(Graphics2D g2d, int iX, int iY, int iWidth, int iHeight, int iDayOfTheWeek) {
		// If the current day of the week is a non-working day for this employee then...
		if(m_eCurrentEmployee.isDateANonWorkingDay(iDayOfTheWeek)){
			// Draw an opaque background over the ruler for the current day
			g2d.setColor(new Color(112, 146, 190, 53));
			g2d.fillRect(iX, iY, iWidth, iHeight);
		} // End if(m_eCurrentEmployee.isDateANonWorkingDay(iDayOfTheWeek))
	}
		
	
	// Draws the requested quarter's status for the day passed in
	protected void drawQuarterCellStatuses(Graphics2D g2d, int iX, int iDayTop, int iMaxWidth, Date dtDate) {
		// Get the statuses for the requested date
		CDayStatuses dsStatuses = new CDayStatuses(); 
		m_eCurrentEmployee.getStatusesForDate(dtDate, dsStatuses);
		
				
		// If we have a Rejected status for the current date then draw the status indicator
		int iQuarter = 0;
		Status iStatus = dsStatuses.getStatusRejected(); 
		if(iStatus != Status.NotSet) { drawStatus(g2d, iX, (iDayTop + (iQuarter * Constants.DAY_QUARTER_HEIGHT)), iMaxWidth, getColorForStatus(iStatus)); }
				
		// If we have a Not-Submitted status for the current date then draw the status indicator
		iQuarter = 1;
		iStatus = dsStatuses.getStatusNotSubmitted();
		if(iStatus != Status.NotSet) { drawStatus(g2d, iX, (iDayTop + (iQuarter * Constants.DAY_QUARTER_HEIGHT)), iMaxWidth, getColorForStatus(iStatus)); }
		
		// If we have an Under Review status for the current date then draw the status indicator
		iQuarter = 2;
		iStatus = dsStatuses.getStatusUnderReview();
		if(iStatus != Status.NotSet) { drawStatus(g2d, iX, (iDayTop + (iQuarter * Constants.DAY_QUARTER_HEIGHT)), iMaxWidth, getColorForStatus(iStatus)); }
		
		// If we have an Approved status for the current date then draw the status indicator
		iQuarter = 3;
		iStatus = dsStatuses.getStatusApproved();
		if(iStatus != Status.NotSet) { drawStatus(g2d, iX, (iDayTop + (iQuarter * Constants.DAY_QUARTER_HEIGHT)), iMaxWidth, getColorForStatus(iStatus)); }
	}
	
	
	// Draws the Status indicator 
	protected void drawStatus(Graphics2D g2d, int iX, int iTop, int iMaxWidth, Color colStatus) {
		// Our triangle will be 10 pixels wide and 14 pixels tall
		int iXStart = (iX + ((iMaxWidth - 10) / 2));
		int iYStart = (iTop + ((Constants.DAY_QUARTER_HEIGHT - 14) / 2));
		
		// Create our triangle object
		Polygon pTriangle = new Polygon();
		pTriangle.addPoint(iXStart, iYStart);
		pTriangle.addPoint((iXStart + 3), iYStart);
		pTriangle.addPoint((iXStart + 9), (iYStart + 6));
		pTriangle.addPoint((iXStart + 9), (iYStart + 7));
		pTriangle.addPoint((iXStart + 3), (iYStart + 13));
		pTriangle.addPoint(iXStart, (iYStart + 13));
		
		// Fill the triangle's background
		g2d.setColor(colStatus);
		g2d.fillPolygon(pTriangle);
		
		// Get a darker version of the current color and then draw just the border of the triangle
		g2d.setColor(colStatus.darker());
		g2d.drawPolygon(pTriangle);
	}
	
	
	// Returns the color needed for the Status specified
	protected Color getColorForStatus(CDayStatuses.Status iStatus) {
		// Based on the status passed in, return the proper color
		switch(iStatus) {
		case Rejected:
			return new Color(255, 76, 76); 
		case NotSubmitted:
			return Color.black;
		case UnderReview:
			return new Color(255, 227, 117); 
		case Approved:
			return new Color(97, 169, 250);
		default:
			return new Color(0, 0, 0, 0); // Transparent (the day likely does not have any time entries)
		}
	}
	
	
	// This panel doubles as a 'Processing' indicator and this function handles the work of displaying the 'Processing' text 
	protected void drawProcessing(Graphics2D g2d, int iMaxWidth, int iMaxHeight) {
		// If we're to indicate processing then...
		if(m_bIndicateProcessing) {
			// Draw an opaque background over the ruler
			g2d.setColor(new Color(0, 0, 0, 0.6F));
			g2d.fillRect(0, 0, iMaxWidth,iMaxHeight);
			
			// Set up the Processing font with a color of White			
			Font fProcessing = new Font("Arial", Font.BOLD, 26);
			g2d.setFont(fProcessing);
			g2d.setColor(Color.white);
			
			// Create our string and get it's dimensions
			String sProcessing = "Processing...One moment please.";
			FontMetrics fmMetrics = g2d.getFontMetrics();
			Rectangle2D rcBounds = fmMetrics.getStringBounds(sProcessing, g2d);
			
			// Calculate the X and Y positions so that the text is centered horizontally and vertically
			int iX = ((iMaxWidth - (int)rcBounds.getWidth()) / 2);
			int iY = (iMaxHeight - (int)rcBounds.getHeight()) / 2  + fmMetrics.getAscent();
						
			// Draw the text
			g2d.drawString(sProcessing, iX, iY);
		} // End if(m_bIndicateProcessing)
	}
}
