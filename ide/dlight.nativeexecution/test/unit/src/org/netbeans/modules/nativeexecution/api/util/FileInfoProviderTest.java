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

package org.netbeans.modules.nativeexecution.api.util;

import java.io.File;
import java.util.Date;
import java.util.concurrent.Future;
import junit.framework.Test;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.FileInfoProvider.StatInfo;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;
import org.netbeans.modules.nativeexecution.test.NativeExecutionBaseTestCase;
import org.netbeans.modules.nativeexecution.test.NativeExecutionBaseTestSuite;
import org.openide.util.Exceptions;

/**
 *
 * @author Vladimir Kvashin
 */
public class FileInfoProviderTest extends NativeExecutionBaseTestCase {

    private String remoteTmpDir;
    private String remoteFile;
    private String remoteLink;
    private String remoteSubdir;
    private String remoteSubdirWithSpace;
    private String remoteSubdirLink;
    private Date creationDate;
    
    public FileInfoProviderTest(String name, ExecutionEnvironment testExecutionEnvironment) {
        super(name, testExecutionEnvironment);
    }

    @SuppressWarnings("unchecked")
    public static Test suite() {
        return new NativeExecutionBaseTestSuite(FileInfoProviderTest.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ExecutionEnvironment env = getTestExecutionEnvironment();
        ConnectionManager.getInstance().connectTo(env);
        clearRemoteTmpDir();
        remoteTmpDir = createRemoteTmpDir();
        File localFile = File.createTempFile("test_stat_", ".dat");
        File dirNameFile = File.createTempFile("test dir with a space", ".dat");
        localFile.delete();
        dirNameFile.delete();
        remoteFile = remoteTmpDir + "/" + localFile.getName();
        remoteLink = remoteFile + ".link";
        remoteSubdir = remoteFile + ".subdir";
        remoteSubdirWithSpace = remoteTmpDir + "/" + dirNameFile.getName();
        remoteSubdirLink = remoteFile + ".subdir.link";                
        String script = 
                "echo 123 > " + remoteFile + ";" +
                "ln -s " + remoteFile + ' ' + remoteLink + ";"  +
                " mkdir -p " + remoteSubdir + ";" + 
                " mkdir -p \"" + remoteSubdirWithSpace + "\";" + 
                "ln -s " + remoteSubdir + ' ' + remoteSubdirLink;
        runScript(script);
        creationDate = new Date();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        clearRemoteTmpDir();
    }
        
    @ForAllEnvironments(section = "remote.platforms")
    public void testStat() throws Exception {
        StatInfo statInfo;

        statInfo = getStatInfo(remoteFile);
        System.err.printf("Stat for %s: %s\n", remoteFile, statInfo);
        assertExpected(statInfo, remoteFile, false, null);
        
        statInfo = getStatInfo(remoteLink);
        System.err.printf("Stat for %s: %s\n", remoteLink, statInfo);
        assertExpected(statInfo, remoteLink, false, remoteFile);

        statInfo = getStatInfo(remoteSubdir);
        System.err.printf("Stat for %s: %s\n", remoteSubdir, statInfo);
        assertExpected(statInfo, remoteSubdir, true, null);
        
        statInfo = getStatInfo(remoteSubdirWithSpace);
        System.err.printf("Stat for %s: %s\n", remoteSubdirWithSpace, statInfo);
        assertExpected(statInfo, remoteSubdirWithSpace, true, null);
        
        statInfo = getStatInfo(remoteSubdirLink);
        System.err.printf("Stat for %s: %s\n", remoteSubdirLink, statInfo);
        assertExpected(statInfo, remoteSubdirLink, false, remoteSubdir);
    }
    
    @ForAllEnvironments(section = "remote.platforms")
    public void testAccessMode() throws Exception {
        ExecutionEnvironment env = getTestExecutionEnvironment();
        
        checkAccess(remoteFile, "700", env);
        checkAccess(remoteFile, "400", env);
        checkAccess(remoteFile, "200", env);
        checkAccess(remoteFile, "100", env);
               
        checkAccess(remoteFile, "007", env);
        checkAccess(remoteFile, "004", env);
        checkAccess(remoteFile, "002", env);
        checkAccess(remoteFile, "001", env);
        
        checkAccess(remoteFile, "070", env);
        checkAccess(remoteFile, "040", env);
        checkAccess(remoteFile, "020", env);
        checkAccess(remoteFile, "010", env);
        
//        String oldGroup = HostInfoUtils.getHostInfo(env).getGroup();
//        boolean groupChanged = false;
//        try {
//            try {
//               runScript("chgrp nobody " + remoteFile);
//               groupChanged = true;
//            } catch (Throwable thr) {
//                thr.printStackTrace();
//            }
//            if (groupChanged) {
//                checkAccess(remoteFile, "070", env);
//                checkAccess(remoteFile, "040", env);
//                checkAccess(remoteFile, "020", env);
//                checkAccess(remoteFile, "010", env);
//            }
//        } finally {
//            if (groupChanged) {
//                runScript("chgrp " + oldGroup + ' ' + remoteFile);
//            }
//        }
        
        // TODO: test (other) groups
        checkAccess("/usr/include", null, env);
        checkAccess("/etc/shadow", null, env);
    }

    private static void doTestExternalForm(StatInfo statInfo1, ExecutionEnvironment env) throws Exception {    
        String extForm = statInfo1.toExternalForm();
        StatInfo statInfo2 = StatInfo.fromExternalForm(extForm);
        assertEquals("getName()", statInfo1.getName(), statInfo2.getName());
        assertEquals("getAccess()", statInfo1.getAccess(), statInfo2.getAccess());
        assertEquals("getGropupId()", statInfo1.getGropupId(), statInfo2.getGropupId());
        assertEquals("getLastModified()", statInfo1.getLastModified(), statInfo2.getLastModified());
        assertEquals("getLinkTarget()", statInfo1.getLinkTarget(), statInfo2.getLinkTarget());
        assertEquals("getUserId()", statInfo1.getUserId(), statInfo2.getUserId());        
        assertEquals("isDirectory()", statInfo1.isDirectory(), statInfo2.isDirectory());
        assertEquals("isLink()", statInfo1.isLink(), statInfo2.isLink());        
        assertEquals("canRead()", statInfo1.canRead(env), statInfo2.canRead(env));
        assertEquals("canWrite()", statInfo1.canWrite(env), statInfo2.canWrite(env));
        assertEquals("canExecute()", statInfo1.canExecute(env), statInfo2.canExecute(env));
    }
    
    @ForAllEnvironments(section = "remote.platforms")
    public void testExternalForm() throws Exception {
        doTestExternalForm(getStatInfo(remoteLink), getTestExecutionEnvironment());
        doTestExternalForm(getStatInfo(remoteFile), getTestExecutionEnvironment());
    }
    
    private void checkAccess(String path, String chmod, ExecutionEnvironment env) throws Exception {
        if (chmod != null) {
            runScript("chmod " + chmod + ' ' + path);
        }
        boolean read = canRead(env, path);
        boolean write = canWrite(env, path);
        boolean execute = canExecute(env, path);
        StatInfo statInfo = getStatInfo(path);
        checkAccess("canRead", path, env, read, statInfo.canRead(env));        
        checkAccess("canWrite", path, env, write, statInfo.canWrite(env));        
        checkAccess("canExecute", path, env, execute, statInfo.canExecute(env));
    }
    
    private void checkAccess(String prefix, String path, ExecutionEnvironment env, boolean expected, boolean actual) throws Exception {
        if (expected != actual) {
            StringBuilder sb = new StringBuilder(prefix).append(" differs for ").append(path).append(": ");
            sb.append("expected ").append(expected).append(" but was ").append(actual).append('\n');
            try {
                sb.append(runScript(env, "id; ls -ld " + path));
            } catch (Throwable ex) {
                Exceptions.printStackTrace(ex);
            }
            assertTrue(sb.toString(), false);
        }
    }            

    @ForAllEnvironments(section = "remote.platforms")
    public void testLs() throws Exception {
        StatInfo[] res = getLs(remoteTmpDir);
        System.err.printf("LS for %s\n", remoteTmpDir);
        for (StatInfo info : res) {                        
            System.err.printf("\t%s\n", info);
        }
        StatInfo statInfo;        
        statInfo = find(res, getBaseName(remoteFile));
        assertExpected(statInfo, remoteFile, false, null);
        
        statInfo = find(res, getBaseName(remoteLink));
        assertExpected(statInfo, remoteLink, false, remoteFile);

        statInfo = find(res, getBaseName(remoteSubdir));
        assertExpected(statInfo, remoteSubdir, true, null);
        
        statInfo = find(res, getBaseName(remoteSubdirWithSpace));
        System.err.printf("Stat for %s: %s\n", remoteSubdirWithSpace, statInfo);
        assertExpected(statInfo, remoteSubdirWithSpace, true, null);
        
        statInfo = find(res, getBaseName(remoteSubdirLink));
        assertExpected(statInfo, remoteSubdirLink, false, remoteSubdir);
    }
    // copied from org.netbeans.modules.dlight.libs.common.PathUtilities
    private static String getBaseName(String path) {
        if (path.length()>0 && (path.charAt(path.length()-1) == '\\' || path.charAt(path.length()-1) == '/')) {
            path = path.substring(0,path.length()-1);
        }
        int sep = path.lastIndexOf('/');
        if (sep == -1) {
            sep = path.lastIndexOf('\\');
        }
        if (sep != -1) {
            return path.substring(sep + 1);
        }
        return path;
    }
    
    private StatInfo find(StatInfo[] infList, String name) throws Exception {
        for (StatInfo info : infList) {
            if (info.getName().equals(name)) {
                return info;
            }
        }
        assertTrue("can not found in ls info: " + name, false);
        return null;
    }
    
    private StatInfo[] getLs(String path) throws Exception {
        Future<StatInfo[]> res = FileInfoProvider.ls(getTestExecutionEnvironment(), path);
        assertNotNull(res);
        StatInfo[] info = res.get();
        assertNotNull("ls returned null", info);
        assertTrue("ls returned empty array", info.length > 0);
        return info;
        
    }

    private StatInfo getStatInfo(String path) throws Exception {
        Future<StatInfo> res = FileInfoProvider.lstat(getTestExecutionEnvironment(), path);
        assertNotNull(res);
        StatInfo statInfo = res.get();
        assertNotNull("stat returned null", statInfo);
        return statInfo;
    }
        
    private void assertExpected(StatInfo statInfo, String path, boolean dir, String link) throws Exception {
        int slashPos = path.lastIndexOf('/');
        String name = slashPos < 0 ? path : path.substring(slashPos + 1);
        assertEquals("name for " + path, name, statInfo.getName());
        assertEquals("isLink() for " + path, (link != null), statInfo.isLink());
        assertEquals("isDirectory() for " + path, dir, statInfo.isDirectory());
        if (link != null) {
            assertEquals("link target for " + path, link, statInfo.getLinkTarget());
        }
        long skew = HostInfoUtils.getHostInfo(getTestExecutionEnvironment()).getClockSkew();
        Date lastMod = statInfo.getLastModified();
        long delta = Math.abs(creationDate.getTime() - lastMod.getTime());
        if (delta > Math.abs(skew) + (long)(1000*60*15)) {
            assertTrue("last modified differs too much: " + creationDate +  " vs " + lastMod + 
                    " delta " + delta + " ms; skew " + skew, false);
        }
    }
}
