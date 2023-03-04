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
package org.netbeans.modules.java.api.common.impl;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertNull;
import static junit.framework.TestCase.assertSame;
import static junit.framework.TestCase.assertTrue;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.api.common.SourceRoots;
import org.netbeans.modules.java.api.common.TestProject;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.test.MockLookup;
import org.openide.util.test.MockPropertyChangeListener;

/**
 *
 * @author Tomas Zezula
 */
public class MultiModuleTest extends NbTestCase {

    private FileObject src1;
    private FileObject src2;
    private FileObject mod1a;
    private FileObject mod1b;
    private FileObject mod2c;
    private FileObject mod1d;
    private FileObject mod2d;
    private TestProject tp;
    private ModuleTestUtilities mtu;

    public MultiModuleTest(final String name) {
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
        mod1b = src1.createFolder("lib.util").createFolder("classes");          //NOI18N
        assertNotNull(mod1b);
        mod2c = src2.createFolder("lib.discovery").createFolder("classes");     //NOI18N
        assertNotNull(mod2c);
        mod2d = src2.createFolder("lib.event").createFolder("classes");         //NOI18N
        assertNotNull(mod2d);
        mod1d = src1.createFolder("lib.event").createFolder("classes");         //NOI18N
        assertNotNull(mod1d);
        final Project prj = TestProject.createProject(wd, null, null);
        tp = prj.getLookup().lookup(TestProject.class);
        assertNotNull(tp);
        mtu = ModuleTestUtilities.newInstance(tp);
        assertNotNull(mtu);
    }

    public void testCompositeModuleSourcePath1() throws IOException {
        final FileObject wd = FileUtil.toFileObject(FileUtil.normalizeFile(getWorkDir()));
        final FileObject modulesFolder = wd.createFolder("modules"); //NOI18N
        assertNotNull(modulesFolder);
        final FileObject m1 = modulesFolder.createFolder("m1");         //NOI18N
        assertNotNull(m1);
        final FileObject m2 = modulesFolder.createFolder("m2");         //NOI18N
        assertNotNull(m2);
        assertTrue(mtu.updateModuleRoots(false, "classes:resources", modulesFolder));   //NOI18N
        final SourceRoots sources = mtu.newSourceRoots(false);
        assertEquals(
                Arrays.stream(new FileObject[]{m1, m2})
                .map((fo) -> FileUtil.toFile(fo))
                .flatMap((f) -> Arrays.stream(new File[]{new File(f,"classes"), new File(f,"resources")}))  //NOI18N
                .map((f) -> {
                    return FileUtil.urlForArchiveOrDir(f);
                })
                .sorted((a,b) -> a.toExternalForm().compareTo(b.toExternalForm()))
                .collect(Collectors.toList()),
                Arrays.stream(sources.getRootURLs())
                .sorted((a,b) -> a.toExternalForm().compareTo(b.toExternalForm()))
                .collect(Collectors.toList()));
    }

    public void testCompositeModuleSourcePath2() throws IOException {
        final FileObject wd = FileUtil.toFileObject(FileUtil.normalizeFile(getWorkDir()));
        final FileObject modulesFolder = wd.createFolder("modules"); //NOI18N
        assertNotNull(modulesFolder);
        final FileObject m1 = modulesFolder.createFolder("m1");     //NOI18N
        assertNotNull(m1);
        final FileObject m2 = modulesFolder.createFolder("m2");     //NOI18N
        assertNotNull(m2);
        assertTrue(mtu.updateModuleRoots(false, "{classes,resources}", modulesFolder));   //NOI18N
        final SourceRoots sources = mtu.newSourceRoots(false);
        assertEquals(
                Arrays.stream(new FileObject[]{m1, m2})
                .map((fo) -> FileUtil.toFile(fo))
                .flatMap((f) -> Arrays.stream(new File[]{new File(f,"classes"), new File(f,"resources")}))  //NOI18N
                .map((f) -> {
                    return FileUtil.urlForArchiveOrDir(f);
                })
                .sorted((a,b) -> a.toExternalForm().compareTo(b.toExternalForm()))
                .collect(Collectors.toList()),
                Arrays.stream(sources.getRootURLs())
                .sorted((a,b) -> a.toExternalForm().compareTo(b.toExternalForm()))
                .collect(Collectors.toList()));
    }

