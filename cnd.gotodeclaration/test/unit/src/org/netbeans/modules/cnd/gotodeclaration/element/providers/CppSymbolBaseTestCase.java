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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
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
