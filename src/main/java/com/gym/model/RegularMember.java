package com.gym.model;

import java.time.LocalDate;
import java.time.YearMonth;

public class RegularMember extends Member {

	public RegularMember(String firstName, String lastName, int age, LocalDate joinDate, double baseFee) {
		super(firstName, lastName, age, joinDate, baseFee);
		
	}
	
	public RegularMember(String memberId, String firstName, String lastName, int age,
            LocalDate joinDate, double baseFee) {
  super(memberId, firstName, lastName, age, joinDate, baseFee);
}
	
	
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
