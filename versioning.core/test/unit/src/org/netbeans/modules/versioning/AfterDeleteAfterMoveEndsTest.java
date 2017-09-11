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
