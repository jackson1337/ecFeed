package com.testify.ecfeed.modelif.java;

import com.testify.ecfeed.model.ClassNode;
import com.testify.ecfeed.model.MethodNode;

public class JavaClassUtils {
	public static ImplementationStatus implementationStatus(ClassNode classNode){
		if(classDefinitionImplemented(classNode) == false){
			return ImplementationStatus.NOT_IMPLEMENTED;
		}
		for(MethodNode method : classNode.getMethods()){
			if(JavaMethodUtils.implementationStatus(method) != ImplementationStatus.IMPLEMENTED){
				return ImplementationStatus.PARTIALLY_IMPLEMENTED;
			}
		}
		return ImplementationStatus.IMPLEMENTED;
	}
	
	public static String getLocalName(ClassNode classNode){
		return getLocalName(classNode.getName());
	}

	public static String getLocalName(String qualifiedName){
		int lastDotIndex = qualifiedName.lastIndexOf('.');
		return (lastDotIndex == -1)?qualifiedName: qualifiedName.substring(lastDotIndex + 1);
	}

	public static String getQualifiedName(ClassNode classNode){
		return classNode.getName();
	}

	public static String getPackageName(ClassNode classNode){
		return getPackageName(classNode.getName());
	}
	
	public static String getQualifiedName(String packageName, String localName){
		return packageName + "." + localName;
	}

	public static String getPackageName(String qualifiedName){
		int lastDotIndex = qualifiedName.lastIndexOf('.');
		return (lastDotIndex == -1)? "" : qualifiedName.substring(0, lastDotIndex);
	}

	private static boolean classDefinitionImplemented(ClassNode classNode) {
		return (LoaderProvider.loadClass(LoaderProvider.getClassLoader(true, null), classNode.getQualifiedName()) != null);
	}

}