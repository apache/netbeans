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
package org.netbeans.modules.nativeexecution;

import java.io.File;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import junit.framework.Test;
import org.netbeans.junit.diff.Diff;
import org.netbeans.modules.nativeexecution.test.NativeExecutionBaseTestCase;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;
import org.netbeans.modules.nativeexecution.test.NativeExecutionBaseTestSuite;
import org.netbeans.modules.nativeexecution.test.NativeExecutionTestSupport;
import org.netbeans.modules.nativeexecution.test.RcFile;

public class HostInfoTestCase extends NativeExecutionBaseTestCase {

    public HostInfoTestCase(String name) {
        super(name);
    }

    public HostInfoTestCase(String name, ExecutionEnvironment execEnv) {
        super(name, execEnv);
    }

    public static Test suite() {
        return new NativeExecutionBaseTestSuite(HostInfoTestCase.class);
    }

    @org.junit.Test
    public void testGetHostInfoLocal() throws Exception {
        HostInfo hi = HostInfoUtils.getHostInfo(ExecutionEnvironmentFactory.getLocal());
        assertNotNull(hi);
    }

    @org.junit.Test
    @ForAllEnvironments(section = "remote.platforms")
    public void testGetHostInfo() throws Exception {
        HostInfo hi = HostInfoUtils.getHostInfo(getTestExecutionEnvironment());
        assertNotNull(hi);
    }

    @org.junit.Test
    @ForAllEnvironments(section = "remote.platforms")
    public void testGetHostInfoEx() throws Exception {
        if (org.openide.util.Utilities.isWindows()) {
            return; // skip test for Windows
        }
        HostInfo hi = HostInfoUtils.getHostInfo(getTestExecutionEnvironment());
        assertNotNull(hi);
        int uid = hi.getUserId();
        int gid = hi.getGroupId();
        String group = hi.getGroup();
        int[] allGids = hi.getAllGroupIDs();
        
        String[] tmp = runScript("groups").trim().split(" +");
        Set<String> refGroups = new HashSet<>(Arrays.asList(tmp));
        Set<String> realGroups = new HashSet<>(Arrays.asList(hi.getAllGroups()));
        assertEquals("Groups names differ", refGroups, realGroups);

        assertEquals("Groups ids count differ", refGroups.size(), allGids.length);
        
        tmp = runScript("getent passwd " + getTestExecutionEnvironment().getUser()).split(":");
        // format is:
        // greys:x:1000:113:Gleb Reys,,,:/home/greys:/bin/bash
        int refUid = Integer.parseInt(tmp[2]);
        assertEquals("User IDs", refUid, uid);
        
        int refGid = Integer.parseInt(tmp[3]);
        assertEquals("Group IDs", refGid, gid);
        
        // TODO: test all group IDs
    }
        
    @org.junit.Test
    @ForAllEnvironments(section = "remote.platforms")
    public void testGetHostInfoEx2() throws Exception {
        ExecutionEnvironment execEnv = getTestExecutionEnvironment();
        HostInfo hi = HostInfoUtils.getHostInfo(execEnv);
        assertNotNull(hi);
        RcFile rcFile = NativeExecutionTestSupport.getRcFile();
        String mspec = NativeExecutionTestSupport.getMspec(execEnv);
        String section = "execution." + mspec + ".hostInfo";
        Map<String, String> expectedMap = new HashMap<>();
        Collection<String> keys = rcFile.getKeys(section);
        if (keys.isEmpty()) {
            return;
        }
        for (String key :  keys) {
            String value = rcFile.get(section, key);
            expectedMap.put(key, value);
        }
        Map<String, String> actualMap = new HashMap<>();
        actualMap.put("getCpuFamily", "" + hi.getCpuFamily());
        actualMap.put("getCpuNum", "" + hi.getCpuNum());
        actualMap.put("getGroup", "" + hi.getGroup());
        actualMap.put("getGroupId", "" + hi.getGroupId());
        actualMap.put("getHostname", "" + hi.getHostname());
        actualMap.put("getLoginShell", "" + hi.getLoginShell());        
        actualMap.put("getOS.getFamily", "" + hi.getOS().getFamily());
        actualMap.put("getOS.getName", "" + hi.getOS().getName());
        actualMap.put("getOS.getVersion", "" + hi.getOS().getVersion());
        actualMap.put("getOS.getBitness", "" + hi.getOS().getBitness());        
        actualMap.put("getOSFamily", "" + hi.getOSFamily());
        actualMap.put("getShell", "" + hi.getShell());
        actualMap.put("getUserDir", "" + hi.getUserDir());
        actualMap.put("getUserId", "" + hi.getUserId());
        for (Map.Entry<String, String> e : expectedMap.entrySet()) {
            String key = e.getKey();
            String expected = e.getValue().trim();
            String actual = actualMap.get(key);
            assertNotNull("Can not find " + key + " in hostinfo data", actual);
            if (! expected.equals(actual)) {
                printDiff(mspec, expectedMap, actualMap);
            }
            assertEquals(key, expected, actual);
        }
    }
    
