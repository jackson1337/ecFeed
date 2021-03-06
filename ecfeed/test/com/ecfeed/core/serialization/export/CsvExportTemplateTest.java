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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.ecfeed.core.model.MethodNode;
import com.ecfeed.core.utils.StringHelper;

public class CsvExportTemplateTest {

	@Test
	public void ShouldNotThrowWhenEmpty() {

		String templateText = new String();

		CsvExportTemplate csvExportTemplate = new CsvExportTemplate(createMethodNode());

		try {
			csvExportTemplate.setTemplateText(templateText);
		} catch (Exception e) {
			fail("Exception thrown during export.");
		}

		assertTrue(StringHelper.isNullOrEmpty(csvExportTemplate.getHeaderTemplate()));
		assertTrue(StringHelper.isNullOrEmpty(csvExportTemplate.getTestCaseTemplate()));
		assertTrue(StringHelper.isNullOrEmpty(csvExportTemplate.getFooterTemplate()));
	}

	@Test
	public void ShouldParseForTwoParamsTemplateRepeatedly() {

		String templateText = 
				StringHelper.appendNewline(CsvExportTemplate.HEADER_MARKER)
				+ StringHelper.appendNewline("$1.name,$2.name") 
				+ StringHelper.appendNewline(CsvExportTemplate.TEST_CASE_MARKER) 
				+ StringHelper.appendNewline("$1.value,$2.value")
				+ StringHelper.appendNewline(CsvExportTemplate.FOOTER_MARKER);

		CsvExportTemplate csvExportTemplate = 
				new CsvExportTemplate(createMethodNode());

		setTemplateText(csvExportTemplate, templateText);
		setTemplateText(csvExportTemplate, templateText);
		setTemplateText(csvExportTemplate, templateText);
	}

	private void setTemplateText(CsvExportTemplate csvExportTemplate, String templateText) {
		try {
			csvExportTemplate.setTemplateText(templateText);
		} catch (Exception e) {
			fail("Exception thrown during export.");
		}

		assertEquals("$1.name,$2.name", csvExportTemplate.getHeaderTemplate());
		assertEquals("$1.value,$2.value", csvExportTemplate.getTestCaseTemplate());
		assertTrue(StringHelper.isNullOrEmpty(csvExportTemplate.getFooterTemplate()));
	}

	@Test
	public void ShouldParseMultiLineSectionsTemplate() {

		String templateText = 
				StringHelper.appendNewline(CsvExportTemplate.HEADER_MARKER)
				+ StringHelper.appendNewline("HEADER")
				+ StringHelper.appendNewline("$1.name,$2.name") 

				+ StringHelper.appendNewline(CsvExportTemplate.TEST_CASE_MARKER)
				+ StringHelper.appendNewline("TEST CASE")
				+ StringHelper.appendNewline("$1.value,$2.value")

				+ StringHelper.appendNewline(CsvExportTemplate.FOOTER_MARKER)
				+ StringHelper.appendNewline("FOOTER 1")
				+ StringHelper.appendNewline("FOOTER 2");

		CsvExportTemplate csvExportTemplate = 
				new CsvExportTemplate(createMethodNode());

		try {
			csvExportTemplate.setTemplateText(templateText);
		} catch (Exception e) {
			fail("Exception thrown during export.");
		}

		String header = csvExportTemplate.getHeaderTemplate();
		String expectedHeader = "HEADER" + StringHelper.newLine() + "$1.name,$2.name";
		assertEquals(expectedHeader, header);

		String testCase = csvExportTemplate.getTestCaseTemplate(); 
		String expectedTestCase = "TEST CASE" + StringHelper.newLine() + "$1.value,$2.value";

		assertEquals(expectedTestCase, testCase);
	}	

	@Test
	public void ShouldNotThrowWhenOnlyInvalidMarker() {

		String templateText = "[xxx]";

		CsvExportTemplate csvExportTemplate = 
				new CsvExportTemplate(createMethodNode());

		try {
			csvExportTemplate.setTemplateText(templateText);
		} catch (Exception e) {
			fail("Exception thrown during export.");
		}

		assertTrue(StringHelper.isNullOrEmpty(csvExportTemplate.getHeaderTemplate()));
		assertTrue(StringHelper.isNullOrEmpty(csvExportTemplate.getTestCaseTemplate()));
		assertTrue(StringHelper.isNullOrEmpty(csvExportTemplate.getFooterTemplate()));
	}

	@Test
	public void ShouldIgnoreInvalidMarker() {

		String templateText = "[Xxx]" + "\n" + "$1.name\n"
				+ CsvExportTemplate.TEST_CASE_MARKER + "\n" + "$1.value\n"
				+ CsvExportTemplate.FOOTER_MARKER;

		CsvExportTemplate csvExportTemplate = 
				new CsvExportTemplate(createMethodNode());

		try {
			csvExportTemplate.setTemplateText(templateText);
		} catch (Exception e) {
			fail("Exception thrown during export.");
		}

		assertEquals("$1.value", csvExportTemplate.getTestCaseTemplate());
	}

	private MethodNode createMethodNode() {
		return new MethodNode("methodName");
	}

}
