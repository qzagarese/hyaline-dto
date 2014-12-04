package org.hyaline.core.proxy;

import java.lang.reflect.Method;

import org.hyaline.core.ProxyStrategy;

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
