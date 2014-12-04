package org.hyaline.core;

import org.hyaline.core.exception.CannotBuildClassException;
import org.hyaline.core.reflect.DTODescription;

public interface ClassBuilder {

	Class<?> buildClass(DTODescription description, ProxyStrategy strategy, InterfaceImplementationStrategy interfaceImplementationStrategy) throws CannotBuildClassException;
	
}
