package com.dovico.timeentrystatus;

public class Constants {
	// Key for the Consumer Secret field on the Settings dialog (if you leave this blank, the Consumer Secret field will be visible allowing the user to specify their
	// own Consumer Secret - If you want to keep the Consumer Secret from the users of this app, place it here and the CPanel_SettingsEx class will tell the settings
	// pane not to show the field)
	public static String CONSUMER_SECRET_API_TOKEN = "";
	
	// Keys for storing/caching data
	public static String PREFS_KEY_CONSUMER_SECRET = "ConsumerSecret";
	public static String PREFS_KEY_USER_TOKEN = "UserToken";
	public static String PREFS_KEY_EMPLOYEE_LIST_MODE = "EmployeeListMode";
	
	public static String PREV_PAGE_URI = "PrevPageURI";
	public static String NEXT_PAGE_URI = "NextPageURI";
	
	public static String URI_NOT_AVAILABLE = "N/A"; 
	
	// The REST API returns and expects dates in this format
	public static String XML_DATE_FORMAT = "yyyy-MM-dd";
	
	// Our display formats/values
	public static String MONTH_ABBREVIATION_FORMAT = "MMM";
	public static String DAY_OF_MONTH_FORMAT = "d";
	public static String TODAY_STRING_FORMAT = "'Today ('MMMM d, yyyy')'";
	
	public static int DATE_RANGE_DAYS = 30;
	public static int RULER_HEIGHT = 95;
	public static int DAY_QUARTER_HEIGHT = 16;
	
	// Status values for time entries (will be received in the following format from the REST API)
	public static String STATUS_NOT_SUBMITTED = "N";
	public static String STATUS_UNDER_REVIEW = "U";
	public static String STATUS_REJECTED = "R";
	public static String STATUS_APPROVED = "A";
	
	// The API version that we are targeting
	public static String API_VERSION_TARGETED = "2";	
}
