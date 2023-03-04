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
import java.util.Arrays;
import java.util.stream.Collectors;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;
import org.netbeans.api.project.Project;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.api.common.SourceRoots;
import org.netbeans.modules.java.api.common.TestProject;
import org.netbeans.modules.java.api.common.impl.ModuleTestUtilities;
import org.netbeans.modules.java.api.common.impl.MultiModule;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.test.MockLookup;

/**
 *
 * @author Tomas Zezula
 */
public class MultiModuleUnitTestForSourceQueryImplTest extends NbTestCase {

    private FileObject src1;
    private FileObject src2;
    private FileObject mod1a;
    private FileObject mod1b;
    private FileObject mod2c;
    private FileObject mod1d;
    private FileObject mod2d;
    private FileObject mod1aTests;
    private FileObject mod1bTests;
    private FileObject mod2cTests;
    private FileObject mod1dTests;
    private FileObject mod2dTests;
    private TestProject tp;
    private ModuleTestUtilities mtu;
    private MultiModuleUnitTestForSourceQueryImpl impl;

    public MultiModuleUnitTestForSourceQueryImplTest(final String name) {
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
        src2 = wd.createFolder("src2"); //NOI18N
        assertNotNull(src2);
        mod1a = src1.createFolder("lib.common").createFolder("classes");        //NOI18N
        assertNotNull(mod1a);
        mod1aTests = src1.getFileObject("lib.common").createFolder("tests");        //NOI18N
        mod1b = src1.createFolder("lib.util").createFolder("classes");          //NOI18N
        assertNotNull(mod1b);
        mod1bTests = src1.getFileObject("lib.util").createFolder("tests");          //NOI18N
        assertNotNull(mod1bTests);
        mod2c = src2.createFolder("lib.discovery").createFolder("classes");     //NOI18N
        assertNotNull(mod2c);
        mod2cTests = src2.getFileObject("lib.discovery").createFolder("tests");     //NOI18N
        assertNotNull(mod2cTests);
        mod2d = src2.createFolder("lib.event").createFolder("classes");         //NOI18N
        assertNotNull(mod2d);
        mod2dTests = src2.getFileObject("lib.event").createFolder("tests");     //NOI18N
        assertNotNull(mod2dTests);
        mod1d = src1.createFolder("lib.event").createFolder("classes");         //NOI18N
        assertNotNull(mod1d);
        mod1dTests = src1.getFileObject("lib.event").createFolder("tests");         //NOI18N
        assertNotNull(mod1dTests);
        final Project prj = TestProject.createMultiModuleProject(wd);
        tp = prj.getLookup().lookup(TestProject.class);
        assertNotNull(tp);
        mtu = ModuleTestUtilities.newInstance(tp);
        assertNotNull(mtu);
        assertTrue(mtu.updateModuleRoots(false, src1,src2));
        assertTrue(mtu.updateModuleRoots(true, src1,src2));
        final SourceRoots modules = mtu.newModuleRoots(false);
        assertTrue(Arrays.equals(new FileObject[]{src1, src2}, modules.getRoots()));
        final SourceRoots testModules = mtu.newModuleRoots(true);
        assertTrue(Arrays.equals(new FileObject[]{src1, src2}, testModules.getRoots()));
        final SourceRoots sources = mtu.newSourceRoots(false);
        assertEquals(
                Arrays.stream(new FileObject[]{mod1a, mod1b, mod2c, mod1d, mod2d})
                    .map((fo) -> fo.getPath())
                    .sorted()
                    .collect(Collectors.toList()),
                Arrays.stream(sources.getRoots())
                    .map((fo) -> fo.getPath())
                    .sorted()
                    .collect(Collectors.toList()));
        final SourceRoots testSources = mtu.newSourceRoots(true);
        assertEquals(
                Arrays.stream(new FileObject[]{mod1aTests, mod1bTests, mod2cTests, mod1dTests, mod2dTests})
                    .map((fo) -> fo.getPath())
                    .sorted()
                    .collect(Collectors.toList()),
                Arrays.stream(testSources.getRoots())
                    .map((fo) -> fo.getPath())
                    .sorted()
                    .collect(Collectors.toList()));
        final MultiModule model = MultiModule.getOrCreate(modules, sources);
        final MultiModule testModel = MultiModule.getOrCreate(testModules, testSources);
        impl = new MultiModuleUnitTestForSourceQueryImpl(model, testModel);
    }

