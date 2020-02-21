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


package org.netbeans.modules.cnd.modelimpl.csm.core;

import java.io.File;
import java.util.Collection;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.modelimpl.trace.TraceModelTestBase;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;

/**
 * test for line-col/offset converting
 */
public class FileImplOffsetsTest extends TraceModelTestBase {

    public FileImplOffsetsTest(String testName) {
        super(testName);
    }

    public void testConverting() throws Exception {
        performOffsetsTest("dummy.cc");
    }
    
    private void performOffsetsTest(String source) throws Exception {
        File testFile = getDataFile(source);
        assertTrue("File not found "+testFile.getAbsolutePath(),testFile.exists());        
        super.performModelTest(testFile, System.out, System.err);
        FileImpl file = getProject().getFile(CndFileUtils.normalizeFile(testFile).getAbsolutePath(), true);
        assertNotNull("csm file not found for " + testFile.getAbsolutePath(), file);
        checkFileOffsetsConverting(file);
    }
    
    private void checkFileOffsetsConverting(final FileImpl file) {
        Collection<CsmOffsetableDeclaration> decls = file.getDeclarations();
        assertEquals(decls.size(), 4);
        for (CsmOffsetableDeclaration csmOffsetableDeclaration : decls) {
            checkOffsetConverting(file, csmOffsetableDeclaration.getStartPosition());
            checkOffsetConverting(file, csmOffsetableDeclaration.getEndPosition());
            checkLineColumnConverting(file, csmOffsetableDeclaration.getStartPosition());
            checkLineColumnConverting(file, csmOffsetableDeclaration.getEndPosition());
        }
    }
    
    private void checkOffsetConverting(FileImpl file, CsmOffsetable.Position pos) {
        int offset = pos.getOffset();
        int[] lineCol = file.getLineColumn(offset);
        assertEquals("different lines", pos.getLine(), lineCol[0]);
        assertEquals("different columns", pos.getColumn(), lineCol[1]);
    }

    private void checkLineColumnConverting(FileImpl file, CsmOffsetable.Position pos) {
        int offset = file.getOffset(pos.getLine(), pos.getColumn());
        assertEquals("different offset for " + pos, pos.getOffset(), offset);
    }
}
