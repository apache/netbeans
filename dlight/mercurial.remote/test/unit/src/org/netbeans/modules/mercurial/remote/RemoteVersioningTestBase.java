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
package org.netbeans.modules.mercurial.remote;

import java.beans.PropertyChangeEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import junit.framework.Test;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.mercurial.remote.util.HgCommand;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.test.ClassForAllEnvironments;
import org.netbeans.modules.nativeexecution.test.NativeExecutionBaseTestCase;
import org.netbeans.modules.nativeexecution.test.NativeExecutionTestSupport;
import org.netbeans.modules.remote.impl.fs.RemoteFileTestBase;
import org.netbeans.modules.remotefs.versioning.spi.FilesystemInterceptorProviderImpl;
import org.netbeans.modules.remotefs.versioning.spi.VersioningAnnotationProviderImpl;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.versioning.core.VersioningManager;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.api.VersioningSupport;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

/**
 *
 * 
 */
public abstract class RemoteVersioningTestBase extends RemoteFileTestBase {
    public static final String PROVIDED_EXTENSIONS_REMOTE_LOCATION = "ProvidedExtensions.RemoteLocation";
   
    protected VCSFileProxy dataRootDir;
    protected FileStatusCache cache;
    private boolean skipTest = false;
    protected String testName;
    protected Version version;
    
    public RemoteVersioningTestBase(String testName, ExecutionEnvironment execEnv) {
        super(testName, execEnv);
    }
    
    protected static final void addTest(NbTestSuite suite, Class<? extends NativeExecutionBaseTestCase> testClass, String testName)  {
        try {
            Method test = testClass.getDeclaredMethod(testName);
            if (test == null) {
                System.err.println("Not found test "+testClass.getName()+"."+testName);
                return;
            }
            ClassForAllEnvironments forAllEnvAnnotation = testClass.getAnnotation(ClassForAllEnvironments.class);
            String envSection = forAllEnvAnnotation.section();
            if (envSection == null || envSection.length() == 0) {
                envSection = "remote.platforms";
            }
            Constructor forAllEnvConstructor = null;
            for(Constructor constructor : testClass.getConstructors()) {
                Class[] parameterTypes = constructor.getParameterTypes();
                if (parameterTypes.length == 2 && 
                    parameterTypes[0].equals(String.class) &&
                    parameterTypes[1].equals(ExecutionEnvironment.class)) {
                    forAllEnvConstructor = constructor;
                }
            }
            if (forAllEnvConstructor==null) {
                System.err.println("Not found constructor "+testClass.getName()+"(String, ExecutionEnvironment)");
            }
            String[] platforms = NativeExecutionTestSupport.getPlatforms(envSection, suite);
            for (String platform : platforms) {
                suite.addTest((Test) forAllEnvConstructor.newInstance(testName, NativeExecutionTestSupport.getTestExecutionEnvironment(platform)));
            }
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        } catch (InstantiationException ex) {
            ex.printStackTrace(System.err);
        } catch (IllegalAccessException ex) {
            ex.printStackTrace(System.err);
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace(System.err);
        } catch (InvocationTargetException ex) {
            ex.printStackTrace(System.err);
        } catch (NoSuchMethodException ex) {
            ex.printStackTrace(System.err);
        } catch (SecurityException ex) {
            ex.printStackTrace(System.err);
        }
    }
    
    protected boolean skipTest() {
        return skipTest;
    }
    
    @Override
    protected void setUp() throws Exception {          
        super.setUp();
        final String hgPath = "/usr/bin/hg";
        FileObject hg = rootFO.getFileObject(hgPath);
        if (hg == null || !hg.isValid()) {
            skipTest = true;
            return;
        }
        version = new Version(execEnv, hg);
        if (version.compareTo(new Version(1,4,0)) < 0) {
            System.err.println("Usupported hg version "+version);
            skipTest = true;
            return;
        } else {
            System.err.println("hg version "+version);
        }
        
        MockServices.setServices(new Class[] {VersioningAnnotationProviderImpl.class, MercurialVCS.class, FilesystemInterceptorProviderImpl.class});
        // create temporary folder
        String remoteDir = mkTempAndRefreshParent(true);
        ProcessUtils.execute(execEnv, "umask", "0002");
        FileObject remoteDirFO = rootFO.getFileObject(remoteDir);
        assertNotNull("Failed to find file object for a directory that was just created " + remoteDir, remoteDirFO);
        remoteDirFO = remoteDirFO.createFolder("remoteMercurial");
        remoteDir = remoteDirFO.getPath();
        //
        dataRootDir = VCSFileProxy.createFileProxy(remoteDirFO);
        testName = getName();
        testName = testName.substring(0, testName.indexOf('[')).trim();
        VersioningSupport.refreshFor(new VCSFileProxy[]{dataRootDir});
        
        Logger.getLogger("").addHandler(versionCheckBlocker);
        
        try {
            assertTrue(VCSFileProxySupport.mkdirs(dataRootDir));
            HgCommand.doCreate(getWorkTreeDir(), null);
            VCSFileProxySupport.createNew(VCSFileProxy.createFileProxy(getWorkTreeDir(), "empty"));
        } catch (IOException iOException) {
            throw iOException;
        } catch (HgException hgException) {
        }
        cache = Mercurial.getInstance().getFileStatusCache();
        VersioningManager.getInstance().propertyChange(new PropertyChangeEvent(this, VersioningManager.EVENT_VERSIONED_ROOTS, null, null));
        
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        if (skipTest) {
            return;
        }
        VCSFileProxySupport.deleteExternally(dataRootDir.getParentFile());
    }

