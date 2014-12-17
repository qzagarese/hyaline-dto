package org.hyaline.core.build;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javassist.ClassPool;
import javassist.NotFoundException;
import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.AnnotationMemberValue;
import javassist.bytecode.annotation.ArrayMemberValue;
import javassist.bytecode.annotation.BooleanMemberValue;
import javassist.bytecode.annotation.ByteMemberValue;
import javassist.bytecode.annotation.CharMemberValue;
import javassist.bytecode.annotation.ClassMemberValue;
import javassist.bytecode.annotation.DoubleMemberValue;
import javassist.bytecode.annotation.EnumMemberValue;
import javassist.bytecode.annotation.FloatMemberValue;
import javassist.bytecode.annotation.IntegerMemberValue;
import javassist.bytecode.annotation.LongMemberValue;
import javassist.bytecode.annotation.MemberValue;
import javassist.bytecode.annotation.ShortMemberValue;
import javassist.bytecode.annotation.StringMemberValue;

import org.hyaline.core.exception.CannotBuildClassException;

public class JavassistUtils {

	public static Annotation createJavassistAnnotation(ConstPool constpool,
			java.lang.annotation.Annotation annotation)
			throws CannotBuildClassException {
		Annotation annotationCopy = new Annotation(annotation.annotationType()
				.getName(), constpool);
		for (Method m : annotation.annotationType().getDeclaredMethods()) {
			try {
				Object value = m.invoke(annotation);
				annotationCopy.addMemberValue(m.getName(),
						JavassistUtils.createMemberValue(value, constpool));
			} catch (IllegalAccessException | IllegalArgumentException
					| InvocationTargetException e) {
				e.printStackTrace();
				throw new CannotBuildClassException(
						"Could not copy info from annotation "
								+ annotation.annotationType().getName());
			}
		}
		return annotationCopy;
	}

	public static MemberValue createMemberValue(Object value,
			ConstPool constpool) {
		MemberValue member = null;

		if (value.getClass().isAnnotation()) {
			try {
				member = new AnnotationMemberValue(
						new Annotation(constpool, ClassPool.getDefault().get(
								value.getClass().getName())), constpool);
			} catch (NotFoundException e) {
				e.printStackTrace();
			}
		} else if (value.getClass().isArray()) {
			member = new ArrayMemberValue(createMemberValue(value.getClass(),
					constpool), constpool);
			Object[] valueArray = (Object[]) value;
			List<MemberValue> members = new ArrayList<MemberValue>();
			for (Object object : valueArray) {
				members.add(createMemberValue(object, constpool));
			}
			ArrayMemberValue arrayMemberValue = (ArrayMemberValue) member;
			arrayMemberValue.setValue(members.toArray(new MemberValue[] {}));
		} else if (value.getClass().isEnum()) {
			Enum<?> enumValue = (Enum<?>) value;
			int enumClassIndex = constpool.addUtf8Info(enumValue
					.getDeclaringClass().getName());
			int enumValueIndex = constpool.addUtf8Info(enumValue.name());
			member = new EnumMemberValue(enumClassIndex, enumValueIndex,
					constpool);
		} else {
			switch (value.getClass().getName()) {
			case "java.lang.Boolean":
				member = new BooleanMemberValue((Boolean) value, constpool);
				break;
			case "boolean":
				member = new BooleanMemberValue((boolean) value, constpool);
				break;
			case "java.lang.Integer":
				member = new IntegerMemberValue(constpool, (Integer) value);
				break;
			case "int":
				member = new IntegerMemberValue(constpool, (int) value);
				break;
			case "java.lang.Long":
				member = new LongMemberValue((Long) value, constpool);
				break;
			case "long":
				member = new LongMemberValue((long) value, constpool);
				break;
			case "java.lang.Short":
				member = new ShortMemberValue((Short) value, constpool);
				break;
			case "short":
				member = new ShortMemberValue((short) value, constpool);
				break;
			case "java.lang.Float":
				member = new FloatMemberValue((Float) value, constpool);
				break;
			case "float":
				member = new FloatMemberValue((float) value, constpool);
				break;
			case "java.lang.Double":
				member = new DoubleMemberValue((Double) value, constpool);
				break;
			case "double":
				member = new DoubleMemberValue((double) value, constpool);
				break;
			case "java.lang.String":
				member = new StringMemberValue((String) value, constpool);
				break;
			case "java.lang.Byte":
				member = new ByteMemberValue((Byte) value, constpool);
				break;
			case "byte":
				member = new ByteMemberValue((byte) value, constpool);
				break;
			case "java.lang.Character":
				member = new CharMemberValue((Character) value, constpool);
				break;
			case "char":
				member = new CharMemberValue((char) value, constpool);
				break;
			case "java.lang.Class":
				member = new ClassMemberValue(((Class<?>) value).getName(),
						constpool);
				break;
			}

		}
		return member;
	}
}
