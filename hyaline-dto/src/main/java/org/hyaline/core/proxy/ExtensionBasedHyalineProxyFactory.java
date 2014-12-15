package org.hyaline.core.proxy;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import org.hyaline.api.DTO;
import org.hyaline.api.HyalineProxy;
import org.hyaline.core.ClassBuilder;
import org.hyaline.core.ClassRepository;
import org.hyaline.core.HyalineProxyFactory;
import org.hyaline.core.InterfaceImplementationStrategy;
import org.hyaline.core.ProxyStrategy;
import org.hyaline.core.build.JavassistBasedClassBuilder;
import org.hyaline.core.exception.CannotBuildClassException;
import org.hyaline.core.exception.CannotInstantiateProxyException;
import org.hyaline.core.reflect.DTODescription;
import org.hyaline.core.reflect.FieldDescription;

public class ExtensionBasedHyalineProxyFactory implements HyalineProxyFactory {

	private ClassBuilder classBuilder = new JavassistBasedClassBuilder();

	private ProxyStrategy proxyStrategy = new NOPProxyStrategy();

	private InterfaceImplementationStrategy interfaceImplementationStrategy = new HyalineProxyImplementationStrategy();

	private ClassRepository<String, Class<?>> classRepository = new BaseClassRepository();

	@Override
	public ClassRepository<String, Class<?>> getClassRepository() {
		return classRepository;
	}

	@Override
	public void setClassRepository(
			ClassRepository<String, Class<?>> classRepository) {
		this.classRepository = classRepository;
	}

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
		return create(entity, config, true);
	}

	@Override
	public <T> Object createFromClass(T entity, DTO config)
			throws CannotInstantiateProxyException {
		return create(entity, config, false);
	}

	private <T> Object create(T entity, DTO config, boolean override)
			throws CannotInstantiateProxyException {
		// check if a proxy definition for the template class already exists
		Class<?> templateClass = getTemplateClass(config);
		Class<?> proxyClass = classRepository.get(templateClass
				.getCanonicalName());

		// if not, create it and save it to the repository
		if (proxyClass == null) {
			// create a description for the proxy definition
			DTODescription description = createDescription(entity, config,
					override);
			// build the proxy definition
			proxyClass = buildProxyClass(description);
		}
		classRepository.put(templateClass.getCanonicalName(), proxyClass);

		// finally, instantiate the proxy class
		Object proxy = null;
		try {
			proxy = proxyClass.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
			throw new CannotInstantiateProxyException();
		}

		return proxy;
	}

	private Class<?> getTemplateClass(DTO config) {
		Class<?> templateClass = null;
		Class<?>[] innerClasses = config.getClass().getClasses();
		if (innerClasses != null && config.getClass().getClasses().length > 0) {
			templateClass = innerClasses[0];
		} else {
			templateClass = config.getClass();
		}
		return templateClass;
	}

	private Class<?> buildProxyClass(DTODescription description)
			throws CannotInstantiateProxyException {
		Class<?> proxyClass = null;
		try {
			proxyClass = classBuilder.buildClass(description, proxyStrategy,
					interfaceImplementationStrategy);
		} catch (CannotBuildClassException e) {
			throw new CannotInstantiateProxyException();
		}
		return proxyClass;
	}

	private DTODescription createDescription(Object entity, DTO config,
			boolean override) {
		DTODescription description = new DTODescription(entity);
		description.addImplementedInterface(HyalineProxy.class);
		if (override) {
			getClassAnnotationsFromDTO(config, description);
		} else {
			mergeClassAnnotationsFromDTO(config, description);
		}
		// TODO Handle fields and methods annotation here
		Class<?> dtoType = getTemplateClass(config);
		for (Field f : dtoType.getDeclaredFields()) {
			FieldDescription desc = new FieldDescription();
			boolean injectable = isFieldInjectable(config, f);
			desc.setInjectable(injectable);
			desc.setField(f);
			Annotation[] fieldAnnotations = f.getDeclaredAnnotations();
			if (fieldAnnotations != null) {
				for (Annotation annotation : fieldAnnotations) {
					desc.addAnnotation(annotation);
				}
			}
		}

		return description;
	}

	private boolean isFieldInjectable(Object dto, Field f) {
		boolean accessible = f.isAccessible();
		f.setAccessible(true);
		Object value = null;
		try {
			value = f.get(dto);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
		f.setAccessible(accessible);
		return (value != null);
	}

	private void mergeClassAnnotationsFromDTO(DTO config,
			DTODescription description) {
		Class<?> dtoType = getTemplateClass(config);
		Annotation[] dtoTypeAnnotations = dtoType.getDeclaredAnnotations();
		Annotation[] entityClassAnnotations = description.getType()
				.getDeclaredAnnotations();
		if (dtoTypeAnnotations != null) {
			for (Annotation annotation : dtoTypeAnnotations) {
				description.addAnnotation(annotation);
			}
		}
		if (entityClassAnnotations != null) {
			for (Annotation annotation : entityClassAnnotations) {
				if (!dtoType.isAnnotationPresent(annotation.annotationType())) {
					description.addAnnotation(annotation);
				}
			}
		}
	}

	private void getClassAnnotationsFromDTO(DTO config,
			DTODescription description) {
		Annotation[] declaredAnnotations = getTemplateClass(config)
				.getDeclaredAnnotations();
		if (declaredAnnotations != null) {
			for (Annotation annotation : declaredAnnotations) {
				description.addAnnotation(annotation);
			}
		}
	}

}
