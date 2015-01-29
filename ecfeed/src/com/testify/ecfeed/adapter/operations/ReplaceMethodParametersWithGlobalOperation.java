package com.testify.ecfeed.adapter.operations;

import java.util.List;

import com.testify.ecfeed.adapter.ITypeAdapterProvider;
import com.testify.ecfeed.adapter.ModelOperationException;
import com.testify.ecfeed.model.ConstraintNode;
import com.testify.ecfeed.model.GlobalParameterNode;
import com.testify.ecfeed.model.GlobalParametersParentNode;
import com.testify.ecfeed.model.MethodNode;
import com.testify.ecfeed.model.MethodParameterNode;
import com.testify.ecfeed.model.TestCaseNode;

public class ReplaceMethodParametersWithGlobalOperation extends BulkOperation{

	private class ReplaceParameterWithLink extends BulkOperation{

		public ReplaceParameterWithLink(MethodParameterNode target, GlobalParametersParentNode parent, ITypeAdapterProvider adapterProvider) {
			super(OperationNames.REPLACE_PARAMETER_WITH_LINK, true);
			MethodNode method = target.getMethod();
			GlobalParameterNode global = new GlobalParameterNode(target);
			addOperation(new GenericOperationAddParameter(parent, global));
			addOperation(new MethodParameterOperationSetLink(target, global));
			addOperation(new MethodParameterOperationSetLinked(target, true));
			for(ConstraintNode constraint : method.getConstraintNodes()){
				if(constraint.mentions(target)){
					ConstraintNode copy = constraint.getCopy();
//					addOperation(new MethodOperationRemoveConstraint(method, constraint));
					addOperation(new MethodOperationAddConstraint(method, copy, constraint.getIndex()));
				}
			}
			for(TestCaseNode tc : method.getTestCases()){
				TestCaseNode copy = tc.getCopy();
				addOperation(new MethodOperationAddTestCase(method, copy, adapterProvider, tc.getIndex()));
			}
		}

		@Override
		public void execute() throws ModelOperationException{
			try {
				super.execute();
			} catch (ModelOperationException e) {
				throw e;
			}
		}

	}

	public ReplaceMethodParametersWithGlobalOperation(GlobalParametersParentNode parent, List<MethodParameterNode> originals, ITypeAdapterProvider adapterProvider){
		super(OperationNames.REPLACE_PARAMETERS, false);
		for(MethodParameterNode parameter : originals){
			addOperation(new ReplaceParameterWithLink(parameter, parent, adapterProvider));
		}
	}

}
