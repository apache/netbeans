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
package org.netbeans.modules.versioning.core;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.util.VCSSystemProvider;
import org.netbeans.modules.versioning.core.util.VCSSystemProvider.VersioningSystem;
import org.netbeans.modules.versioning.core.spi.VCSAnnotator;
import org.netbeans.modules.versioning.core.spi.VCSInterceptor;
import org.netbeans.modules.versioning.core.spi.VCSVisibilityQuery;
import org.netbeans.modules.versioning.core.api.VersioningSupport;
import org.netbeans.modules.versioning.core.spi.*;
import org.netbeans.spi.queries.CollocationQueryImplementation2;
import org.openide.util.test.MockLookup;

/**
 *
 * @author ondra
 */
public class ConnectDisconnectTest extends NbTestCase {
    private static final String VERSIONED_COMMON_FOLDER_SUFFIX = "-connectdisconnect-versioned-common";

    public ConnectDisconnectTest (String name) {
        super(name);
    }
    
    @Override
    protected void setUp() throws Exception {
        MockLookup.setLayersAndInstances();
        File userdir = new File(getWorkDir(), "userdir");
        userdir.mkdirs();
        System.setProperty("netbeans.user", userdir.getAbsolutePath());
        
        // cleanup disconnected folders
        Utils.put(VersioningConfig.getDefault().getPreferences(), "disconnectedFolders", new ArrayList<String>());
        
        super.setUp();
    }
    
    public void testSingleVCS () throws Exception {
        File root = new File(getWorkDir(), "root-connectdisconnect-versioned1");
        File folder = new File(root, "folder");
        folder.mkdirs();
        File file = new File(folder, "file");
        file.createNewFile();
        
        VCSFileProxy fileProxy = VCSFileProxy.createFileProxy(file);
        VCSFileProxy rootProxy = VCSFileProxy.createFileProxy(root);
        VersioningSupport.getOwner(fileProxy);
        awakeDelegates();
        
        assertEquals(DisconnectableVCS1.proxyVS.getDelegate(), VersioningSupport.getOwner(fileProxy));
        assertEquals(rootProxy, DisconnectableVCS1.proxyVS.getTopmostManagedAncestor(fileProxy));
        // disconnect
        VersioningConfig.getDefault().disconnectRepository(DisconnectableVCS1.proxyVS, rootProxy);
        VersioningManager.getInstance().versionedRootsChanged();
        assertEquals(null, VersioningSupport.getOwner(fileProxy));
        assertEquals(rootProxy, DisconnectableVCS1.proxyVS.getTopmostManagedAncestor(fileProxy));
        // reconnect
        new VersioningMainMenu.ConnectAction(DisconnectableVCS1.proxyVS, rootProxy, "").actionPerformed(new ActionEvent(DisconnectableVCS1.proxyVS, ActionEvent.ACTION_PERFORMED, null));
        assertEquals(DisconnectableVCS1.proxyVS.getDelegate(), VersioningSupport.getOwner(fileProxy));
        assertEquals(rootProxy, DisconnectableVCS1.proxyVS.getTopmostManagedAncestor(fileProxy));
    }

    public void testMultipleVCS () throws Exception {
        File root = new File(getWorkDir(), "root-connectdisconnect-versioned-common");
        File folder = new File(root, "folder");
        folder.mkdirs();
        File file = new File(folder, "file");
        file.createNewFile();

        VCSFileProxy fileProxy = VCSFileProxy.createFileProxy(file);
        VCSFileProxy rootProxy = VCSFileProxy.createFileProxy(root);
        awakeDelegates();
        
        org.netbeans.modules.versioning.core.spi.VersioningSystem owner = VersioningSupport.getOwner(fileProxy);
        assertTrue(owner.toString(), owner == DisconnectableVCS1.proxyVS.getDelegate() || owner == DisconnectableVCS2.proxyVS.getDelegate());
        assertEquals(rootProxy, DisconnectableVCS1.proxyVS.getTopmostManagedAncestor(fileProxy));
        assertEquals(rootProxy, DisconnectableVCS2.proxyVS.getTopmostManagedAncestor(fileProxy));
        // disconnect vcs1
        VersioningConfig.getDefault().disconnectRepository(DisconnectableVCS1.proxyVS, rootProxy);
        VersioningManager.getInstance().versionedRootsChanged();
        assertEquals(DisconnectableVCS2.proxyVS.getDelegate(), VersioningSupport.getOwner(fileProxy));
        assertEquals(rootProxy, DisconnectableVCS1.proxyVS.getTopmostManagedAncestor(fileProxy));
        assertEquals(rootProxy, DisconnectableVCS2.proxyVS.getTopmostManagedAncestor(fileProxy));
        // disconnect vcs2
        VersioningConfig.getDefault().disconnectRepository(DisconnectableVCS2.proxyVS, rootProxy);
        VersioningManager.getInstance().versionedRootsChanged();
        assertEquals(null, VersioningSupport.getOwner(fileProxy));
        assertEquals(rootProxy, DisconnectableVCS1.proxyVS.getTopmostManagedAncestor(fileProxy));
        assertEquals(rootProxy, DisconnectableVCS2.proxyVS.getTopmostManagedAncestor(fileProxy));
        // reconnect vcs1
        new VersioningMainMenu.ConnectAction(DisconnectableVCS1.proxyVS, rootProxy, "").actionPerformed(new ActionEvent(DisconnectableVCS1.proxyVS, ActionEvent.ACTION_PERFORMED, null));
        assertEquals(DisconnectableVCS1.proxyVS.getDelegate(), VersioningSupport.getOwner(fileProxy));
        assertEquals(rootProxy, DisconnectableVCS1.proxyVS.getTopmostManagedAncestor(fileProxy));
        assertEquals(rootProxy, DisconnectableVCS2.proxyVS.getTopmostManagedAncestor(fileProxy));
        // reconnect vcs2
        new VersioningMainMenu.ConnectAction(DisconnectableVCS2.proxyVS, rootProxy, "").actionPerformed(new ActionEvent(DisconnectableVCS2.proxyVS, ActionEvent.ACTION_PERFORMED, null));
        owner = VersioningSupport.getOwner(fileProxy);
        assertTrue(owner.toString(), owner == DisconnectableVCS1.proxyVS.getDelegate() || owner == DisconnectableVCS2.proxyVS.getDelegate());
        assertEquals(rootProxy, DisconnectableVCS1.proxyVS.getTopmostManagedAncestor(fileProxy));
        assertEquals(rootProxy, DisconnectableVCS2.proxyVS.getTopmostManagedAncestor(fileProxy));
    }

