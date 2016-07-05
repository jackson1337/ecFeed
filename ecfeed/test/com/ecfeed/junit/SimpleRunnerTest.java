/*******************************************************************************
 *
 * Copyright (c) 2016 ecFeed AS.                                                
 * All rights reserved. This program and the accompanying materials              
 * are made available under the terms of the Eclipse Public License v1.0         
 * which accompanies this distribution, and is available at                      
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 *******************************************************************************/

package com.ecfeed.junit;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.ecfeed.junit.SimpleRunner;

@RunWith(SimpleRunner.class)
public class SimpleRunnerTest{
	
	@Test
	public void simpleTest1(){
		System.out.println("function simpleTest1");
	}
}
