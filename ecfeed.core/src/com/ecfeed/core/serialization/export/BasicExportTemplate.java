/*******************************************************************************
 *
 * Copyright (c) 2016 ecFeed AS.                                                
 * All rights reserved. This program and the accompanying materials              
 * are made available under the terms of the Eclipse Public License v1.0         
 * which accompanies this distribution, and is available at                      
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 *******************************************************************************/
package com.ecfeed.core.serialization.export;

import com.ecfeed.core.model.MethodNode;

public class BasicExportTemplate extends AbstractExportTemplate {

	public BasicExportTemplate(MethodNode methodNode) {
		super(methodNode);
	}

	@Override
	public String createDefaultTemplateText() {

		return null;
	}

	@Override
	public String getFileExtension() {
		return null;
	}

	@Override 
	public String getTemplateFormat() {
		return null;
	}

}
