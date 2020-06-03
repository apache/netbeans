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
package org.netbeans.modules.cnd.highlight.error;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.junit.RandomlyFails;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmListeners;
import org.netbeans.modules.cnd.api.model.CsmModel;
import org.netbeans.modules.cnd.api.model.CsmProgressAdapter;
import org.netbeans.modules.cnd.api.model.CsmProgressListener;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;

/**
 *
 */
public class MultiProjectsErrorHighlightingTest extends ErrorHighlightingBaseTestCase {
    private static final String PROJECT_FIRST = "project_first";
    private static final String PROJECT_FIFTH = "project_fifth";

    public MultiProjectsErrorHighlightingTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        System.setProperty("cnd.csm.errors.async", "false");
        Logger logger = Logger.getLogger("org.netbeans.modules.masterfs.filebasedfs.utils.FileChangedManager");
        if (logger != null) {
            logger.setLevel(Level.OFF);
        }
        super.setUp();
    }

    @Override
    protected boolean needRepository() {
        return true;
    }

    @Override
    protected File[] changeDefProjectDirBeforeParsingProjectIfNeeded(File projectDir) {
        // we have following structure for this test
        // test-folder
        //  --first\
        //        first.cpp
        //  --second\
        //        second.cpp
        //  --third\
        //        third.cpp
        //  --forth\
        //        forth.cpp
        //  --fifth\
        //        fifth.cpp
        //  --includedLibrary\ 
        //        lib_header.h
        //  --otherLibrary\
        //        other_lib_header.h
        //
        // so, adjust used folders

        File srcDir1 = new File(projectDir, "first");
        File srcDir2 = new File(projectDir, "second");
        File srcDir3 = new File(projectDir, "third");
        File srcDir4 = new File(projectDir, "forth");
        File srcDir5 = new File(projectDir, "fifth");
        File incl1 = new File(projectDir, "includedLibrary");
        File incl2 = new File(projectDir, "otherLibrary");
        checkDir(srcDir1);
        checkDir(srcDir2);
        checkDir(srcDir3);
        checkDir(srcDir4);
        checkDir(srcDir5);
        checkDir(incl1);
        checkDir(incl2);
        List<String> sysIncludes = Arrays.asList(incl1.getAbsolutePath(), incl2.getAbsolutePath());
        File[] outPrjDirs = new File[] {srcDir1, srcDir2, srcDir3, srcDir4, srcDir5};
        for (File prjDir : outPrjDirs) {
            super.setSysIncludes(prjDir.getAbsolutePath(), sysIncludes);
        }
        return outPrjDirs;
    }
    
    @RandomlyFails
    public void test210384() throws Exception {
        // #210384 - unresolved "using namespace std"
        CsmModel model = super.getModel();
        assertNotNull("null model", model);
        performStaticTest("first/first.cpp");
        CsmProject firstPrj = super.getProject(PROJECT_FIRST);
        assertNotNull("null project for first", firstPrj);
        CsmProject secondPrj = super.getProject("project_second");
        assertNotNull("null project for second", secondPrj);
        CsmProject thirdPrj = super.getProject(PROJECT_FIRST);
        assertNotNull("null project for first", thirdPrj);
        CsmProject forthPrj = super.getProject("project_second");
        assertNotNull("null project for second", forthPrj);
        performStaticTest("first/first.cpp");
        performStaticTest("second/second.cpp");
        performStaticTest("third/third.cpp");
        performStaticTest("forth/forth.cpp");
    }

    public void testRedFilesWhenProjectClose202433() throws Exception {
        // #202433 - parser errors in studio system includes
        CsmModel model = super.getModel();
        assertNotNull("null model", model);
        performStaticTest("first/first.cpp");
        CsmProject firstPrj = super.getProject(PROJECT_FIRST);
        assertNotNull("null project for first", firstPrj);
        CsmProject secondPrj = super.getProject("project_second");
        assertNotNull("null project for second", secondPrj);
        super.closeProject(PROJECT_FIRST);
        performStaticTest("includedLibrary/lib_header.h");
        performStaticTest("otherLibrary/other_lib_header.h");
        performStaticTest("second/second.cpp");
    }

    public void testRedFilesWhenNoReparseProject210898() throws Exception {
        // #210898 incorrect content of system includes after reopening projects => unresolved identifiers in dependent projects
        doTestRedFilesWhenReopenProject210898(false);
    }

    public void testRedFilesWhenReparseAndReopenProject210898() throws Exception {
        // #210898 incorrect content of system includes after reopening projects => unresolved identifiers in dependent projects
        doTestRedFilesWhenReopenProject210898(true);
    }

    private void doTestRedFilesWhenReopenProject210898(boolean reparse) throws Exception {
        assertTrue("reposiroty Must Be ON " + TraceFlags.PERSISTENT_REPOSITORY, TraceFlags.PERSISTENT_REPOSITORY);
        // #210898 incorrect content of system includes after reopening projects => unresolved identifiers in dependent projects
        CsmModel model = super.getModel();
        final AtomicInteger parseCounter = new AtomicInteger(0);
        final CsmProgressListener listener = new CsmProgressAdapter() {
            @Override
            public void fileParsingFinished(CsmFile file) {
                parseCounter.incrementAndGet();
            }
        };
        CsmListeners.getDefault().addProgressListener(listener);
        assertNotNull("null model", model);
        performStaticTest("first/first.cpp");
        performStaticTest("fifth/fifth.cpp");
        CsmProject firstPrj = super.getProject(PROJECT_FIRST);
        assertNotNull("null project for first", firstPrj);
        // fifth project defines macro which defines extra classes
        CsmProject macroDefinedProject = super.getProject(PROJECT_FIFTH);
        assertNotNull("null project for first", macroDefinedProject);
        assertEquals("reparse was detected ", 0, parseCounter.intValue());
        // close project which uses this extra classes
        super.closeProject(PROJECT_FIFTH);
        assertEquals("reparse was detected ", 0, parseCounter.intValue());
        if (reparse) {
            // reparse all projects
            super.reparseAllProjects();
            parseCounter.set(0);
        }
        performStaticTest("first/first.cpp");
        super.reopenProject(PROJECT_FIFTH, true);
        performStaticTest("fifth/fifth.cpp");
        assertEquals("extra reparse was detected ", reparse ? 3 : 0, parseCounter.intValue());
        CsmListeners.getDefault().removeProgressListener(listener);
    }
}
