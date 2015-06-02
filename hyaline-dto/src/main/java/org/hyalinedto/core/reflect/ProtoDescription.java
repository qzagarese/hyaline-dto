package org.hyalinedto.core.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProtoDescription {

	private Class<?> superType;

	private Class<?> protoType;

	private List<Annotation> annotations = new ArrayList<Annotation>();

	private Map<String, FieldDescription> fields = new HashMap<String, FieldDescription>();

	private Map<String, MethodDescription> methods = new HashMap<String, MethodDescription>();

	public ProtoDescription(Class<?> superType, Class<?> protoType) {
		this.superType = superType;
		this.protoType = protoType;
	}

	public Class<?> getSuperType() {
		return superType;
	}

	public Class<?> getProtoType() {
		return protoType;
	}

	public List<Annotation> getAnnotations() {
		return annotations;
	}

	public void setAnnotations(List<Annotation> annotations) {
		this.annotations = annotations;
	}

	public void addAnnotation(Annotation annotation) {
		annotations.add(annotation);
	}

	public Map<String, FieldDescription> getFields() {
		return fields;
	}

	public void setFields(Map<String, FieldDescription> fields) {
		this.fields = fields;
	}

	public Map<String, MethodDescription> getMethods() {
		return methods;
	}

	public void setMethods(Map<String, MethodDescription> methods) {
		this.methods = methods;
	}

	public void putField(FieldDescription field) {
		fields.put(field.getField().getName(), field);
	}

	public void putMethod(MethodDescription method) {
		methods.put(getSignature(method.getMethod()), method);
	}

	public FieldDescription getField(String name) {
		return fields.get(name);
	}

	public MethodDescription getMethod(Method m) {
		return methods.get(getSignature(m));
	}

	private String getSignature(Method m) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(m.getReturnType().getCanonicalName());
		buffer.append("_");
		buffer.append(m.getName()).append("(");
		for (Class<?> c : m.getParameterTypes()) {
			buffer.append(c.getCanonicalName()).append("_");
		}
		buffer.append(")");
		return buffer.toString();
	}

}
