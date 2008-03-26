/*******************************************************************************
 * Copyright (c) 2001, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Jens Lukowski/Innoopract - initial renaming/restructuring
 *     David Carver - STAR - [205989] - [validation] validate XML after XInclude resolution
 *     
 *******************************************************************************/
package org.eclipse.wst.xml.ui.internal.preferences;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.PlatformUI;
import org.eclipse.wst.sse.core.internal.encoding.CommonEncodingPreferenceNames;
import org.eclipse.wst.sse.ui.internal.preferences.ui.AbstractPreferencePage;
import org.eclipse.wst.xml.core.internal.XMLCorePlugin;
import org.eclipse.wst.xml.core.internal.preferences.XMLCorePreferenceNames;
import org.eclipse.wst.xml.core.internal.provisional.contenttype.ContentTypeIdForXML;
import org.eclipse.wst.xml.ui.internal.XMLUIMessages;
import org.eclipse.wst.xml.ui.internal.editor.IHelpContextIds;

public class XMLFilesPreferencePage extends AbstractPreferencePage {
	protected EncodingSettings fEncodingSettings = null;

	private Combo fDefaultSuffix = null;
	private List fValidExtensions = null;
	private Button fWarnNoGrammar = null;
	private Button fUseXinclude = null;

	protected Control createContents(Composite parent) {
		Composite composite = (Composite) super.createContents(parent);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(composite, IHelpContextIds.XML_PREFWEBX_FILES_HELPID);
		createContentsForCreatingGroup(composite);
		createContentsForValidatingGroup(composite);

		setSize(composite);
		loadPreferences();

		return composite;
	}

	protected void createContentsForCreatingGroup(Composite parent) {
		Group creatingGroup = createGroup(parent, 2);
		creatingGroup.setText(XMLUIMessages.Creating_files);

		// Default extension for New file Wizard
		createLabel(creatingGroup, XMLUIMessages.XMLFilesPreferencePage_ExtensionLabel);
		fDefaultSuffix = createDropDownBox(creatingGroup);
		String[] validExtensions = (String[]) getValidExtensions().toArray(new String[0]);
		Arrays.sort(validExtensions);
		fDefaultSuffix.setItems(validExtensions);
		fDefaultSuffix.addSelectionListener(this);

		Label label = createLabel(creatingGroup, XMLUIMessages.Encoding_desc);
		((GridData) label.getLayoutData()).horizontalSpan = 2;
		fEncodingSettings = new EncodingSettings(creatingGroup, XMLUIMessages.Encoding);
		((GridData) fEncodingSettings.getLayoutData()).horizontalSpan = 2;
	}

	protected void createContentsForValidatingGroup(Composite parent) {
		Group validatingGroup = createGroup(parent, 1);
		validatingGroup.setText(XMLUIMessages.Validating_files);

		if (fWarnNoGrammar == null) {
			fWarnNoGrammar = createCheckBox(validatingGroup, XMLUIMessages.Warn_no_grammar_specified);
		}
		if (fUseXinclude == null) {
			fUseXinclude = createCheckBox(validatingGroup, XMLUIMessages.Use_XInclude);
		}
	}

	public void dispose() {
		fDefaultSuffix.removeModifyListener(this);
		super.dispose();
	}

	protected void doSavePreferenceStore() {
		XMLCorePlugin.getDefault().savePluginPreferences(); // model
	}

	/**
	 * Get content type associated with this new file wizard
	 * 
	 * @return IContentType
	 */
	protected IContentType getContentType() {
		return Platform.getContentTypeManager().getContentType(ContentTypeIdForXML.ContentTypeID_XML);
	}

