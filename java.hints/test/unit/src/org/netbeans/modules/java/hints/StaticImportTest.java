/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010, 2016 Oracle and/or its affiliates. All rights reserved.
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
 */
/*
 * Contributor(s): markiewb
 */
package org.netbeans.modules.java.hints;

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.hints.errors.ImportClassTest;
import org.netbeans.modules.java.hints.test.api.HintTest;
import org.openide.util.NbBundle;

/**
 * The following shell script was used to generate the code snippets
 *
 * <code>cat test/unit/data/test/Test.java | tr '\n' ' ' | tr '\t' ' ' | sed -E 's| +| |g' | sed 's|"|\\"|g'</code>
 *
 * but it will break if the source includes single-line comments or strange characters in Strings.
 *
 * @author Samuel Halliday
 * @see ImportClassTest
 */
public class StaticImportTest extends NbTestCase {

    public StaticImportTest(String name) {
        super(name);
    }

    public void testStaticImportHint_ForEnumFields() throws Exception {
        String test = "package test; import java.util.concurrent.TimeUnit; public class Test { public Test() { System.out.println(TimeUnit.D|AYS); } }";
        String golden = "package test; import java.util.concurrent.TimeUnit; import static java.util.concurrent.TimeUnit.DAYS; public class Test { public Test() { System.out.println(DAYS); } }";
        HintTest.create()
                .setCaretMarker('|')
                .input(test)
                .run(StaticImport.class)
                .findWarning("0:107-0:120:hint:" + NbBundle.getMessage(StaticImport.class, "ERR_StaticImport"))
                .applyFix()
                .assertCompilable()
                .assertOutput(golden);
    }
    
    public void testStaticImportHint_ForEnumFields_InAssignment() throws Exception {
        String test = "package test; import java.util.concurrent.TimeUnit; public class Test { public Test() { TimeUnit foo = TimeUnit.D|AYS; } }";
        String golden = "package test; import java.util.concurrent.TimeUnit; import static java.util.concurrent.TimeUnit.DAYS; public class Test { public Test() { TimeUnit foo = DAYS; } }";
        HintTest.create()
                .setCaretMarker('|')
                .input(test)
                .run(StaticImport.class)
                .findWarning("0:103-0:116:hint:" + NbBundle.getMessage(StaticImport.class, "ERR_StaticImport"))
                .applyFix()
                .assertCompilable()
                .assertOutput(golden);
    }

    public void testStaticImportHint_ForFields() throws Exception {
        String test = "package test; import java.util.Calendar; public class Test { public Test() { System.out.println(Calendar.JAN|UARY); } }";
        String golden = "package test; import java.util.Calendar; import static java.util.Calendar.JANUARY; public class Test { public Test() { System.out.println(JANUARY); } }";
        HintTest.create()
                .setCaretMarker('|')
                .input(test)
                .run(StaticImport.class)
                .findWarning("0:96-0:112:hint:" + NbBundle.getMessage(StaticImport.class, "ERR_StaticImport"))
                .applyFix()
                .assertCompilable()
                .assertOutput(golden);
    }
    
    public void testStaticImportHint_ForFields_InAssignment() throws Exception {
        String test = "package test; import java.util.Calendar; public class Test { public Test() { int foo = Calendar.JAN|UARY; } }";
        String golden = "package test; import java.util.Calendar; import static java.util.Calendar.JANUARY; public class Test { public Test() { int foo = JANUARY; } }";
        HintTest.create()
                .setCaretMarker('|')
                .input(test)
                .run(StaticImport.class)
                .findWarning("0:87-0:103:hint:" + NbBundle.getMessage(StaticImport.class, "ERR_StaticImport"))
                .applyFix()
                .assertCompilable()
                .assertOutput(golden);
    }
    
    public void testStaticImportHint1() throws Exception {
        String test = "package test; public class Test { public Test() { Math.|abs(1); } }";
        String golden = "package test; import static java.lang.Math.abs; public class Test { public Test() { abs(1); } }";
        performFixTest(test, golden);
    }

