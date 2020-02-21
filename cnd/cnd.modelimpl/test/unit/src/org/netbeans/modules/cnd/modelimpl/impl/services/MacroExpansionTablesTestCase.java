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
import javax.swing.text.Document;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.test.CndCoreTestUtils;

/**
 * Class for MacroExpansionDocProviderImpl tests for transformation tables
 *
 */
public class MacroExpansionTablesTestCase extends MacroExpansionDocProviderImplBaseTestCase {

    public MacroExpansionTablesTestCase(String testName) {
        super(testName);
    }

    // Dump tables
    // Format:
    // performExpandFileTest("file name"); // NOI18N
    // or
    // performExpandFileTest("file name, start_line, start_column, end_line, end_column); // NOI18N

    public void testFile1() throws Exception {
        performDumpTablesTest("file1.cc"); // NOI18N
    }

    public void testFile1_2() throws Exception {
        performDumpTablesTest("file1.cc", 10, 1, 15, 1); // NOI18N
    }

    ////////////////////////////////////////////////////////////////////////////
    // general staff

    @Override
    protected void doTest(String[] args, PrintStream streamOut, PrintStream streamErr, Object... params) throws Exception {
        String path = args[0];
        FileImpl currentFile = getFileImpl(new File(path));

        assertNotNull("Csm file was not found for " + path, currentFile); // NOI18N

        if (params.length == 0) {
            // Dump tables

            MacroExpansionDocProviderImpl mp = new MacroExpansionDocProviderImpl();

            String objectSource = currentFile.getName().toString();

            BaseDocument doc = getBaseDocument(getDataFile(objectSource));
            assertNotNull(doc);

            CsmFile csmFile = CsmUtilities.getCsmFile(doc, false, false);
            mp.expand(doc, csmFile, 0, 0, true);

            String res = mp.dumpTables(doc);
            assertNotNull(res);
            streamOut.println(res);

        } else if (params.length == 4) {
            // Dump tables

            MacroExpansionDocProviderImpl mp = new MacroExpansionDocProviderImpl();

            String objectSource = currentFile.getName().toString();

            BaseDocument doc = getBaseDocument(getDataFile(objectSource));
            assertNotNull(doc);

            CsmFile csmFile = CsmUtilities.getCsmFile(doc, false, false);
            mp.expand(doc, csmFile, 0, 0, true);

            String res = mp.dumpTables(doc);
            assertNotNull(res);
            streamOut.println(res);

            int startLine = (Integer) params[0];
            int startColumn = (Integer) params[1];

            int endLine = (Integer) params[2];
            int endColumn = (Integer) params[3];

            int startOffset = CndCoreTestUtils.getDocumentOffset(doc, startLine, startColumn);
            int endOffset = CndCoreTestUtils.getDocumentOffset(doc, endLine, endColumn);

            Document doc2 = createExpandedContextDocument(doc, currentFile);
            assertNotNull(doc2);
            mp.expand(doc, startOffset, endOffset, doc2, new AtomicBoolean(false));
            res = mp.dumpTables(doc2);
            streamOut.println(res);

        } else {
            assert true; // Bad test params
        }
    }

    private void performDumpTablesTest(String source) throws Exception {
        super.performTest(source, getName(), null);
    }

    private void performDumpTablesTest(String source, int startLine, int startColumn, int endLine, int endColumn) throws Exception {
        super.performTest(source, getName(), null, startLine, startColumn, endLine, endColumn);
    }


}
