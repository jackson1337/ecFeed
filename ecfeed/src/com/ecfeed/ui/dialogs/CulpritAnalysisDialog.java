/*******************************************************************************
 *
 * Copyright (c) 2016 ecFeed AS.                                                
 * All rights reserved. This program and the accompanying materials              
 * are made available under the terms of the Eclipse Public License v1.0         
 * which accompanies this distribution, and is available at                      
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 *******************************************************************************/

package com.ecfeed.ui.dialogs;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import com.ecfeed.core.generators.TestResultDescription;
import com.ecfeed.core.generators.TestResultsAnalysis;
import com.ecfeed.core.generators.TestResultsAnalyzer;
import com.ecfeed.ui.dialogs.basic.DialogObjectToolkit;

public class CulpritAnalysisDialog extends TitleAreaDialog {

	private DialogObjectToolkit fDialogObjectToolkit;
	private int fCount = 0;
	private int fNumRow = 0;
	final private int n1 = 1;
	final private int n2 = 3;
	final private int maxNumRow = 31;
	final private int ComboMaxLimit = 10;
	private Table fTable = null;
	private List<TestResultDescription> fTestResultDescrs = createtestResultDescrs(); // MDX move create... to constructor -- yes when this is connected to a real dialog
	private TestResultsAnalysis fTestResultsAnalysis 
	= new TestResultsAnalyzer().generateAnalysis(fTestResultDescrs, n1, n2); // MDX move creation to contructor -- yes when this is connected to a real dialog
	int total = fTestResultsAnalysis.getCulpritCount();	
	Button NextButton;
	Button PrevButton;

	public CulpritAnalysisDialog() {
		super(null);
		fDialogObjectToolkit = DialogObjectToolkit.getInstance();
	}

	public void run() { // MDX do we need this now ? -- until we connect this to a real dialog.
		setBlockOnOpen(true);
		open();
		Display.getCurrent().dispose();
	}

	@Override
	public void create() {
		super.create();
		setTitle("Test execution report");	
	}

	@Override
	protected boolean isResizable()	{
		return true;
	}

	@Override
	protected Control createDialogArea(Composite parent) {

		Composite dialogArea = (Composite) super.createDialogArea(parent);

		Composite LabelComposite = (Composite) fDialogObjectToolkit.createGridComposite(dialogArea, 1);
		final String text = "Select tuple size for combinatoric analysis";
		createLabel(LabelComposite, text);

		Composite ComboComposite = (Composite) fDialogObjectToolkit.createGridComposite(dialogArea, 4);
		createLabel(ComboComposite, "min");
		createCombo(ComboComposite, 0);
		createLabel(ComboComposite, "max");
		createCombo(ComboComposite, 2);

		Composite TableComposite = (Composite) fDialogObjectToolkit.createGridComposite(dialogArea, 1);
		fTable = createTableContents(TableComposite, 4);

		Composite ButtonComposite = (Composite) fDialogObjectToolkit.createGridComposite(dialogArea, 2);
		PrevButton = 
				fDialogObjectToolkit.createButton(
						ButtonComposite, "Previous Page", new PrevButtonSelectionAdapter());
		NextButton = 
				fDialogObjectToolkit.createButton(
						ButtonComposite, "Next Page", new NextButtonSelectionAdapter());
		PrevButton.setLayoutData(new GridData(SWT.RIGHT, SWT.RIGHT, true, true));
		NextButton.setLayoutData(new GridData(SWT.RIGHT, SWT.RIGHT, false, false));
		PrevButton.setEnabled(false);


		// MDX convert PrevButton and NextButton to class fields + call e.g fPrevButton.setEnabled methods -- we agreed to discuss this

		return dialogArea;
	}



	private void createCombo(Composite parentComposite, int defaultValue) {
		fDialogObjectToolkit.createCombo(parentComposite, ComboMaxLimit, defaultValue);
	}

	private void createLabel(Composite parentComposite, String text) {
		fDialogObjectToolkit.createLabel(parentComposite, text);
	}

