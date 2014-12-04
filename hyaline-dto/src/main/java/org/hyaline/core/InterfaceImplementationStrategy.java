package org.hyaline.core;

import java.lang.reflect.Method;

public interface InterfaceImplementationStrategy {

	String getImplementationFor(Object target, Method method);
	
}