    public void testModel() {
        assertTrue(mtu.updateModuleRoots(false, src1,src2));
        final SourceRoots modules = mtu.newModuleRoots(false);
        assertTrue(Arrays.equals(new FileObject[]{src1, src2}, modules.getRoots()));
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
        final MultiModule model = MultiModule.getOrCreate(modules, sources);
        assertNotNull(model);
        final MultiModule model2 = MultiModule.getOrCreate(modules, sources);
        assertSame(model, model2);

        assertNull(model.getModuleSources("foo"));  //NOI18N

        ClassPath scp = model.getModuleSources(mod1a.getParent().getNameExt());
        assertNotNull(scp);
        assertEquals(Arrays.asList(mod1a), Arrays.asList(scp.getRoots()));

        scp = model.getModuleSources(mod1b.getParent().getNameExt());
        assertNotNull(scp);
        assertEquals(Arrays.asList(mod1b), Arrays.asList(scp.getRoots()));

        scp = model.getModuleSources(mod2c.getParent().getNameExt());
        assertNotNull(scp);
        assertEquals(Arrays.asList(mod2c), Arrays.asList(scp.getRoots()));

        scp = model.getModuleSources(mod1d.getParent().getNameExt());
        assertNotNull(scp);
        assertEquals(Arrays.asList(mod1d, mod2d), Arrays.asList(scp.getRoots()));
    }

    public void testModulePathChanges() {
        assertTrue(mtu.updateModuleRoots(false, src1));
        final SourceRoots modules = mtu.newModuleRoots(false);
        assertTrue(Arrays.equals(new FileObject[]{src1}, modules.getRoots()));
        final SourceRoots sources = mtu.newSourceRoots(false);
        assertEquals(
                Arrays.stream(new FileObject[]{mod1a, mod1b, mod1d})
                    .map((fo) -> fo.getPath())
                    .sorted()
                    .collect(Collectors.toList()),
                Arrays.stream(sources.getRoots())
                    .map((fo) -> fo.getPath())
                    .sorted()
                    .collect(Collectors.toList()));
        final MultiModule model = MultiModule.getOrCreate(modules, sources);
        assertNotNull(model);
        ClassPath scp = model.getModuleSources(mod1a.getParent().getNameExt());
        assertNotNull(scp);
        assertEquals(Arrays.asList(mod1a), Arrays.asList(scp.getRoots()));

        scp = model.getModuleSources(mod1b.getParent().getNameExt());
        assertNotNull(scp);
        assertEquals(Arrays.asList(mod1b), Arrays.asList(scp.getRoots()));

        scp = model.getModuleSources(mod1d.getParent().getNameExt());
        assertNotNull(scp);
        assertEquals(Arrays.asList(mod1d), Arrays.asList(scp.getRoots()));

        assertTrue(mtu.updateModuleRoots(false, src1, src2));
        assertTrue(Arrays.equals(new FileObject[]{src1, src2}, modules.getRoots()));
        assertEquals(
                Arrays.stream(new FileObject[]{mod1a, mod1b, mod2c, mod1d, mod2d})
                    .map((fo) -> fo.getPath())
                    .sorted()
                    .collect(Collectors.toList()),
                Arrays.stream(sources.getRoots())
                    .map((fo) -> fo.getPath())
                    .sorted()
                    .collect(Collectors.toList()));
        scp = model.getModuleSources(mod1a.getParent().getNameExt());
        assertNotNull(scp);
        assertEquals(Arrays.asList(mod1a), Arrays.asList(scp.getRoots()));

        scp = model.getModuleSources(mod1b.getParent().getNameExt());
        assertNotNull(scp);
        assertEquals(Arrays.asList(mod1b), Arrays.asList(scp.getRoots()));

        scp = model.getModuleSources(mod2c.getParent().getNameExt());
        assertNotNull(scp);
        assertEquals(Arrays.asList(mod2c), Arrays.asList(scp.getRoots()));

        scp = model.getModuleSources(mod1d.getParent().getNameExt());
        assertNotNull(scp);
        assertEquals(Arrays.asList(mod1d, mod2d), Arrays.asList(scp.getRoots()));

        assertTrue(mtu.updateModuleRoots(false, src2));
        assertTrue(Arrays.equals(new FileObject[]{src2}, modules.getRoots()));
        assertEquals(
                Arrays.stream(new FileObject[]{mod2c, mod2d})
                    .map((fo) -> fo.getPath())
                    .sorted()
                    .collect(Collectors.toList()),
                Arrays.stream(sources.getRoots())
                    .map((fo) -> fo.getPath())
                    .sorted()
                    .collect(Collectors.toList()));
        scp = model.getModuleSources(mod2c.getParent().getNameExt());
        assertNotNull(scp);
        assertEquals(Arrays.asList(mod2c), Arrays.asList(scp.getRoots()));

        scp = model.getModuleSources(mod2d.getParent().getNameExt());
        assertNotNull(scp);
        assertEquals(Arrays.asList(mod2d), Arrays.asList(scp.getRoots()));

        scp = model.getModuleSources(mod1a.getParent().getNameExt());
        assertNull(scp);
    }

