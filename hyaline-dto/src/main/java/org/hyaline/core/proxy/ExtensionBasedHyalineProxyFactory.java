package org.hyaline.core.proxy;

import java.lang.annotation.Annotation;

import org.hyaline.api.DTO;
import org.hyaline.api.HyalineProxy;
import org.hyaline.core.ClassBuilder;
import org.hyaline.core.HyalineProxyFactory;
import org.hyaline.core.InterfaceImplementationStrategy;
import org.hyaline.core.ProxyStrategy;
import org.hyaline.core.build.JavassistBasedClassBuilder;
import org.hyaline.core.exception.CannotBuildClassException;
import org.hyaline.core.exception.CannotInstantiateProxyException;
import org.hyaline.core.reflect.DTODescription;

public class ExtensionBasedHyalineProxyFactory implements HyalineProxyFactory {

	private ClassBuilder classBuilder = new JavassistBasedClassBuilder();

	private ProxyStrategy proxyStrategy = new NOPProxyStrategy();

	private InterfaceImplementationStrategy interfaceImplementationStrategy = new HyalineProxyImplementationStrategy();

	@Override
	public InterfaceImplementationStrategy getInterfaceImplementationStrategy() {
		return interfaceImplementationStrategy;
	}

	@Override
	public void setInterfaceImplementationStrategy(
			InterfaceImplementationStrategy interfaceImplementationStrategy) {
		this.interfaceImplementationStrategy = interfaceImplementationStrategy;
	}

	@Override
	public ProxyStrategy getProxyStrategy() {
		return proxyStrategy;
	}

	@Override
	public void setProxyStrategy(ProxyStrategy proxyStrategy) {
		this.proxyStrategy = proxyStrategy;
	}

	@Override
	public ClassBuilder getClassBuilder() {
		return classBuilder;
	}

	@Override
	public void setClassBuilder(ClassBuilder classBuilder) {
		this.classBuilder = classBuilder;
	}

	@Override
	public <T> Object createFromScratch(T entity, DTO config)
			throws CannotInstantiateProxyException {
		DTODescription description = createDescription(entity, config, true);
		Object proxy = buildProxy(description);
		return proxy;
	}

	@Override
	public <T> Object createFromClass(T entity, DTO config)
			throws CannotInstantiateProxyException {
		DTODescription description = createDescription(entity, config, false);
		Object proxy = buildProxy(description);
		return proxy;
	}

	private Object buildProxy(DTODescription description)
			throws CannotInstantiateProxyException {
		Object proxy = null;
		try {
			Class<?> proxyClass = classBuilder.buildClass(description,
					proxyStrategy, interfaceImplementationStrategy);
			proxy = proxyClass.newInstance();
		} catch (CannotBuildClassException | InstantiationException
				| IllegalAccessException e) {
			throw new CannotInstantiateProxyException();
		}
		return proxy;
	}

	private DTODescription createDescription(Object entity, DTO config,
			boolean override) {
		DTODescription description = new DTODescription(entity);
		description.addImplementedInterface(HyalineProxy.class);
		if (override) {
			getAnnotationsFromDTO(config, description);
		} else {
			mergeAnnotationsFromDTO(config, description);
		}

		return description;
	}

	private void mergeAnnotationsFromDTO(DTO config, DTODescription description) {
		Class<?> dtoTypeConfig = getDTOTypeConfig(config);
		Annotation[] dtoConfigTypeAnnotations = dtoTypeConfig
				.getDeclaredAnnotations();
		Annotation[] entityClassAnnotations = description.getType()
				.getDeclaredAnnotations();
		if (dtoConfigTypeAnnotations != null) {
			for (Annotation annotation : dtoConfigTypeAnnotations) {
				description.addAnnotation(annotation);
			}
		}
		if (entityClassAnnotations != null) {
			for (Annotation annotation : entityClassAnnotations) {
				if (!dtoTypeConfig.isAnnotationPresent(annotation
						.annotationType())) {
					description.addAnnotation(annotation);
				}
			}
		}
	}

	private void getAnnotationsFromDTO(DTO config, DTODescription description) {
		Annotation[] declaredAnnotations = getDTOTypeConfig(config)
				.getDeclaredAnnotations();
		if (declaredAnnotations != null) {
			for (Annotation annotation : declaredAnnotations) {
				description.addAnnotation(annotation);
			}
		}
	}

	private Class<?> getDTOTypeConfig(DTO config) {
		Class<?> typeConfig = null;
		Class<?>[] classes = config.getClass().getClasses();
		if (classes != null && classes.length > 0) {
			// if user defines more than one class in the invocation, we take
			// only the first
			typeConfig = classes[0];
		}
		return typeConfig;
	}

}
