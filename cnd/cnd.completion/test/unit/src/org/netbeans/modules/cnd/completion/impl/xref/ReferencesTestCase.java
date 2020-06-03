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

package org.netbeans.modules.cnd.completion.impl.xref;

/**
 *
 *
 */
public class ReferencesTestCase extends ReferencesBaseTestCase {
    
    public ReferencesTestCase(String testName) {
        super(testName);
    }    

    public void testCpuH() throws Exception {
        performTest("cpu.h");
    }
    
    public void testCpuCC() throws Exception {
        performTest("cpu.cc");
    }

    public void testCustomerH() throws Exception {
        performTest("customer.h");
    }
    
    public void testCustomerCC() throws Exception {
        performTest("customer.cc");
    }
    
    public void testDiskH() throws Exception {
        performTest("disk.h");
    }
    
    public void testDiskCC() throws Exception {
        performTest("disk.cc");
    }
    
    public void testMemoryH() throws Exception {
        performTest("memory.h");
    }
    
    public void testMemoryCC() throws Exception {
        performTest("memory.cc");
    }
    
    public void testModuleH() throws Exception {
        performTest("module.h");
    }
    
    public void testModuleCC() throws Exception {
        performTest("module.cc");
    }
    
    public void testSystemH() throws Exception {
        performTest("system.h");
    }
    
    public void testSystemCC() throws Exception {
        performTest("system.cc");
    }
    
    public void testQuoteCC() throws Exception {
        performTest("quote.cc");
    }   
}
