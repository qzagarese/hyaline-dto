package org.hyalinedto.core.build;

import java.lang.reflect.Method;
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

import org.hyalinedto.api.Proto;
import org.hyalinedto.core.ClassBuilder;
import org.hyalinedto.core.exception.CannotBuildClassException;
import org.hyalinedto.core.exception.FieldNotFoundException;
import org.hyalinedto.core.reflect.FieldDescription;
import org.hyalinedto.core.reflect.MethodDescription;
import org.hyalinedto.core.reflect.ProtoDescription;
import org.hyalinedto.core.reflect.ReflectionUtils;

public class JavassistBasedClassBuilder implements ClassBuilder {

	private final String PROTO_TEMPLATE_NAME_PREFIX = "__proto_";

	@Override
	public Class<?> buildClass(ProtoDescription description,
			String proxyClassName) throws CannotBuildClassException {
		ClassPool classPool = ClassPool.getDefault();
		Class<?> superClass = description.getSuperType();
		CtClass hyalineProtoClass = classPool.makeClass(proxyClassName);

		if (Modifier.isFinal(superClass.getModifiers())) {
			throw new CannotBuildClassException("Cannot extend class "
					+ superClass.getCanonicalName() + " since it is final.");
		}

		setSuperClassAndImplementHyalineDTO(classPool, superClass,
				hyalineProtoClass);

		ClassFile ccFile = hyalineProtoClass.getClassFile();
		ConstPool constpool = ccFile.getConstPool();

		// Add annotations to Class
		addClassAnnotations(description, hyalineProtoClass, constpool);

		// Add a field for target
		CtField protoTemplateField;
		try {
			String protoTemplateFieldName = PROTO_TEMPLATE_NAME_PREFIX
					+ System.currentTimeMillis();
			protoTemplateField = CtField.make("private "
					+ description.getProtoType().getName() + " "
					+ protoTemplateFieldName + ";", hyalineProtoClass);
			hyalineProtoClass.addField(protoTemplateField);

			handleFieldsDescriptions(description, protoTemplateFieldName,
					classPool, hyalineProtoClass, constpool);

			handleMethodDescriptions(description, protoTemplateFieldName,
					classPool, hyalineProtoClass, constpool);

			implementHyalineDTOGetAttribute(classPool, hyalineProtoClass);

			implementHyalineDTOSetAttribute(classPool, hyalineProtoClass);

			implementHyalineDTOGetTargetFieldName(classPool, hyalineProtoClass,
					protoTemplateFieldName);

			return hyalineProtoClass.toClass();
		} catch (CannotCompileException e) {
			e.printStackTrace();
			throw new CannotBuildClassException(e.getMessage());
		}
	}

	private void implementHyalineDTOGetAttribute(ClassPool classPool,
			CtClass hyalineProxyClass) throws CannotBuildClassException {
		StringBuffer buffer = new StringBuffer();

		buffer.append("{");
		buffer.append("return " + ReflectionUtils.class.getCanonicalName()
				+ ".getFieldValue($1, this);");
		buffer.append("}");

		String methodBody = buffer.toString();
		try {
			CtMethod method = CtNewMethod.make(Modifier.PUBLIC, classPool
					.get(Object.class.getCanonicalName()), "getAttribute",
					new CtClass[] { classPool.get(String.class
							.getCanonicalName()) }, new CtClass[] {},
					methodBody, hyalineProxyClass);

			hyalineProxyClass.addMethod(method);
		} catch (CannotCompileException | NotFoundException e) {
			e.printStackTrace();
			throw new CannotBuildClassException(e.getMessage());
		}
	}

	private void implementHyalineDTOSetAttribute(ClassPool classPool,
			CtClass hyalineProxyClass) throws CannotBuildClassException {
		StringBuffer buffer = new StringBuffer();

		buffer.append("{");
		buffer.append(ReflectionUtils.class.getCanonicalName()
				+ ".injectField($1, this, $2);");
		buffer.append("}");

		String methodBody = buffer.toString();
		try {
			CtClass objectClass = classPool
					.get(Object.class.getCanonicalName());
			CtClass stringClass = classPool
					.get(String.class.getCanonicalName());
			CtMethod method = CtNewMethod.make(Modifier.PUBLIC,
					classPool.get("void"), "setAttribute", new CtClass[] {
							stringClass, objectClass }, new CtClass[] {},
					methodBody, hyalineProxyClass);

			hyalineProxyClass.addMethod(method);
		} catch (CannotCompileException | NotFoundException e) {
			e.printStackTrace();
			throw new CannotBuildClassException(e.getMessage());
		}
	}

