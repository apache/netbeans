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
package org.netbeans.modules.payara.tooling.admin;

import org.netbeans.modules.payara.tooling.TaskState;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.regex.Matcher;
import org.netbeans.modules.payara.tooling.CommonTest;
import static org.netbeans.modules.payara.tooling.CommonTest.BACKUP_DOMAIN;
import static org.netbeans.modules.payara.tooling.CommonTest.readProperties;
import static org.netbeans.modules.payara.tooling.CommonTest.restoreDomain;
import org.netbeans.modules.payara.tooling.PayaraIdeException;
import org.netbeans.modules.payara.tooling.PayaraStatus;
import org.netbeans.modules.payara.tooling.data.PayaraServer;
import org.netbeans.modules.payara.tooling.data.StartupArgsEntity;
import org.netbeans.modules.payara.tooling.logging.Logger;
import org.netbeans.modules.payara.tooling.server.FetchLog;
import org.netbeans.modules.payara.tooling.server.FetchLogSimple;
import org.netbeans.modules.payara.tooling.server.ServerTasks;
import org.netbeans.modules.payara.tooling.utils.StreamLinesList;
import static org.testng.Assert.*;
import org.testng.annotations.AfterGroups;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Parameters;

/**
 * Common Payara HTTP command execution test.
 * <p>
 * @author Tomas Kraus, Peter Benedikovic
 */
public abstract class CommandHttpTest extends CommandTest {
    
    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Logger instance for this class. */
    private static final Logger LOGGER = new Logger(CommandHttpTest.class);

    /** Payara test server property file. */
    private static final String PAYARA_PROPERTES
            = "src/test/java/org/netbeans/modules/payara/tooling/PayaraHttp.properties";

    /** Payara test server properties. */
    private static volatile Properties payaraProperties;

    /** Payara test server object. */
    private static volatile PayaraServer payaraServer;

    /** Payara test server stdout lines stored in linked list. */
    protected static volatile StreamLinesList gfStdOut = null;

    /** Payara test server stderr lines stored in linked list. */
    protected static volatile StreamLinesList gfStdErr = null;

    ////////////////////////////////////////////////////////////////////////////
    // Static methods                                                         //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get Payara test server properties.
     * <p>
     * @return Payara test server properties.
     */
    @BeforeSuite
    @Parameters({ "http-properties" })
    public static Properties payaraProperties(String propertyFile) {
        if (payaraProperties != null) {
            return payaraProperties;
        }
        else {
            synchronized(CommandHttpTest.class) {
                if (payaraProperties == null) {
                    payaraProperties = readProperties(propertyFile);
                }
            }
            return payaraProperties;
        }
    }
    
    public static Properties payaraProperties() {
        return payaraProperties(PAYARA_PROPERTES);
    }

    /**
     * Get Payara test server properties for HTTP tests.
     * <p/>
     * If the key is not found in this property list, the default property list,
     * and its defaults, recursively, are then checked. The method returns
     * <code>null</code> if the property is not found.
     * <p/>
     * @param key Property key.
     * @return Value in Payara test server properties property list with
     *         the specified key value.
     */
    public static String getPayaraProperty(String key) {
        return CommandHttpTest.payaraProperties().getProperty(key);
    }

    /**
     * Constructs <code>PayaraServer</code> object using Payara
     * test server properties
     * <p/>
     * @param properties
     * @return <code>PayaraServer</code> object initialized with Payara
     *         test server properties values.
     */
    public static PayaraServer createPayaraServer() {
        return CommonTest.createPayaraServer(CommandHttpTest.class);
    }

    /**
     * Get Payara test server object with HTTP test specific values.
     * <p>
     * @return Payara test server object with HTTP test specific values.
     */
    public static PayaraServer payaraServer() {
        if (payaraServer != null) {
            return payaraServer;
        }
        else {
            synchronized(CommandHttpTest.class) {
                if (payaraServer == null) {
                    payaraServer = createPayaraServer();
                    PayaraStatus.add(payaraServer);
                }
            }
            return payaraServer;
        }
    }

