/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.html.parser;

import java.util.Properties;
import org.netbeans.modules.html.parser.model.HtmlTagProvider;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.logging.Level;
import static junit.framework.Assert.assertNotNull;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.modules.html.editor.lib.api.SyntaxAnalyzer;
import org.netbeans.modules.html.editor.lib.api.elements.Attribute;
import org.netbeans.modules.html.editor.lib.api.elements.CloseTag;
import org.netbeans.modules.html.editor.lib.api.elements.Element;
import org.netbeans.modules.html.editor.lib.api.elements.ElementType;
import org.netbeans.modules.html.editor.lib.api.elements.ElementUtils;
import org.netbeans.modules.html.editor.lib.api.HtmlSource;
import org.netbeans.modules.html.editor.lib.api.ParseException;
import org.netbeans.modules.html.editor.lib.api.ProblemDescription;
import org.netbeans.modules.html.editor.lib.api.HelpItem;
import org.netbeans.modules.html.editor.lib.api.elements.Node;
import org.netbeans.modules.html.editor.lib.api.elements.OpenTag;
import org.netbeans.modules.html.editor.lib.api.model.HtmlModel;
import org.netbeans.modules.html.editor.lib.api.HtmlParseResult;
import org.netbeans.modules.html.editor.lib.api.model.HtmlTag;
import org.netbeans.modules.html.editor.lib.api.model.HtmlTagAttribute;
import org.netbeans.modules.html.editor.lib.api.model.HtmlTagType;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.html.editor.lib.api.*;
import org.netbeans.modules.html.editor.lib.api.elements.ElementVisitor;
import org.netbeans.modules.html.editor.lib.api.model.*;
import org.netbeans.modules.html.parser.model.ElementDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.xml.sax.SAXException;

/**
 *
 * @author marekfukala
 */
public class Html5ParserTest extends NbTestCase {

    public Html5ParserTest(String name) {
        super(name);
    }

    public static Test Xsuite() {
        String testName = "testParseFileLongerThan2048chars";

        System.err.println("Only " + testName + " test is going to be run!!!!");
        System.err.println("******************************************************\n");

//        NodeTreeBuilder.DEBUG = true;
//        NodeTreeBuilder.DEBUG_STATES = true;
        TestSuite suite = new TestSuite();
        suite.addTest(new Html5ParserTest(testName));
        return suite;
    }

    public void testaParseErrorneousHeadContent() throws ParseException {
        //the &lt; char after the title close tag makes the parse tree really bad
        String code = "<!doctype html>\n"
                + "<html>\n"
                + "<head>\n"
                + "<title>\n"
                + "</title> < \n"
                + "</head>\n"
                + "<body>\n"
                + "</body>\n"
                + "</html>\n";

//        NodeTreeBuilder.DEBUG_STATES = true;
        HtmlParseResult result = parse(code);
        Node root = result.root();
        assertNotNull(root);
//        NodeUtils.dumpTree(result.root());

//        Collection<ProblemDescription> problems = result.getProblems();
//        for(ProblemDescription pd : problems) {
//            System.out.println(pd);
//        }

    }

    public void testSimpleDocument() throws ParseException {
        String code = "<!doctype html><html><head><title>x</title></head><body><div onclick=\"alert();\"/></body></html>";
        //             012345678901234567890123456789012345678901234567890123456789012345678 901234567 8901234567890123456789
        //             0         1         2         3         4         5         6          7          8         9

//        NodeTreeBuilder.DEBUG_STATES = true;
        HtmlParseResult result = parse(code);
        Node root = result.root();
        assertNotNull(root);
//        NodeUtils.dumpTree(result.root());

        OpenTag html = ElementUtils.query(root, "html");
        assertEquals("html", html.name());
        assertEquals(15, html.from());
        assertEquals(21, html.to());
        assertEquals(15, html.from());
        assertEquals(95, html.semanticEnd());

        OpenTag body = ElementUtils.query(root, "html/body");
        assertEquals("body", body.name());
        assertEquals(50, body.from());
        assertEquals(56, body.to());
        assertEquals(50, body.from());
        assertEquals(88, body.semanticEnd());

        CloseTag bodyEndTag = body.matchingCloseTag();
        assertNotNull(bodyEndTag);
        assertSame(body, bodyEndTag.matchingOpenTag());
        assertSame(bodyEndTag, body.matchingCloseTag());

        OpenTag title = ElementUtils.query(root, "html/head/title");
        assertEquals("title", title.name());
        assertEquals(27, title.from());
        assertEquals(34, title.to());
        assertEquals(27, title.from());
        assertEquals(43, title.semanticEnd());

        CloseTag titleEndTag = title.matchingCloseTag();
        assertNotNull(titleEndTag);
        assertSame(title, titleEndTag.matchingOpenTag());
        assertSame(titleEndTag, title.matchingCloseTag());



    }

    public void testBasic() throws SAXException, IOException, ParseException {
        HtmlParseResult result = parse("<!doctype html><section><div></div></section>");
        Node root = result.root();
        assertNotNull(root);
        assertNotNull(ElementUtils.query(root, "html/body/section/div")); //html/body are generated

    }

    public void testHtmlAndBodyTags() throws ParseException {
        HtmlParseResult result = parse("<!DOCTYPE html><html><head><title>hello</title></head><body><div>ahoj</div></body></html>");
        Node root = result.root();
//        NodeUtils.dumpTree(root);
        assertNotNull(root);
        assertNotNull(ElementUtils.query(root, "html"));
        assertNotNull(ElementUtils.query(root, "html/head"));
        assertNotNull(ElementUtils.query(root, "html/head/title"));
        assertNotNull(ElementUtils.query(root, "html/body"));
        assertNotNull(ElementUtils.query(root, "html/body/div"));
    }

