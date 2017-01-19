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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import com.ecfeed.core.model.AbstractNode;
import com.ecfeed.core.model.ChoiceNode;
import com.ecfeed.core.model.ClassNode;
import com.ecfeed.core.model.ConstraintNode;
import com.ecfeed.core.model.GlobalParameterNode;
import com.ecfeed.core.model.IModelVisitor;
import com.ecfeed.core.model.MethodNode;
import com.ecfeed.core.model.MethodParameterNode;
import com.ecfeed.core.model.RootNode;
import com.ecfeed.core.model.TestCaseNode;
import com.ecfeed.core.utils.SystemLogger;
import com.ecfeed.ui.common.utils.IFileInfoProvider;
import com.ecfeed.ui.dialogs.basic.ExceptionCatchDialog;
import com.ecfeed.ui.editor.actions.GlobalActions;
import com.ecfeed.ui.editor.actions.IActionProvider;
import com.ecfeed.ui.editor.actions.NamedAction;
import com.ecfeed.ui.editor.actions.RedoAction;
import com.ecfeed.ui.editor.actions.UndoAction;
import com.ecfeed.ui.modelif.IModelUpdateContext;

/**
 * Section with a main StructuredViewer composite and buttons below or aside
 */
public abstract class ViewerSection extends ButtonsCompositeSection implements ISelectionProvider{

	private final int VIEWER_STYLE = SWT.BORDER | SWT.MULTI;

	private List<Object> fSelectedElements;

	private StructuredViewer fViewer;
	private Composite fViewerComposite;
	private Menu fMenu;
	private Set<KeyListener> fKeyListeners;

	protected class ViewerKeyAdapter extends KeyAdapter{
		private int fKeyCode;
		private Action fAction;
		private int fModifier;

		public ViewerKeyAdapter(int keyCode, int modifier, Action action){
			fKeyCode = keyCode;
			fModifier = modifier;
			fAction = action;
		}

		@Override
		public void keyReleased(KeyEvent e) {
			if((e.stateMask & fModifier) != 0 || fModifier == SWT.NONE){
				if(e.keyCode == fKeyCode){
					fAction.run();
				}
			}
		}
	}

	protected class ActionSelectionAdapter extends SelectionAdapter{
		private Action fAction;
		private String fDescriptionWhenError;

		public ActionSelectionAdapter(Action action, String descriptionWhenError ){
			fAction = action;
			fDescriptionWhenError = descriptionWhenError;
		}

		@Override
		public void widgetSelected(SelectionEvent ev){
			try {
				fAction.run();
			} catch (Exception e) {
				ExceptionCatchDialog.open(fDescriptionWhenError, e.getMessage());
			}
		}
	}

	protected class ViewerMenuListener implements MenuListener{

		private Menu fMenu;

		public ViewerMenuListener(Menu menu) {
			fMenu = menu;
		}

		protected Menu getMenu(){
			return fMenu;
		}

		@Override
		public void menuHidden(MenuEvent e) {
		}

		@Override
		public void menuShown(MenuEvent e) {
			for(MenuItem item : getMenu().getItems()){
				item.dispose();
			}
			populateMenu();
		}

		protected void populateMenu() {

			IActionProvider provider = getActionProvider();
			if(provider == null) {
				return;
			}

			AbstractNode firstSelectedNode = getFirstSelectedNode();
			if (firstSelectedNode == null) {
				return;
			}

			Iterator<String> groupIt = provider.getGroups().iterator();

			while(groupIt.hasNext()){
				for(NamedAction action : provider.getActions(groupIt.next())){
					String convertedName = convertActionName(action.getName(), firstSelectedNode);
					addMenuItem(convertedName, action);
				}
				if(groupIt.hasNext()){
					new MenuItem(fMenu, SWT.SEPARATOR);
				}
			}
		}

		private String convertActionName(String oldName, AbstractNode selectedNode) {
			if (!oldName.equals(GlobalActions.INSERT.getDescription())) {
				return oldName;
			}

			String newName = null;
			ConvertInsertNameVisitor visitor = new ConvertInsertNameVisitor();
			try {
				newName = (String)selectedNode.accept(visitor);
			} catch (Exception e) {
				SystemLogger.logCatch(e.getMessage());
			}

			final String insertKey = "INS";
			return newName + "\t" + insertKey;
		}

		private class MenuItemSelectionAdapter extends SelectionAdapter{

			private Action fAction;

			public MenuItemSelectionAdapter(Action action){
				fAction = action;
			}

