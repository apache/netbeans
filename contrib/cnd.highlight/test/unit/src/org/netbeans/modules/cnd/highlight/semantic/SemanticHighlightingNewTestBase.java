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
package org.netbeans.modules.cnd.highlight.semantic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.swing.text.Document;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.highlight.semantic.debug.InterrupterImpl;
import org.netbeans.modules.cnd.highlight.semantic.debug.TestSemanticHighlighting;
import org.netbeans.modules.cnd.highlight.semantic.debug.TestSemanticHighlighting.Highlight;
import org.netbeans.modules.cnd.modelimpl.test.ProjectBasedTestCase;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.Exceptions;

/**
 *
 */
public class SemanticHighlightingNewTestBase extends ProjectBasedTestCase{
    
    public SemanticHighlightingNewTestBase(String testName) {
        super(testName);
    }
    
    protected final void performTest(String filePath) {
        
        CsmFile csmFile = null;
        try {
            csmFile = getCsmFile(getDataFile(filePath));
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
        
        CloneableEditorSupport ces = CsmUtilities.findCloneableEditorSupport(csmFile);
        Document doc = CsmUtilities.openDocument(ces);
        
        List<Highlight> out = TestSemanticHighlighting.gethighlightsBagForTests(doc, new InterrupterImpl());
        assertNotNull(out);
        List<Highlight> sorted = new ArrayList<>(out);
        Collections.sort(sorted, new Comparator<Highlight>() {

            @Override
            public int compare(Highlight o1, Highlight o2) {
                return o1.getStartOffset() - o2.getStartOffset();
            }
        });
        int i = 1;
        for (Highlight b : sorted) {
            ref( "Block:\tPosition " +  // NOI18N
                    b.getStartPosition() + "-" +  // NOI18N
                    b.getEndPosition() + "\t" +  // NOI18N
                    b.getType());
        }
        compareReferenceFiles();
    }
}
