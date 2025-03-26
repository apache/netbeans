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
package org.netbeans.modules.java.hints.errors;

import com.sun.source.util.TreePath;
import java.util.List;
import java.util.Set;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.infrastructure.ErrorHintsTestBase;
import org.netbeans.modules.java.source.parsing.JavacParser;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.util.NbBundle;

/**
 * Test cases for handing the 'var' compound declaration errors.
 * @author vkprabha
 */
public class VarCompDeclarationTest extends ErrorHintsTestBase {
    
    public VarCompDeclarationTest(String name) {
        super(name);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        sourceLevel = "1.10";
        JavacParser.DISABLE_SOURCE_LEVEL_DOWNGRADE = true;
    }

    public void testCase1() throws Exception {
        performFixTest("test/Test.java",
                       "package test; \n" +
                       "public class Test {\n" +
                       "    private void test() { \n" +
                       "        var v = 1, v1 =  10;\n" +
                       "    } \n" +
                       "}",
                       -1,
                       NbBundle.getMessage(VarCompDeclarationTest.class, "FIX_VarCompDeclaration"),
                       ("package test; \n" +
                       "public class Test {\n" +
                       "    private void test() { \n" +
                       "        var v = 1;\n" +
                       "        var v1 = 10;\n" +
                       "    } \n" +
                       "}").replaceAll("[\\s]+", " "));
    }

    public void testCase2() throws Exception {        
        performFixTest("test/Test.java",
                       "package test; \n" +
                       "public class Test {\n" +
                       "    private void test() { \n" +
                       "        var v = 1, v1 = 10, v2 = 100;\n" +
                       "    } \n" +
                       "}",
                       -1,
                       NbBundle.getMessage(VarCompDeclarationTest.class, "FIX_VarCompDeclaration"),
                       ("package test; \n" +
                       "public class Test {\n" +
                       "    private void test() { \n" +
                       "        var v = 1;\n" +
                       "        var v1 = 10;\n" +
                       "        var v2 = 100;\n" +
                       "    } \n" +
                       "}").replaceAll("[\\s]+", " "));
    }

    public void testCase3() throws Exception {
        performFixTest("test/Test.java",
                       "package test; \n" +
                       "public class Test {\n" +
                       "    private void test() { \n" +
                       "        var v = 10, v1 = \"test\";\n" +
                       "    } \n" +
                       "}",
                       -1,
                       NbBundle.getMessage(VarCompDeclarationTest.class, "FIX_VarCompDeclaration"),
                       ("package test; \n" +
                       "public class Test {\n" +
                       "    private void test() { \n" +
                       "        var v = 10; \n" +
                       "        var v1 = \"test\";\n" +
                       "    } \n" +
                       "}").replaceAll("[\\s]+", " "));
    }

    public void testCase4() throws Exception {
        performFixTest("test/Test.java",
                       "package test; \n" +
                       "public class Test {\n" +
                       "    private void test() { \n" +
                       "        var v = 10, v1 = 11, test_123 = new Object();\n" +
                       "    } \n" +
                       "}",
                       -1,
                       NbBundle.getMessage(VarCompDeclarationTest.class, "FIX_VarCompDeclaration"),
                       ("package test; \n" +
                       "public class Test {\n" +
                       "    private void test() { \n" +
                       "        var v = 10; \n" +
                       "        var v1 = 11; \n" +
                       "        var test_123 = new Object(); \n" +
                       "    } \n" +
                       "}").replaceAll("[\\s]+", " "));
    }

    public void testCase5() throws Exception {
        performFixTest("test/Test.java",
                       "package test; \n" +
                       "public class Test {\n" +
                       "    private void test() { \n" +
                       "        var v = 10, v1 = new Runnable(){ \n" +
                       "        @Override \n" +
                       "        public void run() { \n" +
                       "        var x = 10; \n" +
                       "        } \n" +
                       "      }; \n" +
                       "    } \n" +
                       "}",
                       -1,
                       NbBundle.getMessage(VarCompDeclarationTest.class, "FIX_VarCompDeclaration"),
                       ("package test; \n" +
                       "public class Test {\n" +
                       "    private void test() { \n" +
                       "        var v = 10; \n" +
                       "        var v1 = new Runnable(){ \n" +
                       "        @Override \n" +
                       "        public void run() { \n" +
                       "        var x = 10; \n" +
                       "        } \n" +
                       "      }; \n" +
                       "    } \n" +
                       "}").replaceAll("[\\s]+", " "));       
    }

