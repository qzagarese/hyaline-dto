package org.hyaline.core.proxy;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

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
import org.hyaline.core.reflect.MethodDescription;
import org.hyaline.exception.DTODefinitionException;

public class ReflectionBasedHyalineProxyFactory implements HyalineProxyFactory {

	private ClassBuilder classBuilder = new JavassistBasedClassBuilder();

	private ProxyStrategy proxyStrategy = new NOPProxyStrategy();

	private InterfaceImplementationStrategy interfaceImplementationStrategy = new HyalineProxyImplementationStrategy();

	private ClassRepository<String, Class<?>> classRepository = new BaseClassRepository();

	private Map<String, DTODescription> dtoDescriptions = new HashMap<String, DTODescription>();

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
			throws CannotInstantiateProxyException, DTODefinitionException {
		return create(entity, config, true);
	}

	@Override
	public <T> Object createFromClass(T entity, DTO config)
			throws CannotInstantiateProxyException, DTODefinitionException {
		return create(entity, config, false);
	}

	private <T> Object create(T entity, DTO config, boolean override)
			throws CannotInstantiateProxyException, DTODefinitionException {
		// check if a proxy definition for the template class already exists
		Class<?> templateClass = getTemplateClass(config);
		Class<?> proxyClass = classRepository.get(templateClass
				.getName());

		// check if a DTO description for the template class already exists
		DTODescription description = dtoDescriptions.get(templateClass
				.getName());

		// if not, create a description for the proxy definition and save it for
		// future invocations
		if (description == null) {
			description = createDescription(entity, config, override);
			dtoDescriptions.put(templateClass.getName(), description);
		}

		// if no proxy definition exists, create it and save it to the
		// repository
		if (proxyClass == null) {
			// build the proxy definition
			proxyClass = buildProxyClass(description);
		}
		classRepository.put(templateClass.getName(), proxyClass);

		// finally, instantiate the proxy class
		Object proxy = null;
		try {
			proxy = proxyClass.newInstance();
			// TODO handle here injectable fields
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
			throw new CannotInstantiateProxyException();
		}

		return proxy;
	}

	private Class<?> getTemplateClass(DTO config) {
		Class<?> templateClass = null;
		Class<?>[] innerClasses = config.getClass().getDeclaredClasses();
		if (innerClasses != null && innerClasses.length > 0) {
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
			boolean override) throws DTODefinitionException {
		DTODescription description = new DTODescription(entity);
		description.addImplementedInterface(HyalineProxy.class);
		if (override) {
			getClassAnnotationsFromDTO(config, description);
		} else {
			mergeClassAnnotationsFromDTO(config, description);
		}

		Class<?> dtoType = getTemplateClass(config);
		Class<?> targetType = description.getType();

		for (Field f : dtoType.getDeclaredFields()) {
			// avoid treating anonymous classes as fields
			if (!f.getName().startsWith("this$")) {
				FieldDescription desc = handleFieldFromDTO(config, f,
						targetType);
				description.putField(desc);
			}
		}

		for (Field f : targetType.getDeclaredFields()) {
			FieldDescription desc = handleFieldFromEntityClass(override,
					description, dtoType, f);
			description.putField(desc);
		}

		for (Method m : dtoType.getDeclaredMethods()) {
			MethodDescription desc = handleMethodFromDTO(m, targetType,
					override);
			description.putMethod(desc);
		}

		for (Method m : targetType.getDeclaredMethods()) {
			MethodDescription desc = handleMethodFromEntityClass(override,
					description, m);
			description.putMethod(desc);
		}

		return description;
	}

	private MethodDescription handleMethodFromEntityClass(boolean override,
			DTODescription description, Method m) {
		MethodDescription desc;
		desc = description.getMethod(m.getName());
		if (desc == null) {
			desc = new MethodDescription();
			desc.setMethod(m);
			if (!override) {
				for (Annotation annotation : m.getDeclaredAnnotations()) {
					desc.addAnnotation(annotation);
				}
			}
		} else {
			if (!override) {
				for (Annotation annotation : m.getDeclaredAnnotations()) {
					if (!desc.getMethod().isAnnotationPresent(
							annotation.annotationType())) {
						desc.addAnnotation(annotation);
					}
				}
			}
		}
		return desc;
	}

	private MethodDescription handleMethodFromDTO(Method m,
			Class<?> targetType, boolean override)
			throws DTODefinitionException {
		MethodDescription method = new MethodDescription();
		Method targetMethod = null;
		try {
			targetMethod = targetType.getDeclaredMethod(m.getName(),
					m.getParameterTypes());
		} catch (NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
			throw new DTODefinitionException(
					"You cannot define new methods in the template. "
							+ "You can only declare an abstract method matching a "
							+ "method present in the target class and re-define its "
							+ "annotation-based configuration. "
							+ "You can, otherwise, define a new field and Hyaline will "
							+ "auto-generate its getter and setter.");
		}
		method.setMethod(targetMethod);
		for (Annotation annotation : m.getDeclaredAnnotations()) {
			method.addAnnotation(annotation);
		}
		if (!override) {
			for (Annotation annotation : targetMethod.getDeclaredAnnotations()) {
				if (!m.isAnnotationPresent(annotation.annotationType())) {
					method.addAnnotation(annotation);
				}
			}
		}
		return method;
	}

	private FieldDescription handleFieldFromEntityClass(boolean override,
			DTODescription description, Class<?> dtoType, Field f) {
		FieldDescription desc;
		Field dtoField = null;
		try {
			dtoField = dtoType.getDeclaredField(f.getName());
		} catch (NoSuchFieldException | SecurityException e) {
			// there is no field named like this in the DTO
		}
		if (dtoField == null) {
			desc = new FieldDescription();
			desc.setField(f);
			desc.setInjectable(false);
			if (!override) {
				// if there is no field with this name in the DTO
				// and we start from the entity class, copy
				// the annotations from such class
				for (Annotation annotation : f.getDeclaredAnnotations()) {
					desc.addAnnotation(annotation);
				}
			}
		} else {
			desc = description.getField(dtoField.getName());
			if (!override) {
				// if there is a field with this name in the DTO
				// and we start from the entity class, copy
				// those annotations that are not present in the DTO
				// from such class
				for (Annotation annotation : f.getDeclaredAnnotations()) {
					if (!dtoField.isAnnotationPresent(annotation
							.annotationType())) {
						desc.addAnnotation(annotation);
					}
				}
			}
		}
		return desc;
	}

	private FieldDescription handleFieldFromDTO(DTO config, Field f,
			Class<?> targetType) {
		FieldDescription desc = new FieldDescription();
		boolean injectable = isFieldInjectable(config, f);
		desc.setInjectable(injectable);
		desc.setField(f);
		try {
			targetType.getDeclaredField(f.getName());
		} catch (NoSuchFieldException | SecurityException e) {
			// this field does not exist in the entity class, so we tell the
			// class builder we need a getter and a setter for it
			desc.setFromTemplate(true);
		}
		Annotation[] fieldAnnotations = f.getDeclaredAnnotations();
		if (fieldAnnotations != null) {
			for (Annotation annotation : fieldAnnotations) {
				desc.addAnnotation(annotation);
			}
		}
		return desc;
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
