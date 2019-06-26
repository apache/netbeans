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
package org.netbeans.modules.payara.tooling.server;

import java.io.*;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.modules.payara.tooling.admin.CommandHttpTest;
import org.netbeans.modules.payara.tooling.data.PayaraServer;
import org.netbeans.modules.payara.tooling.data.PayaraServerEntity;
import org.netbeans.modules.payara.tooling.utils.OsUtils;
import org.netbeans.modules.payara.tooling.utils.ServerUtils;
import org.netbeans.modules.payara.tooling.utils.StreamLinesList;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 * Test Payara server log fetcher.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
public class FetchLogTest extends CommandHttpTest {

    /**
     * Test Payara server log fetcher against local server.
     */
    @Test(groups = {"http-commands"})
    public void testLocalFetchLog() {
        PayaraServer server = payaraServer();
        if (server.getDomainsFolder() == null
                || server.getDomainName() == null) {
            fail("Payara server cannot be used as local");
        }
        FetchLog log = FetchLogPiped.create(server, false);
        StreamLinesList list = new StreamLinesList(log.getInputStream());
        try {
            Thread.sleep(2 * FetchLogRemote.LOG_REFRESH_DELAY);
        } catch (InterruptedException ex) {
            fail("Waiting for server log was interrupted");
        }
        log.close();
        list.close();
        assertFalse(list.isEmpty(), "No log lines found");
    }

    /** Log messages to test local fetcher. */
    private static final String[] testLog = {
        "A big black bug bit a big black bear,",
        "then a big black bear bit the big black bug.",
        "And when the big black bear bit the big black bug,",
        "then the big black bug bit the big black bear.",
        "",
        "How much wood would a woodchuck chuck,",
        "if a woodchuck could chuck wood?",
        "As much wood as a woodchuck would,",
        "if a woodchuck could chuck wood."
    };

    /**
     * Test local log rotation.
     */
    @Test(groups = {"http-commands"})
    public void testLocalFetchLogRotation() {
        PayaraServerEntity server = new PayaraServerEntity();
        String ts = Long.toString(System.currentTimeMillis() / 1000l);
        server.setServerHome(File.separator + "tmp"
                + File.separator + "gf" + ts);
        server.setDomainName("domainL");
        server.setDomainsFolder(server.getServerHome() + File.separator
                + "domains");
        String serverDomainDir = ServerUtils.getDomainPath(server);
        String serverLogDir = serverDomainDir
                + File.separator + ServerUtils.PF_LOG_DIR_NAME;
        String serverLogFileTs = serverLogDir
                + File.separator + ts + "_" + ServerUtils.PF_LOG_FILE_NAME;
        File logDir = new File(serverLogDir);
        File logFile = ServerUtils.getServerLogFile(server);
        File logFileTs = new File(serverLogFileTs);
        File tmpGfHome = new File(server.getServerHome());
        if (tmpGfHome.exists()) {
            if (!OsUtils.rmDir(tmpGfHome)) {
                fail("Cannot remove old temporary Payara log directory:  "
                        + tmpGfHome.getAbsolutePath());
            }
        }
        if (logDir.mkdirs()) {
            List<String> linesIn = new LinkedList<String>();
            try {
                if (!logFile.createNewFile()) {
                    fail("Cannot create empty log file:  "
                            + logFile.getAbsolutePath());
                }
                FetchLog log = FetchLogPiped.create(server);
                FileWriter out = new FileWriter(logFile);
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(log.getInputStream()));
                int i, j;
                for (i = 0 ; i < 4 ; i++) {
                    out.write(testLog[i]);
                    out.write(OsUtils.LINES_SEPARATOR);
                }
                out.close();
                // We need at least 2 sec delay.
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ex) {
                    fail("Caught InterruptedException: " + ex.getMessage());
                }
                if (!logFile.renameTo(logFileTs)) {
                    fail("Cannot rename log file to: "
                            + logFileTs.getAbsoluteFile());
                }
                if (!logFile.createNewFile()) {
                    fail("Cannot create empty log file:  "
                            + logFile.getAbsolutePath());
                }
                for (j = 0 ; j < 4 ; j++) {
                    linesIn.add(in.readLine());
                }
                out = new FileWriter(logFile);
                for (; i < testLog.length ; i++) {
                    out.write(testLog[i]);
                    out.write(OsUtils.LINES_SEPARATOR);
                }
                out.close();
                for (; j < testLog.length ; j++) {
                    linesIn.add(in.readLine());
                }

            } catch (IOException ex) {
                fail("Caught IOException: " + ex.getMessage());
            }
            if (!OsUtils.rmDir(tmpGfHome)) {
                fail("Cannot clean up temporary Payara log directory:  "
                        + tmpGfHome.getAbsolutePath());
            }
            assertTrue(linesIn.size() == testLog.length);
            Iterator<String> k = linesIn.iterator();
            int i = 0;
            while (k.hasNext() && i < testLog.length) {
                String line = k.next();
                assertTrue(line.equals(testLog[i]));
                System.out.println(line + " :: " + line.length());
                i += 1;
            }
        } else {
            fail("Cannot create temporary Payara log directory: "
                    + logDir.getAbsolutePath());
        }

    }

    /**
     * Test Payara server log fetcher against remote server.
     */
    @SuppressWarnings("SleepWhileInLoop")
    @Test
    public void testRemoteFetchLog() {
        PayaraServer server = payaraServer();
        ((PayaraServerEntity)server).setDomainsFolder(null);
        FetchLog log = FetchLogPiped.create(server, false);
        try {
            while (0 == log.getInputStream().available()) {
                Thread.sleep(FetchLogRemote.LOG_REFRESH_DELAY);
            }
        } catch (IOException ex) {
            fail("Cannot read log data from remote server");
        } catch (InterruptedException ex) {
            fail("Waiting for server log was interrupted");
        }
        StreamLinesList list = new StreamLinesList(log.getInputStream());
        try {
            Thread.sleep(FetchLogRemote.LOG_REFRESH_DELAY);
        } catch (InterruptedException ex) {
            fail("Waiting for server log was interrupted");
        }
        log.close();
        list.close();
        assertFalse(list.isEmpty(), "No log lines found");
    }

}