    public void testCase6() throws Exception {
        performFixTest("test/Test.java",
                       "package test; \n" +
                       "public class Test {\n" +
                       "    private void test() { \n" +
                       "       var v = 10, v1 = 11\n" +
                       "    } \n" +
                       "}",
                       -1,
                       NbBundle.getMessage(VarCompDeclarationTest.class, "FIX_VarCompDeclaration"),
                       ("package test; \n" +
                       "public class Test {\n" +
                       "    private void test() { \n" +
                       "        var v = 10; \n" +
                       "        var v1 = 11;} \n" +
                       "}").replaceAll("[\\s]+", " "));       
    }

    public void testCase7() throws Exception {
        performFixTest("test/Test.java",
                       "package test; \n" +
                       "public class Test {\n" +
                       "    private void test() { \n" +
                       "        final @DA var x = 10, y = 11\n" +
                       "    } \n" +
                       "}",
                       -1,
                       NbBundle.getMessage(VarCompDeclarationTest.class, "FIX_VarCompDeclaration"),
                       ("package test; \n" +
                       "public class Test {\n" +
                       "    private void test() { \n" +
                       "        @DA final var x = 10; \n" +
                       "        @DA final var y = 11;} \n" +
                       "}").replaceAll("[\\s]+", " "));       
    }

    public void testCase8() throws Exception {
        performFixTest("test/Test.java",
                       "package test; \n" +
                       "public class Test {\n" +
                       "    private void test() { \n" +
                       "        @DA final var v = 1, v1 = 10;\n" +
                       "    } \n" +
                       "}",
                       -1,
                       NbBundle.getMessage(VarCompDeclarationTest.class, "FIX_VarCompDeclaration"),
                       ("package test; \n" +
                       "public class Test {\n" +
                       "    private void test() { \n" +
                       "        @DA final var v = 1;\n" +
                       "        @DA final var v1 = 10;\n" +
                       "    } \n" +
                       "}").replaceAll("[\\s]+", " "));
    }

    public void testCase9() throws Exception {
        performFixTest("test/Test.java",
                       "package test; \n" +
                       "public class Test {\n" +
                       "    private void test() { \n" +
                       "        int i = 1; \n" +
                       "        switch(i){ \n" +
                       "            case 1: \n" +
                       "            var v = 1, v1 = 10;\n" +
                       "            } \n" +
                       "    } \n" +
                       "}",
                       -1,
                       NbBundle.getMessage(VarCompDeclarationTest.class, "FIX_VarCompDeclaration"),
                       ("package test; \n" +
                       "public class Test {\n" +
                       "    private void test() { \n" +
                       "        int i = 1; \n" +
                       "        switch(i){ \n" +
                       "            case 1: \n" +
                       "            var v = 1;\n" +
                       "            var v1 = 10;\n" +
                       "            } \n" +
                       "    } \n" +
                       "}").replaceAll("[\\s]+", " "));
    }

    public void testCase10() throws Exception {
        performFixTest("test/Test.java",
                       "package test; \n" +
                       "public class Test {\n" +
                       "    private void test() { \n" +
                       "        int i = 1; \n" +
                       "        switch(i){ \n" +
                       "            case 1: \n" +
                       "                final var v = 1, v1 = 10;\n" +
                       "            } \n" +
                       "    } \n" +
                       "}",
                       -1,
                       NbBundle.getMessage(VarCompDeclarationTest.class, "FIX_VarCompDeclaration"),
                       ("package test; \n" +
                       "public class Test {\n" +
                       "    private void test() { \n" +
                       "        int i = 1; \n" +
                       "        switch(i){ \n" +
                       "            case 1: \n" +
                       "            final var v = 1;\n" +
                       "            final var v1 = 10;\n" +
                       "            } \n" +
                       "    } \n" +
                       "}").replaceAll("[\\s]+", " "));
    }

    public void testCase11() throws Exception {
        performFixTest("test/Test.java",
                       "package test; \n" +
                       "public class Test {\n" +
                       "    private void test() { \n" +
                       "        var v = {1, 2}, w = 2;\n" +
                       "    } \n" +
                       "}",
                       -1,
                       NbBundle.getMessage(VarCompDeclarationTest.class, "FIX_VarCompDeclaration"),
                       ("package test; \n" +
                       "public class Test {\n" +
                       "    private void test() { \n" +
                       "        var v = {1, 2};\n" +
                       "        var w = 2;\n" +
                       "    } \n" +
                       "}").replaceAll("[\\s]+", " "));
    }

    @Override
    protected List<Fix> computeFixes(CompilationInfo info, int pos, TreePath path) throws Exception {
        return new VarCompDeclaration().run(info, null, pos, path, null);
    }
    
    @Override
    protected Set<String> getSupportedErrorKeys() {
        return new VarCompDeclaration().getCodes();
    }
    
    @Override
    protected String toDebugString(CompilationInfo info, Fix f) {
        return f.getText();
    }
}
