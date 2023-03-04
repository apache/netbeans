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
package org.netbeans.modules.versioning.spi;


import java.io.File;
import java.io.IOException;
import java.util.Date;
import javax.swing.Action;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.util.Utils;
import org.netbeans.modules.versioning.core.util.VCSSystemProvider;
import org.netbeans.modules.versioning.spi.VCSHistoryProvider.HistoryEntry;
import org.netbeans.modules.versioning.spi.testvcs.TestVCSHistoryProvider;

import org.openide.util.Lookup;

/**
 * Versioning SPI unit tests of VCSHistoryProvider.
 * 
 * @author Tomas Stupka
 */
public class VCSHistoryTest extends NbTestCase {
    
    private File dataRootDir;

    public VCSHistoryTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        super.setUp();
        dataRootDir = getDataDir(); 
        File userdir = new File(dataRootDir + "userdir");
        userdir.mkdirs();
        System.setProperty("netbeans.user", userdir.getAbsolutePath());
        if(!dataRootDir.exists()) dataRootDir.mkdirs();
        Lookup.getDefault().lookupAll(VersioningSystem.class);
        File f = new File(dataRootDir, "workdir");
        deleteRecursively(f);
        f.mkdirs();
        f = new File(dataRootDir, "workdir/root-test-versioned");
        f.mkdirs();
    }

    public void testHistoryProvider() throws IOException {
        File f = new File(dataRootDir, "workdir/root-test-versioned");
        VersioningSystem vs = VersioningSupport.getOwner(f);
        assertNotNull(vs);
        VCSHistoryProvider hp = vs.getVCSHistoryProvider();
        assertNotNull(hp);
        
        VCSSystemProvider.VersioningSystem pvs = Utils.getOwner(VCSFileProxy.createFileProxy(f));
        assertNotNull(pvs);
        org.netbeans.modules.versioning.core.spi.VCSHistoryProvider php = pvs.getVCSHistoryProvider();
        assertNotNull(php);
        
    }
    
    public void testHistoryEntryProvidesRevision() throws IOException {
        RevisionProviderImpl provider = new RevisionProviderImpl();
        provider.revisionprovided = false;
        VCSHistoryProvider.HistoryEntry h = 
                new VCSHistoryProvider.HistoryEntry(
                    new File[] {new File("")}, 
                    new Date(System.currentTimeMillis()), 
                    "msg", 
                    "user", 
                    "username", 
                    "12345", 
                    "1234567890", 
                    new Action[0], 
                    provider);
        h.getRevisionFile(new File(""), new File(""));
        assertTrue(provider.revisionprovided);
    }
    
    public void testHistoryEntryProvidesParent() throws IOException {
        ParentProviderImpl provider = new ParentProviderImpl();
        File file = new File("");
        VCSHistoryProvider.HistoryEntry h =
                new VCSHistoryProvider.HistoryEntry(
                new File[] {file},
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
        
        h = h.getParentEntry(file);
        assertNotNull(h);
        assertEquals(ParentProviderImpl.PARENT_MSG, h.getMessage());
    }

    public void testHistoryGetRevisionIsReallyInvoked() throws IOException {
        File f = new File(dataRootDir, "workdir/root-test-versioned/" + TestVCSHistoryProvider.FILE_PROVIDES_REVISIONS_SUFFIX);
        f.createNewFile();
        VersioningSystem vs = VersioningSupport.getOwner(f);
        assertNotNull(vs);
        VCSHistoryProvider hp = vs.getVCSHistoryProvider();
        assertNotNull(hp);
        
        TestVCSHistoryProvider.reset();
        HistoryEntry[] history = hp.getHistory(new File[] {f}, null);
        assertNotNull(history);
        assertTrue(history.length > 0);
        history[0].getRevisionFile(new File(""), new File(""));
        assertTrue(TestVCSHistoryProvider.instance.revisionProvided);
        
        // the same test again just to see that VCSSystemProvider.VersioningSystem properly delegates
        VCSFileProxy proxy = VCSFileProxy.createFileProxy(f);
        VCSSystemProvider.VersioningSystem pvs = Utils.getOwner(proxy);
        assertNotNull(pvs);
        org.netbeans.modules.versioning.core.spi.VCSHistoryProvider php = pvs.getVCSHistoryProvider();
        assertNotNull(php);

        TestVCSHistoryProvider.reset();
        org.netbeans.modules.versioning.core.spi.VCSHistoryProvider.HistoryEntry[] phistory = php.getHistory(new VCSFileProxy[] {proxy}, null);
        assertNotNull(phistory);
        assertTrue(phistory.length > 0);
        phistory[0].getRevisionFile(proxy, proxy);
        assertTrue(TestVCSHistoryProvider.instance.revisionProvided);
    }
    
    public void testHistoryGetParentIsReallyInvoked() throws IOException {
        File f = new File(dataRootDir, "workdir/root-test-versioned/" + TestVCSHistoryProvider.FILE_PROVIDES_REVISIONS_SUFFIX);
        f.createNewFile();
        VersioningSystem vs = VersioningSupport.getOwner(f);
        assertNotNull(vs);
        VCSHistoryProvider hp = vs.getVCSHistoryProvider();
        assertNotNull(hp);
        
        HistoryEntry[] history = hp.getHistory(new File[] {f}, null);
        assertNotNull(history);
        assertTrue(history.length > 0);
        HistoryEntry parentEntry = history[0].getParentEntry(f);
        assertNotNull(parentEntry);
        assertEquals(TestVCSHistoryProvider.PARENT_MSG, parentEntry.getMessage());
        
        // the same test again just to see that VCSSystemProvider.VersioningSystem properly delegates
        VCSFileProxy proxy = VCSFileProxy.createFileProxy(f);
        VCSSystemProvider.VersioningSystem pvs = Utils.getOwner(proxy);
        assertNotNull(pvs);
        org.netbeans.modules.versioning.core.spi.VCSHistoryProvider php = pvs.getVCSHistoryProvider();
        assertNotNull(php);

        org.netbeans.modules.versioning.core.spi.VCSHistoryProvider.HistoryEntry[] phistory = php.getHistory(new VCSFileProxy[] {proxy}, null);
        assertNotNull(phistory);
        assertTrue(phistory.length > 0);
        org.netbeans.modules.versioning.core.spi.VCSHistoryProvider.HistoryEntry proxyParentEntry = phistory[0].getParentEntry(proxy);
        assertEquals(TestVCSHistoryProvider.PARENT_MSG, proxyParentEntry.getMessage());
    }

    public void testHistoryEntryDoesntProvideRevision() throws IOException {
        RevisionProviderImpl provider = new RevisionProviderImpl();
        provider.revisionprovided = false;
        VCSHistoryProvider.HistoryEntry h = 
                new VCSHistoryProvider.HistoryEntry(
                    new File[] {new File("")}, 
                    new Date(System.currentTimeMillis()), 
                    "msg", 
                    "user", 
                    "username", 
                    "12345", 
                    "1234567890", 
                    new Action[0], 
                    null);
        h.getRevisionFile(new File(""), new File(""));
        // nothing happend
    }
    
    public void testHistoryEntryDoesntProvideParent() throws IOException {
        RevisionProviderImpl provider = new RevisionProviderImpl();
        provider.revisionprovided = false;
        final File file = new File("");
        VCSHistoryProvider.HistoryEntry h = 
                new VCSHistoryProvider.HistoryEntry(
                    new File[] {file}, 
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
                    new File[] {new File("")}, 
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
                    new File[] {new File("")}, 
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
        public void getRevisionFile(File originalFile, File revisionFile) {
            revisionprovided = true;
        }
    }
    
    private class ParentProviderImpl implements VCSHistoryProvider.ParentProvider {
        static final String PARENT_MSG = "im.the.parent";
        @Override
        public HistoryEntry getParentEntry(File file) {
            return new VCSHistoryProvider.HistoryEntry(
                    new File[] {file}, 
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
    
    private class MessageEditProviderImpl implements VCSHistoryProvider.MessageEditProvider {
        String message;
        @Override
        public void setMessage(String message) throws IOException {
            this.message = message;
        }
    }       
}
