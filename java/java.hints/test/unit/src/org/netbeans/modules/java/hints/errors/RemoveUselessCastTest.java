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
import org.netbeans.modules.java.hints.spiimpl.TestCompilerSettings;
import org.netbeans.spi.editor.hints.Fix;

/**
 *
 * @author Jan Lahoda
 */
public class RemoveUselessCastTest extends ErrorHintsTestBase {
    
    public RemoveUselessCastTest(String name) {
        super(name);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        TestCompilerSettings.commandLine = "-Xlint:cast";
    }
    
    public void testRedundantCast1() throws Exception {
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "public class Test {\n" +
                       "    public void test() {\n" +
                       "        String s = |(String) get(String.class);\n" +
                       "    }\n" +
                       "    public <T> T get(Class<T> c) {\n" +
                       "        return null;\n" +
                       "    }\n" +
                       "}\n",
                       "FixImpl",
                       ("package test;\n" +
                       "public class Test {\n" +
                       "    public void test() {\n" +
                       "        String s = get(String.class);\n" +
                       "    }\n" +
                       "    public <T> T get(Class<T> c) {\n" +
                       "        return null;\n" +
                       "    }\n" +
                       "}\n").replaceAll("[ \t\n]+", " "));
    }

    public void testRedundantCastRemoveParentheses1() throws Exception {
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "public class Test {\n" +
                       "    public void test() {\n" +
                       "        String s = |(String) (\"a\" + \"b\");\n" +
                       "    }\n" +
                       "}\n",
                       "FixImpl",
                       ("package test;\n" +
                       "public class Test {\n" +
                       "    public void test() {\n" +
                       "        String s = \"a\" + \"b\";\n" +
                       "    }\n" +
                       "}\n").replaceAll("[ \t\n]+", " "));
    }

    public void testRedundantCastRemoveParentheses2() throws Exception {
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "public class Test {\n" +
                       "    public void test() {\n" +
                       "        Integer b = new Integer(18);\n" +
                       "        int a = (|(Integer) b).intValue();\n" +
                       "    }\n" +
                       "}\n",
                       "FixImpl",
                       ("package test;\n" +
                       "public class Test {\n" +
                       "    public void test() {\n" +
                       "        Integer b = new Integer(18);\n" +
                       "        int a = b.intValue();\n" +
                       "    }\n" +
                       "}\n").replaceAll("[ \t\n]+", " "));
    }

    @Override
    protected List<Fix> computeFixes(CompilationInfo info, int pos, TreePath path) throws Exception {
        return new RemoveUselessCast().run(info, null, pos, path, null);
    }

    @Override
    protected String toDebugString(CompilationInfo info, Fix f) {
        return "FixImpl";
    }

}