	/**
	 * Get list of valid extensions
	 * 
	 * @return List
	 */
	private List getValidExtensions() {
		if (fValidExtensions == null) {
			IContentType type = getContentType();
			fValidExtensions = new ArrayList(Arrays.asList(type.getFileSpecs(IContentType.FILE_EXTENSION_SPEC)));
		}
		return fValidExtensions;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.wst.sse.ui.preferences.ui.AbstractPreferencePage#getModelPreferences()
	 */
	protected Preferences getModelPreferences() {
		return XMLCorePlugin.getDefault().getPluginPreferences();
	}

	protected void initializeValues() {
		initializeValuesForCreatingGroup();
		initializeValuesForValidatingGroup();
	}

	protected void initializeValuesForCreatingGroup() {
		String suffix = getModelPreferences().getString(XMLCorePreferenceNames.DEFAULT_EXTENSION);
		fDefaultSuffix.setText(suffix);

		String encoding = getModelPreferences().getString(CommonEncodingPreferenceNames.OUTPUT_CODESET);

		fEncodingSettings.setIANATag(encoding);
	}

	protected void initializeValuesForValidatingGroup() {
		boolean warnNoGrammarButtonSelected = getModelPreferences().getBoolean(XMLCorePreferenceNames.WARN_NO_GRAMMAR);
		boolean useXIncludeButtonSelected = getModelPreferences().getBoolean(XMLCorePreferenceNames.USE_XINCLUDE);

		if (fWarnNoGrammar != null) {
			fWarnNoGrammar.setSelection(warnNoGrammarButtonSelected);
		}
		if (fUseXinclude != null) {
			fUseXinclude.setSelection(useXIncludeButtonSelected);
		}
	}

	protected void performDefaults() {
		performDefaultsForCreatingGroup();
		performDefaultsForValidatingGroup();

		super.performDefaults();
	}

	protected void performDefaultsForCreatingGroup() {
		String suffix = getModelPreferences().getDefaultString(XMLCorePreferenceNames.DEFAULT_EXTENSION);
		fDefaultSuffix.setText(suffix);

		String encoding = getModelPreferences().getDefaultString(CommonEncodingPreferenceNames.OUTPUT_CODESET);

		fEncodingSettings.setIANATag(encoding);
		// fEncodingSettings.resetToDefaultEncoding();
	}

	protected void performDefaultsForValidatingGroup() {
		boolean warnNoGrammarButtonSelected = getModelPreferences().getDefaultBoolean(XMLCorePreferenceNames.WARN_NO_GRAMMAR);
		boolean useXIncludeButtonSelected = getModelPreferences().getDefaultBoolean(XMLCorePreferenceNames.USE_XINCLUDE);
		
		if (fWarnNoGrammar != null) {
			fWarnNoGrammar.setSelection(warnNoGrammarButtonSelected);
		}
		if (fUseXinclude != null) {
			fUseXinclude.setSelection(useXIncludeButtonSelected);
		}
	}

	public boolean performOk() {
		boolean result = super.performOk();

		doSavePreferenceStore();

		return result;
	}

	protected void storeValues() {
		storeValuesForCreatingGroup();
		storeValuesForValidatingGroup();
	}

	protected void storeValuesForCreatingGroup() {
		String suffix = fDefaultSuffix.getText();
		getModelPreferences().setValue(XMLCorePreferenceNames.DEFAULT_EXTENSION, suffix);

		getModelPreferences().setValue(CommonEncodingPreferenceNames.OUTPUT_CODESET, fEncodingSettings.getIANATag());
	}

	protected void storeValuesForValidatingGroup() {
		if (fWarnNoGrammar != null) {
			boolean warnNoGrammarButtonSelected = fWarnNoGrammar.getSelection();
			getModelPreferences().setValue(XMLCorePreferenceNames.WARN_NO_GRAMMAR, warnNoGrammarButtonSelected);
		}
		if (fUseXinclude != null) {
			boolean useXIncludeButtonSelected = fUseXinclude.getSelection();
			getModelPreferences().setValue(XMLCorePreferenceNames.USE_XINCLUDE, useXIncludeButtonSelected);
		}
	}

	protected void validateValues() {
		boolean isValid = false;
		Iterator i = getValidExtensions().iterator();
		while (i.hasNext() && !isValid) {
			String extension = (String) i.next();
			isValid = extension.equalsIgnoreCase(fDefaultSuffix.getText());
		}

		if (!isValid) {
			setErrorMessage(NLS.bind(XMLUIMessages.XMLFilesPreferencePage_ExtensionError, getValidExtensions().toString()));
			setValid(false);
		}
		else {
			setErrorMessage(null);
			setValid(true);
		}
	}
}