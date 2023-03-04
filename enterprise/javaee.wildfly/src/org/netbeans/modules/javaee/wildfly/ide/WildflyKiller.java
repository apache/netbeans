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
package org.netbeans.modules.javaee.wildfly.ide;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import org.openide.util.BaseUtilities;

/**
 *
 * @author Emmanuel Hugonnet (ehsavoie) <ehsavoie@netbeans.org>
 */
public class WildflyKiller {

    private static volatile String errorProcessTable;

    private String javaHome;

    public WildflyKiller() {
        javaHome = System.getenv("JAVA_HOME");
        if (javaHome == null) {
            javaHome = System.getProperty("java.home");
        }
    }

    public boolean killServers() {
        //server is running and port is open.
        Runtime rt = Runtime.getRuntime();
        if (BaseUtilities.isWindows()) {
            return killWindows(rt);
        }
        return killLinux(rt);
    }

    private String getJStackPath() {
        File jstack = new File(javaHome + File.separator + "bin" + File.separator + "jstack");
        if (!jstack.exists() && jstack.getAbsolutePath().contains("jre")) {
            return jstack.getParentFile().getParentFile().getParent() + File.separator + "bin" + File.separator + "jstack";
        }
        return javaHome + File.separator + "bin" + File.separator + "jstack";
    }

    private String getJps() {
        File jps = new File(javaHome + File.separator + "bin" + File.separator + "jps");
        if (!jps.exists() && jps.getAbsolutePath().contains("jre")) {
            return jps.getParentFile().getParentFile().getParent() + File.separator + "bin" + File.separator + "jps";
        }
        return jps.getAbsolutePath();
    }

    private boolean killLinux(Runtime rt) {
        try {
            String getPidCommand = getJps() + " | grep -i \"jboss-modules\" | awk '{ print $1; }'";
            //get a jstack of all the processes
            Process process = rt.exec(new String[]{"/bin/sh", "-c", getPidCommand});
            InputStream in = process.getInputStream();
            String processTable = readString(in);
            readString(process.getErrorStream());
            if (!processTable.isEmpty()) {
                readString(rt.exec(new String[]{"/bin/sh", "-c", getJps() + " | grep -i \"jboss-modules\" | awk '{ print $1; }' | xargs --no-run-if-empty kill -9"}).getInputStream());
                errorProcessTable = processTable;
            }
            long end = System.currentTimeMillis() + 5000;
            while (System.currentTimeMillis() < end) {
                String running = readString(rt.exec(new String[]{"/bin/sh", "-c", getPidCommand}).getInputStream());
                if (running.isEmpty()) {
                    return true;
                } else {
                    Thread.sleep(100);
                }
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean killWindows(Runtime rt) {
        final String GET_PROCESSES_COMMAND = "gwmi win32_process -filter \"name='java.exe' and commandLine like '%jboss-modules%' \" | foreach { " + getJStackPath() + " $_.ProcessId }";
        final String KILL_PROCESSES_COMMAND = "gwmi win32_process -filter \"name='java.exe' and commandLine like '%jboss-modules%' \" | foreach { kill -id $_.ProcessId }";

        try {
            Path getProcessCommand = createCommandFile(GET_PROCESSES_COMMAND);
            Process process = rt.exec(new String[]{"powershell.exe", "-NoProfile", "-NonInteractive", "-ExecutionPolicy", "ByPass", getProcessCommand.toString()});

            InputStream in = process.getInputStream();
            String processTable = readString(in);
            String errors = readString(process.getErrorStream());
            if (errors != null && !errors.isEmpty()) {
                System.out.println("Could not get processes:\n" + errors);
            }

            if (!processTable.isEmpty()) {
                Path killProcessesCommand = createCommandFile(KILL_PROCESSES_COMMAND);
                readString(rt.exec(new String[]{"powershell.exe", "-NoProfile", "-NonInteractive", "-ExecutionPolicy", "ByPass", killProcessesCommand.toString()}).getInputStream());
                errorProcessTable = processTable;
                Files.delete(killProcessesCommand);
            }
            long end = System.currentTimeMillis() + 5000;
            while (System.currentTimeMillis() < end) {
                String running = readString(rt.exec(new String[]{"powershell.exe", "-NoProfile", "-NonInteractive", "-ExecutionPolicy", "ByPass", getProcessCommand.toString()}).getInputStream());
                if (running.isEmpty()) {
                    return true;
                } else {
                    Thread.sleep(100);
                }
            }
            Files.delete(getProcessCommand);
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /*
     we save command into powershell script as otherwise there is too much issues with escaping
     */
    private static Path createCommandFile(String command) throws IOException {
        Path tmp = Files.createTempFile("pskiller", ".ps1");
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(tmp.toFile()))) {
            bos.write(command.getBytes());
        }
        tmp.toFile().deleteOnExit();
        return tmp;
    }

    public static String readString(InputStream file) {
        BufferedInputStream stream = null;
        try {
            stream = new BufferedInputStream(file);
            byte[] buff = new byte[1024];
            StringBuilder builder = new StringBuilder();
            int read = -1;
            while ((read = stream.read(buff)) != -1) {
                builder.append(new String(buff, 0, read));
            }
            return builder.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    //ignore
                }
            }
        }
    }
}
