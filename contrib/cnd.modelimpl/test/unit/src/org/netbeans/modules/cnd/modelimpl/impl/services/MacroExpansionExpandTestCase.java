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
import org.netbeans.modules.cnd.spi.model.services.CsmMacroExpansionDocProvider;
import org.netbeans.modules.cnd.test.CndCoreTestUtils;

/**
 * Tests for expand(...) MacroExpansionDocProviderImpl
 *
 */
public class MacroExpansionExpandTestCase extends MacroExpansionDocProviderImplBaseTestCase {

    public MacroExpansionExpandTestCase(String testName) {
        super(testName);
    }

    // Whole file expansion tests
    // Format:
    // performExpandFileTest("file name"); // NOI18N

    public void testFile1() throws Exception {
        performExpandFileTest("file1.cc"); // NOI18N
    }


    // Part of file expansion tests
    // Format:
    // performExpandFileTest("file name", start_line, start_column, end_line, end_column); // NOI18N

    public void testFile1_2() throws Exception {
        performExpandFileTest("file1.cc", 5, 13, 5, 21); // NOI18N
    }


    // Document expansion tests
    // Format:
    // performExpandDocTest("file name"); // NOI18N

    public void testDoc1() throws Exception {
        performExpandDocTest("file1.cc"); // NOI18N
    }


    // Document expansion tests
    // Format:
    // performExpandDocTest("file name", start_line, start_column, end_line, end_column); // NOI18N

    public void testDoc1_2() throws Exception {
        performExpandDocTest("file1.cc", 10, 1, 15, 1); // NOI18N
    }


    // Code expansion tests
    // Format:
    // performTest("file name", line, column, "code for expansion"); // NOI18N

    public void testCodeExpansion() throws Exception {
        performExpandCodeTest("file1.cc", 10, 13, "CONSTANT + 1"); // NOI18N
    }
    
    public void testCodeWithCommentExpansion() throws Exception {
        // IZ 207378
        performExpandCodeTest("file1.cc", 10, 13, "CONSTANT /*comment*/ + 1"); // NOI18N
    }

    public void testIZ175801() throws Exception {
        // IZ#175801 : Preprocessor #define not handled properly
        performExpandFileTest("iz175801.cc");
    }

    ////////////////////////////////////////////////////////////////////////////
    // general staff

    @Override
    protected void doTest(String[] args, PrintStream streamOut, PrintStream streamErr, Object... params) throws Exception {
        String path = args[0];
        FileImpl currentFile = getFileImpl(new File(path));

        assertNotNull("Csm file was not found for " + path, currentFile); // NOI18N

        if (params.length == 1) {
            // Test whole file

            CsmMacroExpansionDocProvider mp = new MacroExpansionDocProviderImpl();

            String objectSource = currentFile.getName().toString();

            BaseDocument doc = getBaseDocument(getDataFile(objectSource));
            assertNotNull(doc);
            int startOffset = 0;
            int endOffset = doc.getLength();

            boolean expandDoc = (Boolean) params[0];

            if (expandDoc) {
                Document doc2 = createExpandedContextDocument(doc, currentFile);
                assertNotNull(doc2);
                int expNum = mp.expand(doc, startOffset, endOffset, doc2, new AtomicBoolean(false));
                streamOut.println("Number of expansions: " + expNum); // NOI18N
                String res = doc2.getText(0, doc2.getLength());
                assertNotNull(doc);
                streamOut.println(res);
            } else {
                CsmFile csmFile = CsmUtilities.getCsmFile(doc, false, false);
                String res = mp.expand(doc, csmFile, startOffset, endOffset, true);
                assertNotNull(res);

                streamOut.println(res);
            }

        } else if (params.length == 5) {
            // Test part of file

            CsmMacroExpansionDocProvider mp = new MacroExpansionDocProviderImpl();

            String objectSource = currentFile.getName().toString();

            int startLine = (Integer) params[0];
            int startColumn = (Integer) params[1];

            int endLine = (Integer) params[2];
            int endColumn = (Integer) params[3];

            BaseDocument doc = getBaseDocument(getDataFile(objectSource));
            assertNotNull(doc);
            int startOffset = CndCoreTestUtils.getDocumentOffset(doc, startLine, startColumn);
            int endOffset = CndCoreTestUtils.getDocumentOffset(doc, endLine, endColumn);

            boolean expandDoc = (Boolean) params[4];

            if (expandDoc) {
                Document doc2 = createExpandedContextDocument(doc, currentFile);
                assertNotNull(doc2);
                int expNum = mp.expand(doc, startOffset, endOffset, doc2, new AtomicBoolean(false));
                streamOut.println("Number of expansions: " + expNum); // NOI18N
                String res = doc2.getText(0, doc2.getLength());
                assertNotNull(doc);
                streamOut.println(res);
            } else {
                CsmFile csmFile = CsmUtilities.getCsmFile(doc, false, false);
                String res = mp.expand(doc, csmFile, startOffset, endOffset, true);
                assertNotNull(res);
                streamOut.println(res);
            }
        } else if (params.length == 3) {
            // Test expansion of code in specified context

            CsmMacroExpansionDocProvider mp = new MacroExpansionDocProviderImpl();

            String objectSource = currentFile.getName().toString();

            int line = (Integer) params[0];
            int column = (Integer) params[1];

            String code = (String) params[2];

            BaseDocument doc = getBaseDocument(getDataFile(objectSource));
            assertNotNull(doc);
            int offset = CndCoreTestUtils.getDocumentOffset(doc, line, column);

            String res = mp.expand(doc, offset, code);
            assertNotNull(res);

            streamOut.println(res);
        } else {
            assert true; // Bad test params
        }
    }


    private void performExpandFileTest(String source, int startLine, int startColumn, int endLine, int endColumn) throws Exception {
        super.performTest(source, getName(), null, startLine, startColumn, endLine, endColumn, false);
    }

    private void performExpandDocTest(String source, int startLine, int startColumn, int endLine, int endColumn) throws Exception {
        super.performTest(source, getName(), null, startLine, startColumn, endLine, endColumn, true);
    }

    private void performExpandCodeTest(String source, int line, int column, String code) throws Exception {
        super.performTest(source, getName(), null, line, column, code);
    }

    private void performExpandFileTest(String source) throws Exception {
        super.performTest(source, getName(), null, false);
    }

    private void performExpandDocTest(String source) throws Exception {
        super.performTest(source, getName(), null, true);
    }

}
