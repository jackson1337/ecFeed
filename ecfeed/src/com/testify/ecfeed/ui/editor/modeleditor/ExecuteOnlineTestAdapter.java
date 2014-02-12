package com.testify.ecfeed.ui.editor.modeleditor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.events.SelectionEvent;

import com.testify.ecfeed.generators.api.IConstraint;
import com.testify.ecfeed.generators.api.IGenerator;
import com.testify.ecfeed.model.PartitionNode;
import com.testify.ecfeed.model.TestCaseNode;
import com.testify.ecfeed.runner.ParameterizedMethod;
import com.testify.ecfeed.ui.dialogs.GenerateTestSuiteDialog;

public class ExecuteOnlineTestAdapter extends ExecuteTestAdapter {
	private class ExecuteRunnable implements IRunnableWithProgress{

		private IGenerator<PartitionNode> fGenerator;
		private List<List<PartitionNode>> fInput;
		private Collection<IConstraint<PartitionNode>> fConstraints;
		private Map<String, Object> fParameters;

		ExecuteRunnable(IGenerator<PartitionNode> generator, 
				List<List<PartitionNode>> input, 
				Collection<IConstraint<PartitionNode>> constraints, 
				Map<String, Object> parameters){
			fGenerator = generator;
			fInput = input;
			fConstraints = constraints;
			fParameters = parameters;
		}
		
		@Override
		public void run(IProgressMonitor monitor)
				throws InvocationTargetException, InterruptedException {
			Class<?> testClass = loadTestClass();
			Method testMethod = getTestMethod(testClass, getPage().getSelectedMethod());
			List<PartitionNode> next;
			try {
				fGenerator.initialize(fInput, fConstraints, fParameters);
				monitor.beginTask("Generating test data", fGenerator.totalWork());
				while((next = fGenerator.next()) != null && monitor.isCanceled() == false){
					List<TestCaseNode> testCases = new ArrayList<TestCaseNode>();
					testCases.add(new TestCaseNode("", next));
					ParameterizedMethod frameworkMethod = new ParameterizedMethod(testMethod, testCases);
					frameworkMethod.invokeExplosively(testClass.newInstance(), new Object[]{});
					monitor.worked(fGenerator.workProgress());
				}
				monitor.done();
			} catch (Throwable e) {
				throw new InvocationTargetException(e, e.getMessage());
			}
		}
		
	}

	public ExecuteOnlineTestAdapter(MethodNodeDetailsPage page) {
		super(page);
	}

	@Override
	public void widgetSelected(SelectionEvent e){
		GenerateTestSuiteDialog dialog = new GenerateTestSuiteDialog(getPage().getActiveShell(), 
				getPage().getSelectedMethod());
		if(dialog.open() == IDialogConstants.OK_ID){
			IGenerator<PartitionNode> selectedGenerator = dialog.getSelectedGenerator();
			List<List<PartitionNode>> algorithmInput = dialog.getAlgorithmInput();
			Collection<IConstraint<PartitionNode>> constraints = dialog.getConstraints();
			Map<String, Object> parameters = dialog.getGeneratorParameters();
			
			executeTest(selectedGenerator, algorithmInput, constraints, parameters);
		}
	}
	
	private void executeTest(IGenerator<PartitionNode> generator,
			List<List<PartitionNode>> input,
			Collection<IConstraint<PartitionNode>> constraints,
			Map<String, Object> parameters) {

		ProgressMonitorDialog progressDialog = new ProgressMonitorDialog(getPage().getActiveShell());
		ExecuteRunnable runnable = new ExecuteRunnable(generator, input, constraints, parameters);
		progressDialog.open();
		try {
			progressDialog.run(true,  true, runnable);
		} catch (InvocationTargetException | InterruptedException e) {
			e.printStackTrace();
		}
	}
}
