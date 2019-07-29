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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import org.netbeans.modules.payara.tooling.PayaraIdeException;
import org.netbeans.modules.payara.tooling.data.PayaraServer;
import org.netbeans.modules.payara.tooling.data.StartupArgsEntity;
import org.netbeans.modules.payara.tooling.server.ServerTasks;
import org.netbeans.modules.payara.tooling.utils.StreamLinesList;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 * Payara DAS commands execution test.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
@Test(enabled = false, groups = {"unit-tests"})
public class CommandDASTest extends CommandHttpTest {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////
    // Test methods                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Test GlasFissh start local DAS administration HTTP command execution.
     */
    @SuppressWarnings("SleepWhileInLoop")
    @Test
    public void testCommandStartDAS() {
        String javaArgsProperty = getPayaraProperty(PFPROP_JAVA_ARGS);
        String payaraArgsProperty = getPayaraProperty(
                PFPROP_PAYARA_ARGS);
        List<String> javaArgs = javaArgsProperty != null
                ? Arrays.asList(javaArgsProperty.split(" +"))
                : new ArrayList<String>();
        List<String> payaraArgs = payaraArgsProperty != null
                ? Arrays.asList(payaraArgsProperty.split(" +"))
                : new ArrayList<String>();
        StartupArgsEntity startupArgs = new StartupArgsEntity(
                payaraArgs,
                javaArgs,
                new HashMap<String, String>(),
                getJdkProperty(JDKPROP_HOME));
        PayaraServer server = payaraServer();
        ResultProcess result = ServerTasks.startServer(server, startupArgs);

        ValueProcess process = result.getValue();
        StreamLinesList stdOut = new StreamLinesList(
                process.getProcess().getInputStream());
        boolean exit = false;
        boolean started = false;
        boolean shutdownOnError = false;
        int emptyCycles = 0;
        while (!exit && emptyCycles++ < 10) {
            String line;                    
            while ((line = stdOut.getNext()) != null) {
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
                System.out.println("STDOUT: "+line);
                emptyCycles = 0;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ie) {
                fail("FetchLogData command execution failed: "
                        + ie.getMessage());
            }
        }
//                stdOut.close();
//                stdErr.close();
        assertNotNull(result.getValue());
        assertTrue(started);
        if (shutdownOnError) {
            System.out.println("Payara exited on error!");
        }
    }

    /**
     * Test GlasFissh stop DAS administration HTTP command execution.
     */
    @Test//(dependsOnMethods = {"testCommandStartDAS"})
    public void testCommandStopDAS() {
        PayaraServer server = payaraServer();
        Command command = new CommandStopDAS();
        try {
            Future<ResultString> future = 
                    ServerAdmin.<ResultString>exec(server, command);
            try {
                ResultString result = future.get();
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
