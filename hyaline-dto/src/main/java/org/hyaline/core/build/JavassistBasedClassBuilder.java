package org.hyaline.core.build;

import java.lang.reflect.Method;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.Annotation;

import org.hyaline.core.ClassBuilder;
import org.hyaline.core.InterfaceImplementationStrategy;
import org.hyaline.core.ProxyStrategy;
import org.hyaline.core.exception.CannotBuildClassException;
import org.hyaline.core.reflect.DTODescription;
import org.hyaline.core.reflect.FieldDescription;

public class JavassistBasedClassBuilder implements ClassBuilder {

	@Override
	public Class<?> buildClass(DTODescription description,
			ProxyStrategy strategy,
			InterfaceImplementationStrategy interfaceImplementationStrategy)
			throws CannotBuildClassException {
		ClassPool classPool = ClassPool.getDefault();
		Class<? extends Object> entityClass = description.getType();
		CtClass hyalineProxyClass = classPool.makeClass(entityClass
				.getSimpleName()
				+ "$HyalineProxy$"
				+ System.currentTimeMillis());
		try {

			hyalineProxyClass
					.setSuperclass(classPool.get(entityClass.getName()));
			for (Class<?> interfaceType : description
					.getImplementedInterfaces()) {
				hyalineProxyClass.addInterface(ClassPool.getDefault().get(
						interfaceType.getName()));
			}

			ClassFile ccFile = hyalineProxyClass.getClassFile();
			ConstPool constpool = ccFile.getConstPool();

			for (FieldDescription field : description.getFields().values()) {
				CtField ctField = createFieldFromDescription(classPool,
						constpool, field);
				
				hyalineProxyClass.addField(ctField);
				if (field.isFromTemplate()) {

					CtMethod getter = createGetter(field);
					CtMethod setter = createSetter(field);
				}
			}

		} catch (NotFoundException | CannotCompileException e) {
			throw new CannotBuildClassException();
		}
		try {
			return hyalineProxyClass.toClass();
		} catch (CannotCompileException e) {
			e.printStackTrace();
			throw new CannotBuildClassException();
		}
	}

	private CtField createFieldFromDescription(ClassPool classPool,
			ConstPool constpool, FieldDescription field)
			throws CannotCompileException, NotFoundException {
		CtField ctField = CtField.make("private " + field.getField().getClass().getName() + " " + field.getField().getName() + ";",
				classPool.get(field.getField().getClass().getName()));
		if (field.getAnnotations() != null
				&& field.getAnnotations().size() > 0) {
			AnnotationsAttribute attr = new AnnotationsAttribute(
					constpool, AnnotationsAttribute.visibleTag);
			for (java.lang.annotation.Annotation annotation : field.getAnnotations()) {
				Annotation annotationCopy = new Annotation(annotation.annotationType().getName(), constpool);
				for (Method m : annotation.annotationType().getDeclaredMethods()) {
					System.out.println(m.getName());
				}
			}
		}
		return ctField;
	}

	private CtMethod createSetter(FieldDescription field) {
		// TODO Auto-generated method stub
		return null;
	}

	private CtMethod createGetter(FieldDescription field) {
		// TODO Auto-generated method stub
		return null;
	}

}
