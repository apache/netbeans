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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
