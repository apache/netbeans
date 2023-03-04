/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.test.subversion;

import junit.framework.Test;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.test.subversion.main.archeology.AnnotationsTest;
import org.netbeans.test.subversion.main.archeology.SearchHistoryUITest;
import org.netbeans.test.subversion.main.archeology.SearchRevisionsTest;
import org.netbeans.test.subversion.main.branches.*;
import org.netbeans.test.subversion.main.checkout.*;
import org.netbeans.test.subversion.main.commit.CommitDataTest;
import org.netbeans.test.subversion.main.commit.CommitUiTest;
import org.netbeans.test.subversion.main.commit.IgnoreTest;
import org.netbeans.test.subversion.main.delete.DeleteTest;
import org.netbeans.test.subversion.main.delete.FilesViewDoubleRefTest;
import org.netbeans.test.subversion.main.delete.FilesViewRefTest;
import org.netbeans.test.subversion.main.delete.RefactoringTest;
import org.netbeans.test.subversion.main.diff.DiffTest;
import org.netbeans.test.subversion.main.diff.ExportDiffPatchTest;
import org.netbeans.test.subversion.main.properties.SvnPropertiesTest;
import org.netbeans.test.subversion.main.relocate.RelocateTest;

/**
 *
 */
public class SubversionStableTest extends JellyTestCase {

    public SubversionStableTest(String name) {
        super(name);
    }

    public static Test suite() {
        return emptyConfiguration()
                .addTest(CheckoutContentTest.class, "testCheckoutProject", "testCheckoutContent")
                .addTest(CheckoutUITest.class, "testInvokeClose", "testChangeAccessTypes", "testIncorrentUrl", "testAvailableFields", "testRepositoryFolder")
                .addTest(CreateProjectVersionedDirTest.class, "testCreateNewProject")
                .addTest(ImportUITest.class, "testInvoke", "testWarningMessage", "testCommitStep")
                .addTest(ProxySettingsUITest.class, "testProxySettings", "testProxyBeforeUrl")
                .addTest(CommitDataTest.class, "testCommitFile", "testCommitPackage", "testRecognizeMimeType")
                .addTest(CommitUiTest.class, "testInvokeCloseCommit")
                .addTest(IgnoreTest.class, "testIgnoreUnignoreFile", "testIgnoreUnignorePackage", "testIgnoreUnignoreFilePackage", "testFinalRemove")
                //.addTest(DeleteTest.class, "testDeleteRevert", "testDeleteCommit")
                //.addTest(FilesViewDoubleRefTest.class, "testFilesViewDoubleRefactoring")
                //.addTest(FilesViewRefTest.class, "testFilesViewRefactoring")
                //.addTest(RefactoringTest.class, "testRefactoring")
                .addTest(DiffTest.class, "testDiffFile")
                .addTest(ExportDiffPatchTest.class, "invokeExportDiffPatch")
                .addTest(AnnotationsTest.class, "testShowAnnotations")
                .addTest(SearchRevisionsTest.class, "testSearchRevisionsTest")
                .addTest(SearchHistoryUITest.class, "testInvokeSearch")
                .addTest(CopyTest.class, "testCreateNewCopySwitch", "testCreateNewCopy")
                .addTest(CopyUiTest.class, "testInvokeCloseCopy")
                .addTest(MergeUiTest.class, "testInvokeCloseMerge")
                .addTest(RevertUiTest.class, "testInvokeCloseRevert")
                .addTest(SwitchUiTest.class, "testInvokeCloseSwitch")
                .addTest(SvnPropertiesTest.class, "propTest")
                .addTest(RelocateTest.class, "relocate")
                .suite();
    }
}
