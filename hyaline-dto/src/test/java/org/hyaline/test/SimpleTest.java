package org.hyaline.test;

import javax.xml.bind.annotation.XmlAttribute;

import org.hyaline.api.DTO;
import org.hyaline.api.Hyaline;
import org.hyaline.exception.HyalineException;
import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SimpleTest {

	@Test
	public void testDtoFromScratch() throws HyalineException {
		final Person entity = new Person();
		final Person p2 = new Person();
		entity.setFirstName("hello");
		Person dto = Hyaline.dtoFromScratch(entity, new DTO() {
			
			Template template = new Template();
			
			class Template {
							
				@JsonProperty
				String firstName = entity.getFirstName().trim();

				String p2Name = p2.getFirstName();
			}
		});

		// Hyaline.dtoFromScratch(entity, new DTO() {
		//
		// abstract class Template {
		//
		// private String firstAndLast = entity.getFirstName()
		// + entity.getLastName();
		//
		// abstract String getFirstAndLast();
		// }
		//
		// });
	}

}
