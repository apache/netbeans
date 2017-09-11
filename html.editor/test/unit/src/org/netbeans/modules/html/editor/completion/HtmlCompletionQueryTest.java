/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.html.editor.completion;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.junit.MockServices;
import org.netbeans.modules.html.editor.HtmlExtensions;
import org.netbeans.modules.html.editor.HtmlPreferences;
import org.netbeans.modules.html.editor.api.completion.HtmlCompletionItem;
import org.netbeans.modules.html.editor.api.gsf.HtmlExtension;
import org.netbeans.modules.html.editor.api.gsf.HtmlExtension.CompletionContext;
import org.netbeans.modules.html.editor.completion.HtmlCompletionTestSupport.Match;
import org.netbeans.modules.html.editor.lib.api.HtmlSource;
import org.netbeans.modules.html.editor.lib.api.HtmlVersion;
import org.netbeans.modules.html.editor.lib.api.foreign.UndeclaredContentResolver;
import org.netbeans.modules.html.parser.HtmlDocumentation;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.spi.editor.completion.CompletionItem;

/**Html completion test
 * This class extends TestBase class which provides access to the html editor module layer
 *
 * @author Marek Fukala
 */
public class HtmlCompletionQueryTest extends HtmlCompletionTestBase {

    public HtmlCompletionQueryTest(String name) throws IOException, BadLocationException {
        super(name);
    }

    @Override
    protected HtmlVersion getExpectedVersion() {
        return HtmlVersion.HTML41_TRANSATIONAL;
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        HtmlVersion.DEFAULT_VERSION_UNIT_TESTS_OVERRIDE = HtmlVersion.HTML41_TRANSATIONAL;
        MockServices.setServices(MockMimeLookup.class);
        HtmlDocumentation.setupDocumentationForUnitTests();
    }

    public static Test suite() throws IOException, BadLocationException {
	TestSuite suite = new TestSuite();
        suite.addTest(new HtmlCompletionQueryTest("testSimpleEndTagBeforeText"));
        return suite;
    }

    //test methods -----------
//    public void testIndexHtml() throws IOException, BadLocationException {
//        testCompletionResults("index.html");
//    }
//
//    public void testNetbeansFrontPageHtml() throws IOException, BadLocationException {
//        testCompletionResults("netbeans.org.html");
//    }



    public void testSimpleEndTag() throws BadLocationException, ParseException {
        assertItems("<div></|", arr("div"), Match.CONTAINS, 7);
        //           01234567
    }

    public void testSimpleEndTagBeforeText() throws BadLocationException, ParseException {
        assertItems("<div></| text ", arr("div"), Match.CONTAINS, 7);
        //           01234567
    }

    public void testOpenTagWithCommonPrefix() throws BadLocationException, ParseException {
        assertItems("<a| ", arr("abbr"), Match.CONTAINS, 1);
        //           01234567
    }

    public void testOpenTagJustAfterText() throws BadLocationException, ParseException {
        assertItems("text<| ", arr("abbr"), Match.CONTAINS, 5);
        //           01234567
    }

    //causes: java.lang.AssertionError: content is null after sibling 'meta'
    public void testOpenTagAtMetaTagEnd() throws BadLocationException, ParseException {
        assertItems("<html><head><meta><title></title><| </head></html>", arr("link"), Match.CONTAINS);
        //           01234567
    }


    public void testTags() throws BadLocationException, ParseException {
        assertItems("<|", arr("div"), Match.CONTAINS, 1);
        assertItems("<|", arr("jindra"), Match.DOES_NOT_CONTAIN);
        assertItems("<d|", arr("div"), Match.CONTAINS, 1);
        assertItems("<d|", arr("html"), Match.DOES_NOT_CONTAIN, 1);
        assertItems("<div|", arr("div"), Match.CONTAINS, 1);
        assertItems("<div|", arr("body"), Match.DOES_NOT_CONTAIN, 1);

        //           01234567
        assertItems("<div></|", arr("div"), Match.CONTAINS, 7);
        assertItems("<div></d|", arr("div"), Match.CONTAINS, 7);
        assertItems("<div></div|", arr("div"), Match.EXACT, 7);
    }

