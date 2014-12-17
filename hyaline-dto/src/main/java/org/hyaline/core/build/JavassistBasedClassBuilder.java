package org.hyaline.core.build;

import java.lang.reflect.InvocationTargetException;
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
import javassist.bytecode.annotation.BooleanMemberValue;
import javassist.bytecode.annotation.ByteMemberValue;
import javassist.bytecode.annotation.CharMemberValue;
import javassist.bytecode.annotation.DoubleMemberValue;
import javassist.bytecode.annotation.FloatMemberValue;
import javassist.bytecode.annotation.IntegerMemberValue;
import javassist.bytecode.annotation.LongMemberValue;
import javassist.bytecode.annotation.MemberValue;
import javassist.bytecode.annotation.ShortMemberValue;
import javassist.bytecode.annotation.StringMemberValue;

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
			throw new CannotBuildClassException(e.getMessage());
		}
		try {
			return hyalineProxyClass.toClass();
		} catch (CannotCompileException e) {
			e.printStackTrace();
			throw new CannotBuildClassException(e.getMessage());
		}
	}

	private CtField createFieldFromDescription(ClassPool classPool,
			ConstPool constpool, FieldDescription field)
			throws CannotCompileException, NotFoundException,
			CannotBuildClassException {
		CtField ctField = CtField.make("private "
				+ field.getField().getClass().getName() + " "
				+ field.getField().getName() + ";",
				classPool.get(field.getField().getClass().getName()));
		if (field.getAnnotations() != null && field.getAnnotations().size() > 0) {
			AnnotationsAttribute attr = new AnnotationsAttribute(constpool,
					AnnotationsAttribute.visibleTag);
			for (java.lang.annotation.Annotation annotation : field
					.getAnnotations()) {
				Annotation annotationCopy = JavassistUtils.createJavassistAnnotation(
						constpool, annotation);
				attr.addAnnotation(annotationCopy);
			}
			ctField.getFieldInfo().addAttribute(attr);
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
