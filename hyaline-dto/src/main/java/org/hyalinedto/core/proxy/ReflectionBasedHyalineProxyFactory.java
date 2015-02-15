package org.hyalinedto.core.proxy;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.hyalinedto.api.DTO;
import org.hyalinedto.core.ClassBuilder;
import org.hyalinedto.core.ClassRepository;
import org.hyalinedto.core.HyalineProxyFactory;
import org.hyalinedto.core.build.JavassistBasedClassBuilder;
import org.hyalinedto.core.exception.CannotBuildClassException;
import org.hyalinedto.core.exception.CannotInstantiateProxyException;
import org.hyalinedto.core.reflect.DTODescription;
import org.hyalinedto.core.reflect.FieldDescription;
import org.hyalinedto.core.reflect.MethodDescription;
import org.hyalinedto.core.reflect.ReflectionUtils;
import org.hyalinedto.exception.DTODefinitionException;

public class ReflectionBasedHyalineProxyFactory implements HyalineProxyFactory {

	private ClassBuilder classBuilder = new JavassistBasedClassBuilder();

	private ClassRepository<String, Class<?>> classRepository = new InMemoryClassRepository();

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
	public ClassBuilder getClassBuilder() {
		return classBuilder;
	}

	@Override
	public void setClassBuilder(ClassBuilder classBuilder) {
		this.classBuilder = classBuilder;
	}


	public <T> Object create(T entity, Object dtoTemplate, boolean resetAnnotations, String proxyClassName)
			throws CannotInstantiateProxyException, DTODefinitionException {
		// check if a proxy definition for the template class already exists
		Class<?> templateClass = getTemplateClass(dtoTemplate);
		if (templateClass != dtoTemplate.getClass()) {
			dtoTemplate = findTemplateInstance(templateClass,
					dtoTemplate.getClass(), dtoTemplate);
			if (dtoTemplate == null) {
				throw new DTODefinitionException(
						"Could not find an initialized field of type "
								+ templateClass.getName()
								+ " in DTO definition.");
			}
		}

		Class<?> proxyClass = classRepository.get(templateClass.getName());

		// check if a DTO description for the template class already exists
		DTODescription description = dtoDescriptions.get(templateClass
				.getName());

		// if not, create a description for the proxy definition and save it for
		// future invocations
		if (description == null) {
			description = createDescription(entity, dtoTemplate, resetAnnotations);
			dtoDescriptions.put(templateClass.getName(), description);
		}

		// if no proxy definition exists, create it and save it to the
		// repository
		if (proxyClass == null) {
			// build the proxy definition
			proxyClass = buildProxyClass(description, proxyClassName);
		}
		classRepository.put(templateClass.getName(), proxyClass);

		// finally, instantiate the proxy class
		Object proxy = null;
		try {
			proxy = proxyClass.newInstance();
			injectTarget(proxy, description);
			injectAllFields(proxy, description, dtoTemplate);
		} catch (InstantiationException | IllegalAccessException
				| NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
			throw new CannotInstantiateProxyException();
		}

		return proxy;
	}

	private void injectTarget(Object proxy, DTODescription description)
			throws NoSuchFieldException, SecurityException,
			IllegalArgumentException, IllegalAccessException {
		Field field = proxy.getClass().getDeclaredField("target");
		ReflectionUtils.injectField(field, proxy, description.getTarget());
	}

	private void injectAllFields(Object proxy, DTODescription description,
			Object dtoTemplate) throws IllegalArgumentException,
			IllegalAccessException, NoSuchFieldException, SecurityException {
		for (FieldDescription field : description.getFields().values()) {
			Field proxyField = proxy.getClass().getDeclaredField(
					field.getField().getName());
			Object value = null;
			if (field.isFromTemplate() || field.isInitialized()) {
				value = ReflectionUtils.getFieldValue(field.getField(), dtoTemplate);
			} else {
				// if the template redefines the field but does not initializes
				// it
				// retrieve the corresponding field from target class and get
				// its value
				Field f = field.getField();
				if (!f.getDeclaringClass().equals(description.getType())) {
					f = description.getType().getDeclaredField(f.getName());
				}
				value = ReflectionUtils.getFieldValue(f, description.getTarget());
			}
			ReflectionUtils.injectField(proxyField, proxy, value);
		}
	}

	private Object findTemplateInstance(Class<?> templateClass,
			Class<?> dtoClass, Object config) {
		Object value = null;
		for (Field f : dtoClass.getDeclaredFields()) {
			if (f.getType().equals(templateClass)) {
				boolean accessible = f.isAccessible();
				f.setAccessible(true);
				try {
					value = f.get(config);
				} catch (IllegalArgumentException | IllegalAccessException e) {
					e.printStackTrace();
				}
				f.setAccessible(accessible);
			}
		}
		return value;
	}

	private Class<?> getTemplateClass(Object config) {
		Class<?> templateClass = null;
		Class<?>[] innerClasses = config.getClass().getDeclaredClasses();
		if (innerClasses != null && innerClasses.length > 0) {
			templateClass = innerClasses[0];
		} else {
			templateClass = config.getClass();
		}
		return templateClass;
	}

	private Class<?> buildProxyClass(DTODescription description, String proxyClassName)
			throws CannotInstantiateProxyException {
		Class<?> proxyClass = null;
		try {
			proxyClass = classBuilder.buildClass(description, proxyClassName);
		} catch (CannotBuildClassException e) {
			throw new CannotInstantiateProxyException();
		}
		return proxyClass;
	}

	private DTODescription createDescription(Object entity, Object config,
			boolean override) throws DTODefinitionException {
		DTODescription description = new DTODescription(entity);
		if (override) {
			getClassAnnotationsFromDTO(config, description);
		} else {
			mergeClassAnnotationsFromDTO(config, description);
		}

		Class<?> dtoType = getTemplateClass(config);
		Class<?> targetType = description.getType();

		for (Field f : dtoType.getDeclaredFields()) {
			// avoid treating references to outer classes as fields
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
							+ "You can only re-define a method matching a "
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
			desc.setInitialized(false);
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

	private FieldDescription handleFieldFromDTO(Object config, Field f,
			Class<?> targetType) {
		FieldDescription desc = new FieldDescription();
		boolean initialized = ReflectionUtils.isFieldInitialized(config, f);
		desc.setInitialized(initialized);
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

	private void mergeClassAnnotationsFromDTO(Object config,
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

	private void getClassAnnotationsFromDTO(Object config,
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
