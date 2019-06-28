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
import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.netbeans.modules.payara.tooling.PayaraIdeException;
import org.netbeans.modules.payara.tooling.data.PayaraServer;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.fail;
import org.testng.annotations.Test;

/**
 * Payara domain life cycle HTTP command execution test.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
public class CommandHttpLifeCycleTest extends CommandHttpTest {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    private static final String CLUSTER_NAME = "ToolingCluster";
    private static final String INSTANCE_NAME = "ToolingInstance";

    ////////////////////////////////////////////////////////////////////////////
    // Test methods                                                           //
    ////////////////////////////////////////////////////////////////////////////
/*
 * TODO: Not yes implemented:
 * http://localhost:4848/__asadmin/list-nodes
 * http://localhost:4848/__asadmin/create-instance?DEFAULT=i&node=localhost-domain1&cluster=a
 */
    /**
     * Test GlasFissh create-cluster administration HTTP command execution.
     */
    @Test(groups = {"http-commands"})
    public void testCreateCluster() {
        PayaraServer server = payaraServer();
        Command command = new CommandCreateCluster(CLUSTER_NAME);
        ResultString result = null;
        String value = null;
        try {
            Future<ResultString> future = 
                    ServerAdmin.<ResultString>exec(server, command);
            try {
                result = future.get();
                value = result.getValue();
            } catch (InterruptedException | ExecutionException ie) {
                fail("FetchLogData command execution failed: " + ie.getMessage());
            }
        } catch (PayaraIdeException gfie) {
            fail("FetchLogData command execution failed: " + gfie.getMessage());
        }
        if (result == null || result.getState() != TaskState.COMPLETED) {
            fail(value);
        }
        assertNotNull(value);
    }

    /**
     * Test GlasFissh create-cluster administration HTTP command execution.
     */
    @Test(groups = {"http-commands"}, dependsOnMethods = {"testCreateCluster"})
    public void testCreateClusterInstance() {
        PayaraServer server = payaraServer();
        Command command = new CommandCreateInstance(INSTANCE_NAME, CLUSTER_NAME, "localhost-" + server.getDomainName());
        ResultString result = null;
        String value = null;
        try {
            Future<ResultString> future = 
                    ServerAdmin.<ResultString>exec(server, command);
            try {
                result = future.get();
                value = result.getValue();
            } catch (InterruptedException | ExecutionException ie) {
                fail("FetchLogData command execution failed: " + ie.getMessage());
            }
        } catch (PayaraIdeException gfie) {
            fail("FetchLogData command execution failed: " + gfie.getMessage());
        }
        if (result == null || result.getState() != TaskState.COMPLETED) {
            fail(value);
        }
        assertNotNull(value);
    }

    /**
     * Test GlasFissh deploy administration HTTP command execution.
     */
    @Test(groups = {"http-commands"}, dependsOnMethods = {"testCreateClusterInstance"})
    public void testDeployWebApplication() {
        PayaraServer server = payaraServer();
        Command command = new CommandDeploy("Test", CLUSTER_NAME, new File("target/test/simpleWeb.war"), "simpleWeb", null, null);
        ResultString result = null;
        String value = null;
        try {
            Future<ResultString> future = 
                    ServerAdmin.<ResultString>exec(server, command);
            try {
                result = future.get();
                value = result.getValue();
            } catch (InterruptedException | ExecutionException ie) {
                fail("FetchLogData command execution failed: " + ie.getMessage());
            }
        } catch (PayaraIdeException gfie) {
            fail("FetchLogData command execution failed: " + gfie.getMessage());
        }
        if (result == null || result.getState() != TaskState.COMPLETED) {
            fail(value);
        }
        assertNotNull(value);
    }
    
    /**
     * Test GlasFissh deploy administration HTTP command execution.
     */
    @Test(groups = {"http-commands"}, dependsOnMethods = {"testDeployWebApplication"})
    public void testUndeployWebApplication() {
        PayaraServer server = payaraServer();
        Command command = new CommandUndeploy("Test", CLUSTER_NAME);
        ResultString result = null;
        String value = null;
        try {
            Future<ResultString> future = 
                    ServerAdmin.<ResultString>exec(server, command);
            try {
                result = future.get();
                value = result.getValue();
            } catch (InterruptedException | ExecutionException ie) {
                fail("FetchLogData command execution failed: " + ie.getMessage());
            }
        } catch (PayaraIdeException gfie) {
            fail("FetchLogData command execution failed: " + gfie.getMessage());
        }
        if (result == null || result.getState() != TaskState.COMPLETED) {
            fail(value);
        }
        assertNotNull(value);
    }

    /**
     * Test GlasFissh create-cluster administration HTTP command execution.
     */
    @Test(groups = {"http-commands"}, alwaysRun = true, dependsOnMethods = {"testUndeployWebApplication"})
    public void testDeleteClusterInstance() {
        PayaraServer server = payaraServer();
        Command command = new CommandDeleteInstance(INSTANCE_NAME);
        ResultString result = null;
        String value = null;
        try {
            Future<ResultString> future = 
                    ServerAdmin.<ResultString>exec(server, command);
            try {
                result = future.get();
                value = result.getValue();
            } catch (InterruptedException | ExecutionException ie) {
                fail("FetchLogData command execution failed: " + ie.getMessage());
            }
        } catch (PayaraIdeException gfie) {
            fail("FetchLogData command execution failed: " + gfie.getMessage());
        }
        if (result == null || result.getState() != TaskState.COMPLETED) {
            fail(value);
        }
        assertNotNull(value);
    }

    /**
     * Test GlasFissh create-cluster administration HTTP command execution.
     */
    @Test(groups = {"http-commands"}, alwaysRun = true, dependsOnMethods = {"testDeleteClusterInstance"})
    public void testDeleteCluster() {
        PayaraServer server = payaraServer();
        Command command = new CommandDeleteCluster(CLUSTER_NAME);
        ResultString result = null;
        String value = null;
        try {
            Future<ResultString> future = 
                    ServerAdmin.<ResultString>exec(server, command);
            try {
                result = future.get();
                value = result.getValue();
            } catch (InterruptedException | ExecutionException ie) {
                fail("FetchLogData command execution failed: " + ie.getMessage());
            }
        } catch (PayaraIdeException gfie) {
            fail("FetchLogData command execution failed: " + gfie.getMessage());
        }
        if (result == null || result.getState() != TaskState.COMPLETED) {
            fail(value);
        }
        assertNotNull(value);
    }

}
