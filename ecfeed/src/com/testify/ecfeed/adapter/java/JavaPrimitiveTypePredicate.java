package com.testify.ecfeed.adapter.java;

import com.testify.ecfeed.adapter.IPrimitiveTypePredicate;

public class JavaPrimitiveTypePredicate implements IPrimitiveTypePredicate{
	@Override
	public boolean isPrimitive(String type){
		return JavaUtils.isPrimitive(type);
	}
}
