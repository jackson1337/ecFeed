package com.testify.ecfeed.ui.editor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.TabItem;

import com.testify.ecfeed.adapter.java.JavaUtils;
import com.testify.ecfeed.model.AbstractParameterNode;
import com.testify.ecfeed.ui.common.JavaDocSupport;
import com.testify.ecfeed.ui.modelif.AbstractParameterInterface;
import com.testify.ecfeed.ui.modelif.IModelUpdateContext;

public abstract class AbstractParameterCommentsSection extends JavaDocCommentsSection {

	private TabItem fParameterCommentsTab;

	protected class ImportTypeSelectionAdapter extends AbstractSelectionAdapter{
		@Override
		public void widgetSelected(SelectionEvent e) {
			getTargetIf().importTypeJavadocComments();
			getTabFolder().setSelection(getTabFolder().indexOf(getTypeCommentsTab()));
		}
	}

	protected class ImportFullTypeSelectionAdapter extends AbstractSelectionAdapter{
		@Override
		public void widgetSelected(SelectionEvent e) {
			getTargetIf().importFullTypeJavadocComments();
			getTabFolder().setSelection(getTabFolder().indexOf(getTypeCommentsTab()));
		}
	}

	protected class ExportTypeSelectionAdapter extends AbstractSelectionAdapter{
		@Override
		public void widgetSelected(SelectionEvent e) {
			getTargetIf().exportTypeJavadocComments();
			getTabFolder().setSelection(getTabFolder().indexOf(getTypeJavadocTab()));
		}
	}

	protected class ExportFullTypeSelectionAdapter extends AbstractSelectionAdapter{
		@Override
		public void widgetSelected(SelectionEvent e) {
			getTargetIf().exportFullTypeJavadocComments();
			getTabFolder().setSelection(getTabFolder().indexOf(getTypeJavadocTab()));
		}
	}

	protected class EditButtonListener extends AbstractSelectionAdapter{
		@Override
		public void widgetSelected(SelectionEvent e) {
			if(getActiveItem() == fParameterCommentsTab){
				getTargetIf().editComments();
			}
			else if(getActiveItem() == getTypeCommentsTab() || getActiveItem() == getTypeJavadocTab()){
				getTargetIf().editTypeComments();
			}
		}
	}

	public AbstractParameterCommentsSection(ISectionContext sectionContext, IModelUpdateContext updateContext) {
		super(sectionContext, updateContext);

		fParameterCommentsTab = addTextTab("Parameter", 0);
		getTypeCommentsTab().setText("Type");
		getTypeJavadocTab().setText("Type javadoc");
	}

	@Override
	public void refresh(){
		super.refresh();

		String javadoc = JavaDocSupport.getTypeJavadoc(getTarget());
		getTextFromTabItem(getTypeJavadocTab()).setText(javadoc != null ? javadoc : "");

		if(getTargetIf().getComments() != null){
			getTextFromTabItem(fParameterCommentsTab).setText(getTargetIf().getComments());
		}else{
			getTextFromTabItem(fParameterCommentsTab).setText("");
		}
		if(getTargetIf().getTypeComments() != null){
			getTextFromTabItem(getTypeCommentsTab()).setText(getTargetIf().getTypeComments());
		}else{
			getTextFromTabItem(getTypeCommentsTab()).setText("");
		}

		boolean importExportEnabled = getTargetIf().commentsImportExportEnabled();
		getExportButton().setEnabled(importExportEnabled);
		getImportButton().setEnabled(importExportEnabled);
	}

	@Override
	public AbstractParameterNode getTarget(){
		return (AbstractParameterNode)super.getTarget();
	}

	public void setInput(AbstractParameterNode input){
		super.setInput(input);
		getTargetIf().setTarget(input);
	}

	@Override
	protected void createExportMenuItems() {
		MenuItem exportAllItem = new MenuItem(getExportButtonMenu(), SWT.NONE);
		exportAllItem.setText("Export type and choices comments");
		exportAllItem.addSelectionListener(new ExportFullTypeSelectionAdapter());
		MenuItem exportTypeItem = new MenuItem(getExportButtonMenu(), SWT.NONE);
		exportTypeItem.setText("Export only type comments");
		exportTypeItem.addSelectionListener(new ExportTypeSelectionAdapter());
	}

	@Override
	protected void createImportMenuItems() {
		MenuItem importAllItem = new MenuItem(getImportButtonMenu(), SWT.NONE);
		importAllItem.setText("Import type and choices comments");
		importAllItem.addSelectionListener(new ImportFullTypeSelectionAdapter());
		MenuItem importTypeItem = new MenuItem(getImportButtonMenu(), SWT.NONE);
		importTypeItem.setText("Import only type comments");
		importTypeItem.addSelectionListener(new ImportTypeSelectionAdapter());
	}

	@Override
	protected void refreshEditButton() {
		TabItem activeItem = getActiveItem();
		boolean enabled = true;
		if(activeItem == getTypeCommentsTab() || activeItem == getTypeJavadocTab()){
			if(JavaUtils.isPrimitive(getTarget().getType())){
				enabled = false;
			}
		}
		getEditButton().setEnabled(enabled);

		AbstractParameterInterface targetIf = getTargetIf();
		String editButtonText;
		if(getActiveItem() == getTypeCommentsTab() || getActiveItem() == getTypeJavadocTab()){
			if(targetIf.getTypeComments() != null && targetIf.getTypeComments().length() > 0){
				editButtonText = "Edit type comments";
			}else{
				editButtonText = "Add type comments";
			}
		}else{
			if(targetIf.getComments() != null && targetIf.getComments().length() > 0){
				editButtonText = "Edit comments";
			}else{
				editButtonText = "Add comments";
			}
		}
		getEditButton().setText(editButtonText);
		getButtonsComposite().layout();
	}

	@Override
	protected SelectionAdapter createEditButtonSelectionAdapter(){
		return new EditButtonListener();
	}

	protected TabItem getParameterCommentsTab(){
		return fParameterCommentsTab;
	}

	protected TabItem getTypeCommentsTab(){
		return getCommentsItem();
	}

	protected TabItem getTypeJavadocTab(){
		return getJavaDocItem();
	}

	@Override
	protected abstract AbstractParameterInterface getTargetIf();

}