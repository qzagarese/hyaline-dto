package org.hyalinedto.api;

/**
 * 
 * Every DTO generated with Hyaline implements this interface. This allows
 * read/write access to dynamically created fields. You should use this method
 * only for those fields that do not exist in the proxied instance. For other
 * fields, you should rely on getter methods. Using getAttribute and
 * setAttribute on fields that already exist in the proxied instance will lead
 * to unexpected results. 
 * 
 * <p>
 * 
 * In Hyaline, invocations of setters propagate the value from the proxy (the
 * dto) to the superclass. Analogously, invocations of getters propagate the
 * value from the superclass to the proxy (the dto). 
 * 
 * <p>
 * 
 * If you set the value of
 * field x by invoking {@code setAttribute("x", "foo")} and then get it by invoking
 * {@code getX()}, you are possibly going to get a different value (e.g. the value of x
 * in the superclass instance). 
 * 
 * <p>
 * 
 * Moreover, the invocation of getX() will
 * overwrite the value of x in the proxy and set it to the value it is set in the
 * superclass instance. Hence, you will not be able to retrieve it, even if,
 * afterwards, you invoke {@code getAttribute("x")}. 
 * 
 * <p>
 * 
 * The best practice is: if the superclass declares field x, use its accessor methods (e.g. {@code getX()} and {@code setX()}).
 * If the superclass does not declare field x, then use {@code getAttribute()} and {@code setAttribute()}.
 * 
 * 
 * 
 * 
 * @author Quirino Zagarese
 *
 */
public interface HyalineDTO {

	/**
	 * Sets the field named "name" to value "value". This will not overwrite the
	 * value of the proxied object (the instance you pass as first parameter to
	 * {@link org.hyalinedto.api.Hyaline} calls). You should use this method
	 * only for those fields that do not exist in the proxied instance. For
	 * other fields, you should rely on setter methods.
	 * 
	 * @param name
	 * @param value
	 */
	void setAttribute(String name, Object value);

	/**
	 * Returns the value of the field named "name" or null if there is no such
	 * field. You should use this method only for those fields that do not exist
	 * in the proxied instance. For other fields, you should rely on getter
	 * methods.
	 * 
	 * @param name
	 * @return
	 */
	Object getAttribute(String name);

}
