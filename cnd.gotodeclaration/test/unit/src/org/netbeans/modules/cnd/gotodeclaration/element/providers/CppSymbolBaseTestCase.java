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

package org.netbeans.modules.cnd.gotodeclaration.element.providers;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.netbeans.junit.Manager;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.gotodeclaration.symbol.CppSymbolProvider;
import org.netbeans.modules.cnd.modelimpl.test.ProjectBasedTestCase;
import org.netbeans.modules.cnd.test.CndCoreTestUtils;
import org.netbeans.spi.jumpto.symbol.SymbolDescriptor;
import org.netbeans.spi.jumpto.symbol.SymbolProvider;
import org.netbeans.spi.jumpto.symbol.SymbolProviderContextAndResultFactory;
import org.netbeans.spi.jumpto.type.SearchType;


/**
 * Common base for test CppSymbolProvider cases
 */
public class CppSymbolBaseTestCase extends ProjectBasedTestCase {

    public CppSymbolBaseTestCase(String testName) {
        super(testName);
    }

    @Override
    protected File getTestCaseDataDir() {
        return getQuoteDataDir();
    }


    ////////////////////////////////////////////////////////////////////////////

    protected final File getQuoteDataDir() {
        return Manager.normalizeFile(new File(getDataDir(), "common/quote_nosyshdr"));
    }

    protected void peformTest(String text, SearchType type) throws Exception {
        CppSymbolProvider provider = new CppSymbolProvider();

        CsmProject project = getProject();
        assertNotNull(project);

        List<SymbolDescriptor> elems = new ArrayList<SymbolDescriptor>();
        SymbolProvider.Context context = SymbolProviderContextAndResultFactory.createContext(null, type, text);
        SymbolProvider.Result result = SymbolProviderContextAndResultFactory.createResult(elems, context, provider);
        provider.computeSymbolNames(context, result);
        assertNotNull(elems);

        List<SymbolDescriptor> items = new ArrayList<SymbolDescriptor>();
        items.addAll(elems);

        Collections.sort(items, new TypeComparator());

        for (SymbolDescriptor elementDescriptor : items) {
            ref(elementDescriptor.getProjectName() + " " + elementDescriptor.getSymbolName()); // NOI18N
        }

        File output = new File(getWorkDir(), getName() + ".ref"); // NOI18N
        File goldenDataFile = getGoldenFile(getName() + ".ref");


        if (!goldenDataFile.exists()) {
            fail("No golden file " + goldenDataFile.getAbsolutePath() + "\n to check with output file " + output.getAbsolutePath()); // NOI18N
        }
        if (CndCoreTestUtils.diff(output, goldenDataFile, null)) {
            // copy golden
            File goldenCopyFile = new File(getWorkDir(), getName() + ".ref.golden"); // NOI18N
            CndCoreTestUtils.copyToWorkDir(goldenDataFile, goldenCopyFile); // NOI18N
            StringBuilder buf = new StringBuilder("OUTPUT Difference between diff " + output + " " + goldenCopyFile);
            File diffErrorFile = new File(getWorkDir(), getName() + ".diff");
            CndCoreTestUtils.diff(output, goldenDataFile, diffErrorFile);
            showDiff(diffErrorFile, buf);
            fail(buf.toString());
        }
        
    }

    private class TypeComparator implements Comparator<SymbolDescriptor> {

        @Override
        public int compare(SymbolDescriptor t1, SymbolDescriptor t2) {
            int result = compareStrings(t1.getSymbolName(), t2.getSymbolName());
            if (result == 0) {
                result = compareStrings(t1.getOwnerName(), t2.getOwnerName());
                if (result == 0) {
                    result = compareStrings(t1.getProjectName(), t2.getProjectName());
                    if (result == 0) {
                        result = compareStrings(t1.getFileDisplayPath(), t2.getFileDisplayPath());
                    }
                }
            }
            return result;
        }
    }

    private int compareStrings(String s1, String s2) {
        if (s1 == null) {
            s1 = ""; // NOI18N
        }
        if (s2 == null) {
            s2 = ""; // NOI18N
        }
        return s1.compareTo(s2);
    }

}
