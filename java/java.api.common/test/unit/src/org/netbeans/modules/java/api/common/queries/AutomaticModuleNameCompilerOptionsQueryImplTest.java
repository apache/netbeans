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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;
import java.util.jar.Manifest;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.api.common.SourceRoots;
import org.netbeans.modules.java.api.common.TestProject;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.spi.java.queries.CompilerOptionsQueryImplementation;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.test.MockLookup;

/**
 *
 * @author Tomas Zezula
 */
public class AutomaticModuleNameCompilerOptionsQueryImplTest extends NbTestCase {
    private TestProject project;
    private SourceRoots srcRoots;

    public AutomaticModuleNameCompilerOptionsQueryImplTest(final String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        MockLookup.setInstances(TestProject.createProjectType());
        final FileObject wd = FileUtil.toFileObject(FileUtil.normalizeFile(getWorkDir()));
        final FileObject src = FileUtil.createFolder(wd,"src"); //NOI18N
        final FileObject tst = FileUtil.createFolder(wd,"test");    //NOI18N
        Project p = TestProject.createProject(wd, src, tst);
        project = p.getLookup().lookup(TestProject.class);
        assertNotNull(project);
        srcRoots = project.getSourceRoots();
        assertNotNull(srcRoots);
        assertEquals(srcRoots.getRoots().length, 1);
        assertEquals(srcRoots.getRoots()[0], src);
    }

    public void testOwner() throws IOException {
        final AutomaticModuleNameCompilerOptionsQueryImpl q = new AutomaticModuleNameCompilerOptionsQueryImpl(
                project.getUpdateHelper().getAntProjectHelper(),
                project.getEvaluator(),
                srcRoots,
                ProjectProperties.MANIFEST_FILE);
        assertNotNull(q.getOptions(srcRoots.getRoots()[0]));
        assertNull(q.getOptions(project.getProjectDirectory()));
        assertSame(
                q.getOptions(srcRoots.getRoots()[0]),
                q.getOptions(srcRoots.getRoots()[0]));
    }

    public void testAutomaticModuleName() throws IOException {
        assertNull(getManifest());
        AutomaticModuleNameCompilerOptionsQueryImpl q = new AutomaticModuleNameCompilerOptionsQueryImpl(
                project.getUpdateHelper().getAntProjectHelper(),
                project.getEvaluator(),
                srcRoots,
                ProjectProperties.MANIFEST_FILE);
        CompilerOptionsQueryImplementation.Result r = q.getOptions(srcRoots.getRoots()[0]);
        assertNotNull(r);
        assertTrue(r.getArguments().isEmpty());
        createManifest();
        assertNotNull(getManifest());
        q = new AutomaticModuleNameCompilerOptionsQueryImpl(
                project.getUpdateHelper().getAntProjectHelper(),
                project.getEvaluator(),
                srcRoots,
                ProjectProperties.MANIFEST_FILE);
        r = q.getOptions(srcRoots.getRoots()[0]);
        assertNotNull(r);
        assertTrue(r.getArguments().isEmpty());
        updateManifest(Collections.singletonMap("Automatic-Module-Name", "org.me.foo"));    //NOI18N
        q = new AutomaticModuleNameCompilerOptionsQueryImpl(
                project.getUpdateHelper().getAntProjectHelper(),
                project.getEvaluator(),
                srcRoots,
                ProjectProperties.MANIFEST_FILE);
        r = q.getOptions(srcRoots.getRoots()[0]);
        assertNotNull(r);
        assertEquals(
                Collections.singletonList("-XDautomatic-module-name:org.me.foo"),    //NOI18N
                r.getArguments());
        final FileObject modInfo = srcRoots.getRoots()[0].createData("module-info", "java");               //NOI18N
        q = new AutomaticModuleNameCompilerOptionsQueryImpl(
                project.getUpdateHelper().getAntProjectHelper(),
                project.getEvaluator(),
                srcRoots,
                ProjectProperties.MANIFEST_FILE);
        r = q.getOptions(srcRoots.getRoots()[0]);
        assertNotNull(r);
        assertTrue(r.getArguments().isEmpty());
        modInfo.delete();
        q = new AutomaticModuleNameCompilerOptionsQueryImpl(
                project.getUpdateHelper().getAntProjectHelper(),
                project.getEvaluator(),
                srcRoots,
                ProjectProperties.MANIFEST_FILE);
        r = q.getOptions(srcRoots.getRoots()[0]);
        assertNotNull(r);
        assertEquals(
                Collections.singletonList("-XDautomatic-module-name:org.me.foo"),    //NOI18N
                r.getArguments());
        updateManifest(Collections.emptyMap());
        q = new AutomaticModuleNameCompilerOptionsQueryImpl(
                project.getUpdateHelper().getAntProjectHelper(),
                project.getEvaluator(),
                srcRoots,
                ProjectProperties.MANIFEST_FILE);
        r = q.getOptions(srcRoots.getRoots()[0]);
        assertNotNull(r);
        assertTrue(r.getArguments().isEmpty());
    }