	private Table createTableContents(Composite parentComposite, int ColumnNr) {

		Table table = fDialogObjectToolkit.createTable(parentComposite);
		String[] ColumnNames = {"Rank", "Tuple", "Failure Index", "Occurences", "Fails"};
		TableColumn[] column = fDialogObjectToolkit.addColumn(table, 5, ColumnNames);
		fillTable(table, fTestResultDescrs);

		for (int i = 0; i< column.length; i++) {
			column[i].pack();
		}

		makeColumnResizable(parentComposite, column);
		SortByHeaders(table, column);
		return table;
	}

	private void fillTable(Table table, List<TestResultDescription> testResultDescrs ) {

		table.setRedraw(false);

		initializeRowCount();	

		while (fCount < fNumRow) {

			createTableItem(table);
			fCount++;
		}

		table.setRedraw(true);	
	}

	private void createTableItem( Table table) {

		int c = 0;
		TableItem item = new TableItem(table, SWT.NONE);
		item.setText(c++, Integer.toString(fCount+1));
		item.setText(c++, fTestResultsAnalysis.getCulprit(fCount).getItem().toString());
		item.setText(c++, String.valueOf(fTestResultsAnalysis.getCulprit(fCount).getFailureIndex()));
		item.setText(c++, Integer.toString(fTestResultsAnalysis.getCulprit(fCount).getOccurenceCount()));
		item.setText(c++, Integer.toString(fTestResultsAnalysis.getCulprit(fCount).getFailureCount()));	

	}

	private void initializeRowCount() {
		if (fNumRow == 0) {
			if (fTestResultsAnalysis.getCulpritCount() > maxNumRow) {  
				fNumRow = maxNumRow;
			} else {
				fNumRow = fTestResultsAnalysis.getCulpritCount();
			}
		}	
	}

	private void makeColumnResizable(Composite parentComposite, TableColumn[] column) {
		parentComposite.addControlListener(new ColumnResizeListener(parentComposite, column));
	}

	private class ColumnResizeListener extends ControlAdapter {

		Composite fParentComposite;
		TableColumn[] fColumn;

		public ColumnResizeListener(Composite parentComposite, TableColumn[] column) {
			fParentComposite = parentComposite;
			fColumn = column;
		}

		@Override
		public void controlResized(ControlEvent e) {

			Rectangle area = fParentComposite.getClientArea();
			Point preferredSize = fTable.computeSize(SWT.DEFAULT, SWT.DEFAULT);
			int width = area.width - 2 * fTable.getBorderWidth();

			if (preferredSize.y > area.height + fTable.getHeaderHeight()) {
				Point vBarSize = fTable.getVerticalBar().getSize();
				width -= vBarSize.x;
			}

			Point oldSize = fTable.getSize();

			RegulateTableSize(oldSize, width, area);

		}

		private void RegulateTableSize(Point oldSize, int width, Rectangle area) {

			if (oldSize.x > area.width) {

				setColumnWidth(width);

				fTable.setSize(area.width, area.height);

			} else {

				fTable.setSize(area.width, area.height);

				setColumnWidth(width);
			}
		}

		private void setColumnWidth(int width) {

			for (int i = 0; i < fColumn.length; i++) {

				if (i == 1) { //More space needed to describe the tuple
					fColumn[i].setWidth(width / 2);
				} else {
					fColumn[i].setWidth(((width - width / 3))/(fColumn.length));
				}
			}
		}
	}

	private void addTestResult(
			String[] testArguments, boolean result, List<TestResultDescription> testResultDescrs) {

		List<String> testArgList = new ArrayList<String>();

		for (String testArgument : testArguments) {
			testArgList.add(testArgument);
		}

		testResultDescrs.add(new TestResultDescription(testArgList, result));
	}

	public List<TestResultDescription> createtestResultDescrs()	{ // MDX to be removed after connecting to a real dialog -> yes 

		List<TestResultDescription> testResultDescrs = new ArrayList<TestResultDescription>();

		addTestResult(new String[]{ "1", "2", "3", "4", "5" }, false, testResultDescrs);
		addTestResult(new String[]{ "0", "2", "3", "5", "4" }, false, testResultDescrs);
		addTestResult(new String[]{ "5", "2", "3", "7", "8" }, true, testResultDescrs);
		addTestResult(new String[]{ "7", "7", "3", "9", "8" }, false, testResultDescrs);
		addTestResult(new String[]{ "2", "4", "5", "3", "8" }, true, testResultDescrs);

		return testResultDescrs;
	}

