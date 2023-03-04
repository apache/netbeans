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
package org.netbeans.test.git;

import junit.framework.Test;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.test.git.main.archeology.AnnotationsTest;
import org.netbeans.test.git.main.commit.CloneTest;
import org.netbeans.test.git.main.commit.CommitDataTest;
import org.netbeans.test.git.main.commit.CommitUiTest;
import org.netbeans.test.git.main.commit.IgnoreTest;
import org.netbeans.test.git.main.commit.InitializeTest;
import org.netbeans.test.git.main.delete.DeleteTest;
import org.netbeans.test.git.main.delete.RefactoringTest;
import org.netbeans.test.git.main.diff.DiffTest;
import org.netbeans.test.git.main.diff.ExportDiffPatchTest;
import org.netbeans.test.git.utils.gitExistsChecker;

/**
 *
 * @author kanakmar
 */
public class GitStableTest extends JellyTestCase {

    public GitStableTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        if (gitExistsChecker.check(false)) {
            return NbModuleSuite.create(NbModuleSuite.emptyConfiguration()
                    .addTest(AnnotationsTest.class, "testShowAnnotations")
                    .addTest(CloneTest.class, "testCloneProject")
                    .addTest(CommitDataTest.class, "testCommitFile", "testRecognizeMimeType")
                    .addTest(CommitUiTest.class, "testInvokeCloseCommit")
                    .addTest(DeleteTest.class, "testDeleteRevert", "testDeleteCommit")
                    .addTest(IgnoreTest.class, "testIgnoreUnignoreFile")
                    .addTest(InitializeTest.class, "testInitializeAndFirstCommit")
                    .addTest(RefactoringTest.class, "testRefactoring")
                    .addTest(DiffTest.class, "testDiffFile")
                    .addTest(ExportDiffPatchTest.class, "testInvokeExportDiffPatch")
                    .enableModules(".*")
                    .clusters(".*"));
        } else {
            return NbModuleSuite.create(NbModuleSuite.emptyConfiguration());
        }
    }
}
