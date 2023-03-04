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
import java.util.Set;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.infrastructure.ErrorHintsTestBase;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.util.NbBundle;

/**
 *
 * @author lahvac
 */
public class AddCatchFixTest extends ErrorHintsTestBase {
    
    public AddCatchFixTest(String testName) {
        super(testName);
    }

    public void test207480a() throws Exception {
        performFixTest("test/Test.java",
                       "package test;\nimport java.io.IOException;\npublic class Test {public void test() {try { test2(); } finally{ } } private void test2() throws IOException {} }",
                       -1,
                       "LBL_AddCatchClauses",
                       "package test; import java.io.IOException; import java.util.logging.Level; import java.util.logging.Logger; public class Test {public void test() {try { test2(); } catch (IOException ex) { Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex); } finally{ } } private void test2() throws IOException {} }");
    }

    protected List<Fix> computeFixes(CompilationInfo info, int pos, TreePath path) throws Exception {
        return new UncaughtException().run(info, null, pos, path, null);
    }

    @Override
    protected Set<String> getSupportedErrorKeys() {
        return new UncaughtException().getCodes();
    }

    @Override
    protected String toDebugString(CompilationInfo info, Fix f) {
        return f.getText();
    }

    static {
        NbBundle.setBranding("test");
    }
}
