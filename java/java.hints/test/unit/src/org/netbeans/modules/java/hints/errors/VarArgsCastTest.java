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
public class VarArgsCastTest extends ErrorHintsTestBase {

    public VarArgsCastTest(String name) {
        super(name);
    }
    
    public void testSimple1() throws Exception {
        performFixTest("test/Test.java",
                       "package test; public class Test {private void test(String s, Object... o) { test(\"\", |null); } }",
                       "VarArgsCastFix:Object",
                       "package test; public class Test {private void test(String s, Object... o) { test(\"\", (Object) null); } }");
    }
    
    public void testSimple2() throws Exception {
        performFixTest("test/Test.java",
                       "package test; public class Test {private void test(String s, Object... o) { test(\"\", n|ull); } }",
                       "VarArgsCastFix:Object",
                       "package test; public class Test {private void test(String s, Object... o) { test(\"\", (Object) null); } }");
    }
    
    public void testFixesAndOrder() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test; public class Test {private void test(String s, Object... o) { test(\"\", |null); } }",
                            "VarArgsCastFix:Object",
                            "VarArgsCastFix:Object[]");
    }

    @Override
    protected List<Fix> computeFixes(CompilationInfo info, int pos, TreePath path) throws Exception {
        return new VarArgsCast().run(info, null, pos, path, null);
    }

    @Override
    protected String toDebugString(CompilationInfo info, Fix f) {
        return f.getText();
    }

    static {
        NbBundle.setBranding("test");
    }
}