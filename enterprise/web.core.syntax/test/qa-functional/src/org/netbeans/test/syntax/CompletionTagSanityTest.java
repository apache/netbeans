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
package org.netbeans.test.syntax;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.modules.editor.CompletionJListOperator;
import org.netbeans.junit.NbModuleSuite;

/**
 *
 * @author Vladimir Riha
 */
public class CompletionTagSanityTest extends GeneralJSP {

    public CompletionTagSanityTest(String arg0) {
        super(arg0);
    }

    public static Test suite() {
        NbModuleSuite.Configuration conf = NbModuleSuite.createConfiguration(CompletionTagSanityTest.class);
        addServerTests(Server.GLASSFISH, conf, new String[0]);//register server
        return NbModuleSuite.create(conf.addTest(
                "openProject",
                "testDirectivesTag",
                "testAttributesTag",
                "testValuesTag",
                "testEntitiesTag",
                "testEncodingTag",
                "testImportTag",
                "testIncludeDirectiveTag",
                "testIncludeTag",
                "testScriptletsTag",
                "testHTMLTagsTag",
                "testJSPTagsTag",
                "testImplicitObjectsTag",
                "testJavaBeansTag",
                "testDeclarationsTag",
                "testExpressionsTag",
                "testLibraryTagsTag"
        ).enableModules(".*").clusters(".*").honorAutoloadEager(true));
    }

    public void openProject() throws Exception {
        startTest();
        CompletionTagSanityTest.current_project = "sampleJSP";
        openProject(CompletionTagSanityTest.current_project);
        resolveServer(CompletionTagSanityTest.current_project);
        openFile("WEB-INF|tags|test.tag", CompletionTagSanityTest.current_project);
        EditorOperator eo = new EditorOperator("test.tag");
        CompletionTagSanityTest.original_content = eo.getText();
        endTest();
    }

    public void testDirectivesTag() throws Exception {
        startTest();
        doTest(new EditorOperator("test.tag"), 6, 1, "<%@", 0, new String[]{"include", "attribute", "taglib", "variable"});
        endTest();
    }

    public void testAttributesTag() throws Exception {
        startTest();
        doTest(new EditorOperator("test.tag"), 6, 1, "<%@tag ", 0, new String[]{"body-content", "description", "display-name", "dynamic-attributes", "example", "import", "isELIgnored", "large-icon", "language", "pageEncoding", "small-icon"});
        endTest();
    }

    public void testValuesTag() throws Exception {
        startTest();
        doTest(new EditorOperator("test.tag"), 6, 1, "<%@tag isELIgnored=\"\"", 1, new String[]{"true", "false"});
        endTest();
    }

    public void testEntitiesTag() throws Exception {
        startTest();
        doTest(new EditorOperator("test.tag"), 15, 9, "&", 0, new String[]{"AMP", "AElig"});
        endTest();
    }

    public void testCustomTag() throws Exception {
        startTest();
        doTest(new EditorOperator("test.tag"), 15, 9, "<%! void test(){} %>\n<div>Nothing here</div>\n<% t %>", 3, new String[]{"test"});
        endTest();
    }

    public void testEncodingTag() throws Exception {
        startTest();
        EditorOperator file = new EditorOperator("test.tag");
        doTest(file, 6, 1, "<%@tag pageEncoding=\"\"%>", 3, new String[]{"Big5", "IBM-Thai"});
        file.typeKey(' ', InputEvent.CTRL_MASK);
        evt.waitNoEvent(1000);
        file.pressKey(KeyEvent.VK_ENTER);
        assertEquals("Incorrect encoding completion", "<%@tag pageEncoding=\"Big5\"%>", file.getText(file.getLineNumber()).trim());
        endTest();
    }

