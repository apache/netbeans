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
package org.netbeans.modules.java.api.common.queries;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.api.common.SourceRoots;
import org.netbeans.modules.java.api.common.TestProject;
import org.netbeans.modules.java.api.common.impl.ModuleTestUtilities;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.spi.java.queries.CompilerOptionsQueryImplementation;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.test.MockLookup;


/**
 * Test for {@link MultiModuleUnitTestsCompilerOptionsQueryImpl}.
 * @author Tomas Zezula
 * Todo:
 *  add/remove module test root
 *  events
 */
public class MultiModuleUnitTestsCompilerOptionsQueryImplTest extends NbTestCase {

    //Module Source Path
    private FileObject src1;
    //Source Path
    private FileObject mod1a;   // in src1
    private FileObject mod1b;   // in src1
    //Unit tests
    private FileObject mod1aTests;   // in src1
    private FileObject mod1bTests;   // in src1

    private TestProject tp;
    private SourceRoots modules;
    private SourceRoots sources;
    private SourceRoots testModules;
    private SourceRoots testSources;
    private ModuleTestUtilities mtu;
    private CompilerOptionsQueryImplementation query;

    public MultiModuleUnitTestsCompilerOptionsQueryImplTest(final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        MockLookup.setInstances(TestProject.createProjectType());
        final FileObject wd = FileUtil.toFileObject(FileUtil.normalizeFile(getWorkDir()));
        src1 = wd.createFolder("src1"); //NOI18N
        assertNotNull(src1);
        mod1a = src1.createFolder("lib.common").createFolder("classes");        //NOI18N
        assertNotNull(mod1a);
        createModuleInfo(mod1a, "lib.common", "java.base");
        mod1b = src1.createFolder("lib.util").createFolder("classes");          //NOI18N
        assertNotNull(mod1b);
        createModuleInfo(mod1b, "lib.util", "java.base","java.logging");      //NOI18N
        mod1aTests = src1.getFileObject("lib.common").createFolder("tests");        //NOI18N
        assertNotNull(mod1aTests);
        mod1bTests = src1.getFileObject("lib.util").createFolder("tests");          //NOI18N
        assertNotNull(mod1bTests);

        final Project prj = TestProject.createProject(wd, null, null);
        tp = prj.getLookup().lookup(TestProject.class);
        assertNotNull(tp);
        mtu = ModuleTestUtilities.newInstance(tp);
        assertNotNull(mtu);

        //Set module roots
        assertTrue(mtu.updateModuleRoots(false, src1));
        modules = mtu.newModuleRoots(false);
        assertTrue(Arrays.equals(new FileObject[]{src1}, modules.getRoots()));
        sources = mtu.newSourceRoots(false);
        assertTrue(mtu.updateModuleRoots(true, src1));
        testModules = mtu.newModuleRoots(true);
        assertTrue(Arrays.equals(new FileObject[]{src1}, testModules.getRoots()));
        testSources = mtu.newSourceRoots(true);
        setSourceLevel("9");    //NOI18N
        query = QuerySupport.createMultiModuleUnitTestsCompilerOptionsQuery(prj, modules, sources, testModules, testSources);
    }


    public void testCompilerOptions() {
        assertNotNull(query);
        assertNull(query.getOptions(src1));
        assertNull(query.getOptions(mod1a));
        assertNull(query.getOptions(mod1b));
        CompilerOptionsQueryImplementation.Result res = query.getOptions(mod1aTests);
        assertNotNull(res);
        assertEquals(
            newSet(
                String.format("--patch-module lib.common=%s", FileUtil.toFile(mod1aTests).getAbsolutePath()),   //NOI18N
                String.format("--patch-module lib.util=%s", FileUtil.toFile(mod1bTests).getAbsolutePath()),   //NOI18N
                Stream.of(mod1a, mod1b).map(FileObject::getParent).map(FileObject::getNameExt).collect(Collectors.joining(",", "--add-modules ", "")),  //NOI18N
                "--add-reads lib.common=ALL-UNNAMED",   //NOI18N
                "--add-reads lib.util=ALL-UNNAMED"),   //NOI18N
            newPairs(res.getArguments()));
        res = query.getOptions(mod1bTests);
        assertNotNull(res);
        assertEquals(
            newSet(
                String.format("--patch-module lib.common=%s", FileUtil.toFile(mod1aTests).getAbsolutePath()),   //NOI18N
                String.format("--patch-module lib.util=%s", FileUtil.toFile(mod1bTests).getAbsolutePath()),   //NOI18N
                Stream.of(mod1a, mod1b).map(FileObject::getParent).map(FileObject::getNameExt).collect(Collectors.joining(",", "--add-modules ", "")),  //NOI18N
                "--add-reads lib.common=ALL-UNNAMED",   //NOI18N
                "--add-reads lib.util=ALL-UNNAMED"),   //NOI18N
            newPairs(res.getArguments()));
    }

