package org.hyalinedto.test.domainclasses;

import org.hyalinedto.test.annotations.TestFieldAnnotation;


public class Person {

	private String firstName;

	private String lastName;

	private String[] colors = { "Green", "White", "Red" };

	@TestFieldAnnotation
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

	public String[] getColors() {
		return colors;
	}

	public void setColors(String[] colors) {
		this.colors = colors;
	}

}