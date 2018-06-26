/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.jsf.editor;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import static junit.framework.Assert.assertNotNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.Sources;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.lexer.test.TestLanguageProvider;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.netbeans.modules.projectapi.SimpleFileOwnerQueryImplementation;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.project.ProjectFactory;
import org.netbeans.spi.project.ProjectState;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.util.test.MockLookup;

/**
 *
 * @author marekfukala
 */
public class TestBaseForTestProject extends TestBase {

    private FileObject srcFo, webFo, projectFo, javaLibSrc, javaLibProjectFo;

    public TestBaseForTestProject(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        //disable info exceptions from j2eeserver
        Logger.getLogger("org.netbeans.modules.j2ee.deployment.impl.ServerRegistry").setLevel(Level.SEVERE);

        //the InstalledFileLocatorImpl needs the netbeans.dirs properly set 
        //so it can find the jsf "modules/ext/jsf-2_1/javax.faces.jar"
        assertNotNull("the netbeans.dirs property must be specified!", System.getProperty("netbeans.dirs"));

        this.projectFo = copyProjectFolderToWorkDir("testWebProject");
        assertNotNull(projectFo);
        this.srcFo = FileUtil.toFileObject(getWorkDir()).getFileObject("testWebProject/src");
        assertNotNull(srcFo);
        this.webFo = FileUtil.toFileObject(getWorkDir()).getFileObject("testWebProject/web");
        assertNotNull(webFo);

        this.javaLibProjectFo = copyProjectFolderToWorkDir("testJavaJSFLibrary");
        assertNotNull(javaLibProjectFo);
        this.javaLibSrc = FileUtil.toFileObject(getWorkDir()).getFileObject("testJavaJSFLibrary/src");
        assertNotNull(javaLibSrc);

        Map<FileObject, ProjectInfo> projects = new HashMap<FileObject, ProjectInfo>();

        //create classpath for web project
        Map<String, ClassPath> cps = new HashMap<String, ClassPath>();

        //depend also on the java library
        cps.put(ClassPath.COMPILE, 
                ClassPathSupport.createProxyClassPath(
                    createServletAPIClassPath(),
                    ClassPathSupport.createClassPath(new FileObject[]{javaLibSrc})));
        
        cps.put(ClassPath.EXECUTE, createServletAPIClassPath());
        cps.put(ClassPath.SOURCE, ClassPathSupport.createClassPath(new FileObject[]{srcFo, webFo}));
        cps.put(ClassPath.BOOT, createBootClassPath());
        ClassPathProvider classpathProvider = new TestMultiClassPathProvider(projectFo, cps);
        Sources sources = new TestSources(srcFo, webFo);

        projects.put(projectFo, new ProjectInfo(classpathProvider, sources));

        //create classpath for java library project
        cps = new HashMap<String, ClassPath>();
        cps.put(ClassPath.BOOT, createBootClassPath());
        cps.put(ClassPath.COMPILE, createBootClassPath());
        cps.put(ClassPath.EXECUTE, createBootClassPath());
        cps.put(ClassPath.SOURCE, ClassPathSupport.createClassPath(new FileObject[]{javaLibSrc}));
        ClassPathProvider javaLibClasspathProvider = new TestMultiClassPathProvider(javaLibProjectFo, cps);
        Sources javaLibSources = new TestSources(javaLibSrc);

        projects.put(javaLibProjectFo, new ProjectInfo(javaLibClasspathProvider, javaLibSources));

        ClassPathProvider mergedClassPathProvider = new MergedClassPathProvider(projects);

        MockLookup.setInstances(
                new TestUserCatalog(),
                new TestMultiProjectFactory(projects),
                new SimpleFileOwnerQueryImplementation(),
                mergedClassPathProvider,
                new TestLanguageProvider(),
                new FakeWebModuleProvider(webFo, srcFo));

        refreshIndexAndWait();
    }

    protected void refreshIndexAndWait() throws FileStateInvalidException {
        //uff, it looks like we need to refresh the source roots separately since
        //if I use the project's folder here, then the index data are stored to
        //its index folder, but later the QuerySupport uses different cache folders
        //for webFO and srcFO so the index returns nothing.
        IndexingManager.getDefault().refreshIndexAndWait(srcFo.getURL(), null);
        IndexingManager.getDefault().refreshIndexAndWait(webFo.getURL(), null);
        IndexingManager.getDefault().refreshIndexAndWait(javaLibSrc.getURL(), null);
    }

