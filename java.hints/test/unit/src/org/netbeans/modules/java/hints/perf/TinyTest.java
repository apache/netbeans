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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.hints.perf;

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.hints.test.api.HintTest;

/**
 *
 * @author lahvac
 */
public class TinyTest extends NbTestCase {

    public TinyTest(String name) {
        super(name);
    }

    public void testStringConstructor1() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "     private String test(String aa) {\n" +
                       "         return new String(aa);\n" +
                       "     }\n" +
                       "}\n")
                .run(Tiny.class)
                .findWarning("3:16-3:30:verifier:new String(...)")
                .applyFix("Remove new String(...)")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "     private String test(String aa) {\n" +
                              "         return aa;\n" +
                              "     }\n" +
                              "}\n");
    }

    public void testStringConstructor2() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "     private String test(String aa) {\n" +
                       "         return new String(aa.substring(1));\n" +
                       "     }\n" +
                       "}\n")
                .preference(Tiny.SC_IGNORE_SUBSTRING, true)
                .run(Tiny.class)
                .assertWarnings();
    }

    public void testStringConstructor3() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "     private String test(String aa) {\n" +
                       "         return new String(aa.substring(1));\n" +
                       "     }\n" +
                       "}\n")
                .preference(Tiny.SC_IGNORE_SUBSTRING, false)
                .run(Tiny.class)
                .findWarning("3:16-3:43:verifier:new String(...)")
                .applyFix("Remove new String(...)")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "     private String test(String aa) {\n" +
                              "         return aa.substring(1);\n" +
                              "     }\n" +
                              "}\n");
    }

    public void testStringConstructor4() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "     private String test(String aa) {\n" +
                       "         return new String(this.substring(1));\n" +
                       "     }\n" +
                       "     private String substring(int i) {return null;}\n" +
                       "}\n")
                .preference(Tiny.SC_IGNORE_SUBSTRING, true)
                .run(Tiny.class)
                .findWarning("3:16-3:45:verifier:new String(...)")
                .applyFix("Remove new String(...)")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "     private String test(String aa) {\n" +
                              "         return this.substring(1);\n" +
                              "     }\n" +
                              "     private String substring(int i) {return null;}\n" +
                              "}\n");
    }

    public void testStringEqualsEmpty1SL15() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "     private boolean test(String aa) {\n" +
                       "         return aa.equals(\"\");\n" +
                       "     }\n" +
                       "}\n")
                .run(Tiny.class)
                .findWarning("3:16-3:29:verifier:$string.equals(\"\")")
                .applyFix("$string.length() == 0")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "     private boolean test(String aa) {\n" +
                              "         return aa.length() == 0;\n" +
                              "     }\n" +
                              "}\n");
    }

    public void testStringEqualsEmpty2SL15() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "     private boolean test(String aa) {\n" +
                       "         return !aa.equals(\"\");\n" +
                       "     }\n" +
                       "}\n")
                .run(Tiny.class)
                .findWarning("3:17-3:30:verifier:$string.equals(\"\")")
                .applyFix("$string.length() != 0")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "     private boolean test(String aa) {\n" +
                              "         return aa.length() != 0;\n" +
                              "     }\n" +
                              "}\n");
    }

    public void testStringEqualsEmpty1_6() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "     private boolean test(String aa) {\n" +
                       "         return aa.equals(\"\");\n" +
                       "     }\n" +
                       "}\n")
                .sourceLevel("1.6")
                .run(Tiny.class)
                .findWarning("3:16-3:29:verifier:$string.equals(\"\")")
                .applyFix("$string.isEmpty()")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "     private boolean test(String aa) {\n" +
                              "         return aa.isEmpty();\n" +
                              "     }\n" +
                              "}\n");
    }

    public void testStringEqualsIgnoreCaseEmpty1_6() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "     private boolean test(String aa) {\n" +
                       "         return aa.equalsIgnoreCase(\"\");\n" +
                       "     }\n" +
                       "}\n")
                .sourceLevel("1.6")
                .run(Tiny.class)
                .findWarning("3:16-3:39:verifier:$string.equals(\"\")")
                .applyFix("$string.isEmpty()")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "     private boolean test(String aa) {\n" +
                              "         return aa.isEmpty();\n" +
                              "     }\n" +
                              "}\n");
    }

    public void testLengthOneStringIndexOf1() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "     private boolean test(String aa) {\n" +
                       "         return aa.indexOf(\"a\") != 0;\n" +
                       "     }\n" +
                       "}\n")
                .run(Tiny.class)
                .findWarning("3:27-3:30:verifier:indexOf(\"a\")")
                .applyFix("indexOf('.')")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "     private boolean test(String aa) {\n" +
                              "         return aa.indexOf(\'a\') != 0;\n" +
                              "     }\n" +
                              "}\n");
    }

    public void testLengthOneStringIndexOf2() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "     private boolean test(String aa) {\n" +
                       "         return aa.indexOf(\"'\", 2) != 0;\n" +
                       "     }\n" +
                       "}\n")
                .run(Tiny.class)
                .findWarning("3:27-3:30:verifier:indexOf(\"'\")")
                .applyFix("indexOf('.')")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "     private boolean test(String aa) {\n" +
                              "         return aa.indexOf(\'\\'\', 2) != 0;\n" +
                              "     }\n" +
                              "}\n");
    }

    public void testLengthOneStringIndexOf3() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "     private boolean test(String aa) {\n" +
                       "         return aa.indexOf(\"\\\"\", 2) != 0;\n" +
                       "     }\n" +
                       "}\n")
                .run(Tiny.class)
                .findWarning("3:27-3:31:verifier:indexOf(\"\\\"\")")
                .applyFix("indexOf('.')")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "     private boolean test(String aa) {\n" +
                              "         return aa.indexOf(\'\"\', 2) != 0;\n" +
                              "     }\n" +
                              "}\n");
    }

    public void testLengthOneStringIndexOf206141a() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "     private boolean test(String aa) {\n" +
                       "         return aa.indexOf(\"\\\\\", 2) != 0;\n" +
                       "     }\n" +
                       "}\n")
                .run(Tiny.class)
                .findWarning("3:27-3:31:verifier:indexOf(\"\\\\\")")
                .applyFix("indexOf('.')")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "     private boolean test(String aa) {\n" +
                              "         return aa.indexOf(\'\\\\\', 2) != 0;\n" +
                              "     }\n" +
                              "}\n");
    }

    public void testLengthOneStringIndexOf206141b() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "     private boolean test(String aa) {\n" +
                       "         return aa.indexOf(\"\\n\", 2) != 0;\n" +
                       "     }\n" +
                       "}\n")
                .run(Tiny.class)
                .findWarning("3:27-3:31:verifier:indexOf(\"\\n\")")
                .applyFix("indexOf('.')")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "     private boolean test(String aa) {\n" +
                              "         return aa.indexOf(\'\\n\', 2) != 0;\n" +
                              "     }\n" +
                              "}\n");
    }

    public void testGetClassInsteadOfDotClass1() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "     private Class<?> test() {\n" +
                       "         return new String().getClass();\n" +
                       "     }\n" +
                       "}\n")
                .run(Tiny.class)
                .findWarning("3:16-3:39:verifier:ERR_GetClassInsteadOfDotClass")
                .applyFix("FIX_GetClassInsteadOfDotClass")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "     private Class<?> test() {\n" +
                              "         return String.class;\n" +
                              "     }\n" +
                              "}\n");
    }

    public void testGetClassInsteadOfDotClass2() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "import java.util.LinkedList;\n" +
                       "public class Test {\n" +
                       "     private Class<?> test() {\n" +
                       "         return new LinkedList(java.util.Arrays.asList(1, 2)).getClass();\n" +
                       "     }\n" +
                       "}\n")
                .run(Tiny.class)
                .findWarning("4:16-4:72:verifier:ERR_GetClassInsteadOfDotClass")
                .applyFix("FIX_GetClassInsteadOfDotClass")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "import java.util.LinkedList;\n" +
                              "public class Test {\n" +
                              "     private Class<?> test() {\n" +
                              "         return LinkedList.class;\n" +
                              "     }\n" +
                              "}\n");
    }

    public void testConstantIntern1() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "     private String test() {\n" +
                       "         return \"foo-bar\".intern();\n" +
                       "     }\n" +
                       "}\n")
                .run(Tiny.class)
                .findWarning("3:16-3:34:verifier:ERR_ConstantIntern")
                .applyFix("FIX_ConstantIntern")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "     private String test() {\n" +
                              "         return \"foo-bar\";\n" +
                              "     }\n" +
                              "}\n");
    }

    public void testConstantIntern2() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "     private String test() {\n" +
                       "         return (\"foo\" + \"-\" + \"bar\").intern();\n" +
                       "     }\n" +
                       "}\n")
                .run(Tiny.class)
                .findWarning("3:16-3:46:verifier:ERR_ConstantIntern")
                .applyFix("FIX_ConstantIntern")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "     private String test() {\n" +
                              "         return \"foo\" + \"-\" + \"bar\";\n" +
                              "     }\n" +
                              "}\n");
    }

    public void testConstantIntern3() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "     private int test() {\n" +
                       "         return (\"foo\" + \"-\" + \"bar\").intern().length();\n" +
                       "     }\n" +
                       "}\n")
                .run(Tiny.class)
                .findWarning("3:16-3:46:verifier:ERR_ConstantIntern")
                .applyFix("FIX_ConstantIntern")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "     private int test() {\n" +
                              "         return (\"foo\" + \"-\" + \"bar\").length();\n" +
                              "     }\n" +
                              "}\n");
    }

    public void testEnumSet1() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "     private java.util.Set<java.lang.annotation.RetentionPolicy> test() {\n" +
                       "         return new java.util.HashSet<java.lang.annotation.RetentionPolicy>();\n" +
                       "     }\n" +
                       "}\n")
                .run(Tiny.class)
                .assertWarnings("3:16-3:77:verifier:ERR_Tiny_enumSet");
    }

    public void testEnumMap1() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "     private java.util.Map<java.lang.annotation.RetentionPolicy, Boolean> test() {\n" +
                       "         return new java.util.HashMap<java.lang.annotation.RetentionPolicy, Boolean>();\n" +
                       "     }\n" +
                       "}\n")
                .run(Tiny.class)
                .findWarning("3:16-3:86:verifier:ERR_Tiny_enumMap")
                .applyFix("FIX_Tiny_enumMap")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "import java.util.EnumMap;\n" +
                              "public class Test {\n" +
                              "     private java.util.Map<java.lang.annotation.RetentionPolicy, Boolean> test() {\n" +
                              "         return new EnumMap<java.lang.annotation.RetentionPolicy, Boolean>(java.lang.annotation.RetentionPolicy.class);\n" +
                              "     }\n" +
                              "}\n");
    }

    public void testEnumMap2() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "     private java.util.Map<java.lang.annotation.RetentionPolicy, Boolean> test() {\n" +
                       "         return new java.util.EnumMap<java.lang.annotation.RetentionPolicy, Boolean>(java.lang.annotation.RetentionPolicy.class);\n" +
                       "     }\n" +
                       "}\n")
                .run(Tiny.class)
                .assertWarnings();
    }
    
    public void testEnumMap218550() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "     private java.util.HashMap<java.lang.annotation.RetentionPolicy, Boolean> test() {\n" +
                       "         return new java.util.HashMap<java.lang.annotation.RetentionPolicy, Boolean>();\n" +
                       "     }\n" +
                       "}\n")
                .run(Tiny.class)
                .assertWarnings();
    }

    public void testCollectionsToArray() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "     private String[] test(java.util.Collection<String> col) {\n" +
                       "         return col.toArray(new String[0]);\n" +
                       "     }\n" +
                       "}\n")
                .run(Tiny.class)
                .findWarning("3:20-3:27:verifier:ERR_Tiny_collectionsToArray")
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "     private String[] test(java.util.Collection<String> col) {\n" +
                              "         return col.toArray(new String[col.size()]);\n" +
                              "     }\n" +
                              "}\n");
    }

    public void testCollectionsToArray2() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "     private java.util.Collection<String> col() { return null; }\n" +
                       "     private String[] test() {\n" +
                       "         return col().toArray(new String[0]);\n" +
                       "     }\n" +
                       "}\n")
                .run(Tiny.class)
                .findWarning("4:22-4:29:verifier:ERR_Tiny_collectionsToArray")
                .assertFixes();
    }
}