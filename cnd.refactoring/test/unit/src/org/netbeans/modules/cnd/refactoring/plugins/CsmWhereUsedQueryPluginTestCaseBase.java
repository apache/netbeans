/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.cnd.refactoring.plugins;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.services.CsmCacheManager;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.refactoring.test.RefactoringBaseTestCase;
import org.netbeans.modules.cnd.test.CndCoreTestUtils;
import org.netbeans.modules.refactoring.spi.FiltersManager;
import org.netbeans.modules.refactoring.spi.RefactoringElementImplementation;
import org.netbeans.modules.refactoring.spi.ui.FiltersDescription;
import org.openide.filesystems.FileObject;
import org.openide.util.Parameters;

/**
 *
 */
public class CsmWhereUsedQueryPluginTestCaseBase extends RefactoringBaseTestCase {

    public CsmWhereUsedQueryPluginTestCaseBase(String testName) {
        super(testName);
        System.setProperty("cnd.test.skip.coloring", "true");
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        CsmWhereUsedQueryPlugin.LOG.setLevel(Level.WARNING);
    }

    protected void performWhereUsed(String source, int line, int column) throws Exception {
        performWhereUsed(source, line, column, Collections.<Object, Boolean>emptyMap());
    }
    
    protected void performWhereUsed(String source, int line, int column, Map<Object, Boolean> params) throws Exception {
        performWhereUsed(source, line, column, params, Collections.<String>emptyList());
    }
    
    protected void performWhereUsed(String source, int line, int column, 
            Map<Object, Boolean> params, List<String> selectedFilters) throws Exception {
        CsmCacheManager.enter();
        try {
            CsmReference ref = super.getReference(source, line, column);
            assertNotNull(ref);
            CsmObject targetObject = ref.getReferencedObject();
            assertNotNull(targetObject);

            FiltersDescription filtersDescription = new FiltersDescription();
            if (params == null) {
                params = Collections.emptyMap();
            }
            Collection<RefactoringElementImplementation> elements = CsmWhereUsedQueryPlugin.getWhereUsed(ref, params, filtersDescription);

            // do filtering
            TestFiltersManager filtersManager;
            if (!selectedFilters.isEmpty()) {
                filtersManager = new TestFiltersManager(selectedFilters);
            } else {
                filtersManager = new TestFiltersManager(filtersDescription);
            }

            ArrayList<RefactoringElementImplementation> res = new ArrayList<>();
            for (RefactoringElementImplementation elem : elements) {
                if (elem instanceof FiltersManager.Filterable) {
                    if (((FiltersManager.Filterable) elem).filter(filtersManager)) {
                        res.add(elem);
                    }
                }
            }

            dumpAndCheckResults(res, getName() + ".ref");
        } finally {
            CsmCacheManager.leave();
        }
    }

    private void dumpAndCheckResults(Collection<RefactoringElementImplementation> elements, String goldenFileName) throws Exception {
        File workDir = getWorkDir();

        File output = new File(workDir, goldenFileName);
        PrintStream streamOut = new PrintStream(output);

        assertNotNull("Result should not be null", elements);
        List<RefactoringElementImplementation> sortedElems = new ArrayList<>(elements);
        Collections.sort(sortedElems, COMPARATOR);
        FileObject lastFO = null;
        for (RefactoringElementImplementation elem : sortedElems) {
            FileObject curFO = elem.getParentFile();
            if (!curFO.equals(lastFO)) {
                streamOut.println("References in file " + curFO.getParent().getName() + "/" + curFO.getNameExt());
                lastFO = curFO;
            }
            int startLine = elem.getPosition().getBegin().getLine()+1;
            int startCol = elem.getPosition().getBegin().getColumn()+1;
            int endLine = elem.getPosition().getEnd().getLine()+1;
            int endCol = elem.getPosition().getEnd().getColumn()+1;
            streamOut.printf("[%d:%d-%d:%d] %s\n", startLine, startCol, endLine, endCol, elem.getDisplayText());
        }
        streamOut.close();

        File goldenDataFile = getGoldenFile(goldenFileName);
        if (!goldenDataFile.exists()) {
            fail("No golden file " + goldenDataFile.getAbsolutePath() + "\n to check with output file " + output.getAbsolutePath());
        }
        if (CndCoreTestUtils.diff(output, goldenDataFile, null)) {
            // copy golden
            File goldenCopyFile = new File(workDir, goldenFileName + ".golden");
            CndCoreTestUtils.copyToWorkDir(goldenDataFile, goldenCopyFile); // NOI18N
            StringBuilder buf = new StringBuilder("OUTPUT Difference between diff " + output + " " + goldenCopyFile); // NOI18N
            File diffErrorFile = new File(output.getAbsolutePath() + ".diff"); // NOI18N
            CndCoreTestUtils.diff(output, goldenDataFile, diffErrorFile);
            showDiff(diffErrorFile, buf);
            fail(buf.toString());
        }
    }
    private static final Comparator<RefactoringElementImplementation> COMPARATOR = new Comparator<RefactoringElementImplementation>() {

        @Override
        public int compare(RefactoringElementImplementation o1, RefactoringElementImplementation o2) {
            Parameters.notNull("o1", o1);
            Parameters.notNull("o2", o2);
            String path1 = o1.getParentFile().getPath();
            String path2 = o2.getParentFile().getPath();
            int res = path1.compareToIgnoreCase(path2);
            if (res == 0) {
                int offset1 = o1.getPosition().getBegin().getOffset();
                int offset2 = o2.getPosition().getBegin().getOffset();
                res = offset1 - offset2;
            }
            return res;
        }
    };
    
    private static class TestFiltersManager extends FiltersManager {
        private final Collection<String> selectedFilters;

        public TestFiltersManager(Collection<String> selectedFilters) {
            this.selectedFilters = selectedFilters;
        }

        public TestFiltersManager(FiltersDescription filtersDescription) {
            this.selectedFilters = new ArrayList<>();
            for (int i = 0; i < filtersDescription.getFilterCount(); i++) {
                if (filtersDescription.isSelected(i)) {
                    selectedFilters.add(filtersDescription.getKey(i));
                }
            }
        }
        
        @Override
        public boolean isSelected(String filterName) {
            return selectedFilters.contains(filterName);
        }
    }
}
