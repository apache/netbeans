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

import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import static org.netbeans.modules.payara.tooling.CommonTest.JDKPROP_HOME;
import org.netbeans.modules.payara.tooling.PayaraIdeException;
import org.netbeans.modules.payara.tooling.TaskState;
import org.netbeans.modules.payara.tooling.data.PayaraServer;
import org.netbeans.modules.payara.tooling.logging.Logger;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
import org.testng.annotations.Test;

/**
 * <p/>
 * @author Tomas Kraus
 */
public class CommandAsadminTest extends CommandTest {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Logger instance for this class. */
    private static final Logger LOGGER = new Logger(CommandAsadminTest.class);

    ////////////////////////////////////////////////////////////////////////////
    // Test methods                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Test change administrator's password command execution using
     * local asadmin interface on Payara.
     */
    @Test
    public void testCommandChangeAdminPasswordPF() {
        final String METHOD = "testCommandChangeAdminPasswordPF";
        Properties properties = jdkProperties();
        PayaraServer server = CommandHttpTest.payaraServer();
        Command command = new CommandChangeAdminPassword(
                properties.getProperty(JDKPROP_HOME), "admin123");
        try {
            Future<ResultString> future =
                    ServerAdmin.<ResultString>exec(server, command);
            try {
                ResultString result = future.get();
                String output = result.getValue();
                LOGGER.log(Level.INFO, METHOD, "output", output);
                assertNotNull(result.getValue());
                assertTrue(result.getState() == TaskState.COMPLETED);
            } catch (InterruptedException | ExecutionException ie) {
                fail("Change administrator's password command execution failed: "
                        + ie.getMessage());
            }
        } catch (PayaraIdeException gfie) {
            fail("Change administrator's password command execution failed: "
                    + gfie.getMessage());
        }
    }

    /**
     * Test change administrator's password command execution using
     * local asadmin interface on Payara v4.
     */
    @Test
    public void testCommandChangeAdminPasswordGFv4() {
        final String METHOD = "testCommandChangeAdminPasswordGFv4";
        Properties properties = jdkProperties();
        PayaraServer server = CommandRestTest.payaraServer();
        Command command = new CommandChangeAdminPassword(
                properties.getProperty(JDKPROP_HOME), "admin123");
        try {
            Future<ResultString> future =
                    ServerAdmin.<ResultString>exec(server, command);
            try {
                ResultString result = future.get();
                String output = result.getValue();
                LOGGER.log(Level.INFO, METHOD, "output", output);
                assertNotNull(result.getValue());
                assertTrue(result.getState() == TaskState.COMPLETED);
            } catch (InterruptedException | ExecutionException ie) {
                fail("Change administrator's password command execution failed: "
                        + ie.getMessage());
            }
        } catch (PayaraIdeException gfie) {
            fail("Change administrator's password command execution failed: "
                    + gfie.getMessage());
        }
    }

}