    public void testAttributes() throws ParseException {
        HtmlParseResult result = parse("<!DOCTYPE html><html><head><title>hello</title></head><body onclick=\"alert()\"></body></html>");
        Node root = result.root();
//        NodeUtils.dumpTree(root);
        assertNotNull(root);
        OpenTag body = ElementUtils.query(root, "html/body");
        assertNotNull(body);

        assertEquals(1, body.attributes().size());

        Attribute attr = body.attributes().iterator().next();
        assertNotNull(attr);
        assertEquals("onclick", attr.name().toString());
        assertEquals("\"alert()\"", attr.value().toString());
    }

//    public void testProblemsReporting() throws ParseException {
//        HtmlParseResult result = parse("<!DOCTYPE html></section>");
//        //                              012345678901234567890123456789
//        //                              0         1         2
//        Collection<ProblemDescription> problems = result.getProblems();
//
//        assertEquals(1, problems.size());
//        ProblemDescription p = problems.iterator().next();
//
//        assertEquals(ProblemDescription.ERROR, p.getType());
//        assertEquals("nokey", p.getKey()); //XXX fix that
//        assertEquals("Stray end tag “section”.", p.getText());
//        assertEquals(15, p.getFrom());
//        assertEquals(25, p.getTo());
//
//    }
    public void testStyle() throws ParseException {
        String code = "<!DOCTYPE html>\n<style type=\"text/css\">\n@import \"resources2/ezcompik/newcss2moje.css\";\n</style>\n";
        //             0123456789012345 67890123456 7890123456 789 012345678 90123456789012345678 90123456789012 345678901 23456789
        //             0         1          2          3           4          5         6          7          8           9

//        NodeTreeBuilder.DEBUG = true;
//        NodeTreeBuilder.DEBUG_STATES = true;
        HtmlParseResult result = parse(code);
        Node root = result.root();
        assertNotNull(root);
//        NodeUtils.dumpTree(result.root());

        OpenTag head = ElementUtils.query(root, "html/head");
        assertNotNull(head);
        assertEquals(2, head.children().size());

        Iterator<Element> itr = head.children().iterator();

        OpenTag styleOpenTag = (OpenTag) itr.next();
        assertNotNull(styleOpenTag);
        assertEquals(16, styleOpenTag.from());
        assertEquals(39, styleOpenTag.to());

        Element styleEndTag = itr.next();
        assertNotNull(styleEndTag);
        assertEquals(87, styleEndTag.from());
        assertEquals(95, styleEndTag.to());

        assertSame(styleEndTag, styleOpenTag.matchingCloseTag());

    }

    public void testParseUnfinishedCode() throws ParseException {
        String code = "<!DOCTYPE HTML>"
                + "<html>"
                + "<head>"
                + "<title>"
                + "</title>"
                + "</head>"
                + "<body>"
                + "</table>"
                + "</html>";

        HtmlParseResult result = parse(code);
        Node root = result.root();

        assertNotNull(root);

//        NodeUtils.dumpTree(root);

        Node html = ElementUtils.query(root, "html");
        assertNotNull(html);

        Collection<Element> children = html.children();
        assertEquals(4, children.size()); // <head>, </head>,<body>,</table>
        Iterator<Element> childernItr = children.iterator();

        assertTrue(childernItr.hasNext());
        Element child = childernItr.next();
        assertEquals(ElementType.OPEN_TAG, child.type());
        assertEquals("head", ((OpenTag) child).name().toString());
        assertTrue(childernItr.hasNext());
        child = childernItr.next();
        assertEquals(ElementType.CLOSE_TAG, child.type());
        assertEquals("head", ((CloseTag) child).name().toString());
        assertTrue(childernItr.hasNext());
        child = childernItr.next();
        assertEquals(ElementType.OPEN_TAG, child.type());
        assertEquals("body", ((OpenTag) child).name().toString());
        assertTrue(childernItr.hasNext());
        child = childernItr.next();
        assertEquals(ElementType.CLOSE_TAG, child.type());
        assertEquals("table", ((CloseTag) child).name().toString());


//        NodeUtils.dumpTree(root);

    }

    public void testLogicalRangesOfUnclosedOpenTags() throws ParseException {
        HtmlParseResult result = parse("<!DOCTYPE html>"
                + "<html>"
                + "<head>"
                + "<title>hello</title>"
                + "</head>"
                + "<body>"
                + "<table>"
                + "</html>");
        Node root = result.root();

//        NodeUtils.dumpTree(root);

        assertNotNull(root);
        OpenTag htmlOpen = ElementUtils.query(root, "html");
        assertNotNull(htmlOpen);
        CloseTag htmlEnd = htmlOpen.matchingCloseTag();
        assertNotNull(htmlEnd);

        assertNotNull(ElementUtils.query(root, "html/head"));
        assertNotNull(ElementUtils.query(root, "html/head/title"));
        OpenTag body = ElementUtils.query(root, "html/body");
        assertNotNull(body);
        OpenTag table = ElementUtils.query(root, "html/body/table");
        assertNotNull(table);

        //both body and table should be logically closed at the beginning of the html end tag
        assertEquals(htmlEnd.from(), body.semanticEnd());
        assertEquals(htmlEnd.from(), table.semanticEnd());
    }

