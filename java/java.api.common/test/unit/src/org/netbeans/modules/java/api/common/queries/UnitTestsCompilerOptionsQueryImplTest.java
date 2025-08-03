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
package org.netbeans.modules.java.api.common.queries;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Ignore;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.queries.SourceLevelQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.api.common.SourceRoots;
import org.netbeans.modules.java.api.common.TestProject;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.modules.java.source.parsing.JavacParserFactory;
import org.netbeans.spi.java.queries.CompilerOptionsQueryImplementation;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.test.TestFileUtils;
import org.openide.util.test.MockChangeListener;
import org.openide.util.test.MockLookup;


/**
 *
 * @author Tomas Zezula
 */
public class UnitTestsCompilerOptionsQueryImplTest extends NbTestCase {

    private TestProject project;
    private SourceRoots srcRoots;
    private SourceRoots testRoots;

    public UnitTestsCompilerOptionsQueryImplTest(final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        MockLookup.setInstances(TestProject.createProjectType());
        MockMimeLookup.setInstances(MimePath.get("text/x-java"), new JavacParserFactory()); //NOI18N
        final FileObject wd = FileUtil.toFileObject(FileUtil.normalizeFile(getWorkDir()));
        final FileObject src = FileUtil.createFolder(wd,"src"); //NOI18N
        final FileObject tst = FileUtil.createFolder(wd,"test");    //NOI18N
        final FileObject lib = FileUtil.createFolder(wd,"lib"); //NOI18N
        Project p = TestProject.createProject(wd, src, tst);
        project = p.getLookup().lookup(TestProject.class);
        assertNotNull(project);
        srcRoots = project.getSourceRoots();
        assertNotNull(srcRoots);
        assertEquals(srcRoots.getRoots().length, 1);
        assertEquals(srcRoots.getRoots()[0], src);
        assertNotNull(ClassPath.getClassPath(src, ClassPath.SOURCE));
        assertEquals(Collections.singletonList(src), Arrays.asList(ClassPath.getClassPath(src, ClassPath.SOURCE).getRoots()));
        testRoots = project.getTestRoots();
        assertNotNull(testRoots);
        assertEquals(testRoots.getRoots().length, 1);
        assertEquals(testRoots.getRoots()[0], tst);
        assertNotNull(ClassPath.getClassPath(tst, ClassPath.SOURCE));
        assertEquals(Collections.singletonList(tst), Arrays.asList(ClassPath.getClassPath(tst, ClassPath.SOURCE).getRoots()));
    }

    public void testJDK8() {
        setSourceLevel(project, "1.8"); //NOI18N
        final CompilerOptionsQueryImplementation impl = QuerySupport.createUnitTestsCompilerOptionsQuery(project.getEvaluator(), srcRoots, testRoots);
        assertNotNull(impl);
        assertNull(impl.getOptions(srcRoots.getRoots()[0]));
        final CompilerOptionsQueryImplementation.Result r = impl.getOptions(testRoots.getRoots()[0]);
        assertNotNull(r);
        final List<? extends String> args = r.getArguments();
        assertEquals(Collections.emptyList(), args);
    }

    public void testJDK9_UnnamedModule() {
        setSourceLevel(project, "9"); //NOI18N
        final CompilerOptionsQueryImplementation impl = QuerySupport.createUnitTestsCompilerOptionsQuery(project.getEvaluator(), srcRoots, testRoots);
        assertNotNull(impl);
        assertNull(impl.getOptions(srcRoots.getRoots()[0]));
        final CompilerOptionsQueryImplementation.Result r = impl.getOptions(testRoots.getRoots()[0]);
        assertNotNull(r);
        final List<? extends String> args = r.getArguments();
        assertEquals(Collections.emptyList(), args);
    }

    @Ignore // TODO failure
    public void testJDK9_TestInlinedIntoSourceModule() throws IOException {
        setSourceLevel(project, "9"); //NOI18N
        final String srcModuleName = "org.nb.App";  //NOI18N
        createModuleInfo(srcRoots.getRoots()[0], srcModuleName);
        final CompilerOptionsQueryImplementation impl = QuerySupport.createUnitTestsCompilerOptionsQuery(project.getEvaluator(), srcRoots, testRoots);
        assertNotNull(impl);
        assertNull(impl.getOptions(srcRoots.getRoots()[0]));
        final CompilerOptionsQueryImplementation.Result r = impl.getOptions(testRoots.getRoots()[0]);
        assertNotNull(r);
        final List<? extends String> args = r.getArguments();
        assertEquals(
            Arrays.asList(
                String.format("-XD-Xmodule:%s", srcModuleName),    //NOI18N
                "--add-reads",                                  //NOI18N
                String.format("%s=ALL-UNNAMED", srcModuleName)  //NOI18N
            ),
            args);
    }

