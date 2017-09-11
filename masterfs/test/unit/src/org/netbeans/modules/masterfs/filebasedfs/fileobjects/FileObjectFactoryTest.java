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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.masterfs.filebasedfs.fileobjects;

import java.io.IOException;
import java.io.File;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.masterfs.filebasedfs.utils.FileInfo;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author rmatous
 */
public class FileObjectFactoryTest extends NbTestCase {
    private File testFile;


    public FileObjectFactoryTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();        
        testFile = new File(getWorkDir(),"testfile");//NOI18N
        if (!testFile.exists()) {
            assert testFile.createNewFile();
        }
        
    }
    
    public void testRefreshFor() throws Exception {
        EventsEvaluator fdc = new EventsEvaluator();        
        try {
            File workDir = getWorkDir();
            File external = new File(workDir, "externalFile");        
            assertFalse(external.exists());
            FileObject foWorkDir = FileUtil.toFileObject(workDir);            
            assertNotNull(foWorkDir);
            //fully fill in cache
            foWorkDir.getChildren();
            
            assertNull(foWorkDir.getFileObject(external.getName()));
            
            /*1.create file */            
            assertTrue(external.createNewFile());
            assertNull(foWorkDir.getFileObject(external.getName()));
            fdc.assertDataCreated(0);        
            FileUtil.refreshFor(workDir);        
            fdc.assertDataCreated(1);
            assertNotNull(foWorkDir.getFileObject(external.getName()));
            
            /*2.delete file */            
            assertTrue(external.delete());
            assertNotNull(foWorkDir.getFileObject(external.getName()));
            FileUtil.refreshFor(workDir);        
            assertNull(foWorkDir.getFileObject(external.getName()));
            fdc.assertDeleted(1);                    

            /*3.create folder */                        
            external = new File(workDir, "externalFolder");        
            assertTrue(external.mkdir());
            assertNull(foWorkDir.getFileObject(external.getName()));
            fdc.assertFolderCreated(0);                        
            FileUtil.refreshFor(workDir);   
            fdc.assertFolderCreated(1);
            assertNotNull(foWorkDir.getFileObject(external.getName()));                                    
            
        } finally {
            fdc.cleanUp();            
        }
    }

    public void testRefreshForRoot() throws Exception {
        EventsEvaluator fdc = new EventsEvaluator();                
        try {
            File workDir = getWorkDir();
            File external = new File(workDir, "externalFile");        
            assertFalse(external.exists());
            FileObject foWorkDir = FileUtil.toFileObject(workDir);
            assertNotNull(foWorkDir);
            //fully fill in cache
            foWorkDir.getChildren();            
            assertNull(foWorkDir.getFileObject(external.getName()));
            
            /*1.create file*/
            assertTrue(external.createNewFile());
            assertNull(foWorkDir.getFileObject(external.getName()));
            
            File root = new FileInfo(workDir).getRoot().getFile();
            
            fdc.assertDataCreated(0);                
            FileUtil.refreshFor(root);
            fdc.assertDataCreated(1);
            assertNotNull(foWorkDir.getFileObject(external.getName()));

            /*2.delete file*/                                    
            fdc.assertDeleted(0);                                
            assertTrue(external.delete());
            assertNotNull(foWorkDir.getFileObject(external.getName()));
            FileUtil.refreshFor(root);        
            assertNull(foWorkDir.getFileObject(external.getName()));
            fdc.assertDeleted(1);                    
            
            /*3.create folder */                        
            external = new File(workDir, "externalFolder");        
            assertTrue(external.mkdir());
            assertNull(foWorkDir.getFileObject(external.getName()));
            fdc.assertFolderCreated(0);                        
            FileUtil.refreshFor(root);   
            fdc.assertFolderCreated(1);
            assertNotNull(foWorkDir.getFileObject(external.getName()));                                                            
        } finally {
            fdc.cleanUp();            
        }
    }
    
    public void testRefreshForWorkDir() throws Exception {
        EventsEvaluator fdc = new EventsEvaluator();                
        try {
            File workDir = getWorkDir();
            File external = new File(workDir, "externalFile");        
            assertFalse(external.exists());
            FileObject foWorkDir = FileUtil.toFileObject(workDir);
            assertNotNull(foWorkDir);
            //fully fill in cache
            foWorkDir.getChildren();            
            assertNull(foWorkDir.getFileObject(external.getName()));
            
            /*1.create file */
            assertTrue(external.createNewFile());
            assertNull(foWorkDir.getFileObject(external.getName()));
            fdc.assertDataCreated(0);                        
            FileUtil.refreshFor(workDir);   
            fdc.assertDataCreated(1);
            assertNotNull(foWorkDir.getFileObject(external.getName()));
            
            /*2.delete file */            
            fdc.assertDeleted(0);                                
            assertTrue(external.delete());
            assertNotNull(foWorkDir.getFileObject(external.getName()));
            FileUtil.refreshFor(workDir);   
            assertNull(foWorkDir.getFileObject(external.getName()));
            fdc.assertDeleted(1);                                
            
            /*3.create folder */            
            external = new File(workDir, "externalFolder");
            assertTrue(external.mkdir());
            assertNull(foWorkDir.getFileObject(external.getName()));
            fdc.assertFolderCreated(0);                        
            FileUtil.refreshFor(workDir);   
            fdc.assertFolderCreated(1);
            assertNotNull(foWorkDir.getFileObject(external.getName()));                        
        } finally {
            fdc.cleanUp();            
        }
    }
    
    public void testRefreshForExternal() throws Exception {
        EventsEvaluator fdc = new EventsEvaluator();                        
        try {
            File workDir = getWorkDir();
            File external = new File(workDir, "externalFile");        
            assertFalse(external.exists());
            FileObject foWorkDir = FileUtil.toFileObject(workDir);
            assertNotNull(foWorkDir);
            //fully fill in cache
            foWorkDir.getChildren();            
            assertNull(foWorkDir.getFileObject(external.getName()));
            
            /*1.create file */                        
            assertTrue(external.createNewFile());
            assertNull(foWorkDir.getFileObject(external.getName()));
            fdc.assertDataCreated(0);
            FileUtil.refreshFor(external.getParentFile());        
            fdc.assertDataCreated(1);
            assertNotNull(foWorkDir.getFileObject(external.getName()));
            
            /*2.delete file */            
            fdc.assertDeleted(0);                                
            assertTrue(external.delete());
            assertNotNull(foWorkDir.getFileObject(external.getName()));
            FileUtil.refreshFor(external.getParentFile());   
            assertNull(foWorkDir.getFileObject(external.getName()));
            fdc.assertDeleted(1);                                
            
            /*3.create folder */            
            external = new File(workDir, "externalFolder");
            assertTrue(external.mkdir());
            assertNull(foWorkDir.getFileObject(external.getName()));
            fdc.assertFolderCreated(0);                        
            FileUtil.refreshFor(external.getParentFile());   
            fdc.assertFolderCreated(1);
            assertNotNull(foWorkDir.getFileObject(external.getName()));                        
            
        } finally {
            fdc.cleanUp();            
        }
    }

    public void testRefreshForExternalWithNotExistingParent() throws Exception {
        EventsEvaluator fdc = new EventsEvaluator();                        
        try {
            File workDir = getWorkDir();
            File external = new File(workDir, "externalFile");        
            assertFalse(external.exists());
            assertTrue(external.createNewFile());
            fdc.assertDataCreated(0);
            FileUtil.refreshFor(external);        
            fdc.assertDataCreated(0);
        } finally {
            fdc.cleanUp();
        }
    }
    
    public void testRefreshForBoth() throws Exception {
        EventsEvaluator fdc = new EventsEvaluator();                        
        try {
            File workDir = getWorkDir();
            File external = new File(workDir, "externalFile");        
            assertFalse(external.exists());
            FileObject foWorkDir = FileUtil.toFileObject(workDir);
            assertNotNull(foWorkDir);
            assertNull(foWorkDir.getFileObject(external.getName()));
            assertTrue(external.createNewFile());
            assertNull(foWorkDir.getFileObject(external.getName()));
            fdc.assertDataCreated(0);        
            FileUtil.refreshFor(external, workDir);        
            fdc.assertDataCreated(1);
            assertNotNull(foWorkDir.getFileObject(external.getName()));        
        } finally {
            fdc.cleanUp();            
        }
    }
    
    public void testRefreshForNotExisting() throws Exception {
        EventsEvaluator fdc = new EventsEvaluator();                                
        try {
            File workDir = getWorkDir();
            File external = new File(workDir, "externalFile");        
            assertFalse(external.exists());
            fdc.assertDataCreated(0);                
            FileUtil.refreshFor(external);                
            fdc.assertDataCreated(0);
        } finally {
            fdc.cleanUp();        
        }
    }
    
    private class EventsEvaluator extends FileChangeAdapter {
        private int folderCreatedCount;
        private int dataCreatedCount;
        private int deletedCount;        
        private FileSystem fs;
        EventsEvaluator() throws FileStateInvalidException {
            fs = FileUtil.toFileObject(testFile).getFileSystem();
            fs.refresh(true);
            fs.addFileChangeListener(this);
        }

        @Override
        public void fileFolderCreated(FileEvent fe) {
            super.fileFolderCreated(fe);
            folderCreatedCount++;
        }

        
        @Override
        public void fileDataCreated(FileEvent fe) {
            super.fileDataCreated(fe);
            dataCreatedCount++;
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            super.fileDeleted(fe);
            deletedCount++;
        }

        public void assertFolderCreated(int count) {
            assertEquals(this.folderCreatedCount, count);
        }
        
        public void assertDataCreated(int count) {
            assertEquals(this.dataCreatedCount, count);
        }

        public void assertDeleted(int count) {
            assertEquals(this.deletedCount, count);
        }
        
        public void resetFolderCreated() {
            folderCreatedCount = 0;
        }
        
        public void resetDataCreated() {
            dataCreatedCount = 0;
        }


        public void resetDeleted() {
            deletedCount = 0;
        }
        
        public void cleanUp() throws FileStateInvalidException {
            fs.removeFileChangeListener(this);
        }
    }
    
    
    /*
    public void testIssue64363() throws Exception {
        assertTrue(testFo.isValid());
        assertTrue(testFile.delete());
        testFo.getFileSystem().findResource(testFo.getPath());        
                
        FileObject testFo2 = Cache.getDefault().getValidOrInvalid(((MasterFileObject)testFo).getResource());
        if (!ProvidedExtensionsTest.ProvidedExtensionsImpl.isImplsDeleteRetVal()) {
            assertFalse(testFo2.isValid());
        }
        assertEquals(testFo, testFo2);        
    }
    
    public void testIssue61221() throws Exception {        
        assertTrue(testFo.isValid());
        FileObject par = testFo.getParent();
        testFo.delete();
        MasterFileObject testFo2 = (MasterFileObject)par.createData(testFo.getNameExt());
        assertNotSame(testFo2, testFo);
        Reference ref = new WeakReference(testFo);
        testFo = null;
        assertGC("",ref);
        MasterFileObject testFo3 = (MasterFileObject)par.getFileObject(testFo2.getNameExt());
        assertEquals(testFo3.isValid(), testFo2.isValid());
        assertSame(testFo3, testFo2);
    }

    public void testIssue61221_2() throws Exception {        
        assertTrue(testFo.isValid());
        FileObject par = testFo.getParent();
        testFo.delete();
        MasterFileObject testFo2 = (MasterFileObject)par.createData(testFo.getNameExt());
        assertNotSame(testFo2, testFo);
        Reference ref = new WeakReference(testFo);
        testFo = null;
        assertGC("",ref);
        assertNotNull(Cache.getDefault().get(testFo2.getResource()));
    }
    
*/    
}
