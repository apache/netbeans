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

package org.netbeans.modules.cnd.completion.cplusplus.hyperlink;

/**
 *
 *
 */
public class UnnamedEnumTestCase extends HyperlinkBaseTestCase {
    
    public UnnamedEnumTestCase(String testName) {
        super(testName);
        //System.setProperty("cnd.modelimpl.trace.registration", "true");
    }

    public void testFOUR() throws Exception {
        performTest("newfile.cc", 3, 15, "newfile.cc", 3, 14);
        performTest("newfile.cc", 7, 14, "newfile.cc", 3, 14);
    }
    
    public void testONE() throws Exception {
        performTest("newfile.cc", 6, 14, "newfile.h", 1, 7);
    }

    public void testExecutionContextT() throws Exception {
        performTest("unnamedTypedefEnum.cc", 6, 20, "unnamedTypedefEnum.cc", 6, 17); // k_eExecutionContextSystemTask
        performTest("unnamedTypedefEnum.cc", 7, 20, "unnamedTypedefEnum.cc", 7, 17); // k_eExecutionContextMPTask
        performTest("unnamedTypedefEnum.cc", 13, 60, "unnamedTypedefEnum.cc", 6, 17); // k_eExecutionContextSystemTask
        performTest("unnamedTypedefEnum.cc", 16, 60, "unnamedTypedefEnum.cc", 7, 17); // k_eExecutionContextMPTask
    }
    
    public void testA() throws Exception {
        performTest("unnamedTypedefEnum.cc", 25, 6, "unnamedTypedefEnum.cc", 25, 5); // A1
    }    
}
