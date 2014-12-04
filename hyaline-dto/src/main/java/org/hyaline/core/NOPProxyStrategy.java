package org.hyaline.core;

import java.lang.reflect.Method;

public class NOPProxyStrategy implements ProxyStrategy {

	@Override
	public String getBeforeTargetCode(Object target, Method method) {
		return "";
	}

	@Override
	public String getAfterTargetCode(Object target, Method method) {
		return "";
	}

}
