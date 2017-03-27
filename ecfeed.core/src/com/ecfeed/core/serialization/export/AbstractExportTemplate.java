/*******************************************************************************
 *
 * Copyright (c) 2016 ecFeed AS.                                                
 * All rights reserved. This program and the accompanying materials              
 * are made available under the terms of the Eclipse Public License v1.0         
 * which accompanies this distribution, and is available at                      
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 *******************************************************************************/
package com.ecfeed.core.serialization.export;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.StringTokenizer;

import com.ecfeed.core.model.ChoiceNode;
import com.ecfeed.core.model.MethodNode;
import com.ecfeed.core.model.MethodParameterNode;
import com.ecfeed.core.model.TestCaseNode;
import com.ecfeed.core.utils.ExceptionHelper;
import com.ecfeed.core.utils.StringHelper;
import com.ecfeed.core.utils.StringHolder;

public abstract class AbstractExportTemplate implements IExportTemplate {

	private static final String HEADER_MARKER = "[Header]";
	private static final String TEST_CASE_MARKER = "[TestCase]";
	private static final String FOOTER_MARKER = "[Footer]";

	private String fHeaderTemplate;
	private String fTestCaseTemplate;
	private String fFooterTemplate;

	private String fDefaultTemplateText;
	private String fTemplateText;

	private MethodNode fMethodNode;

	public AbstractExportTemplate(MethodNode methodNode) {
		fMethodNode = methodNode;
	}

	@Override
	public void initialize() {
		String defaultTemplateText = createDefaultTemplateText();
		setTemplateText(defaultTemplateText);
	}

	@Override
	public void setTemplateText(String templateText) {

		if (templateText == null) {
			ExceptionHelper.reportRuntimeException("Template text must not be empty.");
		}

		fTemplateText = templateText;
		Map<String, String> templateMap = divideIntoSubtemplates(templateText);

		fHeaderTemplate = createUserHeaderTemplate(templateMap);
		fTestCaseTemplate = createUserTestCaseTemplate(templateMap);
		fFooterTemplate = createUserFooterTemplate(templateMap);
		return;
	}

	@Override
	public String getTemplateText() {

		return fTemplateText;
	}

	@Override
	public String getHeaderTemplate() {

		return fHeaderTemplate;
	}

	@Override
	public String getTestCaseTemplate() {

		return fTestCaseTemplate;
	}

	@Override
	public String getFooterTemplate() {

		return fFooterTemplate;
	}

	@Override
	public boolean isTemplateTextModified() {

		if (StringHelper.isEqual(fTemplateText, fDefaultTemplateText)) {
			return false;
		}

		return true;
	}

	@Override
	public String createPreview(Collection<TestCaseNode> selectedTestCases) {

		StringBuilder stringBuilder = new StringBuilder();

		stringBuilder.append(TestCasesExportHelper.generateSection(fMethodNode, fHeaderTemplate));
		stringBuilder.append("\n");

		appendPreviewOfTestCases(selectedTestCases, stringBuilder);

		stringBuilder.append(TestCasesExportHelper.generateSection(fMethodNode, fFooterTemplate));
		stringBuilder.append("\n");

		return stringBuilder.toString();
	}

	private void appendPreviewOfTestCases(
			Collection<TestCaseNode> selectedTestCases, StringBuilder inOutStringBuilder) {

		List<TestCaseNode> testCases = createPreviewTestCasesSample(selectedTestCases);
		int sequenceIndex = 0;

		for (TestCaseNode testCase : testCases) {
			inOutStringBuilder.append(TestCasesExportHelper.generateTestCaseString(sequenceIndex++, testCase, fTestCaseTemplate));
			inOutStringBuilder.append("\n");
		}
	}

	private List<TestCaseNode> createPreviewTestCasesSample(Collection<TestCaseNode> selectedTestCases) {

		final int MAX_PREVIEW_TEST_CASES = 5;

		if (selectedTestCases == null) {
			return createRandomTestCasesSample(MAX_PREVIEW_TEST_CASES);
		}

		return createSampleFromSelectedTestCases(
				selectedTestCases, MAX_PREVIEW_TEST_CASES);
	}

	private List<TestCaseNode> createSampleFromSelectedTestCases(
			Collection<TestCaseNode> selectedTestCases,
			final int maxPreviewTestCases) {

		List<TestCaseNode> testCases = new ArrayList<TestCaseNode>();

		int cnt = Math.min(maxPreviewTestCases, selectedTestCases.size());
		List<TestCaseNode> selectedTestCasesList = new ArrayList<TestCaseNode>(selectedTestCases);

		for (int index = 0; index < cnt; index++) {
			testCases.add(selectedTestCasesList.get(index));
		}

		return testCases;
	}

