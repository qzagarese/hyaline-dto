package org.hyaline.core.build;

import org.hyaline.core.ClassBuilder;
import org.hyaline.core.InterfaceImplementationStrategy;
import org.hyaline.core.ProxyStrategy;
import org.hyaline.core.exception.CannotBuildClassException;
import org.hyaline.core.reflect.DTODescription;

public class JavassistBasedClassBuilder implements ClassBuilder {

	@Override
	public Class<?> buildClass(DTODescription description,
			ProxyStrategy strategy,
			InterfaceImplementationStrategy interfaceImplementationStrategy)
			throws CannotBuildClassException {
		// TODO Auto-generated method stub
		return null;
	}

}