    protected static final OutputLogger NULL_LOGGER = Mercurial.getInstance().getLogger(null);

    public FileStatusCache getCache() {
        return cache;
    }

    @Override
    protected Level logLevel() {
        return Level.FINE;
    }

    protected VCSFileProxy getWorkTreeDir () throws IOException {
        return dataRootDir;
    }
    
    protected void commit(VCSFileProxy... files) throws HgException, IOException {
        commitIntoRepository(getWorkTreeDir(), files);
    }

    protected void commitIntoRepository (VCSFileProxy repository, VCSFileProxy... files) throws HgException, IOException {

        List<VCSFileProxy> filesToAdd = new ArrayList<>();
        FileInformation status;
        for (VCSFileProxy file : files) {
            if(findStatus(HgCommand.getStatus(repository, Collections.singletonList(file), null, null),
                    FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY)) {
                filesToAdd.add(file);
            }
        }

        HgCommand.doAdd(repository, filesToAdd, null);
        List<VCSFileProxy> filesToCommit = new ArrayList<>();
        for (VCSFileProxy file : files) {
            if(file.isFile()) {
                filesToCommit.add(file);
            }
        }

        HgCommand.doCommit(repository, filesToCommit, "commit", null);
        for (VCSFileProxy file : filesToCommit) {
            assertStatus(file, FileInformation.STATUS_VERSIONED_UPTODATE);
        }
    }

    protected VCSFileProxy clone(VCSFileProxy file) throws HgException, IOException {
        VCSFileProxy path = VCSFileProxySupport.getResource(file, file.getPath() + "_cloned");
        HgCommand.doClone(getWorkTreeDir(),path, null);
        return path;
    }
    
    protected  void assertStatus(VCSFileProxy f, int status) throws HgException, IOException {
        FileInformation s = HgCommand.getStatus(getWorkTreeDir(), Collections.singletonList(f), null, null).get(f);
        if (status == FileInformation.STATUS_VERSIONED_UPTODATE) {
            assertEquals(s, null);
        } else {
            assertEquals(status, s.getStatus());
        }
    }        
    
    protected void assertCacheStatus(VCSFileProxy f, int status) throws HgException, IOException {
        assertEquals(status, cache.getStatus(f).getStatus());
    }

    protected VCSFileProxy createFolder(String name) throws IOException {
        FileObject wd = getWorkTreeDir().toFileObject();
        FileObject folder = wd.createFolder(name);        
        return VCSFileProxy.createFileProxy(folder);
    }
    
    protected VCSFileProxy createFolder(VCSFileProxy parent, String name) throws IOException {
        FileObject parentFO = parent.toFileObject();
        FileObject folder = parentFO.createFolder(name);                
        return VCSFileProxy.createFileProxy(folder);
    }
    
    protected VCSFileProxy createFile(VCSFileProxy parent, String name) throws IOException {
        FileObject parentFO = parent.toFileObject();
        FileObject fo = parentFO.createData(name);
        return VCSFileProxy.createFileProxy(fo);
    }
    
    protected VCSFileProxy createFile(String name) throws IOException {
        FileObject wd = getWorkTreeDir().toFileObject();
        FileObject fo = wd.createData(name);
        return VCSFileProxy.createFileProxy(fo);
    }

