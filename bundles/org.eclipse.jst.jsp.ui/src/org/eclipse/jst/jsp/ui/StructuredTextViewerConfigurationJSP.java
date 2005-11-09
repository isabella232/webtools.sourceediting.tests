/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jst.jsp.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jdt.ui.text.IJavaPartitions;
import org.eclipse.jdt.ui.text.JavaSourceViewerConfiguration;
import org.eclipse.jdt.ui.text.JavaTextTools;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.formatter.IContentFormatter;
import org.eclipse.jface.text.formatter.MultiPassContentFormatter;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.jface.text.information.IInformationPresenter;
import org.eclipse.jface.text.information.IInformationProvider;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jst.jsp.core.internal.provisional.text.IJSPPartitionTypes;
import org.eclipse.jst.jsp.core.internal.text.StructuredTextPartitionerForJSP;
import org.eclipse.jst.jsp.ui.internal.autoedit.AutoEditStrategyForTabs;
import org.eclipse.jst.jsp.ui.internal.autoedit.StructuredAutoEditStrategyJSP;
import org.eclipse.jst.jsp.ui.internal.contentassist.JSPContentAssistProcessor;
import org.eclipse.jst.jsp.ui.internal.contentassist.JSPELContentAssistProcessor;
import org.eclipse.jst.jsp.ui.internal.contentassist.JSPJavaContentAssistProcessor;
import org.eclipse.jst.jsp.ui.internal.contentassist.NoRegionContentAssistProcessorForJSP;
import org.eclipse.jst.jsp.ui.internal.format.FormattingStrategyJSPJava;
import org.eclipse.jst.jsp.ui.internal.hyperlink.JSPJavaHyperlinkDetector;
import org.eclipse.jst.jsp.ui.internal.hyperlink.TaglibHyperlinkDetector;
import org.eclipse.jst.jsp.ui.internal.hyperlink.XMLHyperlinkDetector;
import org.eclipse.jst.jsp.ui.internal.style.LineStyleProviderForJSP;
import org.eclipse.jst.jsp.ui.internal.style.java.LineStyleProviderForJava;
import org.eclipse.jst.jsp.ui.internal.style.jspel.LineStyleProviderForJSPEL;
import org.eclipse.jst.jsp.ui.internal.taginfo.JSPJavaJavadocHoverProcessor;
import org.eclipse.jst.jsp.ui.internal.taginfo.JSPTagInfoHoverProcessor;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;
import org.eclipse.wst.css.core.internal.provisional.text.ICSSPartitionTypes;
import org.eclipse.wst.html.core.internal.format.HTMLFormatProcessorImpl;
import org.eclipse.wst.html.core.internal.provisional.text.IHTMLPartitionTypes;
import org.eclipse.wst.html.ui.StructuredTextViewerConfigurationHTML;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredPartitionTypes;
import org.eclipse.wst.sse.ui.StructuredTextViewerConfiguration;
import org.eclipse.wst.sse.ui.internal.SSEUIPlugin;
import org.eclipse.wst.sse.ui.internal.format.StructuredFormattingStrategy;
import org.eclipse.wst.sse.ui.internal.provisional.style.LineStyleProvider;
import org.eclipse.wst.sse.ui.internal.taginfo.TextHoverManager;
import org.eclipse.wst.sse.ui.internal.util.EditorUtility;
import org.eclipse.wst.xml.core.internal.provisional.text.IXMLPartitions;
import org.eclipse.wst.xml.ui.StructuredTextViewerConfigurationXML;

