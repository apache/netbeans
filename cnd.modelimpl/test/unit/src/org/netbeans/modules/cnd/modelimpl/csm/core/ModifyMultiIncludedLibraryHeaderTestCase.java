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
import java.util.Collections;
import java.util.List;
import javax.swing.text.BadLocationException;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.utils.CndUtils;

/**
 *
 */
public class ModifyMultiIncludedLibraryHeaderTestCase extends ModifyDocumentTestCaseBase {
    public ModifyMultiIncludedLibraryHeaderTestCase(String testName) {
        super(testName);
//        System.setProperty("DeepReparsingUtils.level", "FINEST");
//        System.setProperty("cnd.modelimpl.use.deep.reparsing.trace", "true");
//        System.setProperty("cnd.modelimpl.timing.per.file.flat", "true");
//        System.setProperty("cnd.modelimpl.timing", "true");
    }

    @Override
    protected File[] changeDefProjectDirBeforeParsingProjectIfNeeded(File projectDir) {
        // we have following structure for this test
        // test-folder
        //  --mainSources\
        //        main.cpp
        //  --libraryFolder\
        //        hello1.h
        //        hello1.cpp
        //  --secondProject\
        //        secondMain.cpp
        //  --secondLibrary\
        //        secondLib.h
        //        secondLibImpl1.cpp
        //        secondLibImpl2.cpp
        //
        // so, adjust used folders

        File libraryDir = new File(projectDir, "libraryFolder");
        File srcDir = new File(projectDir, "mainSources");
        File library2Dir = new File(projectDir, "secondLibrary");
        File src2Dir = new File(projectDir, "secondProject");
        checkDir(libraryDir);
        checkDir(srcDir);
        checkDir(library2Dir);
        checkDir(src2Dir);
        super.setSysIncludes(srcDir.getAbsolutePath(), Collections.singletonList(libraryDir.getAbsolutePath()));
        super.setLibProjectsPaths(srcDir.getAbsolutePath(), Collections.singletonList(libraryDir.getAbsolutePath()));

        super.setSysIncludes(src2Dir.getAbsolutePath(), Collections.singletonList(library2Dir.getAbsolutePath()));
        super.setLibProjectsPaths(src2Dir.getAbsolutePath(), Collections.singletonList(library2Dir.getAbsolutePath()));

        return new File[]{libraryDir, library2Dir, srcDir, src2Dir};
    }

    public void disabled_test1() throws Exception {
        // check header's content when it is included from best and not best macro context and
        // the best one disables (#undef) it's macros
        ProjectBase libProject = (ProjectBase) super.getProject("project_secondLibrary");
        assertNotNull(libProject);
        assertEquals("three files are expected " + libProject.getAllFiles(), 3, libProject.getAllFiles().size());
        ProjectBase srcProject = (ProjectBase) super.getProject("project_secondProject");
        assertNotNull(srcProject);
        assertEquals("only one file is expected " + srcProject.getAllFiles(), 1, srcProject.getAllFiles().size());
        List<CsmProject> srcLibs = srcProject.getLibraries();
        Collection<ProjectBase> dependentProjects = libProject.getDependentProjects();
        assertEquals("one dependent Project is expected " + dependentProjects, 1, dependentProjects.size());
        assertEquals("one library is expected " + srcLibs, 1, srcLibs.size());
        assertEquals("mainSources project have to be in dependent list of library" + dependentProjects, srcProject, dependentProjects.iterator().next());
        assertEquals("library is expected to be in list of mainSources dependencies" + srcLibs, libProject, srcLibs.iterator().next());
        final File libFile1 = getDataFile("secondLibrary/secondLibImpl1.cpp");
        final File file2Check = getDataFile("secondLibrary/secondLib.h");
        super.insertTextThenSaveAndCheck(libFile1, 5,
                  "#undef SECOND_EMPTY_MACRO_1\n"
                + "#undef SECOND_EMPTY_MACRO_2\n"
                + "#undef SECOND_EMPTY_MACRO\n",
                file2Check, new Checker1(), true);
        if (CndUtils.getLastAssertion() != null) {
            int i = 10;
        }
    }

