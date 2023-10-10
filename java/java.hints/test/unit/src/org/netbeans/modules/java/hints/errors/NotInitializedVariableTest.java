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
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.infrastructure.ErrorHintsTestBase;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.util.NbBundle;

/**
 *
 * @author balek
 */
public class NotInitializedVariableTest extends ErrorHintsTestBase {

    public NotInitializedVariableTest(String name) {
        super(name);
    }

    public void testNotInitializedInDefaultConstructor() throws Exception {
        diagKey = "compiler.err.var.not.initialized.in.default.constructor"; // NOI18N
        performAnalysisTest("test/Test.java",
                       "package test;\n" +
                       "public class Test {\n" +
                       "    public final String |s;" +
                       "}\n",
                       "Initialize variable s", "Initialize variable s in constructor(s)", "Add parameter to constructor Test()");
    }

    public void testNotInitializedInDefaultConstructorInitialize() throws Exception {
        diagKey = "compiler.err.var.not.initialized.in.default.constructor"; // NOI18N
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "public class Test {\n" +
                       "    public final String |s;" +
                       "}\n",
                       "Initialize variable s",
                       "package test; public class Test { public final String s = null;} ");
    }

    public void testNotInitializedInDefaultConstructorInitializeInConstructor() throws Exception {
        diagKey = "compiler.err.var.not.initialized.in.default.constructor"; // NOI18N
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "public class Test {\n" +
                       "    public final String |s;" +
                       "}\n",
                       "Initialize variable s in constructor(s)",
                       "package test; public class Test { public final String s; public Test() { this.s = null; } } ");
    }

    public void testNotInitializedInConstructor() throws Exception {
        diagKey = "compiler.err.var.not.initialized.in.default.constructor"; // NOI18N
        performAnalysisTest("test/Test.java",
                       "package test;\n" +
                       "public class Test {\n" +
                       "    public final String |s;" +
                       "    public Test() {" +
                       "    }" +
                       "}\n",
                       "Initialize variable s", "Initialize variable s in constructor(s)", "Add parameter to constructor Test()");
    }

    public void testNotInitializedInConstructorInitialize() throws Exception {
        diagKey = "compiler.err.var.not.initialized.in.default.constructor"; // NOI18N
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "public class Test {\n" +
                       "    public final String |s;" +
                       "    public Test() {" +
                       "    }" +
                       "}\n",
                       "Initialize variable s",
                       "package test; public class Test { public final String s = null; public Test() { }} ");
    }

    public void testNotInitializedInConstructorInitializeInConstructor() throws Exception {
        diagKey = "compiler.err.var.not.initialized.in.default.constructor"; // NOI18N
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "public class Test {\n" +
                       "    public final String |s;" +
                       "    public Test() {" +
                       "    }" +
                       "}\n",
                       "Initialize variable s in constructor(s)",
                       "package test; public class Test { public final String s; public Test() {this.s = null; }} ");
    }

    public void testNotInitializedInMultipleConstructors() throws Exception {
        diagKey = "compiler.err.var.not.initialized.in.default.constructor"; // NOI18N
        performAnalysisTest("test/Test.java",
                       "package test;\n" +
                       "public class Test {\n" +
                       "    public final String |s;" +
                       "    public Test() {" +
                       "    }" +
                       "    public Test(int i) {" +
                       "    }" +
                       "}\n",
                       "Initialize variable s", "Initialize variable s in constructor(s)", "Add parameter to constructor Test()", "Add parameter to constructor Test(int i)");
    }

    public void testNotInitializedInMultipleConstructorsInitialize() throws Exception {
        diagKey = "compiler.err.var.not.initialized.in.default.constructor"; // NOI18N
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "public class Test {\n" +
                       "    public final String |s;" +
                       "    public Test() {" +
                       "    }" +
                       "    public Test(int i) {" +
                       "    }" +
                       "}\n",
                       "Initialize variable s",
                       "package test; public class Test { public final String s = null; public Test() { } public Test(int i) { }} ");
    }

    public void testNotInitializedInMultipleConstructorsInitializeInConstructor() throws Exception {
        diagKey = "compiler.err.var.not.initialized.in.default.constructor"; // NOI18N
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "public class Test {\n" +
                       "    public final String |s;" +
                       "    public Test() {" +
                       "    }" +
                       "    public Test(int i) {" +
                       "    }" +
                       "}\n",
                       "Initialize variable s in constructor(s)",
                       "package test; public class Test { public final String s; public Test() {this.s = null; } public Test(int i) {this.s = null; }} ");
    }

    public void testNotInitializedInMultipleConstructors2() throws Exception {
        diagKey = "compiler.err.var.not.initialized.in.default.constructor"; // NOI18N
        performAnalysisTest("test/Test.java",
                       "package test;\n" +
                       "public class Test {\n" +
                       "    public final String |s;" +
                       "    public Test() {" +
                       "        this.s = \"test\";" +
                       "    }" +
                       "    public Test(int i) {" +
                       "    }" +
                       "}\n",
                       "Initialize variable s", "Initialize variable s in constructor(s)", "Add parameter to constructor Test(int i)");
    }

    public void testNotInitializedInMultipleConstructors2Initialize() throws Exception {
        diagKey = "compiler.err.var.not.initialized.in.default.constructor"; // NOI18N
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "public class Test {\n" +
                       "    public final String |s;" +
                       "    public Test() {" +
                       "        this.s = \"test\";" +
                       "    }" +
                       "    public Test(int i) {" +
                       "    }" +
                       "}\n",
                       "Initialize variable s",
                       "package test; public class Test { public final String s = null; public Test() { this.s = \"test\"; } public Test(int i) { }} ");
    }

    public void testNotInitializedInMultipleConstructors2InitializeInConstructor() throws Exception {
        diagKey = "compiler.err.var.not.initialized.in.default.constructor"; // NOI18N
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "public class Test {\n" +
                       "    public final String |s;" +
                       "    public Test() {" +
                       "        this.s = \"test\";" +
                       "    }" +
                       "    public Test(int i) {" +
                       "    }" +
                       "}\n",
                       "Initialize variable s in constructor(s)",
                       "package test; public class Test { public final String s; public Test() { this.s = \"test\"; } public Test(int i) {this.s = null; }} ");
    }

    private String diagKey = "compiler.err.var.might.not.have.been.initialized"; // NOI18N

    @Override
    protected List<Fix> computeFixes(CompilationInfo info, int pos, TreePath path) throws Exception {
        return new NotInitializedVariable().run(info, diagKey, pos, path, null);
    }

    @Override
    protected String toDebugString(CompilationInfo info, Fix f) {
        return f.getText();
    }

    static {
        NbBundle.setBranding("test");
    }

}