    protected void write(VCSFileProxy file, String str) throws IOException {
        OutputStreamWriter w = null; 
        try {
            VCSFileProxy parent = file.getParentFile();
            if(parent!=null && !parent.exists()) {
                VCSFileProxySupport.mkdirs(parent);
            }
            w = new OutputStreamWriter(VCSFileProxySupport.getOutputStream(file));
            w.write(str);
            w.flush();
        } finally {
            if (w != null) {
                w.close();
            }
        }
    }

    protected String read (VCSFileProxy file) throws IOException {
        BufferedReader r = null;
        try {
            StringBuilder sb = new StringBuilder();
            r = new BufferedReader(new InputStreamReader(file.getInputStream(false), "UTF-8"));
            for (String line = r.readLine(); line != null; line = r.readLine()) {
                if (sb.length() > 0) {
                    sb.append('\n');
                }
                sb.append(line);
            }
            return sb.toString();
        } finally {
            if (r != null) {
                r.close();
            }
        }
    }

    private boolean findStatus(Map<VCSFileProxy, FileInformation> statuses, int status) {
        for (Map.Entry<VCSFileProxy, FileInformation> e : statuses.entrySet()) {
            if (e.getValue().getStatus() == status) {
                return true;
            }
        }
        return false;
    }
    
    protected void moveDO (VCSFileProxy from, VCSFileProxy to) throws DataObjectNotFoundException, IOException {
        DataObject daoFrom = DataObject.find(from.toFileObject());
        DataObject daoTarget = DataObject.find(to.getParentFile().toFileObject());
        daoFrom.move((DataFolder) daoTarget);
    }

    protected void moveFO (VCSFileProxy from, VCSFileProxy to) throws DataObjectNotFoundException, IOException {
        FileObject foFrom = from.toFileObject();
        assertNotNull(foFrom);
        FileObject foTarget = to.getParentFile().toFileObject();
        assertNotNull(foTarget);
        FileLock lock = foFrom.lock();
        try {
            foFrom.move(lock, foTarget, to.getName(), null);
        } finally {
            lock.releaseLock();
        }
    }

    protected void deleteDO (VCSFileProxy toDelete) throws DataObjectNotFoundException, IOException {
        DataObject dao = DataObject.find(toDelete.toFileObject());
        dao.delete();
    }

    protected void deleteFO (VCSFileProxy toDelete) throws DataObjectNotFoundException, IOException {
        FileObject fo = toDelete.toFileObject();
        assertNotNull(fo);
        FileLock lock = fo.lock();
        try {
            fo.delete(lock);
        } finally {
            lock.releaseLock();
        }
    }
    
    protected void renameDO (VCSFileProxy from, String newName) throws DataObjectNotFoundException, IOException {
        DataObject daoFrom = DataObject.find(from.toFileObject());
        daoFrom.rename(newName);
    }

    protected void renameFO (VCSFileProxy from, String newName) throws DataObjectNotFoundException, IOException {
        // need to let FS know about it
        FileObject parent = from.getParentFile().toFileObject();
        FileObject foFrom = from.toFileObject();
        assertNotNull(foFrom);
        FileLock lock = foFrom.lock();
        try {
            foFrom.rename(lock, newName, null);
        } finally {
            lock.releaseLock();
        }
    }

    protected void copyDO (VCSFileProxy from, VCSFileProxy to) throws DataObjectNotFoundException, IOException {
        DataObject daoFrom = DataObject.find(from.toFileObject());
        DataObject daoTarget = DataObject.find(to.getParentFile().toFileObject());
        daoFrom.copy((DataFolder) daoTarget);
    }

    protected void copyFO (VCSFileProxy from, VCSFileProxy to) throws DataObjectNotFoundException, IOException {
        FileObject foFrom = from.toFileObject();
        assertNotNull(foFrom);
        FileObject foTarget = to.getParentFile().toFileObject();
        assertNotNull(foTarget);
        FileLock lock = foFrom.lock();
        try {
            foFrom.copy(foTarget, getName(to), getExt(to));
        } finally {
            lock.releaseLock();
        }
    }
    
    private String getName(VCSFileProxy f) {
        String ret = f.getName();
        int idx = ret.lastIndexOf(".");
        return idx > -1 ? ret.substring(0, idx) : ret;
    }

    private String getExt(VCSFileProxy f) {
        String ret = f.getName();
        int idx = ret.lastIndexOf(".");
        return idx > -1 ? ret.substring(idx) : null;
    }

