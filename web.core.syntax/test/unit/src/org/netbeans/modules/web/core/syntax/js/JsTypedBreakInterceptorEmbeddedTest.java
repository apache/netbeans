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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
