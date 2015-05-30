package org.hyalinedto.api;

import org.hyalinedto.core.HyalineProxyFactory;
import org.hyalinedto.core.exception.CannotInstantiateProxyException;
import org.hyalinedto.exception.DTODefinitionException;

public class ProtoBuilder<T> {

	private HyalineProxyFactory factory;

	private Object target;

	private $ template;

	private String typeName;

	private boolean annotations;

	ProtoBuilder(HyalineProxyFactory factory) {
		this.factory = factory;
	}

	public ProtoBuilder<T> from(T t) {
		this.target = t;
		return this;
	}

	public ProtoBuilder<T> proto($ template) {
		this.template = template;
		return this;
	}

	public ProtoBuilder<T> withAnnotations() {
		annotations = true;
		return this;		
	}
	
	public ProtoBuilder<T> withName(String typeName) {
		this.typeName = typeName;
		return this;
	}
	
	@SuppressWarnings("unchecked")
	public T build() throws InstantiationException {
		if(target == null){
			target = new Object();
		}
		if(typeName == null) {
			typeName = "Hyaline$Proxy$" + System.currentTimeMillis();
		}
		Object proto = null;
		try {
			 proto = factory.create(target, template, !annotations, typeName);
		} catch (CannotInstantiateProxyException | DTODefinitionException e) {
			e.printStackTrace();
			throw new InstantiationException();
		}
		return (T) proto;
	}
	
}
