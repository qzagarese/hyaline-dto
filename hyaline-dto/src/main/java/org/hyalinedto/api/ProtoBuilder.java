package org.hyalinedto.api;

import org.hyalinedto.core.HyalineProtoFactory;
import org.hyalinedto.core.exception.CannotInstantiateProxyException;
import org.hyalinedto.exception.ProtoDefinitionException;

public class ProtoBuilder<T> {

	private HyalineProtoFactory factory;

	private Object superClassInstance;

	private $ protoTemplate;

	private String protoClassName;

	private boolean annotations;

	ProtoBuilder(HyalineProtoFactory factory) {
		this.factory = factory;
	}

	public ProtoBuilder<T> from(T t) {
		this.superClassInstance = t;
		return this;
	}

	public ProtoBuilder<T> proto($ template) {
		this.protoTemplate = template;
		return this;
	}

	public ProtoBuilder<T> withAnnotations() {
		annotations = true;
		return this;		
	}
	
	public ProtoBuilder<T> withName(String typeName) {
		this.protoClassName = typeName;
		return this;
	}
	
	@SuppressWarnings("unchecked")
	public T build() throws InstantiationException {
		if(superClassInstance == null){
			superClassInstance = new Object();
		}
		if(protoClassName == null) {
			protoClassName = "Hyaline$Proto$" + System.currentTimeMillis();
		}
		Object proto = null;
		try {
			 proto = factory.create(superClassInstance, protoTemplate, !annotations, protoClassName);
		} catch (CannotInstantiateProxyException | ProtoDefinitionException e) {
			e.printStackTrace();
			throw new InstantiationException();
		}
		return (T) proto;
	}
	
}
