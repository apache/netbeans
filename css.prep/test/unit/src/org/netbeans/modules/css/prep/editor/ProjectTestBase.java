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
package org.netbeans.modules.css.prep.editor;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.Icon;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.junit.MockServices;
import org.netbeans.lib.lexer.test.TestLanguageProvider;
import org.netbeans.modules.css.lib.CssTestBase;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.netbeans.modules.parsing.impl.indexing.PathRecognizerRegistry;
import org.netbeans.modules.parsing.impl.indexing.PathRegistry;
import org.netbeans.modules.parsing.spi.indexing.PathRecognizer;
import org.netbeans.modules.projectapi.SimpleFileOwnerQueryImplementation;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.project.ProjectFactory;
import org.netbeans.spi.project.ProjectState;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.test.MockLookup;

/**
 * Base class for all CP preprocessor tests which needs a working static html project.
 *
 * @author marekfukala
 */
public class ProjectTestBase extends CssTestBase {

    private String projectFolder;
    
    private FileObject srcFo, projectFo;
//    private FileObject javaLibSrc, javaLibProjectFo;

    public ProjectTestBase(String name, String projectFolder) {
        super(name);
        this.projectFolder = projectFolder;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        assertNotNull("the netbeans.dirs property must be specified!", System.getProperty("netbeans.dirs"));

        this.projectFo = getTestFile(projectFolder);
        assertNotNull(projectFo);
        this.srcFo = getTestFile(getSourcesFolderName());
        assertNotNull(srcFo);

        Map<FileObject, ProjectInfo> projects = new HashMap<>();

        //create classpath for web project
        Map<String, ClassPath> cps = new HashMap<>();

        ClassPath sourceClassPath = ClassPathSupport.createClassPath(new FileObject[]{srcFo});
        
        cps.put(ClassPath.SOURCE, sourceClassPath);
        cps.put(ClassPath.COMPILE, ClassPathSupport.createClassPath(new FileObject[]{srcFo}));
        cps.put(ClassPath.BOOT, createBootClassPath());
        ClassPathProvider classpathProvider = new TestMultiClassPathProvider(projectFo, cps);
        Sources sources = new TestSources(srcFo);

        projects.put(projectFo, new ProjectInfo(classpathProvider, sources));

        MockLookup.setInstances(
                new TestMultiProjectFactory(projects),
                new SimpleFileOwnerQueryImplementation(),
                classpathProvider,
                new TestLanguageProvider());

        //provides the ClassPath.SOURCE as source path id so it is returned by
        //PathRecognizerRegistry.getDefault().getSourceIds()
        MockServices.setServices(TestPathRecognizer.class);

        //register the source classpath so PathRegistry.getDefault().getRootsMarkedAs(classpathId) in QuerySupport works
        GlobalPathRegistry.getDefault().register(ClassPath.SOURCE, new ClassPath[]{sourceClassPath});
        
        IndexingManager.getDefault().refreshIndexAndWait(srcFo.toURL(), null);
    }
    
    protected String getTestProjectFolderName() {
        return projectFolder;
    }
    
    protected String getSourcesFolderName() {
        return projectFolder + "/public_html";
    }
    
    @ServiceProvider(service = PathRecognizer.class)
    public static class TestPathRecognizer extends  PathRecognizer {

        @Override
        public Set<String> getSourcePathIds() {
            return Collections.singleton(ClassPath.SOURCE);
        }

        @Override
        public Set<String> getLibraryPathIds() {
            return Collections.emptySet();
        }

        @Override
        public Set<String> getBinaryLibraryPathIds() {
            return Collections.emptySet();
        }

        @Override
        public Set<String> getMimeTypes() {
            Set<String> mimes = new HashSet<>();
            mimes.add("text/css");
            mimes.add("text/scss");
            return mimes;
        }
        
    }
    
    protected final class TestSources implements Sources {

        private FileObject[] roots;

        TestSources(FileObject... roots) {
            this.roots = roots;
        }

        @Override
        public SourceGroup[] getSourceGroups(String type) {
            SourceGroup[] sg = new SourceGroup[roots.length];
            for (int i = 0; i < roots.length; i++) {
                sg[i] = new TestSourceGroup(roots[i]);
            }
            return sg;
        }

        @Override
        public void addChangeListener(ChangeListener listener) {
        }

        @Override
        public void removeChangeListener(ChangeListener listener) {
        }
    }

    protected final class TestSourceGroup implements SourceGroup {

        private FileObject root;

        public TestSourceGroup(FileObject root) {
            this.root = root;
        }

        @Override
        public FileObject getRootFolder() {
            return root;
        }

        @Override
        public String getName() {
            return root.getNameExt();
        }

        @Override
        public String getDisplayName() {
            return getName();
        }

        @Override
        public Icon getIcon(boolean opened) {
            return null;
        }

        @Override
        public boolean contains(FileObject file) throws IllegalArgumentException {
            return FileUtil.getRelativePath(root, file) != null;
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {
        }
    }
    
    /**
     * Creates boot {@link ClassPath} for platform the test is running on,
     * it uses the sun.boot.class.path property to find out the boot path roots.
     * @return ClassPath
     * @throws java.io.IOException when boot path property contains non valid path
     */
    public static ClassPath createBootClassPath() throws IOException {
        String bootPath = System.getProperty("sun.boot.class.path");
        String[] paths = bootPath.split(File.pathSeparator);
        List<URL> roots = new ArrayList<>(paths.length);
        for (String path : paths) {
            File f = new File(path);
            if (!f.exists()) {
                continue;
            }
            URL url = Utilities.toURI(f).toURL();
            if (FileUtil.isArchiveFile(url)) {
                url = FileUtil.getArchiveRoot(url);
            }
            roots.add(url);
//            System.out.println(url);
        }
//        System.out.println("-----------");
        return ClassPathSupport.createClassPath(roots.toArray(new URL[roots.size()]));
    }


    protected FileObject getSourcesFolder() {
        return srcFo;
    }

    protected FileObject getProjectFolder() {
        return projectFo;
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

     protected static class TestProject implements Project {

        private final FileObject dir;
        final ProjectState state;
        Throwable error;
        int saveCount = 0;
        private Lookup lookup;

        public TestProject(FileObject dir, ProjectState state, ClassPathProvider classpathProvider, Sources sources) {
            this.dir = dir;
            this.state = state;

            InstanceContent ic = new InstanceContent();
            ic.add(classpathProvider);
            ic.add(sources);

            this.lookup = new AbstractLookup(ic);

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
    
    private static class TestMultiClassPathProvider implements ClassPathProvider {

        private Map<String, ClassPath> map;
        private FileObject root;

        public TestMultiClassPathProvider(FileObject root, Map<String, ClassPath> map) {
            this.map = map;
            this.root = root;
        }

        @Override
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
