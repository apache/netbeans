/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.api.project;

import java.awt.Component;
import java.awt.Graphics;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.WeakHashMap;
import javax.swing.Icon;
import org.junit.Assert;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.project.ProjectFactory;
import org.netbeans.spi.project.ProjectFactory2;
import org.netbeans.spi.project.ProjectState;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Lookup;
import org.openide.util.test.MockLookup;
import org.openide.util.test.TestFileUtils;

/**
 * Help set up org.netbeans.api.project.*Test.
 * @author Jesse Glick
 */
public final class TestUtil {
    
    private TestUtil() {}
    
    /**
     * Create a scratch directory for tests.
     * Will be in /tmp or whatever, and will be empty.
     * If you just need a java.io.File use clearWorkDir + getWorkDir.
     */
    public static FileObject makeScratchDir(NbTestCase test) throws IOException {
        test.clearWorkDir();
        File root = test.getWorkDir();
        assert root.isDirectory() : root;
        assert root.list().length == 0 : Arrays.toString(root.list());
        MockLookup.init(); // URLMapper asks for default lookup
        FileObject fo = FileUtil.toFileObject(root);
        Assert.assertNotNull(
                "No FileObject for " + root + " found.\n" +
                "Maybe you need ${openide/masterfs.dir}/modules/org-netbeans-modules-masterfs.jar\n" +
                "in test.unit.run.cp.extra, or make sure Lookups.metaInfServices is included in Lookup.default, so that\n" +
                "Lookup.default<URLMapper>=" + Lookup.getDefault().lookupAll(URLMapper.class) + " includes MasterURLMapper\n" +
                "e.g. by using TestUtil.setLookup(Object[]) rather than TestUtil.setLookup(Lookup).",
                fo);
        return fo;
    }
    
    /**
     * Delete a file and all subfiles.
     */
    public static void deleteRec(File f) throws IOException {
        TestFileUtils.deleteFile(f);
    }
    
    /**
     * Create a testing project factory which recognizes directories containing
     * a subdirectory called "testproject".
     * If that subdirectory contains a file named "broken" then loading the project
     * will fail with an IOException.
     */
    public static ProjectFactory testProjectFactory() {
        return new TestProjectFactory();
    }
    
    /**
     * Try to force a GC.
     */
    public static void gc() {
        System.gc();
        System.runFinalization();
        System.gc();
    }
    
    private static final Map<FileObject,Integer> loadCount = new WeakHashMap<FileObject,Integer>();
    
    /**
     * Check how many times {@link ProjectFactory#loadProject} has been called
     * (with any outcome) on a given project directory.
     */
    public static int projectLoadCount(FileObject dir) {
        Integer i = loadCount.get(dir);
        if (i != null) {
            return i;
        } else {
            return 0;
        }
    }
    
    /**
     * Mark a test project to fail with a given error when it is next saved.
     * The error only applies to the next save, not subsequent ones.
     * @param p a test project
     * @param error an error to throw (IOException or Error or RuntimeException),
     *              or null if it should succeed
     */
    public static void setProjectSaveWillFail(Project p, Throwable error) {
        ((TestProject)p).error = error;
    }
    
    /**
     * Get the number of times a test project was successfully saved with no error.
     * @param p a test project
     * @return the save count
     */
    public static int projectSaveCount(Project p) {
        return ((TestProject)p).saveCount;
    }
    
    /**
     * Mark a test project as modified.
     * @param p a test project
     */
    public static void modify(Project p) {
        ((TestProject)p).state.markModified();
    }
    
    /**
     * Mark a test project as modified.
     * @param p a test project
     */
    public static void notifyDeleted(Project p) {
        ((TestProject)p).state.notifyDeleted();
    }
    
    /**If non-null, use the value as the Lookup for newly created projects.
     * 
     */
    public static Lookup LOOKUP = null;

    public static final String TEST_PROJECT_TYPE = "test.project.type";
    public static final Icon TEST_PROJECT_ICON = new Icon() {
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        @Override
        public int getIconWidth() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        @Override
        public int getIconHeight() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    };
    private static final class TestProjectFactory implements ProjectFactory, ProjectFactory2 {
        
        TestProjectFactory() {}
        