    public void testSourceLevel() {
        assertNotNull(query);
        setSourceLevel("1.8");  //NOI18N
        CompilerOptionsQueryImplementation.Result res = query.getOptions(mod1aTests);
        assertNotNull(res);
        res = query.getOptions(mod1bTests);
        assertNotNull(res);
        setSourceLevel("9");    //NOI18N
        res = query.getOptions(mod1aTests);
        assertNotNull(res);
        assertEquals(
            newSet(
                String.format("--patch-module lib.common=%s", FileUtil.toFile(mod1aTests).getAbsolutePath()),   //NOI18N
                String.format("--patch-module lib.util=%s", FileUtil.toFile(mod1bTests).getAbsolutePath()),   //NOI18N
                Stream.of(mod1a, mod1b).map(FileObject::getParent).map(FileObject::getNameExt).collect(Collectors.joining(",", "--add-modules ", "")),  //NOI18N
                "--add-reads lib.common=ALL-UNNAMED",   //NOI18N
                "--add-reads lib.util=ALL-UNNAMED"),   //NOI18N
            newPairs(res.getArguments()));
        res = query.getOptions(mod1bTests);
        assertNotNull(res);
        assertEquals(
            newSet(
                String.format("--patch-module lib.common=%s", FileUtil.toFile(mod1aTests).getAbsolutePath()),   //NOI18N
                String.format("--patch-module lib.util=%s", FileUtil.toFile(mod1bTests).getAbsolutePath()),   //NOI18N
                Stream.of(mod1a, mod1b).map(FileObject::getParent).map(FileObject::getNameExt).collect(Collectors.joining(",", "--add-modules ", "")),  //NOI18N
                "--add-reads lib.common=ALL-UNNAMED",   //NOI18N
                "--add-reads lib.util=ALL-UNNAMED"),   //NOI18N
            newPairs(res.getArguments()));
    }

    public void testModuleSetChanges() throws IOException {
        assertNotNull(query);
        CompilerOptionsQueryImplementation.Result res = query.getOptions(mod1aTests);
        assertNotNull(res);
        assertEquals(
            newSet(
                String.format("--patch-module lib.common=%s", FileUtil.toFile(mod1aTests).getAbsolutePath()),   //NOI18N
                String.format("--patch-module lib.util=%s", FileUtil.toFile(mod1bTests).getAbsolutePath()),   //NOI18N
                Stream.of(mod1a, mod1b).map(FileObject::getParent).map(FileObject::getNameExt).collect(Collectors.joining(",", "--add-modules ", "")),  //NOI18N
                "--add-reads lib.common=ALL-UNNAMED",   //NOI18N
                "--add-reads lib.util=ALL-UNNAMED"),   //NOI18N
            newPairs(res.getArguments()));
        final FileObject mod1c = src1.createFolder("lib.foo").createFolder("classes");        //NOI18N
        assertNotNull(mod1c);
        createModuleInfo(mod1c, "lib.foo", "java.base");
        final FileObject mod1cTests = src1.getFileObject("lib.foo").createFolder("tests");        //NOI18N
        assertNotNull(mod1cTests);
        res = query.getOptions(mod1aTests);
        assertNotNull(res);
        assertEquals(
            newSet(
                String.format("--patch-module lib.common=%s", FileUtil.toFile(mod1aTests).getAbsolutePath()),   //NOI18N
                String.format("--patch-module lib.util=%s", FileUtil.toFile(mod1bTests).getAbsolutePath()),   //NOI18N
                String.format("--patch-module lib.foo=%s", FileUtil.toFile(mod1cTests).getAbsolutePath()),   //NOI18N
                Stream.of(mod1a, mod1c, mod1b).map(FileObject::getParent).map(FileObject::getNameExt).collect(Collectors.joining(",", "--add-modules ", "")),  //NOI18N
                "--add-reads lib.common=ALL-UNNAMED",   //NOI18N
                "--add-reads lib.util=ALL-UNNAMED",     //NOI18N
                "--add-reads lib.foo=ALL-UNNAMED"),   //NOI18N
            newPairs(res.getArguments()));
        res = query.getOptions(mod1cTests);
        assertNotNull(res);
        assertEquals(
            newSet(
                String.format("--patch-module lib.common=%s", FileUtil.toFile(mod1aTests).getAbsolutePath()),   //NOI18N
                String.format("--patch-module lib.util=%s", FileUtil.toFile(mod1bTests).getAbsolutePath()),   //NOI18N
                String.format("--patch-module lib.foo=%s", FileUtil.toFile(mod1cTests).getAbsolutePath()),   //NOI18N
                Stream.of(mod1a, mod1c, mod1b).map(FileObject::getParent).map(FileObject::getNameExt).collect(Collectors.joining(",", "--add-modules ", "")),  //NOI18N
                "--add-reads lib.common=ALL-UNNAMED",   //NOI18N
                "--add-reads lib.util=ALL-UNNAMED",     //NOI18N
                "--add-reads lib.foo=ALL-UNNAMED"),   //NOI18N
            newPairs(res.getArguments()));
        mod1c.getParent().delete();
        res = query.getOptions(mod1aTests);
        assertNotNull(res);
        assertEquals(
            newSet(
                String.format("--patch-module lib.common=%s", FileUtil.toFile(mod1aTests).getAbsolutePath()),   //NOI18N
                String.format("--patch-module lib.util=%s", FileUtil.toFile(mod1bTests).getAbsolutePath()),   //NOI18N
                Stream.of(mod1a, mod1b).map(FileObject::getParent).map(FileObject::getNameExt).collect(Collectors.joining(",", "--add-modules ", "")),  //NOI18N
                "--add-reads lib.common=ALL-UNNAMED",   //NOI18N
                "--add-reads lib.util=ALL-UNNAMED"),   //NOI18N
            newPairs(res.getArguments()));
    }