    public void testHierarchicalVCS () throws Exception {
        File root = new File(getWorkDir(), "root-connectdisconnect-versioned1");
        File folder = new File(root, "root-connectdisconnect-versioned2");
        folder.mkdirs();
        VCSFileProxy folderProxy = VCSFileProxy.createFileProxy(folder);
        VCSFileProxy rootProxy = VCSFileProxy.createFileProxy(root);
        VersioningSupport.getOwner(folderProxy);
        awakeDelegates();

        assertEquals(DisconnectableVCS1.proxyVS.getDelegate(), VersioningSupport.getOwner(rootProxy));
        assertEquals(rootProxy, DisconnectableVCS1.proxyVS.getTopmostManagedAncestor(folderProxy));
        assertEquals(DisconnectableVCS2.proxyVS.getDelegate(), VersioningSupport.getOwner(folderProxy));
        assertEquals(folderProxy, DisconnectableVCS2.proxyVS.getTopmostManagedAncestor(folderProxy));
        // disconnect vcs2
        VersioningConfig.getDefault().disconnectRepository(DisconnectableVCS2.proxyVS, folderProxy);
        VersioningManager.getInstance().versionedRootsChanged();
        assertEquals(DisconnectableVCS1.proxyVS.getDelegate(), VersioningSupport.getOwner(folderProxy));
        assertEquals(DisconnectableVCS1.proxyVS.getDelegate(), VersioningSupport.getOwner(rootProxy));
        assertEquals(rootProxy, DisconnectableVCS1.proxyVS.getTopmostManagedAncestor(folderProxy));
        assertEquals(folderProxy, DisconnectableVCS2.proxyVS.getTopmostManagedAncestor(folderProxy));
        // disconnect vcs1
        VersioningConfig.getDefault().disconnectRepository(DisconnectableVCS1.proxyVS, rootProxy);
        VersioningManager.getInstance().versionedRootsChanged();
        assertEquals(null, VersioningSupport.getOwner(folderProxy));
        assertEquals(null, VersioningSupport.getOwner(rootProxy));
        assertEquals(rootProxy, DisconnectableVCS1.proxyVS.getTopmostManagedAncestor(folderProxy));
        assertEquals(folderProxy, DisconnectableVCS2.proxyVS.getTopmostManagedAncestor(folderProxy));
        // reconnect vcs2
        new VersioningMainMenu.ConnectAction(DisconnectableVCS2.proxyVS, folderProxy, "").actionPerformed(new ActionEvent(DisconnectableVCS2.proxyVS, ActionEvent.ACTION_PERFORMED, null));
        assertEquals(DisconnectableVCS2.proxyVS.getDelegate(), VersioningSupport.getOwner(folderProxy));
        assertEquals(null, VersioningSupport.getOwner(rootProxy));
        assertEquals(rootProxy, DisconnectableVCS1.proxyVS.getTopmostManagedAncestor(folderProxy));
        assertEquals(folderProxy, DisconnectableVCS2.proxyVS.getTopmostManagedAncestor(folderProxy));
        // reconnect vcs1
        new VersioningMainMenu.ConnectAction(DisconnectableVCS1.proxyVS, rootProxy, "").actionPerformed(new ActionEvent(DisconnectableVCS1.proxyVS, ActionEvent.ACTION_PERFORMED, null));
        assertEquals(DisconnectableVCS2.proxyVS.getDelegate(), VersioningSupport.getOwner(folderProxy));
        assertEquals(DisconnectableVCS1.proxyVS.getDelegate(), VersioningSupport.getOwner(rootProxy));
        assertEquals(rootProxy, DisconnectableVCS1.proxyVS.getTopmostManagedAncestor(folderProxy));
        assertEquals(folderProxy, DisconnectableVCS2.proxyVS.getTopmostManagedAncestor(folderProxy));
    }

