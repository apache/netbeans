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

import org.netbeans.modules.versioning.core.VersioningManager;
import java.io.File;
import java.io.IOException;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.versioning.core.util.VCSSystemProvider;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.spi.VersioningSupport;
import org.netbeans.modules.versioning.spi.testvcs.TestAnnotatedVCS;
import org.openide.util.test.MockLookup;

public class GetAnnotatedOwnerTest extends GetOwnerTest {
    
    public GetAnnotatedOwnerTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockLookup.setLayersAndInstances();
    }
    
    @Override
    protected File getVersionedFolder() {
        if (versionedFolder == null) {
            versionedFolder = new File(dataRootDir, "workdir/root-" + TestAnnotatedVCS.VERSIONED_FOLDER_SUFFIX);
            versionedFolder.mkdirs();
            new File(versionedFolder, TestAnnotatedVCS.TEST_VCS_METADATA).mkdirs();
        }
        return versionedFolder;
    }
    
    @Override
    protected Class getVCS() {
        return TestAnnotatedVCS.class;
    }
    
    public static Test suite () {
        TestSuite suite = new NbTestSuite();
        suite.addTest(new GetAnnotatedOwnerTest("testVCSSystemDoesntAwakeOnUnrelatedGetOwner"));
        suite.addTest(new GetAnnotatedOwnerTest("testNoOwnerIfManagedByOtherSPI"));
        return suite;
    }

    // must run as the first test in the suite
    public void testVCSSystemDoesntAwakeOnUnrelatedGetOwner() throws IOException {
        
        assertNull(TestAnnotatedVCS.INSTANCE);
        
        File f = new File(getUnversionedFolder(), "sleepingfile");
        f.createNewFile();
        
        assertNull(TestAnnotatedVCS.INSTANCE);
        VCSSystemProvider.VersioningSystem owner = VersioningManager.getInstance().getOwner(VCSFileProxy.createFileProxy(f));
        assertNull(owner);
        
        assertNull(TestAnnotatedVCS.INSTANCE);
    }
    
    public void testNoOwnerIfManagedByOtherSPI() throws IOException {
        File f = new File(dataRootDir, OtherSPIVCS.MANAGED_FOLDER_PREFIX);
        f.mkdirs();
        f = new File(f, "file.txt");
        assertNull(VersioningSupport.getOwner(f));
        
        f = new File(getVersionedFolder(), "file.txt");
        assertNull(org.netbeans.modules.versioning.core.api.VersioningSupport.getOwner(VCSFileProxy.createFileProxy(f)));
    }
    
    @org.netbeans.modules.versioning.core.spi.VersioningSystem.Registration(
            actionsCategory="fileproxyvcs",
            displayName="fileproxyvcs",
            menuLabel="fileproxyvcs",
            metadataFolderNames="")
    public static class OtherSPIVCS extends org.netbeans.modules.versioning.core.spi.VersioningSystem {
        static String MANAGED_FOLDER_PREFIX = "fileproxyspi";
        @Override
        public VCSFileProxy getTopmostManagedAncestor(VCSFileProxy file) {
            if(file.getParentFile() != null && file.getParentFile().getName().startsWith(MANAGED_FOLDER_PREFIX)) {
                return file.getParentFile();
            }
            return null;
        }
    }
    
}
