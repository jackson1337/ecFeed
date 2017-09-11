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

import java.util.Iterator;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import com.ecfeed.core.model.AbstractNode;
import com.ecfeed.ui.dialogs.basic.ExceptionCatchDialog;
import com.ecfeed.ui.editor.actions.GlobalActions;
import com.ecfeed.ui.editor.actions.IActionProvider;
import com.ecfeed.ui.editor.actions.NamedAction;

	public class ViewerMenuListener implements MenuListener {

		private Menu fMenu;
		IActionProvider fActionProvider;
		INodeSelectionProvider fNodeSelectionProvider;

		private final int LAST_MENU_POSITION = -1;

		public ViewerMenuListener(Menu menu, IActionProvider actionProvider, INodeSelectionProvider nodeSelectionProvider) {
			fMenu = menu;
			fActionProvider = actionProvider;
			fNodeSelectionProvider = nodeSelectionProvider;
		}

		protected Menu getMenu(){
			return fMenu;
		}

		@Override
		public void menuHidden(MenuEvent e) {
		}

		@Override
		public void menuShown(MenuEvent e) {

			for(MenuItem item : getMenu().getItems()) {
				item.dispose();
			}

			populateMenu();
		}

		protected void populateMenu() {

			IActionProvider provider = fActionProvider;
			if(provider == null) {
				return;
			}

			AbstractNode firstSelectedNode = fNodeSelectionProvider.getFirstSelectedAbstractNode();
			if (firstSelectedNode == null) {
				return;
			}

			addActionsForAllGroups(provider);
		}

		private void addActionsForAllGroups(IActionProvider provider) {

			Iterator<String> groupIt = provider.getGroups().iterator();

			while(groupIt.hasNext()) {

				addActionsForGroup(groupIt, provider);

				if(groupIt.hasNext()){
					new MenuItem(fMenu, SWT.SEPARATOR);
				}
			}
		}

		private void addActionsForGroup(
				Iterator<String> groupIt, IActionProvider actionProvider) {

			for (NamedAction action : actionProvider.getActions(groupIt.next())) {

				addMenuItem(action.getName(), action, getMenuItemIndex(action));
			}
		}

		protected void addMenuItem(String text, Action action, int index) {

			MenuItem item;

			if (index == LAST_MENU_POSITION) {
				item = new MenuItem(getMenu(), SWT.NONE);
			} else {
				item = new MenuItem(getMenu(), SWT.NONE, index);
			}

			item.setText(text);
			item.setEnabled(action.isEnabled());
			item.addSelectionListener(new MenuItemSelectionAdapter(action)); 
		}

		protected void addMenuItem(String text, Action action) {
			addMenuItem(text, action, LAST_MENU_POSITION);
		}		

		private int getMenuItemIndex(NamedAction action) {

			String actionName = action.getName();

			if (actionName.equals(GlobalActions.INSERT.getDescription())) {
				return 1;
			}

			return LAST_MENU_POSITION;
		}		

		private class MenuItemSelectionAdapter extends SelectionAdapter {

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

	}