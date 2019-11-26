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
package org.netbeans.modules.payara.tooling.admin.cloud;

import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.netbeans.modules.payara.tooling.PayaraIdeException;
import org.netbeans.modules.payara.tooling.admin.Command;
import org.netbeans.modules.payara.tooling.admin.CommandRestTest;
import org.netbeans.modules.payara.tooling.admin.ResultString;
import org.netbeans.modules.payara.tooling.admin.ServerAdmin;
import org.netbeans.modules.payara.tooling.data.PayaraServer;
import static org.testng.Assert.*;
import org.testng.annotations.Test;


/**
 * Test deploy command against cpas (cloud).
 *
 * @author Peter Benedikovic, Tomas Kraus
 */
public class CommandCloudDeployTest extends CommandRestTest {

    ////////////////////////////////////////////////////////////////////////////
    // Test methods                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Test GlasFish start cluster command via REST.
     */
    @Test(groups = {"cloud-commands"})
    public void deployAppTest() {
        PayaraServer server = payaraServer();
        Command command = new CommandCloudDeploy(null, new File("/home/piotro/workspaces/basic_paas_sample.war"));
        try {
            Future<ResultString> future =
                    ServerAdmin.<ResultString>exec(server, command);
            try {
                ResultString result = future.get();
                //assertNotNull(result.getValue());
                //assertEquals(result.state, TaskState.COMPLETED);
            } catch (InterruptedException | ExecutionException ie) {
                fail("CommandCloudDeploy command execution failed: " + ie.getMessage());
            }
        } catch (PayaraIdeException gfie) {
            fail("CommandCloudDeploy command execution failed: " + gfie.getMessage());
        }
    }
}
