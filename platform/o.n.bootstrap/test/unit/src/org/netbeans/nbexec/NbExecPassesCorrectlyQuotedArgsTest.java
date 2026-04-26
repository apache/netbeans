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
package org.netbeans.nbexec;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Lookup;
import org.openide.util.Utilities;

/**
 *
 * @author Jaroslav Tulach
 */
public class NbExecPassesCorrectlyQuotedArgsTest extends NbTestCase {
    public NbExecPassesCorrectlyQuotedArgsTest(String name) {
        super(name);
    }
    

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
    }
    
    
    
    public void testArgsArePassed() throws Exception {
        run(getWorkDir(), "ahoj");
        
        String[] args = MainCallback.getArgs(getWorkDir());
        assertNotNull("args passed in", args);
        List<String> a = Arrays.asList(args);
        if (!a.contains("ahoj")) {
            fail("Ahoj should be there: " + a);
        }
    }
    
    public void testStartsArePassedInUnparsed() throws Exception {
        File wd = new File(getWorkDir(), "currentdir");
        wd.mkdirs();
        File f1 = new File(wd, "f1");
        File f2 = new File(wd, "f2");
        File f3 = new File(wd, "f3");
        f1.createNewFile();
        f2.createNewFile();
        f3.createNewFile();
        
        String str = "1 * * * *";
        run(wd, str);
        
        String[] args = MainCallback.getArgs(getWorkDir());
        assertNotNull("args passed in", args);
        List<String> a = Arrays.asList(args);
        if (!a.contains(str)) {
            fail(str + " should be there: " + a);
        }
    }
   
    public void testJdkHomePassed() throws Exception {
        File wd = new File(getWorkDir(), "jdk dir");
        wd.mkdirs();
        String origJdkHome = System.getProperty("java.home");
        Path origJdk = new File(origJdkHome).toPath();
        String[] linkNames = {
            "openjdk's jdkhome",
            "jdk \"latest\"",
            "current$JAVA_HOME",
            "link\"'d jdk"
        };
        Path[] links = new Path[linkNames.length];
        for (int i = 0; i < linkNames.length; i++) {
            links[i] = new File(wd, linkNames[i]).toPath();
        }
        
        String[] args = {
            "1 * * * *",
            "a b",
            "c d",
            "$1",
            "$2",
            "$3\"`$2`'$1"
        };
        
        for (Path link : links) {
            try {
                Path l = Files.createSymbolicLink(link, origJdk);
                if (link.compareTo(l) != 0) {
                    fail("link creation mismatch: expected<" + link + "> != actual<" + l + ">");
                }
                System.setProperty("java.home", link.toString());
                String[] nbArgs = {
                    "--jdkhome",
                    link.toString()
                };
                run(wd, Stream.concat(Arrays.stream(nbArgs), Arrays.stream(args)).collect(Collectors.toList()).toArray(new String[]{}));

                String[] gotArgs = MainCallback.getArgs(getWorkDir());
                assertNotNull("args passed in", gotArgs);
                for (int in = args.length, jn = gotArgs.length, i = 0, j = Math.max(0, jn - in); i < in ; i++, j++) {
                    if (j >= jn || !Objects.equals(args[i], gotArgs[j]))
                        fail("args do not match: expected<" + Arrays.toString(args) + "> != actual<" + Arrays.toString(gotArgs) + ">");
                }
            } finally {
                System.setProperty("java.home", origJdkHome);
                Files.delete(link);
            }
        }
    }
    
    private void run(File workDir, String... args) throws Exception {
        URL u = Lookup.class.getProtectionDomain().getCodeSource().getLocation();
        File f = Utilities.toFile(u.toURI());
        assertTrue("file found: " + f, f.exists());
        File nbexec = Utilities.isWindows() ? new File(f.getParent(), "nbexec.exe") : new File(f.getParent(), "nbexec");
        assertTrue("nbexec found: " + nbexec, nbexec.exists());

        URL tu = MainCallback.class.getProtectionDomain().getCodeSource().getLocation();
        File testf = Utilities.toFile(tu.toURI());
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
        Process p = Runtime.getRuntime().exec(allArgs.toArray(new String[0]), new String[0], workDir);
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