    public void testEvaluatorChanges() throws IOException {
        updateManifest(Collections.singletonMap("Automatic-Module-Name", "org.me.foo"));    //NOI18N
        assertNotNull(getManifest());
        final CompilerOptionsQueryImplementation q = new AutomaticModuleNameCompilerOptionsQueryImpl(
                project.getUpdateHelper().getAntProjectHelper(),
                project.getEvaluator(),
                srcRoots,
                ProjectProperties.MANIFEST_FILE);
        final CompilerOptionsQueryImplementation.Result r = q.getOptions(srcRoots.getRoots()[0]);
        assertNotNull(r);
        assertEquals(
                Collections.singletonList("-XDautomatic-module-name:org.me.foo"),    //NOI18N
                r.getArguments());
        ProjectManager.mutex(true, project).writeAccess(() -> {
            final EditableProperties ep = project.getUpdateHelper().getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
            ep.setProperty(ProjectProperties.MANIFEST_FILE, "foo.mf");  //NOI18N
            project.getUpdateHelper().putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        });
        assertTrue(r.getArguments().isEmpty());
        ProjectManager.mutex(true, project).writeAccess(() -> {
            final EditableProperties ep = project.getUpdateHelper().getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
            ep.setProperty(ProjectProperties.MANIFEST_FILE, "manifest.mf");  //NOI18N
            project.getUpdateHelper().putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        });
        assertEquals(
                Collections.singletonList("-XDautomatic-module-name:org.me.foo"),    //NOI18N
                r.getArguments());
    }

    public void testModuleInfoChanges() throws IOException {
        updateManifest(Collections.singletonMap("Automatic-Module-Name", "org.me.foo"));    //NOI18N
        assertNotNull(getManifest());
        final CompilerOptionsQueryImplementation q = new AutomaticModuleNameCompilerOptionsQueryImpl(
                project.getUpdateHelper().getAntProjectHelper(),
                project.getEvaluator(),
                srcRoots,
                ProjectProperties.MANIFEST_FILE);
        final CompilerOptionsQueryImplementation.Result r = q.getOptions(srcRoots.getRoots()[0]);
        assertNotNull(r);
        assertEquals(
                Collections.singletonList("-XDautomatic-module-name:org.me.foo"),    //NOI18N
                r.getArguments());
        FileObject modInfo = srcRoots.getRoots()[0].createData("module-info", "java");       //NOI18N
        assertTrue(r.getArguments().isEmpty());
        try (FileLock lck = modInfo.lock()) {
            modInfo.rename(lck, "module-info", "bak"); //NOI18N
        }
        assertEquals(
                Collections.singletonList("-XDautomatic-module-name:org.me.foo"),    //NOI18N
                r.getArguments());
        try (FileLock lck = modInfo.lock()) {
            modInfo.rename(lck, "module-info", "java"); //NOI18N
        }
        assertTrue(r.getArguments().isEmpty());
        modInfo.delete();
        assertEquals(
                Collections.singletonList("-XDautomatic-module-name:org.me.foo"),    //NOI18N
                r.getArguments());
    }