public class StructuredTextViewerConfigurationJSP extends StructuredTextViewerConfiguration {
	/*
	 * One instance per configuration because not sourceviewer-specific and
	 * it's a String array
	 */
	private String[] fConfiguredContentTypes;
	/*
	 * One instance per configuration
	 */
	private LineStyleProvider fLineStyleProviderForJava;
	/*
	 * One instance per configuration
	 */
	private LineStyleProvider fLineStyleProviderForJSP;
	/*
	 * One instance per configuration
	 */
	private LineStyleProvider fLineStyleProviderForJSPEL;
	private StructuredTextViewerConfiguration fHTMLSourceViewerConfiguration;
	private JavaSourceViewerConfiguration fJavaSourceViewerConfiguration;
	private StructuredTextViewerConfiguration fXMLSourceViewerConfiguration;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getAutoEditStrategies(org.eclipse.jface.text.source.ISourceViewer,
	 *      java.lang.String)
	 */
	public IAutoEditStrategy[] getAutoEditStrategies(ISourceViewer sourceViewer, String contentType) {
		IAutoEditStrategy[] strategies = null;

		if (contentType == IXMLPartitions.XML_DEFAULT) {
			// xml autoedit strategies
			strategies = getXMLSourceViewerConfiguration().getAutoEditStrategies(sourceViewer, contentType);
		}
		else if (contentType == IJSPPartitionTypes.JSP_CONTENT_JAVA) {
			// jsp java autoedit strategies
			List allStrategies = new ArrayList(0);

			IAutoEditStrategy[] javaStrategies = getJavaSourceViewerConfiguration().getAutoEditStrategies(sourceViewer, IJavaPartitions.JAVA_PARTITIONING);
			for (int i = 0; i < javaStrategies.length; i++) {
				allStrategies.add(javaStrategies[i]);
			}
			// be sure this is added last, after others, so it can modify
			// results from earlier steps.
			// add auto edit strategy that handles when tab key is pressed
			allStrategies.add(new AutoEditStrategyForTabs());

			strategies = (IAutoEditStrategy[]) allStrategies.toArray(new IAutoEditStrategy[allStrategies.size()]);
		}
		else if (contentType == IHTMLPartitionTypes.HTML_DEFAULT || contentType == IHTMLPartitionTypes.HTML_DECLARATION) {
			// html and jsp autoedit strategies
			List allStrategies = new ArrayList(0);

			// add the jsp autoedit strategy first then add all html's
			allStrategies.add(new StructuredAutoEditStrategyJSP());

			IAutoEditStrategy[] htmlStrategies = getHTMLSourceViewerConfiguration().getAutoEditStrategies(sourceViewer, contentType);
			for (int i = 0; i < htmlStrategies.length; i++) {
				allStrategies.add(htmlStrategies[i]);
			}

			strategies = (IAutoEditStrategy[]) allStrategies.toArray(new IAutoEditStrategy[allStrategies.size()]);
		}
		else {
			// default autoedit strategies
			List allStrategies = new ArrayList(0);

			IAutoEditStrategy[] superStrategies = super.getAutoEditStrategies(sourceViewer, contentType);
			for (int i = 0; i < superStrategies.length; i++) {
				allStrategies.add(superStrategies[i]);
			}

			// be sure this is added last, after others, so it can modify
			// results from earlier steps.
			// add auto edit strategy that handles when tab key is pressed
			allStrategies.add(new AutoEditStrategyForTabs());

			strategies = (IAutoEditStrategy[]) allStrategies.toArray(new IAutoEditStrategy[allStrategies.size()]);
		}

		return strategies;
	}

	public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
		if (fConfiguredContentTypes == null) {
			/*
			 * A little bit of cheating because assuming html's configured
			 * content types will add default, unknown, and all xml configured
			 * content types
			 */
			String[] htmlTypes = getHTMLSourceViewerConfiguration().getConfiguredContentTypes(sourceViewer);
			String[] jspTypes = StructuredTextPartitionerForJSP.getConfiguredContentTypes();
			fConfiguredContentTypes = new String[htmlTypes.length + jspTypes.length];

			int index = 0;
			System.arraycopy(htmlTypes, 0, fConfiguredContentTypes, index, htmlTypes.length);
			System.arraycopy(jspTypes, 0, fConfiguredContentTypes, index += htmlTypes.length, jspTypes.length);
		}

