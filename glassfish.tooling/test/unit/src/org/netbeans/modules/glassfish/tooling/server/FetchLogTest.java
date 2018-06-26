/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2015, 2016 Oracle and/or its affiliates. All rights reserved.
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
 */
package org.netbeans.modules.glassfish.tooling.server;

import java.io.*;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.modules.glassfish.tooling.admin.CommandHttpTest;
import org.netbeans.modules.glassfish.tooling.data.GlassFishServer;
import org.netbeans.modules.glassfish.tooling.data.GlassFishServerEntity;
import org.netbeans.modules.glassfish.tooling.utils.OsUtils;
import org.netbeans.modules.glassfish.tooling.utils.ServerUtils;
import org.netbeans.modules.glassfish.tooling.utils.StreamLinesList;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 * Test GlassFish server log fetcher.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
public class FetchLogTest extends CommandHttpTest {

    /**
     * Test GlassFish server log fetcher against local server.
     */
    @Test(groups = {"http-commands"})
    public void testLocalFetchLog() {
        GlassFishServer server = glassFishServer();
        if (server.getDomainsFolder() == null
                || server.getDomainName() == null) {
            fail("Glassfish server cannot be used as local");
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
        GlassFishServerEntity server = new GlassFishServerEntity();
        String ts = Long.toString(System.currentTimeMillis() / 1000l);
        server.setServerHome(File.separator + "tmp"
                + File.separator + "gf" + ts);
        server.setDomainName("domainL");
        server.setDomainsFolder(server.getServerHome() + File.separator
                + "domains");
        String serverDomainDir = ServerUtils.getDomainPath(server);
        String serverLogDir = serverDomainDir
                + File.separator + ServerUtils.GF_LOG_DIR_NAME;
        String serverLogFileTs = serverLogDir
                + File.separator + ts + "_" + ServerUtils.GF_LOG_FILE_NAME;
        File logDir = new File(serverLogDir);
        File logFile = ServerUtils.getServerLogFile(server);
        File logFileTs = new File(serverLogFileTs);
        File tmpGfHome = new File(server.getServerHome());
        if (tmpGfHome.exists()) {
            if (!OsUtils.rmDir(tmpGfHome)) {
                fail("Cannot remove old temporary GlassFish log directory:  "
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
                fail("Cannot clean up temporary GlassFish log directory:  "
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
            fail("Cannot create temporary GlassFish log directory: "
                    + logDir.getAbsolutePath());
        }

    }

    /**
     * Test GlassFish server log fetcher against remote server.
     */
    @SuppressWarnings("SleepWhileInLoop")
    @Test
    public void testRemoteFetchLog() {
        GlassFishServer server = glassFishServer();
        ((GlassFishServerEntity)server).setDomainsFolder(null);
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