    public void testFindUnitTests() throws IOException {
        assertNotNull(impl);
        assertNull(impl.findUnitTests(src1));
        assertNull(impl.findUnitTests(src2));
        assertEquals(
                Arrays.stream(new FileObject[]{mod1aTests})
                    .map((fo) -> fo.getPath())
                    .sorted()
                    .collect(Collectors.toList()),
                Arrays.stream(impl.findUnitTests(mod1a))
                    .map((url) -> URLMapper.findFileObject(url))
                    .map((fo) -> fo.getPath())
                    .sorted()
                    .collect(Collectors.toList()));

        final FileObject javaFile = FileUtil.createData(mod1a, "org/me/Foo.java");  //NOI18N
        assertEquals(
                Arrays.stream(new FileObject[]{mod1aTests})
                    .map((fo) -> fo.getPath())
                    .sorted()
                    .collect(Collectors.toList()),
                Arrays.stream(impl.findUnitTests(javaFile))
                    .map((url) -> URLMapper.findFileObject(url))
                    .map((fo) -> fo.getPath())
                    .sorted()
                    .collect(Collectors.toList()));
        assertEquals(
                Arrays.stream(new FileObject[]{mod1bTests})
                    .map((fo) -> fo.getPath())
                    .sorted()
                    .collect(Collectors.toList()),
                Arrays.stream(impl.findUnitTests(mod1b))
                    .map((url) -> URLMapper.findFileObject(url))
                    .map((fo) -> fo.getPath())
                    .sorted()
                    .collect(Collectors.toList()));
        assertEquals(
                Arrays.stream(new FileObject[]{mod2cTests})
                    .map((fo) -> fo.getPath())
                    .sorted()
                    .collect(Collectors.toList()),
                Arrays.stream(impl.findUnitTests(mod2c))
                    .map((url) -> URLMapper.findFileObject(url))
                    .map((fo) -> fo.getPath())
                    .sorted()
                    .collect(Collectors.toList()));
        assertEquals(
                Arrays.stream(new FileObject[]{mod1dTests, mod2dTests})
                    .map((fo) -> fo.getPath())
                    .sorted()
                    .collect(Collectors.toList()),
                Arrays.stream(impl.findUnitTests(mod1d))
                    .map((url) -> URLMapper.findFileObject(url))
                    .map((fo) -> fo.getPath())
                    .sorted()
                    .collect(Collectors.toList()));
        assertEquals(
                Arrays.stream(new FileObject[]{mod1dTests, mod2dTests})
                    .map((fo) -> fo.getPath())
                    .sorted()
                    .collect(Collectors.toList()),
                Arrays.stream(impl.findUnitTests(mod2d))
                    .map((url) -> URLMapper.findFileObject(url))
                    .map((fo) -> fo.getPath())
                    .sorted()
                    .collect(Collectors.toList()));
    }

    public void testFindSources() throws IOException {
        assertNotNull(impl);
        assertNull(impl.findSources(src1));
        assertNull(impl.findSources(src2));
        assertEquals(
                Arrays.stream(new FileObject[]{mod1a})
                    .map((fo) -> fo.getPath())
                    .sorted()
                    .collect(Collectors.toList()),
                Arrays.stream(impl.findSources(mod1aTests))
                    .map((url) -> URLMapper.findFileObject(url))
                    .map((fo) -> fo.getPath())
                    .sorted()
                    .collect(Collectors.toList()));
        final FileObject testFile = FileUtil.createData(mod1aTests, "org/me/FooTest.java");  //NOI18N
        assertEquals(
                Arrays.stream(new FileObject[]{mod1a})
                    .map((fo) -> fo.getPath())
                    .sorted()
                    .collect(Collectors.toList()),
                Arrays.stream(impl.findSources(testFile))
                    .map((url) -> URLMapper.findFileObject(url))
                    .map((fo) -> fo.getPath())
                    .sorted()
                    .collect(Collectors.toList()));
        assertEquals(
                Arrays.stream(new FileObject[]{mod1b})
                    .map((fo) -> fo.getPath())
                    .sorted()
                    .collect(Collectors.toList()),
                Arrays.stream(impl.findSources(mod1bTests))
                    .map((url) -> URLMapper.findFileObject(url))
                    .map((fo) -> fo.getPath())
                    .sorted()
                    .collect(Collectors.toList()));
        assertEquals(
                Arrays.stream(new FileObject[]{mod2c})
                    .map((fo) -> fo.getPath())
                    .sorted()
                    .collect(Collectors.toList()),
                Arrays.stream(impl.findSources(mod2cTests))
                    .map((url) -> URLMapper.findFileObject(url))
                    .map((fo) -> fo.getPath())
                    .sorted()
                    .collect(Collectors.toList()));
        assertEquals(
                Arrays.stream(new FileObject[]{mod1d, mod2d})
                    .map((fo) -> fo.getPath())
                    .sorted()
                    .collect(Collectors.toList()),
                Arrays.stream(impl.findSources(mod1dTests))
                    .map((url) -> URLMapper.findFileObject(url))
                    .map((fo) -> fo.getPath())
                    .sorted()
                    .collect(Collectors.toList()));
        assertEquals(
                Arrays.stream(new FileObject[]{mod1d, mod2d})
                    .map((fo) -> fo.getPath())
                    .sorted()
                    .collect(Collectors.toList()),
                Arrays.stream(impl.findSources(mod2dTests))
                    .map((url) -> URLMapper.findFileObject(url))
                    .map((fo) -> fo.getPath())
                    .sorted()
                    .collect(Collectors.toList()));
    }
}
