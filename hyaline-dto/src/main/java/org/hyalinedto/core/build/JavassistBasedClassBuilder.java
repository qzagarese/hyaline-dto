package org.hyalinedto.core.build;

import java.lang.reflect.Modifier;

import javassist.CannotCompileException;
import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.NotFoundException;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.Annotation;

import org.hyalinedto.core.ClassBuilder;
import org.hyalinedto.core.exception.CannotBuildClassException;
import org.hyalinedto.core.exception.FieldNotFoundException;
import org.hyalinedto.core.reflect.DTODescription;
import org.hyalinedto.core.reflect.FieldDescription;
import org.hyalinedto.core.reflect.MethodDescription;

public class JavassistBasedClassBuilder implements ClassBuilder {

	@Override
	public Class<?> buildClass(DTODescription description, String proxyClassName) throws CannotBuildClassException {
		ClassPool classPool = ClassPool.getDefault();
		Class<?> entityClass = description.getType();
		CtClass hyalineProxyClass = classPool.makeClass(proxyClassName);

		if (Modifier.isFinal(entityClass.getModifiers())) {
			throw new CannotBuildClassException("Cannot proxy class " + entityClass.getCanonicalName()
			        + " since it is final.");
		}

		// avoid NotFoundException if possible
		CtClass clazz = null;
		try {
			clazz = classPool.get(entityClass.getName());
		} catch (NotFoundException e) {
			classPool.appendClassPath(new ClassClassPath(entityClass));
			try {
				clazz = classPool.get(entityClass.getName());
			} catch (NotFoundException e1) {
				throw new CannotBuildClassException(e1.getMessage());
			}
		}

		try {
			hyalineProxyClass.setSuperclass(clazz);
		} catch (CannotCompileException e1) {
			throw new CannotBuildClassException(e1.getMessage());
		}
		for (Class<?> interfaceType : description.getImplementedInterfaces()) {
			CtClass anInterface = null;
			// same here
			try {
				anInterface = classPool.get(interfaceType.getName());
			} catch (NotFoundException e) {
				classPool.appendClassPath(new ClassClassPath(interfaceType));
				try {
					anInterface = classPool.get(interfaceType.getName());
				} catch (NotFoundException e2) {
					throw new CannotBuildClassException(e2.getMessage());
				}
			}
			hyalineProxyClass.addInterface(anInterface);
		}

		ClassFile ccFile = hyalineProxyClass.getClassFile();
		ConstPool constpool = ccFile.getConstPool();

		// Add annotations to Class
		addClassAnnotations(description, hyalineProxyClass, constpool);

		// Add a field for target
		CtField targetField;
		try {
			targetField = CtField.make("private " + description.getType().getName() + " target;",
			        hyalineProxyClass);

			hyalineProxyClass.addField(targetField);

			// Here I create the necessary fields
			for (FieldDescription field : description.getFields().values()) {
				int modifiers = field.getField().getModifiers();
				
				// Check the field is private or, if not private, it is not final
				if (Modifier.isPrivate(modifiers) || ((!Modifier.isPrivate(modifiers)) && (!Modifier.isFinal(modifiers)))) {
					CtField ctField = createFieldFromDescription(classPool, constpool, field, hyalineProxyClass);

					hyalineProxyClass.addField(ctField);

					// Generate a getter and a setter for fields defined in
					// template
					if (field.isFromTemplate()) {
						CtMethod getter = createGetter(field, hyalineProxyClass);
						hyalineProxyClass.addMethod(getter);
						CtMethod setter = createSetter(field, hyalineProxyClass);
						hyalineProxyClass.addMethod(setter);
					}
				}
			}

			for (MethodDescription method : description.getMethods().values()) {
				// Handle only getters and setters
				String methodName = method.getMethod().getName();

				// check the method is not final
				if (!Modifier.isFinal(method.getMethod().getModifiers())) {
					if (methodName.startsWith("get") || methodName.startsWith("set") || methodName.startsWith("is")) {
						CtMethod ctMethod;
						try {
							ctMethod = createMethodFromDescription(classPool, constpool, method, hyalineProxyClass,
							        description);
							if (ctMethod != null) {
								// could correlate method name to accessed field
								hyalineProxyClass.addMethod(ctMethod);
							}
						} catch (FieldNotFoundException e) {
							e.printStackTrace();
						}
					}
				}
			}

			return hyalineProxyClass.toClass();
		} catch (CannotCompileException e) {
			throw new CannotBuildClassException(e.getMessage());
		}
	}

	private CtMethod createMethodFromDescription(ClassPool classPool, ConstPool constpool, MethodDescription method,
	        CtClass hyalineProxyClass, DTODescription description) throws FieldNotFoundException,
	        CannotCompileException, CannotBuildClassException {
		String methodName = method.getMethod().getName();

		// reconstruct field to be accessed based on method name
		String fieldName = null;
		if (methodName.startsWith("is")) {
			fieldName = methodName.substring(2, 3).toLowerCase() + methodName.substring(3);
		} else {
			fieldName = methodName.substring(3, 4).toLowerCase() + methodName.substring(4);
		}

		FieldDescription field = description.getField(fieldName);
		CtMethod ctMethod = null;

		if (field != null) {
			// the method name can be connected to a field name
			// create a getter or a setter based on method name
			if (methodName.startsWith("is") || methodName.startsWith("get")) {
				ctMethod = createGetter(field, hyalineProxyClass);
			} else {
				ctMethod = createSetter(field, hyalineProxyClass);
			}
			// finally copy annotations
			if (method.getAnnotations() != null && method.getAnnotations().size() > 0) {
				AnnotationsAttribute attr = new AnnotationsAttribute(constpool, AnnotationsAttribute.visibleTag);
				for (java.lang.annotation.Annotation annotation : method.getAnnotations()) {
					Annotation annotationCopy = JavassistUtils.createJavassistAnnotation(constpool, annotation);
					attr.addAnnotation(annotationCopy);
				}
				ctMethod.getMethodInfo().addAttribute(attr);
			}
		} else {
			// cannot find correlation between this method and a field name.
			// Ignore this method since it will be inherited by the proxy.
		}

		return ctMethod;
	}

