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

import org.netbeans.modules.java.hints.infrastructure.ErrorHintsTestBase;
import org.openide.util.NbBundle;

/**
 *
 * @author lahvac
 */
public class ChangeMethodReturnTypeTest extends ErrorHintsTestBase {

    public ChangeMethodReturnTypeTest(String name) {
        super(name, ChangeMethodReturnType.class);
    }

    public void testVoidToInt() throws Exception {
        performFixTest("test/Test.java",
                       "package test; public class Test { private void t() { return 1|;} }",
                       "FIX_ChangeMethodReturnType int",
                       "package test; public class Test { private int t() { return 1;} }");
    }

    public void testStringToInt() throws Exception {
        performFixTest("test/Test.java",
                       "package test; public class Test { private String t() { return 1|;} }",
                       "FIX_ChangeMethodReturnType int",
                       "package test; public class Test { private int t() { return 1;} }");
    }

    public void test200467() throws Exception {
        performFixTest("test/Test.java",
                       "package test; import java.util.List; public class Test { <A> void getMForm() { List<? extends A> a = null; return a|; } }",
                       "FIX_ChangeMethodReturnType List&lt;? extends A>",
                       "package test; import java.util.List; public class Test { <A> List<? extends A> getMForm() { List<? extends A> a = null; return a; } }");
    }

    public void test201546() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test; public class Test { Test() { return 1|; } }");
    }

    public void test203360() throws Exception {
        performFixTest("test/Test.java",
                       "package test; import java.util.Collections; public class Test { private int t() { return Collections.emptyList(|); } }",
                       "FIX_ChangeMethodReturnType List&lt;Object>",
                       "package test; import java.util.Collections;import java.util.List; public class Test { private List<Object> t() { return Collections.emptyList(); } }");
    }
    public void test231963() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test; public class Test { private void t() { return nu|ll; } }");
    }

    public void test233213() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test;" +
                            "public class Test {\n" +
                            "    public void setSeverity(String hint, int severity) {\n" +
                            "        return this.setSeverity(|hint);\n" +
                            "    }\n" +
                            "}");
    }
    
    public void testConditional() throws Exception {
        performFixTest("test/Test.java",
                       "package test; import java.util.Collections; public class Test { private int t() { return 1 == 1 ? Collections.emptyList() : Collections.emptyList(); } }",
                       -1,
                       "FIX_ChangeMethodReturnType List&lt;Object>",
                       "package test; import java.util.Collections;import java.util.List; public class Test { private List<Object> t() { return 1 == 1 ? Collections.emptyList() : Collections.emptyList(); } }");
    }

    static {
        NbBundle.setBranding("test");
    }

}