    @Ignore // TODO failure
    public void testJDK9_TestModule() throws IOException {
        setSourceLevel(project, "9"); //NOI18N
        final String srcModuleName = "org.nb.App";  //NOI18N
        final String testModuleName = "org.nb.AppTest";  //NOI18N
        createModuleInfo(srcRoots.getRoots()[0], srcModuleName);
        createModuleInfo(testRoots.getRoots()[0], testModuleName);
        final CompilerOptionsQueryImplementation impl = QuerySupport.createUnitTestsCompilerOptionsQuery(project.getEvaluator(), srcRoots, testRoots);
        assertNotNull(impl);
        assertNull(impl.getOptions(srcRoots.getRoots()[0]));
        final CompilerOptionsQueryImplementation.Result r = impl.getOptions(testRoots.getRoots()[0]);
        assertNotNull(r);
        final List<? extends String> args = r.getArguments();
        assertEquals(
            Arrays.asList(
                "--add-reads",  //NOI18N
                String.format("%s=ALL-UNNAMED", testModuleName) //NOI18N
            ),
            args);
    }

    public void testExplicitArgs() {
        setSourceLevel(project, "9"); //NOI18N
        final List<String> options = Arrays.asList(
                "-Xlint:unchecked",     //NOI18N
                "-Xlint:deprecation"    //NOI18N
        );
        setTestJavacArgs(project,options.stream().collect(Collectors.joining(" ")));    //NOI18N
        final CompilerOptionsQueryImplementation impl = QuerySupport.createUnitTestsCompilerOptionsQuery(project.getEvaluator(), srcRoots, testRoots);
        assertNotNull(impl);
        assertNull(impl.getOptions(srcRoots.getRoots()[0]));
        final CompilerOptionsQueryImplementation.Result r = impl.getOptions(testRoots.getRoots()[0]);
        assertNotNull(r);
        final List<? extends String> args = r.getArguments();
        assertEquals(options, args);
    }

    @Ignore // TODO failure
    public void testSourceLevelChanges() throws IOException {
        setSourceLevel(project, "1.8"); //NOI18N
        final String srcModuleName = "org.nb.App";  //NOI18N
        createModuleInfo(srcRoots.getRoots()[0], srcModuleName);
        final CompilerOptionsQueryImplementation impl = QuerySupport.createUnitTestsCompilerOptionsQuery(project.getEvaluator(), srcRoots, testRoots);
        assertNotNull(impl);
        final CompilerOptionsQueryImplementation.Result r = impl.getOptions(testRoots.getRoots()[0]);
        assertNotNull(r);
        List<? extends String> args = r.getArguments();
        assertEquals(Collections.emptyList(), args);
        final MockChangeListener mcl = new MockChangeListener();
        r.addChangeListener(mcl);
        setSourceLevel(project, "9"); //NOI18N
        mcl.assertEventCount(1);
        args = r.getArguments();
        assertEquals(
            Arrays.asList(
                String.format("-XD-Xmodule:%s", srcModuleName),    //NOI18N
                "--add-reads",                                  //NOI18N
                String.format("%s=ALL-UNNAMED", srcModuleName)  //NOI18N
            ),
            args);
    }

    @Ignore // TODO failure
    public void testRootsChanges() throws IOException {
        setSourceLevel(project, "9"); //NOI18N
        final FileObject src2 = srcRoots.getRoots()[0].getParent().createFolder("src2");
        final String srcModuleName = "org.nb.App";  //NOI18N
        createModuleInfo(src2, srcModuleName);
        final CompilerOptionsQueryImplementation impl = QuerySupport.createUnitTestsCompilerOptionsQuery(project.getEvaluator(), srcRoots, testRoots);
        assertNotNull(impl);
        final CompilerOptionsQueryImplementation.Result r = impl.getOptions(testRoots.getRoots()[0]);
        assertNotNull(r);
        List<? extends String> args = r.getArguments();
        assertEquals(Collections.emptyList(), args);
        final MockChangeListener mcl = new MockChangeListener();
        r.addChangeListener(mcl);
        srcRoots.putRoots(new URL[]{
            srcRoots.getRootURLs()[0],
            src2.toURL()
        }, new String[]{
            srcRoots.getRootNames()[0],
            src2.getName()
        });
        mcl.assertEvent();  //Actually 2 events may come (1 for src, 1 for tests)
        args = r.getArguments();
        assertEquals(
            Arrays.asList(
                String.format("-XD-Xmodule:%s", srcModuleName),    //NOI18N
                "--add-reads",  //NOI18N
                String.format("%s=ALL-UNNAMED", srcModuleName) //NOI18N
            ),
            args);
    }

    @Ignore // TODO failure
    public void testModuleInfoCreation() throws IOException {
        setSourceLevel(project, "9"); //NOI18N
        final CompilerOptionsQueryImplementation impl = QuerySupport.createUnitTestsCompilerOptionsQuery(project.getEvaluator(), srcRoots, testRoots);
        assertNotNull(impl);
        assertNull(impl.getOptions(srcRoots.getRoots()[0]));
        final CompilerOptionsQueryImplementation.Result r = impl.getOptions(testRoots.getRoots()[0]);
        assertNotNull(r);
        List<? extends String> args = r.getArguments();
        assertEquals(Collections.emptyList(), args);
        final MockChangeListener mcl = new MockChangeListener();
        r.addChangeListener(mcl);
        final String srcModuleName = "org.nb.App";  //NOI18N
        createModuleInfo(srcRoots.getRoots()[0], srcModuleName);
        mcl.assertEventCount(1);
        args = r.getArguments();
        assertEquals(
            Arrays.asList(
                String.format("-XD-Xmodule:%s", srcModuleName),    //NOI18N
                "--add-reads",                                  //NOI18N
                String.format("%s=ALL-UNNAMED", srcModuleName)  //NOI18N
            ),
            args);
    }

