package com.testify.ecfeed.adapter.operations;

import java.util.ArrayList;
import java.util.List;

import com.testify.ecfeed.adapter.IModelOperation;
import com.testify.ecfeed.adapter.ModelOperationException;
import com.testify.ecfeed.model.ParameterNode;
import com.testify.ecfeed.model.MethodNode;
import com.testify.ecfeed.model.TestCaseNode;

public class MethodOperationRemoveParameter extends BulkOperation{

	private class RemoveParameterOperation extends AbstractModelOperation{

		private final MethodNode fTarget;
		private final ParameterNode fParameter;
		private int fOriginalIndex;
		private final ArrayList<TestCaseNode> fOriginalTestCases;

		private class ReverseOperation extends AbstractModelOperation{

			public ReverseOperation() {
				super("reverse " + MethodOperationRemoveParameter.this.getName());
			}

			@Override
			public void execute() throws ModelOperationException {
				fTarget.addParameter(fParameter, fOriginalIndex);
				fTarget.replaceTestCases(fOriginalTestCases);
				markModelUpdated();
			}

			@Override
			public IModelOperation reverseOperation() {
				return new RemoveParameterOperation(fTarget, fParameter);
			}
			
		}
		
		public RemoveParameterOperation(MethodNode target, ParameterNode parameter){
			super(OperationNames.REMOVE_PARAMETER);
			fTarget = target;
			fParameter = parameter;
			fOriginalTestCases = new ArrayList<TestCaseNode>(target.getTestCases());
			fOriginalIndex = parameter.getIndex();
		}

		@Override
		public void execute() throws ModelOperationException {
			if(validateNewSignature() == false){
				throw new ModelOperationException(Messages.METHOD_SIGNATURE_DUPLICATE_PROBLEM);
			}
			fOriginalIndex = fParameter.getIndex();
			fTarget.removeParameter(fParameter);
			for(TestCaseNode tc : fTarget.getTestCases()){
				tc.getTestData().remove(fOriginalIndex);
			}
			markModelUpdated();
		}

		@Override
		public IModelOperation reverseOperation() {
			return new ReverseOperation();
		}

		private boolean validateNewSignature() {
			List<String> types = fTarget.getParametersTypes();
			int index = fParameter.getIndex();
			types.remove(index);
			if(fTarget.getClassNode().getMethod(fTarget.getName(), types) != null){
				return false;
			}
			return true;
		}
	}

	public MethodOperationRemoveParameter(MethodNode target, ParameterNode parameter, boolean validate) {
		super(OperationNames.REMOVE_PARAMETER, true);
		addOperation(new RemoveParameterOperation(target, parameter));
		if(validate){
			addOperation(new MethodOperationMakeConsistent(target));
		}
	}
	public MethodOperationRemoveParameter(MethodNode target, ParameterNode parameter) {
		this(target, parameter, true);
	}
}
