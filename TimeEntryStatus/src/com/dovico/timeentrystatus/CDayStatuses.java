package com.dovico.timeentrystatus;


public class CDayStatuses {
	public static enum Status { Rejected, NotSubmitted, UnderReview, Approved, NotSet }
		
	
	private Status m_iStatusRejected = Status.NotSet;
	public void setStatusRejected() { m_iStatusRejected = Status.Rejected; }
	public Status getStatusRejected() { return m_iStatusRejected; }
	
	private Status m_iStatusNotSubmitted = Status.NotSet;
	public void setStatusNotSubmitted() { m_iStatusNotSubmitted = Status.NotSubmitted; }
	public Status getStatusNotSubmitted() { return m_iStatusNotSubmitted; }
	
	private Status m_iStatusUnderReview = Status.NotSet;
	public void setStatusUnderReview() { m_iStatusUnderReview = Status.UnderReview; }
	public Status getStatusUnderReview() { return m_iStatusUnderReview; }
	
	private Status m_iStatusApproved = Status.NotSet;
	public void setStatusApproved() { m_iStatusApproved = Status.Approved; }
	public Status getStatusApproved() { return m_iStatusApproved; }
}
