package com.testify.ecfeed.ui.modelif;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.widgets.Display;

import com.testify.ecfeed.adapter.IModelOperation;
import com.testify.ecfeed.adapter.java.JavaUtils;
import com.testify.ecfeed.adapter.operations.AbstractParameterOperationSetType;
import com.testify.ecfeed.adapter.operations.AbstractParameterOperationSetTypeWithChoices;
import com.testify.ecfeed.model.AbstractParameterNode;
import com.testify.ecfeed.model.ChoiceNode;
import com.testify.ecfeed.ui.common.EclipseModelBuilder;
import com.testify.ecfeed.ui.common.JavaModelAnalyser;
import com.testify.ecfeed.ui.common.Messages;
import com.testify.ecfeed.ui.dialogs.TestClassSelectionDialog;
import com.testify.ecfeed.ui.dialogs.UserTypeSelectionDialog;

public abstract class AbstractParameterInterface extends ChoicesParentInterface {

	public AbstractParameterInterface(IModelUpdateContext updateContext) {
		super(updateContext);
	}

	public String getType() {
		return getTarget().getType();
	}

	public boolean importType(){
		TestClassSelectionDialog dialog = new UserTypeSelectionDialog(Display.getDefault().getActiveShell());

		if (dialog.open() == IDialogConstants.OK_ID) {
			IType selectedEnum = (IType)dialog.getFirstResult();
			String newType = selectedEnum.getFullyQualifiedName();
			List<ChoiceNode> defaultChoices = new EclipseModelBuilder().defaultChoices(newType);
			IModelOperation operation = new AbstractParameterOperationSetTypeWithChoices(setTypeOperation(newType), getTarget(), newType, defaultChoices, getAdapterProvider());
			return execute(operation, Messages.DIALOG_SET_PARAMETER_TYPE_PROBLEM_TITLE);
		}
		return false;
	}

	public static boolean hasLimitedValuesSet(String type) {
		return !isPrimitive(type) || isBoolean(type);
	}

	public static boolean hasLimitedValuesSet(AbstractParameterNode parameter) {
		return hasLimitedValuesSet(parameter.getType());
	}

	public static boolean isPrimitive(String type) {
		return Arrays.asList(JavaUtils.supportedPrimitiveTypes()).contains(type);
	}

	public static boolean isUserType(String type) {
		return !isPrimitive(type);
	}

	public static boolean isBoolean(String type){
		return type.equals(JavaUtils.getBooleanTypeName());
	}

	public static List<String> getSpecialValues(String type) {
		return new EclipseModelBuilder().getSpecialValues(type);
	}

	public static String[] supportedPrimitiveTypes() {
		return JavaUtils.supportedPrimitiveTypes();
	}

	@Override
	public boolean goToImplementationEnabled(){
		if(JavaUtils.isUserType(getTarget().getType()) == false){
			return false;
		}
		return super.goToImplementationEnabled();
	}

	@Override
	public void goToImplementation(){
		if(JavaUtils.isUserType(getTarget().getType())){
			IType type = JavaModelAnalyser.getIType(getType());
			if(type != null){
				try {
					JavaUI.openInEditor(type);
				} catch (Exception e) {}
			}
		}
	}

	public boolean setType(String newType) {
		if(newType.equals(getTarget().getType())){
			return false;
		}
		return execute(setTypeOperation(newType), Messages.DIALOG_SET_PARAMETER_TYPE_PROBLEM_TITLE);
	}

	@Override
	protected AbstractParameterNode getTarget(){
		return (AbstractParameterNode)super.getTarget();
	}

	protected IModelOperation setTypeOperation(String type) {
		return new AbstractParameterOperationSetType(getTarget(), type, getAdapterProvider());
	}
}
