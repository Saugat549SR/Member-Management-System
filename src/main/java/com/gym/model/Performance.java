package com.gym.model;

import java.time.YearMonth;
import java.util.Objects;
import java.util.UUID;

public class Performance {
	
	//declaring instances
	private String memberId;
	private YearMonth month;
	private boolean goalAchieved;
	private Integer rating;
	private String notes;
	
	
	// Validates and creates a performance entry
	public Performance(String memberId,YearMonth month,boolean goalAchieved,Integer rating,String notes) {
		
		// Ensure a valid member ID is stored
		if (memberId != null && !memberId.isBlank()) {
	        this.memberId = memberId;
	    } else {
	        System.out.println("Member ID cannot be null or blank. Setting to default ID.");
	        this.memberId = "UNKNOWN-" + UUID.randomUUID().toString().substring(0, 8);
	    }
		if(month !=null) {
			this.month=month;
		}
		else {
			System.out.println("Month can't be null. Setting to current Month");
			this.month=YearMonth.now();
		}
		if(rating>=1 && rating<=5) {
			this.rating=rating;
		}
		else {
			System.out.println("Invalid Rating! It should be between 1 to 5. Setting to 3 for default");
			this.rating=3;
		}
		
		if(notes !=null) {
			this.notes=notes;
		}
		else {
			// Save notes only if provided
			this.notes="";
		}
		this.goalAchieved = goalAchieved;
		
	}
	// getter  methods
	public String getMemberId() {
		return memberId;
	}
	
	public YearMonth getMonth() {
		return month;
	}
	
	public boolean getGoalAchieved() {
		return goalAchieved;
	}
	public Integer getRating() {
		return rating;
	}
	public String getNotes() {
		return notes;
	}

	//Summary string shown in logs or console table displays
	public String toString() {
		return ("Month: "+month+", Goal Achieved: "+goalAchieved+", Rating: "+rating+", Notes: "+notes);
	}
	
	//Returns true if performance is considered good
	public boolean isPositivePerformance() {
		return (goalAchieved || rating>=4);
	}
	
	
	// Two performance entries are considered the same record if memberId & month match
	@Override
	public boolean equals(Object obj) {
	    if (this == obj) return true;
	    if (obj == null || getClass() != obj.getClass()) return false;
	    Performance that = (Performance) obj;
	    return Objects.equals(memberId, that.memberId) &&
	           Objects.equals(month, that.month);
	}

	@Override
	public int hashCode() {
	    return Objects.hash(memberId, month);
	}

}
