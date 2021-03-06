/*******************************************************************************
 *
 * Copyright (c) 2016 ecFeed AS.                                                
 * All rights reserved. This program and the accompanying materials              
 * are made available under the terms of the Eclipse Public License v1.0         
 * which accompanies this distribution, and is available at                      
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 *******************************************************************************/

package com.ecfeed.utils;

import java.io.FileOutputStream;
import java.io.OutputStream;

import com.ecfeed.core.model.ModelVersionDistributor;
import com.ecfeed.core.model.RootNode;
import com.ecfeed.core.serialization.IModelSerializer;
import com.ecfeed.core.serialization.ect.EctSerializer;
import com.ecfeed.core.utils.DiskFileHelper;
import com.ecfeed.core.utils.ExceptionHelper;
import com.ecfeed.core.utils.StreamHelper;

public class EctFileHelper {

	public static void createNewFile(String pathWithFileName) {
		DiskFileHelper.createNewFile(pathWithFileName);
		FileOutputStream outputStream = StreamHelper.requireCreateFileOutputStream(pathWithFileName);

		String fileName = DiskFileHelper.extractFileName(pathWithFileName);
		String modelName = DiskFileHelper.extractFileNameWithoutExtension(fileName);
		serializeEmptyModel(modelName, outputStream);		
	}

	public static void serializeEmptyModel(String modelName, OutputStream outputStream) {
		RootNode model = new RootNode(modelName, ModelVersionDistributor.getCurrentSoftwareVersion());

		IModelSerializer serializer = 
				new EctSerializer(outputStream, ModelVersionDistributor.getCurrentSoftwareVersion());
		try {
			serializer.serialize(model);
		} catch (Exception e) {
			final String CAN_NOT_SERIALIZE = "Can not serialize empty model."; 
			ExceptionHelper.reportRuntimeException(CAN_NOT_SERIALIZE + " " + e.getMessage());
		}
	}

}
