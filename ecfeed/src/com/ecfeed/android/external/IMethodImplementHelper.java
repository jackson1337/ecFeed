/*******************************************************************************
 *
 * Copyright (c) 2016 ecFeed AS.                                                
 * All rights reserved. This program and the accompanying materials              
 * are made available under the terms of the Eclipse Public License v1.0         
 * which accompanies this distribution, and is available at                      
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 *******************************************************************************/

package com.ecfeed.android.external;

public interface IMethodImplementHelper {

	void createMethod(final String methodContent);
	void createImport(final String type);
	void commitChanges();
	boolean methodDefinitionImplemented();
}
