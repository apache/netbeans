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

package org.netbeans.modules.javascript2.editor;

import org.netbeans.modules.csl.api.Formatter;


public class JsonTypedTextInterceptorTest extends JsonTestBase {

    public JsonTypedTextInterceptorTest(String testName) {
        super(testName);
    }

    @Override
    protected Formatter getFormatter(IndentPrefs preferences) {
        return null;
    }

    private void insertChar(String original, char insertText, String expected) throws Exception {
        insertChar(original, insertText, expected, null);
    }

    private void insertChar(String original, char insertText, String expected, String selection) throws Exception {
        insertChar(original, insertText, expected, selection, false);
    }

    public void testInsertX() throws Exception {
        insertChar("c^ass", 'l', "cl^ass");
    }

    public void testInsertX2() throws Exception {
        insertChar("clas^", 's', "class^");
    }

    public void testNoMatchInStrings() throws Exception {
        insertChar("\"^\"", '\'', "\"'^\"");
        insertChar("\"^\"", '[', "\"[^\"");
        insertChar("\"^\"", '(', "\"(^\"");
        insertChar("\"^)\"", ')', "\")^)\"");
    }

    public void testNoSingleQuotes1() throws Exception {
        insertChar("{ ^ }", '\'', "{ '^ }");
    }

    public void testNoSingleQuotes2() throws Exception {
        insertChar("{ '^ }", '\'', "{ ''^ }");
    }

    public void testDoubleQuotes1() throws Exception {
        insertChar("{ ^ }", '"', "{ \"^\" }");
    }

    public void testDoubleQuotes2() throws Exception {
        insertChar("{ \"^\" }", '"', "{ \"\"^ }");
    }

    public void testDoubleQuotes3() throws Exception {
        insertChar("{ \"^\" }", 'a', "{ \"a^\" }");
    }

    public void testDobuleQuotes4() throws Exception {
        insertChar("{ \"\\^\" }", '"', "{ \"\\\"^\" }");
    }

    public void testBrackets1() throws Exception {
        insertChar("{ ^ }", '[', "{ [^] }");
    }

    public void testBrackets2() throws Exception {
        insertChar("{ [^] }", ']', "{ []^ }");
    }

    public void testBrackets3() throws Exception {
        insertChar("{ [^] }", 'a', "{ [a^] }");
    }

    public void testBrackets4() throws Exception {
        insertChar("{ [^] }", '[', "{ [[^]] }");
    }

    public void testBrackets5() throws Exception {
        insertChar("{ [[^]] }", ']', "{ [[]^] }");
    }

    public void testBrackets6() throws Exception {
        insertChar("{ [[]^] }", ']', "{ [[]]^ }");
    }

    public void testBrace1() throws Exception {
        insertChar("{ \"x\":{^} }", '}', "{ \"x\":{}^ }");
    }

    public void testNoParens1() throws Exception {
        insertChar("{ ^ }", '(', "{ (^ }");
    }

    public void testNoParens2() throws Exception {
        insertChar("{ (^) }", ')', "{ ()^) }");
    }
}
