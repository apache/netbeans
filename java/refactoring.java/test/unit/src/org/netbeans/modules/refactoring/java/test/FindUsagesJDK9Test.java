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
package org.netbeans.modules.refactoring.java.test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.SoftReference;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.logging.Logger;
import javax.lang.model.element.TypeElement;
import junit.framework.Test;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.queries.SourceLevelQuery;
import org.netbeans.api.java.source.*;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.java.j2seplatform.platformdefinition.J2SEPlatformImpl;
import org.netbeans.modules.java.source.BootClassPathUtil;
import org.netbeans.modules.java.source.parsing.JavacParser;
import org.netbeans.modules.refactoring.api.RefactoringElement;
import org.netbeans.modules.refactoring.api.RefactoringSession;
import org.netbeans.modules.refactoring.api.WhereUsedQuery;
import org.netbeans.modules.refactoring.java.api.WhereUsedQueryConstants;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.EditableProperties;
import org.openide.util.lookup.Lookups;

public class FindUsagesJDK9Test extends NbTestCase {
    private FileObject projectDir;
    private String origSourceLevel;
    private static final Logger LOG = Logger.getLogger(FindUsagesTest.class.getName());

    public FindUsagesJDK9Test(String name) {
        super(name);
    }

    /**
     * Set-up the services and project
     */
    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        String work = getWorkDirPath();
        System.setProperty("netbeans.user", work);

        JavacParser.DISABLE_SOURCE_LEVEL_DOWNGRADE = true;

        //set -source 9 for the test project
        origSourceLevel = setSourceLevel("9");

        //ensure the default platform is "modular":
        ClassPath moduleBoot = BootClassPathUtil.getModuleBootPath();
        JavaPlatform defaultPlatform = JavaPlatform.getDefault();
        Field bootstrapField = J2SEPlatformImpl.class.getDeclaredField("bootstrap");
        bootstrapField.setAccessible(true);
        bootstrapField.set(defaultPlatform, new SoftReference<ClassPath>(moduleBoot) {
            @Override
            public ClassPath get() {
                return moduleBoot;
            }
        });

        projectDir = Utilities.openProject("SimpleJ2SEApp", getDataDir());
        SourceUtils.waitScanFinished();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        //reset source level
        setSourceLevel(origSourceLevel);
    }

    private String setSourceLevel(String newSourceLevel) throws IOException {
        File projectsDir = FileUtil.normalizeFile(getDataDir());
        FileObject projectsDirFO = FileUtil.toFileObject(projectsDir);
        FileObject projdir = projectsDirFO.getFileObject("SimpleJ2SEApp");

        FileObject projectProperties = projdir.getFileObject("/nbproject/project.properties");
        EditableProperties editableProjectProperties = new EditableProperties(false);
        try (InputStream in = projectProperties.getInputStream()) {
            editableProjectProperties.load(in);
        }
        String originalJavacSource = editableProjectProperties.put("javac.source", newSourceLevel);
        try (OutputStream out = projectProperties.getOutputStream()) {
            editableProjectProperties.store(out);
        }
        return originalJavacSource;
    }

    public void testAnotherFileNETBEANS721() throws Exception {
        FileObject testFile = projectDir.getFileObject("/src/simplej2seapp/D.java");
        assertEquals("9", SourceLevelQuery.getSourceLevel(testFile));
        JavaSource src = JavaSource.forFileObject(testFile);
        final WhereUsedQuery[] wuq = new WhereUsedQuery[1];
        src.runWhenScanFinished(new Task<CompilationController>() {
            @Override
            public void run(CompilationController controller) throws Exception {
                controller.toPhase(JavaSource.Phase.RESOLVED);
                TypeElement i = controller.getElements().getTypeElement("simplej2seapp.I");
                TreePathHandle element = TreePathHandle.create(i, controller);
                wuq[0] = new WhereUsedQuery(Lookups.singleton(element));
            }
        }, false).get();
        setParameters(wuq, true, false, false, false, false, false);

        doRefactoring("testAnotherFileNETBEANS721", wuq, 2);
    }

    private void doRefactoring(final String name, final WhereUsedQuery[] wuq, final int amount) throws InterruptedException {
        RefactoringSession rs = RefactoringSession.create("Session");

        assertNull(wuq[0].preCheck());
        assertNull(wuq[0].fastCheckParameters());
        assertNull(wuq[0].checkParameters());
        assertNull(wuq[0].prepare(rs));
        rs.finished();
        rs.doRefactoring(true);

        Collection<RefactoringElement> elems = rs.getRefactoringElements();

        LOG.fine(name);
        for (RefactoringElement refactoringElement : elems) {
            LOG.fine(refactoringElement.getParentFile().getNameExt());
        }

        assertEquals("Number of usages", amount, elems.size());
    }

    private void setParameters(final org.netbeans.modules.refactoring.api.WhereUsedQuery[] wuq,
            boolean references, boolean comments, boolean subclasses,
            boolean directSubclasses, boolean overriding, boolean fromBaseclass) {
        wuq[0].putValue(WhereUsedQuery.FIND_REFERENCES, references);
        wuq[0].putValue(WhereUsedQuery.SEARCH_IN_COMMENTS, comments);
        wuq[0].putValue(WhereUsedQueryConstants.FIND_DIRECT_SUBCLASSES, directSubclasses);
        wuq[0].putValue(WhereUsedQueryConstants.FIND_OVERRIDING_METHODS, overriding);
        wuq[0].putValue(WhereUsedQueryConstants.FIND_SUBCLASSES, subclasses);
        wuq[0].putValue(WhereUsedQueryConstants.SEARCH_FROM_BASECLASS, fromBaseclass);
    }

    public static Test suite() throws InterruptedException {
//        return NbModuleSuite.createConfiguration(FindUsagesJDK9Test.class)
//                .clusters(".*").enableModules(".*")
//                .gui(false).suite();
        return NbTestSuite.createTest(Noop.class, "noop");
    }
}
