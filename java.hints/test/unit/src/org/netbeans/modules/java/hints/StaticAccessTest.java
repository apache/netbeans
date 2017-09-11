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
package org.netbeans.modules.java.hints;

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.hints.test.api.HintTest;

/**
 *
 * @author Jaroslav Tulach
 */
public class StaticAccessTest extends NbTestCase {
    
    public StaticAccessTest(String testName) {
        super(testName);
    }

    public void testCallingStaticMethodInInitializer() throws Exception {
        String before = "package test; class Test {\n" +
            "{\n" +
            "Boolean b = null;\n" +
            "b = b.valu";
        String after = "eOf(true);\n" +
            "}\n" +
            "}\n";

        String golden = (before + after).replace('\n', ' ').replace("b.value", "Boolean.value");
        
        performFixTest("test/Test.java", before + after, before.length(), 
            "3:6-3:13:verifier:AS1valueOf",
            "MSG_StaticAccessText",
            golden
        );
    }
    
    public void testCallingStaticMethod() throws Exception {
        String before = "package test; class Test {\n" +
            "public void nic() {\n" +
            "Boolean b = null;\n" +
            "b = b.valu";
        String after = "eOf(true);\n" +
            "}\n" +
            "}\n";

        String golden = ("package test; class Test {\n" +
            "public void nic() {\n" +
            "Boolean b = null;\n" +
            "b = Boolean.valueOf(true);\n" +
            "}\n" +
            "}\n").replace('\n', ' ');
        
        
        performFixTest("test/Test.java", before + after, before.length(), 
            "3:6-3:13:verifier:AS1valueOf",
            "MSG_StaticAccessText",
            golden
        );
    }
    
    public void testCallingNonStaticStringMethod() throws Exception {
        String before = "package test; class Test {\n" +
            "public void nic() {\n" +
            "String s = \"some\";\n" +
            "int x = s.last";
        String after = "IndexOf('x');" +
            "}" +
            "";
        
        performAnalysisTest("test/Test.java", before + after, before.length());
    }
    
    
    public void testOkCallingStaticMethod() throws Exception {
        String before = "package test; class Test {" +
            "{" +
            " Boolean b = null;" +
            " b = Boolean.valu";
        String after = "eOf(true);";
        
        performAnalysisTest("test/Test.java", before + after, before.length()); 
    }
    public void testAccessingStaticField() throws Exception {
        String before = "package test; class Test {" +
            "{" +
            " Boolean b = null;" +
            " b = b.TR";
        String after = "UE;";
        
        performAnalysisTest("test/Test.java", before + after, before.length(), 
            "0:52-0:56:verifier:AS0TRUE"
        );
    }
    public void testOkAccessingStaticField() throws Exception {
        String before = "package test; class Test {" +
            "{" +
            " Boolean b = null;" +
            " b = Boolean.TR";
        String after = "UE;";
        
        performAnalysisTest("test/Test.java", before + after, before.length());
    }
    public void testAccessingStaticFieldViaMethod() throws Exception {
        String before = "package test; class Test {" +
            "static Boolean b() { return null; }" +
            "{" +
            " Object x = b().TR";
        String after = "UE;";
        
        performAnalysisTest("test/Test.java", before + after, before.length(), 
            "0:78-0:82:verifier:AS0TRUE"
        );
    }
    public void testOkToCallEqualsOnString() throws Exception {
        String before = "package test; class Test {" +
            "public void run() {\n" +
            "String s = null;\n" +
            "boolean b = \"A\".e";
        String after =         "quals(s);\n" +
            "}" +
            "}";
        
        performAnalysisTest("test/Test.java", before + after, before.length());
    }

    public void testCorrectResolution() throws Exception {
        String before = "package test; class Test {" +
            "public void run() {\n" +
            "Test t = null;\n" +
            "t.t";
        String after =         "est(2);\n" +
            "}\n" +
            "public void test() {}\n" + 
            "public static void test(int i) {}\n" +
            "}";
        
        performAnalysisTest("test/Test.java", before + after, before.length(),
                "2:2-2:6:verifier:AS1test"
        );
    }
    
    public void testIgnoreErrors1() throws Exception {
        String code = "package test; class Test {\n" +
            "public void run() {\n" +
            "aaa.getClass().getName();\n" +
            "}\n" +
            "}";
        
        performAnalysisTest("test/Test.java", code);
    }
    
    public void testIgnoreErrors2() throws Exception {
        String code = "package test; class Test {\n" +
            "public void run() {\n" +
            "aaa.getClass();\n" +
            "}\n" +
            "}";
        
        performAnalysisTest("test/Test.java", code);
    }
    
    public void testIgnoreErrors3() throws Exception {
        String code = "package test; class Test {\n" +
            "public void run(String aaa) {\n" +
            "aaa.fff();\n" +
            "}\n" +
            "}";
        
        performAnalysisTest("test/Test.java", code);
    }
    
    public void testIgnoreErrors4() throws Exception {
        String code = "package test; class Test {\n" +
            "public void run() {\n" +
            "super.fff();\n" +
            "}\n" +
            "}";
        
        performAnalysisTest("test/Test.java", code);
    }

    public void testInterface198646() throws Exception {
        String code = "package test; class Test {\n" +
            "public void run(A a) {\n" +
            "int i = a.III;\n" +
            "}\n" +
            "}\n" +
            "interface A {\n" +
            "    public static final int III = 0;\n" +
            "}";

        performAnalysisTest("test/Test.java", code, "2:10-2:13:verifier:AS0III");
    }

    public void testEnum198646() throws Exception {
        String code = "package test; class Test {\n" +
            "public void run(A a) {\n" +
            "int i = a.III;\n" +
            "}\n" +
            "}\n" +
            "enum A {\n" +
            "    A;\n" +
            "    public static final int III = 0;\n" +
            "}";

        performAnalysisTest("test/Test.java", code, "2:10-2:13:verifier:AS0III");
    }
    
    protected void performFixTest(String fileName, String code, int pos, String errorDescriptionToString, String fixDebugString, String golden) throws Exception {
        performFixTest(fileName, code, errorDescriptionToString, fixDebugString, golden);
    }
    
    protected void performFixTest(String fileName, String code, String errorDescriptionToString, String fixDebugString, String golden) throws Exception {
        HintTest.create()
                .input(fileName, code)
                .run(StaticAccess.class)
                .findWarning(errorDescriptionToString)
                .applyFix(fixDebugString)
                .assertCompilable()
                .assertOutput(fileName, golden);
    }
    
    protected void performAnalysisTest(String fileName, String code, String... golden) throws Exception {
        HintTest.create()
                .input(fileName, code, false)
                .run(StaticAccess.class)
                .assertWarnings(golden);
    }

    protected void performAnalysisTest(String fileName, String code, int pos, String... golden) throws Exception {
        performAnalysisTest(fileName, code, golden);
    }

}
