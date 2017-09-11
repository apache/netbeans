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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.upgrade.launcher;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.URL;
import java.nio.channels.FileChannel;
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
            File newFile = new File(newBin,f.getName());
            FileChannel newChannel = new RandomAccessFile(newFile,"rw").getChannel();
            new RandomAccessFile(f,"r").getChannel().transferTo(0,f.length(),newChannel);
            newChannel.close();
        }
        RandomAccessFile netbeansCluster = new RandomAccessFile(new File(newEtc,"netbeans.clusters"),"rw");
        netbeansCluster.writeBytes(utilFile.getParentFile().getParent()+"\n");
        netbeansCluster.close();
        String str = "1 * * * *";
        run(wd, str);
        
        String[] args = MainCallback.getArgs(getWorkDir());
        assertNotNull("args passed in", args);
        List<String> a = Arrays.asList(args);
        if (!a.contains(str)) {
            fail(str + " should be there: " + a);
        }
    }
    
    
    private void run(File workDir, String... args) throws Exception {
        File bin = new File(workDir,"bin");
        File nbexec = Utilities.isWindows() ? new File(bin, "netbeans.exe") : new File(bin, "netbeans");
        assertTrue("nbexec found: " + nbexec, nbexec.exists());

        URL tu = MainCallback.class.getProtectionDomain().getCodeSource().getLocation();
        File testf = new File(tu.toURI());
        assertTrue("file found: " + testf, testf.exists());
        
        LinkedList<String> allArgs = new LinkedList<String>(Arrays.asList(args));
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
        Process p = Runtime.getRuntime().exec(allArgs.toArray(new String[0]), null, workDir);
        int res = readOutput(sb, p);
        
        String output = sb.toString();
        
        assertEquals("Execution is ok: " + output, 0, res);
    }
    
    private static int readOutput(final StringBuffer sb, Process p) throws Exception {
        class Read extends Thread {
            private InputStream is;

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
