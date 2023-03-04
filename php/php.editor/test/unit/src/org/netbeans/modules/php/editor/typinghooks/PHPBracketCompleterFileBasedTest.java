/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.php.editor.typinghooks;

/**
 *
 * @author Petr Pisl
 */

import java.util.concurrent.Future;
import java.util.prefs.Preferences;
import javax.swing.JEditorPane;
import javax.swing.text.Caret;
import javax.swing.text.DefaultEditorKit;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.editor.BaseDocument;
import org.netbeans.lib.lexer.test.TestLanguageProvider;
import org.netbeans.modules.csl.api.Formatter;
import org.netbeans.modules.editor.indent.spi.CodeStylePreferences;
import org.netbeans.modules.php.editor.PHPTestBase;
import org.netbeans.modules.php.editor.indent.FmtOptions;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;
import org.openide.filesystems.FileObject;

public class PHPBracketCompleterFileBasedTest extends PHPTestBase {

    public PHPBracketCompleterFileBasedTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
	//System.setProperty("org.netbeans.editor.linewrap.disable", "true");
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
    }

    @Override
    protected boolean runInEQ() {
        return true;
    }

    protected void testIndentInFile(String file) throws Exception {
        testIndentInFile(file, null, 0);
    }

    protected void testIndentInFile(String file, IndentPrefs preferences, int initialIndent) throws Exception {
        FileObject fo = getTestFile(file);
        assertNotNull(fo);
        String source = readFile(fo);

        int sourcePos = source.indexOf('^');
        assertNotNull(sourcePos);
        String sourceWithoutMarker = source.substring(0, sourcePos) + source.substring(sourcePos+1);
        Formatter formatter = getFormatter(preferences);

        JEditorPane ta = getPane(sourceWithoutMarker);
        Caret caret = ta.getCaret();
        caret.setDot(sourcePos);
        BaseDocument doc = (BaseDocument) ta.getDocument();
        if (formatter != null) {
            configureIndenters(doc, formatter, true);
        }

        setupDocumentIndentation(doc, preferences);

        Preferences prefs = CodeStylePreferences.get(doc).getPreferences();
        prefs.putInt(FmtOptions.INITIAL_INDENT, initialIndent);

        runKitAction(ta, DefaultEditorKit.insertBreakAction, "\n");

        // wait for generating comment
        Future<?> future = PhpCommentGenerator.RP.submit(new Runnable() {
            @Override
            public void run() {
            }
        });
        future.get();

        doc.insertString(caret.getDot(), "^", null);
        String target = doc.getText(0, doc.getLength());
        assertDescriptionMatches(file, target, false, ".indented");
    }

    public void testAlternativeSyntaxFor_01()throws Exception {
        testIndentInFile("testfiles/bracketCompleter/alternativeSyntaxFor_01.php");
    }

    public void testAlternativeSyntaxFor_02()throws Exception {
        testIndentInFile("testfiles/bracketCompleter/alternativeSyntaxFor_02.php");
    }

    public void testAlternativeSyntaxFor_03()throws Exception {
        testIndentInFile("testfiles/bracketCompleter/alternativeSyntaxFor_03.php");
    }

    public void testAlternativeSyntaxFor_04()throws Exception {
        testIndentInFile("testfiles/bracketCompleter/alternativeSyntaxFor_04.php");
    }

    public void testAlternativeSyntaxForEach_01()throws Exception {
        testIndentInFile("testfiles/bracketCompleter/alternativeSyntaxForEach_01.php");
    }

    public void testAlternativeSyntaxForEach_02()throws Exception {
        testIndentInFile("testfiles/bracketCompleter/alternativeSyntaxForEach_02.php");
    }

    public void testAlternativeSyntaxWhile_01()throws Exception {
        testIndentInFile("testfiles/bracketCompleter/alternativeSyntaxWhile_01.php");
    }

    public void testAlternativeSyntaxWhile_02()throws Exception {
        testIndentInFile("testfiles/bracketCompleter/alternativeSyntaxWhile_02.php");
    }

    public void testAlternativeSyntaxWhile_03()throws Exception {
        testIndentInFile("testfiles/bracketCompleter/alternativeSyntaxWhile_03.php");
    }

    public void testAlternativeSyntaxSwitch_01()throws Exception {
        testIndentInFile("testfiles/bracketCompleter/alternativeSyntaxSwitch_01.php");
    }

    public void testAlternativeSyntaxSwitch_02()throws Exception {
        testIndentInFile("testfiles/bracketCompleter/alternativeSyntaxSwitch_02.php");
    }

    public void testAlternativeSyntaxSwitch_03()throws Exception {
        testIndentInFile("testfiles/bracketCompleter/alternativeSyntaxSwitch_03.php");
    }

    public void testAlternativeSyntaxIf_01()throws Exception {
        testIndentInFile("testfiles/bracketCompleter/alternativeSyntaxIf_01.php");
    }

    public void testAlternativeSyntaxIf_02()throws Exception {
        testIndentInFile("testfiles/bracketCompleter/alternativeSyntaxIf_02.php");
    }

    public void testAlternativeSyntaxIf_03()throws Exception {
        testIndentInFile("testfiles/bracketCompleter/alternativeSyntaxIf_03.php");
    }

    public void testAlternativeSyntaxIf_04()throws Exception {
        testIndentInFile("testfiles/bracketCompleter/alternativeSyntaxIf_04.php");
    }

    public void testAlternativeSyntaxIf_05()throws Exception {
        testIndentInFile("testfiles/bracketCompleter/alternativeSyntaxIf_05.php");
    }

    public void testAlternativeSyntaxIf_06()throws Exception {
        testIndentInFile("testfiles/bracketCompleter/alternativeSyntaxIf_06.php");
    }

    public void testIssue167816_01()throws Exception {
        testIndentInFile("testfiles/bracketCompleter/issue167816_01.php");
    }

    public void testIssue167816_02()throws Exception {
        testIndentInFile("testfiles/bracketCompleter/issue167816_02.php");
    }

    public void testIssue166424_01()throws Exception {
        testIndentInFile("testfiles/bracketCompleter/issue166424_01.php");
    }

    public void testIssue166424_02()throws Exception {
        testIndentInFile("testfiles/bracketCompleter/issue166424_02.php");
    }

    public void testIssue166424_03()throws Exception {
        testIndentInFile("testfiles/bracketCompleter/issue166424_03.php");
    }

    public void testIssue191892_01()throws Exception {
        testIndentInFile("testfiles/bracketCompleter/issue191892_01.php");
    }

    public void testIssue191856_01()throws Exception {
        testIndentInFile("testfiles/bracketCompleter/issue191856_01.php");
    }

    public void testIssue195771_01()throws Exception {
        testIndentInFile("testfiles/bracketCompleter/issue195771_01.php");
    }

    public void testIssue195771_02()throws Exception {
        testIndentInFile("testfiles/bracketCompleter/issue195771_02.php");
    }

    public void testIssue195771_03()throws Exception {
        testIndentInFile("testfiles/bracketCompleter/issue195771_03.php");
    }

    public void testIssue195771_04()throws Exception {
        testIndentInFile("testfiles/bracketCompleter/issue195771_04.php");
    }

    public void testIssue195771_05()throws Exception {
        testIndentInFile("testfiles/bracketCompleter/issue195771_05.php");
    }

    public void testIssue195771_06()throws Exception {
        testIndentInFile("testfiles/bracketCompleter/issue195771_06.php");
    }

    public void testIssue195771_07()throws Exception {
        testIndentInFile("testfiles/bracketCompleter/issue195771_07.php");
    }

    public void testIssue197924_01()throws Exception {
        testIndentInFile("testfiles/bracketCompleter/issue197924_01.php");
    }

    public void testIssue197924_02()throws Exception {
        testIndentInFile("testfiles/bracketCompleter/issue197924_02.php");
    }

    public void testIssue196596_01()throws Exception {
        testIndentInFile("testfiles/bracketCompleter/issue196596_01.php");
    }

    public void testIssue196596_02()throws Exception {
        testIndentInFile("testfiles/bracketCompleter/issue196596_02.php");
    }

    public void testIssue196596_03()throws Exception {
        testIndentInFile("testfiles/bracketCompleter/issue196596_03.php");
    }

    public void testIssue196596_04()throws Exception {
        testIndentInFile("testfiles/bracketCompleter/issue196596_04.php");
    }

    public void testIssue202362_01()throws Exception {
        testIndentInFile("testfiles/bracketCompleter/issue202362_01.php");
    }

    public void testIssue202362_02()throws Exception {
        testIndentInFile("testfiles/bracketCompleter/issue202362_02.php");
    }

    public void testIssue202362_03()throws Exception {
        testIndentInFile("testfiles/bracketCompleter/issue202362_03.php");
    }

    public void testIssue202362_04()throws Exception {
        testIndentInFile("testfiles/bracketCompleter/issue202362_04.php");
    }

    public void testIssue203513()throws Exception {
        testIndentInFile("testfiles/bracketCompleter/issue203513.php");
    }

    public void testIssue193118_01() throws Exception {
        testIndentInFile("testfiles/bracketCompleter/issue193118_01.php");
    }

    public void testIssue193118_02() throws Exception {
        testIndentInFile("testfiles/bracketCompleter/issue193118_02.php");
    }

    public void testIssue211394_01() throws Exception {
        testIndentInFile("testfiles/bracketCompleter/issue211394_01.php");
    }

    public void testIssue211394_02() throws Exception {
        testIndentInFile("testfiles/bracketCompleter/issue211394_02.php");
    }

    public void testIssue211394_03() throws Exception {
        testIndentInFile("testfiles/bracketCompleter/issue211394_03.php");
    }

    public void testIssue202770_01() throws Exception {
        testIndentInFile("testfiles/bracketCompleter/issue202770_01.php");
    }

    public void testIssue202770_02() throws Exception {
        testIndentInFile("testfiles/bracketCompleter/issue202770_02.php");
    }

    public void testIssue202770_03() throws Exception {
        testIndentInFile("testfiles/bracketCompleter/issue202770_03.php");
    }

    public void testIssue202770_04() throws Exception {
        testIndentInFile("testfiles/bracketCompleter/issue202770_04.php");
    }
}
