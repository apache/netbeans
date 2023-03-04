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
package org.netbeans.modules.javaee.resources;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.j2ee.metadata.model.support.JavaSourceTestCase;
import org.netbeans.modules.projectapi.SimpleFileOwnerQueryImplementation;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.project.ProjectFactory;
import org.netbeans.spi.project.ProjectState;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.test.MockLookup;

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class TestBase extends JavaSourceTestCase {

    private FileObject srcFo, projectFo;
    protected Project project;
    protected List<FileObject> projects = new LinkedList<FileObject>();

    public TestBase(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        this.projectFo = getTestFile("projects/EJBModule53");
        assertNotNull(projectFo);
        this.srcFo = getTestFile("projects/EJBModule53/src");
        assertNotNull(srcFo);
        projects.add(projectFo);
        MockLookup.setInstances(
                new ClassPathProviderImpl(),
                new SimpleFileOwnerQueryImplementation(),
                new TestProjectFactory(projects));
        project = FileOwnerQuery.getOwner(projectFo);
        assertNotNull(project);
    }

    protected FileObject getTestFile(String relFilePath) {
        File wholeInputFile = new File(getDataDir(), relFilePath);
        if (!wholeInputFile.exists()) {
            NbTestCase.fail("File " + wholeInputFile + " not found.");
        }
        FileObject fo = FileUtil.toFileObject(wholeInputFile);
        assertNotNull(fo);

        return fo;
    }

//    public MetadataModel<JsfModel> createJsfModel() throws IOException, InterruptedException {
//        IndexingManager.getDefault().refreshIndexAndWait(srcFO.getURL(), null);
//        ModelUnit modelUnit = ModelUnit.create(
//                ClassPath.getClassPath(srcFO, ClassPath.BOOT),
//                ClassPath.getClassPath(srcFO, ClassPath.COMPILE),
//                ClassPath.getClassPath(srcFO, ClassPath.SOURCE),
//                FileOwnerQuery.getOwner(projectFo));
//        return JsfModelFactory.createMetaModel(modelUnit);
//    }

    private class TestProjectFactory implements ProjectFactory {

        private List<FileObject> projects;

        public TestProjectFactory(List<FileObject> projects) {
            this.projects = projects;
        }

        @Override
        public Project loadProject(FileObject projectDirectory, ProjectState state) throws IOException {
            return new TestProject(projectDirectory, state);
        }

        @Override
        public void saveProject(Project project) throws IOException, ClassCastException {
        }

        @Override
        public boolean isProject(FileObject dir) {
            return projects.contains(dir);
        }
    }

    protected class TestProject implements Project {

        private final FileObject dir;
        final ProjectState state;
        Throwable error;
        int saveCount = 0;
        private Lookup lookup;

        public TestProject(FileObject dir, ProjectState state) {
            this.dir = dir;
            this.state = state;
            this.lookup = Lookups.fixed(new ClassPathProviderImpl());
        }

        @Override
        public Lookup getLookup() {
            return lookup;
        }

        @Override
        public FileObject getProjectDirectory() {
            return dir;
        }

        @Override
        public String toString() {
            return "testproject:" + getProjectDirectory().getNameExt();
        }
    }

    public final class ClassPathProviderImpl implements ClassPathProvider {

        public ClassPath findClassPath(FileObject file, String type) {
            boolean found = false;
            for (FileObject root : roots) {
                if (root.equals(file) || FileUtil.isParentOf(root, file)) {
                    found = true;
                }
            }
            if (!found) {
                return null;
            }
            if (ClassPath.SOURCE.equals(type)) {
                return srcCP;
            } else if (ClassPath.COMPILE.equals(type)) {
                return compileCP;
            } else if (ClassPath.BOOT.equals(type)) {
                return bootCP;
            }
            return null;
        }
    }
}
