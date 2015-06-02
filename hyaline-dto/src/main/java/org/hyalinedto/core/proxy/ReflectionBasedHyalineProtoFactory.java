package org.hyalinedto.core.proxy;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.hyalinedto.api.Proto;
import org.hyalinedto.core.ClassBuilder;
import org.hyalinedto.core.ClassRepository;
import org.hyalinedto.core.HyalineProtoFactory;
import org.hyalinedto.core.build.JavassistBasedClassBuilder;
import org.hyalinedto.core.exception.CannotBuildClassException;
import org.hyalinedto.core.exception.CannotInstantiateProxyException;
import org.hyalinedto.core.reflect.FieldDescription;
import org.hyalinedto.core.reflect.MethodDescription;
import org.hyalinedto.core.reflect.ProtoDescription;
import org.hyalinedto.core.reflect.ReflectionUtils;
import org.hyalinedto.exception.ProtoDefinitionException;

public class ReflectionBasedHyalineProtoFactory implements HyalineProtoFactory {

	private ClassBuilder classBuilder = new JavassistBasedClassBuilder();

	private ClassRepository<String, Class<?>> classRepository = new InMemoryClassRepository();

	private Map<String, ProtoDescription> protoDescriptions = new HashMap<String, ProtoDescription>();

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

	public <T> Object create(T superClassInstance, Object protoTemplate,
			boolean resetAnnotations, String protoClassName)
			throws CannotInstantiateProxyException, ProtoDefinitionException {
		// check if a proto definition for the template class already exists
		Class<?> protoTemplateClass = getProtoTemplateClass(protoTemplate);
		if (protoTemplateClass != protoTemplate.getClass()) {
			protoTemplate = findTemplateInstance(protoTemplateClass,
					protoTemplate.getClass(), protoTemplate);
			if (protoTemplate == null) {
				throw new ProtoDefinitionException(
						"Could not find an initialized field of type "
								+ protoTemplateClass.getName()
								+ " in prototype definition.");
			}
		}

		// check if a prototype description for the template class already
		// exists
		ProtoDescription description = protoDescriptions.get(protoTemplateClass
				.getName());

		// if not, create a description for the prototype definition and save it
		// for future invocations
		if (description == null) {
			description = createDescription(superClassInstance.getClass(),
					protoTemplate, resetAnnotations);
			protoDescriptions.put(protoTemplateClass.getName(), description);
		}

		Class<?> protoType = classRepository.get(protoTemplateClass.getName());

		// if no prototype definition exists, create it and save it to the
		// repository
		if (protoType == null) {
			// build the prototype definition
			protoType = buildProtoClass(description, protoClassName);
			classRepository.put(protoTemplateClass.getName(), protoType);
		}

		// finally, instantiate the prototype class
		Object proto = null;
		try {
			proto = protoType.newInstance();
			injectPrototypeTemplateInstance(proto, protoTemplate);
			injectAllFields(proto, description, protoTemplate,
					superClassInstance);
		} catch (InstantiationException | IllegalAccessException
				| NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
			throw new CannotInstantiateProxyException();
		}

		return proto;
	}

	private void injectPrototypeTemplateInstance(Object proto, Object protoInstance)
			throws NoSuchFieldException, SecurityException,
			IllegalArgumentException, IllegalAccessException {
		Proto hyalinePrototype = (Proto) proto;
		Field field = proto.getClass().getDeclaredField(
				hyalinePrototype.obtainPrototypeInstanceFieldName());
		ReflectionUtils.injectField(field, proto, protoInstance);
	}

