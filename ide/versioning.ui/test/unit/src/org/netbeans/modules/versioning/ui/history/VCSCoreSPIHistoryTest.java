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
package org.netbeans.modules.versioning.ui.history;


import java.io.File;
import java.io.IOException;
import java.util.Collection;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.spi.VCSHistoryProvider.HistoryEntry;
import org.netbeans.modules.versioning.core.util.Utils;
import org.netbeans.modules.versioning.core.util.VCSSystemProvider;
import org.netbeans.modules.versioning.core.util.VCSSystemProvider.VersioningSystem;
import org.netbeans.modules.versioning.spi.testvcs.TestVCSHistoryProvider;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.test.MockLookup;

/**
 * Versioning SPI unit tests of VCSHistoryProvide.
 * 
 * @author Tomas Stupka
 */
public class VCSCoreSPIHistoryTest extends NbTestCase {
    
    private File dataRootDir;

    public VCSCoreSPIHistoryTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        dataRootDir = getDataDir(); 
        File userdir = new File(dataRootDir + "userdir");
        userdir.mkdirs();
        System.setProperty("netbeans.user", userdir.getAbsolutePath());
        if(!dataRootDir.exists()) dataRootDir.mkdirs();
        MockLookup.setLayersAndInstances();
        Lookup.getDefault().lookupAll(VersioningSystem.class);
        File f = new File(dataRootDir, "workdir");
        deleteRecursively(f);
        f.mkdirs();
        f = new File(dataRootDir, "workdir/root-test-versioned");
        f.mkdirs();
    }

    public void testHistoryNodesProperlySetup() throws IOException {
        File f1 = new File(dataRootDir, "workdir/root-test-versioned/file1" + TestVCSHistoryProvider.FILE_PROVIDES_REVISIONS_SUFFIX);
        f1.createNewFile();
        VCSFileProxy proxy1 = VCSFileProxy.createFileProxy(f1);
        File f2 = new File(dataRootDir, "workdir/root-test-versioned/file2" + TestVCSHistoryProvider.FILE_PROVIDES_REVISIONS_SUFFIX);
        f2.createNewFile();
        VCSFileProxy proxy2 = VCSFileProxy.createFileProxy(f2);
        VCSSystemProvider.VersioningSystem pvs = Utils.getOwner(proxy1);
        assertNotNull(pvs);
        org.netbeans.modules.versioning.core.spi.VCSHistoryProvider php = pvs.getVCSHistoryProvider();
        assertNotNull(php);

        org.netbeans.modules.versioning.core.spi.VCSHistoryProvider.HistoryEntry[] phistory = php.getHistory(new VCSFileProxy[] {proxy1, proxy2}, null);
        assertNotNull(phistory);
        assertTrue(phistory.length > 0);
        
        Node node = HistoryTestKit.createHistoryNode(phistory[0]);
        assertNotNull(node.getLookup().lookup(HistoryEntry.class));
        
        Collection<? extends VCSFileProxy> proxies = node.getLookup().lookupAll(VCSFileProxy.class);
        assertNotNull(proxies);
        assertTrue(proxies.size() >= 2);
        assertTrue(proxies.contains(proxy1));
        assertTrue(proxies.contains(proxy2));
    }
    
    private void deleteRecursively(File f) {
        if(f.isFile()) {
            f.delete();
        } else {
            File[] files = f.listFiles();
            if(files != null) {
                for (File file : files) {
                    deleteRecursively(file);
                    file.delete();
                }
            }
        }
    }
}
