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
package org.netbeans.modules.subversion.remote;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import junit.framework.Test;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.test.ClassForAllEnvironments;
import org.netbeans.modules.nativeexecution.test.NativeExecutionBaseTestCase;
import org.netbeans.modules.nativeexecution.test.NativeExecutionTestSupport;
import org.netbeans.modules.remote.impl.fs.RemoteFileTestBase;
import org.netbeans.modules.remotefs.versioning.spi.FilesystemInterceptorProviderImpl;
import org.netbeans.modules.remotefs.versioning.spi.VersioningAnnotationProviderImpl;
import org.netbeans.modules.subversion.remote.api.ISVNStatus;
import org.netbeans.modules.subversion.remote.api.SVNClientException;
import org.netbeans.modules.subversion.remote.api.SVNStatusKind;
import org.netbeans.modules.subversion.remote.api.SVNUrl;
import org.netbeans.modules.subversion.remote.client.SvnClient;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.subversion.remote.utils.TestUtilities;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.api.VersioningSupport;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.modules.Places;
import org.openide.util.Exceptions;

/**
 *
 * 
 */
public abstract class RemoteVersioningTestBase extends RemoteFileTestBase {
    public static final String PROVIDED_EXTENSIONS_REMOTE_LOCATION = "ProvidedExtensions.RemoteLocation";
   
    protected VCSFileProxy dataRootDir;
    protected FileStatusCache cache;
    protected SVNUrl repoUrl;
    protected VCSFileProxy wc;
    protected VCSFileProxy wc2;
    protected VCSFileProxy repoDir;
    protected VCSFileProxy repo2Dir;
    protected SVNUrl repo2Url;
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
        FileObject svn = rootFO.getFileObject("/usr/bin/svn");
        if (svn == null || !svn.isValid()) {
            skipTest = true;
            return;
        }
        version = new Version(execEnv, svn);
        if (version.compareTo(new Version(1,5,0)) < 0) {
            System.err.println("Usupported svn version "+version);
            skipTest = true;
            return;
        } else {
            System.err.println("svn version "+version);
        }
        
        File cacheFile = Places.getCacheSubdirectory("svnremotecache");
        File[] listFiles = cacheFile.listFiles();
        if (listFiles != null) {
            for(File f : listFiles) {
                f.delete();
            }
        }
        MockServices.setServices(new Class[] {VersioningAnnotationProviderImpl.class, SubversionVCS.class, FilesystemInterceptorProviderImpl.class});
        // create temporary folder
        String remoteDir = mkTempAndRefreshParent(true);
        ProcessUtils.execute(execEnv, "umask", "0002");
        FileObject remoteDirFO = rootFO.getFileObject(remoteDir);
        assertNotNull("Failed to find file object for a directory that was just created " + remoteDir, remoteDirFO);
        remoteDirFO = remoteDirFO.createFolder("remoteSubversion");
        remoteDir = remoteDirFO.getPath();
        //
        dataRootDir = VCSFileProxy.createFileProxy(remoteDirFO);
        testName = getName();
        testName = testName.substring(0, testName.indexOf('[')).trim();
        //VersioningSupport.refreshFor(new VCSFileProxy[]{dataRootDir});
        wc = VCSFileProxy.createFileProxy(dataRootDir, testName + "_wc");
        wc2 = VCSFileProxy.createFileProxy(dataRootDir, testName + "_wc2");
        repoDir = VCSFileProxy.createFileProxy(dataRootDir, "repo");
        String repoPath = repoDir.getPath();
        if(repoPath.startsWith("/")) {
            repoPath = repoPath.substring(1, repoPath.length());
        }
        repoUrl = new SVNUrl("file:///" + repoPath);
        
        repo2Dir = VCSFileProxy.createFileProxy(dataRootDir, "repo2");
        repo2Url = new SVNUrl(TestUtilities.formatFileURL(repo2Dir));

        //System.setProperty("netbeans.user", remoteDir + "/userdir");
        cache = Subversion.getInstance().getStatusCache();
        cache.cleanUp();
        
        cleanUpWC(wc);
        cleanUpWC(wc2);
        initRepo();      
        