	private void implementHyalineDTOGetTargetFieldName(ClassPool classPool,
			CtClass hyalineProxyClass, String targetFieldName)
			throws CannotBuildClassException {
		StringBuffer buffer = new StringBuffer();

		buffer.append("{");
		buffer.append("return \"").append(targetFieldName).append("\";");
		buffer.append("}");

		String methodBody = buffer.toString();
		try {
			CtMethod method = CtNewMethod.make(Modifier.PUBLIC,
					classPool.get(String.class.getCanonicalName()),
					"obtainTargetFieldName", new CtClass[] {},
					new CtClass[] {}, methodBody, hyalineProxyClass);

			hyalineProxyClass.addMethod(method);

		} catch (CannotCompileException | NotFoundException e) {
			e.printStackTrace();
			throw new CannotBuildClassException(e.getMessage());
		}

	}

	private void setSuperClassAndImplementHyalineDTO(ClassPool classPool,
			Class<?> entityClass, CtClass hyalineProxyClass)
			throws CannotBuildClassException {
		// avoid NotFoundException if possible
		CtClass clazz = null;
		CtClass hyalineDTO = null;
		try {
			clazz = classPool.get(entityClass.getName());
			hyalineDTO = classPool.get(Proto.class.getCanonicalName());
		} catch (NotFoundException e) {
			classPool.appendClassPath(new ClassClassPath(entityClass));
			classPool.appendClassPath(new ClassClassPath(Proto.class));
			try {
				clazz = classPool.get(entityClass.getName());
				hyalineDTO = classPool.get(Proto.class.getCanonicalName());
			} catch (NotFoundException e1) {
				throw new CannotBuildClassException(e1.getMessage());
			}
		}

		try {
			hyalineProxyClass.setSuperclass(clazz);
			hyalineProxyClass.addInterface(hyalineDTO);
		} catch (CannotCompileException e1) {
			throw new CannotBuildClassException(e1.getMessage());
		}
	}

	private void handleFieldsDescriptions(ProtoDescription description,
			String protoTemplateFieldName, ClassPool classPool,
			CtClass hyalineProtoClass, ConstPool constpool)
			throws CannotCompileException, CannotBuildClassException {
		// Here I create the necessary fields
		for (FieldDescription field : description.getFields().values()) {
			int modifiers = field.getField().getModifiers();

			// Ignore target field if we are proxing a proto (i.e. a hyaline
			// proto has been passed to another hyaline call)
			if (!field.getField().getName()
					.startsWith(PROTO_TEMPLATE_NAME_PREFIX)) {
				// Check the field is private or, if not private, it is not
				// final
				if (Modifier.isPrivate(modifiers)
						|| ((!Modifier.isPrivate(modifiers)) && (!Modifier
								.isFinal(modifiers)))) {
					CtField ctField = createFieldFromDescription(classPool,
							constpool, field, hyalineProtoClass);

					hyalineProtoClass.addField(ctField);

					// Generate a getter and a setter for fields defined in
					// template
					if (field.isFromTemplate()) {
						CtMethod getter = createGetter(field,
								protoTemplateFieldName, hyalineProtoClass);
						hyalineProtoClass.addMethod(getter);
						CtMethod setter = createSetter(field,
								protoTemplateFieldName, hyalineProtoClass);
						hyalineProtoClass.addMethod(setter);
					}
				}
			}
		}
	}

	private void handleMethodDescriptions(ProtoDescription description,
			String protoTemplateFieldName, ClassPool classPool,
			CtClass hyalineProtoClass, ConstPool constpool)
			throws CannotCompileException, CannotBuildClassException {
		for (MethodDescription method : description.getMethods().values()) {
			String methodName = method.getMethod().getName();
			CtMethod ctMethod = null;
			if (!method.isFromTemplate()) {
				// check the method is not final
				if (!Modifier.isFinal(method.getMethod().getModifiers())) {
					// for inherited methods, handle only getters and setters
					if (methodName.startsWith("get")
							|| methodName.startsWith("set")
							|| methodName.startsWith("is")) {

						try {
							ctMethod = createAccessorFromDescription(classPool,
									protoTemplateFieldName, constpool, method,
									hyalineProtoClass, description);

						} catch (FieldNotFoundException e) {
							e.printStackTrace();
						}
					}
				}
			} else {
				ctMethod = createProtoTemplateInvokerMethod(classPool,
						protoTemplateFieldName, constpool, method,
						hyalineProtoClass, description);
			}

			if (ctMethod != null) {
				// could generate the method, so add it to the class
				hyalineProtoClass.addMethod(ctMethod);
			}
		}
	}

