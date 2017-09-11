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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.autoupdate.services;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.autoupdate.DefaultTestCase;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Lookup;
import org.openide.util.Utilities;

public class NewClustersRebootTest extends NbTestCase {
    private Logger LOG;

    public NewClustersRebootTest(String testName) {
        super(testName);
    }

    @Override
    protected int timeOut() {
        return 30000;
    }

    @Override
    protected Level logLevel() {
        return Level.INFO;
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        LOG = Logger.getLogger("test." + getName());
        super.setUp();
        System.setProperty("netbeans.dirs", getWorkDirPath());
        OutputStream os2 = new FileOutputStream(new File(getWorkDir(),"nbmfortest"));
        NewClustersRebootCallback.copy(DefaultTestCase.class.getResourceAsStream("data/com-sun-testmodule-cluster.nbm"), os2);
        os2.close();
    }

    public void testSelf() throws Exception {
        StringBuffer sb = new StringBuffer();
        assertFalse(getNewCluster().exists());
        invokeNbExecAndCreateCluster(getWorkDir(), sb, new String[]{"--clusters", new File(getWorkDir(), "oldcluster").getAbsolutePath()});
        File f = getNewCluster();
        assertTrue("File "+f.getPath()+" exists", getNewCluster().exists());
        f = getTestModule();
        assertTrue("File "+f.getPath()+" exists", f.exists());
    }

    private File getNewCluster() throws IOException {
        return new File(getWorkDir(), NewClustersRebootCallback.NAME_OF_NEW_CLUSTER);
    }
    
    private File getTestModule() throws IOException {
        return new File(getNewCluster(),"modules"+File.separatorChar+"com-sun-testmodule-cluster.jar");
    }
    
    private void invokeNbExecAndCreateCluster(File workDir, StringBuffer sb, String... args) throws Exception {
        URL u = Lookup.class.getProtectionDomain().getCodeSource().getLocation();
        File f = Utilities.toFile(u.toURI());
        assertTrue("file found: " + f, f.exists());
        File nbexec = org.openide.util.Utilities.isWindows() ? new File(f.getParent(), "nbexec.exe") : new File(f.getParent(), "nbexec");
        assertTrue("nbexec found: " + nbexec, nbexec.exists());
        LOG.log(Level.INFO, "nbexec: {0}", nbexec);

        URL tu = NewClustersRebootCallback.class.getProtectionDomain().getCodeSource().getLocation();
        File testf = Utilities.toFile(tu.toURI());
        assertTrue("file found: " + testf, testf.exists());
                
        LinkedList<String> allArgs = new LinkedList<String>(Arrays.asList(args));
        allArgs.addFirst("-J-Dnetbeans.mainclass=" + NewClustersRebootCallback.class.getName());
        allArgs.addFirst(System.getProperty("java.home"));
        allArgs.addFirst("--jdkhome");
        allArgs.addFirst(getWorkDirPath());
        allArgs.addFirst("--userdir");
        allArgs.addFirst(testf.getPath());
        allArgs.addFirst("-cp:p");
        allArgs.addFirst("--nosplash");

        if (!org.openide.util.Utilities.isWindows()) {
            allArgs.addFirst(nbexec.getPath());
            allArgs.addFirst("-x");
            allArgs.addFirst("/bin/sh");
        } else {
            allArgs.addFirst(nbexec.getPath());
        }
        LOG.log(Level.INFO, "About to execute {0}@{1}", new Object[]{allArgs, workDir});
        Process p = Runtime.getRuntime().exec(allArgs.toArray(new String[0]), new String[0], workDir);
        LOG.log(Level.INFO, "Process created {0}", p);
        int res = readOutput(sb, p);
        LOG.log(Level.INFO, "Output read: {0}", res);
        String output = sb.toString();
//        System.out.println("nbexec output is: " + output);
        assertEquals("Execution is ok: " + output, 0, res);
    }

    private int readOutput(final StringBuffer sb, final Process p) throws Exception {
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
                    for (;;) {
                        LOG.info("reading....");
                        int len = is.read(arr);
                        LOG.log(Level.INFO, "got {0} bytes", len);
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

        LOG.info("waitFor");
        int res = p.waitFor();
        LOG.log(Level.INFO, "waitFor finished: {0}", res);

        out.interrupt();
        err.interrupt();
        
        LOG.info("Interrupting readers");
        out.join();
        err.join();
        LOG.info("Join OK");

        return res;
    }
}
