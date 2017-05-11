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
                Collections.singletonList("--automatic-module-name:org.me.foo"),    //NOI18N
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
                Collections.singletonList("--automatic-module-name:org.me.foo"),    //NOI18N
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
                Collections.singletonList("--automatic-module-name:org.me.foo"),    //NOI18N
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
                Collections.singletonList("--automatic-module-name:org.me.foo"),    //NOI18N
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
                Collections.singletonList("--automatic-module-name:org.me.foo"),    //NOI18N
                r.getArguments());
        FileObject modInfo = srcRoots.getRoots()[0].createData("module-info", "java");       //NOI18N
        assertTrue(r.getArguments().isEmpty());
        try (FileLock lck = modInfo.lock()) {
            modInfo.rename(lck, "module-info", "bak"); //NOI18N
        }
        assertEquals(
                Collections.singletonList("--automatic-module-name:org.me.foo"),    //NOI18N
                r.getArguments());
        try (FileLock lck = modInfo.lock()) {
            modInfo.rename(lck, "module-info", "java"); //NOI18N
        }
        assertTrue(r.getArguments().isEmpty());
        modInfo.delete();
        assertEquals(
                Collections.singletonList("--automatic-module-name:org.me.foo"),    //NOI18N
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
                Collections.singletonList("--automatic-module-name:org.me.foo"),    //NOI18N
                r.getArguments());
        updateManifest(Collections.singletonMap("Automatic-Module-Name", "org.me.boo"));    //NOI18N
        assertEquals(
                Collections.singletonList("--automatic-module-name:org.me.boo"),    //NOI18N
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
                Collections.singletonList("--automatic-module-name:org.me.boo"),    //NOI18N
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