			@Override
			public void widgetSelected(SelectionEvent ev){
				try {
					fAction.run();
				} catch (Exception e) {
					ExceptionCatchDialog.open(null, e.getMessage());
				}
			}
		}

		private class ConvertInsertNameVisitor implements IModelVisitor{

			private final static String insertClass = "Insert class";
			private final static String insertMethod = "Insert method";
			private final static String insertParameter = "Insert parameter";
			private final static String insertChoice = "Insert choice";

			@Override
			public Object visit(RootNode node) throws Exception {
				return insertClass;
			}

			@Override
			public Object visit(ClassNode node) throws Exception {
				return insertMethod;
			}

			@Override
			public Object visit(MethodNode node) throws Exception {
				return insertParameter;
			}

			@Override
			public Object visit(MethodParameterNode node) throws Exception {
				return insertChoice;
			}

			@Override
			public Object visit(GlobalParameterNode node) throws Exception {
				return insertChoice;
			}

			@Override
			public Object visit(TestCaseNode node) throws Exception {
				return null;
			}

			@Override
			public Object visit(ConstraintNode node) throws Exception {
				return null;
			}

			@Override
			public Object visit(ChoiceNode node) throws Exception {
				return insertChoice;
			}
		}

		protected void addMenuItem(String text, Action action){
			MenuItem item = new MenuItem(getMenu(), SWT.NONE);

			item.setText(text);
			item.setEnabled(action.isEnabled());
			item.addSelectionListener(new MenuItemSelectionAdapter(action)); 
		}

	}

	public ViewerSection(
			ISectionContext sectionContext, 
			IModelUpdateContext updateContext, 
			IFileInfoProvider fileInfoProvider, 
			int style) {
		super(sectionContext, updateContext, fileInfoProvider, style);
		fSelectedElements = new ArrayList<>();
		fKeyListeners = new HashSet<KeyListener>();
	}

	@Override
	public void refresh(){
		super.refresh();
		if(fViewer != null && fViewer.getControl().isDisposed() == false){
			fViewer.refresh();
		}
	}

	public Object getSelectedElement(){
		if(fSelectedElements.size() > 0){
			return fSelectedElements.get(0);
		}
		return null;
	}

	public void selectElement(Object element){
		getViewer().setSelection(new StructuredSelection(element), true);
	}

	public void setInput(Object input){
		fViewer.setInput(input);
		refresh();
	}

	public Object getInput(){
		return fViewer.getInput();
	}

	public StructuredViewer getViewer(){
		return fViewer;
	}

	@Override
	public void addSelectionChangedListener(ISelectionChangedListener listener){
		fViewer.addSelectionChangedListener(listener);
	}

	@Override
	public IStructuredSelection getSelection(){
		return (IStructuredSelection)fViewer.getSelection();
	}

	public List<AbstractNode> getSelectedNodes(){
		List<AbstractNode> result = new ArrayList<>();
		for(Object o : getSelection().toList()){
			if(o instanceof AbstractNode){
				result.add((AbstractNode)o);
			}
		}
		return result;
	}

	public AbstractNode getFirstSelectedNode() {
		List<AbstractNode> selectedNodes = getSelectedNodes();

		if(selectedNodes.size() == 0) {
			return null;
		}

		return selectedNodes.get(0);
	}

	@Override
	public void removeSelectionChangedListener(ISelectionChangedListener listener){
		fViewer.removeSelectionChangedListener(listener);
	}

	@Override
	public void setSelection(ISelection selection){
		fViewer.setSelection(selection);
	}

	@Override
	protected Composite createClientComposite() {
		Composite client = super.createClientComposite();
		createViewer();
		return client;
	}

