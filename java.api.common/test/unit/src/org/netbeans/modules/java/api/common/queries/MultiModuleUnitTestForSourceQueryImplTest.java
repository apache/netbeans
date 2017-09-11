/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
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