    public void testIncludeTag() throws Exception {
        startTest();
        EditorOperator file = new EditorOperator("test.tag");
        file.setCaretPosition(15, 9);
        file.insert("<jsp:include page=\"\"");
        pressKey(file, KeyEvent.VK_LEFT, 1);
        evt.waitNoEvent(1000);
        file.typeKey(' ', InputEvent.CTRL_MASK);

        CompletionInfo completion = getCompletion();
        CompletionJListOperator cjo = completion.listItself;
        completion.listItself.hideAll();
        checkCompletionItemsJsp(cjo, new String[]{"../", "newtag_file.tag", "test.tag"});

        type(file, "../");

        file.typeKey(' ', InputEvent.CTRL_MASK);
        completion = getCompletion();
        cjo = completion.listItself;
        completion.listItself.hideAll();
        checkCompletionItemsJsp(cjo, new String[]{"../", "tlds/"});
        type(file, "../tes");
        file.pressKey(KeyEvent.VK_ESCAPE);
        file.typeKey(' ', InputEvent.CTRL_MASK);
        evt.waitNoEvent(1000);
        file.typeKey(' ', InputEvent.CTRL_MASK);
        evt.waitNoEvent(1000);
        assertEquals("Incorrectly completed text", "<jsp:include page=\"../../test.jsp\"", file.getText(file.getLineNumber()).trim());

        endTest();
    }

    public void testImportTag() throws Exception {
        startTest();
        EditorOperator file = new EditorOperator("test.tag");
        file.setCaretPosition(6, 1);
        file.insert("<%@tag import=\"j\" %>");
        pressKey(file, KeyEvent.VK_LEFT, 4);
        evt.waitNoEvent(1000);
        file.typeKey(' ', InputEvent.CTRL_MASK);

        CompletionInfo completion = getCompletion();
        CompletionJListOperator cjo = completion.listItself;
        completion.listItself.hideAll();
        checkCompletionItemsJsp(cjo, new String[]{"java", "javax"});

        type(file, "ava.");

        file.typeKey(' ', InputEvent.CTRL_MASK);
        completion = getCompletion();
        cjo = completion.listItself;
        completion.listItself.hideAll();
        checkCompletionItemsJsp(cjo, new String[]{"util", "text"});

        type(file, "util.Da");

        file.pressKey(KeyEvent.VK_ESCAPE);
        file.pressKey(KeyEvent.VK_ESCAPE);
        evt.waitNoEvent(3000);
        file.typeKey(' ', InputEvent.CTRL_MASK);
        evt.waitNoEvent(1000);
        file.pressKey(KeyEvent.VK_ENTER);
        assertEquals("Incorrectly completed text", "<%@tag import=\"java.util.Date\" %>", file.getText(file.getLineNumber()).trim());

        endTest();
    }

    public void testIncludeDirectiveTag() throws Exception {
        startTest();
        EditorOperator file = new EditorOperator("test.tag");
        file.setCaretPosition(6, 1);
        file.insert("<%@include file=\"\"%>");
        pressKey(file, KeyEvent.VK_LEFT, 3);
        evt.waitNoEvent(1000);
        file.typeKey(' ', InputEvent.CTRL_MASK);

        CompletionInfo completion = getCompletion();
        CompletionJListOperator cjo = completion.listItself;
        completion.listItself.hideAll();
        checkCompletionItemsJsp(cjo, new String[]{"../", "newtag_file.tag", "test.tag"});

        type(file, "../");

        file.typeKey(' ', InputEvent.CTRL_MASK);
        completion = getCompletion();
        cjo = completion.listItself;
        completion.listItself.hideAll();
        checkCompletionItemsJsp(cjo, new String[]{"../", "tlds/"});
        type(file, "../tes");
        file.pressKey(KeyEvent.VK_ESCAPE);
        file.typeKey(' ', InputEvent.CTRL_MASK);
        evt.waitNoEvent(1000);
        file.typeKey(' ', InputEvent.CTRL_MASK);
        evt.waitNoEvent(1000);
        assertEquals("Incorrect include", "<%@include file=\"../../test.jsp\"%>", file.getText(file.getLineNumber()).trim());

        endTest();
    }

