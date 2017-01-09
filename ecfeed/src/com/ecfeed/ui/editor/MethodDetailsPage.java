/*******************************************************************************
 *
 * Copyright (c) 2016 ecFeed AS.                                                
 * All rights reserved. This program and the accompanying materials              
 * are made available under the terms of the Eclipse Public License v1.0         
 * which accompanies this distribution, and is available at                      
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 *******************************************************************************/

package com.ecfeed.ui.editor;

import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import com.ecfeed.core.adapter.EImplementationStatus;
import com.ecfeed.core.adapter.java.JavaUtils;
import com.ecfeed.core.model.AbstractNode;
import com.ecfeed.core.model.MethodNode;
import com.ecfeed.core.model.NodePropertyDefs;
import com.ecfeed.ui.common.utils.IFileInfoProvider;
import com.ecfeed.ui.dialogs.basic.ExceptionCatchDialog;
import com.ecfeed.ui.modelif.IModelUpdateContext;
import com.ecfeed.ui.modelif.MethodInterface;
import com.ecfeed.utils.SeleniumHelper;

public class MethodDetailsPage extends BasicDetailsPage {

	private Text fMethodNameText;
	private Button fTestOnlineButton;
	private Button fExportOnlineButton;
	private Button fBrowseButton;
	private Combo fRunnerCombo;
	private WebRunnerSection fRunnerSection;
	private MethodParametersViewer fParemetersSection;
	private ConstraintsListViewer fConstraintsSection;
	private TestCasesViewer fTestCasesSection;

	private MethodInterface fMethodInterface;
	private JavaDocCommentsSection fCommentsSection;

	private final NodePropertyDefs.PropertyId fRunnerPropertyId = NodePropertyDefs.PropertyId.PROPERTY_METHOD_RUNNER;

	private class OnlineTestAdapter extends SelectionAdapter {
		@Override
		public void widgetSelected(SelectionEvent ev) {
			try {
				fMethodInterface.executeOnlineTests(getFileInfoProvider());
			} catch (Exception e) {
				ExceptionCatchDialog.open("Can not execute online tests.",
						e.getMessage());
			}
		}
	}

	private class OnlineExportAdapter extends SelectionAdapter {
		@Override
		public void widgetSelected(SelectionEvent ev) {
			try {
				fMethodInterface.executeOnlineExport(getFileInfoProvider());
			} catch (Exception e) {
				ExceptionCatchDialog.open("Can not execute online export.",
						e.getMessage());
			}
		}
	}

	private class ReassignAdapter extends SelectionAdapter {
		@Override
		public void widgetSelected(SelectionEvent e) {
			fMethodInterface.reassignTarget();
		}
	}

	private class MethodNameFocusLostListener extends FocusLostListener {

		@Override
		public void focusLost(FocusEvent e) {
			fMethodInterface.setName(fMethodNameText.getText());
			fMethodNameText.setText(fMethodInterface.getName());
		}
	}	

	public MethodDetailsPage(ModelMasterSection masterSection,
			IModelUpdateContext updateContext,
			IFileInfoProvider fileInfoProvider) {
		super(masterSection, updateContext, fileInfoProvider);
		fMethodInterface = new MethodInterface(this, fileInfoProvider);
	}

	@Override
	public void createContents(Composite parent) {
		super.createContents(parent);

		IFileInfoProvider fileInfoProvider = getFileInfoProvider();

		createMethodNameWidgets(fileInfoProvider);
		createTestAndExportButtons(fileInfoProvider);
		createRunnerCombo();
		createRunnerSection(fileInfoProvider);

		if (fileInfoProvider.isProjectAvailable()) {
			addForm(fCommentsSection = new JavaDocCommentsSection(this, this,
					fileInfoProvider));
		}
		addViewerSection(fParemetersSection = new MethodParametersViewer(this,
				this, fileInfoProvider));
		addViewerSection(fConstraintsSection = new ConstraintsListViewer(this,
				this, fileInfoProvider));
		addViewerSection(fTestCasesSection = new TestCasesViewer(this, this,
				fileInfoProvider));

		getToolkit().paintBordersFor(getMainComposite());
	}

	@Override
	protected Composite createTextClientComposite() {
		Composite textClient = super.createTextClientComposite();
		return textClient;
	}

	private void createMethodNameWidgets(IFileInfoProvider fileInfoProvider) {
		int gridColumns = 2;

		if (fileInfoProvider.isProjectAvailable()) {
			++gridColumns;
		}

		Composite gridComposite = getFormObjectToolkit().createGridComposite(
				getMainComposite(), gridColumns);

		getFormObjectToolkit().createLabel(gridComposite, "Method name ");
		fMethodNameText = getFormObjectToolkit().createGridText(gridComposite,
				new MethodNameFocusLostListener());

		if (fileInfoProvider.isProjectAvailable()) {
			fBrowseButton = getFormObjectToolkit().createButton(gridComposite,
					"Browse...", new ReassignAdapter());
		}

		getFormObjectToolkit().paintBorders(gridComposite);
	}

