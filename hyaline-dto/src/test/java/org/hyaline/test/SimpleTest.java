package org.hyaline.test;

import org.hyaline.api.DTO;
import org.hyaline.api.Hyaline;
import org.hyaline.exception.HyalineException;
import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SimpleTest {

	@Test
	public void testDtoFromScratch() throws HyalineException {
		final Person entity = new Person();
		entity.setFirstName("hello");
		Person dto = Hyaline.dtoFromScratch(entity, new DTO() {

			class Template {
			
				@JsonProperty
				String firstName = entity.getFirstName().trim();

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
