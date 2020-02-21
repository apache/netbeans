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

package org.netbeans.modules.cnd.completion;

import java.io.File;
import org.netbeans.junit.Manager;
import org.netbeans.modules.cnd.completion.cplusplus.ext.CompletionBaseTestCase;
import org.netbeans.modules.cnd.completion.cplusplus.ext.CompletionTestPerformer;
import org.netbeans.modules.cnd.completion.csm.CompletionResolver;

/**
 *
 *
 */
public class SmartCompletionInQuoteTestCase extends CompletionBaseTestCase {

    public SmartCompletionInQuoteTestCase(String name) {
        super(name, false); // we do not plan to modify or insert something in this test case
    }

    @Override 
    protected File getTestCaseDataDir() {
        return getQuoteDataDir();
    } 

    protected final File getQuoteDataDir() {
        return Manager.normalizeFile(new File(getDataDir(), "common/quote_nosyshdr"));
    }

    @Override
    protected CompletionTestPerformer createTestPerformer() {
        return new CompletionTestPerformer(CompletionResolver.QueryScope.SMART_QUERY);
    }

    public void testInCpuConstructorImpl() throws Exception {
        super.performTest("cpu.cc", 48, 9);
    }

    public void testInCpuComputeSupportMetricImplInExpr() throws Exception {
        super.performTest("cpu.cc", 58, 27);
    }

    public void testInCpuComputeSupportMetricImplInSwitch() throws Exception {
        super.performTest("cpu.cc", 60, 14);
    }

    public void testInCpuComputeSupportMetricImplInCase() throws Exception {
        super.performTest("cpu.cc", 61, 16);
    }

    public void testInCpuComputeSupportMetricImplInMethodCall() throws Exception {
        super.performTest("cpu.cc", 70, 7);
    }

    public void testInCpuComputeSupportMetricImplInMethodCallParam() throws Exception {
        super.performTest("cpu.cc", 70, 23);
    }

    public void testClassesInParameters() throws Exception {
        super.performTest("customer.cc", 57, 45);
    }

    public void testCCAfterSemicolon() throws Exception {
        super.performTest("quote.cc", 142, 28);
    }
}
