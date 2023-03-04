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

package org.netbeans.modules.websvc.api.support;

import java.beans.PropertyChangeListener;
import java.io.File;
import javax.swing.Icon;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.source.usages.IndexUtil;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Erno Mononen
 */
public class SourceGroupsTest extends NbTestCase {

    private static final String JAVA_APP_NAME = "JavaApp";
    
    private Project javaApp; 
    
    public SourceGroupsTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        IndexUtil.setCacheFolder(getWorkDir());
        FileObject projectPath = FileUtil.toFileObject(new File(getDataDir(), JAVA_APP_NAME));
        javaApp = ProjectManager.getDefault().findProject(projectPath);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        clearWorkDir();
    }

    public void testGetClassSourceGroup() throws Exception{
        SourceGroup fooBarSG = SourceGroups.getClassSourceGroup(javaApp, "foo.bar.baz.FooBar");
        assertEquals(getJavaAppSourceRoot(), fooBarSG.getRootFolder());

        SourceGroup fooSG = SourceGroups.getClassSourceGroup(javaApp, "Foo");
        assertEquals(getJavaAppSourceRoot(), fooSG.getRootFolder());

        assertNull(SourceGroups.getClassSourceGroup(javaApp, "should.not.Exist")); 
    }

    public void testGetFolderSourceGroup() throws Exception{
        SourceGroup fooBarSG = SourceGroups.getClassSourceGroup(javaApp, "foo.bar.baz.FooBar");
        assertEquals(getJavaAppSourceRoot(), fooBarSG.getRootFolder());

        assertNull(SourceGroups.getClassSourceGroup(javaApp, "should.not.Exist")); 
    }

    public void testGetFolderForPackage() throws Exception{
        SourceGroup[] javaSGs = SourceGroups.getJavaSourceGroups(javaApp);
        FileObject folder = SourceGroups.getFolderForPackage(javaSGs[0], "foo.bar", false);
        assertEquals(javaApp.getProjectDirectory().getFileObject("src/foo/bar"), folder);

        // default package
        folder = SourceGroups.getFolderForPackage(javaSGs[0], "", false);
        assertEquals(getJavaAppSourceRoot(), folder);
    }
    
    public void testGetJavaSourceGroups(){
        SourceGroup[] javaSGs = SourceGroups.getJavaSourceGroups(javaApp);
        
        assertEquals(1, javaSGs.length);
        assertEquals(getJavaAppSourceRoot(), javaSGs[0].getRootFolder());
    }
    
    public void testIsFolderWritable() throws Exception {
        SourceGroup[] javaSGs = SourceGroups.getJavaSourceGroups(javaApp);
        // existing folder
        assertTrue(SourceGroups.isFolderWritable(javaSGs[0], "javaapp"));
        // non-existing
        assertTrue(SourceGroups.isFolderWritable(javaSGs[0], "should.not.exist"));
        
        File notWritableRoot = new File(getWorkDir(), "cantwrite");
        notWritableRoot.mkdir();
        notWritableRoot.setReadOnly();
        SourceGroup notWritable = new SourceGroupImpl(notWritableRoot);
        assertFalse(SourceGroups.isFolderWritable(notWritable, ""));
        assertFalse(SourceGroups.isFolderWritable(notWritable, "should.not.exist"));
    }

    public void testGetPackageForFolder() throws Exception {
        SourceGroup[] javaSGs = SourceGroups.getJavaSourceGroups(javaApp);
        FileObject folder = javaApp.getProjectDirectory().getFileObject("src/foo/bar/baz");
        String packageName =  SourceGroups.getPackageForFolder(javaSGs[0], folder);

        assertEquals("foo.bar.baz", packageName);

        // default package
        packageName =  SourceGroups.getPackageForFolder(javaSGs[0], getJavaAppSourceRoot());
        assertEquals("", packageName);
        
        try{
            SourceGroups.getPackageForFolder(javaSGs[0], FileUtil.toFileObject(getWorkDir()));
            fail();
        }catch (IllegalStateException expected){
        }
    }

    private FileObject getJavaAppSourceRoot(){
        return javaApp.getProjectDirectory().getFileObject("/src");
    }
    
    private static class SourceGroupImpl implements SourceGroup{
        private final File root;

        public SourceGroupImpl(File root) {
            this.root = root;
        }

        public FileObject getRootFolder() {
            return FileUtil.toFileObject(root);
        }

        public String getName() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public String getDisplayName() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Icon getIcon(boolean opened) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public boolean contains(FileObject file) throws IllegalArgumentException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void addPropertyChangeListener(PropertyChangeListener listener) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void removePropertyChangeListener(PropertyChangeListener listener) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
    }
}
