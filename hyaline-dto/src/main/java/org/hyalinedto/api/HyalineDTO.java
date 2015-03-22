package org.hyalinedto.api;

/**
 * 
 * Every DTO generated with Hyaline implements this interface. This allows read/write access to dynamically created fields 
 * 
 * 
 * @author Quirino Zagarese
 *
 */
public interface HyalineDTO {

	void setAttribute(String name, Object value);
	
	Object getAttribute(String name);
	
}
