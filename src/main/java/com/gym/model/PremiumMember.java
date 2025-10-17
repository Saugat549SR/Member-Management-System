package com.gym.model;
import java.time.LocalDate;
import java.time.YearMonth;

public class PremiumMember extends Member {
	// Indicates whether the member has access to spa facilities
	private boolean spa;
    // Additional fee charged for premium services like spa
	private double premiumService;

    //Constructor for creating a new premium membe
	 public PremiumMember(String firstName, String lastName, int age, LocalDate joinDate, 
			double baseFee,boolean spa, double premiumService) {
		super(firstName, lastName, age, joinDate, baseFee);
		this.spa=spa;
		this.premiumService=spa ? premiumService:0;
	}
	 
     //Constructor for creating a premium member with an existing ID
	 public PremiumMember(String memberId, String firstName, String lastName, int age,
              LocalDate joinDate, double baseFee,
              boolean spa, double premiumService) {
    super(memberId, firstName, lastName, age, joinDate, baseFee);
    this.spa = spa;
    this.premiumService = spa ? premiumService : 0;
}
	
	//Calculates the monthly fee for this premium member
	@Override
	public double calculateMonthlyFee(YearMonth month) {
		double fee=getBaseFee()+premiumService;
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

    //Returns a formatted string containing the member’s basic and premium details
	  @Override
	    public String toString() {
	        return "[Premium Member] " + getSummary() +
	               " | Spa Access: " + (spa ? "Yes" : "No") +
	               " | Premium Fee: $" + String.format("%.2f", premiumService);
	    }

	    // Getters
	    public boolean hasSpaAccess() {
	        return spa;
	    }

	    public double getPremiumServiceFee() {
	        return premiumService;
	    }

}