    public void testCompleteTags() throws BadLocationException, ParseException {
        assertCompletedText("<|", "div", "<div|");
        assertCompletedText("<di|", "div", "<div|");
        assertCompletedText("<div|", "div", "<div|");

        assertCompletedText("<div></|", "div", "<div></div>|");
        assertCompletedText("<div></d|", "div", "<div></div>|");
        assertCompletedText("<div></div|", "div", "<div></div>|");
    }
    
    public void testCompleteTagsBeforeText() throws BadLocationException, ParseException {
        assertCompletedText("<| ", "div", "<div| ");
        assertCompletedText("<di| ", "div", "<div| ");
        assertCompletedText("<div| ", "div", "<div| ");

        assertCompletedText("<div></| ", "div", "<div></div>| ");
        assertCompletedText("<div></d| ", "div", "<div></div>| ");
        assertCompletedText("<div></div| ", "div", "<div></div>| ");
    }

    public void testTagsBeforeText() throws BadLocationException, ParseException {
        assertItems("<| ", arr("div"), Match.CONTAINS, 1);
        assertItems("<| ", arr("jindra"), Match.DOES_NOT_CONTAIN);
        assertItems("<d| ", arr("div"), Match.CONTAINS, 1);
        assertItems("<div| ", arr("div"), Match.CONTAINS, 1);

        //           01234567
        assertItems("<div></| ", arr("div"), Match.CONTAINS, 7);
        assertItems("<div></d| ", arr("div"), Match.CONTAINS, 7);
        assertItems("<div></div| ", arr("div"), Match.EXACT, 7);
    }


    public void testTagAttributes() throws BadLocationException, ParseException {
        //           012345
        assertItems("<div |", arr("class"), Match.CONTAINS, 5);
        assertItems("<div |", arr("jindra"), Match.DOES_NOT_CONTAIN);
        assertItems("<div c|", arr("class"), Match.CONTAINS, 5);
    }

    public void testCompleteTagAttributes() throws BadLocationException, ParseException {
        assertCompletedText("<div |", "class", "<div class=\"|\"");
        assertCompletedText("<div c|", "class", "<div class=\"|\"");
    }

    public void testTagAttributeValues() throws BadLocationException, ParseException {
        assertItems("<col align=\"|\"", arr("center"), Match.CONTAINS, 12);
        //           01234567890 12
        assertItems("<col align=\"ce|\"", arr("center"), Match.CONTAINS, 12);
        assertItems("<col align=\"center|\"", arr("center"), Match.EXACT, 12);
    }

    public void testCompleteTagAttributeValues() throws BadLocationException, ParseException {
        assertCompletedText("<col align=\"|\"", "center", "<col align=\"center|\"");
        assertCompletedText("<col align=\"ce|\"", "center", "<col align=\"center|\"");

        //regression test - issue #161852
        assertCompletedText("<col align=\"|\"", "left", "<col align=\"left|\"");

        //test single quote
        assertCompletedText("<col align='|'", "center", "<col align='center'|");

        //test values cc without quotation
        assertCompletedText("<col align=|", "left", "<col align=left|");
        assertCompletedText("<col align=ri|", "right", "<col align=right|");
    }

    public void testCharacterReferences() throws BadLocationException, ParseException {
        assertItems("&|", arr("amp"), Match.CONTAINS, 1);
        assertItems("&a|", arr("amp"), Match.CONTAINS, 1);
        assertItems("&amp|", arr("amp"), Match.EXACT, 1);
        assertItems("|&amp;", arr("amp"), Match.CONTAINS, 1);

        assertCompletedText("&|", "amp", "&amp;|");
        assertCompletedText("&am|", "amp", "&amp;|");
    }

    public void testBooleanAttributes() throws BadLocationException, ParseException {
        assertItems("<input d|", arr("disabled"), Match.CONTAINS, 7);
        //           01234567
        assertCompletedText("<input d|", "disabled", "<input disabled|");
    }

