package org.hyaline.test;

import java.lang.reflect.Method;

import org.hyaline.core.InterfaceImplementationStrategy;
import org.hyaline.core.proxy.HyalineProxyImplementationStrategy;

public class ImplementationStrategyRunner {

	public static void main(String[] args) {

		Class<?> clazz = Person.class;
		InterfaceImplementationStrategy strategy = new HyalineProxyImplementationStrategy();

		for (Method m : clazz.getDeclaredMethods()) {
			System.out.println(strategy.getImplementationFor(new Person(), m));
		}

	}

}
