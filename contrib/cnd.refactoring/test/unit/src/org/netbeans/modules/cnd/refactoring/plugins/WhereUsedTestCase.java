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
package org.netbeans.modules.cnd.refactoring.plugins;

import java.util.Arrays;

/**
 *
 */
public class WhereUsedTestCase extends CsmWhereUsedQueryPluginTestCaseBase {

    public WhereUsedTestCase(String testName) {
        super(testName);
    }

    public void testIZ211703_1() throws Exception {
        // IZ#211703 : Find Usages can not find references to functions
        performWhereUsed("iz211703_1.c", 3, 20);
        performWhereUsed("iz211703_1.c", 5, 20);
        performWhereUsed("iz211703_1.c", 11, 20);
        performWhereUsed("iz211703_2.c", 4, 20);
        performWhereUsed("iz211703_2.c", 7, 20);
    }

    public void testIZ211703_2() throws Exception {
        // IZ#211703 : Find Usages can not find references to functions
        performWhereUsed("iz211703_1.c", 2, 30);
        performWhereUsed("iz211703_1.c", 12, 30);
        performWhereUsed("iz211703_1.c", 15, 30);
        performWhereUsed("iz211703_2.c", 3, 30);
        performWhereUsed("iz211703_2.c", 8, 30);
    }
    
    public void test219526_1() throws Exception {
        // IZ#219526 - incorrect Find Usages for the same qualified classifiers
        performWhereUsed("iz219526_1.cpp", 1, 20);
        performWhereUsed("iz219526_1.cpp", 3, 10);
    }
    
    public void test219526_2() throws Exception {
        // IZ#219526 - incorrect Find Usages for the same qualified classifiers
        performWhereUsed("iz219526_2.cpp", 1, 20);
        performWhereUsed("iz219526_2.cpp", 3, 10);
    }    
    
    public void test216130_1() throws Exception {
        // IZ#216130 Find usages: only references to the file where a global definition is defined are found
        performWhereUsed("iz216130.h", 6, 20);
        performWhereUsed("iz216130_2.c", 4, 20);
        
        performWhereUsed("iz216130_1.c", 12, 20);
        performWhereUsed("iz216130_2.c", 15, 20);
    }
    
    public void test216130_2() throws Exception {
        // IZ#216130 Find usages: only references to the file where a global definition is defined are found
        performWhereUsed("iz216130.h", 7, 20);
        performWhereUsed("iz216130_1.c", 5, 20);
        
        performWhereUsed("iz216130_1.c", 12, 40);
        performWhereUsed("iz216130_2.c", 15, 40);
    }

    public void test216130_3() throws Exception {
        // IZ#216130 Find usages: only references to the file where a global definition is defined are found
        performWhereUsed("iz216130_1.c", 7, 20);
        performWhereUsed("iz216130_2.c", 9, 20);
        
        performWhereUsed("iz216130_1.c", 12, 60);
        performWhereUsed("iz216130_2.c", 15, 60);
    }
    
    public void test216130_4() throws Exception {
        // IZ#216130 Find usages: only references to the file where a global definition is defined are found
        performWhereUsed("iz216130_1.c", 10, 20);
        performWhereUsed("iz216130_2.c", 12, 20);
        
        performWhereUsed("iz216130_1.c", 12, 80);
        performWhereUsed("iz216130_2.c", 15, 80);
    }
    
    public void test228094() throws Exception {
        // IZ#228094 - Refactoring: only usages are changed, #define in header from the refactoring was called, remains unchanged
        performWhereUsed("iz228094.cpp", 1, 10, null, Arrays.asList(CsmWhereUsedFilters.DECLARATIONS.getKey(), CsmWhereUsedFilters.MACROS.getKey(),CsmWhereUsedFilters.READ.getKey(),CsmWhereUsedFilters.WRITE.getKey(),CsmWhereUsedFilters.READ_WRITE.getKey()));
    }
    
    public void test268930_1() throws Exception {
        // Bug 268930 - C++11: user-defined literals
        performWhereUsed("bug268930.cpp", 2, 32);
    }
    
    public void test268930_2() throws Exception {
        // Bug 268930 - C++11: user-defined literals
        performWhereUsed("bug268930.cpp", 6, 25);
    }
    
    public void test268930_3() throws Exception {
        // Bug 268930 - C++11: user-defined literals
        performWhereUsed("bug268930.cpp", 10, 30);
    }
}
