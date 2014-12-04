package org.hyaline.test;

import org.hyaline.api.DTO;
import org.hyaline.api.Hyaline;
import org.hyaline.exception.HyalineException;
import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SimpleTest {

	@Test
	public void testDtoFromScratch() throws HyalineException {
		Person entity = new Person();
		entity = Hyaline.dtoFromScratch(entity, new DTO() {

			@JsonProperty
			String firstName;
			
			
			
		});

		Hyaline.dtoFromScratch(entity, new DTO() {

		});
	}

}
