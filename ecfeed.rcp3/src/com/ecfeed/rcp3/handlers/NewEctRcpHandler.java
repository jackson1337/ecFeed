/*******************************************************************************
 * Copyright (c) 2015 Testify AS..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/

package com.ecfeed.rcp3.handlers;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.ecfeed.ui.handlers.NewEctHandler;


public class NewEctRcpHandler extends org.eclipse.core.commands.AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		NewEctHandler.execute();
		return null;
	}

}