	private void addClassAnnotations(DTODescription description, CtClass hyalineProxyClass, ConstPool constpool)
	        throws CannotBuildClassException {
		if (description.getAnnotations() != null) {
			AnnotationsAttribute attr = new AnnotationsAttribute(constpool, AnnotationsAttribute.visibleTag);
			for (java.lang.annotation.Annotation annotation : description.getAnnotations()) {
				Annotation annotationCopy = JavassistUtils.createJavassistAnnotation(constpool, annotation);
				attr.addAnnotation(annotationCopy);
			}
			hyalineProxyClass.getClassFile().addAttribute(attr);
		}
	}

	private CtField createFieldFromDescription(ClassPool classPool, ConstPool constpool, FieldDescription field,
	        CtClass hyalineProxyClass) throws CannotCompileException, CannotBuildClassException {
		String fieldTypeName = field.getField().getType().getCanonicalName();
		CtField ctField = CtField.make("private " + fieldTypeName + " " + field.getField().getName() + ";",
		        hyalineProxyClass);
		if (field.getAnnotations() != null && field.getAnnotations().size() > 0) {
			AnnotationsAttribute attr = new AnnotationsAttribute(constpool, AnnotationsAttribute.visibleTag);
			for (java.lang.annotation.Annotation annotation : field.getAnnotations()) {
				Annotation annotationCopy = JavassistUtils.createJavassistAnnotation(constpool, annotation);
				attr.addAnnotation(annotationCopy);
			}
			ctField.getFieldInfo().addAttribute(attr);
		}
		return ctField;
	}

	private CtMethod createSetter(FieldDescription field, CtClass hyalineProxyClass) throws CannotCompileException,
	        CannotBuildClassException {
		String fieldName = field.getField().getName();
		String methodName = "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
		StringBuffer buffer = new StringBuffer();
		buffer.append("{");
		// if this method overrides a method from the entity class
		// and the field has not been initialized in the template
		// invoke the superclass method first
		if (!field.isFromTemplate() && !field.isInitialized()) {
			// Check whether target is null to avoid NPE after deserialization
			buffer.append("if(target != null) { ");
			buffer.append("target.").append(methodName).append("($1);");
			buffer.append("}");
		}
		// assign the argument value to the field
		buffer.append("this.").append(fieldName).append(" = $1;}");
		CtClass returnType = null;
		ClassPool classPool = ClassPool.getDefault();
		try {
			returnType = classPool.get("void");
		} catch (NotFoundException e) {
			e.printStackTrace();
		}
		String methodBody = buffer.toString();
		CtClass parameter = null;
		try {
			parameter = classPool.get(field.getField().getType().getName());
		} catch (NotFoundException e) {
			classPool.appendClassPath(new ClassClassPath(field.getField().getType()));
			try {
				parameter = classPool.get(field.getField().getType().getName());
			} catch (NotFoundException e2) {
				throw new CannotBuildClassException(e2.getMessage());
			}
		}
		CtMethod method = CtNewMethod.make(Modifier.PUBLIC, returnType, methodName, new CtClass[] { parameter },
		        new CtClass[] {}, methodBody, hyalineProxyClass);
		return method;
	}

	private CtMethod createGetter(FieldDescription field, CtClass hyalineProxyClass) throws CannotCompileException,
	        CannotBuildClassException {
		String fieldName = field.getField().getName();
		String methodName = null;
		String prefix = null;
		String fieldTypeName = field.getField().getType().getName();
		if (fieldTypeName.equals("boolean")) {
			prefix = "is";
		} else {
			prefix = "get";
		}
		methodName = prefix + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
		StringBuffer buffer = new StringBuffer();
		buffer.append("{");
		// if this method overrides a method from the entity class
		// and the field has not been initialized in the template
		// invoke the superclass method first and assign the returned
		// value to the local field
		if (!field.isFromTemplate() && !field.isInitialized()) {
			// Check whether target is null to avoid NPE after deserialization
			buffer.append("if(target != null) { ");
			buffer.append("this.").append(fieldName).append(" = target.").append(methodName).append("();");
			buffer.append("}");
		}
		// return the value of the corresponding field
		buffer.append("return this.").append(fieldName).append(";}");
		ClassPool classPool = ClassPool.getDefault();
		CtClass returnType = null;
		try {
			returnType = classPool.get(field.getField().getType().getName());
		} catch (NotFoundException e) {
			classPool.appendClassPath(new ClassClassPath(field.getField().getType()));
			try {
				returnType = classPool.get(field.getField().getType().getName());
			} catch (NotFoundException e1) {
				throw new CannotBuildClassException(e1.getMessage());
			}
		}
		String methodBody = buffer.toString();
		CtMethod method = CtNewMethod.make(Modifier.PUBLIC, returnType, methodName, new CtClass[] {}, new CtClass[] {},
		        methodBody, hyalineProxyClass);
		return method;
	}

}
