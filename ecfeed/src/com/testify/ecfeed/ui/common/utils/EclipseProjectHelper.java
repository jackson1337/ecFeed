/*******************************************************************************
 * Copyright (c) 2015 Testify AS.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/

package com.testify.ecfeed.ui.common.utils;

import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;

import com.testify.ecfeed.external.IProjectHelper;
import com.testify.ecfeed.ui.common.IFileInfoProvider;
import com.testify.ecfeed.ui.common.Messages;
import com.testify.ecfeed.utils.DiskFileHelper;
import com.testify.ecfeed.utils.EcException;
import com.testify.ecfeed.utils.ExceptionHelper;
import com.testify.ecfeed.utils.SystemLogger;

public class EclipseProjectHelper implements IProjectHelper {

	private static final String DEV_HOOK_ANDROID = "dvhAndroid";
	private static final String DEV_HOOK_NO_INSTALL = "dvhNoInstall";
	private static final String VALUE_ANDROID = "Android";
	private static final String ANDROID_ECLIPSE_QUALIFIER = "com.android.ide.eclipse.adt";
	private static final String ANDROID_TARGET_CACHE = "androidTargetCache";

	private static boolean fWasCalculated = false;
	private static boolean fIsAndroidProject = false;
	private IFileInfoProvider fFileInfoProvider;

	public EclipseProjectHelper(IFileInfoProvider fileInfoProvider) {
		if (fileInfoProvider == null) {
			SystemLogger.logInfoWithStack(Messages.EXCEPTION_FILE_INFO_PROVIDER_NOT_NULL);
		}

		fFileInfoProvider = fileInfoProvider;
	}

	@Override
	public String getProjectPath() {
		return fFileInfoProvider.getProject().getLocation().toOSString();
	}

	public String getProjectPath(IProject project) {
		return project.getLocation().toOSString();
	}

	@Override
	public boolean isAndroidProject() {

		if(fWasCalculated) {
			return fIsAndroidProject;
		}

		try {
			if(isAndroidProjectDevelopmentHook() || calculateFlagIsAndroidProject()) {
				fIsAndroidProject = true;
			}
		} catch (EcException e) {
			SystemLogger.logCatch(e.getMessage());
		}

		fWasCalculated = true;
		return fIsAndroidProject;
	}

	@Override
	public boolean isAndroidProjectDevelopmentHook() {
		return isDevelopmentHook(DEV_HOOK_ANDROID, fFileInfoProvider);
	}

	@Override
	public boolean isNoInstallDevelopmentHook() {
		return isDevelopmentHook(DEV_HOOK_NO_INSTALL, fFileInfoProvider);
	}


	private boolean isDevelopmentHook(String hookName, IFileInfoProvider fileInfoProvider) {
		String projectPath = null;
		try {
			projectPath = getProjectPath();
		} catch (Exception e){
			SystemLogger.logCatch(e.getMessage());
			return false;
		}

		String qualifiedName = DiskFileHelper.joinPathWithFile(projectPath, hookName);
		if(DiskFileHelper.fileExists(qualifiedName)) {
			return true;
		}
		return false;
	}	

	@Override
	public String getApkPathAndName() {
		IProject project = fFileInfoProvider.getProject();
		return getApkName(project);
	}

	@Override
	public String getReferencedApkPathAndName() {
		IProject testingProject = fFileInfoProvider.getProject();
		IProject testedProject = getReferencedProject(testingProject);
		return getApkName(testedProject);
	}

	private IProject getReferencedProject(IProject project) {
		IProject[] referencedProjects = null;
		try {
			referencedProjects = project.getReferencedProjects();
		} catch (CoreException e) {
			ExceptionHelper.reportRuntimeException(e.getMessage());
		}

		if (referencedProjects == null || referencedProjects.length == 0) {
			ExceptionHelper.reportRuntimeException(Messages.EXCEPTION_NO_REFERENCED_PROJECTS); 
		}
		if (referencedProjects.length > 1) {
			ExceptionHelper.reportRuntimeException(Messages.EXCEPTION_TOO_MANY_REFERENCED_PROJECTS); 
		}

		return referencedProjects[0];
	}

	private String getApkName(IProject project) {
		if (project == null) {
			ExceptionHelper.reportRuntimeException(Messages.EXCEPTION_NO_APK_NAME_PROJECT_NULL);
		}
		String projectPath = getProjectPath(project);
		String binPath = DiskFileHelper.joinSubdirectory(projectPath, DiskFileHelper.BIN_SUBDIRECTORY);
		String apkFileName = DiskFileHelper.createFileName(project.getName(), DiskFileHelper.APK_EXTENSION);

		return DiskFileHelper.joinPathWithFile(binPath, apkFileName);
	}

	private boolean calculateFlagIsAndroidProject() throws EcException {
		IProject project = fFileInfoProvider.getProject();

		Map<QualifiedName, String> properties = null;
		try {
			properties = project.getPersistentProperties();
		} catch (CoreException e) {
			SystemLogger.logCatch(e.getMessage());
		}

		for(Map.Entry<QualifiedName, String> property : properties.entrySet()) {
			if(isAndroidProperty(property)) {
				return true;
			}
		}
		return false;
	}	

	private static boolean isAndroidProperty(Map.Entry<QualifiedName, String> property) {
		//e.g. com.android.ide.eclipse.adt:androidTargetCache/Android 4.4.2
		if (!property.getValue().contains(VALUE_ANDROID)) {
			return false;
		}
		QualifiedName key = property.getKey();
		if (!key.getQualifier().equals(ANDROID_ECLIPSE_QUALIFIER)) {
			return false;
		}
		if (!key.getLocalName().equals(ANDROID_TARGET_CACHE)) {
			return false;
		}
		return true;
	}

}
