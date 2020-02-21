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

package org.netbeans.modules.cnd.refactoring;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.modules.cnd.refactoring.actions.GlobalRenamePerformerTestCase;
import org.netbeans.modules.cnd.refactoring.actions.InstantRenamePerformerTestCase;
import org.netbeans.modules.cnd.refactoring.hints.IntroduceVariable2TestCase;
import org.netbeans.modules.cnd.refactoring.hints.IntroduceVariableTestCase;
import org.netbeans.modules.cnd.refactoring.plugins.WhereUsedFiltersTestCase;
import org.netbeans.modules.cnd.refactoring.plugins.WhereUsedInQuoteTestCase;
import org.netbeans.modules.cnd.refactoring.plugins.WhereUsedTestCase;
import org.netbeans.modules.cnd.test.CndBaseTestSuite;

/**
 *
 */
public class RefactoringTest extends CndBaseTestSuite {
    
    private RefactoringTest() {
        super("C/C++ Refactoring Test"); // NOI18N
        
        addTestSuite(InstantRenamePerformerTestCase.class);
        addTestSuite(GlobalRenamePerformerTestCase.class);
        addTestSuite(WhereUsedInQuoteTestCase.class);
        addTestSuite(WhereUsedTestCase.class);
        addTestSuite(WhereUsedFiltersTestCase.class);
        addTestSuite(IntroduceVariableTestCase.class);
        addTestSuite(IntroduceVariable2TestCase.class);
    }

    public static Test suite() {
        TestSuite suite = new RefactoringTest();
        return suite;
    }

}