    public void testScriptletsTag() throws Exception {
        startTest();
        EditorOperator file = new EditorOperator("test.tag");
        doTest(file, 15, 9, "<% String var=\"\"; var.", 0, new String[]{"public char charAt(int index)", "public boolean contains(CharSequence s)"});
        file.insert("charAt(1);%><!-- test -->");
        doTest(file, 15, 56, "<% var.", 0, new String[]{"public char charAt(int index)", "public boolean contains(CharSequence s)"});
        file.typeKey(' ', InputEvent.CTRL_MASK);
        evt.waitNoEvent(1000);
        file.pressKey(KeyEvent.VK_ENTER);
        file.setCaretPositionToEndOfLine(file.getLineNumber());
        type(file, ";%>");
        file.replace("index", "2");
        evt.waitNoEvent(200);
        assertTrue("Icorrectly completed scriptlet", file.getText(file.getLineNumber()).trim().contains("<% var.charAt(2)"));
        doTest(file, 16, 9, "<% \"test\".", 0, new String[]{"public char charAt(int index)", "public boolean contains(CharSequence s)"});
        file.insert("charAt(1);%>");
        doTest(file, 17, 9, "<% ", 0, new String[]{"ServletContext application", "ServletConfig config", "JspWriter out", "Object page", "PageContext pageContext", "HttpServletRequest request", "HttpServletResponse response", "HttpSession session"});
        endTest();
    }

    public void testHTMLTagsTag() throws Exception {
        startTest();
        EditorOperator file = new EditorOperator("test.tag");
        doTest(file, 15, 9, "<t", 0, new String[]{"table", "td", "tbody"});
        file.typeKey(' ', InputEvent.CTRL_MASK);
        evt.waitNoEvent(1000);
        file.pressKey(KeyEvent.VK_ENTER);
        assertEquals("HTML tag not completed properly", "<table", file.getText(file.getLineNumber()).trim());
        endTest();
    }

    public void testJSPTagsTag() throws Exception {
        startTest();
        EditorOperator file = new EditorOperator("test.tag");
        doTest(file, 15, 9, "<jsp:", 0, new String[]{"jsp:attribute", "jsp:body", "jsp:declaration", "jsp:directive.page", "jsp:directive.include", "jsp:element",
            "jsp:expression", "jsp:fallback", "jsp:forward", "jsp:getProperty", "jsp:include", "jsp:param", "jsp:params", "jsp:plugin", "jsp:setProperty",
            "jsp:text", "jsp:useBean","jsp:directive.tag","jsp:directive.attribute","jsp:doBody","jsp:invoke", "directive.variable"}, 30);
        file.typeKey(' ', InputEvent.CTRL_MASK);
        evt.waitNoEvent(1000);
        file.pressKey(KeyEvent.VK_ENTER);
        assertTrue("JSP tag not completed properly", file.getText(file.getLineNumber()).trim().equals("<jsp:attribute"));
        endTest();
    }

    public void testImplicitObjectsTag() throws Exception {
        startTest();
        EditorOperator file = new EditorOperator("test.tag");
        doTest(file, 15, 9, "<% response.", 0, new String[]{"public abstract void addCookie(Cookie cookie)", "public abstract void addHeader(String name, String value)"});
        file.typeKey(' ', InputEvent.CTRL_MASK);
        evt.waitNoEvent(1000);
        file.pressKey(KeyEvent.VK_ENTER);
        assertEquals("Implicit object not completed properly", "<% response.addCookie(cookie);", file.getText(file.getLineNumber()).trim());
        file.setCaretPositionToEndOfLine(file.getLineNumber());
        type(file, ";%>");
        doTest(file, 16, 9, "<% session.", 0, new String[]{"public abstract HttpSessionContext getSessionContext()"});
        endTest();
    }

    public void testJavaBeansTag() throws Exception {
        startTest();
        EditorOperator file = new EditorOperator("test.tag");
        doTest(file, 15, 9, "<jsp:useBean id=\"rDate\" scope=\"request\" class=\"j\" />", 4, new String[]{"java"});
        type(file, "ava.");

        file.typeKey(' ', InputEvent.CTRL_MASK);
        evt.waitNoEvent(1000);
        CompletionInfo completion = getCompletion();
        CompletionJListOperator cjo = completion.listItself;
        checkCompletionItemsJsp(cjo, new String[]{"util"});
        type(file, "util.");

        evt.waitNoEvent(1000);
        completion = getCompletion();
        cjo = completion.listItself;
        checkCompletionItemsJsp(cjo, new String[]{"Date"});
        type(file, "Date");
        file.save();
        evt.waitNoEvent(1000);

        file.setCaretPosition(16, 9);
        type(file, "<% r");
        file.save();
        evt.waitNoEvent(1000);
        file.pressKey(KeyEvent.VK_ESCAPE);

        file.typeKey(' ', InputEvent.CTRL_MASK);
        completion = getCompletion();
        cjo = completion.listItself;

        checkCompletionItemsJsp(cjo, new String[]{"Date rDate"}, 20);

        completion.listItself.hideAll();

        type(file, "Date.");
        evt.waitNoEvent(1000);

        completion = getCompletion();
        cjo = completion.listItself;
        checkCompletionItemsJsp(cjo, new String[]{"public boolean after(Date when)", "public boolean before(Date when)"});
        type(file, "getDate();%>");
        doTest(file, 17, 9, "${r", 0, new String[]{"rDate"});

        type(file, "Date.");
        completion = getCompletion();
        cjo = completion.listItself;
        checkCompletionItemsJsp(cjo, new String[]{"after", "before"});

        endTest();
    }

