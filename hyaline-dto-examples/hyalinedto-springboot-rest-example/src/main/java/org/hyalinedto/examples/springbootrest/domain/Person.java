package org.hyalinedto.examples.springbootrest.domain;

import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Person {

    private String firstName;

    private String lastName;

    private List<Double> scores = Arrays.asList(new Double[]{1.2, 9.45, 0.432, 4.67});
    
   


	public List<Double> getScores() {
		return scores;
	}

	public void setScores(List<Double> scores) {
		this.scores = scores;
	}

    private Address address;

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public Address getAddress() {
		return address;
	}

	public void setAddress(Address address) {
		this.address = address;
	}

    
    
}   