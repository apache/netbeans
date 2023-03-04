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

package org.netbeans.modules.apisupport.hints;

import java.net.URL;
import org.junit.Ignore;
import org.junit.Test;
import static org.netbeans.modules.apisupport.hints.Bundle.*;
import org.netbeans.modules.java.hints.test.api.HintTest;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;

public class HelpCtxHintTest {

    @Test 
    public void literalString() throws Exception {
        HintTest.create().classpath(cp()).
                input("package test;\n" +
                       "class Test {\n" +
                       "    Object o = new org.openide.util.HelpCtx(\"some.id\");\n" +
                       "}\n").
                run(HelpCtxHint.class).
                assertWarnings();
    }

    @Test
    public void constantString() throws Exception {
        HintTest.create().classpath(cp()).
                input("package test;\n" +
                       "class Test {\n" +
                       "    Object o = new org.openide.util.HelpCtx(\"some.\" + \"id\");\n" +
                       "}\n").
                run(HelpCtxHint.class).
                assertWarnings();
    }

    @Ignore // XXX need #209759 to implement check for constants
    @Test 
    public void computedString() throws Exception {
        HintTest.create().classpath(cp()).
                input("package test;\n" +
                       "class Test {\n" +
                       "    Object o = new org.openide.util.HelpCtx(toString());\n" +
                       "}\n").
                run(HelpCtxHint.class).
                assertWarnings("2:15-2:55:verifier:nonconstant help ID");
    }

    @Test 
    public void simpleClass() throws Exception {
        HintTest.create().classpath(cp()).
                input("package test;\n" +
                       "class Test {\n" +
                       "    Object o = new org.openide.util.HelpCtx(Test.class);\n" +
                       "}\n").
                run(HelpCtxHint.class).
                findWarning("2:15-2:55:verifier:" + HelpCtx_onClass_warning()).
                applyFix(HelpCtx_onClass_fix()).
                assertCompilable().
                assertOutput("package test;\n" +
                       "class Test {\n" +
                       "    Object o = new org.openide.util.HelpCtx(\"test.Test\");\n" +
                       "}\n");
    }

    @Test 
    public void className() throws Exception {
        HintTest.create().classpath(cp()).
                input("package test;\n" +
                       "class Test {\n" +
                       "    Object o = new org.openide.util.HelpCtx(Test.class.getName());\n" +
                       "}\n").
                run(HelpCtxHint.class).
                findWarning("2:15-2:65:verifier:" + HelpCtx_onClassName_warning()).
                applyFix(HelpCtx_onClass_fix()).
                assertCompilable().
                assertOutput("package test;\n" +
                       "class Test {\n" +
                       "    Object o = new org.openide.util.HelpCtx(\"test.Test\");\n" +
                       "}\n");
    }

    @Test 
    public void nestedClass() throws Exception {
        HintTest.create().classpath(cp()).
                input("package test;\n" +
                       "class Test {\n" +
                       "    class Nested {\n" +
                       "        Object o = new org.openide.util.HelpCtx(Nested.class);\n" +
                       "    }\n" +
                       "}\n").
                run(HelpCtxHint.class).
                findWarning("3:19-3:61:verifier:" + HelpCtx_onClass_warning()).
                applyFix(HelpCtx_onClass_fix()).
                assertCompilable().
                assertOutput("package test;\n" +
                       "class Test {\n" +
                       "    class Nested {\n" +
                       "        Object o = new org.openide.util.HelpCtx(\"test.Test$Nested\");\n" +
                       "    }\n" +
                       "}\n");
    }

    @Test 
    public void nestedClassName() throws Exception {
        HintTest.create().classpath(cp()).
                input("package test;\n" +
                       "class Test {\n" +
                       "    class Nested {\n" +
                       "        Object o = new org.openide.util.HelpCtx(Nested.class.getName());\n" +
                       "    }\n" +
                       "}\n").
                run(HelpCtxHint.class).
                findWarning("3:19-3:71:verifier:" + HelpCtx_onClassName_warning()).
                applyFix(HelpCtx_onClass_fix()).
                assertCompilable().
                assertOutput("package test;\n" +
                       "class Test {\n" +
                       "    class Nested {\n" +
                       "        Object o = new org.openide.util.HelpCtx(\"test.Test$Nested\");\n" +
                       "    }\n" +
                       "}\n");
    }

    private URL cp() {
        URL cp = HelpCtx.class.getProtectionDomain().getCodeSource().getLocation();
        return cp.toString().endsWith("/") ? cp : FileUtil.getArchiveRoot(cp);
    }
}
