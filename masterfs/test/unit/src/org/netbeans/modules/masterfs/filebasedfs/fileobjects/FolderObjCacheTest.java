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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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
