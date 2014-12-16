package org.hyaline.core;

import org.hyaline.api.DTO;
import org.hyaline.core.exception.CannotInstantiateProxyException;
import org.hyaline.exception.DTODefinitionException;

public interface HyalineProxyFactory {

	<T> Object createFromScratch(T entity, DTO config)
			throws CannotInstantiateProxyException, DTODefinitionException;

	<T> Object createFromClass(T entity, DTO config)
			throws CannotInstantiateProxyException, DTODefinitionException;

	ClassBuilder getClassBuilder();

	void setClassBuilder(ClassBuilder builder);

	ProxyStrategy getProxyStrategy();

	void setProxyStrategy(ProxyStrategy proxyStrategy);

	InterfaceImplementationStrategy getInterfaceImplementationStrategy();

	void setInterfaceImplementationStrategy(
			InterfaceImplementationStrategy interfaceImplementationStrategy);
	
	ClassRepository<String, Class<?>> getClassRepository();
	
	void setClassRepository(ClassRepository<String, Class<?>> classRepository);

}
