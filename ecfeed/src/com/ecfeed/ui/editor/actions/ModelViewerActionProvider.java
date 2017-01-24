/*******************************************************************************
 *
 * Copyright (c) 2016 ecFeed AS.                                                
 * All rights reserved. This program and the accompanying materials              
 * are made available under the terms of the Eclipse Public License v1.0         
 * which accompanies this distribution, and is available at                      
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 *******************************************************************************/

package com.ecfeed.ui.editor.actions;

import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;

import com.ecfeed.core.adapter.IModelImplementer;
import com.ecfeed.ui.common.EclipseModelImplementer;
import com.ecfeed.ui.common.utils.IFileInfoProvider;
import com.ecfeed.ui.modelif.IModelUpdateContext;


public class ModelViewerActionProvider extends ActionProvider {

	BasicActionRunnerProvider fBasicActionRunnerProvider;

	public ModelViewerActionProvider(
			TreeViewer viewer, 
			IModelUpdateContext 
			context, 
			IFileInfoProvider fileInfoProvider,
			BasicActionRunnerProvider basicActionRunnerProvider,
			boolean selectRoot) {

		addEditActions(viewer, viewer, context, fileInfoProvider);
		if(fileInfoProvider != null && fileInfoProvider.isProjectAvailable()){
			addImplementationActions(viewer, context, fileInfoProvider);
		}

		addViewerActions(viewer, context, selectRoot);
		addMoveActions(viewer, context);
	}

	public ModelViewerActionProvider(
			TableViewer viewer, 
			IModelUpdateContext context, 
			IFileInfoProvider fileInfoProvider) {

		addEditActions(viewer, viewer, context, fileInfoProvider);

		if(fileInfoProvider != null && fileInfoProvider.isProjectAvailable()){
			addImplementationActions(viewer, context, fileInfoProvider);
		}

		addViewerActions(viewer);
		addMoveActions(viewer, context);
	}

	private void addEditActions(
			ISelectionProvider selectionProvider,
			StructuredViewer structuredViewer,
			IModelUpdateContext context,
			IFileInfoProvider fileInfoProvider) {

		DeleteAction deleteAction = new DeleteAction(selectionProvider, context);
		addAction("edit", new CopyAction(selectionProvider));
		addAction("edit", new CutAction(new CopyAction(selectionProvider), deleteAction));
		addAction("edit", new PasteAction(selectionProvider, context, fileInfoProvider));
		addAction("edit", new InsertAction(selectionProvider, structuredViewer, context, fileInfoProvider));
		addAction("edit", deleteAction);

		if (fBasicActionRunnerProvider != null) {
			addBasicEditActions();
		}
	}
	
	private void addBasicEditActions() {
		
		addAction(
				"edit", 
				new NamedActionWithRunner(
						GlobalActions.SAVE.getId(), GlobalActions.SAVE.getDescription(), 
						fBasicActionRunnerProvider.getSaveRunner()));

		addAction(
				"edit", 
				new NamedActionWithRunner(
						GlobalActions.UNDO.getId(), GlobalActions.UNDO.getDescription(), 
						fBasicActionRunnerProvider.getUndoRunner()));

		addAction(
				"edit", 
				new NamedActionWithRunner(
						GlobalActions.REDO.getId(), GlobalActions.REDO.getDescription(), 
						fBasicActionRunnerProvider.getRedoRunner()));
	}

	private void addImplementationActions(StructuredViewer viewer, IModelUpdateContext context, IFileInfoProvider fileInfoProvider) {
		IModelImplementer implementer = new EclipseModelImplementer(fileInfoProvider);
		addAction("implement", new ImplementAction(viewer, context, implementer));
		addAction("implement", new GoToImplementationAction(viewer, fileInfoProvider));
	}

	private void addMoveActions(ISelectionProvider selectionProvider, IModelUpdateContext context){
		addAction("move", new MoveUpDownAction(true, selectionProvider, context));
		addAction("move", new MoveUpDownAction(false, selectionProvider, context));
	}

	private void addViewerActions(TreeViewer viewer, IModelUpdateContext context, boolean selectRoot){
		addAction("viewer", new SelectAllAction(viewer, selectRoot));
		addAction("viewer", new ExpandCollapseAction(viewer));
	}

	private void addViewerActions(TableViewer viewer){
		addAction("viewer", new SelectAllAction(viewer));
	}

}
