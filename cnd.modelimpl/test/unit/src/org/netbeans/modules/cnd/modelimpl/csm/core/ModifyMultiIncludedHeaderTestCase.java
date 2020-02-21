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
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;

/**
 *
 */
public class ModifyMultiIncludedHeaderTestCase extends ModifyDocumentTestCaseBase {
    public ModifyMultiIncludedHeaderTestCase(String testName) {
        super(testName);
//        System.setProperty("cnd.modelimpl.trace191307", "true");
    }

    public void test174007() throws Exception {
        // #174007:  (Sometimes) Incorrect processing of #ifndef .. #define
        if (TraceFlags.TRACE_191307_BUG) {
            System.err.printf("TEST MULTI INCLUSION\n");
        }
        final File sourceFile = getDataFile("multiIncludedFileForModification.h");
        super.insertTextThenSaveAndCheck(sourceFile, 12 + 1, "void foo();\n", 
                sourceFile, new DeclarationsNumberChecker(3, 4), true);
    }

    public void test213261_1() throws Exception {
        doTest213261(true, true);
    }

    public void test213261_2() throws Exception {
        doTest213261(true, false);
    }

    public void test213261_3() throws Exception {
        doTest213261(false, true);
    }

    public void test213261_4() throws Exception {
        doTest213261(false, false);
    }

    private void doTest213261(boolean extraReopen, boolean waitParseAfterChange) throws Exception {
        // #213261 - failing test on all platforms ModifyMultiIncludedHeaderTestCase.test174007
        final File testFile = getDataFile("multiIncludedFileForModification.h");
        FileImpl csmFile = (FileImpl) super.getCsmFile(testFile);
        assertNotNull(csmFile);
        ProjectBase project = csmFile.getProjectImpl(true);
        if (project == null) {
            assertNotNull("no project for test " + getName() + " in " + getModel().projects(), project);
        }
        String projectName = project.getName().toString();
        if (extraReopen) {
            super.closeProject(projectName);
            super.reopenProject(projectName, true);
        }
        csmFile = (FileImpl) super.getCsmFile(testFile);
        project = csmFile.getProjectImpl(true);
        if (project == null) {
            assertNotNull("no " + projectName + " for test " + getName() + " in " + getModel().projects(), project);
        }
        DeepReparsingUtils.tryPartialReparseOnChangedFile(project, csmFile);
        if (waitParseAfterChange) {
            super.waitAllProjectsParsed();
        }
        super.closeProject(projectName);
        super.reopenProject(projectName, true);
    }
}
