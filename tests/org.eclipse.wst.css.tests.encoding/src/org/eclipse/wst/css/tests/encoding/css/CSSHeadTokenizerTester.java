/*******************************************************************************
 * Copyright (c) 2004, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.wst.css.tests.encoding.css;

import java.io.IOException;
import java.io.Reader;

import junit.framework.TestCase;

import org.eclipse.wst.css.core.internal.contenttype.CSSHeadTokenizer;
import org.eclipse.wst.css.core.internal.contenttype.CSSHeadTokenizerConstants;
import org.eclipse.wst.css.core.internal.contenttype.HeadParserToken;
import org.eclipse.wst.css.tests.encoding.CSSEncodingTestsPlugin;

public class CSSHeadTokenizerTester extends TestCase {
	private boolean DEBUG = false;
	private String fcharset;
	private final String fileDir = "css/";
	private final String fileHome = "testfiles/";
	private final String fileLocation = this.fileHome + this.fileDir;

	private void doTestFile(String filename, String expectedName) throws IOException {
		doTestFile(filename, expectedName, null);
	}

	private void doTestFile(String filename, String expectedName, String finalTokenType) throws IOException {
		CSSHeadTokenizer tokenizer = null;
		Reader fileReader = null;
		try {
			if (this.DEBUG) {
				System.out.println();
				System.out.println("       " + filename);
				System.out.println();
			}
			fileReader = CSSEncodingTestsPlugin.getTestReader(filename);
			tokenizer = new CSSHeadTokenizer(fileReader);
		}
		catch (IOException e) {
			System.out.println("Error opening file \"" + filename + "\"");
			throw e;
		}

		HeadParserToken resultToken = null;
		HeadParserToken token = parseHeader(tokenizer);
		String resultValue = this.fcharset;
		fileReader.close();
		if (finalTokenType != null) {
			assertTrue("did not end as expected. found:  " + token.getType(), finalTokenType.equals(token.getType()));
		}
		else {
			if (expectedName == null) {
				assertTrue("expected no encoding, but found: " + resultValue, resultToken == null);
			}
			else {
				assertTrue("expected " + expectedName + " but found " + resultValue, expectedName.equals(resultValue));
			}
		}

	}

	private boolean isLegalString(String tokenType) {
		boolean result = false;
		if (tokenType == null) {
			result = false;
		}
		else {
			result = tokenType.equals(EncodingParserConstants.StringValue) || tokenType.equals(EncodingParserConstants.UnDelimitedStringValue) || tokenType.equals(EncodingParserConstants.InvalidTerminatedStringValue) || tokenType.equals(EncodingParserConstants.InvalidTermintatedUnDelimitedStringValue);
		}
		return result;
	}

	/**
	 * Give's priority to encoding value, if found else, looks for contentType
	 * value;
	 */
	private HeadParserToken parseHeader(CSSHeadTokenizer tokenizer) throws IOException {
		HeadParserToken token = null;
		HeadParserToken finalToken = null;
		do {
			token = tokenizer.getNextToken();
			String tokenType = token.getType();
			if (tokenType == CSSHeadTokenizerConstants.CHARSET_RULE) {
				if (tokenizer.hasMoreTokens()) {
					HeadParserToken valueToken = tokenizer.getNextToken();
					String valueTokenType = valueToken.getType();
					if (isLegalString(valueTokenType)) {
						this.fcharset = valueToken.getText();

					}
				}
			}
		}
		while (tokenizer.hasMoreTokens());
		finalToken = token;
		return finalToken;

	}

	public void testBestCase() throws IOException {
		String filename = this.fileLocation + "nonStandard.css";
		doTestFile(filename, "ISO-8859-6");

	}

	public void testEmptyFile() throws IOException {
		String filename = this.fileLocation + "emptyFile.css";
		doTestFile(filename, null);
	}

	public void _testEUCJP() throws IOException {
		String filename = this.fileLocation + "encoding_test_eucjp.css";
		doTestFile(filename, "EUC-JP");
	}

	public void testJIS() throws IOException {
		String filename = this.fileLocation + "encoding_test_jis.css";
		doTestFile(filename, "ISO-2022-JP");
	}

	public void testNoEncoding() throws IOException {
		String filename = this.fileLocation + "noEncoding.css";
		doTestFile(filename, null);
	}

	public void testnonStandardIllFormed() throws IOException {
		String filename = this.fileLocation + "nonStandardIllFormed.css";
		doTestFile(filename, "ISO-8859-6");
	}

	public void testnonStandardIllFormed2() throws IOException {
		String filename = this.fileLocation + "nonStandardIllFormed2.css";
		doTestFile(filename, "ISO-8859-6");
	}

	public void _testShiftJIS() throws IOException {
		String filename = this.fileLocation + "encoding_test_sjis.css";
		doTestFile(filename, "SHIFT_JIS");
	}
}