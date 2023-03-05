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
package org.netbeans.modules.glassfish.tooling.admin;

import org.netbeans.modules.glassfish.tooling.TaskState;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.regex.Matcher;
import org.netbeans.modules.glassfish.tooling.CommonTest;
import static org.netbeans.modules.glassfish.tooling.CommonTest.BACKUP_DOMAIN;
import static org.netbeans.modules.glassfish.tooling.CommonTest.readProperties;
import static org.netbeans.modules.glassfish.tooling.CommonTest.restoreDomain;
import org.netbeans.modules.glassfish.tooling.GlassFishIdeException;
import org.netbeans.modules.glassfish.tooling.GlassFishStatus;
import static org.netbeans.modules.glassfish.tooling.admin.CommandRestTest.getGlassFishProperty;
import org.netbeans.modules.glassfish.tooling.data.GlassFishServer;
import org.netbeans.modules.glassfish.tooling.data.StartupArgsEntity;
import org.netbeans.modules.glassfish.tooling.logging.Logger;
import org.netbeans.modules.glassfish.tooling.server.FetchLog;
import org.netbeans.modules.glassfish.tooling.server.FetchLogSimple;
import org.netbeans.modules.glassfish.tooling.server.ServerTasks;
import org.netbeans.modules.glassfish.tooling.utils.StreamLinesList;
import static org.testng.Assert.*;
import org.testng.annotations.AfterGroups;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Parameters;

/**
 * Common GlassFish HTTP command execution test.
 * <p>
 * @author Tomas Kraus, Peter Benedikovic
 */
public abstract class CommandHttpTest extends CommandTest {
    
    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Logger instance for this class. */
    private static final Logger LOGGER = new Logger(CommandHttpTest.class);

    /** GlassFish test server property file. */
    private static final String GLASSFISH_PROPERTIES
            = "src/test/java/org/netbeans/modules/glassfish/tooling/GlassFishHttp.properties";

    /** GlassFish test server properties. */
    private static volatile Properties glassfishProperties;

    /** GlassFish test server object. */
    private static volatile GlassFishServer glassfishServer;

    /** GlassFish test server stdout lines stored in linked list. */
    protected static volatile StreamLinesList gfStdOut = null;

    /** GlassFish test server stderr lines stored in linked list. */
    protected static volatile StreamLinesList gfStdErr = null;

    ////////////////////////////////////////////////////////////////////////////
    // Static methods                                                         //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get GlassFish test server properties.
     * <p>
     * @return GlassFish test server properties.
     */
    @BeforeSuite
    @Parameters({ "http-properties" })
    public static Properties glassfishProperties(String propertyFile) {
        if (glassfishProperties != null) {
            return glassfishProperties;
        }
        else {
            synchronized(CommandHttpTest.class) {
                if (glassfishProperties == null) {
                    glassfishProperties = readProperties(propertyFile);
                }
            }
            return glassfishProperties;
        }
    }
    
    public static Properties glassfishProperties() {
        return glassfishProperties(GLASSFISH_PROPERTIES);
    }

    /**
     * Get GlassFish test server properties for HTTP tests.
     * <p/>
     * If the key is not found in this property list, the default property list,
     * and its defaults, recursively, are then checked. The method returns
     * <code>null</code> if the property is not found.
     * <p/>
     * @param key Property key.
     * @return Value in GlassFish test server properties property list with
     *         the specified key value.
     */
    public static String getGlassFishProperty(String key) {
//        String value = glassfishProperties().getProperty(key);
//        if (value == null) {
//            return CommonTest.glassfishProperties().getProperty(key);
//        } else {
//            return value;
//        }
        return glassfishProperties().getProperty(key);
    }

    /**
     * Constructs <code>GlassFishServer</code> object using GlassFish
     * test server properties
     * <p/>
     * @param properties
     * @return <code>GlassFishServer</code> object initialized with GlassFish
     *         test server properties values.
     */
    public static GlassFishServer createGlassfishServer() {
        return CommonTest.createGlassfishServer(CommandHttpTest.class);
    }

    /**
     * Get GlassFish test server object with HTTP test specific values.
     * <p>
     * @return GlassFish test server object with HTTP test specific values.
     */
    public static GlassFishServer glassFishServer() {
        if (glassfishServer != null) {
            return glassfishServer;
        }
        else {
            synchronized(CommandHttpTest.class) {
                if (glassfishServer == null) {
                    glassfishServer = createGlassfishServer();
                    GlassFishStatus.add(glassfishServer);
                }
            }
            return glassfishServer;
        }
    }

    /**
     * GlassFish startup.
     * <p/>
     * Starts GlassFish server for tests.
     */
    @SuppressWarnings("SleepWhileInLoop")
    @BeforeGroups(groups = {"http-commands"})
    public static void startGlassFish() {
        final String METHOD = "startGlassFish";
        LOGGER.log(Level.INFO, METHOD, "startFrame");
        LOGGER.log(Level.INFO, METHOD, "startText");
        LOGGER.log(Level.INFO, METHOD, "startFrame");
        String javaArgsProperty = getGlassFishProperty(GFPROP_JAVA_ARGS);
        String glassFishArgsProperty = getGlassFishProperty(
                GFPROP_GLASSFISH_ARGS);
        List<String> javaArgs = javaArgsProperty != null
                ? Arrays.asList(javaArgsProperty.split(" +"))
                : new ArrayList<String>();
        List<String> glassFishArgs = glassFishArgsProperty != null
                ? Arrays.asList(glassFishArgsProperty.split(" +"))
                : new ArrayList<String>();
        StartupArgsEntity startupArgs = new StartupArgsEntity(
                glassFishArgs,
                javaArgs,
                new HashMap<String, String>(),
                getJdkProperty(JDKPROP_HOME));
        GlassFishServer server = glassFishServer();
        restoreDomain(server, getGlassFishProperty(BACKUP_DOMAIN));
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
    public static void stopGlassFish() {
        final String METHOD = "stopGlassFish";
        LOGGER.log(Level.INFO, METHOD, "stopFrame");
        LOGGER.log(Level.INFO, METHOD, "stopText");
        LOGGER.log(Level.INFO, METHOD, "stopFrame");
        GlassFishServer server = glassFishServer();
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
        } catch (GlassFishIdeException gfie) {
            fail("Version command execution failed: " + gfie.getMessage());
        }
    }

}
