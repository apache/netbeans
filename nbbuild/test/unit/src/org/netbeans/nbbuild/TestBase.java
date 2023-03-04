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
package org.netbeans.nbbuild;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.URL;
import java.security.Permission;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.jar.Manifest;
import junit.framework.AssertionFailedError;
import org.netbeans.junit.NbTestCase;

/**
 * Predefines commonly used utilities.
 */
abstract class TestBase extends NbTestCase {

    protected TestBase(String name) {
        super(name);
    }

    protected @Override void setUp() throws Exception {
        clearWorkDir();
    }

    // XXX look for other copy-pasted utility methods, like createNewJarFile

    protected final String readFile(java.io.File f) throws java.io.IOException {
        int s = (int) f.length();
        byte[] data = new byte[s];
        assertEquals("Read all data", s, new java.io.FileInputStream(f).read(data));

        return new String(data);
    }

    protected final Manifest createManifest() {
        Manifest m = new Manifest();
        m.getMainAttributes().putValue(java.util.jar.Attributes.Name.MANIFEST_VERSION.toString(), "1.0");
        return m;
    }

    protected final File extractString(String res) throws Exception {
        File f = File.createTempFile("res", ".xml", getWorkDir());

        try (FileOutputStream os = new FileOutputStream(f)) {
            InputStream is = new ByteArrayInputStream(res.getBytes("UTF-8"));
            for (;;) {
                int ch = is.read();
                if (ch == -1) {
                    break;
                }
                os.write(ch);
            }
        }

        return f;
    }

    protected final File extractResource(String res) throws Exception {
        File f = File.createTempFile("res", ".xml", getWorkDir());
        extractResource(f, res);
        return f;
    }

    protected final void extractResource(File f, String res) throws Exception {
        URL u = getClass().getResource(res);
        assertNotNull("Resource should be found " + res, u);


        try (FileOutputStream os = new FileOutputStream(f)) {
            InputStream is = u.openStream();
            for (;;) {
                int ch = is.read();
                if (ch == -1) {
                    break;
                }
                os.write(ch);
            }
        }
    }

    protected final void execute(String res, String... args) throws Exception {
        execute(extractResource(res), args);
    }

    private static ByteArrayOutputStream out, err;

    protected final String getStdOut() {
        return out.toString();
    }

    protected final String getStdErr() {
        return err.toString();
    }

    protected final void execute(File f, String[] args) throws Exception {
        // we need security manager to prevent System.exit
        if (!(System.getSecurityManager() instanceof MySecMan)) {
            out = new java.io.ByteArrayOutputStream();
            err = new java.io.ByteArrayOutputStream();
            System.setOut(new java.io.PrintStream(out));
            System.setErr(new java.io.PrintStream(err));

            System.setSecurityManager(new MySecMan());
        }

        MySecMan sec = (MySecMan) System.getSecurityManager();

        // Jesse claims that this is not the right way how the execution
        // of an ant script should be invoked:
        //
        // better IMHO to just run the task directly
        // (setProject() and similar, configure its bean properties, and call
        // execute()), or just make a new Project and initialize it.
        // ant.Main.main is not intended for embedded use. Then you could get rid
        // of the SecurityManager stuff, would be cleaner I think.
        //
        // If I had to write this once again, I would try to follow the
        // "just make a new Project and initialize it", but as this works
        // for me now, I leave it for the time when somebody really
        // needs that...

        List<String> arr = new ArrayList<>();
        arr.add("-f");
        arr.add(f.toString());
        arr.addAll(Arrays.asList(args));


        out.reset();
        err.reset();

        try {
            sec.setActive(true);
            org.apache.tools.ant.Main.main(arr.toArray(new String[0]));
        } catch (MySecExc ex) {
            assertNotNull("The only one to throw security exception is MySecMan and should set exitCode", sec.exitCode);
            ExecutionError.assertExitCode("Execution has to finish without problems", sec.exitCode);
        } finally {
            sec.setActive(false);
        }
    }

    protected static final class ExecutionError extends AssertionFailedError {
        public final int exitCode;
        public ExecutionError(String msg, int e) {
            super(msg);
            this.exitCode = e;
        }
        private static void assertExitCode(String msg, int e) {
            if (e != 0) {
                throw new ExecutionError(
                        msg + " was: " + e + "\nOutput: " + out.toString()
                        + "\nError: " + err.toString(),
                        e);
            }
        }
    }

    private static class MySecExc extends SecurityException {
        @Override
        public void printStackTrace() {
        }
        @Override
        public void printStackTrace(PrintStream ps) {
        }
        @Override
        public void printStackTrace(PrintWriter ps) {
        }
    }

    private static class MySecMan extends SecurityManager {
        public Integer exitCode;
        private boolean active;
        @Override
        public void checkExit(int status) {
            if (active) {
                exitCode = status;
                throw new MySecExc();
            }
        }
        @Override
        public void checkPermission(Permission perm, Object context) {
        }
        @Override
        public void checkPermission(Permission perm) {
            /*
            if (perm instanceof RuntimePermission) {
            if (perm.getName ().equals ("setIO")) {
            throw new MySecExc ();
            }
            }
             */
        }
        @Override
        public void checkMulticast(InetAddress maddr) {
        }
        @Override
        public void checkAccess(ThreadGroup g) {
        }
        @Override
        public void checkWrite(String file) {
        }
        @Override
        public void checkLink(String lib) {
        }
        @Override
        public void checkExec(String cmd) {
        }
        @Override
        public void checkDelete(String file) {
        }
        @Override
        public void checkPackageAccess(String pkg) {
        }
        @Override
        public void checkPackageDefinition(String pkg) {
        }
        @Override
        public void checkPropertyAccess(String key) {
        }
        @Override
        public void checkRead(String file) {
        }
        @Override
        public void checkSecurityAccess(String target) {
        }
        @Override
        public void checkWrite(FileDescriptor fd) {
        }
        @Override
        public void checkListen(int port) {
        }
        @Override
        public void checkRead(FileDescriptor fd) {
        }
        @SuppressWarnings("deprecation")
        @Override
        public void checkMulticast(InetAddress maddr, byte ttl) {
        }
        @Override
        public void checkAccess(Thread t) {
        }
        @Override
        public void checkConnect(String host, int port, Object context) {
        }
        @Override
        public void checkRead(String file, Object context) {
        }
        @Override
        public void checkConnect(String host, int port) {
        }
        @Override
        public void checkAccept(String host, int port) {
        }
        @Override
        public void checkSetFactory() {
        }
        @Override
        public void checkCreateClassLoader() {
        }
        @Override
        public void checkPrintJobAccess() {
        }
        @Override
        public void checkPropertiesAccess() {
        }
        void setActive(boolean b) {
            active = b;
        }
    } // end of MySecMan

}