	private void createTestAndExportButtons(IFileInfoProvider fileInfoProvider) {

		Composite childComposite = getFormObjectToolkit().createRowComposite(
				getMainComposite());

		FormObjectToolkit formObjectToolkit = getFormObjectToolkit();

		fTestOnlineButton = formObjectToolkit.createButton(
				childComposite, "Test online...", new OnlineTestAdapter());

		fExportOnlineButton = formObjectToolkit.createButton(
				childComposite, "Export online...", new OnlineExportAdapter());

		formObjectToolkit.paintBorders(childComposite);
	}

	private void createRunnerCombo() {
		FormObjectToolkit formObjectToolkit = getFormObjectToolkit();
		Composite gridComposite = formObjectToolkit.createGridComposite(getMainComposite(), 2);

		formObjectToolkit.createLabel(gridComposite, "Runner");

		fRunnerCombo = formObjectToolkit.createReadOnlyGridCombo(gridComposite, new RunnerChangedAdapter());
		fRunnerCombo.setItems(NodePropertyDefs.getValueSet(fRunnerPropertyId, null).getPossibleValues()); 
		fRunnerCombo.setText(NodePropertyDefs.getPropertyDefaultValue(fRunnerPropertyId, null));
	}	

	private class RunnerChangedAdapter extends AbstractSelectionAdapter {
		@Override
		public void widgetSelected(SelectionEvent e) {
			fMethodInterface.setProperty(fRunnerPropertyId, fRunnerCombo.getText());
		}
	}	

	private void createRunnerSection(IFileInfoProvider fileInfoProvider) {
		fRunnerSection = new WebRunnerSection(this, this, fMethodInterface, fileInfoProvider);
	}

	@Override
	public void refresh() {
		super.refresh();

		if (!(getSelectedElement() instanceof MethodNode)) {
			return;
		}

		IFileInfoProvider fileInfoProvider = getFileInfoProvider();
		MethodNode methodNode = (MethodNode)getSelectedElement();
		fMethodInterface.setTarget(methodNode);

		refreshMethodNameAndSignature(methodNode);

		refreshRunnerCombo(methodNode);		
		refreshRunnerSection(methodNode);

		refrestTestOnlineButton(fileInfoProvider);
		refreshExportOnlineButton(methodNode);

		setInputForOtherSections(fileInfoProvider, methodNode);

		refreshBrowseButton(fileInfoProvider, methodNode);
	}

	private void refreshMethodNameAndSignature(MethodNode methodNode) {
		getMainSection().setText(JavaUtils.simplifiedToString(methodNode));
		fMethodNameText.setText(fMethodInterface.getName());
	}

	private void refreshRunnerCombo(MethodNode methodNode) {
		String methodRunner = methodNode.getPropertyValue(fRunnerPropertyId);
		if (methodRunner != null) {
			fRunnerCombo.setText(methodRunner);
		}		
	}

	private void refreshRunnerSection(MethodNode methodNode) {
		fRunnerSection.refresh();

		String runner = methodNode.getPropertyValue(fRunnerPropertyId);
		if (NodePropertyDefs.isJavaRunnerMethod(runner)) {
			fRunnerSection.setEnabled(false);
		} else {
			fRunnerSection.setEnabled(true);
		}		
	}

	private void refrestTestOnlineButton(IFileInfoProvider fileInfoProvider) {
		EImplementationStatus methodImplementationStatus = null;
		if (fileInfoProvider.isProjectAvailable()) {
			methodImplementationStatus = fMethodInterface.getImplementationStatus();
		}

		fTestOnlineButton.setEnabled(isTestOnlineButtonEnabled(fileInfoProvider, methodImplementationStatus));
	}

	private void refreshExportOnlineButton(MethodNode methodNode) {
		if (methodNode.getParametersCount() > 0) {
			fExportOnlineButton.setEnabled(true);
		} else {
			fExportOnlineButton.setEnabled(false);
		}
	}

	private void setInputForOtherSections(IFileInfoProvider fileInfoProvider, MethodNode methodNode) {
		fParemetersSection.setInput(methodNode);
		fConstraintsSection.setInput(methodNode);
		fTestCasesSection.setInput(methodNode);

		if (fileInfoProvider.isProjectAvailable()) {
			fCommentsSection.setInput(methodNode);
		}
	}

	private void refreshBrowseButton(IFileInfoProvider fileInfoProvider, MethodNode methodNode) {
		if (!fileInfoProvider.isProjectAvailable()) {
			return;
		}

		EImplementationStatus parentStatus = fMethodInterface.getImplementationStatus(methodNode.getClassNode());

		boolean parentImplemented = 
				parentStatus == EImplementationStatus.IMPLEMENTED
				|| parentStatus == EImplementationStatus.PARTIALLY_IMPLEMENTED; 

		boolean isEnabled = parentImplemented && (!fMethodInterface.getCompatibleMethods().isEmpty());

		fBrowseButton.setEnabled(isEnabled);
	}

	private boolean isTestOnlineButtonEnabled(IFileInfoProvider fileInfoProvider, EImplementationStatus methodStatus) {

		MethodNode methodNode = fMethodInterface.getTarget();

		if (SeleniumHelper.isSeleniumRunnerMethod(methodNode)) {
			return true;
		}		

		if (fileInfoProvider.isProjectAvailable()) {
			return methodStatus != EImplementationStatus.NOT_IMPLEMENTED;
		}

		return false;
	}	

	@Override
	protected Class<? extends AbstractNode> getNodeType() {
		return MethodNode.class;
	}

}
