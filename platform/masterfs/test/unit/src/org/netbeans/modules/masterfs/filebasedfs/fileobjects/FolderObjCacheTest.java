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

package org.netbeans.modules.masterfs.filebasedfs.fileobjects;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.masterfs.filebasedfs.FileBasedFileSystem;
import org.netbeans.modules.masterfs.filebasedfs.utils.FileChangedManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * FolderObjTest.java
 * @author Radek Matous
 */
public class FolderObjCacheTest extends NbTestCase {
    static {
        System.setSecurityManager(FileChangedManager.getInstance());
    }
    
    Logger LOG;
    
    public FolderObjCacheTest(String testName) {
        super(testName);
    }
            
    @Override
    protected void setUp() throws Exception {
        LOG = Logger.getLogger("test." + getName());
        try {
            clearWorkDir();
        } catch (IOException ex) {
            LOG.log(Level.WARNING, "Cannot clear work dir for some reason", ex);
        }
    }

    @Override
    protected Level logLevel() {
        return Level.FINEST;
    }

    public void testFileObjectDistributionWorksAccuratelyAccordingToChildrenCache() throws IOException {
        doWork(false);
    }

    public void testFileObjectDistributionWorksAccuratelyAccordingToChildrenCacheWithGC() throws IOException {
        doWork(true);
    }

    private void doWork(boolean gc) throws IOException {
        File wd = null;
        for (int i = 0; i < 1000; i++) {
            wd = new File(getWorkDir(), "dir" + i);
            if (!wd.exists() && wd.mkdirs()) {
                break;
            }
        }
        LOG.log(Level.INFO, "Using wd: {0}", wd);
        
        final FileObject workDirFo = FileBasedFileSystem.getFileObject(wd);
        assertNotNull(workDirFo);        
        assertNotNull(workDirFo.getFileSystem().findResource(workDirFo.getPath()));                
        File fold = new File(wd,"fold");//NOI18N
        assertNull(FileUtil.toFileObject(fold));
        FileObject foldFo = workDirFo.createFolder(fold.getName());
        assertNotNull(foldFo);
        
        foldFo.delete();
        assertNull(FileBasedFileSystem.getFileObject(fold));        
        assertNull(FileBasedFileSystem.getFileObject(fold));
        assertNull(workDirFo.getFileObject(fold.getName()));                
        assertFalse(existsChild(workDirFo, fold.getName()));
        fold.mkdir();
        assertNotNull((workDirFo.getFileSystem()).findResource(workDirFo.getPath()+"/"+fold.getName()));                
        assertNotNull(workDirFo.getFileObject(fold.getName()));                        
        assertTrue(existsChild(workDirFo, fold.getName()));        
        workDirFo.refresh();
        assertNotNull(workDirFo.getFileObject(fold.getName()));        
        assertTrue(existsChild(workDirFo, fold.getName()));        
        FileObject gcFo = workDirFo.getFileObject(fold.getName());
        assertNotNull("One exists", gcFo);
        fold.delete();
        assertNull("Immediatelly invalidated thanks to FileChangedManager", workDirFo.getFileObject(fold.getName()));
        assertFalse(existsChild(workDirFo, fold.getName()));
        assertFalse("No longer valid", gcFo.isValid());
        
        if (gc) {
            WeakReference<Object> ref = new WeakReference<Object>(gcFo);
            gcFo = null;
            assertGC("Can be GCed", ref);
        }
        
        LOG.info("Before mkdir: " + fold);
        fold.mkdir();
        LOG.info("After mkdir: " + fold);
        final FileObject okFn = workDirFo.getFileObject(fold.getName());
        if (okFn == null) {
            LOG.log(Level.INFO, "show children: {0}", Arrays.toString(workDirFo.getChildren()));
        }
        assertNotNull("Just created folder shall be visible", okFn);
        LOG.info("OK, passed thru");
        assertTrue(existsChild(workDirFo, fold.getName()));        
        workDirFo.getFileSystem().refresh(false);
        assertNotNull(workDirFo.getFileObject(fold.getName()));                                        
        assertTrue(existsChild(workDirFo, fold.getName()));        
        foldFo.delete();
        assertNull(workDirFo.getFileObject(fold.getName()));                                        
        assertFalse(existsChild(workDirFo, fold.getName()));        
        fold.mkdir();
        //assertNull(((FileBasedFileSystem)workDirFo.getFileSystem()).findFileObject(fold));                
        //assertNull(MasterFileSystem.getFileObject(fold));                                
        assertNotNull(workDirFo.getFileObject(fold.getName()));                                        
        assertTrue(existsChild(workDirFo, fold.getName()));        
        workDirFo.getFileSystem().refresh(false);
        assertNotNull(workDirFo.getFileObject(fold.getName()));                                        
        assertTrue(existsChild(workDirFo, fold.getName()));        
        fold.delete();
        assertNull(workDirFo.getFileObject(fold.getName()));                                
        assertFalse(existsChild(workDirFo, fold.getName()));                
    }
   
   private static boolean existsChild(final FileObject folder, final String childName) {
       FileObject[] childs = folder.getChildren();
       for (int i = 0; i < childs.length; i++) {
           if (childs[i].getNameExt().equals(childName)) {
               return true;
           } 
       }
       return false;
   }
        
}
