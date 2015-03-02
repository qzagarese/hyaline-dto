package org.hyalinedto.test.domainclasses;

import java.util.Arrays;
import java.util.List;

public class Person {

	private String firstName;

	private String lastName;

	private List<Double> scores = Arrays.asList(new Double[] { 1.2, 9.45,
			0.432, 4.67 });
	
	private String[] colors = {"Green", "White", "Red"};

	private Address address;

	public List<Double> getScores() {
		return scores;
	}

	public void setScores(List<Double> scores) {
		this.scores = scores;
	}

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

	public String[] getColors() {
		return colors;
	}

	public void setColors(String[] colors) {
		this.colors = colors;
	}

}