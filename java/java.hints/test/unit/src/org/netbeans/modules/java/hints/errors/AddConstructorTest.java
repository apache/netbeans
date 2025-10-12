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
public class AddConstructorTest extends ErrorHintsTestBase {
    
    public AddConstructorTest(String name) {
        super(name, AddConstructor.class);
    }
    
    public void testSimple() throws Exception {
        performFixTest("test/Test.java",
                       "package test; public class Test extends A { } class A { A(String str) {} }",
                       -1,
                       "FIX_AddConstructor:Test(String)",
                       "package test; public class Test extends A { public Test(String str) { super(str); } } class A { A(String str) {} }");
    }
    
    public void testInaccessible() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test; public class Test extends A { } class A { private A(String str) {} }",
                            -1);
    }
    
    public void testMultipleVariants() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test; public class Test extends A { } class A { A(String str) {} A(int i) {} }",
                            -1,
                            "FIX_AddConstructor:Test(String)",
                            "FIX_AddConstructor:Test(int)");
    }

    static {
        NbBundle.setBranding("test");
    }

}