    public void testGetPossibleOpenTagsInContext() throws ParseException {
        HtmlParseResult result = parse("<!DOCTYPE html><html><head><title>hello</title></head><body><div>ahoj</div></body></html>");

        assertNotNull(result.root());

        Node body = ElementUtils.query(result.root(), "html/body");
        Collection<HtmlTag> possible = result.getPossibleOpenTags(body);

        assertTrue(!possible.isEmpty());

        HtmlTag divTag = new HtmlTagImpl("div");
        HtmlTag headTag = new HtmlTagImpl("head");

        assertTrue(possible.contains(divTag));
        assertFalse(possible.contains(headTag));

        Node head = ElementUtils.query(result.root(), "html/head");
        possible = result.getPossibleOpenTags(head);

        assertTrue(!possible.isEmpty());

        HtmlTag titleTag = new HtmlTagImpl("title");
        assertTrue(possible.contains(titleTag));
        assertFalse(possible.contains(headTag));

        Node html = ElementUtils.query(result.root(), "html");
        possible = result.getPossibleOpenTags(html);
        assertTrue(!possible.isEmpty());
        assertTrue(possible.contains(divTag));

    }

    public void testGetPossibleEndTagsInContext() throws ParseException {
        HtmlParseResult result = parse("<!DOCTYPE html><html><head><title>hello</title></head><body><div>ahoj</div></body></html>");

        assertNotNull(result.root());

        Node body = ElementUtils.query(result.root(), "html/body");
        Collection<HtmlTag> possible = result.getPossibleCloseTags(body).keySet();

        assertFalse(possible.isEmpty());

        HtmlTag htmlTag = new HtmlTagImpl("html");
        HtmlTag headTag = new HtmlTagImpl("head");
        HtmlTag bodyTag = new HtmlTagImpl("body");
        HtmlTag divTag = new HtmlTagImpl("div");

        Iterator<HtmlTag> possibleItr = possible.iterator();
        assertEquals(bodyTag, possibleItr.next());
        assertEquals(htmlTag, possibleItr.next());

        assertFalse(possible.contains(divTag));

        Node head = ElementUtils.query(result.root(), "html/head");
        possible = result.getPossibleOpenTags(head);

        assertTrue(!possible.isEmpty());

        HtmlTag titleTag = new HtmlTagImpl("title");
        assertTrue(possible.contains(titleTag));
        assertFalse(possible.contains(headTag));

    }

    public void testAllowDialogInDiv() throws ParseException {
        HtmlParseResult result = parse("<!doctype html>"
                + "<html>\n"
                + "<title>title</title>\n"
                + "<body>\n"
                + "<div>\n"
                + "</div>\n"
                + "</body>\n"
                + "</html>\n");

        assertNotNull(result.root());

        Node div = ElementUtils.query(result.root(), "html/body/div");
        Collection<HtmlTag> possible = result.getPossibleOpenTags(div);

        assertTrue(!possible.isEmpty());

        HtmlTag divTag = new HtmlTagImpl("div");
        HtmlTag dialogTag = new HtmlTagImpl("dialog");

        assertTrue(possible.contains(divTag));

        //fails - bug
//        assertFalse(possible.contains(dialogTag));

    }

    public void testParseUnfinishedTagFollowedByChars() throws ParseException {
        String code = "<!doctype html> \n"
                + "<html>    \n"
                + "<title>dd</title>\n"
                + "<b\n" //the tag is unfinished during typing
                + "      a\n" //this text is considered as the tag's attribute (correctly)
                + "</body>\n"
                + "</html> ";

        HtmlParseResult result = parse(code);
        Node root = result.root();

        assertNotNull(root);

//        NodeUtils.dumpTree(root);
    }

    public void testParseNotMatchingBodyTags() throws ParseException {
        String code = "<!doctype html>\n"
                + "<html>\n"
                + "<title></title>\n"
                + "<body>\n"
                + "</body>\n"
                + "</html>\n";

        HtmlParseResult result = parse(code);
        Node root = result.root();

        assertNotNull(root);

//        NodeUtils.dumpTree(root);
    }

    public void testParseFileLongerThan2048chars() throws ParseException {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < 2048 * 3; i++) {
            b.append('*');
        }

        String code = "<!doctype html>\n"
                + "<html>\n"
                + "<title></title>\n"
                + "<body>\n"
                + b.toString()
                + "</body>\n"
                + "</html>\n";


//        ParseTreeBuilder.setLoggerLevel(Level.ALL);
        HtmlParseResult result = parse(code);
        Node root = result.root();

        assertNotNull(root);
//        ElementUtils.dumpTree(root);

        OpenTag body = ElementUtils.query(result.root(), "html/body");
        assertNotNull(body);

        CloseTag bodyEnd = body.matchingCloseTag();
        assertNotNull(bodyEnd);

