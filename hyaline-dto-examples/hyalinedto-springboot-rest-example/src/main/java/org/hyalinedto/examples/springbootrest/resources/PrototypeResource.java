package org.hyalinedto.examples.springbootrest.resources;

import static org.hyalinedto.api.Hyaline.from;
import static org.hyalinedto.api.Hyaline.proto;

import org.hyalinedto.api.$;
import org.hyalinedto.examples.springbootrest.domain.Address;
import org.hyalinedto.examples.springbootrest.domain.Person;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonProperty;

@RestController
public class PrototypeResource {

	final Person john;

	public PrototypeResource() {
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
	
	
	@RequestMapping("/extended")
	public Person getPersonProto() throws InstantiationException {
	
		return from(john).proto(new $(){
			
			@JsonProperty("stringified") String addressAsString = john.getAddress().toString();
			
		}).build();
		
	}
	
	
	@RequestMapping("/proto")
	@SuppressWarnings("unused")
	public Object getProto() throws InstantiationException {
		Object proto = proto(new $() {

			String greeting = "Hello World!";

			Object[] protos = new Object[] {

			"Create", proto(new $() {

				Object any = proto(new $() {

					Object structure = proto(new $() {

						String you = "want";

					}).build();

				}).build();

			}).build(), "!!!!!!!!!!!" };

		}).build();

		return proto;
	}
}
