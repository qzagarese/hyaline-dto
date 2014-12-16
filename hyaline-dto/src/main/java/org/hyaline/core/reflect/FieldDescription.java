package org.hyaline.core.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class FieldDescription {

	private Field field;
	
	private boolean injectable;
	
	private boolean fromTemplate = false;
	
	private List<Annotation> annotations;

	public Field getField() {
		return field;
	}

	public void setField(Field field) {
		this.field = field;
	}

	public boolean isInjectable() {
		return injectable;
	}

	public void setInjectable(boolean injectable) {
		this.injectable = injectable;
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

	public boolean isFromTemplate() {
		return fromTemplate;
	}

	public void setFromTemplate(boolean fromTemplate) {
		this.fromTemplate = fromTemplate;
	}
	
}
