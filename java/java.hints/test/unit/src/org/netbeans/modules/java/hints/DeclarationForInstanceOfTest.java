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

package org.netbeans.modules.java.hints;

import com.sun.source.util.TreePath;
import java.util.List;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.infrastructure.TreeRuleTestBase;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;

/**
 *
 * @author Jan lahoda
 */
public class DeclarationForInstanceOfTest extends TreeRuleTestBase {
    
    public DeclarationForInstanceOfTest(String testName) {
        super(testName);
    }

    public void testSimple() throws Exception {
        performFixTest("test/Test.java",
                       "package test;" +
                       "public class Test {\n" +
                       "    private void test(Object o) {\n" +
                       "        if (o instan|ceof String) {\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n",
                       "2:20-2:20:hint:Declaration for instanceof",
                       "FixImpl",
                       "package test;public class Test { private void test(Object o) { if (o instanceof String) { String string = (String) o; } } } ");
    }
    
    public void DISABLEDtestBrokenSource() throws Exception {
        performFixTest("test/Test.java",
                       "package test;" +
                       "public class Test {\n" +
                       "    private void test(Object o) {\n" +
                       "        if (o instan|ceof String)\n" +
                       "    }\n" +
                       "}\n",
                       "2:20-2:20:hint:Declaration for instanceof",
                       "FixImpl",
                       "package test;public class Test { private void test(Object o) { if (o instanceof String) { String string = (String) o; } } } ");
    }
    
    public void DISABLEtest132746() throws Exception {
        performAnalysisTest("test/Test.java",
                       "package test;" +
                       "public class Test {\n" +
                       "    private void test(Object o) {\n" +
                       "        if (o instan|ceof String) {\n" +
                       "            String s = (String) o;" +
                       "        }\n" +
                       "    }\n" +
                       "}\n");
    }
    
    public void test132747() throws Exception {
        performAnalysisTest("test/Test.java",
                       "package test;" +
                       "public class Test {\n" +
                       "    private void test(Integer o) {\n" +
                       "        if (o instan|ceof String) {\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n");
    }
    
    public void test132755() throws Exception {
        performFixTest("test/Test.java",
                       "package test;" +
                       "public class Test {\n" +
                       "    private void test(Object o) {\n" +
                       "        if (o instan|ceof String)\n" +
                       "            System.err.println(o);\n" +
                       "    }\n" +
                       "}\n",
                       "2:20-2:20:hint:Declaration for instanceof",
                       "FixImpl",
                       "package test;public class Test { private void test(Object o) { if (o instanceof String) { String string = (String) o; System.err.println(o); } } } ");
    }
    
    public void test132757() throws Exception {
        performAnalysisTest("test/Test.java",
                       "package test;" +
                       "public class Test {\n" +
                       "    private void test(Integer o) {\n" +
                       "        if (o instan|ceof ) {\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n");
    }
    
    @Override
    protected List<ErrorDescription> computeErrors(CompilationInfo info, TreePath path) {
        throw new UnsupportedOperationException("Should not be called");
    }

    @Override
    protected List<ErrorDescription> computeErrors(CompilationInfo info, TreePath path, int offset) {
        return new DeclarationForInstanceOf().run(info, path, offset);
    }

    @Override
    protected String toDebugString(CompilationInfo info, Fix f) {
        if (f instanceof DeclarationForInstanceOf.FixImpl) {
            return "FixImpl";
        }
        return super.toDebugString(info, f);
    }

}
