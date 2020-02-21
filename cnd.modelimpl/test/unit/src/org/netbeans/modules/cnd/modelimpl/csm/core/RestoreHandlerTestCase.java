/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
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
