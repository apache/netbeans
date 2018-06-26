/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript2.editor.formatter;

import com.oracle.js.parser.ir.FunctionNode;
import java.util.Dictionary;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.Formatter;
import org.netbeans.modules.csl.api.test.CslTestBase.IndentPrefs;
import org.netbeans.modules.editor.indent.api.Indent;
import org.netbeans.modules.editor.indent.spi.CodeStylePreferences;
import org.netbeans.modules.javascript2.editor.JsTestBase;
import org.netbeans.modules.javascript2.lexer.api.JsTokenId;
import org.netbeans.modules.javascript2.lexer.api.LexUtilities;
import org.netbeans.modules.javascript2.editor.parser.JsParser;
import org.netbeans.modules.javascript2.editor.parser.JsParserResult;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Source;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Petr Hejl
 */
public class JsFormatterTestBase extends JsTestBase {

    public static String FORMAT_START_MARK = "/*FORMAT_START*/"; //NOI18N

    public static String FORMAT_END_MARK = "/*FORMAT_END*/"; //NOI18N

    public JsFormatterTestBase(String testName) {
        super(testName);
    }
    
    protected void dumpFormatTokens(String file) throws Exception {
        FileObject fo = getTestFile(file);
        assertNotNull(fo);

        String text = read(fo);

        dumpFormatTokens(file, text, 0, text.length());
    }

    protected void dumpFormatTokens(String file, String text, int startOffset, int endOffset) throws Exception {

        Document doc = getDocument(text);
        Snapshot snapshot = Source.create(doc).createSnapshot();

        TokenSequence<? extends JsTokenId> ts = LexUtilities.getTokenSequence(
                snapshot.getTokenHierarchy(), startOffset, JsTokenId.javascriptLanguage());

        FormatTokenStream tokenStream = FormatTokenStream.create(null,
                ts, startOffset, endOffset);
        JsFormatVisitor visitor = new JsFormatVisitor(tokenStream,
                ts, endOffset);

        JsParser parser = new JsParser();
        parser.parse(snapshot, null, null);
        FunctionNode root = ((JsParserResult) parser.getResult(null)).getRoot();
        if (root != null) {
            root.accept(visitor);
        }

        StringBuilder sb = new StringBuilder();
        for (FormatToken token : tokenStream.getTokens()) {
            sb.append(token.toString()).append("\n");
        }
        assertDescriptionMatches(file, sb.toString(), false, ".formatTokens");
    }

    protected void reformatFileContents(String file, Map<String, Object> options) throws Exception {
        reformatFileContents(file, options, null, false);
    }

    protected void reformatFileContents(String file, Map<String, Object> options, String suffix) throws Exception {
        reformatFileContents(file, options, suffix, false);
    }

    protected void reformatFileContents(String file, Map<String, Object> options, String suffix, boolean template) throws Exception {
        FileObject fo = getTestFile(file);
        assertNotNull(fo);

        String text = read(fo);

        int formatStart = 0;
        int formatEnd = text.length();
        int startMarkPos = text.indexOf(FORMAT_START_MARK);

        if (startMarkPos >= 0){
            formatStart = startMarkPos;
            text = text.substring(0, formatStart) + text.substring(formatStart + FORMAT_START_MARK.length());
            formatEnd = text.indexOf(FORMAT_END_MARK);
            text = text.substring(0, formatEnd) + text.substring(formatEnd + FORMAT_END_MARK.length());
            formatEnd --;
            if (formatEnd == -1){
                throw new IllegalStateException();
            }
        }

        BaseDocument doc = getDocument(text);
        assertNotNull(doc);

        if (template) {
            Dictionary<Object, Object> dict = doc.getDocumentProperties();
            dict.put(JsFormatter.CT_HANDLER_DOC_PROPERTY, "test");
        }

        IndentPrefs preferences = new IndentPrefs(4, 4);
        Formatter formatter = getFormatter(preferences);
        //assertNotNull("getFormatter must be implemented", formatter);

        setupDocumentIndentation(doc, preferences);

        Preferences prefs = CodeStylePreferences.get(doc).getPreferences();
        // clear prefs
        prefs.clear();

        prefs = CodeStylePreferences.get(doc).getPreferences();
        for (String option : options.keySet()) {
            assertTrue(FmtOptions.tabSize.equals(option)
                    || FmtOptions.expandTabToSpaces.equals(option)
                    || FmtOptions.indentSize.equals(option)
                    || FmtOptions.rightMargin.equals(option)
                    || prefs.get(option, null) == null);
            Object value = options.get(option);
            if (value instanceof CodeStyle.BracePlacement) {
                prefs.put(option, ((CodeStyle.BracePlacement) value).name());
            } else if (value instanceof CodeStyle.WrapStyle) {
                prefs.put(option, ((CodeStyle.WrapStyle) value).name());
            } else {
                prefs.put(option, value.toString());
            }
        }

        try {
            format(doc, formatter, formatStart, formatEnd, false);
            // XXX tests fails randomly on this with JDK7
            // XXX so we aretrying to track down whats happening
            Logger.getAnonymousLogger().log(Level.INFO,
                    "Space before method call setting: " + CodeStyle.get(doc, Defaults.getInstance(JsTokenId.JAVASCRIPT_MIME_TYPE)).spaceBeforeMethodCallParen());
        } finally {
            for (String option : options.keySet()) {
                prefs.remove(option);
                assertTrue(FmtOptions.tabSize.equals(option)
                        || FmtOptions.expandTabToSpaces.equals(option)
                        || FmtOptions.indentSize.equals(option)
                        || FmtOptions.rightMargin.equals(option)
                        || prefs.get(option, null) == null);
            }
        }
        String after = doc.getText(0, doc.getLength());
        String realSuffix = ".formatted";
        if (suffix != null) {
            realSuffix = suffix;
        }
 
        assertDescriptionMatches(file, after, false, realSuffix);
    }

    protected void reindentFileContents(String file, IndentPrefs preferences) throws Exception {
        FileObject fo = getTestFile(file);
        assertNotNull(fo);

        String text = read(fo);

        int formatStart = 0;
        int formatEnd = text.length();

        BaseDocument doc = getDocument(text);
        assertNotNull(doc);

        Formatter formatter = getFormatter(preferences);
        //assertNotNull("getFormatter must be implemented", formatter);

        setupDocumentIndentation(doc, preferences);

        indent(doc, formatter, formatStart, formatEnd);

        String after = doc.getText(0, doc.getLength());
        assertDescriptionMatches(file, after, false, ".indented");
    }

    private void indent(Document document, Formatter formatter, int startPos, int endPos) throws BadLocationException {
        //assertTrue(SwingUtilities.isEventDispatchThread());
        configureIndenters(document, formatter, true);

        final Indent i = Indent.get(document);
        i.lock();
        try {
            if (document instanceof BaseDocument) {
                ((BaseDocument) document).atomicLock();
            }
            try {
                i.reindent(Math.min(document.getLength(), startPos), Math.min(document.getLength(), endPos));
            } finally {
                if (document instanceof BaseDocument) {
                    ((BaseDocument) document).atomicUnlock();
                }
            }
        } finally {
            i.unlock();
        }
    }
}
