/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