    public void testDeclarationsTag() throws Exception {
        startTest();
        EditorOperator file = new EditorOperator("test.tag");
        doTest(file, 15, 9, "<%! ", 0, new String[]{"abstract", "boolean"});
        type(file, "void hello() { this.");

        file.typeKey(' ', InputEvent.CTRL_MASK);
        evt.waitNoEvent(1000);
        CompletionInfo completion = getCompletion();
        CompletionJListOperator cjo = completion.listItself;
        checkCompletionItemsJsp(cjo, new String[]{"public boolean equals(Object obj)", "public void destroy()"});

        endTest();
    }

    public void testExpressionsTag() throws Exception {
        startTest();
        EditorOperator file = new EditorOperator("test.tag");
        doTest(file, 15, 9, "<%= re", 0, new String[]{"HttpServletRequest request", "HttpServletResponse response"});
        type(file, "quest.");
        evt.waitNoEvent(1000);

        file.typeKey(' ', InputEvent.CTRL_MASK);
        CompletionInfo completion = getCompletion();
        CompletionJListOperator cjo = completion.listItself;
        checkCompletionItemsJsp(cjo, new String[]{"public abstract String getHeader(String name)", "public abstract String getAuthType()"});
        completion.listItself.hideAll();
        endTest();
    }

    public void testLibraryTagsTag() throws Exception {
        startTest();
        EditorOperator file = new EditorOperator("test.tag");
        doTest(file, 6, 1, "<%@taglib prefix=\"ax\" uri=\"", 0, new String[]{"/WEB-INF/tlds/lib", "/WEB-INF/tlds/lib.tld", "http://java.sun.com/jsp/jstl/core"});
        file.insert("/WEB-INF/tlds/lib\"");
        file.setCaretPositionToEndOfLine(file.getLineNumber());
        file.insert("%>");
        evt.waitNoEvent(1000);
        doTest(file, 16, 9, "<", 0, new String[]{"ax:newtag_file"}, 60);

        endTest();
    }

    public void doTest(EditorOperator file, int line, int column, String text, int moveLeft, String[] result) throws Exception {
        doTest(file, line, column, text, moveLeft, result, 20);
    }

    public void doTest(EditorOperator file, int line, int column, String text, int moveLeft, String[] result, int limit) throws Exception {
        file.setCaretPosition(line, column);
        type(file, text);
        evt.waitNoEvent(1000);
        file.pressKey(KeyEvent.VK_ESCAPE);

        for (int i = 0; i < moveLeft; i++) {
            file.pressKey(KeyEvent.VK_LEFT);
        }

        file.typeKey(' ', InputEvent.CTRL_MASK);
        CompletionInfo completion = getCompletion();
        CompletionJListOperator cjo = completion.listItself;

        if (result.length > 0) {
            checkCompletionItemsJsp(cjo, result, limit);
        }
        completion.listItself.hideAll();

    }

    @Override
    public void tearDown() throws Exception {
        EditorOperator eo = new EditorOperator("test.tag");
        eo.typeKey('a', InputEvent.CTRL_MASK);
        eo.pressKey(KeyEvent.VK_DELETE);
        eo.insert(CompletionTagSanityTest.original_content);
        eo.save();
        eo.pressKey(KeyEvent.VK_ESCAPE);
        evt.waitNoEvent(800);
    }

}
