/*******************************************************************************
 *
 * Copyright (c) 2016 ecFeed AS.                                                
 * All rights reserved. This program and the accompanying materials              
 * are made available under the terms of the Eclipse Public License v1.0         
 * which accompanies this distribution, and is available at                      
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 *******************************************************************************/

package com.ecfeed.utils;

import java.io.File;
import java.net.URI;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;

import com.ecfeed.core.utils.DiskFileHelper;
import com.ecfeed.core.utils.ExceptionHelper;
import com.ecfeed.core.utils.UriHelper;
import com.ecfeed.ui.dialogs.basic.EcSaveAsDialog;
import com.ecfeed.ui.dialogs.basic.SaveAsEctDialogWithConfirm;
import com.ecfeed.ui.editor.CanAddDocumentChecker;

public class EclipseEditorHelper {
	
	public static IEditorPart getActiveEditor() {
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
	}

	public static Object getFileEditorInput(IEditorInput editorInput) { // returns FileEditorInput type
		if (!(editorInput instanceof FileEditorInput)) {
			return null;
		}
		return  (FileEditorInput)editorInput;
	}

	public static Shell getActiveShell() {
		return Display.getDefault().getActiveShell();
	}

	public static IWorkbenchPage getActiveWorkBenchPage() {
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
	}

	public static IWorkbenchWindow getActiveWorkbenchWindow() {
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow();
	}

	public static void openEditorOnExistingExtFile(String pathWithFileName) {
		IWorkbenchPage page = getActiveWorkBenchPage();
		IFileStore fileStore = EclipseEditorHelper.getFileStoreForExistingFile(pathWithFileName);
		if (fileStore == null) {
			ExceptionHelper.reportRuntimeException("Can not open editor on file: " + pathWithFileName + " .");
		}
		openEditorOnFileStore(page, fileStore);
	}

	private static IFileStore getFileStoreForExistingFile(String filePath) {
		if (filePath == null) {
			return null;
		}
		File file = new File(filePath);
		if (!file.exists()) {
			return null;
		}
		if (!file.isFile()) {
			return null;
		}

		return getFileStore(file.toURI());
	}

	private static IFileStore getFileStore(URI uri) {
		return EFS.getLocalFileSystem().getStore(uri);
	}

	public static void openEditorOnFileInMemory(String tmpPathWithFileName) {
		IWorkbenchPage page = getActiveWorkBenchPage();
		File file = new File(tmpPathWithFileName);
		IFileStore fileStore = null;
		try {
			fileStore = EFS.getStore(file.toURI());
		} catch (CoreException e) {
			ExceptionHelper.reportRuntimeException("Can not get store. Message:" + e.getMessage());
		}

		if (fileStore == null) {
			ExceptionHelper.reportRuntimeException("Can not open editor on temporary file.");
		}

		openEditorOnFileStore(page, fileStore);
	}		

	private static void openEditorOnFileStore(IWorkbenchPage page, IFileStore fileStore) {
		try {
			IDE.openEditorOnFileStore(page, fileStore);
		} catch (PartInitException e) {
			ExceptionHelper.reportRuntimeException(e.getMessage());
		}
	}

	public static IAction getGlobalAction(String actionId) {
		IActionBars actionBars = EclipseEditorHelper.getActionBarsForActiveEditor();
		return actionBars.getGlobalActionHandler(actionId);
	}

	public static IActionBars getActionBarsForActiveEditor() {
		IEditorPart editorPart = EclipseEditorHelper.getActiveEditor();

		if (editorPart == null) {
			return null;
		}
		return editorPart.getEditorSite().getActionBars();
	}

	public static String selectFileForSaveAs(IEditorInput editorInput, Shell shell) {
		if (editorInput instanceof FileEditorInput) {
			return selectFileForFileEditorInput((FileEditorInput)editorInput);
		}
		if (editorInput instanceof FileStoreEditorInput) { 
			return selectFileForFileStoreEditorInput((FileStoreEditorInput)editorInput, shell);
		}
		return null;
	}

	private static String selectFileForFileEditorInput(FileEditorInput fileEditorInput) {
		EcSaveAsDialog dialog = new EcSaveAsDialog(Display.getDefault().getActiveShell());
		IFile original = fileEditorInput.getFile();
		dialog.setOriginalFile(original);

		dialog.create();
		if (dialog.open() == Window.CANCEL) {
			return null;
		}

		IPath path = (IPath)dialog.getResultPath();
		return path.toOSString();
	}	

	private static String selectFileForFileStoreEditorInput(FileStoreEditorInput fileStoreEditorInput, Shell shell) {

		String pathWithFileName = UriHelper.convertUriToFilePath(fileStoreEditorInput.getURI());
		String fileName = DiskFileHelper.extractFileName(pathWithFileName);
		String path = DiskFileHelper.extractPathWithSeparator(pathWithFileName);

		CanAddDocumentChecker checker = new CanAddDocumentChecker();
		return SaveAsEctDialogWithConfirm.open(path, fileName, checker, shell);
	}
	

}
