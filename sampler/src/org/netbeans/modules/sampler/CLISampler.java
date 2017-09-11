/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.sampler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

/**
 * Support for sampling from command line
 *
 * @author Tomas Hurka, Jaroslav Tulach
 *
 */
class CLISampler extends Sampler {
    
    private final ThreadMXBean threadMXBean;
    private final File output;

    public static void main(String... args) throws Exception {
        if (args.length != 2) {
            System.out.println("Usage: <port> <snapshot.npss>");
            System.out.println();
            System.out.println("First of all start your application with following parameters:");
            System.out.println("  -Dcom.sun.management.jmxremote.authenticate=false");
            System.out.println("  -Dcom.sun.management.jmxremote.ssl=false");
            System.out.println("  -Dcom.sun.management.jmxremote.port=<port>");
            System.out.println("Then you can start this sampler with correct port and file to write snapshot to.");
            System.exit(1);
        }
        if (!SamplesOutputStream.isSupported()) {
            System.err.println("Sampling is not supported by JVM");
            System.exit(2);
        }

        String u = args[0];
        try {
            u = "service:jmx:rmi:///jndi/rmi://localhost:" + Integer.parseInt(args[0]) + "/jmxrmi";
        } catch (NumberFormatException ex) {
            // OK, use args[0]
        }

        System.err.println("Connecting to " + u);
        JMXServiceURL url = new JMXServiceURL(u);
        JMXConnector jmxc = null;
        Exception ex = null;
        for (int i = 0; i < 100; i++) {
            try {
                jmxc = JMXConnectorFactory.connect(url, null);
                break;
            } catch (IOException e) {
                ex = e;
                System.err.println("Connection failed. Will retry in 300ms.");
                Thread.sleep(300);
            }
        }
        if (jmxc == null) {
            ex.printStackTrace();
            System.err.println("Cannot connect to " + u);
            System.exit(3);
        }
        MBeanServerConnection server = jmxc.getMBeanServerConnection();

        final ThreadMXBean threadMXBean = ManagementFactory.newPlatformMXBeanProxy(
                server, ManagementFactory.THREAD_MXBEAN_NAME, ThreadMXBean.class);
        final File output = new File(args[1]);


        CLISampler s = new CLISampler(threadMXBean, output);
        s.start();
        System.out.println("Press enter to generate sample into " + output);
        System.in.read();
        s.stop();
        System.out.println();
        System.out.println("Sample written to " + output);
        System.exit(0);
    }

    private CLISampler(ThreadMXBean threadBean, File out) {
        super("CLISampler");
        threadMXBean = threadBean;
        output = out;
    }

    @Override
    protected ThreadMXBean getThreadMXBean() {
        return threadMXBean;
    }

    @Override
    protected void saveSnapshot(byte[] arr) throws IOException {
        FileOutputStream os = new FileOutputStream(output);
        os.write(arr);
        os.close();
    }

    @Override
    protected void printStackTrace(Throwable ex) {
        ex.printStackTrace();
        System.exit(2);
    }

    @Override
    protected void openProgress(int steps) {
    }

    @Override
    protected void closeProgress() {
    }

    @Override
    protected void progress(int i) {
        System.out.print("#");
        System.out.flush();
    }
}