    public void testStaticImportHint2() throws Exception {
        String test = "package test; public class Test { public Test() { Test.get|Logger(); } public static void getLogger() { } }";
        String golden = "package test; public class Test { public Test() { getLogger(); } public static void getLogger() { } }";
        performFixTest(test, golden);
    }

    public void testStaticImportHint3() throws Exception {
        String test = "package test; public class Test extends Foo { public Test() { Foo.f|oo(); } } class Foo { static protected void foo() { } }";
        String golden = "package test; public class Test extends Foo { public Test() { foo(); } } class Foo { static protected void foo() { } }";
        performFixTest(test, golden);
    }

    public void testStaticImportHint4() throws Exception {
        String test = "package test; import java.util.Calendar; import static java.util.Calendar.*; public class Test { public Test() { Calendar.getInstance|(); } }";
        String golden = "package test; import java.util.Calendar; import static java.util.Calendar.*; public class Test { public Test() { getInstance(); } }";
        performFixTest(test, golden);
    }

    // XXX disabled... the experimental hint does the error cases
//    public void testStaticImportHint5() throws Exception {
//        String test = "package test; import java.util.logging.Logger; public class Test { public Test() { Logger.getLogger|(\"\"); } public static void getLogger() { } }";
//        performAnalysisTest(test);
//    }
//
//    public void testStaticImportHint6() throws Exception {
//        String test = "package test; public class Test extends Foo { public Test() { Bar.foo|(); } } class Foo { static protected void foo() { } } class Bar { static protected void foo() { } }";
//        performAnalysisTest(test);
//    }
//
//    public void testStaticImportHint7() throws Exception {
//        String test = "package test; import javax.crypto.KeyAgreement; import static java.util.Calendar.*; public class Test { public Test() throws Exception { KeyAgreement.getInstance|(\"\"); } }";
//        performAnalysisTest(test);
//    }

    public void testStaticImportHint8() throws Exception {
        String test = "package test; public class Test extends Foo { class FooBar { FooBar() { Foo.foo|(); } } } class Foo { static protected void foo() { } }";
        String golden = "package test; public class Test extends Foo { class FooBar { FooBar() { foo(); } } } class Foo { static protected void foo() { } }";
        performFixTest(test, golden);
    }

    // XXX disabled... the experimental hint does the error cases
//    public void testStaticImportHint9() throws Exception {
//        String test = "package test; public class Test extends Foo { class FooBar { FooBar() { Bar.foo|(); } } } class Foo { static protected void foo() { } } class Bar { static protected void foo() { } }";
//        performAnalysisTest(test);
//    }

    public void testStaticImportHint190135() throws Exception {
        String test = "package test; public class Test extends Foo { class FooBar { FooBar() { Foo.<String>foo|(); } } } class Foo { static protected void foo() { } }";
        performAnalysisTest(test);
    }
    
    public void test222375() throws Exception {
        String test = "package test; public class Test { { Foo.foo|(); } } class Foo { private static void foo() { } }";
        performAnalysisTest(test);
    }

    public void testIgnoreClass() throws Exception {
        String test = "package test; public class Test { public vod x() { Class c = Test.c|lass; } }";
        performAnalysisTest(test);
    }

    // test is single line source code for test.Test, | in the member select, space before
    // golden is the output to test against
    // sn is the simple name of the static method
    private void performFixTest(String test, String golden) throws Exception {
        int offset = test.indexOf("|");
        assertTrue(offset != -1);
        int end = test.indexOf("(", offset) - 1;
        assertTrue(end > 0);
        int start = test.lastIndexOf(" ", offset) + 1;
        assertTrue(start > 0);
        HintTest.create()
                .input(test.replace("|", ""))
                .run(StaticImport.class)
                .findWarning("0:" + start + "-0:" + end + ":hint:" + NbBundle.getMessage(StaticImport.class, "ERR_StaticImport"))
                .applyFix()
                .assertCompilable()
                .assertOutput(golden);
    }

    // test is single line source code for test.Test, | in the member select, space before
    // completes successfully if there are no hints presented
    private void performAnalysisTest(String test) throws Exception {
        int offset = test.indexOf("|");
        assertTrue(offset != -1);
        HintTest.create()
                .input(test.replace("|", ""), false)
                .run(StaticImport.class)
                .assertWarnings();
    }

}
