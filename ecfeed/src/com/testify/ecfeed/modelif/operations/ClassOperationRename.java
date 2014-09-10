package com.testify.ecfeed.modelif.operations;

import com.testify.ecfeed.model.ClassNode;
import com.testify.ecfeed.model.GenericNode;
import com.testify.ecfeed.modelif.IModelOperation;
import com.testify.ecfeed.modelif.ModelIfException;
import com.testify.ecfeed.modelif.java.Constants;
import com.testify.ecfeed.modelif.java.JavaUtils;

public class ClassOperationRename extends AbstractOperationRename {

	public ClassOperationRename(GenericNode target, String newName) {
		super(target, newName);
	}

	@Override
	public void execute() throws ModelIfException {
		if(fNewName.matches(Constants.REGEX_CLASS_NODE_NAME) == false){
			throw new ModelIfException(Messages.CLASS_NAME_REGEX_PROBLEM);
		}
		
		for(String token : fNewName.split("\\.")){
			if(JavaUtils.isJavaKeyword(token)){
				throw new ModelIfException(Messages.CLASS_NAME_CONTAINS_KEYWORD_PROBLEM);
			}
		}
		
		ClassNode target = (ClassNode)fTarget;
		if(target.getRoot().getClassModel(fNewName) != null){
			throw new ModelIfException(Messages.CLASS_NAME_DUPLICATE_PROBLEM);
		}
		target.setName(fNewName);
	}

	@Override
	public IModelOperation reverseOperation() {
		return new ClassOperationRename(fTarget, fOriginalName);
	}

}