	private void injectAllFields(Object proto, ProtoDescription description,
			Object protoTemplate, Object superClassInstance)
			throws IllegalArgumentException, IllegalAccessException,
			SecurityException, NoSuchFieldException {
		for (FieldDescription field : description.getFields().values()) {

			Field proxyField = null;
			try {
				proxyField = proto.getClass().getDeclaredField(
						field.getField().getName());
			} catch (NoSuchFieldException e) {
				// proto does not have this field because it cannot inherit it
				// (e.g. it was a final field)
			}

			if (proxyField != null) {
				Object value = null;
				if (field.isFromTemplate() || field.isInitialized()) {
					value = ReflectionUtils.getFieldValue(field.getField(),
							protoTemplate);
				} else {
					// if the template redefines the field but does not
					// initialises it, retrieve the corresponding field
					// from super class and get its value
					Field f = field.getField();
					if (!f.getDeclaringClass().equals(description.getSuperType())) {
						f = description.getSuperType().getDeclaredField(f.getName());
					}
					value = ReflectionUtils
							.getFieldValue(f, superClassInstance);
				}
				ReflectionUtils.injectField(proxyField, proto, value);
			}
		}
	}

	private Object findTemplateInstance(Class<?> actualProtoTemplateClass,
			Class<?> protoTemplateDefinitionClass, Object protoTemplate) {
		// if there is an inner class inside the proto template,
		// hyaline picks that one as template (actualProtoTemplateClass)
		// protoTemplateDefinitionClass is the anonymous class (the $) that is
		// not used in this case.
		// This is an exceptional case that allows the client to place
		// annotations on the newly
		// defined type (annotations are placed on the inner class).
		// In this case, an initialised field of type "actualProtoTemplateClass"
		// must be provided.
		// This method looks for such field
		/**
		 * 
		 * Ex.
		 * 
		 * proto(new $(){ // <-- Here starts protoTemplateDefinitionClass
		 * 
		 * ActualProtoTemplateClass inst = new ActualProtoTemplateClass(); // we
		 * are looking for this
		 * 
		 * @AnyAnnotation // this way we can put annotations on the prototype
		 *                class class ActualProtoTemplateClass() {
		 * 
		 *                String anyField = "whatever.";
		 * 
		 *                }
		 * 
		 *                });
		 * 
		 */
		Object value = null;
		for (Field f : protoTemplateDefinitionClass.getDeclaredFields()) {
			if (f.getType().equals(actualProtoTemplateClass)) {
				boolean accessible = f.isAccessible();
				f.setAccessible(true);
				try {
					value = f.get(protoTemplate);
				} catch (IllegalArgumentException | IllegalAccessException e) {
					e.printStackTrace();
				}
				f.setAccessible(accessible);
			}
		}
		return value;
	}

	private Class<?> getProtoTemplateClass(Object protoTemplate) {
		Class<?> templateClass = null;
		Class<?>[] innerClasses = protoTemplate.getClass().getDeclaredClasses();
		if (innerClasses != null && innerClasses.length > 0) {
			templateClass = innerClasses[0];
		} else {
			templateClass = protoTemplate.getClass();
		}
		return templateClass;
	}

	private Class<?> buildProtoClass(ProtoDescription description,
			String proxyClassName) throws CannotInstantiateProxyException {
		Class<?> proxyClass = null;
		try {
			proxyClass = classBuilder.buildClass(description, proxyClassName);
		} catch (CannotBuildClassException e) {
			e.printStackTrace();
			throw new CannotInstantiateProxyException();
		}
		return proxyClass;
	}

	private ProtoDescription createDescription(Class<?> superClassType,
			Object protoTemplate, boolean resetAnnotations)
			throws ProtoDefinitionException {
		ProtoDescription description = new ProtoDescription(superClassType, getProtoTemplateClass(protoTemplate));
		if (resetAnnotations) {
			getClassAnnotationsFromProto(protoTemplate, description);
		} else {
			mergeClassAnnotationsFromProto(protoTemplate, description);
		}

		Class<?> protoType = getProtoTemplateClass(protoTemplate);

		for (Field f : protoType.getDeclaredFields()) {
			// avoid treating references to outer classes as fields
			if (!f.getName().startsWith("this$")) {
				FieldDescription desc = handleFieldFromProto(protoTemplate, f,
						superClassType);
				description.putField(desc);
			}
		}

		for (Field f : superClassType.getDeclaredFields()) {
			FieldDescription desc = handleFieldFromSuperClass(resetAnnotations,
					description, protoType, f);
			description.putField(desc);
		}

		for (Method m : protoType.getDeclaredMethods()) {
			MethodDescription desc = handleMethodFromProto(m, superClassType,
					resetAnnotations);
			description.putMethod(desc);
		}

		for (Method m : superClassType.getDeclaredMethods()) {
			MethodDescription desc = handleMethodFromSuperClass(
					resetAnnotations, description, m);
			description.putMethod(desc);
		}

		return description;
	}

