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
package org.netbeans.modules.css.editor.indent;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.JEditorPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.api.actions.Closable;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.Language;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.Formatter;
import org.netbeans.modules.css.editor.test.TestBase;
import org.netbeans.modules.css.lib.api.CssTokenId;
import org.netbeans.modules.editor.NbEditorKit;
import org.netbeans.modules.editor.indent.spi.IndentTask;
import org.netbeans.modules.web.indent.api.support.AbstractIndenter;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;

public class CssIndenterTest extends TestBase {

    public CssIndenterTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        AbstractIndenter.inUnitTestRun = true;
//        CssBracketCompleter.unitTestingSupport = true;

        IndentTask.Factory cssFactory = new CssIndentTaskFactory();
        MockMimeLookup.setInstances(MimePath.parse("text/css"), cssFactory, CssTokenId.language());

        IndentTask.Factory htmlReformatFactory = (IndentTask.Factory) Class.forName("org.netbeans.modules.html.editor.indent.HtmlIndentTaskFactory").getDeclaredConstructor().newInstance();
        NbEditorKit htmlKit = (NbEditorKit) Class.forName("org.netbeans.modules.html.editor.api.HtmlKit").getDeclaredConstructor(String.class).newInstance("text/x-jsp");
        MockMimeLookup.setInstances(MimePath.parse("text/html"), htmlReformatFactory, htmlKit, HTMLTokenId.language());
    }

    public static Test xxsuite() throws IOException, BadLocationException {
        TestSuite suite = new TestSuite();
        suite.addTest(new CssIndenterTest("testNativeEmbeddingFormattingCase2"));
        return suite;
    }

    @Override
    protected boolean runInEQ() {
        return true;
    }

    @Override
    protected BaseDocument getDocument(FileObject fo, String mimeType, Language language) {
        // for some reason GsfTestBase is not using DataObjects for BaseDocument construction
        // which means that for example Java formatter which does call EditorCookie to retrieve
        // document will get difference instance of BaseDocument for indentation
        try {
            DataObject dobj = DataObject.find(fo);
            assertNotNull(dobj);

            EditorCookie ec = dobj.getLookup().lookup(EditorCookie.class);
            assertNotNull(ec);

            return (BaseDocument) ec.openDocument();
        } catch (Exception ex) {
            fail(ex.toString());
            return null;
        }
    }

    @Override
    protected void configureIndenters(Document document, Formatter formatter, boolean indentOnly, String mimeType) {
        // override it because I've already done in setUp()
    }

    public void testScssFormatting() throws Exception {
        format(".a {\n"
                + ".b:p {\n"
                + "p:v;\n"
                + "}\n"
                + "}",
                
                ".a {\n"
                + "    .b:p {\n"
                + "        p:v;\n"
                + "    }\n"
                + "}", null);

    }

    public void testFormatting() throws Exception {
        format("a{\nbackground: red,\nblue;\n  }\n",
                "a{\n    background: red,\n        blue;\n}\n", null);
        format("a{     background: green,\nyellow;\n      }\n",
                "a{\n    background: green,\n        yellow;\n}\n", null);

        // comments formatting:
        format("a{\n/*comme\n  *dddsd\n nt*/\nbackground: red;\n}",
                "a{\n    /*comme\n      *dddsd\n     nt*/\n    background: red;\n}", null);

        // even though lines are preserved they will be indented according to indent of previous line;
        // I'm not sure it is OK but leaving like that for now:
        format("a{\ncolor: red; /* start\n   comment\n end*/ background: blue;\n}",
                "a{\n    color: red; /* start\n       comment\n     end*/\n    background: blue;\n}", null);

        // formatting of last line:
        format("a{\nbackground: red,",
                "a{\n    background: red,", null);
        format("a{\nbackground: red,\n",
                "a{\n    background: red,\n", null);

        // #160105:
        format("/* unfinished comment\n* /\n\n/* another comment\n*/",
                "/* unfinished comment\n* /\n\n/* another comment\n*/", null);
        format("a{\n    /*\n    comment\n    */\n    color: green;\n}",
                "a{\n    /*\n    comment\n    */\n    color: green;\n}", null);

        // #218884
        format("/**\n    *\n  */",
                "/**\n    *\n  */", null);

        format("h1{font-face: 12pt;font-family: sans-serif;}h2{font-face: 10pt;font-family: sans-serif;}",
            "h1{\n    font-face: 12pt;\n    font-family: sans-serif;\n}\nh2{\n    font-face: 10pt;\n    font-family: sans-serif;\n}", null);
    }

    public void testNativeEmbeddingFormattingCase1() throws Exception {
        reformatFileContents("testfiles/format1.html", new IndentPrefs(4, 4));
    }

    public void testNativeEmbeddingFormattingCase2() throws Exception {
        reformatFileContents("testfiles/format2.html", new IndentPrefs(4, 4));
    }

    public void testTabCharacterHandling() throws Exception {
        reformatFileContents("testfiles/format3.html", new IndentPrefs(4, 4));
    }

    public void testFormattingCase1() throws Exception {
        //#160344
        reformatFileContents("testfiles/case001.css", new IndentPrefs(4, 4));
    }

    public void testFormattingCase2() throws Exception {
        reformatFileContents("testfiles/case002.css", new IndentPrefs(4, 4));
    }

    public void testFormattingCase3() throws Exception {
        // #160089
        reformatFileContents("testfiles/case003.css", new IndentPrefs(4, 4));
    }

    public void testFormattingCase4() throws Exception {
        // #161874
        reformatFileContents("testfiles/case004.css", new IndentPrefs(4, 4));
    }

    public void testFormattingNetBeansCSS() throws Exception {
        reformatFileContents("testfiles/netbeans.css", new IndentPrefs(4, 4));
    }

    public void testPartitialFormatting() throws Exception {
        IndentPrefs preferences = new IndentPrefs(4, 4);
        String file = "testfiles/partialformatting.css";

        FileObject fo = getTestFile(file);
        assertNotNull(fo);
        BaseDocument doc = getDocument(fo);
        assertNotNull(doc);

        Formatter formatter = getFormatter(preferences);

        setupDocumentIndentation(doc, preferences);
        format(doc, formatter, 1056, 1173, false);

        String after = doc.getText(0, doc.getLength());
        assertDescriptionMatches(file, after, false, ".formatted");

        DataObject.find(fo).getLookup().lookup(Closable.class).close();

        doc = getDocument(fo);
        setupDocumentIndentation(doc, preferences);
        format(doc, formatter, 857, 904, false);
        after = doc.getText(0, doc.getLength());
        assertDescriptionMatches(file, after, false, ".formatted2");
    }

    public void testIndentation() throws Exception {
        // property indentation:
        insertNewline("a{^background: red;\n  }\n", "a{\n    ^background: red;\n  }\n", null);
        insertNewline("a{\n    background: red;\n}\na{\n    background: red;^\n}", "a{\n    background: red;\n}\na{\n    background: red;\n    ^\n}", null);
        insertNewline("a{background: red;}\nb{^", "a{background: red;}\nb{\n    ^", null);
        insertNewline("a {       background: red;^a: b;", "a {       background: red;\n          ^a: b;", null);
        insertNewline("a {       background: red,^blue", "a {       background: red,\n              ^blue", null);
        // value indentation:
        insertNewline("a{\n    background:^red;\n  }\n", "a{\n    background:\n        ^red;\n  }\n", null);
        insertNewline("a { background: red,^blue; }", "a { background: red,\n        ^blue; }", null);
        // indentation of end of line:
        insertNewline("a { background: red,^", "a { background: red,\n        ^", null);
        insertNewline("a { background: red,^\n", "a { background: red,\n        ^\n", null);
        // property indentation:
        insertNewline("a{\n    background: red;^\n  }\n", "a{\n    background: red;\n    ^\n  }\n", null);
        // new rule indentation:
        insertNewline("a{\n    background: red;\n  }^", "a{\n    background: red;\n  }\n  ^", null);
        // check that indentation cooperates with bracket insertion:
        insertNewline("a{^}", "a{\n    ^\n}", null);
        insertNewline("a{\n/**/^\n}", "a{\n/**/\n^\n}", null);
        insertNewline("a{\n     /*^comment\n     */\n}", "a{\n     /*\n     ^comment\n     */\n}", null);

        //#160344
        insertNewline(
                "xxxxxh2 { color:aqua /* aaa^bbb\nccc*/ ;}",
                "xxxxxh2 { color:aqua /* aaa\n                     ^bbb\nccc*/ ;}", null);
        insertNewline(
                "xxxxxh2 { color:aqua /* aaa^*/ ;}",
                "xxxxxh2 { color:aqua /* aaa\n              ^*/ ;}", null);

        //#161642
        insertNewline(
                "xxxxxh2 { color:aqua /* aaa^*/;}",
                "xxxxxh2 { color:aqua /* aaa\n              ^*/;}", null);

        // #160089
        insertNewline(
                "@media TV{\n    h1{}^}",
                "@media TV{\n    h1{}\n^}", null);
        insertNewline(
                "@media TV, Screen {\n    h1 > h2 + h3 h4 {\n    }^}",
                "@media TV, Screen {\n    h1 > h2 + h3 h4 {\n    }\n^}", null);
        insertNewline(
                "@media Screen { h1{\n                ^}\n}",
                "@media Screen { h1{\n                \n                ^}\n}", null);

        // #164493
        insertNewline(
                "@media page {\n    @media {^}\n}\n",
                "@media page {\n    @media {\n        ^\n    }\n}\n", null);

        //#187524
        insertNewline(
                "a something { /* comment*/^color:green",
                "a something { /* comment*/\n    ^color:green", null);
    }

    @Override
    /**
     * really ugly override of the default impl. Since the KeystrokeHandlers may
     * invoke reformat which is in some cases done in a separate EDT task, we
     * need to wait for such modification before we try to compare the document
     * content with the golden file.
     */
    public void insertNewline(final String source, final String reformatted, final IndentPrefs preferences) throws Exception {
        final int sourcePos = source.indexOf('^');
        assertNotNull(sourcePos);
        final String source2 = source.substring(0, sourcePos) + source.substring(sourcePos + 1);


        final AtomicReference<Document> doc = new AtomicReference<>();
        final AtomicReference<Caret> caret = new AtomicReference<>();
        //first invoke the action in the AWT
        try {
            JEditorPane ta = getPane(source2);
            Caret c = ta.getCaret();
            caret.set(c);
            c.setDot(sourcePos);
            BaseDocument bdoc = (BaseDocument) ta.getDocument();
            doc.set(bdoc);
            Formatter formatter = getFormatter(null);
            if (formatter != null) {
                configureIndenters(bdoc, formatter, true);
            }
            setupDocumentIndentation(bdoc, preferences);
            runKitAction(ta, DefaultEditorKit.insertBreakAction, "\n");
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }

        try {
            int reformattedPos = reformatted.indexOf('^');
            assertNotNull(reformattedPos);
            String reformatted2 = reformatted.substring(0, reformattedPos) + reformatted.substring(reformattedPos + 1);
            Document _doc = doc.get();
            Caret _caret = caret.get();
            String formatted = _doc.getText(0, _doc.getLength());
            assertEquals(reformatted2, formatted);
            if (reformattedPos != -1) {
                assertEquals(reformattedPos, _caret.getDot());
            }
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }

    }
}
