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

import java.util.List;

import com.sun.source.util.TreePath;

import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.errors.RenameConstructor.RenameConstructorFix;
import org.netbeans.modules.java.hints.infrastructure.ErrorHintsTestBase;
import org.netbeans.spi.editor.hints.Fix;

/**
 *
 * @author Dusan Balek
 */
public class RenameConstructorTest extends ErrorHintsTestBase {

    public RenameConstructorTest(String name) {
        super(name);
    }

    public void testSimple() throws Exception {
        performFixTest("test/Test.java",
                "package test; public class Test {public |test() {}}",
                "[RenameConstructorFix:test:Test]",
                "package test; public class Test {public Test() {}}");
    }

    public void testNonClashing() throws Exception {
        performFixTest("test/Test.java",
                "package test; public class Test {public |test() {} public Test(int i) {}}",
                "[RenameConstructorFix:test:Test]",
                "package test; public class Test {public Test() {} public Test(int i) {}}");
    }

    public void testSimpleClashing() throws Exception {
        performAnalysisTest("test/Test.java",
                "package test; public class Test {public |test() {} public Test() {}}");
    }

    public void testClashing() throws Exception {
        performAnalysisTest("test/Test.java",
                "package test; public class Test {public |test(java.util.List<String> l) {} public Test(java.util.List<Number> l) {}}");
    }

    @Override
    protected List<Fix> computeFixes(CompilationInfo info, int pos, TreePath path) throws Exception {
        return new RenameConstructor().run(info, null, pos, path, null);
    }

    @Override
    protected String toDebugString(CompilationInfo info, Fix f) {
        if (f instanceof RenameConstructorFix) {
            return ((RenameConstructorFix) f).toDebugString();
        }
        return super.toDebugString(info, f);
    }
}
