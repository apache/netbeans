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
import java.util.Arrays;
import java.util.Collection;
import javax.swing.text.BadLocationException;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;

/**
 *
 */
public class RestoreHandlerTestCase extends ModifyDocumentTestCaseBase {
    private static final boolean TRACE = false;
    static {
//        System.setProperty("apt.use.clank", "true");
//        System.setProperty("cnd.modelimpl.parser.threads", "1");
        System.setProperty("cnd.modelimpl.timing", "true");
        System.setProperty("cnd.modelimpl.timing.per.file.flat", "true");

    }

    public RestoreHandlerTestCase(String testName) {
        super(testName);
    }

    public void test_iz_255449() throws Exception {
        final File fSrc = getDataFile("iz_255449.c");
        final File fHdrA = getDataFile("iz_255449_a.h");
        final File fHdrB = getDataFile("iz_255449_b.h");
        waitAllProjectsParsed();
        final FileImpl src = (FileImpl) super.getCsmFile(fSrc);
        final FileImpl hdrA = (FileImpl) super.getCsmFile(fHdrA);
        final FileImpl hdrB = (FileImpl) super.getCsmFile(fHdrB);

        
        Collection<CsmOffsetableDeclaration> declsB = hdrB.getDeclarations();
        assertEquals("Declarations count", 1, declsB.size());
        CsmOffsetableDeclaration declB = declsB.iterator().next();
        assertEquals("Declaration name", "struct_255449_b", declB.getName().toString());

        Collection<CsmOffsetableDeclaration> declsA = hdrA.getDeclarations();
        assertEquals("Declarations count", 1, declsB.size());
        CsmOffsetableDeclaration declA = declsA.iterator().next();
        assertEquals("Declaration name", "struct_255449_a", declA.getName().toString());

        Collection<PreprocessorStatePair> ppPairsA = ((ProjectBase)getProject()).getFileContainerStatePairsToDump(hdrA.getAbsolutePath());
        Collection<PreprocessorStatePair> ppPairsB = ((ProjectBase)getProject()).getFileContainerStatePairsToDump(hdrB.getAbsolutePath());
        if (TRACE) {
            System.err.printf("StateA:\n%s\n", Arrays.toString(ppPairsA.toArray()));
            System.err.printf("StateB:\n%s\n", Arrays.toString(ppPairsB.toArray()));
        }
        System.err.printf("Modifying cpp; Parse count == %d\n", FileImpl.getParseCount());
        //int parseCount1 = FileImpl.getParseCount();
//        replaceText(fSrc, "\\#include \"iz_255449_a.h\"\n#define DEF\n\\#include \"iz_255449_b.h\"\n", true);
        replaceText(fSrc, "//#include \"iz_255449_a.h\"\n#define DEF\n#include \"iz_255449_b.h\"\n", true);
        //int parseCount2 = FileImpl.getParseCount();
        
        waitAllProjectsParsed();
        
        declsB = hdrB.getDeclarations();
        assertEquals("Declarations count", 1, declsB.size());
        declB = declsB.iterator().next();
        assertEquals("Declaration name", "struct_255449_b", declB.getName().toString());
        
        declsA = hdrA.getDeclarations();
        assertEquals("Declarations count", 1, declsB.size());
        declA = declsA.iterator().next();
        assertEquals("Declaration name", "struct_255449_a", declA.getName().toString());
        
        Collection<PreprocessorStatePair> ppPairsA_1 = ((ProjectBase)getProject()).getFileContainerStatePairsToDump(hdrA.getAbsolutePath());
        Collection<PreprocessorStatePair> ppPairsB_1 = ((ProjectBase)getProject()).getFileContainerStatePairsToDump(hdrB.getAbsolutePath());
        if (TRACE) {
            System.err.printf("StateA_1:\n%s\n", Arrays.toString(ppPairsA_1.toArray()));
            System.err.printf("StateB_1:\n%s\n", Arrays.toString(ppPairsB_1.toArray()));
        }
        
        System.err.printf("Modifying cpp done; Parse count == %d\n", FileImpl.getParseCount());

        //assertEquals("Parse count", parseCount1 + 2, parseCount2);
        //sleep(1000);

        System.err.printf("Modifying hdr; Parse count == %d\n", FileImpl.getParseCount());
        modifyText(fHdrA, new DocumentModifier() {
            @Override
            public void modify(BaseDocument doc) throws BadLocationException {
                doc.insertString(doc.getLength(), "\n // just a comment \n", null);
            }
        }, true);
        System.err.printf("Modifying hdr done; Parse count == %d\n", FileImpl.getParseCount());

        waitAllProjectsParsed();

        Collection<PreprocessorStatePair> ppPairsA_2 = ((ProjectBase)getProject()).getFileContainerStatePairsToDump(hdrA.getAbsolutePath());
        Collection<PreprocessorStatePair> ppPairsB_2 = ((ProjectBase)getProject()).getFileContainerStatePairsToDump(hdrB.getAbsolutePath());
        if (TRACE) {
            System.err.printf("StateA_2:\n%s\n", Arrays.toString(ppPairsA_2.toArray()));
            System.err.printf("StateB_2:\n%s\n", Arrays.toString(ppPairsB_2.toArray()));
        }
        
        declsB = hdrB.getDeclarations();
        assertEquals("Declarations count", 1, declsB.size());
        declB = declsB.iterator().next();
        assertEquals("Declaration name", "struct_255449_b", declB.getName().toString());
        declsA = hdrA.getDeclarations();
        assertEquals("Declarations count", 1, declsA.size());
        declA = declsA.iterator().next();
        assertEquals("Declaration name", "struct_255449_a", declA.getName().toString());
    }
}
