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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.netbeans.modules.glassfish.tooling.GlassFishIdeException;
import static org.netbeans.modules.glassfish.tooling.admin.CommandHttpTest.glassFishServer;
import org.netbeans.modules.glassfish.tooling.data.GlassFishServer;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 * GlassFish list resources HTTP command execution test.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
public class CommandHttpResourcesTest extends CommandHttpTest {

    // Test methods                                                           //
    /**
     * Test GlasFissh administration HTTP command execution.
     */
    @Test(groups = {"http-commands"})
    public void testCommandCreateJdbcPool() {
        GlassFishServer server = glassFishServer();
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
        } catch (GlassFishIdeException gfie) {
            fail("DeleteResource command execution failed: " + gfie.getMessage());
        }
    }
    
    @Test(groups = {"http-commands"}, dependsOnMethods = {"testCommandCreateJdbcPool"})
    public void testCommandCreateJdbcResource() {
        GlassFishServer server = glassFishServer();
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
        } catch (GlassFishIdeException gfie) {
            fail("DeleteResource command execution failed: " + gfie.getMessage());
        }
    }
    
    /**
     * Test GlasFissh administration HTTP command execution.
     */
    @Test(groups = {"http-commands"}, dependsOnMethods = {"testCommandCreateJdbcResource"})
    public void testListJdbcResources() {
        GlassFishServer server = glassFishServer();
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
        } catch (GlassFishIdeException gfie) {
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
        GlassFishServer server = glassFishServer();
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
        } catch (GlassFishIdeException gfie) {
            fail("DeleteResource command execution failed: " + gfie.getMessage());
        }
    }

    /**
     * Test GlasFissh administration HTTP command execution.
     */
    @Test(groups = {"http-commands"}, dependsOnMethods = {"testCommandDeleteJdbcResource"},
            alwaysRun = true)
    public void testCommandDeleteJdbcPool() {
        GlassFishServer server = glassFishServer();
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
        } catch (GlassFishIdeException gfie) {
            fail("DeleteResource command execution failed: " + gfie.getMessage());
        }
    }

}