	private MethodDescription handleMethodFromSuperClass(boolean override,
			ProtoDescription description, Method superTypeMethod) {
		MethodDescription desc;
		desc = description.getMethod(superTypeMethod);
		if (desc == null) {
			desc = new MethodDescription();
			desc.setMethod(superTypeMethod);
			if (!override) {
				for (Annotation annotation : superTypeMethod.getDeclaredAnnotations()) {
					desc.addAnnotation(annotation);
				}
			}
		} else {
			if (!override) {
				for (Annotation annotation : superTypeMethod.getDeclaredAnnotations()) {
					if (!desc.getMethod().isAnnotationPresent(
							annotation.annotationType())) {
						desc.addAnnotation(annotation);
					}
				}
			}
		}
		return desc;
	}

	private MethodDescription handleMethodFromProto(Method protoTypeMethod,
			Class<?> superClassType, boolean resetAnnotations) {
		MethodDescription methodDescription = new MethodDescription();
		Method superClassMethod = null;
		try {
			superClassMethod = superClassType.getDeclaredMethod(
					protoTypeMethod.getName(),
					protoTypeMethod.getParameterTypes());
		} catch (NoSuchMethodException | SecurityException e) {
			// this method has been added in the prototype template

		}
		methodDescription.setMethod(protoTypeMethod);
		methodDescription.setFromTemplate(true);

		for (Annotation annotation : protoTypeMethod.getDeclaredAnnotations()) {
			methodDescription.addAnnotation(annotation);
		}
		if ((superClassMethod != null) && !resetAnnotations) {
			for (Annotation annotation : superClassMethod
					.getDeclaredAnnotations()) {
				if (!protoTypeMethod.isAnnotationPresent(annotation
						.annotationType())) {
					methodDescription.addAnnotation(annotation);
				}
			}
		}
		return methodDescription;
	}

	private FieldDescription handleFieldFromSuperClass(
			boolean resetAnnotations, ProtoDescription description,
			Class<?> protoType, Field f) {
		FieldDescription desc;
		Field protoField = null;
		try {
			protoField = protoType.getDeclaredField(f.getName());
		} catch (NoSuchFieldException | SecurityException e) {
			// there is no field named like this in the DTO
		}
		if (protoField == null) {
			desc = new FieldDescription();
			desc.setField(f);
			desc.setInitialized(false);
			if (!resetAnnotations) {
				// if there is no field with this name in the DTO
				// and we start from the entity class, copy
				// the annotations from such class
				for (Annotation annotation : f.getDeclaredAnnotations()) {
					desc.addAnnotation(annotation);
				}
			}
		} else {
			desc = description.getField(protoField.getName());
			if (!resetAnnotations) {
				// if there is a field with this name in the DTO
				// and we start from the entity class, copy
				// those annotations that are not present in the DTO
				// from such class
				for (Annotation annotation : f.getDeclaredAnnotations()) {
					if (!protoField.isAnnotationPresent(annotation
							.annotationType())) {
						desc.addAnnotation(annotation);
					}
				}
			}
		}
		return desc;
	}

	private FieldDescription handleFieldFromProto(Object config, Field f,
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

	private void mergeClassAnnotationsFromProto(Object config,
			ProtoDescription description) {
		Class<?> dtoType = getProtoTemplateClass(config);
		Annotation[] dtoTypeAnnotations = dtoType.getDeclaredAnnotations();
		Annotation[] entityClassAnnotations = description.getSuperType()
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

	private void getClassAnnotationsFromProto(Object config,
			ProtoDescription description) {
		Annotation[] declaredAnnotations = getProtoTemplateClass(config)
				.getDeclaredAnnotations();
		if (declaredAnnotations != null) {
			for (Annotation annotation : declaredAnnotations) {
				description.addAnnotation(annotation);
			}
		}
	}

}
