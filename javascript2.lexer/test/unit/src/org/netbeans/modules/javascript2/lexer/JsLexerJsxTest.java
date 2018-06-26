/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript2.lexer;

import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.lexer.test.LexerTestUtilities;
import org.netbeans.lib.lexer.test.dump.TokenDumpCheck;
import org.netbeans.modules.javascript2.lexer.api.JsTokenId;

/**
 *
 * @author Petr Pisl
 */
public class JsLexerJsxTest extends NbTestCase {
    
    public JsLexerJsxTest(String name) {
        super(name);
    }
    
    @Override
    protected void setUp() throws java.lang.Exception {
        // Set-up testing environment
        LexerTestUtilities.setTesting(true);
    }
    
    public void testSimple01() throws Exception {
        LexerTestUtilities.checkTokenDump(this, "testfiles/jsx/simple01.js",
                JsTokenId.javascriptLanguage());
    }
    
    public void testSimple02() throws Exception {
        LexerTestUtilities.checkTokenDump(this, "testfiles/jsx/simple02.js",
                JsTokenId.javascriptLanguage());
    }
    
    public void testSimple03() throws Exception {
        LexerTestUtilities.checkTokenDump(this, "testfiles/jsx/simple03.js",
                JsTokenId.javascriptLanguage());
    }
    
    public void testSimple04() throws Exception {
        LexerTestUtilities.checkTokenDump(this, "testfiles/jsx/simple04.js",
                JsTokenId.javascriptLanguage());
    }
    
    public void testSimple05() throws Exception {
        LexerTestUtilities.checkTokenDump(this, "testfiles/jsx/simple05.js",
                JsTokenId.javascriptLanguage());
    }
    
    public void testInner01() throws Exception {
        LexerTestUtilities.checkTokenDump(this, "testfiles/jsx/inner01.js",
                JsTokenId.javascriptLanguage());
    }
    
    public void testTemplates01() throws Exception {
        LexerTestUtilities.checkTokenDump(this, "testfiles/jsx/templates01.js",
                JsTokenId.javascriptLanguage());
    }
    
    public void testIssue267422() throws Exception {
        LexerTestUtilities.checkTokenDump(this, "testfiles/jsx/issue267422.js",
                JsTokenId.javascriptLanguage());
    }
    
    public void testIssue268900() throws Exception {
        LexerTestUtilities.checkTokenDump(this, "testfiles/jsx/issue268900.js",
                JsTokenId.javascriptLanguage());
    }
    
    public void testStyleJSX() throws Exception {
        LexerTestUtilities.checkTokenDump(this, "testfiles/jsx/styleInJSXissue.js",
                JsTokenId.javascriptLanguage());
    }
    
    public void testIncLess() throws Exception {
        LexerTestUtilities.checkTokenDump(this, "testfiles/jsx/incLess.js",
                JsTokenId.javascriptLanguage());
    }
}