    /**
     * Payara startup.
     * <p/>
     * Starts Payara server for tests.
     */
    @SuppressWarnings("SleepWhileInLoop")
    @BeforeGroups(groups = {"http-commands"})
    public static void startPayara() {
        final String METHOD = "startPayara";
        LOGGER.log(Level.INFO, METHOD, "startFrame");
        LOGGER.log(Level.INFO, METHOD, "startText");
        LOGGER.log(Level.INFO, METHOD, "startFrame");
        String javaArgsProperty = getPayaraProperty(PFPROP_JAVA_ARGS);
        String payaraArgsProperty = getPayaraProperty(
                PFPROP_PAYARA_ARGS);
        List<String> javaArgs = javaArgsProperty != null
                ? Arrays.asList(javaArgsProperty.split(" +"))
                : new ArrayList<String>();
        List<String> payaraArgs = payaraArgsProperty != null
                ? Arrays.asList(payaraArgsProperty.split(" +"))
                : new ArrayList<>();
        StartupArgsEntity startupArgs = new StartupArgsEntity(
                payaraArgs,
                javaArgs,
                new HashMap<String, String>(),
                getJdkProperty(JDKPROP_HOME));
        PayaraServer server = payaraServer();
        restoreDomain(server, getPayaraProperty(BACKUP_DOMAIN));
        ResultProcess result = ServerTasks.startServer(server, startupArgs);
        ValueProcess process = result.getValue();
        FetchLog stdOutLog = new FetchLogSimple(process.getProcess()
                .getInputStream());
        FetchLog stdErrLog = new FetchLogSimple(process.getProcess()
                .getErrorStream());
        gfStdOut = new StreamLinesList(stdOutLog);
        gfStdErr = new StreamLinesList(stdErrLog);
        boolean exit = false;
        boolean started = false;
        boolean shutdownOnError = false;
        int emptyCycles = 0;
        while (!exit && emptyCycles++ < 30) {
            String line;                    
            while ((line = gfStdOut.getNext()) != null) {
                Matcher startedMessageMatcher
                        = STARTED_MESSAGE_PATTERN.matcher(line);
                Matcher shutdownMessageMatcher
                        = SHUTDOWN_MESSAGE_PATTERN.matcher(line);
                if (startedMessageMatcher.matches()) {
                    started = true;
                    exit = true;
                }
                if (shutdownMessageMatcher.matches()) {
                    shutdownOnError = true;
                }
                LOGGER.log(Level.INFO, METHOD, "stdout", line);
                emptyCycles = 0;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ie) {
                fail("FetchLogData command execution failed: "
                        + ie.getMessage());
            }
        }
        assertNotNull(result.getValue());
        assertTrue(started);
        if (shutdownOnError) {
            LOGGER.log(Level.SEVERE, METHOD, "failed");
        }
    }

    /**
     * Test cleanup
     * 
     */
    @AfterGroups(groups = {"http-commands"})
    public static void stopPayara() {
        final String METHOD = "stopPayara";
        LOGGER.log(Level.INFO, METHOD, "stopFrame");
        LOGGER.log(Level.INFO, METHOD, "stopText");
        LOGGER.log(Level.INFO, METHOD, "stopFrame");
        PayaraServer server = payaraServer();
        Command command = new CommandStopDAS();
        try {
            Future<ResultString> future = 
                    ServerAdmin.<ResultString>exec(server, command);
            try {
                ResultString result = future.get();
                gfStdOut.close();
                gfStdErr.close();
                assertNotNull(result.getValue());
                assertTrue(result.getState() == TaskState.COMPLETED);
            } catch (InterruptedException | ExecutionException ie) {
                fail("Version command execution failed: " + ie.getMessage());
            }
        } catch (PayaraIdeException gfie) {
            fail("Version command execution failed: " + gfie.getMessage());
        }
    }

}