    protected FileInformation getCachedStatus (VCSFileProxy file, int expectedStatus) throws InterruptedException {
        for (int i = 0; i < 20; ++i) {
            FileInformation info = getCache().getCachedStatus(file);
            if ((info.getStatus() & expectedStatus) != 0) {
                return info;
            }
            Thread.sleep(1000);
        }
        fail("Status " + expectedStatus + " expected for " + file);
        return null;
    }

    protected VCSFileProxy[] prepareTree (VCSFileProxy folder, VCSFileProxy copy) throws IOException {
        createFile(folder, "file1");
        createFile(folder, "file2");
        VCSFileProxy subfolder1 = createFolder(folder, "subfolder1");
        createFile(subfolder1, "file1");
        createFile(subfolder1, "file2");
        VCSFileProxy subfolder1_1 = createFolder(subfolder1, "subfolder1_1");
        createFile(subfolder1_1, "file1");
        createFile(subfolder1_1, "file2");
        VCSFileProxy subfolder1_2 = createFolder(subfolder1, "subfolder1_2");
        createFile(subfolder1_2, "file1");
        createFile(subfolder1_2, "file2");
        VCSFileProxy subfolder2 = createFolder(folder, "subfolder2");
        createFile(subfolder2, "file1");
        createFile(subfolder2, "file2");
        VCSFileProxy subfolder2_1 = createFolder(subfolder2, "subfolder2_1");
        createFile(subfolder2_1, "file1");
        createFile(subfolder2_1, "file2");
        VCSFileProxy subfolder2_2 = createFolder(subfolder2, "subfolder2_2");
        createFile(subfolder2_2, "file1");
        createFile(subfolder2_2, "file2");

        return new VCSFileProxy[] { 
            VCSFileProxy.createFileProxy(copy, "file1"), 
            VCSFileProxy.createFileProxy(copy, "file2"), 
            VCSFileProxy.createFileProxy(VCSFileProxy.createFileProxy(copy, "subfolder1"), "file1"), 
            VCSFileProxy.createFileProxy(VCSFileProxy.createFileProxy(copy, "subfolder1"), "file2"),
            VCSFileProxy.createFileProxy(VCSFileProxy.createFileProxy(copy, "subfolder2"), "file1"), 
            VCSFileProxy.createFileProxy(VCSFileProxy.createFileProxy(copy, "subfolder2"), "file2"),
            VCSFileProxy.createFileProxy(VCSFileProxy.createFileProxy(VCSFileProxy.createFileProxy(copy, "subfolder1"), "subfolder1_1"), "file1"), VCSFileProxy.createFileProxy(VCSFileProxy.createFileProxy(VCSFileProxy.createFileProxy(copy, "subfolder1"), "subfolder1_1"), "file2"),
            VCSFileProxy.createFileProxy(VCSFileProxy.createFileProxy(VCSFileProxy.createFileProxy(copy, "subfolder1"), "subfolder1_2"), "file1"), VCSFileProxy.createFileProxy(VCSFileProxy.createFileProxy(VCSFileProxy.createFileProxy(copy, "subfolder1"), "subfolder1_2"), "file2"),
            VCSFileProxy.createFileProxy(VCSFileProxy.createFileProxy(VCSFileProxy.createFileProxy(copy, "subfolder2"), "subfolder2_1"), "file1"), VCSFileProxy.createFileProxy(VCSFileProxy.createFileProxy(VCSFileProxy.createFileProxy(copy, "subfolder2"), "subfolder2_1"), "file2"),
            VCSFileProxy.createFileProxy(VCSFileProxy.createFileProxy(VCSFileProxy.createFileProxy(copy, "subfolder2"), "subfolder2_2"), "file1"), VCSFileProxy.createFileProxy(VCSFileProxy.createFileProxy(VCSFileProxy.createFileProxy(copy, "subfolder2"), "subfolder2_2"), "file2")
                    };

    }
        
    protected boolean isParentHasChild(VCSFileProxy toFolder) {
        final VCSFileProxy[] listFiles = toFolder.getParentFile().listFiles();
        if (listFiles != null) {
            for(VCSFileProxy child : listFiles) {
                if (child.getName().equals(toFolder.getName())) {
                    return true;
                }
            }
        }
        return false;
    }
    

    private static class VersionCheckBlocker extends Handler {
        boolean versionChecked = false;
        @Override
        public void publish(LogRecord record) {
            final String message = record.getMessage();
            if(message != null && message.indexOf("version: ") > -1) {
                versionChecked = true;                    
            }
        }
        @Override
        public void flush() { }
        @Override
        public void close() throws SecurityException { }        
    };
    private static VersionCheckBlocker versionCheckBlocker = new VersionCheckBlocker();
}