    public void testFileAttrValue() throws BadLocationException, ParseException {
        String code = "<a href='|'";
        //             01234567890
        //we need a fileobject backed document here
        Document doc = createDocuments("test.html", "another.html", "image.png")[0]; //use test.html
        doc.insertString(0, code, null);
        assertItems(doc, arr("../", "another.html", "image.png"), Match.CONTAINS, 9);

        //double quotes
        code = "<a href=\"|\"";
        //             01234567890
        //we need a fileobject backed document here
        doc = createDocuments("test.html", "another.html", "image.png")[0]; //use test.html
        doc.insertString(0, code, null);
        assertItems(doc, arr("../", "another.html", "image.png"), Match.CONTAINS, 9);

        //unquoted
        code = "<a href=|";
        //             01234567890
        //we need a fileobject backed document here
        doc = createDocuments("test.html", "another.html", "image.png")[0]; //use test.html
        doc.insertString(0, code, null);
        assertItems(doc, arr("../", "another.html", "image.png"), Match.CONTAINS, 8);
    }

     public void testFileAttrValueAllTags() throws BadLocationException, ParseException {
        String code = "<link href='|'";
        //             01234567890
        //we need a fileobject backed document here
        Document doc = createDocuments("test.html", "another.html", "image.png")[0]; //use test.html
        doc.insertString(0, code, null);
        assertItems(doc, arr("../", "another.html", "image.png"), Match.CONTAINS, 12);

        code = "<base href='|'";
        //             01234567890
        //we need a fileobject backed document here
        doc = createDocuments("test.html", "another.html", "image.png")[0]; //use test.html
        doc.insertString(0, code, null);
        assertItems(doc, arr("../", "another.html", "image.png"), Match.CONTAINS, 12);

     }

    public void testFileAttrValueFolders() throws BadLocationException, ParseException {
        String code = "<a href='|'";
        //             01234567890
        //we need a fileobject backed document here
        Document doc = createDocuments("test.html", "folder1/another.html", "images/image.png")[0]; //use test.html
        doc.insertString(0, code, null);
        assertItems(doc, arr("folder1/", "images/", "../"), Match.CONTAINS, 9);

        //complete in folder
        code = "<a href='folder1/|'";
        //      012345678901234567
        //we need a fileobject backed document here
        doc = createDocuments("test.html", "folder1/another.html", "images/image.png")[0]; //use test.html
        doc.insertString(0, code, null);
        assertItems(doc, arr("../", "another.html"), Match.CONTAINS, 9);

        //complete go up in nested folders
        code = "<a href='folder1/folder2/|'";
        //      01234567890123456789023456
        //we need a fileobject backed document here
        doc = createDocuments("test.html", "folder1/folder2/another.html")[0]; //use test.html
        doc.insertString(0, code, null);
        assertItems(doc, arr("../", "another.html"), Match.CONTAINS, 9);
        
    }

    public void testFileAttrValueWithPrefix() throws BadLocationException, ParseException {
        String code = "<a href='ima|'";
        //             01234567890
        //we need a fileobject backed document here
        Document doc = createDocuments("test.html", "another.html", "image.png")[0]; //use test.html
        doc.insertString(0, code, null);
        assertItems(doc, arr("image.png"), Match.CONTAINS, 9);

        //unquoted
        code = "<a href=ima|";
        //             01234567890
        //we need a fileobject backed document here
        doc = createDocuments("test.html", "another.html", "image.png")[0]; //use test.html
        doc.insertString(0, code, null);
        assertItems(doc, arr("image.png"), Match.CONTAINS, 8);

        //complete go up in nested folders
        code = "<a href='folder1/folder2/.|'";
        //      01234567890123456789023456
        //we need a fileobject backed document here
        doc = createDocuments("test.html", "folder1/folder2/another.html")[0]; //use test.html
        doc.insertString(0, code, null);
        assertItems(doc, arr("../"), Match.CONTAINS, 9);

        //complete go up in nested folders
        code = "<a href='folder1/folder2/..|'";
        //      01234567890123456789023456
        //we need a fileobject backed document here
        doc = createDocuments("test.html", "folder1/folder2/another.html")[0]; //use test.html
        doc.insertString(0, code, null);
        assertItems(doc, arr("../"), Match.CONTAINS, 9);
    }

