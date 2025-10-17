package com.gym.repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.gym.model.Member;
import com.gym.model.Performance;

public class MemberRepository {
	private final List<Member> members=new ArrayList<>();
	
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
	public boolean deleteMember(String memberId) {
	    for (int i = 0; i < members.size(); i++) {
	        if (members.get(i).getMemberId().equalsIgnoreCase(memberId)) {
	            members.remove(i);
	            return true; // Successfully removed
	        }
	    }
	    return false; // Member not found
	}
	 public Member findMemberById(String memberId) {
	        for (Member m : members) {
	            if (m.getMemberId().equalsIgnoreCase(memberId)) {
	                return m;
	            }
	        }
	        return null; // Not found
	    }
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
	
//	 public boolean updateMember(String memberId, double newBaseFee) {
//		    for (int i = 0; i < members.size(); i++) {
//		        Member m = members.get(i);
//		        if (m.getMemberId().equalsIgnoreCase(memberId)) {
//		            Member updated;
//
//		            if (m instanceof RegularMember) {
//		                updated = new RegularMember(
//		                        m.getFirstName(), m.getLastName(), m.getAge(), m.getJoinDate(), newBaseFee);
//
//		            } else if (m instanceof PersonalTrainingMember) {
//		                PersonalTrainingMember pt = (PersonalTrainingMember) m;
//		                updated = new PersonalTrainingMember(
//		                        m.getFirstName(), m.getLastName(), m.getAge(), m.getJoinDate(),
//		                        newBaseFee, pt.getSessionsPerMonth(), pt.getFeePerSession());
//
//		            } else if (m instanceof PremiumMember) {
//		                PremiumMember pm = (PremiumMember) m;
//		                updated = new PremiumMember(
//		                        m.getFirstName(), m.getLastName(), m.getAge(), m.getJoinDate(),
//		                        newBaseFee, pm.hasSpaAccess(), pm.getPremiumServiceFee());
//
//		            } else {
//		                return false; // unknown subtype
//		            }
//
//		            // copy performance history over
//		            for (Performance p : m.getPerformanceHistory()) {
//		                updated.addOrReplacePerformance(p);
//		            }
//
//		            members.set(i, updated);
//		            return true;
//		        }
//		    }
//		    return false; // not found
//		}
//	 
	 
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
	  public void replaceAllMembers(List<Member> newMembers) {
	        members.clear();
	        members.addAll(newMembers);
	    }
	  public boolean isEmpty() {
	        return members.isEmpty();
	    }

}
