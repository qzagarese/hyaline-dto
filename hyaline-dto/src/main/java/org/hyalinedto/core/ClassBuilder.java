package org.hyalinedto.core;

import org.hyalinedto.core.exception.CannotBuildClassException;
import org.hyalinedto.core.reflect.ProtoDescription;

public interface ClassBuilder {

	Class<?> buildClass(ProtoDescription description, String proxyClassName) throws CannotBuildClassException;
	
}
