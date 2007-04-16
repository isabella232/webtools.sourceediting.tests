/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     
 *******************************************************************************/
package org.eclipse.jst.jsp.core.tests.translation;

import junit.framework.TestCase;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jst.jsp.core.internal.JSPCorePlugin;
import org.eclipse.jst.jsp.core.internal.java.IJSPTranslation;
import org.eclipse.jst.jsp.core.internal.java.JSPTranslation;
import org.eclipse.jst.jsp.core.internal.java.JSPTranslationAdapter;
import org.eclipse.jst.jsp.core.internal.java.JSPTranslationAdapterFactory;
import org.eclipse.jst.jsp.core.internal.preferences.JSPCorePreferenceNames;
import org.eclipse.jst.jsp.core.tests.JSPCoreTestsPlugin;
import org.eclipse.jst.jsp.core.tests.taglibindex.BundleResourceUtil;
import org.eclipse.wst.sse.core.StructuredModelManager;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.validation.internal.operations.ValidatorManager;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMModel;

public class JSPJavaTranslatorCoreTest extends TestCase {

	static final String WTP_AUTOTEST_NONINTERACTIVE = "wtp.autotest.noninteractive";

	public JSPJavaTranslatorCoreTest() {
	}

	public JSPJavaTranslatorCoreTest(String name) {
		super(name);
	}

	String wtp_autotest_noninteractive = null;

	protected void setUp() throws Exception {
		super.setUp();
		String noninteractive = System.getProperty(WTP_AUTOTEST_NONINTERACTIVE);
		if (noninteractive != null)
			wtp_autotest_noninteractive = noninteractive;
		System.setProperty(WTP_AUTOTEST_NONINTERACTIVE, "true");
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		if (wtp_autotest_noninteractive != null)
			System.setProperty(WTP_AUTOTEST_NONINTERACTIVE, wtp_autotest_noninteractive);
	}

	public void test_107338() throws Exception {
		String projectName = "bug_107338";
		// Create new project
		IProject project = BundleResourceUtil.createSimpleProject(projectName, null, null);
		assertTrue(project.exists());
		BundleResourceUtil.copyBundleEntriesIntoWorkspace("/testfiles/" + projectName, "/" + projectName);
		IFile file = project.getFile("WebContent/test107338.jsp");
		assertTrue(file.exists());

		IStructuredModel model = StructuredModelManager.getModelManager().getModelForRead(file);
		IDOMModel jspModel = (IDOMModel) model;

		String jspSource = model.getStructuredDocument().get();

		assertTrue("line delimiters have been converted to Windows [CRLF]", jspSource.indexOf("\r\n") < 0);
		assertTrue("line delimiters have been converted to Mac [CR]", jspSource.indexOf("\r") < 0);

		if (model.getFactoryRegistry().getFactoryFor(IJSPTranslation.class) == null) {
			JSPTranslationAdapterFactory factory = new JSPTranslationAdapterFactory();
			model.getFactoryRegistry().addFactory(factory);
		}
		IDOMDocument xmlDoc = jspModel.getDocument();
		JSPTranslationAdapter translationAdapter = (JSPTranslationAdapter) xmlDoc.getAdapterFor(IJSPTranslation.class);
		JSPTranslation translation = translationAdapter.getJSPTranslation();
		// System.err.print(translation.getJavaText());

		assertTrue("new-line beginning scriptlet missing from translation", translation.getJavaText().indexOf("int i = 0;") >= 0);

		model.releaseFromRead();
	}

