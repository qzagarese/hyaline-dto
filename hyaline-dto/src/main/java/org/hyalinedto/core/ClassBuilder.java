package org.hyalinedto.core;

import org.hyalinedto.core.exception.CannotBuildClassException;
import org.hyalinedto.core.reflect.DTODescription;

public interface ClassBuilder {

	Class<?> buildClass(DTODescription description, String proxyClassName) throws CannotBuildClassException;
	
}
