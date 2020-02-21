/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of the
 * License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include the
 * License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by Oracle
 * in the GPL Version 2 section of the License file that accompanied this code.
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or only
 * the GPL Version 2, indicate your decision by adding "[Contributor] elects to
 * include this software in this distribution under the [CDDL or GPL Version 2]
 * license." If you do not indicate a single choice of license, a recipient has
 * the option to distribute your version of this file under either the CDDL, the
 * GPL Version 2 or to extend the choice of license to its licensees as provided
 * above. However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is made
 * subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
