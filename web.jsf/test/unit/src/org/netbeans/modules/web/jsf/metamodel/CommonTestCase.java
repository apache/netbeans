/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.web.jsf.metamodel;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.Icon;
import javax.swing.event.ChangeListener;
import static junit.framework.Assert.assertNotNull;
import org.netbeans.api.j2ee.core.Profile;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.j2ee.dd.api.web.WebAppMetadata;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.support.JavaSourceTestCase;
import org.netbeans.modules.j2ee.metadata.model.support.TestUtilities;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.netbeans.modules.projectapi.SimpleFileOwnerQueryImplementation;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.jsf.api.metamodel.JsfModel;
import org.netbeans.modules.web.jsf.impl.metamodel.JsfModelProviderImpl;
import org.netbeans.modules.web.jsf.api.metamodel.ModelUnit;
import org.netbeans.modules.web.spi.webmodule.WebModuleFactory;
import org.netbeans.modules.web.spi.webmodule.WebModuleImplementation2;
import org.netbeans.modules.web.spi.webmodule.WebModuleProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.project.ProjectFactory;
import org.netbeans.spi.project.ProjectState;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.test.MockLookup;

/**
 * @author ads
 *
 */
public class CommonTestCase extends JavaSourceTestCase {

    protected FileObject webFo, projectFo;
    protected Project project;
    protected WebModuleProvider webModuleProvider;
    protected List<FileObject> projects = new LinkedList<FileObject>();

    public CommonTestCase(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        super.setUp();

        projectFo = copyProjectFolder();
        assertNotNull(projectFo);

        webFo = FileUtil.toFileObject(getWorkDir()).getFileObject("web");
        assertNotNull(webFo);

        //create classpath for web project
        Map<String, ClassPath> cps = new HashMap<String, ClassPath>();
        cps.put(ClassPath.SOURCE, ClassPathSupport.createClassPath(new FileObject[]{srcFO, webFo}));
        projects.add(projectFo);

        webModuleProvider = new FakeWebModuleProvider(srcFO);
        MockLookup.setInstances(
                webModuleProvider,
                cpProvider,
                new SimpleFileOwnerQueryImplementation(),
                new TestProjectFactory(projects));
        Project p = FileOwnerQuery.getOwner(projectFo);
        assertNotNull(p);
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

    public MetadataModel<JsfModel> createJsfModel() throws IOException, InterruptedException {
        IndexingManager.getDefault().refreshIndexAndWait(srcFO.getURL(), null);
        ModelUnit modelUnit = ModelUnit.create(
                ClassPath.getClassPath(srcFO, ClassPath.BOOT),
                ClassPath.getClassPath(srcFO, ClassPath.COMPILE),
                ClassPath.getClassPath(srcFO, ClassPath.SOURCE),
                FileOwnerQuery.getOwner(projectFo));
        return JsfModelProviderImpl.createMetaModel(modelUnit);
    }

    public String getFileContent(String relativePath) throws IOException {
        return TestUtilities.copyStreamToString(SeveralXmlModelTest.class.
                getResourceAsStream(relativePath));
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

    private FileObject copyProjectFolder() throws IOException {
        String[] projectContent = new String[]{"nbproject", "web", "build.xml"};
        for (String content : projectContent) {
            FileObject contentFO = getTestFile("testWebProject/" + content);
            assertNotNull(contentFO);
            if (contentFO.isFolder()) {
                assertNotNull(copyFolderRecursively(contentFO, srcFO.getParent()));
            } else {
                assertNotNull(FileUtil.copyFile(contentFO, srcFO.getParent(), contentFO.getName()));
            }
        }
        return FileUtil.toFileObject(getWorkDir());
    }

    protected static class FakeWebModuleProvider implements WebModuleProvider {

        private FileObject webRoot;

        public FakeWebModuleProvider(FileObject webRoot) {
            this.webRoot = webRoot;
        }

        public WebModule findWebModule(FileObject file) {
            return WebModuleFactory.createWebModule(new FakeWebModuleImplementation2(webRoot));
        }
    }

    private static class FakeWebModuleImplementation2 implements WebModuleImplementation2 {

        private FileObject webRoot;

        public FakeWebModuleImplementation2(FileObject webRoot) {
            this.webRoot = webRoot;
        }

        public FileObject getDocumentBase() {
            return webRoot;
        }

        public String getContextPath() {
            return "/";
        }

        public Profile getJ2eeProfile() {
            return Profile.JAVA_EE_6_FULL;
        }

        public FileObject getWebInf() {
            return null;
        }

        public FileObject getDeploymentDescriptor() {
            return null;
        }

        public FileObject[] getJavaSources() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public MetadataModel<WebAppMetadata> getMetadataModel() {
            return null;
        }

        public void addPropertyChangeListener(PropertyChangeListener listener) {
        }

        public void removePropertyChangeListener(PropertyChangeListener listener) {
        }
    }

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

            InstanceContent ic = new InstanceContent();
            ic.add(cpProvider);
            ic.add(new SourcesImpl());
            ic.add(new JsfModelProviderImpl(this));
            this.lookup = new AbstractLookup(ic);

        }

        public Lookup getLookup() {
            return lookup;
        }

        public FileObject getProjectDirectory() {
            return dir;
        }

        public String toString() {
            return "testproject:" + getProjectDirectory().getNameExt();
        }
    }

    private class SourcesImpl implements Sources {

        public SourcesImpl() {}

        public SourceGroup[] getSourceGroups(String type) {
            return new SourceGroup[] { new SourceGroupImpl() };
        }

        public void addChangeListener(ChangeListener listener) {
        }

        public void removeChangeListener(ChangeListener listener) {
        }

    }

    private class SourceGroupImpl implements SourceGroup {

        public SourceGroupImpl() {}

        public FileObject getRootFolder() {
            return srcFO;
        }

        public String getName() {
            return "Sources";
        }

        public String getDisplayName() {
            return "Sources";
        }

        public Icon getIcon(boolean opened) {
            return null;
        }

        public boolean contains(FileObject file) {
            return FileUtil.isParentOf(projectFo, file);
        }

        public void addPropertyChangeListener(PropertyChangeListener listener) {
        }

        public void removePropertyChangeListener(PropertyChangeListener listener) {
        }

    }
}
