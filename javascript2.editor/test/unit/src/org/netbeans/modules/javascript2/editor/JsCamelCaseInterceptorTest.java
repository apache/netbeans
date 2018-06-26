/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript2.editor;

import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.test.CslTestBase;
import org.netbeans.modules.javascript2.lexer.api.JsTokenId;

/**
 *
 * @author Petr Pisl
 */
public class JsCamelCaseInterceptorTest extends CslTestBase {

    public JsCamelCaseInterceptorTest(String testName) {
        super(testName);
    }
    
    public void testIssue248478_01() throws Exception {
        checkNextWordOffset("var fo^oBarBaz = '';", 7, false);
    }
    
    public void testIssue248478_02() throws Exception {
        checkNextWordOffset("var fooB^arBaz = '';", 10, false);
    }
    
    public void testIssue248478_03() throws Exception {
        checkNextWordOffset("var fooBar^Baz = '';", 13, false);
    }
    
    public void testIssue248478_04() throws Exception {
        checkNextWordOffset("var fooBarB^az = '';", 13, false);
    }
    
    public void testIssue248478_05() throws Exception {
        checkNextWordOffset("var fooBarBaz^ = '';", -1, false);
    }
    
    public void checkNextWordOffset(String text, int expected, boolean reverse) {
        int index = text.indexOf('^');
        if (index == -1) {
            assertFalse("The input text doesn't contain caret position marked via '^'", index == -1);
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append(text.substring(0, index));
        sb.append(text.substring(index + 1));
        BaseDocument document = getDocument(sb.toString(), JsTokenId.JAVASCRIPT_MIME_TYPE);
        int newOffset = JsCamelCaseInterceptor.getWordOffset(document, index, reverse);
        assertEquals(expected,  newOffset);
    } 
}
