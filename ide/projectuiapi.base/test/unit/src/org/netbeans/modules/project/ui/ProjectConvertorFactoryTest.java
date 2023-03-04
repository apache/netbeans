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
package org.netbeans.modules.project.ui;

import java.beans.PropertyChangeListener;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;
import javax.swing.Icon;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.project.ui.convertor.ProjectConvertorFactory;
import org.netbeans.spi.project.ui.support.ProjectConvertors;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.Parameters;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.openide.util.test.MockPropertyChangeListener;

/**
 *
 * @author Tomas Zezula
 */
public class ProjectConvertorFactoryTest extends NbTestCase {

    static {
        System.setProperty("org.openide.util.Lookup.paths", "Services");
    }

    private FileObject projectDir;

    public ProjectConvertorFactoryTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        projectDir = createCheckout(getWorkDir());
    }

    public void testProjectConvertor() throws IOException {
        final TestProject.OpenHook oh1 = new TestProject.OpenHook();
        final TestProject.OpenHook oh2 = new TestProject.OpenHook();
        TestProject.Factory.LOOKUP_FACTORY = new Callable<Lookup>() {
            @Override
            public Lookup call() throws Exception {
                return Lookups.fixed(
                    oh1,
                    oh2,
                    new TestPI(projectDir),
                    new ProjectAdditionalService());
            }
        };
        final Lookup clp = Lookups.singleton(new ConvertorAdditionalServicePermanent());
        final Lookup clt = ProjectConvertors.createProjectConvertorLookup(new ConvertorAdditionalServiceTransient());
        final Lookup cl = new ProxyLookup(clp, clt);
        TestProject.Convertor.LOOKUP_FACTORY = new Callable<Lookup>() {
            @Override
            public Lookup call() throws Exception {
                return cl;
            }
        };
        TestProject.Convertor.CALLBACK = new Runnable() {
            @Override
            public void run() {
                try {
                    ((Closeable)clt).close();
                } catch (IOException ioe) {
                    throw new RuntimeException(ioe);
                }
            }
        };
        final Project artPrj = ProjectManager.getDefault().findProject(projectDir);
        assertNotNull(artPrj);
        assertEquals(projectDir, artPrj.getProjectDirectory());
        final ProjectInformation artPi = artPrj.getLookup().lookup(ProjectInformation.class);
        assertNotNull(artPi);
        assertEquals(projectDir.getName(), artPi.getDisplayName());
        assertNotNull(artPrj.getLookup().lookup(ConvertorAdditionalServicePermanent.class));
        assertNotNull(artPrj.getLookup().lookup(ConvertorAdditionalServiceTransient.class));
        assertNull(artPrj.getLookup().lookup(ProjectAdditionalService.class));
        final MockPropertyChangeListener ml = new MockPropertyChangeListener(
                ProjectInformation.PROP_DISPLAY_NAME,
                ProjectInformation.PROP_NAME,
                ProjectInformation.PROP_ICON);
        artPi.addPropertyChangeListener(ml);
        OpenProjects.getDefault().open(new Project[]{artPrj}, false);
        assertTrue(OpenProjects.getDefault().isProjectOpen(artPrj));
        assertNotNull(projectDir.getFileObject(TestProject.PROJECT_MARKER));
        final Project realPrj = ProjectManager.getDefault().findProject(projectDir);
        assertNotNull(realPrj);
        assertNotSame(artPrj, realPrj);
        assertNotSame(artPrj.getClass(), realPrj.getClass());
        assertEquals(artPrj, realPrj);
        assertNull(realPrj.getLookup().lookup(ConvertorAdditionalServicePermanent.class));
        assertNull(realPrj.getLookup().lookup(ConvertorAdditionalServiceTransient.class));
        assertNotNull(realPrj.getLookup().lookup(ProjectAdditionalService.class));
        assertNotNull(artPrj.getLookup().lookup(ConvertorAdditionalServicePermanent.class));
        assertNull(artPrj.getLookup().lookup(ConvertorAdditionalServiceTransient.class));
        assertNotNull(artPrj.getLookup().lookup(ProjectAdditionalService.class));
        assertEquals(1, oh1.openCalls.get());
        assertEquals(0, oh1.closeCalls.get());
        assertEquals(1, oh2.openCalls.get());
        assertEquals(0, oh2.closeCalls.get());
        OpenProjects.getDefault().close(new Project[]{realPrj});
        assertFalse(OpenProjects.getDefault().isProjectOpen(artPrj));
        assertFalse(OpenProjects.getDefault().isProjectOpen(realPrj));
        assertEquals(1, oh1.openCalls.get());
        assertEquals(1, oh1.closeCalls.get());
        assertEquals(1, oh2.openCalls.get());
        assertEquals(1, oh2.closeCalls.get());
        //Artificial ProjectInformation should be updated and events should be fired
        assertEquals(projectDir.getPath(), artPi.getDisplayName());
        ml.assertEvents(
            ProjectInformation.PROP_DISPLAY_NAME,
            ProjectInformation.PROP_NAME,
            ProjectInformation.PROP_ICON);
    }

    public void testProjectConvertorWithExplicitProjectInfo() throws IOException {
        TestProject.Factory.LOOKUP_FACTORY = new Callable<Lookup>() {
            @Override
            public Lookup call() throws Exception {
                return Lookups.fixed(
                    new ProjectAdditionalService());
            }
        };
        final ProjectInformation testPI = new TestPI(projectDir);
        TestProject.Convertor.LOOKUP_FACTORY = new Callable<Lookup>() {
            @Override
            public Lookup call() throws Exception {
                return Lookups.fixed(
                    new ConvertorAdditionalServicePermanent(),
                    testPI);
            }
        };
        final Project artPrj = ProjectManager.getDefault().findProject(projectDir);
        assertNotNull(artPrj);
        assertEquals(projectDir, artPrj.getProjectDirectory());
        assertNotNull(artPrj.getLookup().lookup(ProjectInformation.class));
        assertEquals(projectDir.getPath(), artPrj.getLookup().lookup(ProjectInformation.class).getDisplayName());
        assertNotNull(artPrj.getLookup().lookup(ConvertorAdditionalServicePermanent.class));
        assertNull(artPrj.getLookup().lookup(ProjectAdditionalService.class));
        OpenProjects.getDefault().open(new Project[]{artPrj}, false);
        assertTrue(OpenProjects.getDefault().isProjectOpen(artPrj));
        assertNotNull(projectDir.getFileObject(TestProject.PROJECT_MARKER));
        final Project realPrj = ProjectManager.getDefault().findProject(projectDir);
        assertNotNull(realPrj);
        assertNotSame(artPrj, realPrj);
        assertNotSame(artPrj.getClass(), realPrj.getClass());
        assertEquals(artPrj, realPrj);
        assertNull(realPrj.getLookup().lookup(ConvertorAdditionalServicePermanent.class));
        assertNotNull(realPrj.getLookup().lookup(ProjectAdditionalService.class));
    }

    public void testIsConvertorProject() throws IOException {
        final Project artPrj = ProjectManager.getDefault().findProject(projectDir);
        assertNotNull(artPrj);
        assertTrue(ProjectConvertorFactory.isConvertorProject(artPrj));
        OpenProjects.getDefault().open(new Project[]{artPrj}, false);
        assertTrue(OpenProjects.getDefault().isProjectOpen(artPrj));
        assertFalse(ProjectConvertorFactory.isConvertorProject(artPrj));
        final Project newPrj = ProjectManager.getDefault().findProject(projectDir);
        assertNotSame(newPrj, artPrj);
        assertFalse(ProjectConvertorFactory.isConvertorProject(newPrj));
    }

    @NonNull
    private static FileObject createCheckout(@NonNull final File workDir) throws IOException {
        final FileObject projectDir = FileUtil.createFolder(
            FileUtil.toFileObject(workDir),
            "checkout");    //NOI18N
        projectDir.createData(TestProject.CONVERTOR_MARKER);  //NOI18N
        return projectDir;
    }

    private static final class ConvertorAdditionalServicePermanent {}
    private static final class ConvertorAdditionalServiceTransient {}
    private static final class ProjectAdditionalService {}
    private static final class TestPI implements ProjectInformation {

        private final FileObject projectDir;

        TestPI(@NonNull final FileObject projectDir) {
            Parameters.notNull("projectDir", projectDir);
            this.projectDir = projectDir;
        }

        @Override
        public String getName() {
            return projectDir.getPath();
        }

        @Override
        public String getDisplayName() {
            return getName();
        }

        @Override
        public Icon getIcon() {
            return null;
        }

        @Override
        public Project getProject() {
            try {
                return ProjectManager.getDefault().findProject(projectDir);
            } catch (IOException | IllegalArgumentException ex) {
                return null;
            }
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {
        }
    }
}