	private void SortByHeaders(Table table, TableColumn[] column)
	{
		CompareByVal(column);

		Listener sortListener = new SortByHeadersListener(table);

		for (int i = 0; i < column.length; i++)
		{
			column[i].addListener(SWT.Selection, sortListener);
		}	
	}

	private class SortByHeadersListener implements Listener {

		Table fTable;

		public SortByHeadersListener(Table table) {
			fTable = table;
		}

		public void handleEvent(Event e)
		{
			TableColumn sortColumn = fTable.getSortColumn();
			TableColumn selectedColumn = (TableColumn) e.widget;
			int dir = fTable.getSortDirection();

			setSortDir(sortColumn, selectedColumn, dir);

			if(dir == SWT.UP)
			{
				fTestResultsAnalysis.SortColumnInput("SWT.UP", selectedColumn.getText());
				fTable.removeAll();
				fCount = 0;
				fillTable(fTable, fTestResultDescrs);

			} else {
				fTestResultsAnalysis.SortColumnInput("SWT.DOWN", selectedColumn.getText());
				fTable.removeAll();
				fCount = 0;
				fillTable(fTable, fTestResultDescrs);
			}			
		}

		private void setSortDir(TableColumn sortColumn, TableColumn selectedColumn, int dir) {

			if (sortColumn == selectedColumn)
			{
				if (dir == SWT.UP)
				{
					fTable.setSortDirection(SWT.DOWN);
				} else {
					fTable.setSortDirection(SWT.UP);
				}
			} else {
				fTable.setSortColumn(selectedColumn);
				dir = SWT.UP;
			}

		}

	}

	private void CompareByVal(TableColumn[] column) {
		for(int i = 0; i < column.length; i++)
		{
			column[i].setData(new Comparator<TableItem>(){

				@Override
				public int compare(TableItem item1, TableItem item2)
				{
					int val1 = Integer.parseInt(item1.getText());
					int val2 = Integer.parseInt(item2.getText());
					if(val1 < val2)
					{
						return -1;
					}
					if(val1 > val2)
					{
						return 1;
					}
					return 0;
				}
			});
		}
	}


	class NextButtonSelectionAdapter extends SelectionAdapter {

		@Override
		public void widgetSelected(SelectionEvent arg0)
		{

			if (total - fNumRow >= maxNumRow) { 
				fNumRow = fNumRow + maxNumRow;
				fTable.removeAll();
				fillTable(fTable, fTestResultDescrs);

			} else if (fNumRow < total) {
				fNumRow = total;
				fTable.removeAll();
				fillTable(fTable, fTestResultDescrs);
			}

			if (fCount == total && NextButton.isEnabled())
			{

				NextButton.setEnabled(false);
			}

			if (fCount > 0 && !PrevButton.isEnabled())
			{
				PrevButton.setEnabled(true);
			}
		}
	}

	class PrevButtonSelectionAdapter extends SelectionAdapter {

		@Override
		public void widgetSelected(SelectionEvent arg0) {

			if (fNumRow >= maxNumRow && fCount >= maxNumRow*2) { // MDX similar code as above - maybe extract to methods -> It is different algorithm ..
				fNumRow = fNumRow - maxNumRow;
				fCount = fCount - maxNumRow * 2;
				fTable.removeAll();
				fillTable(fTable, fTestResultDescrs);
			} else if(fCount - maxNumRow * 2 < 0) {
				fCount = 0;
				fNumRow = maxNumRow;
				fTable.removeAll();
				fillTable(fTable, fTestResultDescrs);
			}

			if (fCount < total && !NextButton.isEnabled())
			{
				NextButton.setEnabled(true);
			}
			if (fCount == maxNumRow && PrevButton.isEnabled())
			{
				PrevButton.setEnabled(false);
			}
		}
	}

	public static void main(String[] args) { // MDX do we need this ? -- until we connect this to a real dialog
		new CulpritAnalysisDialog().run();
	}

}