        assertEquals(6190, bodyEnd.from());
        assertEquals(6197, bodyEnd.to());

    }

    public void test_A_TagProblem() throws ParseException {
        //
        //java.lang.AssertionError
        //at org.netbeans.modules.html.parser.NodeTreeBuilder.elementPopped(NodeTreeBuilder.java:106)
        //

        //such broken source really confuses the html parser
        String code = "<!DOCTYPE html>\n"
                + "<html>\n"
                + "     <title>HTML5 Demo: geolocation</title>\n"
                + "     <body>\n"
                + "         <a>  \n"
                + "         <footer>\n"
                + "             <a>\n"
                + "         </footer> \n"
                + "     </body>\n" //from some reason the tree builder pushes <a> open tag node here?!?!?
                + "</html>  \n";

        HtmlParseResult result = parse(code, false);
        Node root = result.root();

        assertNotNull(root);

//        NodeUtils.dumpTree(root);
    }

    public void testParseTreeAfterOpenedTag() throws ParseException {
        String code = "<!DOCTYPE html>\n"
                + "<html>\n"
                + "<title>HTML5 Demo: geolocation</title>\n"
                + "<body>\n"
                + "<div></\n"
                + "</body>\n"
                + "</html>\n";

        HtmlParseResult result = parse(code);
        Node root = result.root();

        assertNotNull(root);

//        NodeUtils.dumpTree(root);
    }

    public void testBodyTagHasNoParent() throws ParseException {
        String code = "<!doctype html> "
                + "<html> "
                + "<body >"
                + "      "
                + "<       "
                + "</body>"
                + "</html>";

        HtmlParseResult result = parse(code);
        Node root = result.root();

        assertNotNull(root);

        Node body = ElementUtils.query(result.root(), "html/body");
        assertNotNull(body);

        assertNotNull(body.parent());

//        NodeUtils.dumpTree(root);
    }

    public void testUnclosedTitleTag() throws ParseException {
        //this is causing all the nodes missing their logical ranges
        //the body tag is parsed as virtual only regardless the open and end
        //tag is present in the code.

        //looks like caused by the CDATA content of the unclosed title tag
        //which causes the parser consume everything after as unparseable code :-(
        String code = "<!doctype html><html><head><title></head><body></body></html>";
        //             0123456789012345678901234567890123456789012345678901234567890123456789
        //             0         1         2         3         4         5         6

        HtmlParseResult result = parse(code);
        Node root = result.root();

        assertNotNull(root);

        Node title = ElementUtils.query(result.root(), "html/head/title");
        assertNotNull(title);

//        NodeUtils.dumpTree(root);

        //FAILING - http://netbeans.org/bugzilla/show_bug.cgi?id=190183

//        assertTrue(title.semanticEnd() != -1);

    }

    public void testOnlyDivInFile() throws ParseException {
        String code = "<!doctype html><html><head><title>x</title></head><body><di </body></html>";
        HtmlParseResult result = parse(code);
        Node root = result.root();

        assertNotNull(root);

//        NodeUtils.dumpTree(root);
    }

    public void testParseFileTest1() throws ParseException {
        parse(getTestFile("testfiles/test1.html"));
    }

    public void testParseFileTest2() throws ParseException {
        parse(getTestFile("testfiles/test2.html"));
    }

    public void testParseFileTest3() throws ParseException {
        parse(getTestFile("testfiles/test3.html"));
    }

    public void testParseFileTest4() throws ParseException {
        parse(getTestFile("testfiles/test4.html"));
    }

    public void testParseFileTest5() throws ParseException {
        parse(getTestFile("testfiles/test5.html"));
    }

    //Bug 211776 - Self-closing element breaks code folding
    public void testIssue211776() throws ParseException {
//        ParseTreeBuilder.setLoggerLevel(Level.ALL);

        HtmlParseResult result = parse(getTestFile("testfiles/test6.html"));
        Node root = result.root();
//        ElementUtils.dumpTree(root);

        OpenTag body = ElementUtils.query(root, "html/body");
        assertNotNull(body);
        assertFalse(body.isEmpty());

        OpenTag link = ElementUtils.query(root, "html/head/link");
        assertNotNull(link);
        assertTrue(link.isEmpty());

        OpenTag div = ElementUtils.query(root, "html/body/div");
        assertNotNull(div);
        assertFalse(div.isEmpty());

    }

    public void testAddChildToEmptyTag() throws ParseException {
//        ParseTreeBuilder.setLoggerLevel(Level.ALL);

        String code = "<div align='center'/><a>text</a></div>";
        HtmlParseResult result = parse(code);
        Node root = result.root();

    }

    //----------------------------------------------------------------
    protected FileObject getTestFile(String relFilePath) {
        File wholeInputFile = new File(getDataDir(), relFilePath);
        if (!wholeInputFile.exists()) {
            NbTestCase.fail("File " + wholeInputFile + " not found.");
        }
        FileObject fo = FileUtil.toFileObject(wholeInputFile);
        assertNotNull(fo);

        return fo;
    }

    public void testHtml5Model() throws ParseException {
        String code = "<!doctype html><title>hi</title>";
        HtmlParseResult result = parse(code);

        HtmlModel model = result.model();
        assertNotNull(model);
//        assertEquals("html5model", model.getModelId());

        Collection<HtmlTag> all = model.getAllTags();
        assertNotNull(all);
        assertEquals(ElementDescriptor.values().length, all.size());

        HtmlTag table = HtmlTagProvider.getTagForElement("table");
        assertNotNull(table);

        assertTrue(all.contains(table));

        //try to modify the unmodifiable collection
        try {
            all.remove(table);
            assertTrue("The tags collection can be modified!", false);
        } catch (UnsupportedOperationException t) {
            //ok
        }


    }

    //unknown node 't' has no parent set
    public void testParseUnknownElementInTable() throws ParseException {
        String code = "<!doctype html>"
                + "<html>"
                + "<head><title></title></head>"
                + "<body>"
                + "<table>"
                + "<t>"
                + "</table>"
                + "</body>"
                + "</html>";
//      NodeTreeBuilder.DEBUG = true;
        HtmlParseResult result = parse(code);
        Node root = result.root();

//      NodeUtils.dumpTree(root);

        //the 't' node is foster parented, so it goes to the table's parent, not table itself
        Node t = ElementUtils.query(root, "html/body/t");
        assertNotNull(t);
        Node body = ElementUtils.query(root, "html/body");
        assertNotNull(body);

        assertEquals(body, t.parent());

    }

    public void testDivLogicalEndAtTheEOF() throws ParseException {
        String code = "<!doctype html><div><div></div>";
        //             0123456789012345678901234567890123456789
        //                                           ^
//        NodeTreeBuilder.DEBUG = true;
        HtmlParseResult result = parse(code);
        Node root = result.root();

//        NodeUtils.dumpTree(root);

        //the 't' node is foster parented, so it goes to the table's parent, not table itself
        OpenTag div = ElementUtils.query(root, "html/body/div");
        assertNotNull(div);

        assertEquals(30, div.semanticEnd());

        code = "<!doctype html><div><div></</div>";
        //      0123456789012345678901234567890123456789
        //                                      ^
//        NodeTreeBuilder.DEBUG = true;
        result = parse(code);
        root = result.root();

//        NodeUtils.dumpTree(root);

        //the 't' node is foster parented, so it goes to the table's parent, not table itself
        div = ElementUtils.query(root, "html/body/div");
        assertNotNull(div);

        assertEquals(32, div.semanticEnd());

        code = "<!doctype html><div></";
        //        0123456789012345678901234567890123456789
        //                             ^
//        NodeTreeBuilder.DEBUG = true;
        result = parse(code);
        root = result.root();

//        NodeUtils.dumpTree(root);

        //the 't' node is foster parented, so it goes to the table's parent, not table itself
        div = ElementUtils.query(root, "html/body/div");
        assertNotNull(div);

        assertEquals(21, div.semanticEnd());

    }

    //Bug 191873 - IllegalStateException: Stack's top root:ROOT(0-186){} is not the same as <*head>(34-40/47){}
    public void testNodePopWithoutPush() throws ParseException {
        String code = "<!doctype html>"
                + "<head></head>"
                + "<meta charset=\"utf-8\" />";

//        NodeTreeBuilder.setLoggerLevel(Level.FINER);
        HtmlParseResult result = parse(code);
        Node root = result.root();

        assertNotNull(root);
//        NodeUtils.dumpTree(root);

    }

