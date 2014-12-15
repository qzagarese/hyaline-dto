package org.hyaline.core.build;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtNewConstructor;
import javassist.NotFoundException;

import org.hyaline.core.ClassBuilder;
import org.hyaline.core.InterfaceImplementationStrategy;
import org.hyaline.core.ProxyStrategy;
import org.hyaline.core.exception.CannotBuildClassException;
import org.hyaline.core.reflect.DTODescription;

public class JavassistBasedClassBuilder implements ClassBuilder {

	@Override
	public Class<?> buildClass(DTODescription description,
			ProxyStrategy strategy,
			InterfaceImplementationStrategy interfaceImplementationStrategy)
			throws CannotBuildClassException {
		ClassPool classPool = ClassPool.getDefault();
		Class<? extends Object> realClass = description.getType();
		CtClass clazz = classPool.makeClass(realClass.getSimpleName()
				+ "$HyalineProxy$" + System.currentTimeMillis());
		try {
			CtClass javassistRealClass = classPool.get(realClass.getName());

			clazz.setSuperclass(classPool.get(realClass.getName()));
			for (Class<?> interfaceType : description
					.getImplementedInterfaces()) {
				clazz.addInterface(ClassPool.getDefault().get(
						interfaceType.getCanonicalName()));
			}

		} catch (NotFoundException | CannotCompileException e) {
			throw new CannotBuildClassException();
		}
		return null;
	}

	
	private void createConstructor(CtClass clazz) {
		CtConstructor c = null;
		CtClass[] parameters = new CtClass[1];
		ClassPool classPool = ClassPool.getDefault();
		try {
			parameters[0] = classPool
					.get("it.unisannio.ding.dyno4ws.proxy.ProxyContext");
		} catch (NotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		CtClass[] exceptions = new CtClass[0];
		String body = "{" + "		this.context = $1;"
				+ "		this.context.setProxy(this);"
				+ "		this.context.getInterceptor().onConstruct(this.context);"
				+ "}";
		try {
			c = CtNewConstructor.make(parameters, exceptions, body, clazz);
			clazz.addConstructor(c);
		} catch (CannotCompileException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
