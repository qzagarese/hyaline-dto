package org.hyaline.core;

import java.lang.reflect.Method;

public class HyalineProxyImplementationStrategy extends
		DoNothingImplementationStrategy {

	@Override
	public String getImplementationFor(Object target, Method method) {
		StringBuffer buffer = new StringBuffer();
		super.writeModifiersReturnTypeAndName(method, buffer);
		super.writeParameters(method, buffer);
		super.writeDeclaredExceptions(method, buffer);
		writeBody(method, buffer);
		return buffer.toString();
	}

	@Override
	public String getImplementationBodyFor(Object target, Method method) {
		StringBuffer buffer = new StringBuffer();
		writeBody(method, buffer);
		return buffer.toString();
	}

	protected void writeBody(Method method, StringBuffer buffer) {
		if (method.getName().equals("getTarget")) {
			buffer.append("{return target;}");
		} else {
			super.writeBody(method, buffer);
		}
	}

}
