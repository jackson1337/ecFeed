/*******************************************************************************
 *
 * Copyright (c) 2016 ecFeed AS.                                                
 * All rights reserved. This program and the accompanying materials              
 * are made available under the terms of the Eclipse Public License v1.0         
 * which accompanies this distribution, and is available at                      
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 *******************************************************************************/

package com.ecfeed.core.adapter.java;

import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import com.ecfeed.core.model.AbstractParameterNode;
import com.ecfeed.core.model.ClassNode;
import com.ecfeed.core.model.MethodNode;
import com.ecfeed.core.model.ModelHelper;
import com.ecfeed.core.utils.JavaLanguageHelper;
import com.ecfeed.core.utils.JavaTypeHelper;

public class JavaUtils {

	public static boolean isValidTypeName(String name){
		if(name == null) return false;
		if(JavaTypeHelper.isJavaType(name)) return true;
		if(name.matches(AdapterConstants.REGEX_CLASS_NODE_NAME) == false) return false;
		StringTokenizer tokenizer = new StringTokenizer(name, ".");
		while(tokenizer.hasMoreTokens()){
			String segment = tokenizer.nextToken();
			if(JavaLanguageHelper.isValidJavaIdentifier(segment) == false){
				return false;
			}
		}
		return true;
	}

	public static List<String> enumValuesNames(URLClassLoader loader, String enumTypeName){
		List<String> values = new ArrayList<String>();
		try {
			Class<?> enumType = loader.loadClass(enumTypeName);
			if(enumType != null && enumType.isEnum()){
				for (Object object: enumType.getEnumConstants()) {
					values.add(((Enum<?>)object).name());
				}
			}
		} catch (ClassNotFoundException e) {
		}
		return values;
	}

	public static boolean isValidTestCaseName(String name) {
		return name.matches(AdapterConstants.REGEX_TEST_CASE_NODE_NAME);
	}

	public static boolean isValidConstraintName(String name) {
		return name.matches(AdapterConstants.REGEX_CONSTRAINT_NODE_NAME);
	}

	public static boolean validateTestCaseName(String name){
		return name.matches(AdapterConstants.REGEX_TEST_CASE_NODE_NAME);
	}

	public static List<String> getArgNames(MethodNode method) {
		List<String> result = new ArrayList<String>();
		for(AbstractParameterNode parameter : method.getParameters()){
			result.add(parameter.getName());
		}
		return result;
	}

	public static List<String> getArgTypes(MethodNode method) {
		List<String> result = new ArrayList<String>();
		for(AbstractParameterNode parameter : method.getParameters()){
			result.add(parameter.getType());
		}
		return result;
	}

	public static boolean validateMethodName(String name) {
		return validateMethodName(name, null);
	}

	public static boolean validateMethodName(String name, List<String> problems) {
		boolean valid = name.matches(AdapterConstants.REGEX_METHOD_NODE_NAME);
		valid &= Arrays.asList(AdapterConstants.JAVA_KEYWORDS).contains(name) == false;
		if(valid == false){
			if(problems != null){
				problems.add(Messages.METHOD_NAME_REGEX_PROBLEM);
			}
		}
		return valid;
	}

	public static String simplifiedToString(AbstractParameterNode parameter){
		String result = parameter.toString();
		String type = parameter.getType();
		result.replace(type, ModelHelper.convertToLocalName(type));
		return result;
	}

	public static String simplifiedToString(MethodNode method){
		String result = method.toString();
		for(AbstractParameterNode parameter : method.getParameters()){
			String type = parameter.getType();
			String newType = ModelHelper.convertToLocalName(type);
			result = result.replaceAll(type, newType);
		}
		return result;
	}

	public static boolean validateNewMethodSignature(ClassNode parent, String methodName, List<String> argTypes){
		return validateNewMethodSignature(parent, methodName, argTypes, null);
	}

	public static boolean validateNewMethodSignature(ClassNode parent, String methodName,
			List<String> argTypes, List<String> problems){
		boolean valid = JavaUtils.validateMethodName(methodName, problems);
		if(parent.getMethod(methodName, argTypes) != null){
			valid = false;
			if(problems != null){
				problems.add(Messages.METHOD_SIGNATURE_DUPLICATE_PROBLEM(parent.getName(), methodName));
			}
		}
		return valid;
	}
}