    public void testFileAttrValueUppercase() throws BadLocationException, ParseException {
        String code = "<A HREF='|'";
        //             01234567890
        //we need a fileobject backed document here
        Document doc = createDocuments("test.html", "another.html", "image.png")[0]; //use test.html
        doc.insertString(0, code, null);
        assertItems(doc, arr("../", "another.html", "image.png"), Match.CONTAINS, 9);
    }

    public void testCompleteAttributesInUnknownTag() throws BadLocationException, ParseException {
        assertItems("<gggg |", arr(), Match.EMPTY);
        assertItems("<gggg hhh=|", arr(), Match.EMPTY);
        assertItems("<gggg hhh='|", arr(), Match.EMPTY);
    }

    public void testEndTagAutocompletion() throws BadLocationException, ParseException {
        assertItems("<div>|", arr("div"), Match.EXACT, 5);
    }
    
    public void testNoEndTagAutocompletion() throws BadLocationException, ParseException {
        //test end tag ac for unknown tags
        assertItems("<div><bla>|", arr(), Match.EMPTY);
    }

    public void testJustBeforeTag() throws BadLocationException, ParseException {
        assertItems("<|<table>", arr("div"), Match.CONTAINS);
        assertItems("<div></|<table>", arr("div"), Match.CONTAINS);
    }

    public void testNoCompletionInDoctype() throws BadLocationException, ParseException {
        assertItems("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" |  \"http://www.w3.org/TR/html40/strict.dtd\">", arr(), Match.EMPTY);
    }

    public void testEndTagsAutoCompletionOfUndeclaredTags() throws BadLocationException, ParseException {
        assertItems("<x:out>|", arr("x:out"), Match.CONTAINS);
    }

    public void testEndTagsCompletionOfUndeclaredTags() throws BadLocationException, ParseException {

        assertItems("<x:out></|", arr("x:out"), Match.CONTAINS);
        assertItems("<x:out></x|", arr("x:out"), Match.CONTAINS);
        assertItems("<x:out></x:|", arr("x:out"), Match.CONTAINS);
        assertItems("<x:out></x:ou|", arr("x:out"), Match.CONTAINS);
        assertItems("<x:out></x:out|", arr("x:out"), Match.CONTAINS);
        
        //nested - the tags needs to be close, so only the closest unclosed tag is offered
        assertItems("<x:out><x:in></|", arr("x:in"), Match.CONTAINS);
        assertItems("<x:out><x:in></x:|", arr("x:in"), Match.CONTAINS);
        assertItems("<x:out><x:in></|", arr("x:out"), Match.DOES_NOT_CONTAIN);
        assertItems("<x:out><x:in></x:|", arr("x:out"), Match.DOES_NOT_CONTAIN);

        assertItems("<x:out><x:in></x:in></|", arr("x:out"), Match.CONTAINS);
        assertItems("<x:out><x:in></x:in></x|", arr("x:out"), Match.CONTAINS);
        assertItems("<x:out><x:in></x:in></x:|", arr("x:out"), Match.CONTAINS);
        assertItems("<x:out><x:in></x:in></|", arr("x:in"), Match.DOES_NOT_CONTAIN);
    }

    public void testEndTagsCompletionOfUndeclaredTagsMixedWithHtml() throws BadLocationException, ParseException {
        //including html content
//        assertItems("<div><x:out></|", arr("x:out"), Match.CONTAINS);
        assertItems("<div><x:out></x| </div>", arr("x:out"), Match.CONTAINS);
        assertItems("<div><x:out></x:|", arr("x:out"), Match.CONTAINS);
        assertItems("<p><x:out></x:ou| </p>", arr("x:out"), Match.CONTAINS);
        assertItems("<div><div><x:out></x:out|", arr("x:out"), Match.CONTAINS);

        //nested - the tags needs to be close, so only the closest unclosed tag is offered
        assertItems("<div><x:out><div><x:in></|", arr("x:in"), Match.CONTAINS);
        assertItems("<div><x:out><div><x:in></x:| </div></div>", arr("x:in"), Match.CONTAINS);
        assertItems("<p><x:out><x:in></|", arr("x:out"), Match.DOES_NOT_CONTAIN);
        assertItems("<p><x:out><x:in></x:|", arr("x:out"), Match.DOES_NOT_CONTAIN);

        assertItems("<x:out><div><x:in></x:in></div></|", arr("x:out"), Match.CONTAINS);
        assertItems("<x:out><div><x:in></div></x:in></x|", arr("x:out"), Match.CONTAINS); //crossed
        assertItems("<div><x:out><div><x:in></x:in></div></x:| </div>", arr("x:out"), Match.CONTAINS);
        assertItems("<p><x:out><x:in></x:in></|", arr("x:in"), Match.DOES_NOT_CONTAIN);
    }

