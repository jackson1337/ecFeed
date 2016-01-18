/*******************************************************************************
 * Copyright (c) 2015 Testify AS..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Patryk Chamuczynski (p.chamuczynski(at)radytek.com) - initial implementation
 ******************************************************************************/

package com.testify.ecfeed.core.runner;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.testify.ecfeed.core.adapter.java.ChoiceValueParser;
import com.testify.ecfeed.core.adapter.java.Constants;
import com.testify.ecfeed.core.adapter.java.JavaUtils;
import com.testify.ecfeed.core.adapter.java.ModelClassLoader;
import com.testify.ecfeed.core.model.ChoiceNode;
import com.testify.ecfeed.core.model.ClassNode;
import com.testify.ecfeed.core.model.MethodNode;

public class JavaTestRunner {

	private ModelClassLoader fLoader;
	private MethodNode fTarget;
	private Class<?> fTestClass;
	private Method fTestMethod;
	private ITestMethodInvoker fTestMethodInvoker;

	public JavaTestRunner(ModelClassLoader loader, ITestMethodInvoker testMethodInvoker){
		fLoader = loader;
		fTestMethodInvoker = testMethodInvoker; 
	}

	public void setTarget(MethodNode target) throws RunnerException{
		fTarget = target;
		ClassNode classNode = fTarget.getClassNode();
		fTestClass = getTestClass(classNode.getName());
		fTestMethod = getTestMethod(fTestClass, fTarget);
	}

	public void runTestCase(List<ChoiceNode> testData) throws RunnerException{

		validateTestData(testData);

		Object instance = null;

		if (!fTestMethodInvoker.isRemote())	{
			try {
				instance = fTestClass.newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				RunnerException.report(
						Messages.CANNOT_INVOKE_TEST_METHOD(
								fTarget.toString(), 
								testData.toString(), 
								e.getMessage()));
			}
		}

		String className = fTestClass.getName();
		Object[] arguments = getArguments(testData);

		try {
			fTestMethodInvoker.invoke(fTestMethod, className, instance, arguments, testData.toString());
		} catch (Exception e) {
			RunnerException.report(e.getMessage());
		}
	}

	protected Method getTestMethod(Class<?> testClass, MethodNode methodModel) throws RunnerException {
		for(Method method : testClass.getMethods()){
			if(isModel(method, methodModel)){
				return method;
			}
		}
		RunnerException.report(Messages.METHOD_NOT_FOUND(methodModel.toString()));
		return null;
	}

	protected boolean isModel(Method method, MethodNode methodModel) {
		String methodName = method.getName();
		Class<?>[] parameterTypes = method.getParameterTypes();
		List<String> types = new ArrayList<String>();
		for(Class<?> type : parameterTypes){
			types.add(JavaUtils.getTypeName(type.getCanonicalName()));
		}
		return methodName.equals(methodModel.getName()) && types.equals(methodModel.getParametersTypes());
	}

	protected Object[] getArguments(List<ChoiceNode> testData) throws RunnerException {
		List<Object> args = new ArrayList<Object>();
		ChoiceValueParser parser = new ChoiceValueParser(fLoader);
		for(ChoiceNode p : testData){
			Object value = parser.parseValue(p);
			if(value == null){
				String type = p.getParameter().getType();
				//check if null value acceptable
				if(JavaUtils.isString(type) || JavaUtils.isUserType(type)){
					if(p.getValueString().equals(Constants.VALUE_REPRESENTATION_NULL) == false){
						RunnerException.report(Messages.CANNOT_PARSE_PARAMETER(type, p.getValueString()));
					}
				}
			}

			args.add(value);
		}
		return args.toArray();
	}

	private void validateTestData(List<ChoiceNode> testData) throws RunnerException {
		List<String> dataTypes = new ArrayList<String>();
		for(ChoiceNode parameter : testData){
			dataTypes.add(parameter.getParameter().getType());
		}
		if(dataTypes.equals(fTarget.getParametersTypes()) == false){
			RunnerException.report(Messages.WRONG_TEST_METHOD_SIGNATURE(fTarget.toString()));
		}
	}

	private Class<?> getTestClass(String qualifiedName) throws RunnerException {
		Class<?> testClass = fLoader.loadClass(qualifiedName);
		if(testClass == null){
			RunnerException.report(Messages.CANNOT_LOAD_CLASS(qualifiedName));
		}
		return testClass;
	}

}