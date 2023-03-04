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

package org.netbeans.modules.groovy.editor.api;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.groovy.editor.test.GroovyTestBase;

/**
 *
 * @author Martin Adamek
 */
public class GroovyOccurencesFinderTest extends GroovyTestBase {

    public GroovyOccurencesFinderTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Logger.getLogger(GroovyOccurencesFinderTest.class.getName()).setLevel(Level.FINEST);
    }

    public void testParams() throws Exception {
        String caretLine = "        par^ams.each {";
        checkOccurrences("testfiles/BookmarkController.groovy", caretLine, false);
    }

    public void testUnusedParams() throws Exception {
        String caretLine = "    private printParams(params, unus^edParam) {";
        checkOccurrences("testfiles/BookmarkController.groovy", caretLine, true);
    }

    public void testClassVariable1() throws Exception {
        String caretLine = "    Map par^ams = [:]";
        checkOccurrences("testfiles/BookmarkController.groovy", caretLine, true);
    }

    public void testClassVariable2() throws Exception {
        String caretLine = "    def sc^affold = Bookmark ";
        checkOccurrences("testfiles/BookmarkController.groovy", caretLine, true);
    }

    public void testSuperVariable() throws Exception {
        String caretLine = "        b.user = User.get(ses^sion.user.id)";
        checkOccurrences("testfiles/BookmarkController.groovy", caretLine, true);
    }

    public void testVariable() throws Exception {
        String caretLine = "        this.fi^eld1 = 77";
        checkOccurrences("testfiles/Hello.groovy", caretLine, true);
    }

    public void testAbstractClass() throws Exception {
        String caretLine = "abstract class Mini^Class3 {";
        checkOccurrences("testfiles/MiniClass3.groovy", caretLine, true);
    }

    /* now test some stuff from GroovyScopeTestcase.groovy */

    public void testMethod1() throws Exception {
        doTest("new TestCase().met^hod1()");
    }

    public void testMethod2() throws Exception {
        doTest("new TestCase().met^hod1(1)");
    }

    public void testMethod3() throws Exception {
        doTest("c.met^hod1()");
    }

    public void testMethod4() throws Exception {
        doTest("c.met^hod1(1)");
    }

    public void testMethod5() throws Exception {
        doTest("        met^hod1()");
    }

    public void testMethod6() throws Exception {
        doTest("        met^hod1(1)");
    }

    public void testMethod7() throws Exception {
        doTest("        this.meth^od1()");
    }

    public void testMethod8() throws Exception {
        doTest("        this.meth^od1(1)");
    }

    public void testMethod9() throws Exception {
        doTest("    def met^hod1 (int param1){");
    }

    public void testMethod10() throws Exception {
        doTest("TestCase.create().met^hod1()");
    }
    public void testMethod11() throws Exception {
        doTest("TestCase.create().met^hod1(1)");
    }

    public void testConstructor1() throws Exception {
        doTest("new Tes^tCase().method1(1)");
    }

    public void testConstructor2() throws Exception {
        doTest("    TestCa^se() {");
    }

    public void testClass1() throws Exception {
        doTest("class TestC^ase extends java.lang.Object {");
    }

    public void testClass2() throws Exception {
        doTest("T^estCase.create().method1(1)");
    }

    public void testClass3() throws Exception {
        doTest("class TestCase extends ^java.lang.Object {");
    }

    public void testClass4() throws Exception {
        doTest("class Test^Case extends java.lang.Object {");
    }

    public void testLocalVar3() throws Exception {
        doTest("        int local^var1 = 3");
    }

    public void testMemberVar4() throws Exception {
        doTest("    int member^var1 = 2");
    }

    public void testParameter5() throws Exception {
        doTest("        def localvar3 = membervar1 + par^am1 + localvar1 + localvar2");
    }

    public void testPackageInScript() throws Exception {
        doTest("pac^kage foo");
    }

    public void testNonIdentifier() throws Exception {
        doTest("    int membervar1 =^ 2");
    }

    public void testMethodParameter1() throws Exception {
        doTest("        x^y = 5");
    }

    public void testMethodParameter2() throws Exception {
        doTest("    def test (Object ^xy = 0 ) {");
    }

    public void testClosureParameter1() throws Exception {
        doTest("        [1,2,3].each { va^lue ->");
    }

    public void testClosureParameter2() throws Exception {
        doTest("            println val^ue+\" \"+(value*value)");
    }

    private void doTest(String caretLine) throws Exception {
        checkOccurrences("testfiles/GroovyScopeTestcase.groovy", caretLine, true);
    }

}
