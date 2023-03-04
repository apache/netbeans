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

import org.netbeans.modules.java.hints.infrastructure.HintsTestBase;
import org.netbeans.modules.java.hints.spiimpl.TestCompilerSettings;

/**
 *
 * @author Jan Lahoda
 */
public class SuppressWarningsFixerTest extends HintsTestBase {
    
    public SuppressWarningsFixerTest(String name) {
        super(name);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.doSetUp("org/netbeans/modules/java/hints/resources/layer.xml");
        TestCompilerSettings.commandLine = "-Xlint:deprecation -Xlint:fallthrough -Xlint:unchecked";
    }
    
    @Override
    protected boolean createCaches() {
        return false;
    }
    
    @Override
    protected String testDataExtension() {
        return "org/netbeans/test/java/hints/SuppressWarningsFixerTest/";
    }
    
    public void testSuppressWarnings1() throws Exception {
        performTest("Test", "unchecked", 8, 5);
    }
    
    public void testSuppressWarnings2() throws Exception {
        performTest("Test", "unchecked", 11, 5);
    }
    
    public void testSuppressWarnings3() throws Exception {
        performTest("Test", "unchecked", 16, 5);
    }
    
    public void testSuppressWarnings4() throws Exception {
        performTest("Test", "unchecked", 22, 5);
    }
    
    public void testSuppressWarnings5() throws Exception {
        performTest("Test", "unchecked", 28, 5);
    }
    
    public void testSuppressWarnings6() throws Exception {
        performTest("Test", "unchecked", 35, 5);
    }
    
    public void testSuppressWarnings7() throws Exception {
        performTest("Test2", "unchecked", 10, 5);
    }
    
    public void testSuppressWarnings8() throws Exception {
        performTest("Test2", "unchecked", 16, 5);
    }
    
    public void testSuppressWarnings9() throws Exception {
        performTest("Test2", "unchecked", 22, 5);
    }
    
    public void testSuppressWarnings10() throws Exception {
        performTestDoNotPerform("Test2", 31, 5);
    }
    
    public void testSuppressWarnings11() throws Exception {
        performTestDoNotPerform("Test2", 38, 5);
    }
    
    public void testSuppressWarnings106794() throws Exception {
	performTestDoNotPerform("Test3", 3, 10);
    }
    
}