    public void testIssue177347 () throws BadLocationException, ParseException {
//        assertItems("<td><a h|</td>  ", arr("href"), Match.CONTAINS);
    }

    public void testHtmlExtensionCompletionOfTagAttribute() throws BadLocationException, ParseException {
        HtmlExtensions.TEST_EXTENSION = new HtmlExtension() {

            @Override
            public List<CompletionItem> completeAttributes(CompletionContext context) {
                List<CompletionItem> items = new ArrayList<>();
                items.add(HtmlCompletionItem.createAttributeValue("fake", 0));
                return items;
            }
            
            @Override
            public Map<String, List<String>> getUndeclaredNamespaces(HtmlSource source) {
                return Collections.singletonMap("myns", Collections.singletonList("my"));
            }

        };
        
        assertItems("<my:tag att|", arr("fake"), Match.CONTAINS);
        
        HtmlExtensions.TEST_EXTENSION = null;
    }
    
    public void testHtmlExtensionCompletionOfTagAttributeValue() throws BadLocationException, ParseException {
        HtmlExtensions.TEST_EXTENSION = new HtmlExtension() {

            @Override
            public List<CompletionItem> completeAttributeValue(CompletionContext context) {
                List<CompletionItem> items = new ArrayList<>();
                items.add(HtmlCompletionItem.createAttributeValue("fake", 0));
                return items;
            }
            
            @Override
            public Map<String, List<String>> getUndeclaredNamespaces(HtmlSource source) {
                return Collections.singletonMap("myns", Collections.singletonList("my"));
            }

        };
        
        assertItems("<my:tag attr=|", arr("fake"), Match.CONTAINS);
        assertItems("<my:tag attr=\"|", arr("fake"), Match.CONTAINS);
        assertItems("<my:tag attr=\"|\"", arr("fake"), Match.CONTAINS);
        assertItems("<my:tag attr=\"fa|\"", arr("fake"), Match.CONTAINS);
        
        HtmlExtensions.TEST_EXTENSION = null;
    }
    
    public void testInputTagTypeAttributeCompletion() throws BadLocationException, ParseException {
        assertItems("<input type=\"|\">", arr("text", "password", "reset"), Match.CONTAINS);
    }
    
    
    public void testLinkTypeAttributeCompletion() throws BadLocationException, ParseException {
        assertItems("<link rel=\"|\">", arr("stylesheet", "chapter"), Match.CONTAINS);
        assertItems("<link rel=\"|\">", arr("foo"), Match.DOES_NOT_CONTAIN);
    }
    
    //Bug 204227 - When HTML Completion Offers End Tags After Less Than Character option is enabled and used the previous character gets deleted
    public void testIssue204227() throws BadLocationException, ParseException {
        assertCompletedText("<p>abc</|", "p", "<p>abc</p>|");

        HtmlPreferences.completionOffersEndTagAfterLt(); //load
        HtmlPreferences.completionOffersEndTagAfterLt = true; //reset the value
        assertCompletedText("<p>abc<|", "/p", "<p>abc</p>|");
    }
    
    public void testDirAttribute() throws BadLocationException, ParseException {
        assertItems("<div dir=\"|\">", arr("ltr", "rtl"), Match.EXACT);
    }
    
    //Bug 197614 - Problem with HTML4 & code completion - non matching tags offered
    public void testIssue197614() throws BadLocationException, ParseException {
        assertItems("<table><tr><td></ta|", arr("table"), Match.EXACT);
    }

    
    //helper methods ------------

    @Override
    //test HTML 4.01
    protected String getPublicID() {
        return "-//W3C//DTD HTML 4.01 Transitional//EN";
    }

  
}
