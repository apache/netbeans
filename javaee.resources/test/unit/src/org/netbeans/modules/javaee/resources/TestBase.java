/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
