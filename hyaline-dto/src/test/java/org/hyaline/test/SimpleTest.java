package org.hyaline.test;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import javax.xml.bind.annotation.XmlAttribute;

import org.hyaline.api.DTO;
import org.hyaline.api.Hyaline;
import org.hyaline.exception.HyalineException;
import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

public class SimpleTest {

	@Test
	public void testDtoFromScratch() throws HyalineException {
		final Person entity = new Person();
		final Person p2 = new Person();
		entity.setFirstName("hello");
		Object dto = Hyaline.dtoFromScratch(entity, new DTO() {

			Template template = new Template();

			@JsonRootName("person")
//			@JsonInclude(Include.NON_NULL)
			class Template {

				@JsonProperty
				String firstName = entity.getFirstName().trim();

				@JsonIgnore
				String p2Name = p2.getFirstName();
			}
		});

//		ObjectMapper mapper = new ObjectMapper();
//		try {
//			mapper.writeValue(System.out, dto);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		Class<?> clazz = dto.getClass();
		for (Annotation annotation : clazz.getDeclaredAnnotations()) {
			System.out.println(annotation.annotationType().getName());
		}
		for (Field f : clazz.getDeclaredFields()) {
			System.out.println(f.getName() + ":");
			for (Annotation annotation : f.getDeclaredAnnotations()) {
				System.out.println("\t-" + annotation.annotationType().getName());
			}
		}

		
	}
	
	
	@Test
	public void testDtoFromClass() throws HyalineException {
		final Person entity = new Person();
		final Person p2 = new Person();
		entity.setFirstName("hello");
		Object dto = Hyaline.dtoFromClass(entity, new DTO() {

			Template template = new Template();

			@JsonRootName("person")
			class Template {

				@JsonProperty
				String firstName = entity.getFirstName().trim();

				@JsonIgnore
				String p2Name = p2.getFirstName();
			}
		});

		Class<?> clazz = dto.getClass();
		for (Annotation annotation : clazz.getDeclaredAnnotations()) {
			System.out.println(annotation.annotationType().getName());
		}
		for (Field f : clazz.getDeclaredFields()) {
			System.out.println(f.getName() + ":");
			for (Annotation annotation : f.getDeclaredAnnotations()) {
				System.out.println("\t-" + annotation.annotationType().getName());
			}
		}

		
	}

}
