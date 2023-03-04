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


import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import javax.imageio.IIOException;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.versioning.core.DelegatingVCS;
import org.netbeans.modules.versioning.core.VersioningManager;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.spi.VCSInterceptor;
import org.netbeans.modules.versioning.core.util.VCSSystemProvider;
import org.netbeans.modules.versioning.core.spi.testvcs.TestVCS;
import org.openide.filesystems.FileChangeAdapter;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.test.MockLookup;

/**
 * 
 * @author tomas
 */
public class AfterDeleteAfterMoveEndsTest extends NbTestCase {
    
    private ExternalyMovedInterceptor inteceptor;
    private FileObject versionedFolder;

    public AfterDeleteAfterMoveEndsTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockLookup.setLayersAndInstances();
        TestVCS testVCS = (TestVCS) getDelegatingVCS().getDelegate();
        inteceptor = new ExternalyMovedInterceptor();
        testVCS.setVCSInterceptor(inteceptor);
        
        File userdir = new File(getWorkDir(), "userdir");
        userdir.mkdirs();
        System.setProperty("netbeans.user", userdir.getAbsolutePath());
        System.setProperty("versioning.no.localhistory.interceptor", "true");
    }

    @Override
    protected void tearDown() throws Exception {
    }

    public void testExternalyMoved() throws IOException {
        FileObject fo = getVersionedFolder();
        FileObject fromFile = fo.createData("move.txt");
        FileObject toFolder = fo.createFolder("toFolder");
        
        fo.addFileChangeListener(new FileChangeAdapter());
        inteceptor.startAcceptingEvents = true;
        FileObject toFile = fromFile.move(fromFile.lock(), toFolder, fromFile.getName(), fromFile.getExt());
        
        VCSFileProxy fromProxy = VCSFileProxy.createFileProxy(fromFile);
        VCSFileProxy toProxy = VCSFileProxy.createFileProxy(toFile);
        assertEquals(ExternalyMovedInterceptor.DO_MOVE  + " from " + fromProxy + " " + toProxy, inteceptor.events.get(0));
        assertEquals(ExternalyMovedInterceptor.TIMEDOUT + " from " + fromProxy + " " + toProxy, inteceptor.events.get(1));
        
    }

    private DelegatingVCS getDelegatingVCS() {
        Collection<? extends VCSSystemProvider> providers = Lookup.getDefault().lookupAll(VCSSystemProvider.class);
        for (VCSSystemProvider p : providers) {
            Collection<VCSSystemProvider.VersioningSystem> systems = p.getVersioningSystems();
            for (VCSSystemProvider.VersioningSystem vs : systems) {
                if(vs instanceof DelegatingVCS) {
                    DelegatingVCS dvcs = (DelegatingVCS)vs;
                    if("TestVCSDisplay".equals(dvcs.getDisplayName())) {
                        return dvcs;
                    }
                }
            }
        }
        return null;
    }

    private FileObject getVersionedFolder() throws IOException {
        if (versionedFolder == null) {
            File f = new File(getWorkDirPath() + "/root" + TestVCS.VERSIONED_FOLDER_SUFFIX);
            if(f.exists()) {
                FileUtil.toFileObject(f).delete();
            }
            f.mkdirs();
            versionedFolder = FileUtil.toFileObject(f);
            File md = new File(f, TestVCS.TEST_VCS_METADATA);
            md.mkdirs();
            // cleanup the owner cache, this folder just became versioned 
            VersioningManager.getInstance().flushNullOwners(); 
        }
        return versionedFolder;
    }

    private static class ExternalyMovedInterceptor extends VCSInterceptor {

        private static final int TIMEOUT = 10000;
        static final String DO_MOVE = "domove";
        static final String AFTER_DELETE = "afterdelete";
        static final String TIMEDOUT = "timeout";
        private List<String> events = new LinkedList<String>();
        
        private transient Exception afterDelete;
        private transient boolean startAcceptingEvents;

        @Override
        public boolean beforeMove(VCSFileProxy from, VCSFileProxy to) {
            System.out.println(" intercepted beforeMove from " + from + " to " + to );
            return true;
        }
        
        @Override
        public void doMove(VCSFileProxy from, VCSFileProxy to) throws IOException {
            if(!startAcceptingEvents) {
                return;
            }
            System.out.println(" intercepted doMove from " + from + " to " + to );
            events.add(DO_MOVE + " from " + from + " " + to);
            
            // invoke rename
            from.toFile().renameTo(to.toFile());
            
            // now wait if afterDelete doesn't happen before we leave doMove();
            long t = System.currentTimeMillis();
            while(afterDelete == null) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException ex) {
                    throw new IIOException("doMove from " + from + " to " + to , ex);
                }
                if(System.currentTimeMillis() - t > TIMEOUT) {
                    events.add(TIMEDOUT + " from " + from + " " + to);
                    return;
                }
            }
        }

        @Override
        public void afterDelete(VCSFileProxy file) {
            if(!startAcceptingEvents) {
                return;
            }
            
            afterDelete = new Exception("intercepted afterDelete " + file);
//            afterDelete.printStackTrace();
            events.add(AFTER_DELETE + " " + file);
        }
        
    }
}