    public void testModulePathChangesFires() {
        assertTrue(mtu.updateModuleRoots(false, src1));
        final SourceRoots modules = mtu.newModuleRoots(false);
        assertTrue(Arrays.equals(new FileObject[]{src1}, modules.getRoots()));
        final SourceRoots sources = mtu.newSourceRoots(false);
        assertEquals(
                Arrays.stream(new FileObject[]{mod1a, mod1b, mod1d})
                    .map((fo) -> fo.getPath())
                    .sorted()
                    .collect(Collectors.toList()),
                Arrays.stream(sources.getRoots())
                    .map((fo) -> fo.getPath())
                    .sorted()
                    .collect(Collectors.toList()));
        final MultiModule model = MultiModule.getOrCreate(modules, sources);
        assertNotNull(model);
        ClassPath scp = model.getModuleSources(mod1a.getParent().getNameExt());
        assertNotNull(scp);
        assertEquals(Arrays.asList(mod1a), Arrays.asList(scp.getRoots()));

        scp = model.getModuleSources(mod1b.getParent().getNameExt());
        assertNotNull(scp);
        assertEquals(Arrays.asList(mod1b), Arrays.asList(scp.getRoots()));

        scp = model.getModuleSources(mod1d.getParent().getNameExt());
        assertNotNull(scp);
        assertEquals(Arrays.asList(mod1d), Arrays.asList(scp.getRoots()));

        final MockPropertyChangeListener l = new MockPropertyChangeListener(MultiModule.PROP_MODULES);
        model.addPropertyChangeListener(l);
        assertTrue(mtu.updateModuleRoots(false, src1, src2));
        l.assertEventCount(1);
        assertTrue(mtu.updateModuleRoots(false, src2));
        l.assertEventCount(1);
    }

    public void testModulesSetChanges() throws IOException {
        assertTrue(mtu.updateModuleRoots(false, src2));
        final SourceRoots modules = mtu.newModuleRoots(false);
        assertTrue(Arrays.equals(new FileObject[]{src2}, modules.getRoots()));
        final SourceRoots sources = mtu.newSourceRoots(false);
        assertEquals(
                Arrays.stream(new FileObject[]{mod2c, mod2d})
                    .map((fo) -> fo.getPath())
                    .sorted()
                    .collect(Collectors.toList()),
                Arrays.stream(sources.getRoots())
                    .map((fo) -> fo.getPath())
                    .sorted()
                    .collect(Collectors.toList()));
        final MultiModule model = MultiModule.getOrCreate(modules, sources);
        assertNotNull(model);
        ClassPath scp = model.getModuleSources(mod2c.getParent().getNameExt());
        assertNotNull(scp);
        assertEquals(Arrays.asList(mod2c), Arrays.asList(scp.getRoots()));

        scp = model.getModuleSources(mod2d.getParent().getNameExt());
        assertNotNull(scp);
        assertEquals(Arrays.asList(mod2d), Arrays.asList(scp.getRoots()));

        final String newModName = "lib.temp";   //NOI18N
        scp = model.getModuleSources(newModName);
        assertNull(scp);
        final FileObject mod2e = src2.createFolder(newModName).createFolder("classes");         //NOI18N
        scp = model.getModuleSources(newModName);
        assertNotNull(scp);
        assertEquals(Arrays.asList(mod2e), Arrays.asList(scp.getRoots()));
        mod2e.getParent().delete();
        scp = model.getModuleSources(newModName);
        assertNull(scp);
    }