	public void test_174042() throws Exception {
		boolean doValidateSegments = JSPCorePlugin.getDefault().getPluginPreferences().getBoolean(JSPCorePreferenceNames.VALIDATE_FRAGMENTS);
		String testName = "bug_174042";
		// Create new project
		IProject project = BundleResourceUtil.createSimpleProject(testName, null, null);
		assertTrue(project.exists());
		JSPCorePlugin.getDefault().getPluginPreferences().setValue(JSPCorePreferenceNames.VALIDATE_FRAGMENTS, true);
		BundleResourceUtil.copyBundleEntriesIntoWorkspace("/testfiles/" + testName, "/" + testName);
		BundleResourceUtil.copyBundleEntriesIntoWorkspace("/testfiles/struts.jar", "/" + testName + "/struts.jar");
		project.build(IncrementalProjectBuilder.CLEAN_BUILD, new NullProgressMonitor());
		project.build(IncrementalProjectBuilder.FULL_BUILD, new NullProgressMonitor());
		project.build(IncrementalProjectBuilder.FULL_BUILD, "org.eclipse.wst.validation.validationbuilder", null, new NullProgressMonitor());
		Platform.getJobManager().join(ValidatorManager.VALIDATOR_JOB_FAMILY, new NullProgressMonitor());
		Platform.getJobManager().join(ResourcesPlugin.FAMILY_AUTO_BUILD, new NullProgressMonitor());
		Platform.getJobManager().join(ResourcesPlugin.FAMILY_MANUAL_BUILD, new NullProgressMonitor());
		JSPCorePlugin.getDefault().getPluginPreferences().setValue(JSPCorePreferenceNames.VALIDATE_FRAGMENTS, doValidateSegments);
		IFile main = project.getFile("main.jsp");
		IMarker[] markers = main.findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_ZERO);
		StringBuffer s = new StringBuffer();
		for (int i = 0; i < markers.length; i++) {
			s.append("\n" + markers[i].getAttribute(IMarker.LINE_NUMBER) + ":" + markers[i].getAttribute(IMarker.MESSAGE));
		}
		assertEquals("problem markers found" + s.toString(), 0, markers.length);
	}

	public void test_178443() throws Exception {
		boolean doValidateSegments = JSPCorePlugin.getDefault().getPluginPreferences().getBoolean(JSPCorePreferenceNames.VALIDATE_FRAGMENTS);
		String testName = "bug_178443";
		// Create new project
		IProject project = BundleResourceUtil.createSimpleProject(testName, Platform.getStateLocation(JSPCoreTestsPlugin.getDefault().getBundle()).append(testName), null);
		assertTrue(project.exists());
		JSPCorePlugin.getDefault().getPluginPreferences().setValue(JSPCorePreferenceNames.VALIDATE_FRAGMENTS, true);
		BundleResourceUtil.copyBundleEntriesIntoWorkspace("/testfiles/" + testName, "/" + testName);
		BundleResourceUtil.copyBundleEntriesIntoWorkspace("/testfiles/struts.jar", "/" + testName + "/struts.jar");
		project.build(IncrementalProjectBuilder.CLEAN_BUILD, new NullProgressMonitor());
		project.build(IncrementalProjectBuilder.FULL_BUILD, new NullProgressMonitor());
		project.build(IncrementalProjectBuilder.FULL_BUILD, "org.eclipse.wst.validation.validationbuilder", null, new NullProgressMonitor());
		Platform.getJobManager().join(ValidatorManager.VALIDATOR_JOB_FAMILY, new NullProgressMonitor());
		Platform.getJobManager().join(ResourcesPlugin.FAMILY_AUTO_BUILD, new NullProgressMonitor());
		Platform.getJobManager().join(ResourcesPlugin.FAMILY_MANUAL_BUILD, new NullProgressMonitor());
		JSPCorePlugin.getDefault().getPluginPreferences().setValue(JSPCorePreferenceNames.VALIDATE_FRAGMENTS, doValidateSegments);
		IFile main = project.getFile("main.jsp");
		IMarker[] markers = main.findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_ZERO);
		StringBuffer s = new StringBuffer();
		for (int i = 0; i < markers.length; i++) {
			s.append("\n" + markers[i].getAttribute(IMarker.LINE_NUMBER) + ":" + markers[i].getAttribute(IMarker.MESSAGE));
		}
		assertEquals("problem markers found" + s.toString(), 0, markers.length);
	}

}
