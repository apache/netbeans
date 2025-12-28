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
import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.netbeans.modules.glassfish.tooling.GlassFishIdeException;
import org.netbeans.modules.glassfish.tooling.TestDomainV4Constants;
import static org.netbeans.modules.glassfish.tooling.admin.CommandRestTest.glassFishServer;
import org.netbeans.modules.glassfish.tooling.data.GlassFishServer;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 * This test file should test commands running against deployed application.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
public class CommandRestApplicationTest extends CommandRestTest {

    // Test methods                                                           //
    /**
     * Test GlasFish start cluster command via REST.
     */
    @Test(groups = {"rest-commands"})
    public void deployAppTest() {
        GlassFishServer server = glassFishServer();
        System.err.println(server.getHost() + server.getAdminPort());
        CommandDeploy command = new CommandDeploy("Test", null, new File("target/test/simpleWeb.war"), "simpleWeb", null, null);
        try {
            Future<ResultString> future =
                    ServerAdmin.<ResultString>exec(server, command);
            try {
                ResultString result = future.get();
                //assertNotNull(result.getValue());
                assertEquals(result.state, TaskState.COMPLETED);
            } catch (    InterruptedException | ExecutionException ie) {
                fail("CommandStopDAS command execution failed: " + ie.getMessage());
            }
        } catch (GlassFishIdeException gfie) {
            fail("CommandStopDAS command execution failed: " + gfie.getMessage());
        }
    }
    
    /**
     * Test GlasFish disable application command via REST.
     */
    @Test(groups = {"rest-commands"})
    public void disableAppTest() {
        GlassFishServer server = glassFishServer();
        Command command = new CommandDisable(TestDomainV4Constants.APPLICATION, null);
        try {
            Future<ResultString> future =
                    ServerAdmin.<ResultString>exec(server, command);
            try {
                ResultString result = future.get();
                //assertNotNull(result.getValue());
                assertEquals(result.state, TaskState.COMPLETED);
            } catch (    InterruptedException | ExecutionException ie) {
                fail("CommandDisable command execution failed: " + ie.getMessage());
            }
        } catch (GlassFishIdeException gfie) {
            fail("CommandDisable command execution failed: " + gfie.getMessage());
        }
    }
    
    /**
     * Test GlasFish enable application command via REST.
     */
    @Test(groups = {"rest-commands"}, dependsOnMethods = {"disableAppTest"})
    public void enableAppTest() {
        GlassFishServer server = glassFishServer();
        Command command = new CommandEnable(TestDomainV4Constants.APPLICATION, null);
        try {
            Future<ResultString> future =
                    ServerAdmin.<ResultString>exec(server, command);
            try {
                ResultString result = future.get();
                //assertNotNull(result.getValue());
                assertEquals(result.state, TaskState.COMPLETED);
            } catch (    InterruptedException | ExecutionException ie) {
                fail("CommandEnable command execution failed: " + ie.getMessage());
            }
        } catch (GlassFishIdeException gfie) {
            fail("CommandEnable command execution failed: " + gfie.getMessage());
        }
    }
}
