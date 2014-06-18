/*******************************************************************************
 * Copyright (c) 2013 Testify AS.                                                   
 * All rights reserved. This program and the accompanying materials                 
 * are made available under the terms of the Eclipse Public License v1.0            
 * which accompanies this distribution, and is available at                         
 * http://www.eclipse.org/legal/epl-v10.html                                        
 *                                                                                  
 * Contributors:                                                                    
 *     Patryk Chamuczynski (p.chamuczynski(at)radytek.com) - initial implementation
 ******************************************************************************/

package com.testify.ecfeed.ui.editor;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import com.testify.ecfeed.model.PartitionNode;
import com.testify.ecfeed.ui.common.Messages;
import com.testify.ecfeed.utils.ModelUtils;

public class PartitionDetailsPage extends BasicDetailsPage {
	
	private PartitionNode fSelectedPartition;
	private PartitionChildrenViewer fPartitionChildren;
	private PartitionLabelsViewer fLabelsViewer;
	private Text fPartitionNameText;
	private Combo fPartitionValueCombo;

	private class PartitionNameTextListener extends ApplyChangesSelectionAdapter implements Listener{
		@Override
		public void handleEvent(Event event) {
			if(event.keyCode == SWT.CR || event.keyCode == SWT.KEYPAD_CR){
				if(applyNewPartitionName(fSelectedPartition, fPartitionNameText)){
					modelUpdated(null);
				}
			}
		}
	}
	
	private class ApplyChangesSelectionAdapter extends SelectionAdapter{
		@Override
		public void widgetSelected(SelectionEvent e){
			boolean updated = applyNewPartitionName(fSelectedPartition, fPartitionNameText);
			if (applyNewPartitionValue(fSelectedPartition, fPartitionValueCombo)) {
				updated = true;
			} else {
				MessageDialog.openError(Display.getCurrent().getActiveShell(),
						Messages.DIALOG_PARTITION_VALUE_PROBLEM_TITLE,
						Messages.DIALOG_PARTITION_VALUE_PROBLEM_MESSAGE);
			}
			if(updated){
				modelUpdated(null);
			}
		}
		
	}
	
	@Override
	public void createContents(Composite parent){
		super.createContents(parent);

		createNameValueEdit(getMainComposite());
		addForm(fPartitionChildren = new PartitionChildrenViewer(this, getToolkit()));
		addForm(fLabelsViewer = new PartitionLabelsViewer(this, getToolkit()));
		
		getToolkit().paintBordersFor(getMainComposite());
	}
	
	@Override
	public void refresh(){
		if(getSelectedElement() instanceof PartitionNode){
			fSelectedPartition = (PartitionNode)getSelectedElement();
		}
		if(fSelectedPartition != null){
			String title = fSelectedPartition.toString();
			boolean implemented = ModelUtils.isPartitionImplemented(fSelectedPartition);
			if (implemented) {
				title += " [implemented]";
			}
			getMainSection().setText(title);
			fPartitionChildren.setInput(fSelectedPartition);
			fLabelsViewer.setInput(fSelectedPartition);
			fPartitionNameText.setText(fSelectedPartition.getName());
			if(fSelectedPartition.isAbstract()){
				fPartitionValueCombo.setEnabled(false);
				fPartitionValueCombo.setText("");
			}
			else{
				fPartitionValueCombo.setEnabled(true);
				prepareDefaultValues(fSelectedPartition, fPartitionValueCombo);
				fPartitionValueCombo.setText(fSelectedPartition.getValueString());
			}
		}
	}

	public PartitionDetailsPage(ModelMasterSection masterSection) {
		super(masterSection);
	}
	
	private boolean applyNewPartitionName(PartitionNode partition, Text nameText) {
		String newName = nameText.getText(); 
		if(newName.equals(partition.getName()) == false){
			if(partition.getCategory().validatePartitionName(newName)){
				partition.setName(newName);
				return true;
			}
			else{
				nameText.setText(partition.getName());
			}
		}
		return false;
	}

	private boolean applyNewPartitionValue(PartitionNode partition, Combo valueText) {
		String newValue = valueText.getText(); 
		if(newValue.equals(partition.getValueString()) == false){
			if(ModelUtils.validatePartitionStringValue(newValue, partition.getCategory().getType())){
				partition.setValueString(newValue);
				return true;
			}
			else{
				valueText.setText(partition.getValueString());
			}
		}
		return false;
	}
	
	private void createNameValueEdit(Composite parent) {
		Composite composite = getToolkit().createComposite(parent);
		composite.setLayout(new GridLayout(3, false));
		composite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		createNameEdit(composite);
		createValueEdit(composite);
	}

	private void createNameEdit(Composite parent) {
		getToolkit().createLabel(parent, "Name");
		fPartitionNameText = getToolkit().createText(parent, "", SWT.NONE);
		fPartitionNameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		fPartitionNameText.addListener(SWT.KeyDown, new PartitionNameTextListener());
		Composite buttonComposite = getToolkit().createComposite(parent);
		buttonComposite.setLayout(new GridLayout(1, false));
		buttonComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true, 1, 2));
		Button applyButton = getToolkit().createButton(buttonComposite, "Change", SWT.CENTER);
		applyButton.addSelectionListener(new ApplyChangesSelectionAdapter());
		getToolkit().paintBordersFor(parent);

	}

	private void createValueEdit(Composite parent) {
		getToolkit().createLabel(parent, "Value");
		fPartitionValueCombo = new Combo(parent,SWT.DROP_DOWN);
		fPartitionValueCombo.setLayoutData(new GridData(SWT.FILL,  SWT.CENTER, true, false));
		fPartitionValueCombo.addListener(SWT.KeyDown, new Listener(){
			@Override
			public void handleEvent(Event event){
				if(event.keyCode == SWT.CR || event.keyCode == SWT.KEYPAD_CR){
					if(applyNewPartitionValue(fSelectedPartition, fPartitionValueCombo)){
						modelUpdated(null);
					} else {
						MessageDialog.openError(Display.getCurrent().getActiveShell(),
								Messages.DIALOG_PARTITION_VALUE_PROBLEM_TITLE,
								Messages.DIALOG_PARTITION_VALUE_PROBLEM_MESSAGE);
					}
				}
			}
		});
		fPartitionValueCombo.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e){
				if(applyNewPartitionValue(fSelectedPartition, fPartitionValueCombo)){
					modelUpdated(null);
				} else {
					MessageDialog.openError(Display.getCurrent().getActiveShell(),
							Messages.DIALOG_PARTITION_VALUE_PROBLEM_TITLE,
							Messages.DIALOG_PARTITION_VALUE_PROBLEM_MESSAGE);
				}
			}
		});
		getToolkit().paintBordersFor(parent);	
	}
	
	private void prepareDefaultValues(PartitionNode node, Combo valueText){
		HashMap<String, String> values = ModelUtils.generatePredefinedValues(node.getCategory().getType());
		String [] items = new String[values.values().size()];
		items = values.values().toArray(items);
		ArrayList<String> newItems = new ArrayList<String>();

		valueText.setItems(items);
		for (int i = 0; i < items.length; ++i) {
			newItems.add(items[i]);
			if (items[i].equals(node.getValueString())) {
				return;
			}
		}

		newItems.add(node.getValueString());
		valueText.setItems(newItems.toArray(items));
	}
	
}