    private void printDiff(String mspec, Map<String, String> expectedMap, Map<String, String> actualMap) throws Exception {
            File workDir = getWorkDir();
            String prefix = "hostinfo.";
            File expectedFile = new File(workDir, prefix + mspec + ".expected");
            File actualFile = new File(prefix + mspec + ".actual");
            File diffFile = new File(prefix + mspec + ".diff");
            printMap(actualMap, new PrintStream(actualFile));
            printMap(expectedMap, new PrintStream(expectedFile));
            Diff systemDiff = org.netbeans.junit.Manager.getSystemDiff();
            systemDiff.diff(actualFile, expectedFile, diffFile);
            printFile(expectedFile, "EXPECTED ", System.err);
            printFile(actualFile,   "ACTUAL   ", System.err);
            System.err.printf("See diff %s %s\n", actualFile.getAbsolutePath(), expectedFile.getAbsolutePath());
            printFile(diffFile, null, System.err);        
    }
    
    private void printMap(Map<String, String> map, PrintStream printStream) {
        SortedMap<String, String> sortedMap = new TreeMap<>(map);
        for (Map.Entry<String, String> entry : sortedMap.entrySet()) {
            printStream.printf("%s=%s\n", entry.getKey(), entry.getValue());
        }
    }
    
    @org.junit.Test
    @ForAllEnvironments(section = "remote.platforms")
    public void testGetOS() throws Exception {
        ExecutionEnvironment execEnv = getTestExecutionEnvironment();
        String mspec = NativeExecutionTestSupport.getMspec(execEnv);

        HostInfo hi = HostInfoUtils.getHostInfo(execEnv);
        assertNotNull(hi);

        if (mspec.endsWith("-S2")) {
            assertEquals("SunOS", hi.getOS().getName());
            assertEquals(HostInfo.OSFamily.SUNOS, hi.getOSFamily());
        } else if (mspec.endsWith("-Linux")) {
            assertTrue(hi.getOS().getName().startsWith("Linux"));
            assertEquals(HostInfo.OSFamily.LINUX, hi.getOSFamily());
        } else if (mspec.endsWith("-MacOSX")) {
            assertTrue(hi.getOS().getName().startsWith("MacOSX"));
            assertEquals(HostInfo.OSFamily.MACOSX, hi.getOSFamily());
        } else if (mspec.endsWith("-FreeBSD")) {
            assertTrue(hi.getOS().getName().startsWith("FreeBSD"));
            assertEquals(HostInfo.OSFamily.FREEBSD, hi.getOSFamily());
        } else {
            fail("Could not guess OS from mspec " + mspec);
        }

        if (mspec.startsWith("intel-")) {
            assertEquals(HostInfo.CpuFamily.X86, hi.getCpuFamily());
        } else if (mspec.startsWith("sparc-")) {
            assertEquals(HostInfo.CpuFamily.SPARC, hi.getCpuFamily());
        } else {
            fail("Could not guess OS from mspec " + mspec);
        }
    }

    @org.junit.Test
    public void testFileExistsLocal() throws Exception {
        ExecutionEnvironment execEnv = ExecutionEnvironmentFactory.getLocal();
        HostInfo hi = HostInfoUtils.getHostInfo(execEnv);
        String existentFile;
        String nonexistentFile;
        if (hi.getOSFamily() == HostInfo.OSFamily.WINDOWS) {
            existentFile = "C:\\AUTOEXEC.BAT";
            nonexistentFile = "C:\\MANUALEXEC.BAT";
        } else {
            existentFile = "/etc/passwd";
            nonexistentFile = "/etc/passwdx";
        }
        assertTrue(HostInfoUtils.fileExists(execEnv, existentFile));
        assertFalse(HostInfoUtils.fileExists(execEnv, nonexistentFile));
        assertTrue(HostInfoUtils.fileExists(execEnv, existentFile));
        assertFalse(HostInfoUtils.fileExists(execEnv, nonexistentFile));
    }

    @org.junit.Test
    @ForAllEnvironments(section = "remote.platforms")
    public void testFileExists() throws Exception {
        ExecutionEnvironment execEnv = ExecutionEnvironmentFactory.getLocal();
        String existentFile = "/etc/passwd";
        String nonexistentFile = "/etc/passwdx";
        assertTrue(HostInfoUtils.fileExists(execEnv, existentFile));
        assertFalse(HostInfoUtils.fileExists(execEnv, nonexistentFile));
        assertTrue(HostInfoUtils.fileExists(execEnv, existentFile));
        assertFalse(HostInfoUtils.fileExists(execEnv, nonexistentFile));
    }

    @org.junit.Test
    public void testIsLocalhost() {
        assertTrue(HostInfoUtils.isLocalhost("localhost"));
        assertTrue(HostInfoUtils.isLocalhost("127.0.0.1"));
        assertFalse(HostInfoUtils.isLocalhost("localhost1"));
        assertFalse(HostInfoUtils.isLocalhost("localhst"));
        assertFalse(HostInfoUtils.isLocalhost("localhost:22"));
    }
}
