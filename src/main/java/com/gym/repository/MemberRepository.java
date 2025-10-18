package com.gym.repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.gym.model.Member;
import com.gym.model.Performance;

public class MemberRepository {
	// stores all gym members in a list
	private final List<Member> members=new ArrayList<>();
	
	// Adds a new members to the list if not null and not already existing 
	public boolean addMember(Member member) {
		if(member==null) {
			return false;
		}
		if(findMemberById(member.getMemberId()) !=null) {
			return false;
		}
		members.add(member);
		return true;
	}
	//Delets a member by their ID
	public boolean deleteMember(String memberId) {
	    for (int i = 0; i < members.size(); i++) {
	        if (members.get(i).getMemberId().equalsIgnoreCase(memberId)) {
	            members.remove(i);
	            return true; // Successfully removed
	        }
	    }
	    return false; // Member not found
	}
	// Finds a member by ID
	 public Member findMemberById(String memberId) {
	        for (Member m : members) {
	            if (m.getMemberId().equalsIgnoreCase(memberId)) {
	                return m;
	            }
	        }
	        return null; // Not found
	    }
		//Finds all members whose full name contains the given search text
	 public List<Member> findMembersByName(String name) {
	        List<Member> results = new ArrayList<>();
	        for (Member m : members) {
	            String fullName = (m.getFirstName() + " " + m.getLastName()).toLowerCase();
	            if (fullName.contains(name.toLowerCase())) {
	                results.add(m);
	            }
	        }
	        return results;
	    }
	

	 
	 
	 public boolean replaceMember(String memberId, Member updated) {
		    for (int i = 0; i < members.size(); i++) {
		        Member old = members.get(i);
		        if (old.getMemberId().equalsIgnoreCase(memberId)) {
		            // If caller forgot to copy history, keep it
		            if (updated.getPerformanceHistory().isEmpty()) {
		                for (Performance p : old.getPerformanceHistory()) {
		                    updated.addOrReplacePerformance(p);
		                }
		            }
		            members.set(i, updated);
		            return true;
		        }
		    }
		    return false;
		}
	 
	 
	 
	 
	 
	 
	 
	 
	 
	  public List<Member> getAllMembers() {
	        return Collections.unmodifiableList(members);
	    }
		// Replaces all exsiting members with a new list
	  public void replaceAllMembers(List<Member> newMembers) {
	        members.clear();
	        members.addAll(newMembers);
	    }
		// checks if the repository is empty
	  public boolean isEmpty() {
	        return members.isEmpty();
	    }

}
