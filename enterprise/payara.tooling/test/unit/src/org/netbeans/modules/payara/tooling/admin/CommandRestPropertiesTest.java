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
import org.netbeans.modules.payara.tooling.data.PayaraServer;
import static org.testng.Assert.*;
import org.testng.annotations.Test;
import static org.netbeans.modules.payara.tooling.admin.CommandRestTest.payaraServer;

/**
 * Payara get property REST command execution test.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
public class CommandRestPropertiesTest extends CommandRestTest {

    // Test methods                                                           //
    /**
     * Test GlasFissh administration HTTP command execution.
     */
    @Test(groups = {"rest-commands"})
    public void testCommandGetProperty() {
        PayaraServer server = payaraServer();
        Command command = new CommandGetProperty("*.server-config.*.http-listener-1.port");
        try {
            Future<ResultMap<String, String>> future = 
                    ServerAdmin.<ResultMap<String, String>>exec(server, command);
            try {
                ResultMap<String, String> result = future.get();
                assertNotNull(result.getValue());
                assertTrue(result.getState() == TaskState.COMPLETED);
                assertFalse(result.getValue().isEmpty());
            } catch (    InterruptedException | ExecutionException ie) {
                fail("get property command execution failed: " + ie.getMessage());
            }
        } catch (PayaraIdeException gfie) {
            fail("get property command execution failed: " + gfie.getMessage());
        }
    }
    
    /**
     * Test GlasFissh administration HTTP command execution.
     */
    @Test(groups = {"rest-commands"})
    public void testCommandSetProperty() {
        PayaraServer server = payaraServer();
        Command command = new CommandSetProperty("configs.config.server-config.java-config.debug-options",
                "-Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=9009");
        try {
            Future<ResultString> future = 
                    ServerAdmin.<ResultString>exec(server, command);
            try {
                ResultString result = future.get();
                //assertNotNull(result.getValue());
                assertEquals(result.getState(), TaskState.COMPLETED);
            } catch (InterruptedException | ExecutionException ie) {
                fail("SetProperty command execution failed: " + ie.getMessage());
            }
        } catch (PayaraIdeException gfie) {
            fail("SetProperty command execution failed: " + gfie.getMessage());
        }
    }

}
