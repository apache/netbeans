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
 * to a cluster.
 *
 * @author Tomas Kraus, Peter Benedikovic
 */
public class CommandRestClusterTest extends CommandRestTest {

    private static final String CLUSTER_NAME = "cluster1";

    ////////////////////////////////////////////////////////////////////////////
    // Test methods                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Test GlasFish start cluster command via REST.
     */
    @Test(groups = {"rest-commands"}, dependsOnMethods = {"createClusterTest"})
    public void startClusterTest() {
        PayaraServer server = payaraServer();
        Command command = new CommandStartCluster(CLUSTER_NAME);
        try {
            Future<ResultString> future =
                    ServerAdmin.<ResultString>exec(server, command);
            try {
                ResultString result = future.get();
                assertEquals(result.state, TaskState.COMPLETED);
            } catch (    InterruptedException | ExecutionException ie) {
                fail("CommandStopDAS command execution failed: " + ie.getMessage());
            }
        } catch (PayaraIdeException gfie) {
            fail("CommandStopDAS command execution failed: " + gfie.getMessage());
        }
    }
    
    /**
     * Test GlasFish stop cluster command via REST.
     */
    @Test(groups = {"rest-commands"}, dependsOnMethods = {"startClusterTest"})
    public void stopClusterTest() {
        PayaraServer server = payaraServer();
        Command command = new CommandStopCluster(TestDomainV4Constants.CLUSTER);
        try {
            Future<ResultString> future =
                    ServerAdmin.<ResultString>exec(server, command);
            try {
                ResultString result = future.get();
                //assertNotNull(result.getValue());
                assertEquals(result.state, TaskState.COMPLETED);
            } catch (    InterruptedException | ExecutionException ie) {
                fail("CommandStopCluster command execution failed: " + ie.getMessage());
            }
        } catch (PayaraIdeException gfie) {
            fail("CommandStopCluster command execution failed: " + gfie.getMessage());
        }
    }
    
    /**
     * Test GlasFish create cluster command via REST.
     */
    @Test(groups = {"rest-commands"})
    public void createClusterTest() {
        PayaraServer server = payaraServer();
        Command command = new CommandCreateCluster(CLUSTER_NAME);
        try {
            Future<ResultString> future =
                    ServerAdmin.<ResultString>exec(server, command);
            try {
                ResultString result = future.get();
                //assertNotNull(result.getValue());
                assertEquals(result.state, TaskState.COMPLETED);
            } catch (    InterruptedException | ExecutionException ie) {
                fail("CommandCreateCluster command execution failed: " + ie.getMessage());
            }
        } catch (PayaraIdeException gfie) {
            fail("CommandCreateCluster command execution failed: " + gfie.getMessage());
        }
    }
    
    /**
     * Test GlasFish delete cluster command via REST.
     */
    @Test(groups = {"rest-commands"}, dependsOnMethods = {"stopClusterTest"})
    public void deleteClusterTest() {
        PayaraServer server = payaraServer();
        Command command = new CommandDeleteCluster(CLUSTER_NAME);
        try {
            Future<ResultString> future =
                    ServerAdmin.<ResultString>exec(server, command);
            try {
                ResultString result = future.get();
 //               assertNotNull(result.getValue());
                assertEquals(result.state, TaskState.COMPLETED);
            } catch (    InterruptedException | ExecutionException ie) {
                fail("CommandDeleteCluster command execution failed: " + ie.getMessage());
            }
        } catch (PayaraIdeException gfie) {
            fail("CommandDeleteCluster command execution failed: " + gfie.getMessage());
        }
    }
}
