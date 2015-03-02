package org.hyalinedto.core.build;

import java.lang.reflect.Modifier;

import javassist.CannotCompileException;
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
		try {

			hyalineProxyClass.setSuperclass(classPool.get(entityClass.getName()));
			for (Class<?> interfaceType : description.getImplementedInterfaces()) {
				hyalineProxyClass.addInterface(ClassPool.getDefault().get(interfaceType.getName()));
			}

			ClassFile ccFile = hyalineProxyClass.getClassFile();
			ConstPool constpool = ccFile.getConstPool();

			// Add annotations to Class
			addClassAnnotations(description, hyalineProxyClass, constpool);

			// Add a field for target
			CtField targetField = CtField.make("private " + description.getTarget().getClass().getName() + " target;",
			        hyalineProxyClass);
			hyalineProxyClass.addField(targetField);

			// Here I create the necessary fields
			for (FieldDescription field : description.getFields().values()) {
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

			for (MethodDescription method : description.getMethods().values()) {
				// Handle only getters and setters
				String methodName = method.getMethod().getName();
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

		} catch (NotFoundException | CannotCompileException e) {
			e.printStackTrace();
			throw new CannotBuildClassException(e.getMessage());
		}
		try {
			return hyalineProxyClass.toClass();
		} catch (CannotCompileException e) {
			e.printStackTrace();
			throw new CannotBuildClassException(e.getMessage());
		}
	}

	private CtMethod createMethodFromDescription(ClassPool classPool, ConstPool constpool, MethodDescription method,
	        CtClass hyalineProxyClass, DTODescription description) throws FieldNotFoundException,
	        CannotCompileException, NotFoundException, CannotBuildClassException {
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
	        CtClass hyalineProxyClass) throws CannotCompileException, NotFoundException, CannotBuildClassException {
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
	        NotFoundException {
		String fieldName = field.getField().getName();
		String methodName = "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
		StringBuffer buffer = new StringBuffer();
		buffer.append("{");
		// if this method overrides a method from the entity class
		// and the field has not been initialized in the template
		// invoke the superclass method first
		if (!field.isFromTemplate() && !field.isInitialized()) {
			buffer.append("target.").append(methodName).append("($1);");
		}
		// assign the argument value to the field
		buffer.append("this.").append(fieldName).append(" = $1;}");
		CtClass returnType = ClassPool.getDefault().get("void");
		String methodBody = buffer.toString();
		CtMethod method = CtNewMethod.make(Modifier.PUBLIC, returnType, methodName, new CtClass[] { ClassPool
		        .getDefault().get(field.getField().getType().getName()) }, new CtClass[] {}, methodBody,
		        hyalineProxyClass);
		return method;
	}

	private CtMethod createGetter(FieldDescription field, CtClass hyalineProxyClass) throws CannotCompileException,
	        NotFoundException {
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
			buffer.append("this.").append(fieldName).append(" = target.").append(methodName).append("();");
		}
		// return the value of the corresponding field
		buffer.append("return this.").append(fieldName).append(";}");
		CtClass returnType = ClassPool.getDefault().get(field.getField().getType().getName());
		String methodBody = buffer.toString();
		CtMethod method = CtNewMethod.make(Modifier.PUBLIC, returnType, methodName, new CtClass[] {}, new CtClass[] {},
		        methodBody, hyalineProxyClass);
		return method;
	}

}
