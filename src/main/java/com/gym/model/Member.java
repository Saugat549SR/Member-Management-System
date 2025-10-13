package com.gym.model;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

//Abstract base class representing a gym member that stores Abstract base class representing a gym member

public abstract class Member {
	
	// Basic profile data kept private to ensure encapsulation and protect member state
	private final String memberId;
	private final String firstName;
	private final String lastName;
	private final int age;
	private final LocalDate joinDate;
	private final double baseFee;
	private final List<Performance> performanceHistory;
	
	//Constructor for creating a new member with auto-generated memberId
	public Member(String firstName,String lastName,int age,LocalDate joinDate, double baseFee) {
		this.memberId="M" + UUID.randomUUID().toString().substring(0, 8);
		this.firstName = Objects.requireNonNull(firstName, "firstName");
        this.lastName  = Objects.requireNonNull(lastName,  "lastName");
        this.age       = age;
        this.joinDate  = Objects.requireNonNull(joinDate,  "joinDate");
        this.baseFee   = baseFee;
        this.performanceHistory = new ArrayList<>();
		
	}
	
	
	// Secondary constructor used when loading an existing member from csv file
	protected Member(String memberId,
            String firstName,
            String lastName,
            int age,
            LocalDate joinDate,
            double baseFee) {
this.memberId  = Objects.requireNonNull(memberId, "memberId");
this.firstName = Objects.requireNonNull(firstName, "firstName");
this.lastName  = Objects.requireNonNull(lastName,  "lastName");
this.age       = age;
this.joinDate  = Objects.requireNonNull(joinDate,  "joinDate");
this.baseFee   = baseFee;
this.performanceHistory = new ArrayList<>();
}
	
	//implementing by subclasses to define fee calculation per month
	public abstract double calculateMonthlyFee(YearMonth month);
	
	
	//performance record only if it matches this member and doesn't already exist for that month
	 public boolean addPerformance(Performance performance) {
	        if (performance == null) return false;

	     // Ensuring performance belongs to this member
	        if (!memberId.equals(performance.getMemberId())) {
	            
	            return false;
	        }

	        // Check for duplicate month entry
	        boolean alreadyExists = performanceHistory.stream()
	                .anyMatch(p -> p.getMonth().equals(performance.getMonth()));

	        if (alreadyExists) return false;

	        performanceHistory.add(performance);
	        return true;
	    }
	
	
	//Adds or replaces performance for a given month
	 public boolean addOrReplacePerformance(Performance performance) {
	        if (performance == null) return false;
	        if (!memberId.equals(performance.getMemberId())) return false;

	        for (int i = 0; i < performanceHistory.size(); i++) {
	            if (performanceHistory.get(i).getMonth().equals(performance.getMonth())) {
	                performanceHistory.set(i, performance);
	                return true;
	            }
	        }
	        performanceHistory.add(performance);
	        return true;
	    }

	    //Returns performance of a specific month if present
	    public Performance getPerformance(YearMonth month) {
	        for (Performance p : performanceHistory) {
	            if (p.getMonth().equals(month)) return p;
	        }
	        return null;
	    }

	   //Removes performance entry for the given month
	    public boolean removePerformance(YearMonth month) {
	        return performanceHistory.removeIf(p -> p.getMonth().equals(month));
	    }

	   //Gets the most recent performance entry
	    public Performance getLatestPerformance() {
	        if (performanceHistory.isEmpty()) return null;
	        return performanceHistory.get(performanceHistory.size() - 1);
	    }

	   //Calculates average rating across all performance records
	    public double getAverageRating() {
	        if (performanceHistory.isEmpty()) return 0.0;
	        int total = 0;
	        for (Performance p : performanceHistory) {
	            total += p.getRating();
	        }
	        return total / (double) performanceHistory.size();
	    }

	   //Returns read-only list of performance history
	    public List<Performance> getPerformanceHistory() {
	        return Collections.unmodifiableList(performanceHistory);
	    }

	   //Quick summary string for listing members in console
	    public String getSummary() {
	        return "ID: " + memberId +
	               " | " + firstName + " " + lastName +
	               " | Joined: " + joinDate +
	               " | Base Fee: $" + String.format("%.2f", baseFee);
	    }

	    // getters method
	    public String getMemberId()     { return memberId; }
	    public String getFirstName()    { return firstName; }
	    public String getLastName()     { return lastName; }
	    public int getAge()             { return age; }
	    public LocalDate getJoinDate()  { return joinDate; }
	    public double getBaseFee()      { return baseFee; }
	}
