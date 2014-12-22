package org.hyalinedto.core;

import org.hyalinedto.api.DTO;
import org.hyalinedto.core.exception.CannotInstantiateProxyException;
import org.hyalinedto.exception.DTODefinitionException;

public interface HyalineProxyFactory {

	<T> Object createFromScratch(T entity, DTO config)
			throws CannotInstantiateProxyException, DTODefinitionException;

	<T> Object createFromClass(T entity, DTO config)
			throws CannotInstantiateProxyException, DTODefinitionException;

	ClassBuilder getClassBuilder();

	void setClassBuilder(ClassBuilder builder);

	ClassRepository<String, Class<?>> getClassRepository();
	
	void setClassRepository(ClassRepository<String, Class<?>> classRepository);

}