    protected JsfSupportImpl getJsfSupportImpl() {
        JsfSupportImpl instance = JsfSupportImpl.findFor(getWebFolder());
        assertNotNull(instance);

        return instance;
    }

    protected FileObject getSourcesFolder() {
        return srcFo;
    }

    protected FileObject getWebFolder() {
        return webFo;
    }

    protected FileObject getProjectFolder() {
        return projectFo;
    }

    //copied from FileChooserAccessory
    protected FileObject copyFolderRecursively(FileObject sourceFolder, FileObject destination) throws IOException {
        assert sourceFolder.isFolder() : sourceFolder;
        assert destination.isFolder() : destination;
        FileObject destinationSubFolder = destination.getFileObject(sourceFolder.getName());
        if (destinationSubFolder == null) {
            destinationSubFolder = destination.createFolder(sourceFolder.getName());
        }
        for (FileObject fo : sourceFolder.getChildren()) {
            if (fo.isFolder()) {
                copyFolderRecursively(fo, destinationSubFolder);
            } else {
                FileObject foExists = destinationSubFolder.getFileObject(fo.getName(), fo.getExt());
                if (foExists != null) {
                    foExists.delete();
                }
                FileUtil.copyFile(fo, destinationSubFolder, fo.getName(), fo.getExt());
            }
        }
        return destinationSubFolder;
    }

    protected FileObject copyProjectFolderToWorkDir(String projectName) throws IOException {
        FileObject projectDir = FileUtil.createFolder(FileUtil.toFileObject(getWorkDir()), projectName);
        for (FileObject child : getTestFile(projectName).getChildren()) {
            assertNotNull(child);
            if (child.isFolder()) {
                assertNotNull(copyFolderRecursively(child, projectDir));
            } else {
                assertNotNull(FileUtil.copyFile(child, projectDir, child.getName()));
            }
        }
        return projectDir;
    }

    private static class ProjectInfo {
        
        private ClassPathProvider cpp;
        private Sources sources;

        public ProjectInfo(ClassPathProvider cpp, Sources sources) {
            this.cpp = cpp;
            this.sources = sources;
        }

        public ClassPathProvider getCpp() {
            return cpp;
        }

        public Sources getSources() {
            return sources;
        }

    }

    private static class MergedClassPathProvider implements ClassPathProvider {

        private Map<FileObject, ProjectInfo> projects;

        public MergedClassPathProvider(Map<FileObject, ProjectInfo> projects) {
            this.projects = projects;
        }

        @Override
        public ClassPath findClassPath(FileObject file, String type) {
            for(FileObject fo : projects.keySet()) {
                if(FileUtil.isParentOf(fo, file)) {
                    return projects.get(fo).getCpp().findClassPath(file, type);
                }
            }
            return null;
        }

    }

    private static class TestMultiProjectFactory implements ProjectFactory {

        private Map<FileObject, ProjectInfo> projects;

        public  TestMultiProjectFactory(Map<FileObject, ProjectInfo> projects) {
            this.projects = projects;
        }

        @Override
        public Project loadProject(FileObject projectDirectory, ProjectState state) throws IOException {
            ProjectInfo pi = projects.get(projectDirectory);
            return pi != null ? new TestProject(projectDirectory, state, pi.getCpp(), pi.getSources() ) : null;
        }

        @Override
        public void saveProject(Project project) throws IOException, ClassCastException {
        }

        @Override
        public boolean isProject(FileObject dir) {
            return projects.containsKey(dir);
        }
    }

    private static class TestMultiClassPathProvider implements ClassPathProvider {

        private Map<String, ClassPath> map;
        private FileObject root;

        public TestMultiClassPathProvider(FileObject root, Map<String, ClassPath> map) {
            this.map = map;
            this.root = root;
        }

        public ClassPath findClassPath(FileObject file, String type) {
            if (FileUtil.isParentOf(root, file)) {
                if (map != null) {
                    return map.get(type);
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }
    }
}
