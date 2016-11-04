/*******************************************************************************
 *
 * Copyright (c) 2016 ecFeed AS.                                                
 * All rights reserved. This program and the accompanying materials              
 * are made available under the terms of the Eclipse Public License v1.0         
 * which accompanies this distribution, and is available at                      
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 *******************************************************************************/

package com.ecfeed.core.model;

import com.ecfeed.core.utils.ExceptionHelper;

public class ModelConverter {

	private static final String INVALID_MODEL_VERSION = "Invalid model version.";

	public static RootNode convertToCurrentVersion(RootNode model) {
		int currentVersion = ModelVersionDistributor.getCurrentVersion();
		int modelVersion = model.getModelVersion();

		for (int version = modelVersion; version < currentVersion; version++) {
			model = convertToNextVersion(model, version);
		}
		return model;
	}

	private static RootNode convertToNextVersion(RootNode model, int fromVersion) {
		switch (fromVersion) {
		case 0:
			model = convertFrom0To1(model);
			break;
		case 1:
			model = convertFrom1To2(model);
			break;			
		default:
			ExceptionHelper.reportRuntimeException(INVALID_MODEL_VERSION);
			break;
		}

		model.setVersion(fromVersion+1);
		return model;
	}

	private static RootNode convertFrom0To1(RootNode model) {
		return model; // no changes in model internal structure, just serialization and parsing differs  
	}

	private static RootNode convertFrom1To2(RootNode model) {
		return model; // no changes in model internal structure  
	}	
}
