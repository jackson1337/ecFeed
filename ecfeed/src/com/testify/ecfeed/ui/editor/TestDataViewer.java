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

import java.util.List;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.graphics.Color;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import com.testify.ecfeed.model.CategoryNode;
import com.testify.ecfeed.model.PartitionNode;
import com.testify.ecfeed.model.TestCaseNode;
import com.testify.ecfeed.modelif.ImplementationStatus;
import com.testify.ecfeed.modelif.ModelOperationManager;
import com.testify.ecfeed.ui.common.ColorConstants;
import com.testify.ecfeed.ui.common.ColorManager;
import com.testify.ecfeed.ui.common.TestDataEditorListener;
import com.testify.ecfeed.ui.common.TestDataValueEditingSupport;
import com.testify.ecfeed.ui.modelif.GenericNodeInterface;
import com.testify.ecfeed.ui.modelif.TestCaseInterface;

public class TestDataViewer extends TableViewerSection implements TestDataEditorListener{

	private static final int STYLE = Section.EXPANDED | Section.TITLE_BAR;
	private ColorManager fColorManager;
//	private TableViewerColumn fValueColumn; 
	private TestCaseInterface fTestCaseIf;
//	private TestDataValueEditingSupport fTestDataEditingSupport;
	
	public TestDataViewer(BasicDetailsPage page, FormToolkit toolkit, ModelOperationManager operationManager) {
		super(page.getMainComposite(), toolkit, STYLE, page);
		fTestCaseIf = new TestCaseInterface(operationManager);
		fColorManager = new ColorManager();
//		fTestDataEditingSupport = new TestDataValueEditingSupport(getTableViewer(), null, this);
		getSection().setText("Test data");
	}

	@Override
	protected void createTableColumns() {
		addColumn("Parameter", 150, new ColumnLabelProvider(){
			@Override
			public String getText(Object element){
				PartitionNode testValue = (PartitionNode)element;
				CategoryNode parent = testValue.getCategory();
				return parent.toString();
			}
			@Override
			public Color getForeground(Object element){
				return getColor(element);
			}
		});
		
		TableViewerColumn valueColumn = addColumn("Value", 150, new ColumnLabelProvider(){
			@Override
			public String getText(Object element){
				PartitionNode testValue = (PartitionNode)element;
				if(testValue.getCategory().isExpected()){
					return testValue.getValueString();
				}
				return testValue.toString();
			}
			@Override
			public Color getForeground(Object element){
				return getColor(element);
			}
		});
		
		valueColumn.setEditingSupport(new TestDataValueEditingSupport(getTableViewer(), null, this));
	}
	
	private Color getColor(Object element){
		GenericNodeInterface nodeIf = new GenericNodeInterface(null);
		if (nodeIf.implementationStatus((PartitionNode)element) == ImplementationStatus.IMPLEMENTED) {
			return fColorManager.getColor(ColorConstants.ITEM_IMPLEMENTED);
		}
		return null;
	}

	public void setInput(TestCaseNode testCase){
		List<PartitionNode> testData = testCase.getTestData();
		super.setInput(testData);
		fTestCaseIf.setTarget(testCase);
//		fTestDataEditingSupport.setTestData(testCase.getTestData());
//		fValueColumn.setEditingSupport(new TestDataValueEditingSupport(getTableViewer(), testData, this));
	}

	@Override
	public void testDataChanged(int index, PartitionNode value) {
		fTestCaseIf.updateTestData(index, value, this, getUpdateListener());
	}
	
}
