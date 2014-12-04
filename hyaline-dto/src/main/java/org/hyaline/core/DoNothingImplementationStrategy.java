package org.hyaline.core;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;

public class DoNothingImplementationStrategy implements
		InterfaceImplementationStrategy {

	@Override
	public String getImplementationFor(Object target, Method method) {
		StringBuffer buffer = new StringBuffer();
		
		writeModifiersAndName(method, buffer);

		// write parameters
		writeParameters(method, buffer);

		// write declared exceptions
		writeDeclaredExceptions(method, buffer);

		// write default body
		writeBody(method, buffer);

		return buffer.toString();
	}

	protected void writeModifiersAndName(Method method, StringBuffer buffer) {
		buffer.append(Modifier.toString(method.getModifiers())).append(" ")
				.append(method.getName());
	}

	protected void writeBody(Method method, StringBuffer buffer) {
		buffer.append("{");
		Class<?> returnType = method.getReturnType();
		if (!returnType.isPrimitive() && (!returnType.equals(Void.class))) {
			buffer.append("return null;");
		} else if (returnType.isPrimitive()) {
			switch (returnType.getName()) {

			case "Z":
				buffer.append("return false;");
				break;
			default:
				buffer.append("return 0;");
				break;
			}
		} 

		buffer.append("}");
	}

	protected void writeDeclaredExceptions(Method method, StringBuffer buffer) {
		Class<?>[] exceptionTypes = method.getExceptionTypes();
		if (exceptionTypes != null) {
			buffer.append("throws ");
			for (int i = 0; i < exceptionTypes.length; i++) {
				buffer.append(exceptionTypes[i].getCanonicalName()).append(" ");
				if (i < exceptionTypes.length - 1) {
					buffer.append(", ");
				}
			}
		}
	}

	protected void writeParameters(Method method, StringBuffer buffer) {
		buffer.append("(");
		for (int i = 0; i < method.getParameterCount(); i++) {
			Parameter parameter = method.getParameters()[i];
			buffer.append(parameter.getType().getCanonicalName()).append(" ")
					.append(parameter.getName());
			if (i < method.getParameterCount() - 1) {
				buffer.append(", ");
			}
		}
		buffer.append(") ");
	}

}
