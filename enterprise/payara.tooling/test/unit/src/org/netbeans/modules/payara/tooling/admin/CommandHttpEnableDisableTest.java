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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.netbeans.modules.payara.tooling.PayaraIdeException;
import org.netbeans.modules.payara.tooling.data.PayaraServer;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.fail;
import org.testng.annotations.Test;

/**
 * Payara enable and disable HTTP command execution test.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
public class CommandHttpEnableDisableTest extends CommandHttpTest {

    ////////////////////////////////////////////////////////////////////////////
    // Test methods                                                           //
    ////////////////////////////////////////////////////////////////////////////

    // TODO: Deploy something real on real target.
    /**
     * Test GlasFissh enable administration HTTP command execution.
     */
    @Test(groups = {"http-commands"})
    public void testCommandEnable() {
        PayaraServer server = payaraServer();
        Command command = new CommandEnable("name", "target");
        try {
            Future<ResultString> future = 
                    ServerAdmin.<ResultString>exec(server, command);
            try {
                ResultString result = future.get();
                assertNotNull(result.getValue());
            } catch (InterruptedException | ExecutionException ie) {
                fail("FetchLogData command execution failed: " + ie.getMessage());
            }
        } catch (PayaraIdeException gfie) {
            fail("FetchLogData command execution failed: " + gfie.getMessage());
        }
    }

    // TODO: Deploy something real on real target.
    /**
     * Test GlasFissh disable administration HTTP command execution.
     */
    @Test(groups = {"http-commands"})
    public void testCommandDisable() {
        PayaraServer server = payaraServer();
        Command command = new CommandDisable("name", "target");
        try {
            Future<ResultString> future = 
                    ServerAdmin.<ResultString>exec(server, command);
            try {
                ResultString result = future.get();
                assertNotNull(result.getValue());
            } catch (InterruptedException | ExecutionException ie) {
                fail("FetchLogData command execution failed: " + ie.getMessage());
            }
        } catch (PayaraIdeException gfie) {
            fail("FetchLogData command execution failed: " + gfie.getMessage());
        }
    }

}
