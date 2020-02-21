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

package org.netbeans.modules.remote.impl.fs.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.netbeans.modules.nativeexecution.test.NativeExecutionTestSupport;
import org.netbeans.modules.nativeexecution.test.RcFile;

/**
 *
 */
public class FsServerRefreshLoadMeasurementTestCase extends FsServerLocalTestBase {

    public static final String SECTION = "FsServerRefreshLoadMeasurement";

    public FsServerRefreshLoadMeasurementTestCase(String name) {
        super(name);
    }

    @Override
    protected int timeOut() {
        return 60*60*1000;
    }

    public void testFsServerRefreshLoad() throws Exception {

        final RcFile rcFile = NativeExecutionTestSupport.getRcFile();
        final int serverCount = rcFile.get(SECTION, "server_count", 10);
        final int threadCount = rcFile.get(SECTION, "server_threads", 4);
        final int dirCount = rcFile.get(SECTION, "dir_count", 10000);
        //final boolean different = rcFile.get(SECTION, "dirs_differ", false);
        final String startPath = rcFile.get(SECTION, "start_path", getNetBeansSourceDir().getAbsolutePath());
        final int refreshInterval = rcFile.get(SECTION, "refresh_interval", 2);
        final int traceLevel = rcFile.get(SECTION, "trace_level", 0);
        
        /** Allows not to waste time in filling cache for subsequent runs */
        final boolean skipInit = rcFile.get(SECTION, "skip_init", false);

        FSServer[] servers = new FSServer[serverCount];
        String[] dirNames = new String[serverCount];
        for (int i = 0; i < servers.length; i++) {
            dirNames[i] = getName().replace(' ', '_').replace("[", "").replace("]", "") + '_' + i;
        }
        
        if (!skipInit) {
            System.out.printf("%s: gathering %d directories\n", getName(), dirCount);

            final List<File> dirs = gatherDirectories(startPath, dirCount);
            assertEquals(dirs.size(), dirCount);

            System.out.printf("%s: launching %d instances of fs_server\n", getName(), serverCount);

            for (int i = 0; i < servers.length; i++) {
                servers[i] = new FSServer(
                        Mode.TIME,
                        "-t", Integer.toString(threadCount), 
                        "-l", // log requests
                        "-c", // cleanup persistence dir
                        "-p", // persist responses
                        "-d", dirNames[i], 
                        "-v", Integer.toString(rcFile.get(SECTION, "trace_level", 0)));
            }

            long time = System.currentTimeMillis();
            System.out.printf("%s: feeding %d servers with %d directories...\n", getName(), serverCount, dirCount);
            for (File d : dirs) {
                for (FSServer server : servers) {
                    server.requestLs(d.getAbsolutePath());
                    //readLsResponse(server);
                }
                for (FSServer server : servers) {
                    readLsResponse(server);
                }
            }
            time = System.currentTimeMillis() - time;
            System.out.printf("%s: feeding %d servers with %d directories took %d seconds\n", getName(), serverCount, dirCount, time/1000);

            shutDownSevers(servers, 2000);
            sleep(2000);
            analyzeTime("Filling:", servers, System.err);
            System.out.printf("\n\n");
        }

        System.out.printf("%s: launching %d instances of fs_server with %d refresh threads each\n", getName(), serverCount, threadCount);        
        for (int i = 0; i < servers.length; i++) {
            servers[i] = new FSServer(
                    Mode.TIME,
                    "-t", Integer.toString(threadCount), 
                    "-l", // log requests
                    "-p", // persist responses
                    "-d", dirNames[i], 
                    "-r", Integer.toString(refreshInterval),
                    "-v", Integer.toString(traceLevel));
        }
        System.out.printf("%s: launched %d instances of fs_server with %d refresh threads each\n", getName(), serverCount, threadCount);

        final int sleepInterval = rcFile.get(SECTION, "measure_interval", 120); 
        System.out.printf("Sleeping %d seconds...\n", sleepInterval);
        sleep(sleepInterval * 1000);
        
        shutDownSevers(servers, 2000);

        // Linux /usr/bin/time output example:
        //  28.20user 39.32system 1:08.20elapsed 99%CPU (0avgtext+0avgdata 5680maxresident)k
        //  0inputs+8outputs (0major+1348200minor)pagefaults 0swaps        
        // Solaris and Linux with --portability option output example:
        //  real 20.01
        //  user 8.04
        //  sys 11.74
        analyzeTime(String.format("Refresh: %ds; %d dirs; %d servers; interval %ds:", sleepInterval,dirCount, servers.length, refreshInterval), 
                servers, System.err);
    }
    
