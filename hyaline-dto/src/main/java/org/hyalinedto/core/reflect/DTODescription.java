package org.hyalinedto.core.reflect;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DTODescription {

	private Class<?> type;

	private List<Class<?>> implementedInterfaces = new ArrayList<Class<?>>();

	private List<Annotation> annotations = new ArrayList<Annotation>();

	private Map<String, FieldDescription> fields = new HashMap<String, FieldDescription>();

	private Map<String, MethodDescription> methods = new HashMap<String, MethodDescription>();

	public DTODescription(Class<?> type) {
		this.type = type;
	}

	public Class<?> getType() {
		return type;
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
		methods.put(method.getMethod().getName(), method);
	}

	public List<Class<?>> getImplementedInterfaces() {
		return implementedInterfaces;
	}

	public void setImplementedInterfaces(List<Class<?>> implementedInterfaces) {
		this.implementedInterfaces = implementedInterfaces;
	}

	public void addImplementedInterface(Class<?> type) {
		if (implementedInterfaces == null) {
			implementedInterfaces = new ArrayList<Class<?>>();
		}
		if (type.isInterface()) {
			implementedInterfaces.add(type);
		}
	}

	public FieldDescription getField(String name) {
		return fields.get(name);
	}

	public MethodDescription getMethod(String name) {
		return methods.get(name);
	}
}
