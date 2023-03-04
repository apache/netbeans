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
package org.netbeans.modules.versioning;

import org.netbeans.modules.versioning.core.Utils;
import java.io.IOException;
import java.io.File;
import java.lang.reflect.Field;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.VersioningManager;
import org.netbeans.modules.versioning.core.util.VCSSystemProvider.VersioningSystem;
import org.netbeans.modules.versioning.core.api.VersioningSupport;
import org.netbeans.modules.versioning.core.spi.testvcs.TestVCS;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public class VCSOwnerTestCase extends AbstractFSTestCase {
    
    public VCSOwnerTestCase(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        super.setUp();
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
     
    public void testVCSSystemDoesntAwakeOnUnrelatedGetOwner() throws IOException {
        TestVCS.resetInstance();
        assertNull(TestVCS.getInstance());
        
        FileObject f = getNotVersionedFolder().createData("sleepingfile", null);
        
        assertNull(TestVCS.getInstance());
        VersioningSystem owner = VersioningManager.getInstance().getOwner(toVCSFileProxy(f));
        assertNull(owner);
        
        assertNull(TestVCS.getInstance());
    }  

    public void testGetOwnerKnownFileType() throws IOException {
        assertTrue(VersioningSupport.getOwner(toVCSFileProxy(getVersionedFolder())).getClass() == getVCS());

        FileObject f = getVersionedFolder().createData("file", null);
        testGetOwnerKnownFileType(toVCSFileProxy(f), true);
        
        f = getVersionedFolder().createFolder("folder");
        testGetOwnerKnownFileType(toVCSFileProxy(f), false);                         
    }

    private void testGetOwnerKnownFileType(VCSFileProxy proxy, boolean isFile) throws IOException {    
        VersioningSystem vs = VersioningManager.getInstance().getOwner(proxy, isFile); // true => its a file, no io.file.isFile() call needed
        assertNotNull(vs);
        
        vs = VersioningManager.getInstance().getOwner(proxy, isFile);
        assertNotNull(vs);
    }
    
    public void testGetOwnerVersioned() throws IOException {
        assertTrue(VersioningSupport.getOwner(toVCSFileProxy(getVersionedFolder())).getClass() == getVCS());
        FileObject aRoot = getVersionedFolder().createData("a", "txt");
        VCSFileProxy rootProxy = toVCSFileProxy(aRoot);
        assertTrue(VersioningSupport.getOwner(rootProxy).getClass() ==  getVCS());
        
        aRoot = getVersionedFolder().createFolder("b-folder");
        rootProxy = toVCSFileProxy(aRoot);
        assertTrue(VersioningSupport.getOwner(rootProxy).getClass() ==  getVCS());
        rootProxy = toVCSFileProxy(aRoot.createData("deep-file"));
        assertTrue(VersioningSupport.getOwner(rootProxy).getClass() ==  getVCS());
        
        if(isMasterFS()) {
            // works only in case of io.File filesystem
            File vf = FileUtil.toFile(getVersionedFolder());
            File f = new File(vf, "nonexistent-file");
            assertTrue(VersioningSupport.getOwner(VCSFileProxy.createFileProxy(f)).getClass() ==  getVCS());
        }
    }
    
    public void testGetOwnerUnversioned() throws IOException {
//        File aRoot = File.listRoots()[0];
//        VCSFileProxy rootProxy = toVCSFileProxy(aRoot);
//        assertNull(VersioningSupport.getOwner(rootProxy));
        assertNull(VersioningSupport.getOwner(VCSFileProxy.createFileProxy(getWorkDir())));
        assertNull(VersioningSupport.getOwner(toVCSFileProxy(getNotVersionedFolder())));     
        assertNull(VersioningSupport.getOwner(toVCSFileProxy(getNotVersionedFolder().getParent())));     

        FileObject fo = getNotVersionedFolder().createData("a", "txt");
        assertNull(VersioningSupport.getOwner(toVCSFileProxy(fo)));
        
        if(isMasterFS()) {
            File uf = FileUtil.toFile(getNotVersionedFolder()); 
            File f = new File(uf, "notexistent.txt");
            assertNull(VersioningSupport.getOwner(VCSFileProxy.createFileProxy(f)));        
        }
    }

    public void testExcludedFolders () throws Exception {
        Field f = Utils.class.getDeclaredField("unversionedFolders");
        f.setAccessible(true);

        VCSFileProxy a = toVCSFileProxy(getNotVersionedFolder().createData("a"));
        VCSFileProxy b = toVCSFileProxy(getNotVersionedFolder().createData("b"));
        System.setProperty("versioning.unversionedFolders", a.getPath() + ";" + b.getPath() + ";");
        VCSFileProxy c = toVCSFileProxy(getNotVersionedFolder().createData("c")); 
        VersioningSupport.getPreferences().put("unversionedFolders", c.getPath()); //NOI18N
        File userdir = new File(getWorkDir(), "userdir"); userdir.mkdirs();
        
        f.set(Utils.class, (File[]) null);
        
        assertTrue(VersioningSupport.isExcluded(a));
        assertTrue(VersioningSupport.isExcluded(b));
        assertTrue(VersioningSupport.isExcluded(c));
        assertTrue(VersioningSupport.isExcluded(VCSFileProxy.createFileProxy(userdir)));
        File userDirFile = new File(userdir, "ffff"); userDirFile.createNewFile();
        assertTrue(VersioningSupport.isExcluded(VCSFileProxy.createFileProxy(userDirFile)));
        assertFalse(VersioningSupport.isExcluded(VCSFileProxy.createFileProxy(userdir.getParentFile())));

        assertEquals(4, ((String[]) f.get(Utils.class)).length);

        // what if someone still wants to have userdir versioned?
        System.setProperty("versioning.netbeans.user.versioned", "true");
        
        f.set(Utils.class, (VCSFileProxy[]) null);
        
        assertTrue(VersioningSupport.isExcluded(a));
        assertTrue(VersioningSupport.isExcluded(b));
        assertTrue(VersioningSupport.isExcluded(c));
        assertFalse(VersioningSupport.isExcluded(VCSFileProxy.createFileProxy(userdir)));
        assertFalse(VersioningSupport.isExcluded(VCSFileProxy.createFileProxy(userDirFile)));
        assertFalse(VersioningSupport.isExcluded(VCSFileProxy.createFileProxy(userdir.getParentFile())));

        assertEquals(3, ((String[]) f.get(Utils.class)).length);
    }
}