        public Project loadProject(FileObject projectDirectory, ProjectState state) throws IOException {
            Integer i = loadCount.get(projectDirectory);
            if (i == null) {
                i = 1;
            } else {
                i = i + 1;
            }
            loadCount.put(projectDirectory, i);
            FileObject testproject = projectDirectory.getFileObject("testproject");
            if (testproject != null && testproject.isFolder()) {
                if (testproject.getFileObject("broken") != null) {
                    throw new IOException("Load failed of " + projectDirectory);
                } else {
                    return new TestProject(projectDirectory, LOOKUP != null ? LOOKUP : Lookup.EMPTY, state);
                }
            } else {
                return null;
            }
        }
        
        public void saveProject(Project project) throws IOException, ClassCastException {
            TestProject p = (TestProject)project;
            Throwable t = p.error;
            if (t != null) {
                p.error = null;
                if (t instanceof IOException) {
                    throw (IOException)t;
                } else if (t instanceof Error) {
                    throw (Error)t;
                } else {
                    throw (RuntimeException)t;
                }
            }
            p.saveCount++;
        }
        
        public boolean isProject(FileObject dir) {
            FileObject testproject = dir.getFileObject("testproject");
            return testproject != null && testproject.isFolder();
        }
        
        @Override
        public ProjectManager.Result isProject2(FileObject dir) {
            FileObject testproject = dir.getFileObject("testproject");
            if(testproject != null && testproject.isFolder() && testproject.getFileObject("broken") != null) {
                return new ProjectManager.Result(TEST_PROJECT_ICON);
    }
            if(testproject != null && testproject.isFolder()) {
                return new ProjectManager.Result(
                            dir.getName(), 
                            TEST_PROJECT_TYPE, 
                            TEST_PROJECT_ICON);
            }
            return null;
        }
    
    }
    
    private static final class TestProject implements Project {
        
        private final FileObject dir;
        private final Lookup lookup;
        final ProjectState state;
        Throwable error;
        int saveCount = 0;
        
        public TestProject(FileObject dir, Lookup lookup, ProjectState state) {
            this.dir = dir;
            this.lookup = lookup;
            this.state = state;
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

        /* Probably unnecessary to have a ProjectInformation here:
        public String getName() {
            return "testproject:" + getProjectDirectory().getNameExt();
        }
        
        public String getDisplayName() {
            return "Test Project in " + getProjectDirectory().getNameExt();
        }
        
        public Image getIcon() {
            return null;
        }
        
        public void addPropertyChangeListener(PropertyChangeListener listener) {}
        public void removePropertyChangeListener(PropertyChangeListener listener) {}
         */
        
    }
    
    /**
     * Open a URL of content (for example from {@link Class#getResource}) and copy it to a named file.
     * The new file can be given as a parent directory plus a relative (slash-separated) path.
     * The file may not already exist, but intermediate directories may or may not.
     * If the content URL is null, the file is just created, no more; if it already existed
     * it is touched (timestamp updated) and its contents are cleared.
     * @return the file object
     */
    public static FileObject createFileFromContent(URL content, FileObject parent, String path) throws IOException {
        if (parent == null) {
            throw new IllegalArgumentException("null parent");
        }
        Assert.assertTrue("folder", parent.isFolder());
        FileObject fo = parent;
        StringTokenizer tok = new StringTokenizer(path, "/");
        boolean touch = false;
        while (tok.hasMoreTokens()) {
            Assert.assertNotNull("fo is null (parent=" + parent + " path=" + path + ")", fo);
            String name = tok.nextToken();
            if (tok.hasMoreTokens()) {
                FileObject sub = fo.getFileObject(name);
                if (sub == null) {
                    FileObject fo2 = fo.createFolder(name);
                    Assert.assertNotNull("createFolder(" + fo + ", " + name + ") -> null", fo2);
                    fo = fo2;
                } else {
                    Assert.assertTrue("folder", sub.isFolder());
                    fo = sub;
                }
            } else {
                FileObject sub = fo.getFileObject(name);
                if (sub == null) {
                    FileObject fo2 = fo.createData(name);
                    Assert.assertNotNull("createData(" + fo + ", " + name + ") -> null", fo2);
                    fo = fo2;
                } else {
                    fo = sub;
                    touch = true;
                }
            }
        }
        assert fo.isData();
        if (content != null || touch) {
            OutputStream os = fo.getOutputStream();
            try {
                if (content != null) {
                    InputStream is = content.openStream();
                    try {
                        FileUtil.copy(is, os);
                    } finally {
                        is.close();
                    }
                }
            } finally {
                os.close();
            }
        }
        return fo;
    }
    
}
