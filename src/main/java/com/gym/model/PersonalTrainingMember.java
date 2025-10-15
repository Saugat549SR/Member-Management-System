package com.gym.model;

import java.time.LocalDate;
import java.time.YearMonth;

public class PersonalTrainingMember extends Member {
	
    // Number of personal training sessions the member has each month
	private Integer sessionsPerMonth;
    // Fee charged per training session
	private double feePerSession;

    //Constructor for creating a new personal training member
	 public PersonalTrainingMember(String firstName, String lastName, int age, LocalDate joinDate, double baseFee,Integer sessionsPerMonth,double feePerSession) {
		super(firstName, lastName, age, joinDate, baseFee);
		this.sessionsPerMonth=sessionsPerMonth;
		this.feePerSession=feePerSession;
		
	}
    //Constructor for creating a member with an existing ID
	 public PersonalTrainingMember(String memberId, String firstName, String lastName, int age,
             LocalDate joinDate, double baseFee,
             int sessionsPerMonth, double feePerSession) {
super(memberId, firstName, lastName, age, joinDate, baseFee);
this.sessionsPerMonth = sessionsPerMonth;
this.feePerSession = feePerSession;
}
	
    //Calculates the monthly fee for this member
	@Override
	public double calculateMonthlyFee(YearMonth month) {
		double fee=getBaseFee()+(sessionsPerMonth*feePerSession);
		Performance performance=getPerformance(month);
		if(performance !=null) {
			if(performance.getGoalAchieved()) {
				fee-=fee*0.10; //10 % discount on fee
			}else if(performance.getRating()<=2) {
				fee+=10; //penalty on low performance
			}
		}
		return Math.max(fee,0); //making sure that fee won't be in negative
		
	}

    //Returns a formatted string with the member’s details
	 @Override
	    public String toString() {
	        return "[Personal Training Member] " + getSummary() +
	               " | Sessions/Month: " + sessionsPerMonth +
	               " | Fee/Session: $" + feePerSession;
	    }

    // Getter for sessions per month
	 public int getSessionsPerMonth() {
	        return sessionsPerMonth;
	    }
    //Getter for fee per session
	    public double getFeePerSession() {
	        return feePerSession;
	    }
}
