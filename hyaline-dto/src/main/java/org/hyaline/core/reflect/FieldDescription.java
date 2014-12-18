package org.hyaline.core.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class FieldDescription {

	private Field field;
	
	private boolean initialized;

	private boolean fromTemplate = false;
	
	private List<Annotation> annotations = new ArrayList<Annotation>();

	public Field getField() {
		return field;
	}

	public void setField(Field field) {
		this.field = field;
	}

	public boolean isInitialized() {
		return initialized;
	}

	public void setInitialized(boolean initialized) {
		this.initialized = initialized;
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

	public boolean isFromTemplate() {
		return fromTemplate;
	}

	public void setFromTemplate(boolean fromTemplate) {
		this.fromTemplate = fromTemplate;
	}
	
}
