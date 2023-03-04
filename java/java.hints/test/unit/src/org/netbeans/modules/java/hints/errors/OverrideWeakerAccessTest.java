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

/**
 *
 * @author Jan Lahoda
 */
public class OverrideWeakerAccessTest extends ErrorHintsTestBase {
    
    public OverrideWeakerAccessTest(String testName) {
        super(testName);
    }

    public void testSimple() throws Exception {
        performFixTest("test/Test.java",
                       "package test; public class Test { String |toString() { return null; } }",
                       Bundle.FIX_ChangeModifiers("toString", "public"),
                       "package test; public class Test { public String toString() { return null; } }");
    }
    
    public void testInterface() throws Exception {
        performFixTest("test/Test.java",
                       "package test; public class Test implements Runnable { void |run() { } }",
                       Bundle.FIX_ChangeModifiers("run", "public"),
                       "package test; public class Test implements Runnable { public void run() { } }");
    }
    
    public void testProtected() throws Exception {
        performFixTest("test/Test.java",
                       "package test; public class Test extends T { private void |run() { } } class T { protected void run() {} }",
                       Bundle.FIX_ChangeModifiers("run", "protected"),
                       "package test; public class Test extends T { protected void run() { } } class T { protected void run() {} }");
    }
    
    public void testDefault() throws Exception {
        performFixTest("test/Test.java",
                       "package test; public class Test extends T { private void |run() { } } class T { void run() {} }",
                       Bundle.FIX_DefaultAccess("run"),
                       "package test; public class Test extends T { void run() { } } class T { void run() {} }");
    }

    @Override
    protected List<Fix> computeFixes(CompilationInfo info, int pos, TreePath path) throws Exception {
        return new OverrideWeakerAccess().run(info, null, pos, path, null);
    }

    @Override
    protected String toDebugString(CompilationInfo info, Fix f) {
        return f.getText();
    }

}