    public void testTestModuleInfoChanges() throws IOException {
        assertNotNull(query);
        CompilerOptionsQueryImplementation.Result res = query.getOptions(mod1aTests);
        assertNotNull(res);
        assertEquals(
            newSet(
                String.format("--patch-module lib.common=%s", FileUtil.toFile(mod1aTests).getAbsolutePath()),   //NOI18N
                String.format("--patch-module lib.util=%s", FileUtil.toFile(mod1bTests).getAbsolutePath()),   //NOI18N
                Stream.of(mod1a, mod1b).map(FileObject::getParent).map(FileObject::getNameExt).collect(Collectors.joining(",", "--add-modules ", "")),  //NOI18N
                "--add-reads lib.common=ALL-UNNAMED",   //NOI18N
                "--add-reads lib.util=ALL-UNNAMED"),   //NOI18N
            newPairs(res.getArguments()));
        final FileObject modInfo = createModuleInfo(mod1aTests, "lib.common.tests", "java.base");
        res = query.getOptions(mod1aTests);
        assertNotNull(res);
        assertEquals(
            newSet(
                String.format("--patch-module lib.util=%s", FileUtil.toFile(mod1bTests).getAbsolutePath()),   //NOI18N
                Stream.of(mod1a, mod1b).map(FileObject::getParent).map(FileObject::getNameExt).collect(Collectors.joining(",", "--add-modules ", "")),  //NOI18N
                "--add-reads lib.common=ALL-UNNAMED",   //NOI18N
                "--add-reads lib.util=ALL-UNNAMED"),   //NOI18N
            newPairs(res.getArguments()));
        modInfo.delete();
        res = query.getOptions(mod1aTests);
        assertNotNull(res);
        assertEquals(
            newSet(
                String.format("--patch-module lib.common=%s", FileUtil.toFile(mod1aTests).getAbsolutePath()),   //NOI18N
                String.format("--patch-module lib.util=%s", FileUtil.toFile(mod1bTests).getAbsolutePath()),   //NOI18N
                Stream.of(mod1a, mod1b).map(FileObject::getParent).map(FileObject::getNameExt).collect(Collectors.joining(",", "--add-modules ", "")),  //NOI18N
                "--add-reads lib.common=ALL-UNNAMED",   //NOI18N
                "--add-reads lib.util=ALL-UNNAMED"),   //NOI18N
            newPairs(res.getArguments()));
    }

    private void setSourceLevel(@NonNull final String sl) {
        ProjectManager.mutex(true, tp).writeAccess(() -> {
            final EditableProperties ep = tp.getUpdateHelper().getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
            ep.setProperty(ProjectProperties.JAVAC_SOURCE, sl);
            ep.setProperty(ProjectProperties.JAVAC_TARGET, sl);
            tp.getUpdateHelper().putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        });
    }

    @NonNull
    private static Set<String> newSet(@NonNull final String... args) {
        final Set<String> res = new HashSet<>();
        Collections.addAll(res, args);
        return res;
    }

    @NonNull
    private static Set<String> newPairs(@NonNull final List<? extends String> opts) {
        final Set<String> res = new HashSet<>();
        String f = null;
        for (String o : opts) {
            if (f == null) {
                f = o;
            } else {
                res.add(String.format("%s %s", f, o));
                f = null;
            }
        }
        return res;
    }

    @NonNull
    private static FileObject createModuleInfo(
            @NonNull final FileObject root,
            @NonNull final String moduleName,
            @NonNull final String... requiredModules) throws IOException {
        final FileObject moduleInfo = FileUtil.createData(root, "module-info.java");    //NOI18N
        try(PrintWriter out = new PrintWriter(new OutputStreamWriter(moduleInfo.getOutputStream()))) {
            out.printf("module %s {%n", moduleName);    //NOI18N
            for (String requiredModule : requiredModules) {
                out.printf("    requires %s;%n", requiredModule);    //NOI18N
            }
            out.printf("}");    //NOI18N
        }
        return moduleInfo;
    }
}
