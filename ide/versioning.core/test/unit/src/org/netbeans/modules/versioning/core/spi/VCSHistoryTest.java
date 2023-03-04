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
package org.netbeans.modules.versioning.core.spi;


import java.io.File;
import java.io.IOException;
import java.util.Date;
import javax.swing.Action;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.api.VersioningSupport;
import org.netbeans.modules.versioning.core.spi.VCSHistoryProvider.HistoryEntry;
import org.netbeans.modules.versioning.core.util.Utils;
import org.netbeans.modules.versioning.core.util.VCSSystemProvider;
import org.netbeans.modules.versioning.core.spi.testvcs.TestVCS;
import org.netbeans.modules.versioning.core.spi.testvcs.TestVCSHistoryProvider;

import org.openide.util.Lookup;
import org.openide.util.test.MockLookup;

/**
 * Versioning SPI unit tests of VCSHistoryProvide.
 * 
 * @author Tomas Stupka
 */
public class VCSHistoryTest extends NbTestCase {
    
    private File workDir;

    public VCSHistoryTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        workDir = getDataDir(); 
        File userdir = new File(getDataDir(), "userdir");
        userdir.mkdirs();
        System.setProperty("netbeans.user", userdir.getAbsolutePath());
        clearWorkDir();
        MockLookup.setLayersAndInstances();
        Lookup.getDefault().lookupAll(VersioningSystem.class);
        File f = new File(workDir, "workdir/root-test-versioned");
        new File(f, TestVCS.TEST_VCS_METADATA).mkdirs();
    }

    public void testHistoryProvider() throws IOException {
        File f = new File(workDir, "workdir/root-test-versioned");
        VersioningSystem vs = VersioningSupport.getOwner(VCSFileProxy.createFileProxy(f));
        assertNotNull(vs);
        VCSHistoryProvider hp = vs.getVCSHistoryProvider();
        assertNotNull(hp);
    }
    
    public void testHistoryEntryProvidesRevision() throws IOException {
        RevisionProviderImpl provider = new RevisionProviderImpl();
        provider.revisionprovided = false;
        VCSHistoryProvider.HistoryEntry h = 
                new VCSHistoryProvider.HistoryEntry(
                    new VCSFileProxy[] {VCSFileProxy.createFileProxy(new File(""))}, 
                    new Date(System.currentTimeMillis()), 
                    "msg", 
                    "user", 
                    "username", 
                    "12345", 
                    "1234567890", 
                    new Action[0], 
                    provider);
        h.getRevisionFile(VCSFileProxy.createFileProxy(new File("")), VCSFileProxy.createFileProxy(new File("")));
        assertTrue(provider.revisionprovided);
    }
    
    public void testHistoryEntryProvidesParent() throws IOException {
        ParentProviderImpl provider = new ParentProviderImpl();
        VCSFileProxy file = VCSFileProxy.createFileProxy(new File(""));
        VCSHistoryProvider.HistoryEntry h =
                new VCSHistoryProvider.HistoryEntry(
                new VCSFileProxy[] {file},
                new Date(System.currentTimeMillis()),
                "msg",
                "user",
                "username",
                "12345",
                "1234567890",
                new Action[0],
                null,
                null,
                provider);
        HistoryEntry parent = h.getParentEntry(file);
        assertNotNull(parent);
        assertEquals(ParentProviderImpl.PARENT_MSG, parent.getMessage());
    }

    public void testHistoryEntryDoesntProvideRevision() throws IOException {
        RevisionProviderImpl provider = new RevisionProviderImpl();
        provider.revisionprovided = false;
        VCSHistoryProvider.HistoryEntry h = 
                new VCSHistoryProvider.HistoryEntry(
                    new VCSFileProxy[] {VCSFileProxy.createFileProxy(new File(""))}, 
                    new Date(System.currentTimeMillis()), 
                    "msg", 
                    "user", 
                    "username", 
                    "12345", 
                    "1234567890", 
                    new Action[0], 
                    null);
        h.getRevisionFile(VCSFileProxy.createFileProxy(new File("")), VCSFileProxy.createFileProxy(new File("")));
        // nothing happend
    }
    
    public void testHistoryEntryDoesntProvideParent() throws IOException {
        RevisionProviderImpl provider = new RevisionProviderImpl();
        provider.revisionprovided = false;
        VCSFileProxy file = VCSFileProxy.createFileProxy(new File(""));
        VCSHistoryProvider.HistoryEntry h = 
                new VCSHistoryProvider.HistoryEntry(
                    new VCSFileProxy[] {file}, 
                    new Date(System.currentTimeMillis()), 
                    "msg", 
                    "user", 
                    "username", 
                    "12345", 
                    "1234567890", 
                    new Action[0], 
                    null);
        h.getParentEntry(file);
        // nothing happend
    }
    
    public void testHistoryEntryEditable() throws IOException {
        MessageEditProviderImpl provider = new MessageEditProviderImpl();
        provider.message = null;
        VCSHistoryProvider.HistoryEntry h = 
                new VCSHistoryProvider.HistoryEntry(
                    new VCSFileProxy[] {VCSFileProxy.createFileProxy(new File(""))}, 
                    new Date(System.currentTimeMillis()), 
                    "msg", 
                    "user", 
                    "username", 
                    "12345", 
                    "1234567890", 
                    new Action[0], 
                    null,
                    provider);
        assertTrue(h.canEdit());
        h.setMessage("msg2");
        assertEquals("msg2", provider.message);
    }
    
    public void testHistoryEntryMsgReadOnly() throws IOException {
        VCSHistoryProvider.HistoryEntry h = 
                new VCSHistoryProvider.HistoryEntry(
                    new VCSFileProxy[] {VCSFileProxy.createFileProxy(new File(""))}, 
                    new Date(System.currentTimeMillis()), 
                    "msg", 
                    "user", 
                    "username", 
                    "12345", 
                    "1234567890", 
                    new Action[0], 
                    null,
                    null);
        try {
            h.setMessage("msg2");
        } catch(Throwable t) {
            return;
        }
        fail("exception should be raised on readonly entry");
    }

    public void testHistoryGetRevisionIsReallyInvoked() throws IOException {
        File f = new File(workDir, "workdir/root-test-versioned/" + TestVCSHistoryProvider.FILE_PROVIDES_REVISIONS_SUFFIX);
        f.createNewFile();
        VCSFileProxy proxy = VCSFileProxy.createFileProxy(f);
        VersioningSystem vs = VersioningSupport.getOwner(proxy);
        assertNotNull(vs);
        VCSHistoryProvider hp = vs.getVCSHistoryProvider();
        assertNotNull(hp);
        historyGetRevisionIsReallyInvoked(hp, proxy);        
        
        VCSSystemProvider.VersioningSystem pvs = Utils.getOwner(proxy);
        assertNotNull(pvs);
        VCSHistoryProvider php = pvs.getVCSHistoryProvider();
        assertNotNull(php);
        historyGetRevisionIsReallyInvoked(php, proxy);        
    }

    private void historyGetRevisionIsReallyInvoked(VCSHistoryProvider hp, VCSFileProxy proxy) {
        TestVCSHistoryProvider.reset();
        HistoryEntry[] history = hp.getHistory(new VCSFileProxy[] {proxy}, null);
        assertNotNull(history);
        assertTrue(history.length > 0);
        history[0].getRevisionFile(VCSFileProxy.createFileProxy(new File("")), VCSFileProxy.createFileProxy(new File("")));
        assertTrue(TestVCSHistoryProvider.instance.revisionProvided);
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
    
    private class RevisionProviderImpl implements VCSHistoryProvider.RevisionProvider {
        boolean revisionprovided = false;
        @Override
        public void getRevisionFile(VCSFileProxy originalFile, VCSFileProxy revisionFile) {
            revisionprovided = true;
        }
    }
    
    private class MessageEditProviderImpl implements VCSHistoryProvider.MessageEditProvider {
        String message;
        @Override
        public void setMessage(String message) throws IOException {
            this.message = message;
        }
    }       
    
    private class ParentProviderImpl implements VCSHistoryProvider.ParentProvider {
        static final String PARENT_MSG = "im.the.parent";
        @Override
        public HistoryEntry getParentEntry(VCSFileProxy file) {
            return new HistoryEntry(
                    new VCSFileProxy[] {file}, 
                    new Date(System.currentTimeMillis()), 
                    PARENT_MSG, 
                    "user", 
                    "username", 
                    "12345", 
                    "1234567890", 
                    new Action[0], 
                    null,
                    null);
        }
    }

}
