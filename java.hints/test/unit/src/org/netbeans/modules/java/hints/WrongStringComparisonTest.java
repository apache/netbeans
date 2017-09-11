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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.hints;

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.hints.test.api.HintTest;

/**
 *
 * @author Jan Lahoda
 */
public class WrongStringComparisonTest extends NbTestCase {

    private WrongStringComparison wsc;

    public WrongStringComparisonTest(String name) {
        super(name);
        wsc = new WrongStringComparison();
    }

    public void testSimple() throws Exception {
        HintTest.create()
                .input("package test;" +
                       "public class Test {" +
                       "    private String s;" +
                       "    private void test() {" +
                       "        String t = null;" +
                       "        if (s == t);" +
                       "    }" +
                       "}")
                .run(WrongStringComparison.class)
                .assertWarnings("0:114-0:120:verifier:Comparing Strings using == or !=");
    }
    
    public void testDisableWhenCorrectlyCheckedAsIn111441() throws Exception {
        HintTest.create()
                .input("package test;" +
                       "public class Test {" +
                       "    private String s;" +
                       "    private void test() {" +
                       "        Test t = null;" +
                       "        boolean b = this.s != t.s && (this.s == null || !this.s.equals(t.s));" +
                       "    }" +
                       "}")
                .run(WrongStringComparison.class)
                .assertWarnings();
    }

    public void testFixWithTernaryNullCheck() throws Exception {
        HintTest.create()
                .input("package test;" +
                       "public class Test {" +
                       "    private String s;" +
                       "    private void test() {" +
                       "        String t = null;" +
                       "        if (s == t);" +
                       "    }" +
                       "}")
                .run(WrongStringComparison.class)
                .findWarning("0:114-0:120:verifier:Comparing Strings using == or !=")
                .applyFix("[WrongStringComparisonFix:Use equals() with null check (ternary)]")
                .assertCompilable()
                .assertOutput("package test;public class Test { private String s; private void test() { String t = null; if (s == null ? t == null : s.equals(t)); }}");
    }

    public void testFixWithoutNullCheck() throws Exception {
        HintTest.create()
                .input("package test;" +
                       "public class Test {" +
                       "    private String s;" +
                       "    private void test() {" +
                       "        String t = null;" +
                       "        if (s == t);" +
                       "    }" +
                       "}")
                .preference(WrongStringComparison.TERNARY_NULL_CHECK, false)
                .run(WrongStringComparison.class)
                .findWarning("0:114-0:120:verifier:Comparing Strings using == or !=")
                .applyFix("[WrongStringComparisonFix:Use equals()]")
                .assertCompilable()
                .assertOutput("package test;public class Test { private String s; private void test() { String t = null; if (s.equals(t)); }}");
    }

    public void testFixWithNullCheck() throws Exception {
        HintTest.create()
                .input("package test;" +
                       "public class Test {" +
                       "    private String s;" +
                       "    private void test() {" +
                       "        String t = null;" +
                       "        if (s == t);" +
                       "    }" +
                       "}")
                .preference(WrongStringComparison.TERNARY_NULL_CHECK, false)
                .run(WrongStringComparison.class)
                .findWarning("0:114-0:120:verifier:Comparing Strings using == or !=")
                .applyFix("[WrongStringComparisonFix:Use equals() with null check]")
                .assertCompilable()
                .assertOutput("package test;public class Test { private String s; private void test() { String t = null; if ((s == null && t == null) || (s != null && s.equals(t))); }}");
    }

    public void testFixWithStringLiteralFirst() throws Exception {
        HintTest.create()
                .input("package test;" +
                       "public class Test {" +
                       "    private String s;" +
                       "    private void test() {" +
                       "        if (\"\" == s);" +
                       "    }" +
                       "}")
                .run(WrongStringComparison.class)
                .findWarning("0:90-0:97:verifier:Comparing Strings using == or !=")
                .applyFix("[WrongStringComparisonFix:Use equals()]")
                .assertCompilable()
                .assertOutput("package test;public class Test { private String s; private void test() { if (\"\".equals(s)); }}");
    }

    public void testFixWithStringLiteralSecondReverseOperands() throws Exception {
        HintTest.create()
                .input("package test;" +
                       "public class Test {" +
                       "    private String s;" +
                       "    private void test() {" +
                       "        if (s == \"\");" +
                       "    }" +
                       "}")
                .run(WrongStringComparison.class)
                .findWarning("0:90-0:97:verifier:Comparing Strings using == or !=")
                .applyFix("[WrongStringComparisonFix:Use equals() and reverse operands]")
                .assertCompilable()
                .assertOutput("package test;public class Test { private String s; private void test() { if (\"\".equals(s)); }}");
    }

    public void testFixWithStringLiteralSecondNullCheck() throws Exception {
        HintTest.create()
                .input("package test;" +
                       "public class Test {" +
                       "    private String s;" +
                       "    private void test() {" +
                       "        if (s == \"\");" +
                       "    }" +
                       "}")
                .preference(WrongStringComparison.STRING_LITERALS_FIRST, false)
                .run(WrongStringComparison.class)
                .findWarning("0:90-0:97:verifier:Comparing Strings using == or !=")
                .applyFix("[WrongStringComparisonFix:Use equals() with null check]")
                .assertCompilable()
                .assertOutput("package test;public class Test { private String s; private void test() { if (s != null && s.equals(\"\")); }}");
    }

    public void testFixWithStringLiteralSecondNoNullCheck() throws Exception {
        HintTest.create()
                .input("package test;" +
                       "public class Test {" +
                       "    private String s;" +
                       "    private void test() {" +
                       "        if (s == \"\");" +
                       "    }" +
                       "}")
                .preference(WrongStringComparison.STRING_LITERALS_FIRST, false)
                .run(WrongStringComparison.class)
                .findWarning("0:90-0:97:verifier:Comparing Strings using == or !=")
                .applyFix("[WrongStringComparisonFix:Use equals()]")
                .assertCompilable()
                .assertOutput("package test;public class Test { private String s; private void test() { if (s.equals(\"\")); }}");
    }

    public void testFixWithTwoStringLiterals() throws Exception {
        HintTest.create()
                .input("test/Test.java",
                       "package test;" +
                       "public class Test {" +
                       "    private void test() {" +
                       "        if (\"\" == \"\");" +
                       "    }" +
                       "}")
                .run(WrongStringComparison.class)
                .findWarning("0:69-0:77:verifier:Comparing Strings using == or !=")
                .applyFix("[WrongStringComparisonFix:Use equals()]")
                .assertCompilable()
                .assertOutput("package test;public class Test { private void test() { if (\"\".equals(\"\")); }}");
    }

}