//    //Bug 193268 - AssertionError: Unexpected node type ENDTAG
//    public void testScriptTagInBody() throws ParseException {
//        String scriptOpenTag = "<script type=\"text/javascript\" src=\"test.js\">";
//        //                   0123456789012 3456789012345678 901234 56789012 345678901234567890123456789
//        //                   0         1          2          3          4          5
//        String code = "<!doctype html>"
//                + "<html>"
//                + "<head>"
//                + "<title></title>"
//                + "</head>"
//                + "<body>"
//                + "<canvas>"
//                + "<a/>"
//                + "</canvas>"
//                + scriptOpenTag + "</script>"
//                + "</body>"
//                + "</html>";
//
////        ParseTreeBuilder.setLoggerLevel(Level.FINER);
//        HtmlParseResult result = parse(code);
//        Node root = result.root();
//
//        assertNotNull(root);
//        ElementUtils.dumpTree(root);
//
//        OpenTag title = ElementUtils.query(root, "html/head/title");
//        assertNotNull(title);
//        assertNotNull(title.matchingCloseTag());
//        
//        OpenTag a = ElementUtils.query(root, "html/body/canvas/a");
//        assertNotNull(a);
//        assertTrue(a.isEmpty());
//        
//        OpenTag scriptOpen = ElementUtils.query(root, "html/body/script");
//        assertNotNull(scriptOpen);
//
//        assertEquals(76, scriptOpen.from());
//        assertEquals(76 + 45, scriptOpen.to());
//
//        CloseTag scriptEnd = scriptOpen.matchingCloseTag();
//        assertNotNull(scriptEnd);
//        
//        assertEquals(121, scriptEnd.from());
//        assertEquals(130, scriptEnd.to());
//
//    }
    //[Bug 195103] Refactoring changes a changed filename incorrectly in the html <script> tag
    public void testIsAttributeQuoted() throws ParseException {
        String code = "<!doctype html>"
                + "<html>"
                + "<head>"
                + "<title></title>"
                + "</head>"
                + "<body>"
                + "<div onclick=\"alert()\">x</div>"
                + "<p onclick='alert()'>x</p>"
                + "<a onclick=alert>x</a>"
                + "</body>"
                + "</html>";

        HtmlParseResult result = parse(code);
        Node root = result.root();

        assertNotNull(root);
//        NodeUtils.dumpTree(root);

        OpenTag div = ElementUtils.query(root, "html/body/div");
        assertNotNull(div);

        Attribute attr = div.getAttribute("onclick");
        assertNotNull(attr);
        assertTrue(attr.isValueQuoted());

        OpenTag p = ElementUtils.query(root, "html/body/p");
        assertNotNull(p);

        attr = p.getAttribute("onclick");
        assertNotNull(attr);
        assertTrue(attr.isValueQuoted());

        OpenTag a = ElementUtils.query(root, "html/body/a");
        assertNotNull(a);

        attr = a.getAttribute("onclick");
        assertNotNull(attr);
        assertFalse(attr.isValueQuoted());

    }

    //Bug 196479 - Problem with finding end tag for style element
    public void testStyleTag() throws ParseException {
//        NodeTreeBuilder.setLoggerLevel(Level.FINER);

        String code = "<!doctype html>"
                + "<html>"
                + "<head>"
                + "<title></title>"
                + "<style> div { } </style>"
                // 2345678901234        890123456
                + "</head>"
                + "<body>"
                + "</body>"
                + "</html>";

        HtmlParseResult result = parse(code);
        Node root = result.root();

        assertNotNull(root);
//        ElementUtils.dumpTree(root);

        Node style = ElementUtils.query(root, "html/head/style");
        assertNotNull(style);

        assertEquals(42, style.from());
        assertEquals(49, style.to());

        //space after the style tag name
        code = "<!doctype html>"
                + "<html>"
                + "<head>"
                + "<title></title>"
                + "<style  > div { } </style>"
                // 2345678901234        890123456
                + "</head>"
                + "<body>"
                + "</body>"
                + "</html>";

        result = parse(code);
        root = result.root();

        assertNotNull(root);
//        ElementUtils.dumpTree(root);

        style = ElementUtils.query(root, "html/head/style");
        assertNotNull(style);

        assertEquals(42, style.from());
        assertEquals(51, style.to());

    }

    //Bug 197608 - Non-html tags offered as closing tags using code completion
    public void testIssue197608() throws ParseException {
//        NodeTreeBuilder.setLoggerLevel(Level.FINER);

        String code = "<div></di   <p> aaa";
        //             0123456789012345

        HtmlParseResult result = parse(code);
        Node root = result.root();

        assertNotNull(root);
//        ElementUtils.dumpTree(root);

        Collection<ProblemDescription> problems = result.getProblems();
        assertNotNull(problems);
        assertEquals(2, problems.size());

        Iterator<ProblemDescription> problemsItr = problems.iterator();
        ProblemDescription pd = problemsItr.next();
        assertNotNull(pd.getKey());
        assertNotNull(pd.getText());
        assertEquals(ProblemDescription.ERROR, pd.getType());
        assertEquals(9, pd.getFrom());
        assertEquals(9, pd.getTo());

        pd = problemsItr.next();
        assertNotNull(pd.getKey());
        assertNotNull(pd.getText());
        assertEquals(ProblemDescription.ERROR, pd.getType());
        assertEquals(12, pd.getFrom());
        assertEquals(12, pd.getTo());

    }

    public void testHtmlModelEntries() {
    }

    public void testDuplicatedEntriesInHtmlModel() {
        HtmlModel model = HtmlModelFactory.getModel(HtmlVersion.HTML5);
        assertNotNull(model);

        Collection<HtmlTag> tags = model.getAllTags();

        Collection<HtmlTag> names = new HashSet<HtmlTag>();
        StringBuilder sb = new StringBuilder();
        for (HtmlTag t : tags) {
            if (!names.add(t)) {
                sb.append(t.toString());
                sb.append("(");
                sb.append(t.hashCode());
                sb.append(")");
                sb.append(", ");
            }
        }
        assertTrue("found duplicated entry/ies: " + sb.toString(), sb.length() == 0);
    }

    public void testParseTagAttributeWithoutValue() throws ParseException {
        String code = "<!doctype html><body><div align/></body>";
        HtmlParseResult result = parse(code);
        Node root = result.root();
        assertNotNull(root);

//        ElementUtils.dumpTree(root);

        OpenTag div = ElementUtils.query(root, "html/body/div");
        assertNotNull(div);

        Attribute attr = div.getAttribute("align");
        assertNotNull(attr);

        assertNull(attr.value());
        assertNull(attr.unquotedValue());

    }

    //Bug 210976 - StringIndexOutOfBoundsException: String index out of range: 399
    public void testIssue210976() throws ParseException {
        String code = "<a href=\"@&msc=@@@&klub=@@@\"></a>";
        //             01234567 8901234567890123456 7890123
        //             0          1         2          3    

        HtmlParseResult result = parse(code);
        Node root = result.root();
        assertNotNull(root);

//        ElementUtils.dumpTree(root);

        OpenTag a = ElementUtils.query(root, "html/body/a");
        assertNotNull(a);
        assertEquals(0, a.from());
        assertEquals(29, a.to());

        Attribute attr = a.getAttribute("href");
        assertNotNull(attr);

        assertNotNull(attr.value());
        assertNotNull(attr.unquotedValue());

        assertEquals("\"@&msc=@@@&klub=@@@\"", attr.value());
        assertEquals("@&msc=@@@&klub=@@@", attr.unquotedValue());

    }

    public void testSelfCloseTagEndOffset() throws ParseException {
//        ParseTreeBuilder.setLoggerLevel(Level.ALL);

        String code = "<div/>text";
        //             0123456
        HtmlParseResult result = parse(code);
        Node root = result.root();

        OpenTag div = ElementUtils.query(root, "html/body/div");
        assertNotNull(div);

        assertEquals(0, div.from());
        assertEquals(6, div.to());
    }

    public void testParseNetbeans_org() throws ParseException {
        parse(getTestFile("testfiles/netbeans_org.html"));
    }

    public void testParseW3c_org() throws ParseException {
        parse(getTestFile("testfiles/w3c_org.html"));
    }

    public void testParseWikipedia_org() throws ParseException {
        parse(getTestFile("testfiles/wikipedia_org.html"));
    }

    //Bug 213332 - IllegalStateException: A bug #212445 just happended for source text " scrollbar-arrow-color:"black"; } </STYLE> <TITLE>Cyprus :: Larnaca</TITLE></HEAD> <BOD". Please report a new bug or r 
    //http://netbeans.org/bugzilla/show_bug.cgi?id=213332
    public void testIssue213332() throws ParseException {
//        ParseTreeBuilder.setLoggerLevel(Level.ALL);

        String code = "<html><head><style type=text/css></style></head></html>";
        //             012345678901234567890123456789012345678901234567890123456789
        //             0         1         2         3         4         5
        Node root = parse(code).root();

//        ElementUtils.dumpTree(root);

        OpenTag styleOpen = ElementUtils.query(root, "html/head/style");
        assertNotNull(styleOpen);

        CloseTag styleClose = styleOpen.matchingCloseTag();
        assertNotNull(styleClose);

        assertEquals(33, styleClose.from());
        assertEquals(41, styleClose.to());

        assertEquals(12, styleOpen.from());
        assertEquals(33, styleOpen.to());

    }

    public void testTextNodes() throws ParseException {
//        ParseTreeBuilder.setLoggerLevel(Level.ALL);
        String code = "<html>\n"
                + "<head>\n"
                + "<title>hello</title>\n"
                + "</head>\n"
                + "<body>\n"
                + "<div>text1</div>\n"
                + "<p>text2\n"
                + "<p>text3\n"
                + "</body>\n"
                + "</html>\n";
        //             0123456789012

        HtmlSource source = new HtmlSource(code);
        InstanceContent ic = new InstanceContent();
        Properties props = new Properties();
        props.setProperty("add_text_nodes", "true");
        ic.add(props);
        Lookup l = new AbstractLookup(ic);
        HtmlParseResult result = new Html5Parser().parse(source, HtmlVersion.HTML5, l);

        Node root = result.root();
//        ElementUtils.dumpTree(root);

        Node title = ElementUtils.query(root, "html/head/title");
        Collection<Element> elements = title.children(ElementType.TEXT);
        assertNotNull(elements);
        assertEquals(1, elements.size());

        Element text = elements.iterator().next();
        assertNotNull(text);
        assertEquals(ElementType.TEXT, text.type());
        assertEquals("hello", text.image().toString());
    }

    public void testNoTextNodesByDefault() throws ParseException {
//        ParseTreeBuilder.setLoggerLevel(Level.ALL);
        String code = "<html>\n"
                + "<head>\n"
                + "<title>hello</title>\n"
                + "</head>\n"
                + "<body>\n"
                + "<div>text1</div>\n"
                + "<p>text2\n"
                + "<p>text3\n"
                + "</body>\n"
                + "</html>\n";
        //             0123456789012
        Node root = parse(code).root();
//        ElementUtils.dumpTree(root);

        Node title = ElementUtils.query(root, "html/head/title");
        Collection<Element> elements = title.children(ElementType.TEXT);
        assertNotNull(elements);
        assertEquals(0, elements.size());
    }

    //Bug 211792 
    //http://netbeans.org/bugzilla/show_bug.cgi?id=211792
    public void testIssue211792() throws ParseException {
//        ParseTreeBuilder.setLoggerLevel(Level.ALL);
        String code = "<a href=\"\"</p>";
        //             01234567 8 901234
        //             0         1         
        Node root = parse(code).root();
//        ElementUtils.dumpTree(root);
        OpenTag a = ElementUtils.query(root, "html/body/a");
        assertNotNull(a);

        Collection<Attribute> attrs = a.attributes();
        assertNotNull(attrs);

//        for(Attribute attr : attrs) {
//            System.out.println("from:" + attr.from());
//            System.out.println("to:" + attr.to());
//            System.out.println("nameoffset:" + attr.nameOffset());
//            System.out.println("valueoffset:" + attr.valueOffset());
//            System.out.println("name:"+attr.name());
//            System.out.println("value:"+attr.value());
//        }

        //properly the number of the attributes should be one, but the 
        //html parser itself serves the three attributes, where first one
        //is correct, second one is completely
        //out of the source are and a third one which represents
        //the close tag after the quotes.
        assertEquals(2, attrs.size());

        Attribute href = attrs.iterator().next();
        assertNotNull(href);

        assertEquals(3, href.from());
        assertEquals(10, href.to());
        assertEquals(3, href.nameOffset());
        assertEquals(8, href.valueOffset());
        assertEquals("href", href.name().toString());
        assertEquals("\"\"", href.value().toString());
        assertEquals("", href.unquotedValue().toString());

    }

    //fails
