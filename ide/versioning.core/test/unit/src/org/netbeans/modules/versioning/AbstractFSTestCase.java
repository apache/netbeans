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
import java.io.File;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.versioning.core.VersioningManager;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.spi.testvcs.TestVCS;
import org.openide.filesystems.FileObject;
import org.openide.util.test.MockLookup;

public class AbstractFSTestCase extends NbTestCase {
    
    protected String workDirPath;
    protected FileObject versionedFolder;
    protected FileObject unversionedFolder;
    private String versionedPath;

    public AbstractFSTestCase(String testName) {
        super(testName);
    }

    protected FileObject getVersionedFolder() throws IOException {
        if (versionedFolder == null) {
            versionedFolder = createFolder(versionedPath);
            FileObject md = versionedFolder.getFileObject(TestVCS.TEST_VCS_METADATA);
            if(md == null || !md.isValid()) {
                createFolder(versionedPath + "/" + TestVCS.TEST_VCS_METADATA);
            }
            // cleanup the owner cache, this folder just became versioned 
            VersioningManager.getInstance().flushNullOwners(); 
        }
        return versionedFolder;
    }
    
    protected FileObject getNotVersionedFolder() throws IOException {
        if (unversionedFolder == null) {
            unversionedFolder = createFolder(workDirPath + "/unversioned/");
        }
        return unversionedFolder;
    }

    protected String getRoot(String path) {
        int idx = path.indexOf(versionedPath);
        return idx > 0 ? path.substring(0, idx) : null;
    } 
    
    protected void setUp() throws Exception {
        super.setUp();
        MockLookup.setLayersAndInstances();
        File workDir = getWorkDir();
        workDirPath = workDir.getParentFile().getName() + "/" + workDir.getName();
        versionedPath = workDirPath + "/root" + TestVCS.VERSIONED_FOLDER_SUFFIX;
        File userdir = new File(workDir, "userdir");
        userdir.mkdirs();
        System.setProperty("netbeans.user", userdir.getAbsolutePath());

        createFolder(workDirPath).delete();
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        createFolder(workDirPath).delete();
    }
     
    
    protected boolean isMasterFS() throws IOException {
        File f = VCSFileProxy.createFileProxy(getVersionedFolder()).toFile(); // reurns null if not masterfs
        return f != null;
    }

    protected FileObject createFolder (String path) throws IOException {
        return VCSFilesystemTestFactory.getInstance(this).createFolder(path);
    }
    
    protected VCSFileProxy toVCSFileProxy(FileObject fo) throws IOException {
        return VCSFileProxy.createFileProxy(fo);
    }

    protected Class getVCS() {
        return TestVCS.class;
    }    
}
