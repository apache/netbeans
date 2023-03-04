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

    CLISampler(ThreadMXBean threadBean, File out) {
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
        try (FileOutputStream os = new FileOutputStream(output)) {
            os.write(arr);
        }
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