    @   org.netbeans.modules.versioning.core.spi.VersioningSystem.Registration(actionsCategory="test", displayName="DisconnectableVCS1", menuLabel="DisconnectableVCS1menu", metadataFolderNames=".test")
    public static class DisconnectableVCS1 extends org.netbeans.modules.versioning.core.spi.VersioningSystem {
        private static DisconnectableVCS1 instance;
        private static VCSSystemProvider.VersioningSystem<org.netbeans.modules.versioning.core.spi.VersioningSystem> proxyVS;
        public static final String VERSIONED_FOLDER_SUFFIX = "-connectdisconnect-versioned1";

        public DisconnectableVCS1 () {
            instance = this;
            proxyVS = new TestVCSProxy(instance);        
        }
        
        @Override
        public VCSFileProxy getTopmostManagedAncestor(VCSFileProxy file) {
            VCSFileProxy topmost = null;
            for (; file != null; file = file.getParentFile()) {
                if (file.getName().endsWith(VERSIONED_FOLDER_SUFFIX) || file.getName().endsWith(VERSIONED_COMMON_FOLDER_SUFFIX)) {
                    topmost = file;
                }
            }
            return topmost;
        }
    }

    @   org.netbeans.modules.versioning.core.spi.VersioningSystem.Registration(actionsCategory="test", displayName="DisconnectableVCS2", menuLabel="DisconnectableVCS2menu", metadataFolderNames=".test")
    public static class DisconnectableVCS2 extends org.netbeans.modules.versioning.core.spi.VersioningSystem {
        private static DisconnectableVCS2 instance;
        private static VCSSystemProvider.VersioningSystem<org.netbeans.modules.versioning.core.spi.VersioningSystem> proxyVS;
        public static final String VERSIONED_FOLDER_SUFFIX = "-connectdisconnect-versioned2";

        public DisconnectableVCS2 () {
            instance = this;
            proxyVS = new TestVCSProxy(instance);
        }
        
        @Override
        public VCSFileProxy getTopmostManagedAncestor(VCSFileProxy file) {
            VCSFileProxy topmost = null;
            for (; file != null; file = file.getParentFile()) {
                if (file.getName().endsWith(VERSIONED_FOLDER_SUFFIX) || file.getName().endsWith(VERSIONED_COMMON_FOLDER_SUFFIX)) {
                    topmost = file;
                }
            }
            return topmost;
        }
    }
    
    private static class TestVCSProxy implements VCSSystemProvider.VersioningSystem<org.netbeans.modules.versioning.core.spi.VersioningSystem> {
        private final org.netbeans.modules.versioning.core.spi.VersioningSystem versioningSystem;

        public TestVCSProxy(org.netbeans.modules.versioning.core.spi.VersioningSystem versioningSystem) {
            this.versioningSystem = versioningSystem;
        }
        
        @Override
        public org.netbeans.modules.versioning.core.spi.VersioningSystem getDelegate() {
            return versioningSystem;
        }

        @Override
        public boolean isLocalHistory() {
            return false;
        }

        @Override
        public VCSFileProxy getTopmostManagedAncestor(VCSFileProxy file) {
            return versioningSystem.getTopmostManagedAncestor(file);
        }

        @Override
        public VCSAnnotator getVCSAnnotator() {
            return null;
        }

        @Override
        public VCSInterceptor getVCSInterceptor() {
            return null;
        }

        @Override
        public void getOriginalFile(VCSFileProxy workingCopy, VCSFileProxy originalFile) { }

        @Override
        public CollocationQueryImplementation2 getCollocationQueryImplementation() {
            return null;
        }

        @Override
        public VCSVisibilityQuery getVisibilityQuery() {
            return null;
        }

        @Override
        public void addPropertyCL(PropertyChangeListener listener) {
            versioningSystem.addPropertyChangeListener(listener);
        }

        @Override
        public void removePropertyCL(PropertyChangeListener listener) {
            versioningSystem.removePropertyChangeListener(listener);
        }

        @Override
        public boolean isExcluded(VCSFileProxy file) {
            return VersioningSupport.isExcluded(file);
        }

        @Override
        public String getDisplayName() {
            return "TestVCSProxy";
        }

        @Override
        public String getMenuLabel() {
            return "TestVCSProxy";
        }

        @Override
        public boolean accept(VCSContext ctx) {
            return true;
        }

        @Override
        public VCSHistoryProvider getVCSHistoryProvider() {
            return null;
        }

        @Override
        public boolean isMetadataFile(VCSFileProxy file) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
    }
    private static void awakeDelegates() {
        for(VersioningSystem s : VersioningManager.getInstance().getVersioningSystems()) {
            Object vs = s.getDelegate();
            if(vs instanceof DelegatingVCS && ((String)((DelegatingVCS) vs).getDisplayName()).startsWith("DisconnectableVCS")) {
                ((DelegatingVCS) vs).getDelegate();
            }
        }
    }
}
