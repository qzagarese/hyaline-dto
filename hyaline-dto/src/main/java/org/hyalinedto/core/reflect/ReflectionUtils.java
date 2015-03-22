package org.hyalinedto.core.reflect;

import java.lang.reflect.Field;

public class ReflectionUtils {

	public static Object getFieldValue(Field field, Object instance) throws IllegalArgumentException,
	        IllegalAccessException {
		Object value = null;
		boolean accessible = field.isAccessible();
		field.setAccessible(true);
		value = field.get(instance);
		field.setAccessible(accessible);
		return value;
	}

	public static void injectField(Field field, Object instance, Object value) throws IllegalArgumentException,
	        IllegalAccessException {
		boolean accessible = field.isAccessible();
		field.setAccessible(true);
		field.set(instance, value);
		field.setAccessible(accessible);
	}

	public static Object getFieldValue(String field, Object instance) {
		Field f = null;
		Object value = null;
		try {
			f = instance.getClass().getDeclaredField(field);
			value = getFieldValue(f, instance);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			// Field does not exist or cannot be accessed, so return null
		}
		return value;
	}

	public static void injectField(String field, Object instance, Object value) {
		Field f = null;
		try {
			f = instance.getClass().getDeclaredField(field);
			injectField(f, instance, value);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			// Field does not exist or cannot be accessed, just ignore
		}
	}

	public static boolean isFieldInitialized(Object dto, Field f) {
		boolean accessible = f.isAccessible();
		f.setAccessible(true);
		Object value = null;
		boolean initialized = false;
		try {
			value = f.get(dto);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
		f.setAccessible(accessible);
		switch (f.getType().getName()) {
		case "byte":
			initialized = (byte) value != 0;
			break;
		case "short":
			initialized = (short) value != 0;
			break;
		case "int":
			initialized = (int) value != 0;
			break;
		case "long":
			initialized = (long) value != 0L;
			break;
		case "float":
			initialized = (float) value != 0.0f;
			break;
		case "double":
			initialized = (double) value != 0.0d;
			break;
		case "char":
			initialized = (char) value != '\u0000';
			break;
		case "boolean":
			initialized = (boolean) value != false;
			break;
		default:
			initialized = value != null;
			break;
		}

		return initialized;
	}

}