		return fConfiguredContentTypes;
	}

	protected IContentAssistProcessor[] getContentAssistProcessors(ISourceViewer sourceViewer, String partitionType) {
		IContentAssistProcessor[] processors = null;

		if (partitionType == IHTMLPartitionTypes.SCRIPT) {
			// HTML JavaScript
			IContentAssistant htmlContentAssistant = getHTMLSourceViewerConfiguration().getContentAssistant(sourceViewer);
			IContentAssistProcessor processor = htmlContentAssistant.getContentAssistProcessor(IHTMLPartitionTypes.SCRIPT);
			processors = new IContentAssistProcessor[]{processor};
		}
		else if (partitionType == ICSSPartitionTypes.STYLE) {
			// HTML CSS
			IContentAssistant htmlContentAssistant = getHTMLSourceViewerConfiguration().getContentAssistant(sourceViewer);
			IContentAssistProcessor processor = htmlContentAssistant.getContentAssistProcessor(ICSSPartitionTypes.STYLE);
			processors = new IContentAssistProcessor[]{processor};
		}
		// // jspcontentassistprocessor handles this?
		// else if ((partitionType == IHTMLPartitionTypes.HTML_DEFAULT) ||
		// (partitionType == IHTMLPartitionTypes.HTML_COMMENT)) {
		// processors = new IContentAssistProcessor[]{new
		// HTMLContentAssistProcessor()};
		// }
		else if ((partitionType == IXMLPartitions.XML_DEFAULT) || (partitionType == IHTMLPartitionTypes.HTML_DEFAULT) || (partitionType == IHTMLPartitionTypes.HTML_COMMENT) || (partitionType == IJSPPartitionTypes.JSP_DEFAULT) || (partitionType == IJSPPartitionTypes.JSP_DIRECTIVE) || (partitionType == IJSPPartitionTypes.JSP_CONTENT_DELIMITER) || (partitionType == IJSPPartitionTypes.JSP_CONTENT_JAVASCRIPT) || (partitionType == IJSPPartitionTypes.JSP_COMMENT)) {
			// jsp
			processors = new IContentAssistProcessor[]{new JSPContentAssistProcessor()};
		}
		else if ((partitionType == IXMLPartitions.XML_CDATA) || (partitionType == IJSPPartitionTypes.JSP_CONTENT_JAVA)) {
			// jsp java
			processors = new IContentAssistProcessor[]{new JSPJavaContentAssistProcessor()};
		}
		else if (partitionType == IJSPPartitionTypes.JSP_DEFAULT_EL) {
			// jsp el
			processors = new IContentAssistProcessor[]{new JSPELContentAssistProcessor()};
		}
		else if (partitionType == IStructuredPartitionTypes.UNKNOWN_PARTITION) {
			// unknown
			processors = new IContentAssistProcessor[]{new NoRegionContentAssistProcessorForJSP()};
		}

		return processors;
	}

	public IContentFormatter getContentFormatter(ISourceViewer sourceViewer) {
		MultiPassContentFormatter formatter = new MultiPassContentFormatter(getConfiguredDocumentPartitioning(sourceViewer), IXMLPartitions.XML_DEFAULT);

		formatter.setMasterStrategy(new StructuredFormattingStrategy(new HTMLFormatProcessorImpl()));
		formatter.setSlaveStrategy(new FormattingStrategyJSPJava(), IJSPPartitionTypes.JSP_CONTENT_JAVA);

		return formatter;
	}

	public ITextDoubleClickStrategy getDoubleClickStrategy(ISourceViewer sourceViewer, String contentType) {
		ITextDoubleClickStrategy strategy = null;

		// html or javascript
		if (contentType == IHTMLPartitionTypes.HTML_DEFAULT || contentType == IHTMLPartitionTypes.SCRIPT)
			strategy = getHTMLSourceViewerConfiguration().getDoubleClickStrategy(sourceViewer, contentType);
		else if (contentType == IJSPPartitionTypes.JSP_CONTENT_JAVA || contentType == IJSPPartitionTypes.JSP_CONTENT_JAVASCRIPT)
			// JSP Java or JSP JavaScript
			strategy = getJavaSourceViewerConfiguration().getDoubleClickStrategy(sourceViewer, contentType);
		else if (contentType == IJSPPartitionTypes.JSP_DEFAULT)
			// JSP (just treat like html)
			strategy = getHTMLSourceViewerConfiguration().getDoubleClickStrategy(sourceViewer, IHTMLPartitionTypes.HTML_DEFAULT);
		else
			strategy = super.getDoubleClickStrategy(sourceViewer, contentType);

		return strategy;
	}

	private StructuredTextViewerConfiguration getHTMLSourceViewerConfiguration() {
		if (fHTMLSourceViewerConfiguration == null) {
			fHTMLSourceViewerConfiguration = new StructuredTextViewerConfigurationHTML();
		}
		return fHTMLSourceViewerConfiguration;
	}

	public IHyperlinkDetector[] getHyperlinkDetectors(ISourceViewer sourceViewer) {
		if (fPreferenceStore == null)
			return null;
		if (sourceViewer == null || !fPreferenceStore.getBoolean(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_HYPERLINKS_ENABLED))
			return null;

		List allDetectors = new ArrayList(0);
		allDetectors.add(new JSPJavaHyperlinkDetector());
		allDetectors.add(new TaglibHyperlinkDetector());
		allDetectors.add(new XMLHyperlinkDetector());

		IHyperlinkDetector[] superDetectors = super.getHyperlinkDetectors(sourceViewer);
		for (int m = 0; m < superDetectors.length; m++) {
			IHyperlinkDetector detector = superDetectors[m];
			if (!allDetectors.contains(detector)) {
				allDetectors.add(detector);
			}
		}
		return (IHyperlinkDetector[]) allDetectors.toArray(new IHyperlinkDetector[0]);
	}

	public String[] getIndentPrefixes(ISourceViewer sourceViewer, String contentType) {
		String[] indentations = null;

		if (contentType == IXMLPartitions.XML_DEFAULT)
			indentations = getXMLSourceViewerConfiguration().getIndentPrefixes(sourceViewer, contentType);
		else
			indentations = getHTMLSourceViewerConfiguration().getIndentPrefixes(sourceViewer, contentType);

		return indentations;
	}

	protected IInformationProvider getInformationProvider(ISourceViewer sourceViewer, String partitionType) {
		IInformationProvider provider = null;
		if (partitionType == IHTMLPartitionTypes.HTML_DEFAULT) {
			// HTML
			IInformationPresenter htmlPresenter = getHTMLSourceViewerConfiguration().getInformationPresenter(sourceViewer);
			provider = htmlPresenter.getInformationProvider(IHTMLPartitionTypes.HTML_DEFAULT);
		}
		else if (partitionType == IHTMLPartitionTypes.SCRIPT) {
			// HTML JavaScript
			IInformationPresenter htmlPresenter = getHTMLSourceViewerConfiguration().getInformationPresenter(sourceViewer);
			provider = htmlPresenter.getInformationProvider(IHTMLPartitionTypes.SCRIPT);
		}
		else if (partitionType == IXMLPartitions.XML_DEFAULT) {
			// XML
			IInformationPresenter xmlPresenter = getXMLSourceViewerConfiguration().getInformationPresenter(sourceViewer);
			provider = xmlPresenter.getInformationProvider(IXMLPartitions.XML_DEFAULT);
		}
		return provider;
	}

	private JavaSourceViewerConfiguration getJavaSourceViewerConfiguration() {
		if (fJavaSourceViewerConfiguration == null) {
			IPreferenceStore store = PreferenceConstants.getPreferenceStore();
			JavaTextTools javaTextTools = new JavaTextTools(store);
			/*
			 * NOTE: null text editor is being passed to
			 * JavaSourceViewerConfiguration because
			 * StructuredTextViewerConfiguration does not know current editor.
			 * this is okay because editor is not needed in the cases we are
			 * using javasourceviewerconfiguration.
			 */
			fJavaSourceViewerConfiguration = new JavaSourceViewerConfiguration(javaTextTools.getColorManager(), store, null, IJavaPartitions.JAVA_PARTITIONING);
		}
		return fJavaSourceViewerConfiguration;
	}

	public LineStyleProvider[] getLineStyleProviders(ISourceViewer sourceViewer, String partitionType) {
		LineStyleProvider[] providers = null;

		if (partitionType == IHTMLPartitionTypes.HTML_DEFAULT || partitionType == IHTMLPartitionTypes.HTML_COMMENT || partitionType == IHTMLPartitionTypes.HTML_DECLARATION) {
			providers = getHTMLSourceViewerConfiguration().getLineStyleProviders(sourceViewer, IHTMLPartitionTypes.HTML_DEFAULT);
		}
		else if (partitionType == IHTMLPartitionTypes.SCRIPT || partitionType == IJSPPartitionTypes.JSP_CONTENT_JAVASCRIPT) {
			providers = getHTMLSourceViewerConfiguration().getLineStyleProviders(sourceViewer, IHTMLPartitionTypes.SCRIPT);
		}
		else if (partitionType == ICSSPartitionTypes.STYLE) {
			providers = getHTMLSourceViewerConfiguration().getLineStyleProviders(sourceViewer, ICSSPartitionTypes.STYLE);
		}
		else if (partitionType == IXMLPartitions.XML_DEFAULT || partitionType == IXMLPartitions.XML_CDATA || partitionType == IXMLPartitions.XML_COMMENT || partitionType == IXMLPartitions.XML_DECLARATION || partitionType == IXMLPartitions.XML_PI) {
			providers = getXMLSourceViewerConfiguration().getLineStyleProviders(sourceViewer, IXMLPartitions.XML_DEFAULT);
		}
		else if (partitionType == IJSPPartitionTypes.JSP_CONTENT_JAVA) {
			providers = new LineStyleProvider[]{getLineStyleProviderForJava()};
		}
		else if (partitionType == IJSPPartitionTypes.JSP_DEFAULT_EL) {
			providers = new LineStyleProvider[]{getLineStyleProviderForJSPEL()};
		}
		else if (partitionType == IJSPPartitionTypes.JSP_COMMENT || partitionType == IJSPPartitionTypes.JSP_CONTENT_DELIMITER || partitionType == IJSPPartitionTypes.JSP_DEFAULT || partitionType == IJSPPartitionTypes.JSP_DIRECTIVE) {
			providers = new LineStyleProvider[]{getLineStyleProviderForJSP()};
		}

		return providers;
	}

	private LineStyleProvider getLineStyleProviderForJava() {
		if (fLineStyleProviderForJava == null) {
			fLineStyleProviderForJava = new LineStyleProviderForJava();
		}
		return fLineStyleProviderForJava;
	}

	private LineStyleProvider getLineStyleProviderForJSP() {
		if (fLineStyleProviderForJSP == null) {
			fLineStyleProviderForJSP = new LineStyleProviderForJSP();
		}
		return fLineStyleProviderForJSP;
	}

	private LineStyleProvider getLineStyleProviderForJSPEL() {
		if (fLineStyleProviderForJSPEL == null) {
			fLineStyleProviderForJSPEL = new LineStyleProviderForJSPEL();
		}
		return fLineStyleProviderForJSPEL;
	}

	public ITextHover getTextHover(ISourceViewer sourceViewer, String contentType, int stateMask) {
		ITextHover hover = null;

		if (contentType == IHTMLPartitionTypes.HTML_DEFAULT || contentType == IHTMLPartitionTypes.SCRIPT) {
			// html and javascript regions
			hover = getHTMLSourceViewerConfiguration().getTextHover(sourceViewer, contentType, stateMask);
		}
		else if (contentType == IXMLPartitions.XML_DEFAULT) {
			// xml regions
			hover = getXMLSourceViewerConfiguration().getTextHover(sourceViewer, contentType, stateMask);
		}
		else if ((contentType == IJSPPartitionTypes.JSP_DEFAULT) || (contentType == IJSPPartitionTypes.JSP_DIRECTIVE) || (contentType == IJSPPartitionTypes.JSP_CONTENT_JAVA)) {
			TextHoverManager manager = SSEUIPlugin.getDefault().getTextHoverManager();
			TextHoverManager.TextHoverDescriptor[] hoverDescs = manager.getTextHovers();
			int i = 0;
			while (i < hoverDescs.length && hover == null) {
				if (hoverDescs[i].isEnabled() && EditorUtility.computeStateMask(hoverDescs[i].getModifierString()) == stateMask) {
					String hoverType = hoverDescs[i].getId();
					if (TextHoverManager.COMBINATION_HOVER.equalsIgnoreCase(hoverType)) {
						if ((contentType == IJSPPartitionTypes.JSP_DEFAULT) || (contentType == IJSPPartitionTypes.JSP_DIRECTIVE)) {
							// JSP
							hover = manager.createBestMatchHover(new JSPTagInfoHoverProcessor());
						}
						else {
							// JSP Java
							hover = manager.createBestMatchHover(new JSPJavaJavadocHoverProcessor());
						}
					}
					else if (TextHoverManager.DOCUMENTATION_HOVER.equalsIgnoreCase(hoverType)) {
						if ((contentType == IJSPPartitionTypes.JSP_DEFAULT) || (contentType == IJSPPartitionTypes.JSP_DIRECTIVE)) {
							// JSP
							hover = new JSPTagInfoHoverProcessor();
						}
						else {
							// JSP Java
							hover = new JSPJavaJavadocHoverProcessor();
						}
					}
				}
				i++;
			}
		}

		// no appropriate text hovers found, try super
		if (hover == null)
			hover = super.getTextHover(sourceViewer, contentType, stateMask);
		return hover;
	}

	private StructuredTextViewerConfiguration getXMLSourceViewerConfiguration() {
		if (fXMLSourceViewerConfiguration == null) {
			fXMLSourceViewerConfiguration = new StructuredTextViewerConfigurationXML();
		}
		return fXMLSourceViewerConfiguration;
	}
}
