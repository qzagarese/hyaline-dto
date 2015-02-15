package org.hyalinedto.api;

import org.hyalinedto.core.HyalineProxyFactory;
import org.hyalinedto.core.exception.CannotInstantiateProxyException;
import org.hyalinedto.core.proxy.ReflectionBasedHyalineProxyFactory;
import org.hyalinedto.exception.DTODefinitionException;
import org.hyalinedto.exception.HyalineException;

/**
 * The Class Hyaline. It is the entry point for this library and allows you to
 * create dynamic DTOs by in-lining your configuration.
 * 
 * @author Quirino Zagarese
 * 
 */
public class Hyaline {

	/** The proxy factory. */
	private static HyalineProxyFactory proxyFactory = new ReflectionBasedHyalineProxyFactory();

	/**
	 * It lets you create a new DTO from scratch. This means that any
	 * configuration for JAXB, Jackson or whatever serialization framework you
	 * are using on your entity T will be ignored. The only configuration that
	 * will be used is the one you are defining in this invocation.
	 *
	 * @param <T>
	 *            the generic type
	 * @param entity
	 *            the entity you need a DTO for.
	 * @param dtoTemplate
	 *            the DTO configuration passed as an anonymous class.
	 * @return a proxy that can be assigned to entity, that holds the values of
	 *         entity and configured according to config
	 * 
	 * @throws HyalineException
	 */
	public static <T> T dtoFromScratch(T entity, DTO dtoTemplate)
			throws HyalineException {
		return dtoFromScratch(entity, dtoTemplate, "Hyaline$Proxy$" +System.currentTimeMillis());
	}

	/**
	 * It lets you create a new DTO from scratch. This means that any
	 * configuration for JAXB, Jackson or whatever serialization framework you
	 * are using on your entity T will be ignored. The only configuration that
	 * will be used is the one you are defining in this invocation.
	 *
	 * @param <T>
	 *            the generic type
	 * @param entity
	 *            the entity you need a DTO for.
	 * @param dtoTemplate
	 *            the DTO configuration passed as an anonymous class.
	 * 
	 * @param proxyClassName
	 *            the name you want to assign to newly generated class
	 * 
	 * 
	 * @return a proxy that can be assigned to entity, that holds the values of
	 *         entity and configured according to config
	 * 
	 * @throws HyalineException
	 */
	public static <T> T dtoFromScratch(T entity, DTO dtoTemplate,
			String proxyClassName) throws HyalineException {
		try {
			return createDTO(entity, dtoTemplate, true, proxyClassName);
		} catch (CannotInstantiateProxyException | DTODefinitionException e) {
			e.printStackTrace();
			throw new HyalineException();
		}
	}

	/**
	 * It lets you create a new DTO starting from the configuration of your
	 * entity. This means that any configuration for JAXB, Jackson or whatever
	 * serialization framework you are using on your entity T will be kept.
	 * However, if you insert a configuration on a field that exists also in
	 * your class, this configuration will override the one in your class.
	 *
	 * @param <T>
	 *            the generic type
	 * @param entity
	 *            the entity you need a DTO for.
	 * @param dtoTemplate
	 *            the DTO configuration passed as an anonymous class.
	 * @return a proxy that can be assigned to entity, that holds the values of
	 *         entity and configured according to config
	 * 
	 * @throws HyalineException
	 */
	public static <T> T dtoFromClass(T entity, DTO dtoTemplate)
			throws HyalineException {
		return dtoFromClass(entity, dtoTemplate,
				"Hyaline$Proxy$" + System.currentTimeMillis());
	}

	/**
	 * It lets you create a new DTO starting from the configuration of your
	 * entity. This means that any configuration for JAXB, Jackson or whatever
	 * serialization framework you are using on your entity T will be kept.
	 * However, if you insert a configuration on a field that exists also in
	 * your class, this configuration will override the one in your class.
	 *
	 * @param <T>
	 *            the generic type
	 * @param entity
	 *            the entity you need a DTO for.
	 * @param dtoTemplate
	 *            the DTO configuration passed as an anonymous class.
	 * 
	 * @param proxyClassName
	 *            the name you want to assign to newly generated class
	 * 
	 * @return a proxy that can be assigned to entity, that holds the values of
	 *         entity and configured according to config
	 * 
	 * @throws HyalineException
	 */
	public static <T> T dtoFromClass(T entity, DTO dtoTemplate,
			String proxyClassName) throws HyalineException {
		try {
			return createDTO(entity, dtoTemplate, false, proxyClassName);
		} catch (CannotInstantiateProxyException | DTODefinitionException e) {
			e.printStackTrace();
			throw new HyalineException();
		}
	}

	@SuppressWarnings("unchecked")
	private static <T> T createDTO(T entity, DTO dtoTemplate,
			boolean resetAnnotations, String proxyClassName)
			throws CannotInstantiateProxyException, DTODefinitionException {
		return (T) proxyFactory.create(entity, dtoTemplate, resetAnnotations,
				proxyClassName);
	}

	/**
	 * Gets the proxy factory.
	 *
	 * @return the proxy factory
	 */
	public static HyalineProxyFactory getProxyFactory() {
		return proxyFactory;
	}

	/**
	 * Sets the proxy factory.
	 *
	 * @param proxyFactory
	 *            the new proxy factory
	 */
	public static void setProxyFactory(HyalineProxyFactory proxyFactory) {
		Hyaline.proxyFactory = proxyFactory;
	}

}
