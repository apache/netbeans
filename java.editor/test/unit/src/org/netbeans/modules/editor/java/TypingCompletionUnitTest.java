/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2011 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2011 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.editor.java;

import java.awt.EventQueue;
import java.awt.event.KeyEvent;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.PlainDocument;
import junit.framework.TestCase;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.lexer.Language;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.openide.awt.AcceleratorBinding;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.MIMEResolver;
import org.openide.util.lookup.ServiceProvider;


/**
 * Test java brackets completion - unlike the original test this one
 * emulates real typing and tests the resulting state of the document.
 *
 * @autor Miloslav Metelka
 */
public class TypingCompletionUnitTest extends NbTestCase {

    public TypingCompletionUnitTest(String testMethodName) {
        super(testMethodName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Preferences prefs = MimeLookup.getLookup(JavaKit.JAVA_MIME_TYPE).lookup(Preferences.class);
        prefs.putBoolean(SimpleValueNames.COMPLETION_PAIR_CHARACTERS, true);
        Class.forName(AcceleratorBinding.class.getName(), true, AcceleratorBinding.class.getClassLoader());
    }

    // ------- Tests for completion of right parenthesis ')' -------------

    public void testTypeSemicolonInForLoop() { // #146139
        Context ctx = new Context(new JavaKit(),
                "for (int i = 0|)"
        );
        ctx.typeChar(';');
        ctx.assertDocumentTextEquals(
                "for (int i = 0;|)"
        );
    }

    public void testTypeSecondSemicolonInForLoop() { // #146139
        Context ctx = new Context(new JavaKit(),
                "for (int i = 0; i <= 0|)"
        );
        ctx.typeChar(';');
        ctx.assertDocumentTextEquals(
                "for (int i = 0; i <= 0;|)"
        );
    }

    public void testTypeSemicolonInArgs() { // #146139
        Context ctx = new Context(new JavaKit(),
                "m(|)"
        );
        ctx.typeChar(';');
        ctx.assertDocumentTextEquals(
                "m();|"
        );
    }
    
    public void testSemicolonOnTheEnd() { // #146139
        Context ctx = new Context(new JavaKit(),
                "m()| "
        );
        ctx.typeChar(';');
        ctx.assertDocumentTextEquals(
                "m();| "
        );
    }

    public void testTypeRightParenWithinBraces() { // #146139
        Context ctx = new Context(new JavaKit(),
                "{(()|; }"
        );
        ctx.typeChar(')');
        ctx.assertDocumentTextEquals(
                "{(()); }"
        );
    }
    
    public void testTypeLeftParen() {
        Context ctx = new Context(new JavaKit(), "m|");
        ctx.typeChar('(');
        ctx.assertDocumentTextEquals("m(|)");
    }

    public void testTypeSecondRightParen() {
        Context ctx = new Context(new JavaKit(),
                "m()|)"
        );
        ctx.typeChar(')');
        ctx.assertDocumentTextEquals(
                "m())|"
        );
    }

    public void testTypeRightParenSwingInvokeLaterRunnable() {
        Context ctx = new Context(new JavaKit(),
                "SwingUtilities.invokeLater(new Runnable()|))"
        );
        ctx.typeChar(')');
        ctx.assertDocumentTextEquals(
                "SwingUtilities.invokeLater(new Runnable())|)"
        );
    }

    public void testTypeSimpleAdditionOfOpeningParenthesisAfterWhile () throws Exception {
        Context ctx = new Context(new JavaKit(),
                "while |"
        );
        ctx.typeChar('(');
        ctx.assertDocumentTextEquals(
                "while (|)"
        );
    }

    public void testTypeRightParenSwingInvokeLaterRunnableRun() {
        Context ctx = new Context(new JavaKit(),
                "SwingUtilities.invokeLater(new Runnable() {\n" +
                "    public void run()|)\n" +
                "})"
        );
        ctx.typeChar(')');
        ctx.assertDocumentTextEquals(
                "SwingUtilities.invokeLater(new Runnable() {\n" +
                "    public void run())|\n" +
                "})"
        );
    }

    public void testTypeRightParenIfMethodCall() {
        Context ctx = new Context(new JavaKit(),
                "if (a()|) + 5 > 6) {\n" +
                "}"
        );
        ctx.typeChar(')');
        ctx.assertDocumentTextEquals(
                "if (a())| + 5 > 6) {\n" +
                "}"
        );
    }

    public void testTypeRightParenNoSkipNonBracketChar() {
        Context ctx = new Context(new JavaKit(),
                "m()|"
        );
        ctx.typeChar(' ');
        ctx.assertDocumentTextEquals(
                "m() |"
        );
    }



    // ------- Tests for completion of right brace '}' -------------

    public void testTypeAddRightBraceIfLeftBrace() {
        Context ctx = new Context(new JavaKit(),
                "class Test {\n" +
                "    {\n" +
                "        if (true) {|\n" +
                "    }\n" +
                "}\n"
        );
        ctx.typeChar('\n');
        ctx.assertDocumentTextEquals(
                "class Test {\n" +
                "    {\n" +
                "        if (true) {\n" +
                "            |\n" +
                "        }\n" +
                "    }\n" +
                "}\n"
        );
    }

    public void testTypeAddRightBraceIfLeftBraceWhiteSpace() {
        Context ctx = new Context(new JavaKit(),
                "class Test {\n" +
                "    {\n" +
                "        if (true) { \t|\n" +
                "    }\n" +
                "}\n"
        );
        ctx.typeChar('\n');
        ctx.assertDocumentTextEquals(
                "class Test {\n" +
                "    {\n" +
                "        if (true) { \t\n" +
                "            |\n" +
                "        }\n" +
                "    }\n" +
                "}\n"
        );
    }

    public void testTypeAddRightBraceIfLeftBraceLineComment() {
        Context ctx = new Context(new JavaKit(),
                "class Test {\n" +
                "    {\n" +
                "        if (true) { // line-comment|\n" +
                "    }\n" +
                "}\n"
        );
        ctx.typeChar('\n');
        ctx.assertDocumentTextEquals(
                "class Test {\n" +
                "    {\n" +
                "        if (true) { // line-comment\n" +
                "            |\n" +
                "        }\n" +
                "    }\n" +
                "}\n"
        );
    }

    public void testTypeAddRightBraceIfLeftBraceBlockComment() {
        Context ctx = new Context(new JavaKit(),
                "class Test {\n" +
                "    {\n" +
                "        if (true) { /* block-comment */|\n" +
                "    }\n" +
                "}\n"
        );
        ctx.typeChar('\n');
        ctx.assertDocumentTextEquals(
                "class Test {\n" +
                "    {\n" +
                "        if (true) { /* block-comment */\n" +
                "            |\n" +
                "        }\n" +
                "    }\n" +
                "}\n"
        );
    }

    public void testTypeAddRightBraceIfLeftBraceAlreadyPresent() {
        Context ctx = new Context(new JavaKit(),
                "class Test {\n" +
                "    {\n" +
                "        if (true) {|\n" +
                "        }\n" +
                "    }\n" +
                "}\n"
        );
        ctx.typeChar('\n');
        ctx.assertDocumentTextEquals(
                "class Test {\n" +
                "    {\n" +
                "        if (true) {\n" +
                "            |\n" +
                "        }\n" +
                "    }\n" +
                "}\n"
        );
    }

    public void XtestTypeAddRightBraceCaretInComment() {
        Context ctx = new Context(new JavaKit(),
                "class Test {\n" +
                "    {\n" +
                "        if (true) { /* unclosed-block-comment|\n" +
                "          */\n" +
                "    }\n" +
                "}\n"
        );
        ctx.typeChar('\n');
        ctx.assertDocumentTextEquals(
                "class Test {\n" +
                "    {\n" +
                "        if (true) { /* unclosed-block-comment\n" +
                "                     * |\n" +
                "          */\n" +
                "    }\n" +
                "}\n"
        );
    }

    public void testTypeAddRightBraceMultiLine() {
        Context ctx = new Context(new JavaKit(),
                "class Test {\n" +
                "    {\n" +
                "        if (true) {| System.out.println(\n" +
                "        \"\");\n" +
                "    }\n" +
                "}\n"
                );

        ctx.typeChar('\n');
        ctx.assertDocumentTextEquals(
                "class Test {\n" +
                "    {\n" +
                "        if (true) {\n" +
                "            System.out.println(\n" +
                "        \"\");\n" +
                "    }\n" +
                "}\n"
                );
    }

    public void testTypeAddRightBraceSingleLine() {
        Context ctx = new Context(new JavaKit(),
                "class Test {\n" +
                "    {\n" +
                "        if (true) {| System.out.println(\"\");\n" +
                "    }\n" +
                "}\n"
                );

        ctx.typeChar('\n');
        ctx.assertDocumentTextEquals(
                "class Test {\n" +
                "    {\n" +
                "        if (true) {\n" +
                "            System.out.println(\"\");\n" +
                "        }\n" +
                "    }\n" +
                "}\n"
                );
    }


    // ------- Tests for completion of quote (") -------------
    public void testTypeSimpleQuoteInEmptyDoc () throws Exception {
        Context ctx = new Context(new JavaKit(),
                "|"
        );
        ctx.typeChar('"');
        ctx.assertDocumentTextEquals(
                "\"|\""
        );
    }

    public void testTypeSimpleQuoteAtBeginingOfDoc () throws Exception {
        Context ctx = new Context(new JavaKit(),
                "|  "
        );
        ctx.typeChar('"');
        ctx.assertDocumentTextEquals(
                "\"|\"  "
        );
    }

    public void testTypeSimpleQuoteAtEndOfDoc () throws Exception {
        Context ctx = new Context(new JavaKit(),
                "  |"
        );
        ctx.typeChar('"');
        ctx.assertDocumentTextEquals(
                "  \"|\""
        );
    }

    public void testTypeSimpleQuoteInWhiteSpaceArea () throws Exception {
        Context ctx = new Context(new JavaKit(),
                "  |  "
        );
        ctx.typeChar('"');
        ctx.assertDocumentTextEquals(
                "  \"|\"  "
        );
    }

    public void testTypeQuoteAtEOL () throws Exception {
        Context ctx = new Context(new JavaKit(),
                "  |\n"
        );
        ctx.typeChar('"');
        ctx.assertDocumentTextEquals(
                "  \"|\"\n"
        );
    }

    public void testTypeQuoteWithUnterminatedStringLiteral () throws Exception {
        Context ctx = new Context(new JavaKit(),
                "  \"unterminated string| \n"
        );
        ctx.typeChar('"');
        ctx.assertDocumentTextEquals(
                "  \"unterminated string\"| \n"
        );
    }

    public void testTypeQuoteAtEOLWithUnterminatedStringLiteral () throws Exception {
        Context ctx = new Context(new JavaKit(),
                "  \"unterminated string | \n"
        );
        ctx.typeChar('"');
        ctx.assertDocumentTextEquals(
                "  \"unterminated string \"| \n"
        );
    }

    public void testTypeQuoteInsideStringLiteral () throws Exception {
        Context ctx = new Context(new JavaKit(),
                "  \"stri|ng literal\" "
        );
        ctx.typeChar('"');
        ctx.assertDocumentTextEquals(
                "  \"stri\"|ng literal\" "
        );
    }

    public void testTypeQuoteInsideEmptyParentheses () throws Exception {
        Context ctx = new Context(new JavaKit(),
                " System.out.println(|) "
        );
        ctx.typeChar('"');
        ctx.assertDocumentTextEquals(
                " System.out.println(\"|\") "
        );
    }

    public void testTypeQuoteInsideNonEmptyParentheses () throws Exception {
        Context ctx = new Context(new JavaKit(),
                " System.out.println(|some text) "
        );
        ctx.typeChar('"');
        ctx.assertDocumentTextEquals(
                " System.out.println(\"|some text) "
        );
    }

    public void testTypeQuoteInsideNonEmptyParenthesesBeforeClosingParentheses () throws Exception {
        Context ctx = new Context(new JavaKit(),
                " System.out.println(i+|) "
        );
        ctx.typeChar('"');
        ctx.assertDocumentTextEquals(
                " System.out.println(i+\"|\") "
        );
    }

    public void testTypeQuoteInsideNonEmptyParenthesesBeforeClosingParenthesesAndUnterminatedStringLiteral () throws Exception {
        Context ctx = new Context(new JavaKit(),
                " System.out.println(\"unterminated string literal |); "
        );
        ctx.typeChar('"');
        ctx.assertDocumentTextEquals(
                " System.out.println(\"unterminated string literal \"|); "
        );
    }

    public void testTypeQuoteBeforePlus () throws Exception {
        Context ctx = new Context(new JavaKit(),
                " System.out.println(|+\"string literal\"); "
        );
        ctx.typeChar('"');
        ctx.assertDocumentTextEquals(
                " System.out.println(\"|\"+\"string literal\"); "
        );
    }

    public void testTypeQuoteBeforeComma () throws Exception {
        Context ctx = new Context(new JavaKit(),
                "String s[] = new String[]{|,\"two\"};"
        );
        ctx.typeChar('"');
        ctx.assertDocumentTextEquals(
                "String s[] = new String[]{\"|\",\"two\"};"
        );
    }

    public void testTypeQuoteBeforeBrace () throws Exception {
        Context ctx = new Context(new JavaKit(),
                "String s[] = new String[]{\"one\",|};"
        );
        ctx.typeChar('"');
        ctx.assertDocumentTextEquals(
                "String s[] = new String[]{\"one\",\"|\"};"
        );
    }

    public void testTypeQuoteBeforeSemicolon() throws Exception {
        Context ctx = new Context(new JavaKit(),
                "String s = \"\" + |;"
        );
        ctx.typeChar('"');
        ctx.assertDocumentTextEquals(
                "String s = \"\" + \"|\";"
        );
    }

    public void testTypeQuoteBeforeSemicolonWithWhitespace() throws Exception {
        Context ctx = new Context(new JavaKit(),
                "String s = \"\" +| ;"
        );
        ctx.typeChar('"');
        ctx.assertDocumentTextEquals(
                "String s = \"\" +\"|\" ;"
        );
    }

    public void testTypeQuoteAfterEscapeSequence() throws Exception {
        Context ctx = new Context(new JavaKit(),
                "\\|"
        );
        ctx.typeChar('"');
        ctx.assertDocumentTextEquals(
                "\\\"|"
        );
    }

    public void testTypeQuoteEaten() throws Exception {
        Context ctx = new Context(new JavaKit(),
                "|"
        );
        ctx.typeChar('"');
        ctx.typeChar('"');
        ctx.assertDocumentTextEquals(
                "\"\"|"
        );
    }

    public void testTypeQuoteOnFirstQuote () throws Exception {
        Context ctx = new Context(new JavaKit(),
                " |\"asdf\""
        );
        ctx.typeChar('"');
        ctx.assertDocumentTextEquals(
                " \"|\"asdf\""
        );
    }

    public void testTypeQuoteInsideComments() throws Exception {
        Context ctx = new Context(new JavaKit(),
                "/** |\n */"
        );
        ctx.typeChar('"');
        ctx.assertDocumentTextEquals(
                "/** \"|\n */"
        );
    }

    public void testTypeQuoteAtTheEndOfLineCommentLine() throws Exception {
        Context ctx = new Context(new JavaKit(),
                "// test line comment |\n"
        );
        ctx.typeChar('"');
        ctx.assertDocumentTextEquals(
                "// test line comment \"|\n"
        );
    }


    // ------- Tests for completion of single quote (') -------------

    public void testTypeSingleQuoteInEmptyDoc () throws Exception {
        Context ctx = new Context(new JavaKit(),
                "|"
        );
        ctx.typeChar('\'');
        ctx.assertDocumentTextEquals(
                "'|'"
        );
    }

    public void testTypeSingleQuoteAtBeginingOfDoc () throws Exception {
        Context ctx = new Context(new JavaKit(),
                "|  "
        );
        ctx.typeChar('\'');
        ctx.assertDocumentTextEquals(
                "'|'  "
        );
    }

    public void testTypeSingleQuoteAtEndOfDoc () throws Exception {
        Context ctx = new Context(new JavaKit(),
                "  |"
        );
        ctx.typeChar('\'');
        ctx.assertDocumentTextEquals(
                "  '|'"
        );
    }

    public void testTypeSingleQuoteInWhiteSpaceArea () throws Exception {
        Context ctx = new Context(new JavaKit(),
                "  |  "
        );
        ctx.typeChar('\'');
        ctx.assertDocumentTextEquals(
                "  '|'  "
        );
    }

    public void testTypeSingleQuoteAtEOL () throws Exception {
        Context ctx = new Context(new JavaKit(),
                "  |\n"
        );
        ctx.typeChar('\'');
        ctx.assertDocumentTextEquals(
                "  '|'\n"
        );
    }

    public void testTypeSingleQuoteWithUnterminatedCharLiteral () throws Exception {
        Context ctx = new Context(new JavaKit(),
                "  '| \n"
        );
        ctx.typeChar('\'');
        ctx.assertDocumentTextEquals(
                "  ''| \n"
        );
    }

    public void testTypeSingleQuoteAtEOLWithUnterminatedCharLiteral () throws Exception {
        Context ctx = new Context(new JavaKit(),
                "  ' |\n"
        );
        ctx.typeChar('\'');
        ctx.assertDocumentTextEquals(
                "  ' '|\n"
        );
    }

    public void testTypeSingleQuoteInsideCharLiteral () throws Exception {
        Context ctx = new Context(new JavaKit(),
                "  '| ' "
        );
        ctx.typeChar('\'');
        ctx.assertDocumentTextEquals(
                "  ''| ' "
        );
    }

    public void testTypeSingleQuoteInsideEmptyParentheses () throws Exception {
        Context ctx = new Context(new JavaKit(),
                " System.out.println(|) "
        );
        ctx.typeChar('\'');
        ctx.assertDocumentTextEquals(
                " System.out.println('|') "
        );
    }

    public void testTypeSingleQuoteInsideNonEmptyParentheses () throws Exception {
        Context ctx = new Context(new JavaKit(),
                " System.out.println(|some text) "
        );
        ctx.typeChar('\'');
        ctx.assertDocumentTextEquals(
                " System.out.println('|some text) "
        );
    }

    public void testTypeSingleQuoteInsideNonEmptyParenthesesBeforeClosingParentheses () throws Exception {
        Context ctx = new Context(new JavaKit(),
                " System.out.println(i+|) "
        );
        ctx.typeChar('\'');
        ctx.assertDocumentTextEquals(
                " System.out.println(i+'|') "
        );
    }

    public void testTypeSingleQuoteInsideNonEmptyParenthesesBeforeClosingParenthesesAndUnterminatedCharLiteral () throws Exception {
        Context ctx = new Context(new JavaKit(),
                " System.out.println(' |); "
        );
        ctx.typeChar('\'');
        ctx.assertDocumentTextEquals(
                " System.out.println(' '|); "
        );
    }

    public void testTypeSingleQuoteBeforePlus () throws Exception {
        Context ctx = new Context(new JavaKit(),
                " System.out.println(|+\"string literal\"); "
        );
        ctx.typeChar('\'');
        ctx.assertDocumentTextEquals(
                " System.out.println('|'+\"string literal\"); "
        );
    }

    public void testTypeSingleQuoteBeforeComma () throws Exception {
        Context ctx = new Context(new JavaKit(),
                "String s[] = new String[]{|,\"two\"};"
        );
        ctx.typeChar('\'');
        ctx.assertDocumentTextEquals(
                "String s[] = new String[]{'|',\"two\"};"
        );
    }

    public void testTypeSingleQuoteBeforeBrace () throws Exception {
        Context ctx = new Context(new JavaKit(),
                "String s[] = new String[]{\"one\",|};"
        );
        ctx.typeChar('\'');
        ctx.assertDocumentTextEquals(
                "String s[] = new String[]{\"one\",'|'};"
        );
    }

    public void testTypeSingleQuoteBeforeSemicolon() throws Exception {
        Context ctx = new Context(new JavaKit(),
                "String s = \"\" + |;"
        );
        ctx.typeChar('\'');
        ctx.assertDocumentTextEquals(
                "String s = \"\" + '|';"
        );
    }

    public void testTypeSingleQuoteBeforeSemicolonWithWhitespace() throws Exception {
        Context ctx = new Context(new JavaKit(),
                "String s = \"\" +| ;"
        );
        ctx.typeChar('\'');
        ctx.assertDocumentTextEquals(
                "String s = \"\" +'|' ;"
        );
    }

    public void testTypeSingleQuoteAfterEscapeSequence() throws Exception {
        Context ctx = new Context(new JavaKit(),
                "\\|"
        );
        ctx.typeChar('\'');
        ctx.assertDocumentTextEquals(
                "\\'|"
        );
    }

    public void testTypeSingleQuoteEaten() throws Exception {
        Context ctx = new Context(new JavaKit(),
                "|"
        );
        ctx.typeChar('\'');
        ctx.typeChar('\'');
        ctx.assertDocumentTextEquals(
                "''|"
        );
    }

    public void testTypeSingleQuoteInsideComments() throws Exception {
        Context ctx = new Context(new JavaKit(),
                "/* |\n */"
        );
        ctx.typeChar('\'');
        ctx.assertDocumentTextEquals(
                "/* \'|\n */"
        );
    }

    public void testTypeSingleQuoteAtTheEndOfLineCommentLine() throws Exception {
        Context ctx = new Context(new JavaKit(),
                "// test line comment |\n"
        );
        ctx.typeChar('\'');
        ctx.assertDocumentTextEquals(
                "// test line comment \'|\n"
        );
    }

    public void testDisable147641() throws Exception {
        boolean orig = TypingCompletion.isCompletionSettingEnabled();
        Preferences prefs = MimeLookup.getLookup(JavaKit.JAVA_MIME_TYPE).lookup(Preferences.class);

        try {
            prefs.putBoolean(SimpleValueNames.COMPLETION_PAIR_CHARACTERS, false);

            Context ctx = new Context(new JavaKit(),
                    "while |"
            );
            ctx.typeChar('(');
            ctx.assertDocumentTextEquals(
                    "while (|"
            );
        } finally {
            prefs.putBoolean(SimpleValueNames.COMPLETION_PAIR_CHARACTERS, orig);
        }
    }

    public void testDoNotSkipWhenNotBalanced147683a() throws Exception {
        Context ctx = new Context(new JavaKit(),
                "System.err.println((true|);"
        );
        ctx.typeChar(')');
        ctx.assertDocumentTextEquals(
                "System.err.println((true)|);"
        );
    }

    public void testSkipWhenBalanced46517() throws Exception {
        Context ctx = new Context(new JavaKit(),
                "if (a(|) )"
        );
        ctx.typeChar(')');
        ctx.assertDocumentTextEquals(
                "if (a()| )"
        );
    }

    public void testDoNotSkipWhenNotBalanced147683b() throws Exception {
        Context ctx = new Context(new JavaKit(),
                "if (a(|) ; )"
        );
        ctx.typeChar(')');
        ctx.assertDocumentTextEquals(
                "if (a()|) ; )"
        );
    }

    public void testDoNotSkipWhenNotBalanced147683c() throws Exception {
        Context ctx = new Context(new JavaKit(),
                "if (a(|) \n )"
        );
        ctx.typeChar(')');
        ctx.assertDocumentTextEquals(
                "if (a()|) \n )"
        );
    }
//problem
    public void testSkipWhenBalanced198194a() throws Exception {
        Context ctx = new Context(new JavaKit(),
                "for (int i = a(|); i < 10; i++)"
        );
        ctx.typeChar(')');
        ctx.assertDocumentTextEquals(
                "for (int i = a()|; i < 10; i++)"
        );
    }

    public void testSkipWhenNotBalanced198194a() throws Exception {
        Context ctx = new Context(new JavaKit(),
                "for (int i = a(|; i < 10; i++)"
        );
        ctx.typeChar(')');
        ctx.assertDocumentTextEquals(
                "for (int i = a()|; i < 10; i++)"
        );
    }

    public void testSkipWhenBalanced198194b() throws Exception {
        Context ctx = new Context(new JavaKit(),
                "for (int i = a(); i < 10; i = a(|))"
        );
        ctx.typeChar(')');
        ctx.assertDocumentTextEquals(
                "for (int i = a(); i < 10; i = a()|)"
        );
    }

    public void testSkipWhenNotBalanced198194b() throws Exception {
        Context ctx = new Context(new JavaKit(),
                "for (int i = a(); i < 10; i = a(|)"
        );
        ctx.typeChar(')');
        ctx.assertDocumentTextEquals(
                "for (int i = a(); i < 10; i = a()|)"
        );
    }

    public void testSkipWhenNotBalanced198194c() throws Exception {
        Context ctx = new Context(new JavaKit(),
                "for (int i = a(); i < 10; i++) a(|;"
        );
        ctx.typeChar(')');
        ctx.assertDocumentTextEquals(
                "for (int i = a(); i < 10; i++) a()|;"
        );
    }

    public void testSkipWhenBalanced198194c() throws Exception {
        Context ctx = new Context(new JavaKit(),
                "for (int i = a(); i < 10; i++) a(|);"
        );
        ctx.typeChar(')');
        ctx.assertDocumentTextEquals(
                "for (int i = a(); i < 10; i++) a()|;"
        );
    }

    public void testKeepBalance148878() throws Exception {
        Context ctx = new Context(new JavaKit(),
                "Map[|] m = new HashMap[1];"
        );
        ctx.typeChar(']');
        ctx.assertDocumentTextEquals(
                "Map[]| m = new HashMap[1];"
        );
    }
    
    public void testQuotes148878() throws Exception {
        Context ctx = new Context(new JavaKit(),
                "if (c == '\\\\|')"
        );
        ctx.typeChar('\'');
        ctx.assertDocumentTextEquals(
                "if (c == '\\\\'|)"
        );
    }
   
    public void testPositionInString() throws Exception {
        Context ctx = new Context(new JavaKit(),
                "class Test {\n"
                + "    {\n"
                + "        \"H|\"\n"
                + "    }\n"
                + "}\n");
        ctx.typeChar('\n');
        ctx.assertDocumentTextEquals(
                "class Test {\n"
                + "    {\n"
                + "        \"H\"\n"
                + "                + \"|\"\n"
                + "    }\n"
                + "}\n");
    }
    
    public void testPositionInEmptyString() throws Exception {
        Context ctx = new Context(new JavaKit(),
                "class Test {\n"
                + "    {\n"
                + "        \"|\"\n"
                + "    }\n"
                + "}\n");
        ctx.typeChar('\n');
        ctx.assertDocumentTextEquals(
                "class Test {\n"
                + "    {\n"
                + "        \"\"\n"
                + "                + \"|\"\n"
                + "    }\n"
                + "}\n");
    }
    
    public void testCommentBlockCompletion() throws Exception {
        Preferences prefs = MimeLookup.getLookup(JavaKit.JAVA_MIME_TYPE).lookup(Preferences.class);
        try {
            prefs.putBoolean("enableBlockCommentFormatting", true);
            Context ctx = new Context(new JavaKit(),
                    "class Test {\n"
                    + "    {\n"
                    + "        /*|\n"
                    + "    }\n"
                    + "}\n");
            ctx.typeChar('\n');
            ctx.assertDocumentTextEquals(
                    "class Test {\n"
                    + "    {\n"
                    + "        /*\n"
                    + "         * |\n"
                    + "         */\n"
                    + "    }\n"
                    + "}\n");
        } finally {
            prefs.remove("enableBlockCommentFormatting");
        }
    }
   
    public void testCommentBlockCompletionNotNeeded() throws Exception {
        Preferences prefs = MimeLookup.getLookup(JavaKit.JAVA_MIME_TYPE).lookup(Preferences.class);
        try {
            prefs.putBoolean("enableBlockCommentFormatting", true);
            Context ctx = new Context(new JavaKit(),
                    "class Test {\n"
                    + "    {\n"
                    + "        /*|\n"
                    + "         */\n"
                    + "    }\n"
                    + "}\n");
            ctx.typeChar('\n');
            ctx.assertDocumentTextEquals(
                    "class Test {\n"
                    + "    {\n"
                    + "        /*\n"
                    + "         * |\n"
                    + "         */\n"
                    + "    }\n"
                    + "}\n");
        } finally {
            prefs.remove("enableBlockCommentFormatting");
        }
    }
    

    public void testCommentBlockCompletionTwoComments () {
        Preferences prefs = MimeLookup.getLookup(JavaKit.JAVA_MIME_TYPE).lookup(Preferences.class);
        try {
            prefs.putBoolean("enableBlockCommentFormatting", true);
            Context ctx = new Context(new JavaKit(),
                    "/*|\n" +
                    "/*\n" +
                    " */"
            );
            ctx.typeChar('\n');
            ctx.assertDocumentTextEquals(
                    "/*\n" +
                    " * |\n" +
                    " */\n" +
                    "/*\n" +
                    " */"
            );
        } finally {
            prefs.remove("enableBlockCommentFormatting");
        }
    }

    public void testCommentBlockCompletionTwoComments2 () {
        Preferences prefs = MimeLookup.getLookup(JavaKit.JAVA_MIME_TYPE).lookup(Preferences.class);
        try {
            prefs.putBoolean("enableBlockCommentFormatting", true);
            Context ctx = new Context(new JavaKit(),
                    "/*|\n" +
                    "\n" +
                    "/*\n" +
                    " */"
            );
            ctx.typeChar('\n');
            ctx.assertDocumentTextEquals(
                    "/*\n" +
                    " * |\n" +
                    " */\n" +
                    "\n" +
                    "/*\n" +
                    " */"
            );
        } finally {
            prefs.remove("enableBlockCommentFormatting");
        }
    }
    
    public void testCommentBlockCompletionNoClose () {
        Preferences prefs = MimeLookup.getLookup(JavaKit.JAVA_MIME_TYPE).lookup(Preferences.class);
        try {
            prefs.putBoolean("enableBlockCommentFormatting", true);
            Context ctx = new Context(new JavaKit(),
                    "/*| a"
            );
            ctx.typeChar('\n');
            ctx.assertDocumentTextEquals(
                    "/*\n" +
                    " * |a"
            );
        } finally {
            prefs.remove("enableBlockCommentFormatting");
        }
    }
    
    public void testJavaDocCompletion() throws Exception {
        Context ctx = new Context(new JavaKit(),
                "class Test {\n"
                + "    {\n"
                + "        /**|\n"
                + "    }\n"
                + "}\n");
        ctx.typeChar('\n');
        ctx.assertDocumentTextEquals(
                "class Test {\n"
                + "    {\n"
                + "        /**\n"
                + "         * |\n"
                + "         */\n"
                + "    }\n"
                + "}\n");
    }
    
    public void testJavaDocCompletionNotNeeded() throws Exception {
        Context ctx = new Context(new JavaKit(),
                "class Test {\n"
                + "    {\n"
                + "        /**|\n"
                + "         */\n"
                + "    }\n"
                + "}\n");
        ctx.typeChar('\n');
        ctx.assertDocumentTextEquals(
                "class Test {\n"
                + "    {\n"
                + "        /**\n"
                + "         * |\n"
                + "         */\n"
                + "    }\n"
                + "}\n");
    }
  
    public void insertBreakJavadocComplete() throws Exception {
        Context ctx = new Context(new JavaKit(),
                "class Test {\n"
                + "    {\n"
                + "        /**|\n"
                + "        public static void main(String[] args) {\n"
                + "        }\n"
                + "    }\n"
                + "}\n");
        ctx.typeChar('\n');
        ctx.assertDocumentTextEquals (
            "class Test {\n"
                + "    {\n"
                + "        /**\n"
                + "         * |\n"
                + "         * @param args\n"
                + "         */\n"
                + "        public static void main(String[] args) {\n"
                + "        }\n"
                + "    }\n"
                + "}\n"
        );
    }
    
    public void testRemoveBracketBackSpace() throws Exception {
        Context ctx = new Context(new JavaKit(),
                "()(|)");
        ctx.typeChar('\b');
        ctx.assertDocumentTextEquals("()|");
    }

    public void testRemoveBracketDelete() throws Exception {
        Context ctx = new Context(new JavaKit(),
                "()|()");
        ctx.typeChar('\f');
        ctx.assertDocumentTextEquals("()|");
    }
    
    public void testRemoveQuotesBackSpace() throws Exception {
        Context ctx = new Context(new JavaKit(),
                "\"\"\"|\"");
        ctx.typeChar('\b');
        ctx.assertDocumentTextEquals("\"\"|");
    }
    
    public void testRemoveQuotesDelete() throws Exception {
        Context ctx = new Context(new JavaKit(),
                "\"\"|\"\"");
        ctx.typeChar('\f');
        ctx.assertDocumentTextEquals("\"\"|");
    }
    
    public void testRemoveQuotes2BackSpace() throws Exception {
        Context ctx = new Context(new JavaKit(),
                "\'\'\'|\'");
        ctx.typeChar('\b');
        ctx.assertDocumentTextEquals("\'\'|");
    }
    
    public void testRemoveQuotes2Delete() throws Exception {
        Context ctx = new Context(new JavaKit(),
                "\'\'|\'\'");
        ctx.typeChar('\f');
        ctx.assertDocumentTextEquals("\'\'|");
    }
    
    public void testJumpCharacters() throws Exception {
        Context ctx = new Context(new JavaKit(), "m(\"p|\");");
        ctx.typeChar('"');
        ctx.assertDocumentTextEquals("m(\"p\"|);");
        ctx.typeChar(')');
        ctx.assertDocumentTextEquals("m(\"p\")|;");
    }
    
    public void testJumpQuote() throws Exception {
        Context ctx = new Context(new JavaKit(), "\"|\"");
        ctx.typeChar('"');
        ctx.assertDocumentTextEquals("\"\"|");
    }
    
    public void testInsertSquareBracket() throws Exception {
        Context ctx = new Context(new JavaKit(), "|");
        ctx.typeChar('[');
        ctx.assertDocumentTextEquals("[|]");
    }

    public void testBackspaceSquareBracket() throws Exception {
        Context ctx = new Context(new JavaKit(), "[|]");
        ctx.typeChar('\b');
        ctx.assertDocumentTextEquals("|");
    }
    
    public void testDeleteSquareBracket() throws Exception {
        Context ctx = new Context(new JavaKit(), "|[]");
        ctx.typeChar('\f');
        ctx.assertDocumentTextEquals("|");
    }
    
    public void testInsertBracketInString() throws Exception {
        Context ctx = new Context(new JavaKit(), "\"|\"");
        ctx.typeChar('(');
        ctx.assertDocumentTextEquals("\"(|\"");
        ctx = new Context(new JavaKit(), "\" |\"");
        ctx.typeChar('(');
        ctx.assertDocumentTextEquals("\" (|\"");
    }
    
    public void testInsertBracketInChar() throws Exception {
        Context ctx = new Context(new JavaKit(), "\'|\'");
        ctx.typeChar('(');
        ctx.assertDocumentTextEquals("\'(|\'");
    }
    
    public void testInsertBracketInComment() throws Exception {
        Context ctx = new Context(new JavaKit(), "//|");
        ctx.typeChar('(');
        ctx.assertDocumentTextEquals("//(|");
    }
    
    public void testSkipBracketInComment() throws Exception {
        Context ctx = new Context(new JavaKit(), "//(|)");
        ctx.typeChar(')');
        ctx.assertDocumentTextEquals("//()|)");
    }
     
    public void testCorrectHandlingOfStringEscapes184059() throws Exception {
        assertTrue(isInsideString("foo\n\"bar|\""));
        assertTrue(isInsideString("foo\n\"bar\\\"|\""));
        assertFalse(isInsideString("foo\n\"bar\\\\\"|"));
        assertFalse(isInsideString("foo\n|\"bar\\\\\""));
        assertTrue(isInsideString("foo\n\"|bar\\\\\""));
    }

    private boolean isInsideString(String code) throws BadLocationException {
        int pos = code.indexOf('|');

        assertNotSame(-1, pos);

        code = code.replaceAll(Pattern.quote("|"), "");

        Document doc = new PlainDocument();

        doc.putProperty(Language.class, JavaTokenId.language());
        doc.insertString(0, code, null);

        return TypingCompletion.posWithinString(doc, pos);
    }

    private static final class Context {
        
        private JEditorPane pane;

        public Context(final EditorKit kit, final String textWithPipe) {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        pane = new JEditorPane();
                        pane.setEditorKit(kit);
                        Document doc = pane.getDocument();
                        // Required by Java's default key typed
                        doc.putProperty(Language.class, JavaTokenId.language());
                        doc.putProperty("mimeType", "text/x-java");
                        int caretOffset = textWithPipe.indexOf('|');
                        String text;
                        if (caretOffset != -1) {
                            text = textWithPipe.substring(0, caretOffset) + textWithPipe.substring(caretOffset + 1);
                        } else {
                            text = textWithPipe;
                        }
                        pane.setText(text);
                        pane.setCaretPosition((caretOffset != -1) ? caretOffset : doc.getLength());
                    }
                });
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }
        
        public JEditorPane pane() {
            return pane;
        }

        public Document document() {
            return pane.getDocument();
        }
        
        public void typeChar(final char ch) {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        KeyEvent keyEvent;
                        switch (ch) {
                            case '\n':
                                keyEvent = new KeyEvent(pane, KeyEvent.KEY_PRESSED,
                                        EventQueue.getMostRecentEventTime(),
                                        0, KeyEvent.VK_ENTER, KeyEvent.CHAR_UNDEFINED); // Simulate pressing of Enter
                                break;
                            case '\b':
                                keyEvent = new KeyEvent(pane, KeyEvent.KEY_PRESSED,
                                        EventQueue.getMostRecentEventTime(),
                                        0, KeyEvent.VK_BACK_SPACE, KeyEvent.CHAR_UNDEFINED); // Simulate pressing of BackSpace
                                break;
                            case '\f':
                                keyEvent = new KeyEvent(pane, KeyEvent.KEY_PRESSED,
                                        EventQueue.getMostRecentEventTime(),
                                        0, KeyEvent.VK_DELETE, KeyEvent.CHAR_UNDEFINED); // Simulate pressing of Delete
                                break;
                            default:
                                keyEvent = new KeyEvent(pane, KeyEvent.KEY_TYPED,
                                        EventQueue.getMostRecentEventTime(),
                                        0, KeyEvent.VK_UNDEFINED, ch);
                        }
                        SwingUtilities.processKeyBindings(keyEvent);
                    }
                });
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }

        public void typeText(String text) {
            for (int i = 0; i < text.length(); i++) {
                typeChar(text.charAt(i));
            }
        }

        public void assertDocumentTextEquals(final String textWithPipe) {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        int caretOffset = textWithPipe.indexOf('|');
                        String text;
                        if (caretOffset != -1) {
                            text = textWithPipe.substring(0, caretOffset) + textWithPipe.substring(caretOffset + 1);
                        } else {
                            text = textWithPipe;
                        }
                        try {
                            // Use debug text to prefix special chars for easier readability
                            text = CharSequenceUtilities.debugText(text);
                            String docText = document().getText(0, document().getLength());
                            docText = CharSequenceUtilities.debugText(docText);
                            if (!text.equals(docText)) {
                                int diffIndex = 0;
                                int minLen = Math.min(docText.length(), text.length());
                                while (diffIndex < minLen) {
                                    if (text.charAt(diffIndex) != docText.charAt(diffIndex)) {
                                        break;
                                    }
                                    diffIndex++;
                                }
                                TestCase.fail("Invalid document text - diff at index " + diffIndex +
                                        "\nExpected: \"" + text +
                                        "\"\n  Actual: \"" + docText + "\""
                                );
                            }
                        } catch (BadLocationException e) {
                            throw new IllegalStateException(e);
                        }
                        if (caretOffset != -1) {
                            TestCase.assertEquals("Invalid caret offset", caretOffset, pane.getCaretPosition());
                        }
                    }
                });
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }
    }

    @ServiceProvider(service=MIMEResolver.class)
    public static final class MIMEResolverImpl extends MIMEResolver {

        public MIMEResolverImpl() {
            super("text/x-nbeditor-keybindingsettings");
        }

        @Override public String findMIMEType(FileObject fo) {
            return fo.getPath().contains("Keybindings") ? "text/x-nbeditor-keybindingsettings" : null;
        }
    }

}
