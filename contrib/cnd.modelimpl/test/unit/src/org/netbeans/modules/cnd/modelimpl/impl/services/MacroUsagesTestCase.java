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
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.services.CsmFileInfoQuery;
import org.netbeans.modules.cnd.api.model.util.CsmTracer;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.modelimpl.trace.TraceModelTestBase;
import org.netbeans.modules.cnd.support.Interrupter;

/**
 *
 */
public class MacroUsagesTestCase extends TraceModelTestBase {

    public MacroUsagesTestCase(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
//        System.setProperty("cnd.smart.parse", "true");
        super.setUp();
    }

    @Override
    protected void postTest(String[] args, Object... params) throws Exception {
        assertNotNull(params);
        assertTrue(params.length > 0);
        for (Object o : params) {
            assertTrue(o instanceof String);
            File file = getDataFile((String) o);
            assertTrue(file.exists());
            CsmFile csmFile = super.getCsmFile(file);
            assertTrue(csmFile != null);
            System.out.printf("Macro references for %s\n", csmFile.getName());
            List<CsmReference> macroRefs = CsmFileInfoQuery.getDefault().getMacroUsages(csmFile, null, Interrupter.DUMMY);
            for (CsmReference ref : macroRefs) {
                CsmObject refedObj = ref.getReferencedObject();
                System.out.printf("%s %s -> %s %s\n", ref.getText(), CsmTracer.getOffsetString(ref, false), ref.getKind(), CsmTracer.getOffsetString(refedObj, true));
            }
        }
    }

    private void doTest(String fileToParse) throws Exception {
        doTest(new String[]{fileToParse}, fileToParse, fileToParse);
    }

    private void doTest(String[] filesToParse, String fileToCheck, String goldenNameBase) throws Exception {
        super.performTest(filesToParse, goldenNameBase, (Object) fileToCheck);
    }

    public void testMacroUsagesSimple() throws Exception {
        doTest("macro_refs_simple.cc");
    }

    public void DISABLED_testMacroUsages210815() throws Exception {
        // #210815 - used macros is not highlighted in editor
        doTest(new String[]{"macro_refs_parse_1.cc", "macro_refs_parse_2.cc"}, "macro_refs_headers_parse.h", "macro_refs_headers_parse.h");
    }
}
