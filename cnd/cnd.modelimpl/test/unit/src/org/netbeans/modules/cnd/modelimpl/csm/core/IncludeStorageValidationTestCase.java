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
