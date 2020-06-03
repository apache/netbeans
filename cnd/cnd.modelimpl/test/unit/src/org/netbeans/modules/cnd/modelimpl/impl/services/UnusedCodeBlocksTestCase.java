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

package org.netbeans.modules.cnd.modelimpl.impl.services;

import java.io.File;
import java.util.List;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.services.CsmFileInfoQuery;
import org.netbeans.modules.cnd.api.model.util.CsmTracer;
import org.netbeans.modules.cnd.modelimpl.trace.TraceModelTestBase;
import org.netbeans.modules.cnd.support.Interrupter;

/**
 * Tests for CsmFileInfoQuery.getUnusedCodeBlocks()
 */
public class UnusedCodeBlocksTestCase extends TraceModelTestBase {

    public UnusedCodeBlocksTestCase(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        System.setProperty("cnd.smart.parse", "true");
        super.setUp();
    }
    
    @Override
    protected void postTest(String[] args, Object... params) throws Exception {
        assertNotNull(params);
        assertTrue(params.length > 0);
        for (Object o : params)  {
            assertTrue(o instanceof String);
            File file = getDataFile((String) o);
            assertTrue(file.exists());
            CsmFile csmFile = super.getCsmFile(file);
            assertTrue(csmFile != null);
            System.out.printf("Unused blocks for %s\n", csmFile.getName());
            List<CsmOffsetable> blocks = CsmFileInfoQuery.getDefault().getUnusedCodeBlocks(csmFile, Interrupter.DUMMY);
            for (CsmOffsetable block : blocks) {
                System.out.printf("%s\n", CsmTracer.getOffsetString(block, false));
            }
        }
    }

    private void doTest(String fileToParse, String fileToCheck) throws Exception {
        doTest(new String[] { fileToParse }, fileToCheck, fileToCheck);
    }
    
    private void doTest(String fileToParse) throws Exception {
        doTest(new String[] { fileToParse }, fileToParse, fileToParse);
    }
    
    private void doTest(String[] filesToParse, String fileToCheck, String goldenNameBase) throws Exception {
        super.performTest(filesToParse, goldenNameBase, (Object) fileToCheck);
    }    
    
    public void testUnusedBlocksSimple() throws Exception {
        doTest("unused_blocks_simple.cc");
    }

    public void testSmartHeadersParse_1() throws Exception {
        doTest("smart_headers_parse_1.cc", "smart_headers_parse_1.h");
    }

    public void testSmartHeadersParse_2() throws Exception {
        doTest("smart_headers_parse_2.cc", "smart_headers_parse_2.h");
    }
}
