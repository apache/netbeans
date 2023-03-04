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
package org.netbeans.modules.java.hints.errors;

import com.sun.source.util.TreePath;
import java.util.List;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.infrastructure.ErrorHintsTestBase;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.util.NbBundle;

/**
 *
 * @author lahvac
 */
public class MissingReturnStatementTest extends ErrorHintsTestBase {

    public MissingReturnStatementTest(String name) {
        super(name);
    }

    public void testToVoid1() throws Exception {
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "public class Test {\n" +
                       "    private String m() { |}\n" +
                       "}\n",
                       "FIX_ChangeMethodReturnType void",
                       ("package test;\n" +
                        "public class Test {\n" +
                        "    private void m() { }\n" +
                        "}\n").replaceAll("[\t\n ]+", " "));
    }

    public void testAddReturn1() throws Exception {
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "public class Test {\n" +
                       "    private String m() {\n" +
                       "    |}\n" +
                       "}\n",
                       "FIX_AddReturnStatement",
                       ("package test;\n" +
                        "public class Test {\n" +
                        "    private String m() {\n" +
                        "        return null;\n" +
                        "    }\n" +
                        "}\n").replaceAll("[\t\n ]+", " "));
    }

    public void test205020a() throws Exception {
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "import java.util.Collection;\n" +
                       "import java.util.List;\n" +
                       "public class Test {\n" +
                       "    public static Collection<String> join(String[] arr1, String[] arr2) {\n" +
                       "        List<String> result \n" +
                       "    |}\n" +
                       "}\n",
                       "FIX_AddReturnStatement",
                       ("package test;\n" +
                        "import java.util.Collection;\n" +
                        "import java.util.List;\n" +
                        "public class Test {\n" +
                        "    public static Collection<String> join(String[] arr1, String[] arr2) {\n" +
                        "        List<String> result \n" +
                        "        return null;\n" +
                        "    }\n" +
                        "}\n").replaceAll("[ \t\n]+", " "));
    }

    public void test205020b() throws Exception {
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "import java.util.Collection;\n" +
                       "import java.util.List;\n" +
                       "public class Test {\n" +
                       "    public static Collection<String> join(String[] arr1, String[] arr2) {\n" +
                       "        List<String> result;|}\n" +
                       "}\n",
                       "FIX_AddReturnStatement",
                       "package test; import java.util.Collection; import java.util.List; public class Test { public static Collection<String> join(String[] arr1, String[] arr2) { List<String> result;return null; } } ");
    }

    public void test205020c() throws Exception {
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "import java.util.Collection;\n" +
                       "import java.util.List;\n" +
                       "public class Test {\n" +
                       "    public static Collection<String> join(String[] arr1, String[] arr2) {\n" +
                       "        List<String> result; |}\n" +
                       "}\n",
                       "FIX_AddReturnStatement",
                       "package test; import java.util.Collection; import java.util.List; public class Test { public static Collection<String> join(String[] arr1, String[] arr2) { List<String> result; return null; } } ");
    }
    
    public void testMissingLambdaReturn() throws Exception {
        diagKey = "compiler.err.prob.found.req/compiler.misc.incompatible.ret.type.in.lambda/compiler.misc.missing.ret.val"; // NOI18N
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "import java.util.Collection;\n" +
                       "import java.util.concurrent.Callable;\n" +
                       "public class Test {\n" +
                       "    public static void test() {\n" +
                       "        Callable<String> c = |() -> {\n" +
                       "        };\n" +
                       "    }\n" +
                       "}\n",
                       "FIX_AddReturnStatement",
                       "package test; import java.util.Collection; import java.util.concurrent.Callable; public class Test { public static void test() { Callable<String> c = () -> { return null; }; } } ");
    }
    
    private String diagKey = "compiler.err.missing.ret.stmt"; // NOI18N

    @Override
    protected List<Fix> computeFixes(CompilationInfo info, int pos, TreePath path) throws Exception {
        return new MissingReturnStatement().run(info, diagKey, pos, path, null);
    }

    @Override
    protected String toDebugString(CompilationInfo info, Fix f) {
        return f.getText();
    }

    static {
        NbBundle.setBranding("test");
    }

}
