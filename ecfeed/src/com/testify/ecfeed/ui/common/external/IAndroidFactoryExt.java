/*******************************************************************************
 * Copyright (c) 2015 Testify AS..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/

package com.testify.ecfeed.ui.common.external;

import com.testify.ecfeed.model.MethodNode;

public interface IAndroidFactoryExt {

	IDeviceCheckerExt createDeviceChecker();

	IApkInstallerExt createApkInstaller();

	ITestMethodInvokerExt createTestMethodInvoker(String androidRunner);

	IImplementerExt createCommonImplementer(
			String baseRunner, String projectPath, IClassImplementHelper classImplementHelper);

	IImplementerExt createUserClassImplementer(
			String projectPath,	String thePackage, 
			String classNameWithoutExtension, IClassImplementHelper classImplementHelper);

	IImplementerExt createAndroidMethodImplementer(
			MethodNode methodNode, IMethodImplementHelper methodImplementHelper);	
}