    @Ignore // TODO failure
    public void testModuleInfoChanges() throws IOException {
        setSourceLevel(project, "9"); //NOI18N
        final String srcModuleName = "org.nb.App";  //NOI18N
        createModuleInfo(srcRoots.getRoots()[0], srcModuleName);
        final CompilerOptionsQueryImplementation impl = QuerySupport.createUnitTestsCompilerOptionsQuery(project.getEvaluator(), srcRoots, testRoots);
        assertNotNull(impl);
        assertNull(impl.getOptions(srcRoots.getRoots()[0]));
        final CompilerOptionsQueryImplementation.Result r = impl.getOptions(testRoots.getRoots()[0]);
        assertNotNull(r);
        List<? extends String> args = r.getArguments();
        assertEquals(
            Arrays.asList(
                String.format("-XD-Xmodule:%s", srcModuleName),    //NOI18N
                "--add-reads",                                  //NOI18N
                String.format("%s=ALL-UNNAMED", srcModuleName)  //NOI18N
            ),
            args);
        final MockChangeListener mcl = new MockChangeListener();
        r.addChangeListener(mcl);
        final String newSrcModuleName = "org.nb.App2";  //NOI18N
        createModuleInfo(srcRoots.getRoots()[0], newSrcModuleName);
        mcl.assertEventCount(1);
        args = r.getArguments();
        assertEquals(
            Arrays.asList(
                String.format("-XD-Xmodule:%s", newSrcModuleName),     //NOI18N
                "--add-reads",                                      //NOI18N
                String.format("%s=ALL-UNNAMED", newSrcModuleName)   //NOI18N
            ),
            args);
    }

    public void testJavacTestCompilerargsChanges() {
        setSourceLevel(project, "9"); //NOI18N
        final CompilerOptionsQueryImplementation impl = QuerySupport.createUnitTestsCompilerOptionsQuery(project.getEvaluator(), srcRoots, testRoots);
        assertNotNull(impl);
        final CompilerOptionsQueryImplementation.Result r = impl.getOptions(testRoots.getRoots()[0]);
        assertNotNull(r);
        List<? extends String> args = r.getArguments();
        assertEquals(Collections.emptyList(), args);
        final MockChangeListener mcl = new MockChangeListener();
        r.addChangeListener(mcl);
        final List<String> options = Arrays.asList(
                "-Xlint:unchecked",     //NOI18N
                "-Xlint:deprecation"    //NOI18N
        );
        setTestJavacArgs(project,options.stream().collect(Collectors.joining(" ")));    //NOI18N
        mcl.assertEventCount(1);
        args = r.getArguments();
        assertEquals(options, args);
    }


    private static void setSourceLevel(
        @NonNull final TestProject prj,
        @NonNull final String sourceLevel) {
        assertNotNull(prj);
        assertNotNull(sourceLevel);
        ProjectManager.mutex().writeAccess(()-> {
            try {
                final UpdateHelper helper = prj.getUpdateHelper();
                final EditableProperties ep = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                ep.setProperty(ProjectProperties.JAVAC_SOURCE, sourceLevel);
                ep.setProperty(ProjectProperties.JAVAC_TARGET, sourceLevel);
                helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
                ProjectManager.getDefault().saveProject(prj);
            } catch (IOException ioe) {
                throw new RuntimeException(ioe);
            }
        });
        assertEquals(sourceLevel, SourceLevelQuery.getSourceLevel(prj.getProjectDirectory()));
    }

    private static void setTestJavacArgs(
            @NonNull final TestProject prj,
            @NullAllowed final String options) {
        ProjectManager.mutex().writeAccess(()->{
            try {
                final UpdateHelper h = prj.getUpdateHelper();
                final EditableProperties ep = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                if (options == null) {
                    ep.remove(ProjectProperties.JAVAC_TEST_COMPILERARGS);
                } else {
                    ep.setProperty(ProjectProperties.JAVAC_TEST_COMPILERARGS, options);
                }
                h.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, ep);
                ProjectManager.getDefault().saveProject(prj);
            } catch (IOException ioe) {
                throw new RuntimeException(ioe);
            }
        });
    }

    private static FileObject createModuleInfo(
        @NonNull final FileObject root,
        @NonNull final String moduleName) throws IOException {
        return TestFileUtils.writeFile(
                root,
                "module-info.java", //NOI18N
                String.format(
                    "module %s {}", //NOI18N
                    moduleName));
    }

}