	private CtMethod createProtoTemplateInvokerMethod(ClassPool classPool,
			String protoTemplateFieldName, ConstPool constpool,
			MethodDescription methodDescription, CtClass hyalineProtoClass,
			ProtoDescription description) {
		StringBuffer buffer = new StringBuffer("{");
		Method m = methodDescription.getMethod();

		// create parameterTypes and args arrays for reflective call
		int numberOfArgs = m.getParameterTypes().length;
		buffer.append("Class[] parameterTypes = new Class[")
				.append(numberOfArgs).append("];");
		buffer.append("Object[] args = new Object[").append(numberOfArgs)
				.append("];");
		for (int i = 0; i < numberOfArgs; i++) {
			// fill types and args arrays
			Class<?> paramType = m.getParameterTypes()[i];
			buffer.append("parameterTypes[").append(i).append("] = ")
					.append(paramType.getCanonicalName()).append(".class;");
			buffer.append("args[").append(i).append("] = $").append(i + 1)
					.append(";");
		}
		// if return type is not void, assign result to an object to be returned
		if (!m.getReturnType().equals(Void.class)
				&& !m.getReturnType().getCanonicalName().endsWith("void")) {
			buffer.append("Object result = ");
		}
		// invoke the proto template method reflectively
		buffer.append(ReflectionUtils.class.getCanonicalName()).append(
				".invokeMethod(this.");
		buffer.append(protoTemplateFieldName).append(", \"")
				.append(m.getName()).append("\", parameterTypes, args)");
		// if return type is not void, cast the result to such type and return
		// it
		if (!m.getReturnType().equals(Void.class)
				&& !m.getReturnType().getCanonicalName().endsWith("void")) {
			buffer.append("return (")
					.append(m.getReturnType().getCanonicalName())
					.append(") result;");
		}
		buffer.append("}");
		CtMethod method = null;
		try {
			CtClass[] exceptions = new CtClass[m.getExceptionTypes().length];
			CtClass[] parameters = new CtClass[numberOfArgs];
			for (int i = 0; i < numberOfArgs; i++) {
				parameters[i] = classPool.get(m.getParameterTypes()[i]
						.getCanonicalName());
			}
			for (int i = 0; i < m.getExceptionTypes().length; i++) {
				exceptions[i] = classPool.get(m.getExceptionTypes()[i]
						.getCanonicalName());
			}
			method = CtNewMethod.make(m.getModifiers(),
					classPool.get(m.getReturnType().getCanonicalName()),
					m.getName(), parameters, exceptions, buffer.toString(),
					hyalineProtoClass);
		} catch (CannotCompileException | NotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (method != null) {
			// finally copy annotations
			try {
				copyAnnotationsToCtMethod(constpool, methodDescription, method);
			} catch (CannotBuildClassException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return method;
	}

	private CtMethod createAccessorFromDescription(ClassPool classPool,
			String protoTemplateFieldName, ConstPool constpool,
			MethodDescription method, CtClass hyalineProtoClass,
			ProtoDescription description) throws FieldNotFoundException,
			CannotCompileException, CannotBuildClassException {
		String methodName = method.getMethod().getName();

		// reconstruct field to be accessed based on method name
		String fieldName = null;
		if (methodName.startsWith("is")) {
			fieldName = methodName.substring(2, 3).toLowerCase()
					+ methodName.substring(3);
		} else {
			fieldName = methodName.substring(3, 4).toLowerCase()
					+ methodName.substring(4);
		}

		FieldDescription field = description.getField(fieldName);
		CtMethod ctMethod = null;

		// if it is a proto, don't create a getter or a setter
		if (field != null
				&& !field.getField().getName()
						.startsWith(PROTO_TEMPLATE_NAME_PREFIX)) {
			// the method name can be connected to a field name
			// create a getter or a setter based on method name
			if (methodName.startsWith("is") || methodName.startsWith("get")) {
				ctMethod = createGetter(field, protoTemplateFieldName,
						hyalineProtoClass);
			} else {
				ctMethod = createSetter(field, protoTemplateFieldName,
						hyalineProtoClass);
			}
			// finally copy annotations
			copyAnnotationsToCtMethod(constpool, method, ctMethod);
		} else {
			// cannot find correlation between this method and a field name.
			// Ignore this method since it will be inherited by the proto.
		}

		return ctMethod;
	}

	private void copyAnnotationsToCtMethod(ConstPool constpool,
			MethodDescription method, CtMethod ctMethod)
			throws CannotBuildClassException {
		if (method.getAnnotations() != null
				&& method.getAnnotations().size() > 0) {
			AnnotationsAttribute attr = new AnnotationsAttribute(constpool,
					AnnotationsAttribute.visibleTag);
			for (java.lang.annotation.Annotation annotation : method
					.getAnnotations()) {
				Annotation annotationCopy = JavassistUtils
						.createJavassistAnnotation(constpool, annotation);
				attr.addAnnotation(annotationCopy);
			}
			ctMethod.getMethodInfo().addAttribute(attr);
		}
	}

	private void addClassAnnotations(ProtoDescription description,
			CtClass hyalineProxyClass, ConstPool constpool)
			throws CannotBuildClassException {
		if (description.getAnnotations() != null) {
			AnnotationsAttribute attr = new AnnotationsAttribute(constpool,
					AnnotationsAttribute.visibleTag);
			for (java.lang.annotation.Annotation annotation : description
					.getAnnotations()) {
				Annotation annotationCopy = JavassistUtils
						.createJavassistAnnotation(constpool, annotation);
				attr.addAnnotation(annotationCopy);
			}
			hyalineProxyClass.getClassFile().addAttribute(attr);
		}
	}

	private CtField createFieldFromDescription(ClassPool classPool,
			ConstPool constpool, FieldDescription field,
			CtClass hyalineProxyClass) throws CannotCompileException,
			CannotBuildClassException {
		String fieldTypeName = field.getField().getType().getCanonicalName();
		CtField ctField = CtField.make("private " + fieldTypeName + " "
				+ field.getField().getName() + ";", hyalineProxyClass);
		if (field.getAnnotations() != null && field.getAnnotations().size() > 0) {
			AnnotationsAttribute attr = new AnnotationsAttribute(constpool,
					AnnotationsAttribute.visibleTag);
			for (java.lang.annotation.Annotation annotation : field
					.getAnnotations()) {
				Annotation annotationCopy = JavassistUtils
						.createJavassistAnnotation(constpool, annotation);
				attr.addAnnotation(annotationCopy);
			}
			ctField.getFieldInfo().addAttribute(attr);
		}
		return ctField;
	}

	private CtMethod createSetter(FieldDescription field,
			String protoTemplateFieldName, CtClass hyalineProtoClass)
			throws CannotCompileException, CannotBuildClassException {
		String fieldName = field.getField().getName();
		String methodName = "set" + fieldName.substring(0, 1).toUpperCase()
				+ fieldName.substring(1);
		StringBuffer buffer = new StringBuffer();
		buffer.append("{");
		if (field.isFromTemplate()) {
			// set the value on the prototype template instance
			buffer.append(ReflectionUtils.class.getCanonicalName())
					.append(".injectField(\"").append(fieldName).append("\", ")
					.append("this.").append(protoTemplateFieldName)
					.append(", $1);");
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
			classPool.appendClassPath(new ClassClassPath(field.getField()
					.getType()));
			try {
				parameter = classPool.get(field.getField().getType().getName());
			} catch (NotFoundException e2) {
				throw new CannotBuildClassException(e2.getMessage());
			}
		}
		CtMethod method = CtNewMethod.make(Modifier.PUBLIC, returnType,
				methodName, new CtClass[] { parameter }, new CtClass[] {},
				methodBody, hyalineProtoClass);
		return method;
	}

	private CtMethod createGetter(FieldDescription field,
			String protoTemplateFieldName, CtClass hyalineProtoClass)
			throws CannotCompileException, CannotBuildClassException {
		String fieldName = field.getField().getName();
		String methodName = null;
		String prefix = null;
		String fieldTypeName = field.getField().getType().getName();
		if (fieldTypeName.equals("boolean")) {
			prefix = "is";
		} else {
			prefix = "get";
		}
		methodName = prefix + fieldName.substring(0, 1).toUpperCase()
				+ fieldName.substring(1);
		StringBuffer buffer = new StringBuffer();
		buffer.append("{");

		// if the field has been defined in the proto template, get the value
		// from there,
		// set it in the prototype
		if (field.isFromTemplate()) {
			buffer.append("this.").append(fieldName).append(" = ");
			// cast returned value
			buffer.append("(")
					.append(field.getField().getType().getCanonicalName())
					.append(")");
			// reflectively get the field value
			buffer.append(ReflectionUtils.class.getCanonicalName())
					.append(".getFieldValue(\"").append(fieldName)
					.append("\", ").append("this.")
					.append(protoTemplateFieldName).append(");");
		}
		// finally return the value
		buffer.append("return this.").append(fieldName).append(";}");
		ClassPool classPool = ClassPool.getDefault();
		CtClass returnType = null;
		try {
			returnType = classPool.get(field.getField().getType().getName());
		} catch (NotFoundException e) {
			classPool.appendClassPath(new ClassClassPath(field.getField()
					.getType()));
			try {
				returnType = classPool
						.get(field.getField().getType().getName());
			} catch (NotFoundException e1) {
				throw new CannotBuildClassException(e1.getMessage());
			}
		}
		String methodBody = buffer.toString();
		CtMethod method = CtNewMethod.make(Modifier.PUBLIC, returnType,
				methodName, new CtClass[] {}, new CtClass[] {}, methodBody,
				hyalineProtoClass);
		return method;
	}

}
