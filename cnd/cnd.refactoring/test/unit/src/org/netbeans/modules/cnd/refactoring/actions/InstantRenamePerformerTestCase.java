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

package org.netbeans.modules.cnd.refactoring.actions;

import java.io.File;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.refactoring.test.RefactoringBaseTestCase;

/**
 *
 */
public class InstantRenamePerformerTestCase extends RefactoringBaseTestCase {

    public InstantRenamePerformerTestCase(String testName) {
        super(testName);
    }

    @Override 
    protected File getTestCaseDataDir() {
        return getQuoteDataDir();
    }

    public void testAllow() throws Exception {
        performInstantRenameAvailable("quote.cc", 53, 25, true); // customers in list<Customer> customers;
        performInstantRenameAvailable("quote.cc", 55, 20, true); // void outCustomersList() {
        performInstantRenameAvailable("quote.cc", 56, 40, true); // customers in customers.size()
        performInstantRenameAvailable("quote.cc", 59, 39, true); // it in for
        performInstantRenameAvailable("quote.cc", 59, 63, true); // it in for
        performInstantRenameAvailable("quote.cc", 59, 88, true); // it in for
        performInstantRenameAvailable("quote.cc", 60, 24, true); // it in for body
    }
    
    public void testNotAllow() throws Exception {
        performInstantRenameAvailable("quote.cc", 60, 15, false); // cout
        performInstantRenameAvailable("quote.cc", 60, 32, false); // endl
        performInstantRenameAvailable("quote.cc", 70, 32, false); // Customer
    }
    
    public void performInstantRenameAvailable(String source, int line, int column, boolean goldenResult) throws Exception {
        CsmReference ref = super.getReference(source, line, column);
        assertNotNull(ref);

        boolean result = InstantRenamePerformer.allowInstantRename(ref);
        assertEquals(goldenResult, result);
    }
}
