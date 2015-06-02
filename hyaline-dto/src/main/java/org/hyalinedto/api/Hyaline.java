package org.hyalinedto.api;

import org.hyalinedto.core.HyalineProtoFactory;
import org.hyalinedto.core.exception.CannotInstantiateProxyException;
import org.hyalinedto.core.proxy.ReflectionBasedHyalineProtoFactory;
import org.hyalinedto.exception.HyalineException;
import org.hyalinedto.exception.ProtoDefinitionException;

/**
 * The Class Hyaline. It is the entry point for this library and allows you to
 * create dynamic DTOs by in-lining your configuration.
 * 
 * @author Quirino Zagarese
 * 
 */
public class Hyaline {

	/** The proxy factory. */
	private static HyalineProtoFactory proxyFactory = new ReflectionBasedHyalineProtoFactory();

	/**
	 * It lets you create a new DTO from scratch.
	 *
	 *
	 * @param dtoTemplate
	 *            the DTO template passed as an anonymous class.
	 * 
	 * @return a new object holding the same instance variables declared in the
	 *         dtoTemplate
	 * 
	 * @throws HyalineException
	 *             if the dynamic type could be created.
	 */
	public static Object dtoFromScratch($ dtoTemplate) throws HyalineException {
		return dtoFromScratch(new Object(), dtoTemplate, "Hyaline$Proto$" + System.currentTimeMillis());
	}

	/**
	 * It lets you create a new DTO from scratch.
	 *
	 *
	 * @param dtoTemplate
	 *            the DTO template passed as an anonymous class.
	 * 
	 * @param proxyClassName
	 *            the name you want to assign to newly generated class
	 * 
	 * @return a new object holding the same instance variables declared in the
	 *         dtoTemplate
	 * 
	 * @throws HyalineException
	 *             if the dynamic type could be created.
	 */
	public static Object dtoFromScratch($ dtoTemplate, String proxyClassName) throws HyalineException {
		return dtoFromScratch(new Object(), dtoTemplate, proxyClassName);
	}

	/**
	 * It lets you create a new DTO from scratch. This means that any annotation
	 * from JAXB, Jackson or whatever serialization framework you are using on
	 * your entity T will be ignored. The only annotation-based configuration
	 * that will be used is the one you are defining in this invocation.
	 *
	 * @param <T>
	 *            the generic type
	 * @param entity
	 *            the entity you are going proxy.
	 * @param dtoTemplate
	 *            the DTO template passed as an anonymous class.
	 * @return a proxy that extends the type of entity, holding the same
	 *         instance variables values as entity and configured according to
	 *         dtoTemplate
	 * 
	 * @throws HyalineException
	 *             if the dynamic type could be created.
	 */
	public static <T> T dtoFromScratch(T entity, $ dtoTemplate) throws HyalineException {
		return dtoFromScratch(entity, dtoTemplate, "Hyaline$Proxy$" + System.currentTimeMillis());
	}

	/**
	 * It lets you create a new DTO from scratch. This means that any annotation
	 * for JAXB, Jackson or whatever serialization framework you are using on
	 * your entity T will be ignored. The only annotation-based configuration
	 * that will be used is the one you are defining in this invocation.
	 *
	 * @param <T>
	 *            the generic type
	 * @param entity
	 *            the entity you are going proxy.
	 * @param dtoTemplate
	 *            the DTO template passed as an anonymous class.
	 * 
	 * @param proxyClassName
	 *            the name you want to assign to newly generated class
	 * 
	 * 
	 * @return a proxy that extends the type of entity, holding the same
	 *         instance variables values as entity and configured according to
	 *         dtoTemplate
	 * 
	 * @throws HyalineException
	 *             if the dynamic type could be created.
	 */
	public static <T> T dtoFromScratch(T entity, $ dtoTemplate, String proxyClassName) throws HyalineException {
		try {
			return createDTO(entity, dtoTemplate, true, proxyClassName);
		} catch (CannotInstantiateProxyException | ProtoDefinitionException e) {
			e.printStackTrace();
			throw new HyalineException();
		}
	}

	/**
	 * It lets you create a new DTO starting from the annotation-based
	 * configuration of your entity. This means that any annotation-based
	 * configuration for JAXB, Jackson or whatever serialization framework you
	 * are using on your entity T will be kept. However, if you insert an
	 * annotation on a field that exists also in your class, this annotation
	 * will override the one in your class.
	 *
	 *
	 * @param <T>
	 *            the generic type
	 * @param entity
	 *            the entity you are going proxy.
	 * @param dtoTemplate
	 *            the DTO template passed as an anonymous class.
	 * 
	 * 
	 * @return a proxy that extends the type of entity, holding the same
	 *         instance variables values as entity and configured according to
	 *         dtoTemplate
	 * 
	 * @throws HyalineException
	 *             if the dynamic type could be created.
	 */
	public static <T> T dtoFromClass(T entity, $ dtoTemplate) throws HyalineException {
		return dtoFromClass(entity, dtoTemplate, "Hyaline$Proxy$" + System.currentTimeMillis());
	}

	/**
	 * It lets you create a new DTO starting from the annotation-based
	 * configuration of your entity. This means that any annotation-based
	 * configuration for JAXB, Jackson or whatever serialization framework you
	 * are using on your entity T will be kept. However, if you insert an
	 * annotation on a field that exists also in your class, this annotation
	 * will override the one in your class.
	 *
	 *
	 * @param <T>
	 *            the generic type
	 * @param entity
	 *            the entity you are going proxy.
	 * @param dtoTemplate
	 *            the DTO template passed as an anonymous class.
	 * 
	 * @param proxyClassName
	 *            the name you want to assign to newly generated class
	 * 
	 * 
	 * @return a proxy that extends the type of entity, holding the same
	 *         instance variables values as entity and configured according to
	 *         dtoTemplate
	 * 
	 * @throws HyalineException
	 *             if the dynamic type could be created.
	 */
	public static <T> T dtoFromClass(T entity, $ dtoTemplate, String proxyClassName) throws HyalineException {
		try {
			return createDTO(entity, dtoTemplate, false, proxyClassName);
		} catch (CannotInstantiateProxyException | ProtoDefinitionException e) {
			e.printStackTrace();
			throw new HyalineException();
		}
	}

	@SuppressWarnings("unchecked")
	private static <T> T createDTO(T entity, $ dtoTemplate, boolean resetAnnotations, String proxyClassName)
			throws CannotInstantiateProxyException, ProtoDefinitionException {
		return (T) proxyFactory.create(entity, dtoTemplate, resetAnnotations, proxyClassName);
	}

	/**
	 * Gets the proxy factory.
	 *
	 * @return the proxy factory
	 */
	public static HyalineProtoFactory getProxyFactory() {
		return proxyFactory;
	}

	/**
	 * Sets the proxy factory.
	 *
	 * @param proxyFactory
	 *            the new proxy factory
	 */
	public static void setProxyFactory(HyalineProtoFactory proxyFactory) {
		Hyaline.proxyFactory = proxyFactory;
	}
	
	
	public static <T> ProtoBuilder<T> proto($ dto){
		return new ProtoBuilder<T>(proxyFactory).proto(dto);
	}
	
	public static <T> ProtoBuilder<T> from(T t) {
		return new ProtoBuilder<T>(proxyFactory).from(t);
	}
	
	public static <T> ProtoBuilder<T> withAnnotations(){
		return new ProtoBuilder<T>(proxyFactory).withAnnotations();
	}
	
	public static <T> ProtoBuilder<T> withName(String typeName) {
		return new ProtoBuilder<T>(proxyFactory).withName(typeName);
	}
	
	

}