//     //Bug 194037 - AssertionError at nu.validator.htmlparser.impl.TreeBuilder.endTag
//    public void testIssue194037() throws ParseException {
//        String code = "<FRAMESET></FRAMESET></HTML><FRAMESET></FRAMESET>";
//
//        NodeTreeBuilder.setLoggerLevel(Level.FINER);
//        HtmlParseResult result = parse(code);
//        Node root = result.root();
//
//        assertNotNull(root);
//        NodeUtils.dumpTree(root);
//
//    }
//    public void test_parse_safari_css_properties_spec() throws ParseException {
//        //source:
//        //http://developer.apple.com/library/safari/#documentation/appleapplications/reference/SafariCSSRef/Articles/StandardCSSProperties.html#//apple_ref/doc/uid/%28null%29-SW1
//        File file = new File("/Users/marekfukala/parse.html");
//        HtmlSource source = new HtmlSource(FileUtil.toFileObject(file));
//        final CharSequence code = source.getSourceCode();
//        HtmlParseResult result = SyntaxAnalyzer.create(source).analyze().parseHtml();
//
//        Node root = result.root();
//
//        NodeVisitor v = new NodeVisitor() {
//
//            public void visit(Node node) {
//                if (node.name().contains("section")) {
//                    Node h3 = NodeUtils.query(node, "h3");
//                    if (h3 == null) {
//                        return;
//                    }
//                    Node h3end = h3.matchingTag();
//                    System.out.print(code.subSequence(h3.to(), h3end.from()));
//                } else if (node.name().equals("dl")) {
//                    Node.Attribute clazzattr = node.getAttribute("class");
//                    if (clazzattr != null && "content_text".equals(clazzattr.unquotedValue())) {
//                        Node strong = NodeUtils.query(node, "b/strong");
//                        if(strong != null && "Support Level".contains(getTextContent(code, strong))) {
//                            Node p = NodeUtils.query(node, "dd/p");
//                            if(p != null) {
//                                System.out.println("=" + getTextContent(code, p));
//                            }
//                        }
//                    }
//                }
//            }
//        };
//                
//        NodeUtils.visitChildren(root, v);
//
//    }
//    
//    private CharSequence getTextContent(CharSequence source, Node openTag) {
//        Node endTag = openTag.matchingTag();
//        if(endTag != null) {
//            return source.subSequence(openTag.to(), endTag.from());
//        }
//        
//        return null;
//    }
    //Bug 218027 - case sensitive for HTML tags
    public void testIssue218027() throws ParseException {
//        ParseTreeBuilder.setLoggerLevel(Level.ALL);
        String code = "<html><body><TABLE></table></body></html>";
        //             01234567 8 901234
        //             0         1         
        Node root = parse(code).root();
//        ElementUtils.dumpTree(root);
        OpenTag a = ElementUtils.query(root, "html/body/TABLE");
        assertNotNull(a);
        assertNotNull(a.matchingCloseTag());
    }

    //Bug 218629 - Wrong syntax coloring in HTML for non-english text 
    public void testIssue218629() throws ParseException {
//        ParseTreeBuilder.setLoggerLevel(Level.ALL);
        String code = "<div>שלום עולם</div>";
        //             012345678901234567890
        //             0         1         
        Node root = parse(code).root();
        ElementUtils.dumpTree(root);
        OpenTag a = ElementUtils.query(root, "html/body/div");
        assertNotNull(a);
        assertNotNull(a.matchingCloseTag());
    }

    public void testIssue227870() throws ParseException {
//         ParseTreeBuilder.setLoggerLevel(Level.ALL);
        String code = "<input \n"
                + "x\n"
                + "onclick=\"hideDivLogin(document.getElementById('div_login'), document.getElementById('div_password'), null, document.getElementById('password'), '@@@', null);\" \n"
                + "s=\"margin-right: 5px; float: left\" />\n";

        Node root = parse(code).root();
        OpenTag input = (OpenTag)ElementUtils.query(root, "html/body/input");
        assertNotNull(input);
        
        Iterator<Attribute> attrs = input.attributes().iterator();
        assertTrue(attrs.hasNext());
        Attribute a = attrs.next();
        assertNotNull(a);
        assertEquals(8, a.nameOffset());
        assertEquals("x", a.name().toString());
        
        assertTrue(attrs.hasNext());
        a = attrs.next();
        assertNotNull(a);
        assertEquals(10, a.nameOffset());
        assertEquals("onclick", a.name().toString());
        
        assertTrue(attrs.hasNext());
        a = attrs.next();
        assertNotNull(a);
        assertEquals(170, a.nameOffset());
        assertEquals("s", a.name().toString());
        
        assertFalse(attrs.hasNext());
        
    }

    private void assertNodeOffsets(final Node root) {
        //assert semantic ends are set
        ElementVisitor check = new ElementVisitor() {
            @Override
            public void visit(Element node) {
                OpenTag openTag = (OpenTag) node;

                if (openTag.from() == -1 && openTag.to() == -1) {
                    return; //virtual 
                }
                try {
                    assertTrue(String.format("Incorrect start offset %s of element %s", openTag.from(), openTag.name()),
                            openTag.from() >= 0);

                    assertTrue(String.format("Incorrect end offset %s ( < start %s ) of element %s",
                            openTag.to(), openTag.from(), openTag.name()),
                            openTag.to() > openTag.from());

                    assertTrue(String.format("Incorrect semantic end %s, from=%s, to=%s of element %s",
                            openTag.semanticEnd(), openTag.from(), openTag.to(), openTag.name()),
                            openTag.semanticEnd() > openTag.from());

                } catch (AssertionError ae) {
                    ElementUtils.dumpTree(root);
                    throw ae;
                }

                //validate attributes
                for (Attribute a : openTag.attributes()) {
                    assertNotNull(a);
                    assertNotNull(a.name());
                    assertTrue(a.nameOffset() > openTag.from());

                    if (a.value() != null) {
                        assertTrue(a.valueOffset() > a.nameOffset() + a.name().length());

                        if (!(a.valueOffset() < root.to())) {
                            System.out.println("error");
                        }
                        assertTrue(a.valueOffset() < root.to());
                    }
                }

            }
        };

        ElementUtils.visitChildren(root, check, ElementType.OPEN_TAG);

    }

    private HtmlParseResult parse(FileObject file) throws ParseException {
        HtmlSource source = new HtmlSource(file);
        HtmlParseResult result = SyntaxAnalyzer.create(source).analyze().parseHtml();

        assertNotNull(result);

        assertNodeOffsets(result.root());

        return result;
    }

    private HtmlParseResult parse(CharSequence code) throws ParseException {
        return parse(code, true);
    }

    private HtmlParseResult parse(CharSequence code, boolean validateNodes) throws ParseException {
        HtmlSource source = new HtmlSource(code);
        final HtmlParseResult result = SyntaxAnalyzer.create(source).analyze().parseHtml();

        assertNotNull(result);

        if (validateNodes) {
            assertNodeOffsets(result.root());
        }

        return result;
    }

    private static class HtmlTagImpl implements HtmlTag {

        private String name;

        public HtmlTagImpl(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 61 * hash + (this.getName() != null ? this.getName().hashCode() : 0);
            hash = 61 * hash + (this.getTagClass() != null ? this.getTagClass().hashCode() : 0);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (!(obj instanceof HtmlTag)) {
                return false;
            }
            HtmlTag other = (HtmlTag) obj;

            if ((this.getName() == null) ? (other.getName() != null) : !this.getName().equals(other.getName())) {
                return false;
            }
            if (this.getTagClass() != other.getTagClass()) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return "HtmlTagImpl{" + "name=" + name + '}';
        }

        @Override
        public Collection<HtmlTagAttribute> getAttributes() {
            return Collections.emptyList();
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public boolean hasOptionalOpenTag() {
            return false;
        }

        @Override
        public boolean hasOptionalEndTag() {
            return false;
        }

        @Override
        public HtmlTagAttribute getAttribute(String name) {
            return null;
        }

        @Override
        public HtmlTagType getTagClass() {
            return HtmlTagType.HTML;
        }

        @Override
        public Collection<HtmlTag> getChildren() {
            return Collections.emptyList();
        }

        @Override
        public HelpItem getHelp() {
            return null;
        }
    }
}