    public void test210816() throws Exception {
        // #210816 - unresolved identifiers due to partial reparse
        ProjectBase libProject = (ProjectBase) super.getProject("project_libraryFolder");
        assertNotNull(libProject);
        assertEquals("two files are expected " + libProject.getAllFiles(), 2, libProject.getAllFiles().size());
        ProjectBase srcProject = (ProjectBase) super.getProject("project_mainSources");
        assertNotNull(srcProject);
        assertEquals("only one file is expected " + srcProject.getAllFiles(), 1, srcProject.getAllFiles().size());
        List<CsmProject> srcLibs = srcProject.getLibraries();
        Collection<ProjectBase> dependentProjects = libProject.getDependentProjects();
        assertEquals("one dependent Project is expected " + dependentProjects, 1, dependentProjects.size());
        assertEquals("one library is expected " + srcLibs, 1, srcLibs.size());
        assertEquals("mainSources project have to be in dependent list of library" + dependentProjects, srcProject, dependentProjects.iterator().next());
        assertEquals("library is expected to be in list of mainSources dependencies" + srcLibs, libProject, srcLibs.iterator().next());
        final File sourceFile = getDataFile("mainSources/main.cpp");
        final File file2Check = getDataFile("libraryFolder/hello1.h");
        super.insertTextThenSaveAndCheck(sourceFile, 1, "#define EMPTY_MACRO\n",
                file2Check, new IZ210816Checker(), true);
    }

    private static final class IZ210816Checker implements Checker {

        @Override
        public void checkBeforeModifyingFile(FileImpl modifiedFile, FileImpl fileToCheck, CsmProject project, BaseDocument doc) throws BadLocationException {
            checkDeclarationNumber(0, fileToCheck);
            checkDeadBlocks(fileToCheck.getProject(), fileToCheck, "1. text before:", doc, "File must have " + 1 + " dead code block ", 1);
        }

        @Override
        public void checkAfterModifyingFile(FileImpl modifiedFile, FileImpl fileToCheck, CsmProject project, BaseDocument doc) throws BadLocationException {
            // should be the same as before start of modifications
            checkBeforeModifyingFile(modifiedFile, fileToCheck, project, doc);
        }

        @Override
        public void checkAfterParseFinished(FileImpl modifiedFile, FileImpl fileToCheck, CsmProject project, BaseDocument doc) throws BadLocationException {
            CsmProject libProject = fileToCheck.getProject();
            assertNotSame(libProject, project);
            libProject.waitParse();
            assertSame("PARSED", fileToCheck.getStateFromTest());
            checkDeclarationNumber(1, fileToCheck);
            checkDeadBlocks(fileToCheck.getProject(), fileToCheck, "2. text after:", doc, "File must have " + 0 + " dead code block ", 0);
        }

        @Override
        public void checkAfterUndo(FileImpl modifiedFile, FileImpl fileToCheck, CsmProject project, BaseDocument doc) throws BadLocationException {
            checkAfterParseFinished(modifiedFile, fileToCheck, project, doc);
        }

        @Override
        public void checkAfterUndoAndParseFinished(FileImpl modifiedFile, FileImpl fileToCheck, CsmProject project, BaseDocument doc) throws BadLocationException {
            // should be the same as before start of modifications
            checkBeforeModifyingFile(modifiedFile, fileToCheck, project, doc);
        }
    }

    private static final class Checker1 implements Checker {
        // file to check content have to be always the same!

        @Override
        public void checkBeforeModifyingFile(FileImpl modifiedFile, FileImpl fileToCheck, CsmProject project, BaseDocument doc) throws BadLocationException {
            checkFile(fileToCheck, doc);
        }

        @Override
        public void checkAfterModifyingFile(FileImpl modifiedFile, FileImpl fileToCheck, CsmProject project, BaseDocument doc) throws BadLocationException {
            checkFile(fileToCheck, doc);
        }

        @Override
        public void checkAfterParseFinished(FileImpl modifiedFile, FileImpl fileToCheck, CsmProject project, BaseDocument doc) throws BadLocationException {
            checkFile(fileToCheck, doc);
        }

        @Override
        public void checkAfterUndo(FileImpl modifiedFile, FileImpl fileToCheck, CsmProject project, BaseDocument doc) throws BadLocationException {
            checkFile(fileToCheck, doc);
        }

        @Override
        public void checkAfterUndoAndParseFinished(FileImpl modifiedFile, FileImpl fileToCheck, CsmProject project, BaseDocument doc) throws BadLocationException {
            checkFile(fileToCheck, doc);
        }

        // have to be always the same!
        private void checkFile(FileImpl fileToCheck, BaseDocument doc) throws BadLocationException {
            checkDeclarationNumber(3, fileToCheck);
            checkDeadBlocks(fileToCheck.getProject(), fileToCheck, "1. text before:", doc, "File must have " + 0 + " dead code block ", 0);
        }
    }
}
