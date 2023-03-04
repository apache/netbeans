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

package org.netbeans.modules.java.completion;

/**
 *
 * @author Jan Lahoda, Dusan Balek
 */
public class JavaCompletionTaskElementCreatingTest extends CompletionTestBase {

    public JavaCompletionTaskElementCreatingTest(String testName) {
        super(testName);
    }

    public void testUnimplementedMethod() throws Exception {
        performTest("UnimplementedMethod", 894, "", "UnimplementedMethod.pass");
    }
    
    public void testOverrideAbstractList() throws Exception {
        performTest("OverrideAbstractList", 927, "", "OverrideAbstractList.pass");
    }
    
    /**
     * Checks that cc: offers just one size() for override, but offers size() for both implement AND override.
     */
    public void testOverrideAbstractListAbstract() throws Exception {
        performTest("OverrideAbstractListAbstract", 935, "", "OverrideAbstractListAbstract.pass");
    }
    
    /** CC should not offer overriding private method from superclass */
    public void testOverridePrivateMethod() throws Exception {
        performTest("OverridePrivateMethod", 898, "cl", "OverridePrivateMethod.pass");
    }
    
    /** CC should not offer overriding package private method from superclass in a different package */
    public void testOverridePackagePrivateMethod() throws Exception {
        performTest("OverridePackagePrivateMethod", 917, "add", "OverridePackagePrivateMethod.pass");
    }

    public void testOverrideAbstractListWithPrefix() throws Exception {
        performTest("OverrideAbstractList", 927, "to", "OverrideAbstractListWithPrefix.pass");
    }
    
    public void testOverrideFinalize() throws Exception {
        performTest("OverrideAbstractList", 927, "fin", "OverrideFinalize.pass");
    }
    
    public void testOverrideAbstractList2a() throws Exception {
        performTest("OverrideAbstractList2", 948, "ad", "OverrideAbstractList2a.pass");
    }
    
    public void testOverrideAbstractList2b() throws Exception {
        performTest("OverrideAbstractList2", 948, "ge", "OverrideAbstractList2b.pass");
    }
    
    public void testOverrideAbstractList3a() throws Exception {
        performTest("OverrideAbstractList3", 935, "ad", "OverrideAbstractList3a.pass");
    }
    
    public void testOverrideAbstractList3b() throws Exception {
        performTest("OverrideAbstractList3", 935, "ge", "OverrideAbstractList3b.pass");
    }
    
    public void testOverrideTypedException1() throws Exception {
        performTest("OverrideTypedException", 1018, "tes", "OverrideTypedException.pass");
    }
    
    public void testOverrideTypedException2() throws Exception {
        performTest("OverrideTypedException", 1114, "tes", "OverrideTypedException.pass");
    }
    
    public void testOverrideInInnerClass() throws Exception {
        performTest("OverrideInInnerClass", 994, "pai", "OverrideInInnerClass.pass");
    }
    
    public void testOverrideInInnerClassUnresolvable() throws Exception {
        performTest("OverrideInInnerClassUnresolvable", 966, "pai", "empty.pass");
    }
    
    public void testCreateConstructorTest() throws Exception {
        performTest("CreateConstructorTest", 1058, "", "CreateConstructorTest.pass");
    }

    public void testCreateConstructorTestInnerClass() throws Exception {
        performTest("CreateConstructorTest", 1243, "", "CreateConstructorTestInnerClass.pass");
    }

    public void testCreateConstructorWithConstructors() throws Exception {
        performTest("CreateConstructorWithConstructors", 1209, "", "CreateConstructorWithConstructors.pass");
    }

    public void testCreateConstructorWithConstructorsInnerClass() throws Exception {
        performTest("CreateConstructorWithConstructors", 1476, "", "CreateConstructorWithConstructorsInnerClass.pass");
    }

    public void testCreateConstructorWithDefaultConstructor() throws Exception {
        performTest("CreateConstructorWithDefaultConstructor", 1161, "", "CreateConstructorWithDefaultConstructor.pass");
    }

    public void testCreateConstructorWithDefaultConstructorInnerClass() throws Exception {
        performTest("CreateConstructorWithDefaultConstructor", 1369, "", "CreateConstructorWithDefaultConstructorInnerClass.pass");
    }

    public void testCreateConstructorNonDefaultConstructor() throws Exception {
        performTest("CreateConstructorNonDefaultConstructor", 1188, "", "CreateConstructorNonDefaultConstructor.pass");
    }

    public void testCreateConstructorNonDefaultConstructorInnerClass() throws Exception {
        performTest("CreateConstructorNonDefaultConstructor", 1433, "", "CreateConstructorNonDefaultConstructorInnerClass.pass");
    }
}
