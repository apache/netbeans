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
import java.io.PrintStream;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.apt.debug.APTTraceFlags;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.test.CndCoreTestUtils;

/**
 * Class for MacroExpansionDocProviderImpl tests for usages of macro parameters
 *
 */
public class MacroExpansionUsagesTestCase extends MacroExpansionDocProviderImplBaseTestCase {

    public MacroExpansionUsagesTestCase(String testName) {
        super(testName);
    }

    // Find usages of token on specified offset
    // Format:
    // performExpandFileTest("file name, line, column); // NOI18N

    public void testFile1() throws Exception {
        if (APTTraceFlags.USE_CLANK) {
            // there is no usages of parameters
            //performTest("file1.cc", 18, 7); // NOI18N
        } else {
            performTest("file1.cc", 18, 7); // NOI18N
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // general staff

    @Override
    protected void doTest(String[] args, PrintStream streamOut, PrintStream streamErr, Object... params) throws Exception {
        String path = args[0];
        FileImpl currentFile = getFileImpl(new File(path));

        assertNotNull("Csm file was not found for " + path, currentFile); // NOI18N

        if (params.length == 2) {

            MacroExpansionDocProviderImpl mp = new MacroExpansionDocProviderImpl();

            String objectSource = currentFile.getName().toString();

            BaseDocument doc = getBaseDocument(getDataFile(objectSource));
            assertNotNull(doc);

            CsmFile csmFile = CsmUtilities.getCsmFile(doc, false, false);
            mp.expand(doc, csmFile, 0, 0, true);

            int line = (Integer) params[0];
            int column = (Integer) params[1];

            int offset = CndCoreTestUtils.getDocumentOffset(doc, line, column);

            BaseDocument doc2 = (BaseDocument)createExpandedContextDocument(doc, currentFile);
            assertNotNull(doc2);
            mp.expand(doc, 0, doc.getLength(), doc2, new AtomicBoolean(false));
            int[][] res = mp.getUsages(doc2, offset);
            if (res != null) {
                streamOut.println("Usages: "); // NOI18N
                for (int i = 0; i < res.length; i++) {
                    int[] is = res[i];

                    streamOut.print("start offset: line " + getLine(doc2, is[0]) + " column " + getColumn(doc2, is[0])); // NOI18N
                    streamOut.println(" - end offset: line " + getLine(doc2, is[1]) + " column " + getColumn(doc2, is[1])); // NOI18N
                }
            }
        } else {
            assert true; // Bad test params
        }
    }

    private void performTest(String source, int line, int column) throws Exception {
        super.performTest(source, getName(), null, line, column);
    }

}
