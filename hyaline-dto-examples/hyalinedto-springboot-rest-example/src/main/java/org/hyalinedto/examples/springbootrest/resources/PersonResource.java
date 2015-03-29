package org.hyalinedto.examples.springbootrest.resources;

import org.hyalinedto.api.DTO;
import org.hyalinedto.api.Hyaline;
import org.hyalinedto.examples.springbootrest.domain.Address;
import org.hyalinedto.examples.springbootrest.domain.Person;
import org.hyalinedto.exception.HyalineException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@RestController
public class PersonResource {

	final Person john;
	
	public PersonResource() {
		john = new Person();
		john.setFirstName("John");
		john.setLastName("Lennon");

		Address addr = new Address();
		addr.setStreet("Abbey Road");
		addr.setNumber(123);
		addr.setZipcode("NW8 9AX");
		addr.setCity("London");
		addr.setCountry("UK");

		john.setAddress(addr);
	}

	@RequestMapping("/")
	public Person getPlain() {
		return john;
	}

	@RequestMapping("/noaddress")
	public Person getWithoutAdress() throws HyalineException {
		return Hyaline.dtoFromScratch(john, new DTO() {

			@JsonIgnore
			private Address address;

		});
	}

	@RequestMapping("/renamed")
	public Person getRenamed() throws HyalineException {
		return Hyaline.dtoFromScratch(john, new DTO() {

			@JsonProperty("name")
			private String firstName;

			@JsonProperty("surname")
			private String lastName;

		});
	}

	@RequestMapping("/inlined")
	public Person getInlined() throws HyalineException {
		
		return Hyaline.dtoFromClass(john, new DTO() {
			
			@JsonIgnore
			private Address address;

			@JsonProperty("address")
			private String stringAddress = john.getAddress().toString();
		});
	}
}
