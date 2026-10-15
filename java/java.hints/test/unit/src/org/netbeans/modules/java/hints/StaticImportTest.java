/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
    
    public void testStaticImportHint1_InRecord() throws Exception {
        String test = "package test; public record Test(int n) { public Test { Math.|abs(n); } }";
        String golden = "package test; import static java.lang.Math.abs; public record Test(int n) { public Test { abs(n); } }";
        performFixTest(test, golden, 17);
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

    private void performFixTest(String test, String golden) throws Exception {
        performFixTest(test, golden, 8);
    }

    // test is single line source code for test.Test, | in the member select, space before
    // golden is the output to test against
    // sn is the simple name of the static method
    private void performFixTest(String test, String golden, int level) throws Exception {
        int offset = test.indexOf("|");
        assertTrue(offset != -1);
        int end = test.indexOf("(", offset) - 1;
        assertTrue(end > 0);
        int start = test.lastIndexOf(" ", offset) + 1;
        assertTrue(start > 0);
        HintTest.create()
                .sourceLevel(level)
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
