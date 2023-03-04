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
package org.netbeans.modules.web.core.syntax.js;

import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.modules.csl.api.Formatter;
import org.netbeans.modules.html.editor.api.HtmlKit;
import org.netbeans.modules.html.editor.indent.HtmlIndentTaskFactory;
import org.netbeans.modules.javascript2.lexer.api.JsTokenId;
import org.netbeans.modules.web.indent.api.support.AbstractIndenter;
import org.netbeans.test.web.core.syntax.TestBase;

public class JsTypedBreakInterceptorEmbeddedTest extends TestBase {

    public JsTypedBreakInterceptorEmbeddedTest(String testName) {
        super(testName);
    }

    @Override
    protected Formatter getFormatter(IndentPrefs preferences) {
        return null;
    }

    @Override
    protected boolean runInEQ() {
        // Must run in AWT thread (BaseKit.install() checks for that)
        return true;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        MockMimeLookup.setInstances(MimePath.parse("text/javascript"), JsTokenId.javascriptLanguage());
        HtmlIndentTaskFactory htmlReformatFactory = new HtmlIndentTaskFactory();
        MockMimeLookup.setInstances(MimePath.parse("text/html"), htmlReformatFactory, new HtmlKit("text/html"), HTMLTokenId.language());
    }

    public void testIssue233772_1() throws Exception {
        insertBreak("<%@ taglib prefix=\"c\" uri=\"http://java.sun.com/jsp/jstl/core\" %>\n"
                + "<%@page contentType=\"text/html\" pageEncoding=\"UTF-8\"%>\n"
                + "<!DOCTYPE html>\n"
                + "<html>\n"
                + "    <head>\n"
                + "        <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n"
                + "        <title>JSP Page</title>\n"
                + "        <script type=\"text/javascript\">\n"
                + "            function someFunction1(){^}\n"
                + "            \n"
                + "            var fillEditor = function(selectedItem) { \n"
                + "                <c:out value=\"\">\n"
                + "            };\n"
                + "                \n"
                + "            function someFunction3(){\n"
                + "        </script>\n"
                + "    </head>\n"
                + "    <body>\n"
                + "        <h1>Hello World!</h1>\n"
                + "    </body>\n"
                + "</html>",
                "<%@ taglib prefix=\"c\" uri=\"http://java.sun.com/jsp/jstl/core\" %>\n"
                + "<%@page contentType=\"text/html\" pageEncoding=\"UTF-8\"%>\n"
                + "<!DOCTYPE html>\n"
                + "<html>\n"
                + "    <head>\n"
                + "        <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n"
                + "        <title>JSP Page</title>\n"
                + "        <script type=\"text/javascript\">\n"
                + "            function someFunction1(){\n"
                + "                ^\n"
                + "            }\n"
                + "            \n"
                + "            var fillEditor = function(selectedItem) { \n"
                + "                <c:out value=\"\">\n"
                + "            };\n"
                + "                \n"
                + "            function someFunction3(){\n"
                + "        </script>\n"
                + "    </head>\n"
                + "    <body>\n"
                + "        <h1>Hello World!</h1>\n"
                + "    </body>\n"
                + "</html>");
    }

    public void testIssue233772_2() throws Exception {
        insertBreak("<%@ taglib prefix=\"c\" uri=\"http://java.sun.com/jsp/jstl/core\" %>\n"
                + "<%@page contentType=\"text/html\" pageEncoding=\"UTF-8\"%>\n"
                + "<!DOCTYPE html>\n"
                + "<html>\n"
                + "    <head>\n"
                + "        <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n"
                + "        <title>JSP Page</title>\n"
                + "        <script type=\"text/javascript\">\n"
                + "            function someFunction1(){}\n"
                + "            \n"
                + "            var fillEditor = function(selectedItem) { ^\n"
                + "                <c:out value=\"\">\n"
                + "            };\n"
                + "                \n"
                + "            function someFunction3(){\n"
                + "        </script>\n"
                + "    </head>\n"
                + "    <body>\n"
                + "        <h1>Hello World!</h1>\n"
                + "    </body>\n"
                + "</html>",
                "<%@ taglib prefix=\"c\" uri=\"http://java.sun.com/jsp/jstl/core\" %>\n"
                + "<%@page contentType=\"text/html\" pageEncoding=\"UTF-8\"%>\n"
                + "<!DOCTYPE html>\n"
                + "<html>\n"
                + "    <head>\n"
                + "        <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n"
                + "        <title>JSP Page</title>\n"
                + "        <script type=\"text/javascript\">\n"
                + "            function someFunction1(){}\n"
                + "            \n"
                + "            var fillEditor = function(selectedItem) { \n"
                + "                ^\n"
                + "                <c:out value=\"\">\n"
                + "            };\n"
                + "                \n"
                + "            function someFunction3(){\n"
                + "        </script>\n"
                + "    </head>\n"
                + "    <body>\n"
                + "        <h1>Hello World!</h1>\n"
                + "    </body>\n"
                + "</html>");
    }

    public void testIssue233772_3() throws Exception {
        insertBreak("<%@ taglib prefix=\"c\" uri=\"http://java.sun.com/jsp/jstl/core\" %>\n"
                + "<%@page contentType=\"text/html\" pageEncoding=\"UTF-8\"%>\n"
                + "<!DOCTYPE html>\n"
                + "<html>\n"
                + "    <head>\n"
                + "        <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n"
                + "        <title>JSP Page</title>\n"
                + "        <script type=\"text/javascript\">\n"
                + "            function someFunction1(){}\n"
                + "            \n"
                + "            var fillEditor = function(selectedItem) { \n"
                + "                <c:out value=\"\">\n"
                + "            };\n"
                + "                \n"
                + "            function someFunction3(){^\n"
                + "        </script>\n"
                + "    </head>\n"
                + "    <body>\n"
                + "        <h1>Hello World!</h1>\n"
                + "    </body>\n"
                + "</html>",
                "<%@ taglib prefix=\"c\" uri=\"http://java.sun.com/jsp/jstl/core\" %>\n"
                + "<%@page contentType=\"text/html\" pageEncoding=\"UTF-8\"%>\n"
                + "<!DOCTYPE html>\n"
                + "<html>\n"
                + "    <head>\n"
                + "        <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n"
                + "        <title>JSP Page</title>\n"
                + "        <script type=\"text/javascript\">\n"
                + "            function someFunction1(){}\n"
                + "            \n"
                + "            var fillEditor = function(selectedItem) { \n"
                + "                <c:out value=\"\">\n"
                + "            };\n"
                + "                \n"
                + "            function someFunction3(){\n"
                + "                ^\n"
                + "            }\n"
                + "        </script>\n"
                + "    </head>\n"
                + "    <body>\n"
                + "        <h1>Hello World!</h1>\n"
                + "    </body>\n"
                + "</html>");
    }

}