	private List<TestCaseNode> createRandomTestCasesSample(
			final int maxPreviewTestCases) {

		List<TestCaseNode> testCases = new ArrayList<TestCaseNode>();

		for (int index = 0; index < maxPreviewTestCases; index++) {
			testCases.add(createRandomTestCaseNode(fMethodNode, index));
		}

		return testCases;
	}

	private TestCaseNode createRandomTestCaseNode(MethodNode methodNode, int testCaseNumber) {

		Random randomGenerator = new Random();
		List<ChoiceNode> choiceNodes = new ArrayList<ChoiceNode>();
		List<String> parameterNames = methodNode.getParametersNames();

		for (String parameterName : parameterNames) {
			choiceNodes.add(getRandomChoiceNode(methodNode, parameterName, randomGenerator));
		}

		TestCaseNode testCaseNode = new TestCaseNode("testCase" + testCaseNumber, choiceNodes);
		testCaseNode.setParent(methodNode);

		return testCaseNode;
	}

	ChoiceNode getRandomChoiceNode(MethodNode methodNode, String parameterName, Random randomGenerator) {

		MethodParameterNode methodParameterNode = (MethodParameterNode)methodNode.getParameter(parameterName);
		List<ChoiceNode> choices = methodParameterNode.getChoices();

		ChoiceNode choiceNode = choices.get(randomGenerator.nextInt(choices.size()));
		return choiceNode;
	}

	protected void setDefaultTemplateText(String defaultTemplateText) {
		fDefaultTemplateText = defaultTemplateText;
	}

	protected MethodNode getMethodNode() {
		return fMethodNode;
	}

	private static String createUserHeaderTemplate(Map<String, String> template) {

		String headerTemplate = template.get(HEADER_MARKER);

		if (headerTemplate == null) {
			return null;
		}

		return StringHelper.removeNewlineAtEnd(headerTemplate.trim());
	}

	private static String createUserTestCaseTemplate(Map<String, String> template) {

		String testCaseTemplate = template.get(TEST_CASE_MARKER);

		if (testCaseTemplate == null) {
			return null;
		}
		return StringHelper.removeNewlineAtEnd(testCaseTemplate.trim());
	}

	private static String createUserFooterTemplate(Map<String, String> template) {

		String footerTemplate = template.get(FOOTER_MARKER);

		if (footerTemplate == null) {
			return null;
		}
		return StringHelper.removeNewlineAtEnd(footerTemplate.trim());
	}

	protected static Map<String, String> divideIntoSubtemplates(String templateText) {

		Map<String, String> resultMap = new HashMap<String, String>();
		StringTokenizer tokenizer = new StringTokenizer(templateText, StringHelper.newLine());
		StringHolder currentSectionMarker = new StringHolder();

		while (tokenizer.hasMoreTokens()) {
			String line = tokenizer.nextToken();

			if (isCommentLine(line)) {
				continue;
			}

			if (setSectionMarker(line, currentSectionMarker)) {
				continue;
			}

			if (currentSectionMarker.isNull()) {
				continue;
			}

			updateResultMap(currentSectionMarker.get(), line, resultMap);
		}

		return resultMap;
	}

	private static boolean isCommentLine(String line) {

		final String COMMENTED_LINE_REGEX = "^\\s*#.*";

		if (line.matches(COMMENTED_LINE_REGEX)) {
			return true;
		}
		return false;
	}

	private static boolean setSectionMarker(String line, StringHolder currentMarker) {

		if (!isSectionMarker(line)) {
			return false;
		}

		currentMarker.set(getMarker(line));

		return true;
	}

	private static boolean isSectionMarker(String line) {

		String trimmedLine = line.trim();

		if (trimmedLine.equals(HEADER_MARKER)) {
			return true;
		}

		if (trimmedLine.equals(TEST_CASE_MARKER)) {
			return true;
		}

		if (trimmedLine.equals(FOOTER_MARKER)) {
			return true;
		}

		return false;
	}

	private static void updateResultMap(String marker, String line, Map<String, String> result) {

		if (!result.containsKey(marker)) {
			result.put(marker, line);
			return;
		}

		String oldContents = result.get(marker);
		String newContents = oldContents.concat(StringHelper.newLine() + line);
		result.put(marker, newContents);
	}

	private static String getMarker(String line) {

		int sectionTitleStart = line.indexOf('[');
		int sectionTitleStop = line.indexOf(']') + 1;

		return line.substring(sectionTitleStart, sectionTitleStop);
	}

	@Override
	public void setFooterTemplate(String template) {

		fFooterTemplate = template;
	}

	@Override
	public void setHeaderTemplate(String template) {

		fHeaderTemplate = template;
	}

	@Override
	public void setTestCaseTemplate(String template) {

		fTestCaseTemplate = template;
	}
}
