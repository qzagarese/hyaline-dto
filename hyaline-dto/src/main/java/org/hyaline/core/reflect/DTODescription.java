package org.hyaline.core.reflect;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

public class DTODescription {

	private final Object target;
	
	private final Class<?> type;

	private List<Class<?>> implementedInterfaces;

	private List<Annotation> annotations;

	private List<FieldDescription> fields;

	private List<MethodDescription> methods;

	public DTODescription(Object target) {
		this.target = target;
		this.type = target.getClass();
	}

	public Class<?> getType() {
		return type;
	}

	public Object getTarget() {
		return target;
	}

	public List<Annotation> getAnnotations() {
		return annotations;
	}

	public void setAnnotations(List<Annotation> annotations) {
		this.annotations = annotations;
	}

	public void addAnnotation(Annotation annotation) {
		if (annotations == null) {
			annotations = new ArrayList<Annotation>();
		}
		annotations.add(annotation);
	}

	public List<FieldDescription> getFields() {
		return fields;
	}

	public void setFields(List<FieldDescription> fields) {
		this.fields = fields;
	}

	public void addField(FieldDescription field) {
		if (fields == null) {
			fields = new ArrayList<FieldDescription>();
		}
		fields.add(field);
	}

	public List<MethodDescription> getMethods() {
		return methods;
	}

	public void setMethods(List<MethodDescription> methods) {
		this.methods = methods;
	}

	public void addMethod(MethodDescription method) {
		if (methods == null) {
			methods = new ArrayList<MethodDescription>();
		}
		methods.add(method);
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

}
