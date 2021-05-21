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

/**
 * Payara version REST command execution test.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
public class CommandRestVersionTest extends CommandRestTest {

    ////////////////////////////////////////////////////////////////////////////
    // Test methods                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Test GlasFissh admin REST command execution.
     */
    @Test(groups = {"rest-commands"})
    public void testCommandVersion() {
        PayaraServer server = payaraServer();
        Command command = new CommandVersion();
        try {
            Future<ResultString> future =
                    ServerAdmin.exec(server, command);
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
