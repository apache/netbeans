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
package org.netbeans.upgrade.launcher;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Lookup;
import org.openide.util.Utilities;

/**
 *
 * @author Jaroslav Tulach, Tomas Hurka
 */
public class PlatformWithAbsolutePathTest extends NbTestCase {
    public PlatformWithAbsolutePathTest(String name) {
        super(name);
    }
    

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
    }
    
        
    public void testPlatformWithAbsolutePath() throws Exception {
        File wd = new File(getWorkDir(), "currentdir");
        wd.mkdirs();
        URL u = Lookup.class.getProtectionDomain().getCodeSource().getLocation();
        File utilFile = new File(u.toURI());
        assertTrue("file found: " + utilFile, utilFile.exists());
        File root = utilFile.getParentFile().getParentFile().getParentFile();
        File bin = new File(root,"bin");
        File newBin = new File(wd,"bin");
        newBin.mkdirs();
        File newEtc = new File(wd,"etc");
        newEtc.mkdirs();
        File[] binFiles = bin.listFiles();
        for (File f : binFiles) {
            File newFile = new File(newBin, f.getName());
            Files.copy(f.toPath(), newFile.toPath());
        }
        Files.writeString(new File(newEtc, "netbeans.clusters").toPath(), utilFile.getParentFile().getParent()+"\n");
        String str = "1 * * * *";
        run(wd, str);
        
        String[] args = MainCallback.getArgs(getWorkDir());
        assertNotNull("args passed in", args);
        List<String> a = Arrays.asList(args);
        assertTrue(str + " should be there: " + a, a.contains(str));
    }
    
    
    private void run(File workDir, String... args) throws Exception {
        File bin = new File(workDir,"bin");
        File nbexec = Utilities.isWindows() ? new File(bin, "netbeans.exe") : new File(bin, "netbeans");
        assertTrue("nbexec found: " + nbexec, nbexec.exists());

        URL tu = MainCallback.class.getProtectionDomain().getCodeSource().getLocation();
        File testf = new File(tu.toURI());
        assertTrue("file found: " + testf, testf.exists());
        
        LinkedList<String> allArgs = new LinkedList<>(Arrays.asList(args));
        allArgs.addFirst("-J-Dnetbeans.mainclass=" + MainCallback.class.getName());
        allArgs.addFirst(System.getProperty("java.home"));
        allArgs.addFirst("--jdkhome");
        allArgs.addFirst(getWorkDirPath());
        allArgs.addFirst("--userdir");
        allArgs.addFirst(testf.getPath());
        allArgs.addFirst("-cp:p");
        
        if (!Utilities.isWindows()) {
            allArgs.addFirst(nbexec.getPath());
            allArgs.addFirst("-x");
            allArgs.addFirst("/bin/sh");
        } else {
            allArgs.addFirst(nbexec.getPath());
        }
        
        StringBuffer sb = new StringBuffer();
        Process p = Runtime.getRuntime().exec(allArgs.toArray(String[]::new), null, workDir);
        int res = readOutput(sb, p);
        
        String output = sb.toString();
        
        assertEquals("Execution is ok: " + output, 0, res);
    }
    
    private static int readOutput(final StringBuffer sb, Process p) throws Exception {
        class Read extends Thread {
            private final InputStream is;

            public Read(String name, InputStream is) {
                super(name);
                this.is = is;
                setDaemon(true);
            }

            @Override
            public void run() {
                byte[] arr = new byte[4096];
                try {
                    for(;;) {
                        int len = is.read(arr);
                        if (len == -1) {
                            return;
                        }
                        sb.append(new String(arr, 0, len));
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }

        Read out = new Read("out", p.getInputStream());
        Read err = new Read("err", p.getErrorStream());
        out.start();
        err.start();

        int res = p.waitFor();

        out.interrupt();
        err.interrupt();
        out.join();
        err.join();

        return res;
    }
    
}
