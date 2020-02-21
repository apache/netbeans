/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
