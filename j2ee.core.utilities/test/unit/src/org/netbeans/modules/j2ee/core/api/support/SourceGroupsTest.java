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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.j2ee.core.api.support;

import java.beans.PropertyChangeListener;
import java.io.File;
import javax.swing.Icon;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.j2ee.core.api.support.java.TestUtilities;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.test.MockLookup;

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
        MockLookup.setLayersAndInstances();
        super.setUp();
        TestUtilities.setCacheFolder(getWorkDir());
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
