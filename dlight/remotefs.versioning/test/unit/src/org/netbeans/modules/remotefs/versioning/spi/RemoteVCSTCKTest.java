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
package org.netbeans.modules.remotefs.versioning.spi;

import java.io.IOException;
import java.util.Collection;
import junit.framework.Test;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.dlight.libs.common.PathUtilities;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils.ExitStatus;
import org.netbeans.modules.nativeexecution.test.NativeExecutionTestSupport;
import org.netbeans.modules.nativeexecution.test.NbClustersInfoProvider;
import org.netbeans.modules.nativeexecution.test.RcFile;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.netbeans.modules.versioning.VCSAnnotationProviderTestCase;
import org.netbeans.modules.versioning.VCSFilesystemTestFactory;
import org.netbeans.modules.versioning.VCSInterceptorTestCase;
import org.netbeans.modules.versioning.VCSOwnerTestCase;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.test.MockLookup;

/**
 *
 */
public class RemoteVCSTCKTest extends VCSFilesystemTestFactory {
    static {
        // Setting netbeans.dirs makes installedFileLocator work properly
        // Needed for native execution...
        System.setProperty("netbeans.dirs", NbClustersInfoProvider.getClusters()); // NOI18N
        System.setProperty("remote.user.password.keep_in_memory", "true"); // NOI18N
        // Native file watcher is disabled
        System.setProperty("org.netbeans.modules.masterfs.watcher.disable", "true");
    }

    private ExecutionEnvironment execEnv = null;
    private String tmpDir;
    private FileObject root;

    public RemoteVCSTCKTest(Test test) {
        super(test);
    }

    @Override
    protected String getRootPath() throws IOException {
        return tmpDir;
    }

    @Override
    protected FileObject createFile(String path) throws IOException {
        return createFileObject(path, false);
    }

    @Override
    protected FileObject createFolder(String path) throws IOException {
        return createFileObject(path, true);
    }

    protected FileObject createFileObject(String path, boolean isDirectory) throws IOException {
        FileObject fo = FileSystemProvider.getFileObject(execEnv, getRootPath()+"/"+path);
        if (fo != null && fo.isValid()) {
            assertEquals(isDirectory, fo.isFolder());
            return fo;
        }
        if(isDirectory) {
            fo = FileUtil.createFolder(root, path);
        } else {
            fo = FileUtil.createData(root, path);
        }
        return fo;
    }

    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockLookup.setLayersAndInstances();
        System.setProperty("nativeexecution.mode.unittest", "true");
        System.setProperty("cnd.mode.unittest", "true");
        
        String userdir = System.getProperty("netbeans.user");
        if (userdir == null) {
            System.setProperty("netbeans.user", System.getProperty("nbjunit.workdir"));
        }
        RcFile rcFile = NativeExecutionTestSupport.getRcFile();
        String mspec = rcFile.get("remote", "vcstck.mspec");
        if (mspec == null) {
            for(String section : rcFile.getSections()) {
                if (section.equals("remote.platforms")) {
                    Collection<String> keys = rcFile.getKeys(section);
                    for(String key : keys) {
                        if (key.equals("intel-Linux")) {
                            String get = rcFile.get(section, key, null);
                            if (get == null) {
                                mspec = key;
                            }
                        }
                    }
                }
            }
        }
        
        execEnv = NativeExecutionTestSupport.getTestExecutionEnvironment(mspec);
        ConnectionManager.getInstance().connectTo(execEnv);
        //RemoteFileSystemManager.getInstance().resetFileSystem(execEnv);
        //RemoteFileSystem rfs = RemoteFileSystemManager.getInstance().getFileSystem(execEnv);
        tmpDir = mkTemp(execEnv, true);
        String tmpDirParent = PathUtilities.getDirName(tmpDir);
        FileObject tmpDirParentFO = FileSystemProvider.getFileObject(execEnv, tmpDirParent); 
        if (tmpDirParentFO == null) {
            throw new IOException("Null file object for " + tmpDirParent);
        }
        tmpDirParentFO.refresh();
        root = FileSystemProvider.getFileObject(execEnv, tmpDir);
        if (root == null) {
            throw new IOException("Null file object for " + tmpDir);
        }
        VCSFileProxy.createFileProxy(root); // init APIAccessor
    }
    
    private String mkTemp(ExecutionEnvironment execEnv, boolean directory) throws Exception {        
        String[] mkTempArgs;
        if (HostInfoUtils.getHostInfo(execEnv).getOSFamily() == HostInfo.OSFamily.MACOSX) {
            mkTempArgs = directory ? new String[] { "-t", "/tmp", "-d" } : new String[] { "-t", "/tmp" };
        } else {
            mkTempArgs = directory ? new String[] { "-d" } : new String[0];
        }        
        ProcessUtils.ExitStatus res = ProcessUtils.execute(execEnv, "mktemp", mkTempArgs);
        assertEquals("mktemp failed: " + res.getErrorString(), 0, res.exitCode);
        return res.getOutputString();
    }

    @Override
    public void move(String from, String to) throws IOException {
        ExitStatus res = ProcessUtils.executeInDir(getRootPath(), execEnv, "mv", from, to);
        if (res.exitCode != 0) {
            throw new IOException("mv failed: " + res.getErrorString());
        }
    }

    @Override
    public void copy(String from, String to) throws IOException {
        ExitStatus res = ProcessUtils.executeInDir(getRootPath(), execEnv, "cp", "-r", from, to);
        if (res.exitCode != 0) {
            throw new IOException("cp failed: " + res.getErrorString());
        }
    }

    @Override
    public void delete(String path) throws IOException {
        ExitStatus res = ProcessUtils.executeInDir(getRootPath(), execEnv, "rm", "-rf", getRootPath()+"/"+path);
        if (res.exitCode != 0) {
            throw new IOException("rm failed: " + res.getErrorString());
        }
    }

    @Override
    protected void setReadOnly(String path) throws IOException {
        ExitStatus res = ProcessUtils.executeInDir(getRootPath(), execEnv, "chmod", "444", getRootPath()+"/"+path);
        if (res.exitCode != 0) {
            throw new IOException("chmod failed: " + res.getErrorString());
        }
        FileObject fo = root.getFileObject(path);
        fo.getParent().refresh(true);
    }
    
    public static Test suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(VCSOwnerTestCase.class);
        suite.addTestSuite(VCSInterceptorTestCase.class);
        suite.addTestSuite(VCSAnnotationProviderTestCase.class);
        return new RemoteVCSTCKTest(suite);
    }
    
}
