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

package org.netbeans.modules.java.hints.perf;

import org.junit.Test;
import org.netbeans.modules.java.hints.test.api.HintTest;
import org.netbeans.modules.java.hints.test.api.HintTest.HintOutput;

/**
 *
 * @author lahvac
 */
public class SizeEqualsZeroTest {

    @Test
    public void testEqualsZero() throws Exception {
        final HintOutput output = HintTest.create()
                .input("test/Test.java",
                       "package test;\n" +
                       "import java.util.List;" +
                       "public class Test {\n" +
                       "     private void test(List l) {\n" +
                       "         boolean b = l.size() == 0;\n" +
                       "         boolean b2 = 0 == l.size();\n" +
                       "     }\n" +
                       "}\n")
                .run(SizeEqualsZero.class);
        output.findWarning("3:21-3:34:verifier:.size() == 0")
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;\n" +
                       "import java.util.List;" +
                       "public class Test {\n" +
                       "     private void test(List l) {\n" +
                       "         boolean b = l.isEmpty();\n" +
                       "         boolean b2 = 0 == l.size();\n" +
                       "     }\n" +
                       "}\n");
        output.findWarning("4:22-4:35:verifier:.size() == 0")
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;\n" +
                       "import java.util.List;" +
                       "public class Test {\n" +
                       "     private void test(List l) {\n" +
                       "         boolean b = l.isEmpty();\n" +
                       "         boolean b2 = l.isEmpty();\n" +
                       "     }\n" +
                       "}\n");
    }

    @Test
    public void testNotEqualsZero() throws Exception {
        final HintOutput output = HintTest.create()
                .input("package test;\n" +
                       "import java.util.List;" +
                       "public class Test {\n" +
                       "     private void test(List l) {\n" +
                       "         boolean b = l.size() != 0;\n" +
                       "         boolean b2 = 0 != l.size();\n" +
                       "     }\n" +
                       "}\n")
                .preference(SizeEqualsZero.CHECK_NOT_EQUALS, true)
                .run(SizeEqualsZero.class);
        output.findWarning("3:21-3:34:verifier:.size() != 0")
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;\n" +
                        "import java.util.List;" +
                        "public class Test {\n" +
                        "     private void test(List l) {\n" +
                        "         boolean b = !l.isEmpty();\n" +
                        "         boolean b2 = 0 != l.size();\n" +
                        "     }\n" +
                        "}\n");
        output.findWarning("4:22-4:35:verifier:.size() != 0")
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;\n" +
                        "import java.util.List;" +
                        "public class Test {\n" +
                        "     private void test(List l) {\n" +
                        "         boolean b = !l.isEmpty();\n" +
                        "         boolean b2 = !l.isEmpty();\n" +
                        "     }\n" +
                        "}\n");
    }

    @Test
    public void testGreaterZero() throws Exception {
        final HintOutput output = HintTest.create()
                .input("package test;\n" +
                       "import java.util.List;" +
                       "public class Test {\n" +
                       "     private void test(List l) {\n" +
                       "         boolean b = l.size() > 0;\n" +
                       "         boolean b2 = 0 < l.size();\n" +
                       "     }\n" +
                       "}\n")
                .preference(SizeEqualsZero.CHECK_NOT_EQUALS, true)
                .run(SizeEqualsZero.class);
        output.findWarning("3:21-3:33:verifier:.size() > 0")
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;\n" +
                        "import java.util.List;" +
                        "public class Test {\n" +
                        "     private void test(List l) {\n" +
                        "         boolean b = !l.isEmpty();\n" +
                        "         boolean b2 = 0 < l.size();\n" +
                        "     }\n" +
                        "}\n");
        output.findWarning("4:22-4:34:verifier:.size() > 0")
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;\n" +
                        "import java.util.List;" +
                        "public class Test {\n" +
                        "     private void test(List l) {\n" +
                        "         boolean b = !l.isEmpty();\n" +
                        "         boolean b2 = !l.isEmpty();\n" +
                        "     }\n" +
                        "}\n");
    }

    @Test
    public void testCollection() throws Exception {
        final HintOutput output = HintTest.create()
                .input("test/Test.java",
                       "package test;\n" +
                       "import java.util.ArrayList;" +
                       "public class Test extends ArrayList {\n" +
                       "     private void test() {\n" +
                       "         boolean b = size() == 0;\n" +
                       "         boolean b2 = 0 != size();\n" +
                       "         boolean b3 = 0 < size();\n" +
                       "     }\n" +
                       "}\n")
                .run(SizeEqualsZero.class);
        output.findWarning("3:21-3:32:verifier:.size() == 0")
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;\n" +
                       "import java.util.ArrayList;" +
                       "public class Test extends ArrayList {\n" +
                       "     private void test() {\n" +
                       "         boolean b = isEmpty();\n" +
                       "         boolean b2 = 0 != size();\n" +
                       "         boolean b3 = 0 < size();\n" +
                       "     }\n" +
                       "}\n");
        output.findWarning("4:22-4:33:verifier:.size() != 0")
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;\n" +
                       "import java.util.ArrayList;" +
                       "public class Test extends ArrayList {\n" +
                       "     private void test() {\n" +
                       "         boolean b = isEmpty();\n" +
                       "         boolean b2 = !isEmpty();\n" +
                       "         boolean b3 = 0 < size();\n" +
                       "     }\n" +
                       "}\n");
        output.findWarning("5:22-5:32:verifier:.size() > 0")
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;\n" +
                       "import java.util.ArrayList;" +
                       "public class Test extends ArrayList {\n" +
                       "     private void test() {\n" +
                       "         boolean b = isEmpty();\n" +
                       "         boolean b2 = !isEmpty();\n" +
                       "         boolean b3 = !isEmpty();\n" +
                       "     }\n" +
                       "}\n");
    }

    @Test
    public void testMap() throws Exception {
        final HintOutput output = HintTest.create()
                .input("test/Test.java",
                       "package test;\n" +
                       "import java.util.HashMap;" +
                       "public class Test extends HashMap {\n" +
                       "     private void test() {\n" +
                       "         boolean b = size() == 0;\n" +
                       "         boolean b2 = 0 != size();\n" +
                       "         boolean b3 = size() > 0;\n" +
                       "     }\n" +
                       "}\n")
                .run(SizeEqualsZero.class);
        output.findWarning("3:21-3:32:verifier:.size() == 0")
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;\n" +
                       "import java.util.HashMap;" +
                       "public class Test extends HashMap {\n" +
                       "     private void test() {\n" +
                       "         boolean b = isEmpty();\n" +
                       "         boolean b2 = 0 != size();\n" +
                       "         boolean b3 = size() > 0;\n" +
                       "     }\n" +
                       "}\n");
        output.findWarning("4:22-4:33:verifier:.size() != 0")
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;\n" +
                       "import java.util.HashMap;" +
                       "public class Test extends HashMap {\n" +
                       "     private void test() {\n" +
                       "         boolean b = isEmpty();\n" +
                       "         boolean b2 = !isEmpty();\n" +
                       "         boolean b3 = size() > 0;\n" +
                       "     }\n" +
                       "}\n");
        output.findWarning("5:22-5:32:verifier:.size() > 0")
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;\n" +
                       "import java.util.HashMap;" +
                       "public class Test extends HashMap {\n" +
                       "     private void test() {\n" +
                       "         boolean b = isEmpty();\n" +
                       "         boolean b2 = !isEmpty();\n" +
                       "         boolean b3 = !isEmpty();\n" +
                       "     }\n" +
                       "}\n");
    }

    @Test
    public void testDoNotChangeIsEmptyItself() throws Exception {
        HintTest.create()
                .input("test/Test.java",
                       "package test;\n" +
                       "import java.util.ArrayList;" +
                       "import java.util.HashMap;" +
                       "public class Test extends ArrayList {\n" +
                       "     public boolean isEmpty() {\n" +
                       "         return this.size() == 0;\n" +
                       "     }\n" +
                       "}\n" +
                       "class OtherTest extends HashMap {\n" +
                       "     public boolean isEmpty() {\n" +
                       "         return !(0 != size());\n" +
                       "     }\n" +
                       "}\n" +
                       "class EntirelyDifferentTest extends HashMap {\n" +
                       "     public boolean isEmpty() {\n" +
                       "         return !(0 < size());\n" +
                       "     }\n" +
                       "}\n")
                .run(SizeEqualsZero.class)
                .assertWarnings();
    }

    @Test
    public void testSimpleConfig() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "import java.util.List;" +
                       "public class Test {\n" +
                       "     private void test(List l) {\n" +
                       "         boolean b = l.size() != 0;\n" +
                       "         boolean b2 = 0 != l.size();\n" +
                       "         boolean b3 = 0 < l.size();\n" +
                       "     }\n" +
                       "}\n")
                .preference(SizeEqualsZero.CHECK_NOT_EQUALS, false)
                .run(SizeEqualsZero.class)
                .assertWarnings();
    }

}