    public void testModulesSetChangesFires() throws IOException {
        assertTrue(mtu.updateModuleRoots(false, src2));
        final SourceRoots modules = mtu.newModuleRoots(false);
        assertTrue(Arrays.equals(new FileObject[]{src2}, modules.getRoots()));
        final SourceRoots sources = mtu.newSourceRoots(false);
        assertEquals(
                Arrays.stream(new FileObject[]{mod2c, mod2d})
                    .map((fo) -> fo.getPath())
                    .sorted()
                    .collect(Collectors.toList()),
                Arrays.stream(sources.getRoots())
                    .map((fo) -> fo.getPath())
                    .sorted()
                    .collect(Collectors.toList()));
        final MultiModule model = MultiModule.getOrCreate(modules, sources);
        assertNotNull(model);
        ClassPath scp = model.getModuleSources(mod2c.getParent().getNameExt());
        assertNotNull(scp);
        assertEquals(Arrays.asList(mod2c), Arrays.asList(scp.getRoots()));

        scp = model.getModuleSources(mod2d.getParent().getNameExt());
        assertNotNull(scp);
        assertEquals(Arrays.asList(mod2d), Arrays.asList(scp.getRoots()));

        final MockPropertyChangeListener l = new MockPropertyChangeListener(MultiModule.PROP_MODULES);
        model.addPropertyChangeListener(l);
        final String newModName = "lib.temp";   //NOI18N
        final FileObject mod2e = src2.createFolder(newModName).createFolder("classes");         //NOI18N
        l.assertEventCount(1);
        scp = model.getModuleSources(newModName);
        assertNotNull(scp);
        assertEquals(Arrays.asList(mod2e), Arrays.asList(scp.getRoots()));
        final MockPropertyChangeListener cpl = new MockPropertyChangeListener();
        scp.addPropertyChangeListener(cpl);
        mod2e.getParent().delete();
        l.assertEventCount(1);
        cpl.assertEvents(ClassPath.PROP_ENTRIES, ClassPath.PROP_ROOTS);
    }

    public void testModuleSourcesChanges() throws IOException {
        final FileObject wd = FileUtil.toFileObject(FileUtil.normalizeFile(getWorkDir()));
        final FileObject modulesFolder = wd.createFolder("modules"); //NOI18N
        assertNotNull(modulesFolder);
        final FileObject classesFolder = modulesFolder.createFolder("module").createFolder("classes");        //NOI18N
        assertTrue(mtu.updateModuleRoots(false, "classes:resources",modulesFolder));   //NOI18N
        final SourceRoots modules = mtu.newModuleRoots(false);
        assertTrue(Arrays.equals(new FileObject[]{modulesFolder}, modules.getRoots()));
        final SourceRoots sources = mtu.newSourceRoots(false);
        assertEquals(
                Arrays.stream(new FileObject[]{classesFolder})
                    .map((fo) -> fo.getPath())
                    .sorted()
                    .collect(Collectors.toList()),
                Arrays.stream(sources.getRoots())
                    .map((fo) -> fo.getPath())
                    .sorted()
                    .collect(Collectors.toList()));
        final MultiModule model = MultiModule.getOrCreate(modules, sources);
        assertNotNull(model);
        ClassPath scp = model.getModuleSources("module");   //NOI18N
        assertNotNull(scp);
        assertEquals(Arrays.asList(classesFolder), Arrays.asList(scp.getRoots()));

        final FileObject resourcesFolder = modulesFolder.getFileObject("module").createFolder("resources");        //NOI18N
        assertEquals(Arrays.asList(classesFolder, resourcesFolder), Arrays.asList(scp.getRoots()));

        classesFolder.delete();
        assertEquals(Arrays.asList(resourcesFolder), Arrays.asList(scp.getRoots()));
    }

    public void testModuleSourcesChangesFires() throws IOException {
        final FileObject wd = FileUtil.toFileObject(FileUtil.normalizeFile(getWorkDir()));
        final FileObject modulesFolder = wd.createFolder("modules"); //NOI18N
        assertNotNull(modulesFolder);
        final FileObject classesFolder = modulesFolder.createFolder("module").createFolder("classes");        //NOI18N
        assertTrue(mtu.updateModuleRoots(false, "classes:resources",modulesFolder));   //NOI18N
        final SourceRoots modules = mtu.newModuleRoots(false);
        assertTrue(Arrays.equals(new FileObject[]{modulesFolder}, modules.getRoots()));
        final SourceRoots sources = mtu.newSourceRoots(false);
        assertEquals(
                Arrays.stream(new FileObject[]{classesFolder})
                    .map((fo) -> fo.getPath())
                    .sorted()
                    .collect(Collectors.toList()),
                Arrays.stream(sources.getRoots())
                    .map((fo) -> fo.getPath())
                    .sorted()
                    .collect(Collectors.toList()));
        final MultiModule model = MultiModule.getOrCreate(modules, sources);
        assertNotNull(model);
        ClassPath scp = model.getModuleSources("module");   //NOI18N
        assertNotNull(scp);
        assertEquals(Arrays.asList(classesFolder), Arrays.asList(scp.getRoots()));

        final MockPropertyChangeListener l = new MockPropertyChangeListener();
        scp.addPropertyChangeListener(l);
        final FileObject resourcesFolder = modulesFolder.getFileObject("module").createFolder("resources");        //NOI18N
        l.assertEvents(ClassPath.PROP_ROOTS);

        classesFolder.delete();
        l.assertEvents(ClassPath.PROP_ROOTS);
    }
}
