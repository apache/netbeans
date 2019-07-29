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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.netbeans.modules.payara.tooling.PayaraIdeException;
import org.netbeans.modules.payara.tooling.TestDomainV4Constants;
import org.netbeans.modules.payara.tooling.data.PayaraServer;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 * This test file should contain all tests of commands that talk
 * to an instance.
 *
 * @author Tomas Kraus, Peter Benedikovic
 */
public class CommandRestInstanceTest extends CommandRestTest {

    private static final String INSTANCE_NAME = "instance1";
    private static final String STANDALONE_INSTANCE = "instance2";
    private static final String NODE_NAME = "localhost-domain1";
    private static final String CLUSTER_NAME = "cluster1";

    ////////////////////////////////////////////////////////////////////////////
    // Test methods                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Test GlasFish start instance command via REST.
     */
    @Test(groups = {"rest-commands"}, dependsOnMethods = {"createStandaloneInstanceTest"})
    public void startInstanceTest() {
        PayaraServer server = payaraServer();
        Command command = new CommandStartInstance(STANDALONE_INSTANCE);
        try {
            Future<ResultString> future =
                    ServerAdmin.<ResultString>exec(server, command);
            try {
                ResultString result = future.get();
                //assertNotNull(result.getValue());
                assertEquals(result.state, TaskState.COMPLETED);
            } catch (    InterruptedException | ExecutionException ie) {
                fail("CommandStartInstance command execution failed: " + ie.getMessage());
            }
        } catch (PayaraIdeException gfie) {
            fail("CommandStartInstance command execution failed: " + gfie.getMessage());
        }
    }
    
    /**
     * Test GlasFish stop instance command via REST.
     */
    @Test(groups = {"rest-commands"}, dependsOnMethods = {"startInstanceTest"})
    public void stopInstanceTest() {
        PayaraServer server = payaraServer();
        Command command = new CommandStopInstance(STANDALONE_INSTANCE);
        try {
            Future<ResultString> future =
                    ServerAdmin.<ResultString>exec(server, command);
            try {
                ResultString result = future.get();
                //assertNotNull(result.getValue());
                assertEquals(result.state, TaskState.COMPLETED);
            } catch (    InterruptedException | ExecutionException ie) {
                fail("CommandStopInstance command execution failed: " + ie.getMessage());
            }
        } catch (PayaraIdeException gfie) {
            fail("CommandStopInstance command execution failed: " + gfie.getMessage());
        }
    }
    
    /**
     * Test GlasFish create standalone instance command via REST.
     */
    @Test(groups = {"rest-commands"})
    public void createStandaloneInstanceTest() {
        PayaraServer server = payaraServer();
        Command command = new CommandCreateInstance(STANDALONE_INSTANCE, null,
                TestDomainV4Constants.NODE_NAME);
        try {
            Future<ResultString> future =
                    ServerAdmin.<ResultString>exec(server, command);
            try {
                ResultString result = future.get();
                //assertNotNull(result.getValue());
                assertEquals(result.state, TaskState.COMPLETED);
            } catch (    InterruptedException | ExecutionException ie) {
                fail("CommandCreateInstance command execution failed: " + ie.getMessage());
            }
        } catch (PayaraIdeException gfie) {
            fail("CommandCreateInstance command execution failed: " + gfie.getMessage());
        }
    }
    
    /**
     * Test GlasFish create instance in a cluster command via REST.
     */
    @Test(groups = {"rest-commands"})
    public void createClusterInstanceTest() {
        PayaraServer server = payaraServer();
        Command command = new CommandCreateInstance(INSTANCE_NAME, TestDomainV4Constants.CLUSTER,
                TestDomainV4Constants.NODE_NAME);
        try {
            Future<ResultString> future =
                    ServerAdmin.<ResultString>exec(server, command);
            try {
                ResultString result = future.get();
                assertNotNull(result.getValue());
                assertEquals(result.state, TaskState.COMPLETED);
            } catch (    InterruptedException | ExecutionException ie) {
                fail("CommandCreateInstance command execution failed: " + ie.getMessage());
            }
        } catch (PayaraIdeException gfie) {
            fail("CommandCreateInstance command execution failed: " + gfie.getMessage());
        }
    }
    
    /**
     * Test GlasFish create instance in a cluster command via REST.
     */
    @Test(groups = {"rest-commands"}, dependsOnMethods = {"stopInstanceTest"})
    public void deleteInstanceTest() {
        PayaraServer server = payaraServer();
        Command command = new CommandDeleteInstance(INSTANCE_NAME);
        try {
            Future<ResultString> future =
                    ServerAdmin.<ResultString>exec(server, command);
            try {
                ResultString result = future.get();
                assertNotNull(result.getValue());
                assertEquals(result.state, TaskState.COMPLETED);
            } catch (    InterruptedException | ExecutionException ie) {
                fail("CommandDeleteInstance command execution failed: " + ie.getMessage());
            }
        } catch (PayaraIdeException gfie) {
            fail("CommandDeleteInstance command execution failed: " + gfie.getMessage());
        }
    }
}
