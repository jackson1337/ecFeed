package com.testify.ecfeed.ui.editor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import com.testify.ecfeed.model.RootNode;

public class ModelDetailsPage extends BasicDetailsPage {

	RootNode fModel;
	private ClassViewer fClassesSection;
	private Text fModelNameText;
	
	public ModelDetailsPage(ModelMasterSection masterSection) {
		super(masterSection);
	}

	@Override
	public void createContents(Composite parent){
		super.createContents(parent);
		getMainSection().setText("Model details");

		createModelNameEdit(getMainComposite());
		addForm(fClassesSection = new ClassViewer(this, getToolkit()));

		getToolkit().paintBordersFor(getMainComposite());
	}


	private void createModelNameEdit(Composite parent) {
		Composite composite = getToolkit().createComposite(parent);
		composite.setLayout(new GridLayout(3, false));
		composite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		getToolkit().createLabel(composite, "Model name", SWT.NONE);
		fModelNameText = getToolkit().createText(composite, null, SWT.NONE);
		fModelNameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		fModelNameText.addListener(SWT.KeyDown, new Listener() {
			public void handleEvent(Event event) {
				if(event.keyCode == SWT.CR || event.keyCode == SWT.KEYPAD_CR){
					renameModel(fModelNameText.getText());
				}
			}
		});
		Button button = getToolkit().createButton(composite, "Change", SWT.NONE);
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				renameModel(fModelNameText.getText());
			}
		});
		getToolkit().paintBordersFor(composite);
	}

	private void renameModel(String text) {
		if(RootNode.validateModelName(text) && !fModel.getName().equals(text)){
			fModel.setName(text);
			modelUpdated(null);
		}
		else{
			fModelNameText.setText(fModel.getName());
		}
	}

	@Override
	public void refresh() {
		if(getSelectedElement() instanceof RootNode){
			fModel = (RootNode)getSelectedElement();
		}
		if(fModel != null){
			fModelNameText.setText(fModel.getName());
			fClassesSection.setInput(fModel);
		}
	}
	

}