    private static void analyzeTime(String heading, FSServer[] servers, PrintStream ps) throws IOException {
        
        String sep   = "----------------------------------------------------------------\n";
        String title = "PID             Real     User     Sys     %CPU     %Usr     %Sys\n";
        String format = "%-12s %7.1f  %7.1f %7.1f  %7.1f  %7.1f  %7.1f\n";
        
        float max_real = 0;
        
        float total_real = 0;
        float total_user = 0;
        float total_sys = 0;
        
        ps.flush();
        ps.printf("\n\n%s%s\n%s%s%s", sep, heading, sep, title, sep);

        for (FSServer server : servers) {
            List<String> err = server.getStdErr();
            String line1 = err.get(err.size() - 3);
            String line2 = err.get(err.size() - 2);
            String line3 = err.get(err.size() - 1);
            assertStartsWith(line1, "real ");
            assertStartsWith(line2, "user ");
            assertStartsWith(line3, "sys ");
            float real = Float.parseFloat(line1.substring(4).trim());
            float user = Float.parseFloat(line2.substring(4).trim());
            float sys  = Float.parseFloat(line3.substring(4).trim());
            total_real += real;
            total_user += user;
            total_sys += sys;
            if (real > max_real) {
                max_real = real;
            }
            ps.printf(format, "" + server.getProcess().getPID(), 
                    real, user, sys, 
                    (user+sys)*100/real, user*100/real, sys*100/real);
        }
        ps.printf(format, "Average", 
                total_real/servers.length, total_user/servers.length, total_sys/servers.length, 
                (total_user+total_sys)*100/total_real, total_user*100/total_real, total_sys*100/total_real);
        ps.printf(format, "Total", 
                total_real, total_user, total_sys, 
                (total_user+total_sys)*100/max_real, total_user*100/max_real, total_sys*100/max_real);
        ps.printf("%s\n\n", sep);
    }

    private static void assertStartsWith(String text, String prefix) {
        if (!text.startsWith(prefix)) {
            assertTrue("'" + text + "' does not start with '" + prefix + "'" , false);
        }
    }
    
    private void shutDownSevers(FSServer[] servers, int waitMillis) throws InterruptedException {
        System.out.printf("%s: shutting down %d servers...\n", getName(), servers.length);
        for (FSServer server : servers) {
            server.requestQuit();
        }
        for (FSServer server : servers) {
            timedWait(server.getProcess(), waitMillis);
        }
        System.out.printf("%s: shutted down %d servers.\n", getName(), servers.length);
    }
    
    private void readLsResponse(FSServer server) throws Exception {
        BufferedReader reader = server.getReader();
        String line;
        while((line = reader.readLine()) != null) {
            if (!line.isEmpty() && line.charAt(0) == FSSResponseKind.FS_RSP_END.getChar()) {
                return;
            }
        }
    }

    private List<File> gatherDirectories(String startPath, int count) {
        File startDir = new File(startPath);
        assertTrue(startDir.isDirectory());
        List<File> dirs = new ArrayList<>(count);
        AtomicInteger rest = new AtomicInteger(count);
        gatherDirectories(startDir, dirs, rest, new HashSet<File>());
        return dirs;
    }
    
    private void gatherDirectories(File dir, List<File> dirs, AtomicInteger rest, Set<File> antiLoop) {
        if (antiLoop.contains(dir)) {
            return;
        }
        antiLoop.add(dir);
        if (rest.get() == 0) {
            return;
        }
        dirs.add(dir);        
        if (rest.decrementAndGet() == 0) {
            return;
        }
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                gatherDirectories(file, dirs, rest, antiLoop);
                if (rest.get() == 0) {
                    return;
                }
            }
        }
    }

    private File getNetBeansSourceDir() {
        File dataDir = getDataDir();
        for (File parentFile = dataDir.getParentFile(); parentFile != null; parentFile = parentFile.getParentFile()) {
            if (parentFile.getName().equals("dlight.remote.impl")) {
                return parentFile.getParentFile();
            }
        }
        return null;
    }
}
