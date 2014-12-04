package org.hyaline.core;

import java.lang.reflect.Method;

public class HyalineProxyImplementationStrategy extends DoNothingImplementationStrategy {

	@Override
	public String getImplementationFor(Object target, Method method) {
		StringBuffer buffer = new StringBuffer();
		super.writeModifiersAndName(method, buffer);
		super.writeParameters(method, buffer);
		super.writeDeclaredExceptions(method, buffer);
		if(method.getName().equals("getTarget")){
			buffer.append("{return target;}");
		} else {
			super.writeBody(method, buffer);
		}
		return buffer.toString();
	}

	
	
}
