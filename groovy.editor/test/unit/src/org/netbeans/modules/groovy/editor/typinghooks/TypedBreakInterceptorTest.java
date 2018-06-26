/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.modules.groovy.editor.typinghooks;

import org.netbeans.modules.groovy.editor.test.GroovyTestBase;

/**
 *
 * @author Martin Janicek
 */
public class TypedBreakInterceptorTest extends GroovyTestBase {

    public TypedBreakInterceptorTest(String testName) {
        super(testName);
    }

    @Override
    protected boolean runInEQ() {
        return true;
    }
    
    public void testInsertBrace3() throws Exception {
        insertBreak("x = {^}", "x = {\n    ^\n}");
    }

    public void testInsertEnd1() throws Exception {
        insertBreak("x^", "x\n^");
    }

    public void testInsertBlockComment5() throws Exception {
        insertBreak("^/*\n*/\n", "\n^/*\n*/\n");
    }

    public void testInsertEnd2() throws Exception {
        insertBreak("function foo() {^", "function foo() {\n    ^\n}");
    }

    public void testInsertEnd3() throws Exception {
        insertBreak("function foo() {^\n}", "function foo() {\n    ^\n}");
    }

    public void testInsertIf1() throws Exception {
        insertBreak("    if (true) {^", "    if (true) {\n        ^\n    }");
    }
    
    public void testContComment() throws Exception {
        if (GroovyTypedBreakInterceptor.CONTINUE_COMMENTS) {
            insertBreak("// ^", "// \n// ^");
        } else {
            insertBreak("// ^", "// \n^");
        }
    }

    public void testContComment2() throws Exception {
        // No auto-# on new lines
        if (GroovyTypedBreakInterceptor.CONTINUE_COMMENTS) {
            insertBreak("   //  ^", "   //  \n   //  ^");
        } else {
            insertBreak("   //  ^", "   //  \n   ^");
        }
    }

    public void testContComment3() throws Exception {
        // No auto-# on new lines
        if (GroovyTypedBreakInterceptor.CONTINUE_COMMENTS) {
            insertBreak("   //\t^", "   //\t\n   //\t^");
        } else {
            insertBreak("   //\t^", "   //\t\n   ^");
        }
    }

    public void testContComment4() throws Exception {
        insertBreak("// foo\n^", "// foo\n\n^");
    }

    public void testContComment5() throws Exception {
        // No auto-# on new lines
        if (GroovyTypedBreakInterceptor.CONTINUE_COMMENTS) {
            insertBreak("      // ^", "      // \n      // ^");
        } else {
            insertBreak("      // ^", "      // \n      ^");
        }
    }

    public void testContComment6() throws Exception {
        insertBreak("   // foo^bar", "   // foo\n   // ^bar");
    }

    public void testContComment8() throws Exception {
        insertBreak("   // foo^bar", "   // foo\n   // ^bar");
    }


    public void testContComment9() throws Exception {
        insertBreak("^// foobar", "\n^// foobar");
    }

    public void testContComment10() throws Exception {
        insertBreak("//foo\n^// foobar", "//foo\n// ^\n// foobar");
    }

    public void testContComment11() throws Exception {
        // This behavior is debatable -- to be consistent with testContComment10 I
        // should arguably continue comments here as well
        insertBreak("code //foo\n^// foobar", "code //foo\n\n^// foobar");
    }

    public void testContComment12() throws Exception {
        insertBreak("  code\n^// foobar", "  code\n\n  ^// foobar");
    }

    public void testContComment14() throws Exception {
        insertBreak("function foo() {\n    code\n^// foobar\n}\n", "function foo() {\n    code\n\n    ^// foobar\n}\n");
    }

    public void testContComment15() throws Exception {
        insertBreak("\n\n^// foobar", "\n\n\n^// foobar");
    }

    public void testContComment16() throws Exception {
        insertBreak("\n  \n^// foobar", "\n  \n\n^// foobar");
    }

    public void testContComment17() throws Exception {
        insertBreak("function foo() {\n  // cmnt1\n^  // cmnt2\n}\n", "function foo() {\n  // cmnt1\n  // ^\n  // cmnt2\n}\n");
    }

    public void testNoContComment() throws Exception {
        // No auto-// on new lines
        insertBreak("foo // ^", "foo // \n^");
    }

}
