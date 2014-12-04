package org.hyaline.core;

import java.lang.reflect.Method;

public interface ProxyStrategy {

	String getBeforeTargetCode(Object target, Method method);
	
	String getAfterTargetCode(Object target, Method method);
	
}