        VCSFileProxySupport.mkdirs(wc);
        VCSFileProxySupport.mkdirs(wc2);
        VersioningSupport.refreshFor(new VCSFileProxy[]{dataRootDir});
        svnimport();                   
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        if (skipTest) {
            return;
        }
        VCSFileProxySupport.delete(dataRootDir.getParentFile());
    }

    @Override
    protected Level logLevel() {
        return Level.FINE;
    }

    protected void commit(VCSFileProxy folder) throws SVNClientException {
        TestKit.commit(folder);
    }

    protected void add(VCSFileProxy file) throws SVNClientException {
        TestKit.add(file);
    }
    
    protected void cleanUpWC(VCSFileProxy wc) throws IOException {
        VCSFileProxySupport.delete(wc);
//        if(wc.exists()) {
//            VCSFileProxy[] files = wc.listFiles();
//            if(files != null) {
//                for (VCSFileProxy file : files) {
//                    if(!file.getName().equals("cache")) { // do not delete the cache
//                        FileObject fo = file.toFileObject();
//                        if (fo != null) {
//                            fo.delete();
//                        }
//                    }
//                }
//            }
//        }
    }

    protected void assertStatus(SVNStatusKind status, VCSFileProxy wc) throws SVNClientException {
        ISVNStatus[] values = getClient().getStatus(new VCSFileProxy[]{wc});
        for (ISVNStatus iSVNStatus : values) {
            assertEquals(status, iSVNStatus.getTextStatus());
        }
    }
 
    protected ISVNStatus getSVNStatus(VCSFileProxy file) throws SVNClientException {
        return TestKit.getSVNStatus(file);
    }
    
    protected SvnClient getClient() throws SVNClientException  {
        return TestKit.getClient(VCSFileProxy.createFileProxy(fs.getRoot()));
    }   
    
    protected void assertCachedStatus(VCSFileProxy file, int expectedStatus) throws Exception {
        assert !file.isFile() || expectedStatus != FileInformation.STATUS_VERSIONED_UPTODATE : "doesn't work for dirs with FileInformation.STATUS_VERSIONED_UPTODATE. Use getStatus instead";
        int status = getCachedStatus(file, expectedStatus);
        assertEquals(expectedStatus, status);
    }        

    protected int getCachedStatus(VCSFileProxy file, int exceptedStatus) throws Exception, InterruptedException {
        FileInformation info = null;
        for (int i = 0; i < 200; i++) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                throw ex;
            }
            info = cache.getCachedStatus(file);
            if (info != null && info.getStatus() == exceptedStatus) {
                break;
            }            
        }
        if (info == null) {
            throw new Exception("Cache timeout!");
        }
        return info.getStatus();
    }
    
    protected int getStatus(VCSFileProxy file) {
        return cache.refresh(file, FileStatusCache.REPOSITORY_STATUS_UNKNOWN).getStatus();
    }
    
    protected void initRepo() throws MalformedURLException, IOException, InterruptedException, SVNClientException {        
        TestKit.initRepo(repoDir, wc);
        TestKit.initRepo(repo2Dir, wc);
    }
    
    protected void svnimport() throws SVNClientException, MalformedURLException {
        TestKit.svnimport(repoDir, wc);
        TestKit.svnimport(repo2Dir, wc2);
    }        
    
    protected void delete(VCSFileProxy file) throws IOException {
        DataObject dao = DataObject.find(file.toFileObject());    
        dao.delete();   
    }   
    
    protected void waitALittleBit(long t) {
        try {
            Thread.sleep(t);  
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    protected void renameDO(VCSFileProxy from, VCSFileProxy to) throws DataObjectNotFoundException, IOException {
        DataObject daoFrom = DataObject.find(from.toFileObject());                
        daoFrom.rename(to.getName());               
    }
    
    protected void renameFO(VCSFileProxy from, VCSFileProxy to) throws DataObjectNotFoundException, IOException {
        // ensure parent is known by filesystems
        // otherwise no event will be thrown
        FileObject parent = from.getParentFile().toFileObject();

        FileObject foFrom = from.toFileObject();
        FileLock lock = foFrom.lock();
        try {
            foFrom.rename(lock, to.getName(), null);
        } finally {
            lock.releaseLock();
        }
    }
    
    protected void moveDO(VCSFileProxy from, VCSFileProxy to) throws DataObjectNotFoundException, IOException {
        DataObject daoFrom = DataObject.find(from.toFileObject());    
        DataObject daoTarget = DataObject.find(to.getParentFile().toFileObject());    
        daoFrom.move((DataFolder) daoTarget);    
    }

    protected void copyDO(VCSFileProxy from, VCSFileProxy to) throws DataObjectNotFoundException, IOException {
        DataObject daoFrom = DataObject.find(from.toFileObject());
        DataObject daoTarget = DataObject.find(to.getParentFile().toFileObject());
        daoFrom.copy((DataFolder) daoTarget);
    }
    
    protected void moveFO(VCSFileProxy from, VCSFileProxy to) throws DataObjectNotFoundException, IOException {
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

    protected void copyFO(VCSFileProxy from, VCSFileProxy to) throws DataObjectNotFoundException, IOException {
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

    protected String getName(VCSFileProxy f) {
        String ret = f.getName();
        int idx = ret.lastIndexOf(".");
        return idx > -1 ? ret.substring(0, idx) : ret;
    }

    protected String getExt(VCSFileProxy f) {
        String ret = f.getName();
        int idx = ret.lastIndexOf(".");
        return idx > -1 ? ret.substring(idx) : null;
    }

    protected final class SVNInterceptor extends Handler {
        @Override
        public void publish(LogRecord rec) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        @Override
        public void flush() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void close() throws SecurityException {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}
