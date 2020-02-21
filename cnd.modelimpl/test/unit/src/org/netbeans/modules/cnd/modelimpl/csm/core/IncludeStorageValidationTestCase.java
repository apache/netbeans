/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.modelimpl.csm.core;

import java.io.File;
import java.util.Collection;
import java.util.Map;
import javax.swing.text.BadLocationException;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.model.util.UIDs;
import org.netbeans.modules.cnd.modelimpl.accessors.CsmCorePackageAccessor;

/**
 *
 */
public class IncludeStorageValidationTestCase  extends ModifyDocumentTestCaseBase {
    public IncludeStorageValidationTestCase(String testName) {
        super(testName);
    }

    public void testOwnIncludedStorageInvalidation() throws Exception {
        //
        final File sourceFile = getDataFile("sourceForModification_testOwnIncludedStorageInvalidation.cc");
        final File checkedFile = getDataFile("testOwnIncludedStorageInvalidation.h");
        super.insertTextThenSaveAndCheck(sourceFile, 1, "#define ABC\n",
                checkedFile, new testOwnIncludedStorageInvalidationChecker(), false);
    }

    private static class testOwnIncludedStorageInvalidationChecker implements Checker {

        public testOwnIncludedStorageInvalidationChecker() {
        }

        @Override
        public void checkBeforeModifyingFile(FileImpl modifiedFile, FileImpl fileToCheck, CsmProject project, BaseDocument doc) throws BadLocationException {
            ProjectBase projectImpl = fileToCheck.getProjectImpl(true);
            assertSame(project, projectImpl);
            Map<CsmUID<CsmProject>, Collection<PreprocessorStatePair>> includedPreprocStatePairs = projectImpl.getIncludedPreprocStatePairs(fileToCheck);
            assertEquals("includedPreprocStatePairs=" + includedPreprocStatePairs, 1, includedPreprocStatePairs.size());
            CsmUID<CsmProject> projUID = UIDs.get(project);
            assertTrue("have to have entries for " + project + " in " + includedPreprocStatePairs, includedPreprocStatePairs.containsKey(projUID));
            Collection<PreprocessorStatePair> pairs = includedPreprocStatePairs.get(projUID);
            assertEquals("pairs=" + pairs, 1, pairs.size());
            FilePreprocessorConditionState golden = FilePreprocessorConditionState.build(fileToCheck.getAbsolutePath(), new int[]{});
            for (PreprocessorStatePair preprocessorStatePair : pairs) {
                assertTrue("pair=" + preprocessorStatePair, preprocessorStatePair.state.isValid());
                assertTrue("pair=" + preprocessorStatePair, preprocessorStatePair.pcState.equals(golden));
            }
        }

        @Override
        public void checkAfterModifyingFile(FileImpl modifiedFile, FileImpl fileToCheck, CsmProject project, BaseDocument doc) throws BadLocationException {
            checkBeforeModifyingFile(modifiedFile, fileToCheck, project, doc);
        }

        @Override
        public void checkAfterParseFinished(FileImpl modifiedFile, FileImpl fileToCheck, CsmProject project, BaseDocument doc) throws BadLocationException {
            ProjectBase projectImpl = fileToCheck.getProjectImpl(true);
            assertSame(project, projectImpl);
            Collection<PreprocessorStatePair> goldenPairs = projectImpl.getPreprocessorStatePairs(fileToCheck);
            assertEquals("goldenPairs=" + goldenPairs, 1, goldenPairs.size());
            Map<CsmUID<CsmProject>, Collection<PreprocessorStatePair>> includedPreprocStatePairs = projectImpl.getIncludedPreprocStatePairs(fileToCheck);
            assertEquals("includedPreprocStatePairs=" + includedPreprocStatePairs, 1, includedPreprocStatePairs.size());
            CsmUID<CsmProject> projUID = UIDs.get(project);
            assertTrue("have to have entries for " + project + " in " + includedPreprocStatePairs, includedPreprocStatePairs.containsKey(projUID));
            Collection<PreprocessorStatePair> pairs = includedPreprocStatePairs.get(projUID);
            PreprocessorStatePair golden = goldenPairs.iterator().next();
            PreprocessorStatePair included = pairs.iterator().next();
            assertEquals(golden.pcState, included.pcState);
            assertEquals(golden.state, included.state);
            assertFalse(CsmCorePackageAccessor.get().getPCStateDeadBlocks(included.pcState).length == 1);
        }

        @Override
        public void checkAfterUndo(FileImpl modifiedFile, FileImpl fileToCheck, CsmProject project, BaseDocument doc) throws BadLocationException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void checkAfterUndoAndParseFinished(FileImpl modifiedFile, FileImpl fileToCheck, CsmProject project, BaseDocument doc) throws BadLocationException {
            throw new UnsupportedOperationException();
        }
    }

}
