/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.php.editor.indent;

import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.editor.BaseDocument;
import org.netbeans.lib.lexer.test.TestLanguageProvider;
import org.netbeans.modules.csl.api.Formatter;
import org.netbeans.modules.editor.indent.spi.CodeStylePreferences;
import org.netbeans.modules.html.editor.lib.api.HtmlVersion;
import org.netbeans.modules.php.editor.PHPTestBase;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;
import org.netbeans.modules.php.editor.parser.GSFPHPParserTestUtil;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public abstract class PHPFormatterTestBase extends PHPTestBase {

    private String FORMAT_START_MARK = "/*FORMAT_START*/"; //NOI18N
    private String FORMAT_END_MARK = "/*FORMAT_END*/"; //NOI18N

    public PHPFormatterTestBase(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        try {
            TestLanguageProvider.register(HTMLTokenId.language());
        } catch (IllegalStateException ise) {
            // Ignore -- we've already registered this either via layers or other means
        }
        try {
            TestLanguageProvider.register(PHPTokenId.language());
        } catch (IllegalStateException ise) {
            // Ignore -- we've already registered this either via layers or other means
        }
        HtmlVersion.DEFAULT_VERSION_UNIT_TESTS_OVERRIDE = HtmlVersion.HTML41_TRANSATIONAL;
    }

    protected void reformatFileContents(String file, IndentPrefs indentPrefs, int initialIndent) throws Exception {
        FileObject fo = getTestFile(file);
        assertNotNull(fo);
        BaseDocument doc = getDocument(fo);
        assertNotNull(doc);
        String fullTxt = doc.getText(0, doc.getLength());
        int formatStart = 0;
        int formatEnd = doc.getLength();
        int startMarkPos = fullTxt.indexOf(FORMAT_START_MARK);

        if (startMarkPos >= 0) {
            formatStart = startMarkPos + FORMAT_START_MARK.length();
            formatEnd = fullTxt.indexOf(FORMAT_END_MARK);

            if (formatEnd == -1) {
                throw new IllegalStateException();
            }
        }

        Formatter formatter = getFormatter(indentPrefs);

        setupDocumentIndentation(doc, indentPrefs);

        Map<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.INITIAL_INDENT, initialIndent);
        if (indentPrefs != null) {
            options.put(FmtOptions.INDENT_SIZE, indentPrefs.getIndentation());
        }
        options.put(FmtOptions.CONTINUATION_INDENT_SIZE, 4);
        Preferences prefs = CodeStylePreferences.get(doc).getPreferences();
        for (Map.Entry<String, Object> entry : options.entrySet()) {
            String option = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof Integer) {
                prefs.putInt(option, ((Integer) value).intValue());
            } else if (value instanceof String) {
                prefs.put(option, (String) value);
            } else if (value instanceof Boolean) {
                prefs.put(option, ((Boolean) value).toString());
            } else if (value instanceof CodeStyle.BracePlacement) {
                prefs.put(option, ((CodeStyle.BracePlacement) value).name());
            } else if (value instanceof CodeStyle.WrapStyle) {
                prefs.put(option, ((CodeStyle.WrapStyle) value).name());
            }
        }

        format(doc, formatter, formatStart, formatEnd, false);

        String after = doc.getText(0, doc.getLength());
        assertDescriptionMatches(file, after, false, ".formatted");
    }

    protected void reformatFileContents(String file, Map<String, Object> options) throws Exception {
        reformatFileContents(file, options, false);
    }

    protected void reformatFileContents(String file, Map<String, Object> options, boolean isTemplate) throws Exception {
        reformatFileContents(file, options, isTemplate, false, new IndentPrefs(4, 4));
    }

    protected void reformatFileContents(String file, Map<String, Object> options, boolean isTemplate, boolean includeTestName) throws Exception {
        reformatFileContents(file, options, isTemplate, includeTestName, new IndentPrefs(4, 4));
    }

    private void reformatFileContents(String file, Map<String, Object> options, boolean isTemplate, boolean includeTestName, IndentPrefs indentPrefs) throws Exception {
        FileObject fo = getTestFile(file);
        assertNotNull(fo);

        String text = read(fo);

        int formatStart = 0;
        int formatEnd = text.length();
        int startMarkPos = text.indexOf(FORMAT_START_MARK);

        if (startMarkPos >= 0) {
            formatStart = startMarkPos;
            text = text.substring(0, formatStart) + text.substring(formatStart + FORMAT_START_MARK.length());
            formatEnd = text.indexOf(FORMAT_END_MARK);
            text = text.substring(0, formatEnd) + text.substring(formatEnd + FORMAT_END_MARK.length());
            GSFPHPParserTestUtil.setUnitTestCaretPosition(formatEnd);
            if (!isTemplate) {
                formatEnd --;
            }
            if (formatEnd == -1) {
                throw new IllegalStateException();
            }
        }

        BaseDocument doc = getDocument(text);
        assertNotNull(doc);

        if (isTemplate) {
            int carretPosition = text.indexOf('^');
            if (carretPosition == -1) {
                carretPosition = formatEnd;
            } else {
                if (carretPosition < formatStart) {
                    formatStart--;
                }
                if (carretPosition < formatEnd) {
                    formatEnd--;
                }
                text = text.substring(0, carretPosition) + text.substring(carretPosition + 1);
            }

            TokenFormatter.setUnitTestCarretPosition(carretPosition);
            GSFPHPParserTestUtil.setUnitTestCaretPosition(carretPosition);
            doc.remove(0, doc.getLength());
            doc.insertString(0, text, null);
            doc.putProperty(TokenFormatter.TEMPLATE_HANDLER_PROPERTY, new Object());
        }

        Formatter formatter = getFormatter(indentPrefs);
        //assertNotNull("getFormatter must be implemented", formatter);

        setupDocumentIndentation(doc, indentPrefs);

        Preferences prefs = CodeStylePreferences.get(doc).getPreferences();
        for (Map.Entry<String, Object> entry : options.entrySet()) {
            String option = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof Integer) {
                prefs.putInt(option, ((Integer) value).intValue());
            } else if (value instanceof String) {
                prefs.put(option, (String) value);
            } else if (value instanceof Boolean) {
                prefs.put(option, ((Boolean) value).toString());
            } else if (value instanceof CodeStyle.BracePlacement) {
                prefs.put(option, ((CodeStyle.BracePlacement) value).name());
            } else if (value instanceof CodeStyle.WrapStyle) {
                prefs.put(option, ((CodeStyle.WrapStyle) value).name());
            }
        }

        format(doc, formatter, formatStart, formatEnd, false);
        String after = doc.getText(0, doc.getLength());
        assertDescriptionMatches(file, after, includeTestName, ".formatted");
        GSFPHPParserTestUtil.setUnitTestCaretPosition(-1);
    }
}