	protected void createViewer() {
		fViewer = createViewer(getMainControlComposite(), viewerStyle());
		fViewer.setContentProvider(viewerContentProvider());
		fViewer.setLabelProvider(viewerLabelProvider());
		createViewerColumns();

		fViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@SuppressWarnings("unchecked")
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				fSelectedElements = ((IStructuredSelection)event.getSelection()).toList();
			}
		});
	}

	protected int viewerStyle(){
		return VIEWER_STYLE;
	}

	protected void addDoubleClickListener(IDoubleClickListener listener){
		getViewer().addDoubleClickListener(listener);
	}

	protected GridData viewerLayoutData(){
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.widthHint = 100;
		gd.heightHint = 100;
		return gd;
	}

	protected Composite getViewerComposite(){
		return fViewerComposite;
	}

	protected KeyListener createKeyListener(int keyCode, int modifier, Action action){
		ViewerKeyAdapter adapter = new ViewerKeyAdapter(keyCode, modifier, action);
		fViewer.getControl().addKeyListener(adapter);
		return adapter;
	}

	@Override
	protected void setActionProvider(IActionProvider provider){
		setActionProvider(provider, true);
	}

	protected void setActionProvider(IActionProvider provider, boolean addDeleteAction){
		super.setActionProvider(provider);
		fMenu = new Menu(fViewer.getControl());
		fViewer.getControl().setMenu(fMenu);
		fMenu.addMenuListener(getMenuListener());

		if(provider != null) {
			addKeyListenersForActions(provider, addDeleteAction);
		} else {
			removeKeyListeners();
		}
	}

	private void addKeyListenersForActions(IActionProvider provider, boolean addDeleteAction) {

		NamedAction insertAction = provider.getAction(GlobalActions.INSERT.getId());
		if (insertAction != null) {
			fKeyListeners.add(createKeyListener(SWT.INSERT, SWT.NONE, insertAction));
		}

		NamedAction deleteAction = provider.getAction(GlobalActions.DELETE.getId());
		if (addDeleteAction && deleteAction != null) {
			fKeyListeners.add(createKeyListener(SWT.DEL, SWT.NONE, deleteAction));
		}

		NamedAction arrowUpAction = provider.getAction(GlobalActions.MOVE_UP.getId()); 
		if (arrowUpAction != null) {
			fKeyListeners.add(createKeyListener(SWT.ARROW_UP, SWT.ALT, arrowUpAction));
		}

		NamedAction arrowDownAction = provider.getAction(GlobalActions.MOVE_DOWN.getId()); 
		if (arrowDownAction != null) {
			fKeyListeners.add(createKeyListener(SWT.ARROW_DOWN, SWT.ALT, arrowDownAction));
		}

		if (!getFileInfoProvider().isProjectAvailable()) {
			addActionsForStandaloneApp(provider);
		}
	}

	private void addActionsForStandaloneApp(IActionProvider provider) {
		NamedAction copyAction = provider.getAction(GlobalActions.COPY.getId());
		if (copyAction != null) {
			fKeyListeners.add(createKeyListener('c', SWT.CTRL, copyAction));
			fKeyListeners.add(createKeyListener('C', SWT.CTRL, copyAction));
		}

		NamedAction cutAction = provider.getAction(GlobalActions.CUT.getId());
		if (copyAction != null) {
			fKeyListeners.add(createKeyListener('x', SWT.CTRL, cutAction));
			fKeyListeners.add(createKeyListener('X', SWT.CTRL, cutAction));
		}		

		NamedAction pasteAction = provider.getAction(GlobalActions.PASTE.getId());
		if (copyAction != null) {
			fKeyListeners.add(createKeyListener('v', SWT.CTRL, pasteAction));
			fKeyListeners.add(createKeyListener('V', SWT.CTRL, pasteAction));
		}		

		NamedAction saveAction = provider.getAction(GlobalActions.SAVE.getId());
		if (saveAction != null) {
			fKeyListeners.add(createKeyListener('s', SWT.CTRL, saveAction));
			fKeyListeners.add(createKeyListener('S', SWT.CTRL, saveAction));
		}
		
		UndoAction undoAction = new UndoAction(GlobalActions.UNDO.getId(), GlobalActions.UNDO.getDescription());
		if (undoAction != null) {
			fKeyListeners.add(createKeyListener('z', SWT.CTRL, undoAction));
			fKeyListeners.add(createKeyListener('Z', SWT.CTRL, undoAction));
		}				
		RedoAction redoAction = new RedoAction(GlobalActions.REDO.getId(), GlobalActions.REDO.getDescription());
		if (redoAction != null) {
			fKeyListeners.add(createKeyListener('z', SWT.CTRL | SWT.SHIFT, redoAction));
			fKeyListeners.add(createKeyListener('Z', SWT.CTRL | SWT.SHIFT, redoAction));
		}		
	}

	private void removeKeyListeners() {
		Iterator<KeyListener> it = fKeyListeners.iterator();
		while(it.hasNext()){
			fViewer.getControl().removeKeyListener(it.next());
			it.remove();
		}
	}

	protected MenuListener getMenuListener() {
		return new ViewerMenuListener(fMenu);
	}

	protected Menu getMenu(){
		return fMenu;
	}

	protected abstract void createViewerColumns();
	protected abstract StructuredViewer createViewer(Composite viewerComposite, int style);
	protected abstract IContentProvider viewerContentProvider();
	protected abstract IBaseLabelProvider viewerLabelProvider();
}
