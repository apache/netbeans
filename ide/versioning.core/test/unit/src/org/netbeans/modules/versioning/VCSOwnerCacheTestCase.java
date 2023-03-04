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

import java.io.IOException;
import java.security.Permission;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.modules.versioning.core.VersioningManager;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.api.VersioningSupport;
import org.netbeans.modules.versioning.core.util.VCSSystemProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public class VCSOwnerCacheTestCase extends AbstractFSTestCase {
    
    private StatFiles accessMonitor;
    private SecurityManager defaultSecurityManager;

    public VCSOwnerCacheTestCase(String testName) {
        super(testName);
        accessMonitor = new StatFiles();
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        if(accessMonitor != null) {
            System.setSecurityManager(defaultSecurityManager);
        }
    }
     
    public void testGetOwnerKnownFileType() throws IOException {
        assertTrue(VersioningSupport.getOwner(toVCSFileProxy(getVersionedFolder())).getClass() == getVCS());

        FileObject f = getVersionedFolder().createData("file", null);
        testGetOwnerKnownFileType(toVCSFileProxy(f), true);
        
        f = getVersionedFolder().createFolder("folder");
        testGetOwnerKnownFileType(toVCSFileProxy(f), false);                         
    }

    private void testGetOwnerKnownFileType(VCSFileProxy proxy, boolean isFile) throws IOException {    
        accessMonitor.files.clear();
        VCSSystemProvider.VersioningSystem vs = VersioningManager.getInstance().getOwner(proxy, isFile); // true => its a file, no io.file.isFile() call needed
        assertNotNull(vs);
        
        // file wasn't accessed even on first shot
        assertFalse(accessMonitor.files.contains(proxy.getPath()));
        
        accessMonitor.files.clear();
        vs = VersioningManager.getInstance().getOwner(proxy, isFile);
        assertNotNull(vs);
        
        // file wasn't accessed
        assertFalse(accessMonitor.files.contains(proxy.getPath()));               
    }
    
    public void testFileOwnerCache() throws IOException {
        testFileOwnerCache(true /* versioned */ , false /* file */);
        testFileOwnerCache(false/* unversioned */ , false /* file */);
    }
        
    public void testFolderOwnerCache() throws IOException {
        testFileOwnerCache(true /* versioned */ , true /* folder */);
        testFileOwnerCache(false/* unversioned */ , true /* folder */);
    }

    private void testFileOwnerCache(boolean isVersioned, boolean isFolder) throws IOException {
        FileObject folder = isVersioned ? getVersionedFolder() : getNotVersionedFolder();
        FileObject child;
        FileObject child2;
        if(isFolder) {
            child = folder.createFolder("child");
            child2 = folder.createFolder("child2");
        } else {
            child = folder.createData("child");
            child2 = folder.createData("child2");
        }
        
        assertFileAccess(child, isVersioned, true /* access */);
        
        // try again - shouldn't be accessed anymore
        assertFileAccess(child, isVersioned, false /* no access */);        
        
        // try few more times some other file no file access expected
        assertFileAccess(child2, isVersioned, true /* access */);
        for (int i = 0; i < 100; i++) {
            // try some other file
            assertFileAccess(child2, isVersioned, false /* no access */);
        }        
        
        // try the first file again
        assertFileAccess(child, isVersioned, false /* no access */);        
    }
    
    private void assertFileAccess(FileObject f, boolean versioned, boolean access) throws IOException {
        VCSFileProxy proxy = toVCSFileProxy(f);
        accessMonitor.files.clear();
        org.netbeans.modules.versioning.core.spi.VersioningSystem vs = VersioningSupport.getOwner(proxy);
        if(versioned && vs == null) {
            fail("no VersioningSystem returned for versioned file " + f);
        } else if(!versioned && vs != null) {
            fail("VersioningSystem returned for unversioned file " + f);
        }
        // file was accessed
        boolean accessed = accessMonitor.files.contains(FileUtil.toFile(f).getAbsolutePath());
//        if(access && !accessed) {
//            fail(f + " was not but should be accessed");
//        } else 
        if (!access && accessed) {
            fail(f + " was accessed but shouldn't");
        }
    }

    private class StatFiles extends SecurityManager {
        private List<String> files = new LinkedList<String>();        
        @Override
        public void checkRead(String file) {
            files.add(file);
        }       
        @Override
        public void checkPermission(Permission perm) {
        }
    }    

}
