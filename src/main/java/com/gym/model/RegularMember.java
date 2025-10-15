package com.gym.model;

import java.time.LocalDate;
import java.time.YearMonth;

public class RegularMember extends Member {

	// Constructor for creating a new RegularMember without specifying a member ID
	public RegularMember(String firstName, String lastName, int age, LocalDate joinDate, double baseFee) {
		super(firstName, lastName, age, joinDate, baseFee);
		
	}
	// Constructor for creating a RegularMember with an existing member ID
	public RegularMember(String memberId, String firstName, String lastName, int age,
            LocalDate joinDate, double baseFee) {
  super(memberId, firstName, lastName, age, joinDate, baseFee);
}
	
	//Calculates the monthly fee for the given month based on the member’s performance
	@Override
	public double calculateMonthlyFee(YearMonth month) {
		double fee=getBaseFee();
		Performance performance=getPerformance(month);
		
		if(performance !=null) {
			if(performance.getGoalAchieved()) {
				fee-=fee*0.10; //10 % discount on fee
			}else if(performance.getRating()<=2) {
				fee+=10;//penalty on low performance
			}
		}
		return Math.max(fee,0); //making sure that fee won't be in negative
	}
	
	@Override
	public String toString() {
		return "[Regular Member]" + getSummary();
	}

}
