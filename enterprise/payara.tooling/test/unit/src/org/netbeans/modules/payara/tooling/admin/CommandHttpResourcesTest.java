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
import static org.netbeans.modules.payara.tooling.admin.CommandHttpTest.payaraServer;

/**
 * Payara list resources HTTP command execution test.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
public class CommandHttpResourcesTest extends CommandHttpTest {

    ////////////////////////////////////////////////////////////////////////////
    // Test methods                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Test GlasFissh administration HTTP command execution.
     */
    @Test(groups = {"http-commands"})
    public void testCommandCreateJdbcPool() {
        PayaraServer server = payaraServer();
        Command command = new CommandCreateJDBCConnectionPool(
                "myJdbcConnPool", "oracle.jdbc.pool.OracleDataSource",
                "javax.sql.DataSource", null);
        try {
            Future<ResultString> future
                    = ServerAdmin.<ResultString>exec(server, command);
            try {
                ResultString result = future.get();
                assertNotNull(result.getValue());
                assertTrue(result.getState() == TaskState.COMPLETED);
            } catch (InterruptedException | ExecutionException ie) {
                fail("DeleteResource command execution failed: " + ie.getMessage());
            }
        } catch (PayaraIdeException gfie) {
            fail("DeleteResource command execution failed: " + gfie.getMessage());
        }
    }
    
    @Test(groups = {"http-commands"}, dependsOnMethods = {"testCommandCreateJdbcPool"})
    public void testCommandCreateJdbcResource() {
        PayaraServer server = payaraServer();
        Command command = new CommandCreateJDBCResource("myJdbcConnPool", "jdbc/MyDatasource", null, null);
        try {
            Future<ResultString> future
                    = ServerAdmin.<ResultString>exec(server, command);
            try {
                ResultString result = future.get();
                assertNotNull(result.getValue());
                assertTrue(result.getState() == TaskState.COMPLETED);
            } catch (InterruptedException | ExecutionException ie) {
                fail("DeleteResource command execution failed: " + ie.getMessage());
            }
        } catch (PayaraIdeException gfie) {
            fail("DeleteResource command execution failed: " + gfie.getMessage());
        }
    }
    
    /**
     * Test GlasFissh administration HTTP command execution.
     */
    @Test(groups = {"http-commands"}, dependsOnMethods = {"testCommandCreateJdbcResource"})
    public void testListJdbcResources() {
        PayaraServer server = payaraServer();
        Command command = new CommandListResources(
                CommandListResources.command("jdbc-resource"), null);
        try {
            Future<ResultList<String>> future
                    = ServerAdmin.<ResultList<String>>exec(server, command);
            try {
                ResultList<String> result = future.get();
                assertNotNull(result.getValue());
                assertTrue(result.getState() == TaskState.COMPLETED);
                assertTrue(result.getValue().size() > 0);
                assertTrue(result.getValue().contains("jdbc/MyDatasource"));
            } catch (InterruptedException | ExecutionException ie) {
                fail("List resources command execution failed: "
                        + ie.getMessage());
            }
        } catch (PayaraIdeException gfie) {
            fail("List resources command execution failed: "
                    + gfie.getMessage());
        }
    }
    
    /**
     * Test GlasFissh administration HTTP command execution.
     */
    @Test(groups = {"http-commands"}, dependsOnMethods = {"testListJdbcResources"},
            alwaysRun = true)
    public void testCommandDeleteJdbcResource() {
        PayaraServer server = payaraServer();
        Command command = new CommandDeleteResource("jdbc/MyDatasource", "jdbc-resource",
                "poolname", true);
        try {
            Future<ResultString> future
                    = ServerAdmin.<ResultString>exec(server, command);
            try {
                ResultString result = future.get();
                assertNotNull(result.getValue());
                assertTrue(result.getState() == TaskState.COMPLETED);
            } catch (InterruptedException | ExecutionException ie) {
                fail("DeleteResource command execution failed: " + ie.getMessage());
            }
        } catch (PayaraIdeException gfie) {
            fail("DeleteResource command execution failed: " + gfie.getMessage());
        }
    }

    /**
     * Test GlasFissh administration HTTP command execution.
     */
    @Test(groups = {"http-commands"}, dependsOnMethods = {"testCommandDeleteJdbcResource"},
            alwaysRun = true)
    public void testCommandDeleteJdbcPool() {
        PayaraServer server = payaraServer();
        Command command = new CommandDeleteResource("myJdbcConnPool", "jdbc-connection-pool",
                "poolname", true);
        try {
            Future<ResultString> future
                    = ServerAdmin.<ResultString>exec(server, command);
            try {
                ResultString result = future.get();
                assertNotNull(result.getValue());
                assertTrue(result.getState() == TaskState.COMPLETED);
            } catch (InterruptedException | ExecutionException ie) {
                fail("DeleteResource command execution failed: " + ie.getMessage());
            }
        } catch (PayaraIdeException gfie) {
            fail("DeleteResource command execution failed: " + gfie.getMessage());
        }
    }

}
