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

package org.netbeans.modules.java.editor.completion;

/**
 *
 * @author Jan Lahoda, Dusan Balek
 */
public class JavaCompletionItemElementCreatingTest extends CompletionTestBase {

    public JavaCompletionItemElementCreatingTest(String testName) {
        super(testName);
    }

    public void testOverrideAbstractListWithPrefix() throws Exception {
        performTest("OverrideAbstractList", 118, "to", "toSt.*override", "OverrideAbstractListWithPrefix.pass2");
    }
    
    public void testOverrideFinalize() throws Exception {
        performTest("OverrideAbstractList", 118, "fin", "finali.*override", "OverrideFinalize.pass2");
    }
    
    public void testOverrideAbstractList2a() throws Exception {
        performTest("OverrideAbstractList2", 139, "ad", "add.*override", "OverrideAbstractList2a.pass2");
    }
    
    public void testOverrideAbstractList2b() throws Exception {
        performTest("OverrideAbstractList2", 139, "ge", "ge.*implement", "OverrideAbstractList2b.pass2");
    }
    
    public void testOverrideAbstractList3a() throws Exception {
        performTest("OverrideAbstractList3", 126, "ad", "add.*override", "OverrideAbstractList3a.pass2");
    }
    
    public void testOverrideAbstractList3b() throws Exception {
        performTest("OverrideAbstractList3", 126, "ge", "ge.*implement", "OverrideAbstractList3b.pass2");
    }
    
    public void testOverrideTypedException1() throws Exception {
        performTest("OverrideTypedException", 209, "tes", "tes.*override", "OverrideTypedException1.pass2");
    }
    
    public void testOverrideTypedException2() throws Exception {
        performTest("OverrideTypedException", 305, "tes", "tes.*override", "OverrideTypedException2.pass2");
    }
    
    public void testOverrideInInnerClass() throws Exception {
        performTest("OverrideInInnerClass", 185, "pai", "paint\\(.*override", "OverrideInInnerClass.pass2");
    }

    public void testOverrideInAnonClass() throws Exception {
        performTest("OverrideInAnonClass", 880, null, "toString\\(\\).*override", "OverrideInAnonClass.pass2");
    }

    public void testOverrideInAnonClass2() throws Exception {
        performTest("OverrideInAnonClass", 945, null, "toString\\(\\).*override", "OverrideInAnonClass2.pass2");
    }

    public void testOverrideInAnonInterfaceImpl() throws Exception {
        performTest("OverrideInAnonInterfaceImpl", 922, null, "toString\\(\\).*override", "OverrideInAnonInterfaceImpl.pass2");
    }

    public void testOverrideInAnonInterfaceImpl2() throws Exception {
        performTest("OverrideInAnonInterfaceImpl", 999, null, "toString\\(\\).*override", "OverrideInAnonInterfaceImpl2.pass2");
    }

    // Tests the fix for NETBEANS-252 / #271633
    public void testOverrideInEnumConstantBody() throws Exception {
        performTest("OverrideInEnumConstantBody", 901, null, "toString\\(\\).*override", "OverrideInEnumConstantBody.pass2");
    }
    public void testOverrideInEnumBody2() throws Exception {
        performTest("OverrideInEnumConstantBody", 935, null, "toString\\(\\).*override", "OverrideInEnumConstantBody2.pass2");
    }
    public void testOverrideInEnumBody3() throws Exception {
        performTest("OverrideInEnumConstantBody", 967, null, "toString\\(\\).*override", "OverrideInEnumConstantBody3.pass2");
    }
}