    public void testManifestChanges() throws IOException {
        final CompilerOptionsQueryImplementation q = new AutomaticModuleNameCompilerOptionsQueryImpl(
                project.getUpdateHelper().getAntProjectHelper(),
                project.getEvaluator(),
                srcRoots,
                ProjectProperties.MANIFEST_FILE);
        final CompilerOptionsQueryImplementation.Result r = q.getOptions(srcRoots.getRoots()[0]);
        assertNotNull(r);
        assertTrue(r.getArguments().isEmpty());
        updateManifest(Collections.singletonMap("Automatic-Module-Name", "org.me.foo"));    //NOI18N
        assertNotNull(getManifest());
        assertNotNull(r);
        assertEquals(
                Collections.singletonList("-XDautomatic-module-name:org.me.foo"),    //NOI18N
                r.getArguments());
        updateManifest(Collections.singletonMap("Automatic-Module-Name", "org.me.boo"));    //NOI18N
        assertEquals(
                Collections.singletonList("-XDautomatic-module-name:org.me.boo"),    //NOI18N
                r.getArguments());
        final FileObject mf = getManifest();
        try (FileLock lck = mf.lock()) {
            mf.rename(lck, "manifest", "bak"); //NOI18N
        }
        assertTrue(r.getArguments().isEmpty());
        try (FileLock lck = mf.lock()) {
            mf.rename(lck, "manifest", "mf"); //NOI18N
        }
        assertEquals(
                Collections.singletonList("-XDautomatic-module-name:org.me.boo"),    //NOI18N
                r.getArguments());
        mf.delete();
        assertTrue(r.getArguments().isEmpty());
    }

    @CheckForNull
    private FileObject getManifest() throws IOException {
        return ProjectManager.mutex().readAccess(()-> {
            return Optional.ofNullable(project.getEvaluator().getProperty(ProjectProperties.MANIFEST_FILE))
                    .map(project.getUpdateHelper().getAntProjectHelper()::resolveFile)
                    .map(FileUtil::toFileObject)
                    .orElse(null);
        });
    }

    @NonNull
    private FileObject createManifest() throws IOException {
        try {
            return ProjectManager.mutex().writeAccess((Mutex.ExceptionAction<FileObject>)() -> {
                String path = project.getEvaluator().getProperty(ProjectProperties.MANIFEST_FILE);
                if (path == null) {
                    path = "manifest.mf";   //NOI18N
                    final EditableProperties props = project.getUpdateHelper().getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                    props.setProperty(ProjectProperties.MANIFEST_FILE, path);
                    project.getUpdateHelper().putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
                    ProjectManager.getDefault().saveProject(project);
                }
                File f =  project.getUpdateHelper().getAntProjectHelper().resolveFile(path);
                return FileUtil.createData(f);
            });
        } catch (MutexException e) {
            throw e.getCause() instanceof IOException ?
                    (IOException) e.getCause() :
                    new IOException(e.getCause());
        }
    }

    private void updateManifest(final Map<String,String> attrs) throws IOException {
        final FileObject mf = createManifest();
        final Manifest manifest;
        try(BufferedInputStream in = new BufferedInputStream(mf.getInputStream())) {
            manifest = new Manifest(in);
            final Attributes mfAttrs = manifest.getMainAttributes();
            mfAttrs.clear();
            mfAttrs.putValue(Name.MANIFEST_VERSION.toString(), "1.0");  //NOI18N
            for (Map.Entry<String,String> attr : attrs.entrySet()) {
                mfAttrs.putValue(attr.getKey(), attr.getValue());
            }
        }
        try(BufferedOutputStream out = new BufferedOutputStream(mf.getOutputStream())) {
            manifest.write(out);
        }
    }
}
