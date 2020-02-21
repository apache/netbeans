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
 * Class for MacroExpansionDocProviderImpl tests for offsets transformations
 *
 */
public class MacroExpansionOffsetsTestCase extends MacroExpansionDocProviderImplBaseTestCase {

    public MacroExpansionOffsetsTestCase(String testName) {
        super(testName);
    }

    // Find offset in expanded view by original or vice versa
    // Format:
    // performGetOutOffsetTest("file name, line, column); // NOI18N
    // or
    // performGetInOffsetTest("file name, line, column); // NOI18N

    public void testFile1() throws Exception {
        performGetOutOffsetTest("file1.cc", 11, 5); // NOI18N
    }

    public void testFile1_2() throws Exception {
        performGetInOffsetTest("file1.cc", 9, 5); // NOI18N
    }

    ////////////////////////////////////////////////////////////////////////////
    // general staff

    @Override
    protected void doTest(String[] args, PrintStream streamOut, PrintStream streamErr, Object... params) throws Exception {
        String path = args[0];
        FileImpl currentFile = getFileImpl(new File(path));

        assertNotNull("Csm file was not found for " + path, currentFile); // NOI18N

        if (params.length == 3) {

            MacroExpansionDocProviderImpl mp = new MacroExpansionDocProviderImpl();

            String objectSource = currentFile.getName().toString();

            BaseDocument doc = getBaseDocument(getDataFile(objectSource));
            assertNotNull(doc);

            CsmFile csmFile = CsmUtilities.getCsmFile(doc, false, false);
            mp.expand(doc, csmFile, 0, 0, true);

            int line = (Integer) params[0];
            int column = (Integer) params[1];

            boolean originalToExpanded = (Boolean) params[2];

            Document doc2 = createExpandedContextDocument(doc, currentFile);
            assertNotNull(doc2);
            mp.expand(doc, 0, doc.getLength(), doc2, new AtomicBoolean(false));

            int res = 0;
            if(originalToExpanded) {
                int offset = CndCoreTestUtils.getDocumentOffset(doc, line, column);
                res = mp.getOffsetInExpandedText(doc2, offset);
                assertNotNull(res);
                streamOut.println("Offset: line " + getLine((BaseDocument)doc2, res) + " column " + getColumn((BaseDocument)doc2, res)); // NOI18N
            } else {
                int offset = CndCoreTestUtils.getDocumentOffset((BaseDocument)doc2, line, column);
                res = mp.getOffsetInOriginalText(doc2, offset);
                assertNotNull(res);
                streamOut.println("Offset: line " + getLine(doc, res) + " column " + getColumn(doc, res)); // NOI18N
            }

        } else {
            assert true; // Bad test params
        }
    }

    private void performGetOutOffsetTest(String source, int originalLine, int originalColumn) throws Exception {
        super.performTest(source, getName(), null, originalLine, originalColumn, true);
    }
    
    private void performGetInOffsetTest(String source, int expandedLine, int expandedColumn) throws Exception {
        super.performTest(source, getName(), null, expandedLine, expandedColumn, false);
    }
}
