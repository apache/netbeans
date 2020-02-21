/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.remote.impl.fs;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Collection;
import junit.framework.Test;
import org.netbeans.modules.dlight.libs.common.PathUtilities;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.test.NativeExecutionTestSupport;
import org.netbeans.modules.nativeexecution.test.NbClustersInfoProvider;
import org.netbeans.modules.nativeexecution.test.RcFile;
import org.netbeans.modules.nativeexecution.test.RcFile.FormatException;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.netbeans.modules.remote.test.RemoteTestSuiteBase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileSystemFactoryHid;
import org.openide.util.Exceptions;

/**
 *
 */
public class RemoteFSTCKTestCase extends FileSystemFactoryHid {
    
    public static final String MSPEC;
    static {
        // Setting netbeans.dirs makes installedFileLocator work properly
        // Needed for native execution...
        System.setProperty("netbeans.dirs", NbClustersInfoProvider.getClusters()); // NOI18N
        System.setProperty("remote.user.password.keep_in_memory", "true"); // NOI18N
        System.setProperty("cnd.mode.unittest", "true");
        System.setProperty("remote.fs_server.verbose", "0");
        System.setProperty("remote.fs_server.log", "true");        

        String mspec = null;
        try {
            RcFile rcFile = NativeExecutionTestSupport.getRcFile();
            mspec = rcFile.get("remote", "fstck.mspec");
        } catch (IOException | FormatException ex) {
            Exceptions.printStackTrace(ex);
        }

        if (mspec == null) {
            mspec = System.getProperty("remote.fstck.mspec");
        }
        if (mspec == null) {
            mspec = System.getenv("REMOTE_FSTCK_MSPEC");
            if (mspec == null) {
                mspec = "intel-S2";
            }
        }
        MSPEC = mspec;
    }

    private ExecutionEnvironment execEnv = null;
    private String tmpDir;
            
    public RemoteFSTCKTestCase(Test test) {
        super(test);
    }

    @Override
    protected void setUp() throws Exception {
        RemoteTestSuiteBase.registerTestSuiteSetup(this.getClass().getSimpleName());
        System.setProperty("nativeexecution.mode.unittest", "true");
        
        String userdir = System.getProperty("netbeans.user");
        if (userdir == null) {
            System.setProperty("netbeans.user", System.getProperty("nbjunit.workdir"));
        }
        RcFile rcFile = NativeExecutionTestSupport.getRcFile();
        String mspec = null;
        for(String section : rcFile.getSections()) {
            if (section.equals("remote.platforms")) {
                Collection<String> keys = rcFile.getKeys(section);
                for(String key : keys) {
                    if (key.equals(MSPEC)) {
                        String get = rcFile.get(section, key, null);
                        if (get == null) {
                            mspec = key;
                        }
                    }
                }
            }
        }
        assertNotNull("Can not find key " + MSPEC + " in [remote.platforms] section", mspec);
        execEnv = NativeExecutionTestSupport.getTestExecutionEnvironment(mspec);
        ConnectionManager.getInstance().connectTo(execEnv);
        tmpDir = NativeExecutionTestSupport.mkTemp(execEnv, true);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        if (tmpDir != null) {
            if (CommonTasksSupport.rmDir(execEnv, tmpDir, true, new OutputStreamWriter(System.err)).get() != 0) {
                // let's try once more as directory needs to be removed
                CommonTasksSupport.rmDir(execEnv, tmpDir, true, new OutputStreamWriter(System.err));
            }
        }
        RemoteTestSuiteBase.registerTestSuiteTearDown(this.getClass().getSimpleName());
    }

    @Override
    protected FileSystem[] createFileSystem(String testName, String[] resources) throws IOException {
        RemoteFileSystemManager.getInstance().resetFileSystem(execEnv, true);
        RemoteFileSystem rfs = RemoteFileSystemManager.getInstance().getFileSystem(execEnv);
        createRemoteFSResources(rfs, testName, resources);
        return new FileSystem[] {rfs};
    }

    @Override
    protected void destroyFileSystem(String testName) throws IOException {
        RemoteFileSystemManager.getInstance().resetFileSystem(execEnv, true);
    }

    @Override
    protected String getResourcePrefix(String testName, String[] resources) {
        return tmpDir + '/' + testName;
    }
    
    private void createRemoteFSResources(RemoteFileSystem rfs, String testName, String[] resources) throws IOException {
        String[] data = new String[resources.length];
        for (int i = 0; i < resources.length; i++) {
            String path = resources[i];
            String type;
            if (path.startsWith("/")) {
                path = path.substring(1);
            }
            if (path.endsWith("/")) {
                type = "d ";
                path = path.substring(0, path.length() - 1);
            } else {
                type = "- ";
            }
            data[i] = type + path; 
        }
        String base = getResourcePrefix(testName, resources);
        try {
            RemoteFileTestBase.createDirStructure(execEnv, base, data);
            String tmpDirParent = PathUtilities.getDirName(tmpDir);
            FileObject tmpDirParentFO = FileSystemProvider.getFileObject(execEnv, tmpDirParent); 
            if (tmpDirParentFO == null) {
                throw new IOException("Null file object for " + tmpDirParent);
            }
            //FileObject tmpDirFO = FileSystemProvider.getFileObject(execEnv, tmpDir);
            tmpDirParentFO.refresh();
            
            tmpDirParentFO = FileSystemProvider.getFileObject(execEnv, tmpDir);
            if (tmpDirParentFO == null) {
                throw new IOException("Null file object for " + tmpDir);
            }
            tmpDirParentFO.refresh();

            tmpDirParentFO = FileSystemProvider.getFileObject(execEnv, base);
            if (tmpDirParentFO == null) {
                throw new IOException("Null file object for " + base);
            }
            tmpDirParentFO.refresh();
            
        } catch (Throwable thr) {
            throw new IOException(thr.getMessage(), thr);
        }
    }    
}
