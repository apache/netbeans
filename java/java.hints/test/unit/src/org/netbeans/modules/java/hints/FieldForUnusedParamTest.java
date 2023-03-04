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
import org.netbeans.modules.java.hints.FieldForUnusedParam.FixImpl;
import org.netbeans.modules.java.hints.infrastructure.TreeRuleTestBase;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Lahoda
 */
public class FieldForUnusedParamTest extends TreeRuleTestBase {
    
    public FieldForUnusedParamTest(String testName) {
        super(testName);
    }

    public void testSimple1() throws Exception {
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "public class Test {\n" +
                       "     public Test(int |a) {\n" +
                       "     }\n" +
                       "}\n",
                       "2:21-2:21:hint:Unused Parameter",
                       "FixImpl:false",
                       "package test; public class Test { private final int a; public Test(int a) { this.a = a; } } ");
    }
    
    public void testSimple2() throws Exception {
        performAnalysisTest("test/Test.java",
                           "package test;\n" +
                           "public class Test {\n" +
                           "     private String a;\n"+
                           "     public Test(int |a) {\n" +
                           "     }\n" +
                           "}\n");
    }
    
    public void testSimple3() throws Exception {
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "public class Test {\n" +
                       "     private int a;\n" +
                       "     public Test(int |a) {\n" +
                       "     }\n" +
                       "}\n",
                       "3:21-3:21:hint:Unused Parameter",
                       "FixImpl:true",
                       "package test; public class Test { private int a; public Test(int a) { this.a = a; } } ");
    }
    
    public void test125691() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test;\n" +
                            "public class Test {\n" +
                            "     private int a;\n" +
                            "     public Test(int |a)\n" +
                            "}\n");
    }
    
    public void testOrdering1() throws Exception {
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "public class Test {\n" +
                       "     private int a;\n" +
                       "     private int c;\n" +
                       "     public Test(int a, int |b, int c) {\n" +
                       "          this.a = a;\n" +
                       "          this.c = c;\n" +
                       "     }\n" +
                       "}\n",
                       "4:28-4:28:hint:Unused Parameter",
                       "FixImpl:false",
                       "package test; public class Test { private int a; private final int b; private int c; public Test(int a, int b, int c) { this.a = a; this.b = b; this.c = c; } } ");
    }
    
    public void test206367() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test;\n" +
                            "public class Test {\n" +
                            "     public Test(String|) {}\n" +
                            "}\n");
    }

    public void testMultipleCtors() throws Exception {
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "public class Test {\n" +
                       "     private int a;\n" +
                       "     private int c;\n" +
                       "     public Test() {}\n" +
                       "     public Test(int a, int |b, int c) {\n" +
                       "          this.a = a;\n" +
                       "          this.c = c;\n" +
                       "     }\n" +
                       "}\n",
                       "5:28-5:28:hint:Unused Parameter",
                       "FixImpl:false",
                       "package test; public class Test { private int a; private int b; private int c; public Test() {} public Test(int a, int b, int c) { this.a = a; this.b = b; this.c = c; } } ");
    }
    
    public void testMultipleCtorsChained() throws Exception {
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "public class Test {\n" +
                       "     private int a;\n" +
                       "     private int c;\n" +
                       "     public Test() { this(0, 0, 0); }\n" +
                       "     public Test(int a, int |b, int c) {\n" +
                       "          this.a = a;\n" +
                       "          this.c = c;\n" +
                       "     }\n" +
                       "}\n",
                       "5:28-5:28:hint:Unused Parameter",
                       "FixImpl:false",
                       "package test; public class Test { private int a; private final int b; private int c; public Test() { this(0, 0, 0); } public Test(int a, int b, int c) { this.a = a; this.b = b; this.c = c; } } ");
    }

    @Override
    protected List<ErrorDescription> computeErrors(CompilationInfo info, TreePath path, int offset) {
        FieldForUnusedParam h = new FieldForUnusedParam();

        if ("test206367".equals(getName())) {
            while (path != null && !h.getTreeKinds().contains(path.getLeaf().getKind())) {
                path = path.getParentPath();
            }

            assertNotNull(path);
        }

        return h.run(info, path, offset);
    }

    @Override
    protected List<ErrorDescription> computeErrors(CompilationInfo info, TreePath path) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected String toDebugString(CompilationInfo info, Fix f) {
        if (f instanceof FixImpl) {
            return "FixImpl:" + ((FixImpl) f).existing;
        }
        return super.toDebugString(info, f);
    }

    static {
        NbBundle.setBranding("test");
    }
    
}
