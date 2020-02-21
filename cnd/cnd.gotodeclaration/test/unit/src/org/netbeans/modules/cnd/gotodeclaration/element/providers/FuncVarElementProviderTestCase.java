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

package org.netbeans.modules.cnd.gotodeclaration.element.providers;

import java.io.File;
import org.netbeans.spi.jumpto.type.SearchType;

/**
 */
public class FuncVarElementProviderTestCase extends CppSymbolBaseTestCase {

    public FuncVarElementProviderTestCase(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        System.setProperty("cnd.modelimpl.tracemodel.project.name", "DummyProject"); // NOI18N
        super.setUp();
    }

    @Override
    protected File getTestCaseDataDir() {
        return getQuoteDataDir();
    }

   public void testFuncVarAllRegexp() throws Exception {
        peformTest("*", SearchType.REGEXP);
    }

    public void testFuncVarDotRegexp() throws Exception {
        peformTest("ma?n", SearchType.REGEXP);
    }

    public void testFuncVarCaseInsensitiveRegexp() throws Exception {
        peformTest("MA?*n", SearchType.CASE_INSENSITIVE_REGEXP);
    }
    
    public void testFuncVarCamelCase() throws Exception {
        peformTest("GC", SearchType.CAMEL_CASE);
    }

    public void testFuncVarPrefix() throws Exception {
        peformTest("ope", SearchType.PREFIX);
    }

    public void testFuncVarCaseInsensitivePrefix() throws Exception {
        peformTest("OpE", SearchType.CASE_INSENSITIVE_PREFIX);
    }

    public void testFuncVarExactName() throws Exception {
        peformTest("main", SearchType.EXACT_NAME);
    }

    public void testFuncVarCaseInsensitiveExactName() throws Exception {
        peformTest("Main", SearchType.CASE_INSENSITIVE_EXACT_NAME);
    }

    public void testMacroDotRegexp() throws Exception {
        peformTest("CPU?H", SearchType.REGEXP);
    }

    public void testMacroCaseInsensitiveRegexp() throws Exception {
        peformTest("_cUs?*r_h", SearchType.CASE_INSENSITIVE_REGEXP);
    }

    public void testMacroCamelCase() throws Exception {
        peformTest("GD", SearchType.CAMEL_CASE);
    }

    public void testMacroPrefix() throws Exception {
        peformTest("M", SearchType.PREFIX);
    }

    public void testMacroCaseInsensitivePrefix() throws Exception {
        peformTest("m", SearchType.CASE_INSENSITIVE_PREFIX);
    }

    public void testMacroExactName() throws Exception {
        peformTest("DISK_H", SearchType.EXACT_NAME);
    }

    public void testMacroCaseInsensitiveExactName() throws Exception {
        peformTest("disk_h", SearchType.CASE_INSENSITIVE_EXACT_NAME);
    }
}
