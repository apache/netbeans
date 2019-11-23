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
import static org.netbeans.modules.payara.tooling.admin.CommandRestTest.payaraServer;

/**
 * Tests resources commands via REST interface.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
public class CommandRestResourcesTest extends CommandRestTest {

    private static final String JMS_CONNECTOR_POOL = "pool1";
    private static final String JMS_CONNECTION = "jms/connection2";
    private static final String JDBC_CONNECTION_POOL = "myJdbcConnPool";
    private static final String JDBC_CONNECTION = "jdbc/MyDatasource";
    

    ////////////////////////////////////////////////////////////////////////////
    // Test methods                                                           //
    ////////////////////////////////////////////////////////////////////////////
    
    /**
     * Test GlasFissh administration REST command execution.
     */
    @Test(groups = {"rest-commands"})
    public void testCommandCreateConnectorPool() {
        PayaraServer server = payaraServer();
        Command command = new CommandCreateConnectorConnectionPool(
                JMS_CONNECTOR_POOL, "jmsra", "javax.jms.ConnectionFactory", null);
        try {
            Future<ResultString> future = ServerAdmin.<ResultString>
                    exec(server, command);
            try {
                ResultString result = future.get();
                assertEquals(result.getState(), TaskState.COMPLETED);
            } catch (    InterruptedException | ExecutionException ie) {
                fail("Version command execution failed: " + ie.getMessage());
            }
        } catch (PayaraIdeException gfie) {
            fail("Version command execution failed: " + gfie.getMessage());
        }
    }
    
    /**
     * Test GlasFissh administration REST command execution.
     */
    @Test(groups = {"rest-commands"})
    public void testCommandCreateConnector() {
        PayaraServer server = payaraServer();
        Command command = new CommandCreateConnector(JMS_CONNECTION, TestDomainV4Constants.JMS_CONNECTION_POOL, null);
        try {
            Future<ResultString> future = ServerAdmin.<ResultString>
                    exec(server, command);
            try {
                ResultString result = future.get();
                assertEquals(result.getState(), TaskState.COMPLETED);
            } catch (    InterruptedException | ExecutionException ie) {
                fail("Version command execution failed: " + ie.getMessage());
            }
        } catch (PayaraIdeException gfie) {
            fail("Version command execution failed: " + gfie.getMessage());
        }
    }
    
    /**
     * Test GlasFissh administration REST command execution.
     */
    @Test(groups = {"rest-commands"})
    public void testCommandCreateJDBCConnectionPool() {
        PayaraServer server = payaraServer();
        Command command = new CommandCreateJDBCConnectionPool(JDBC_CONNECTION_POOL, "oracle.jdbc.pool.OracleDataSource", "javax.sql.DataSource", null);
        try {
            Future<ResultString> future = ServerAdmin.<ResultString>
                    exec(server, command);
            try {
                ResultString result = future.get();
                assertEquals(result.getState(), TaskState.COMPLETED);
            } catch (    InterruptedException | ExecutionException ie) {
                fail("Version command execution failed: " + ie.getMessage());
            }
        } catch (PayaraIdeException gfie) {
            fail("Version command execution failed: " + gfie.getMessage());
        }
    }
    
    /**
     * Test GlasFissh administration REST command execution.
     */
    @Test(groups = {"rest-commands"})
    public void testCommandCreateJDBCResource() {
        PayaraServer server = payaraServer();
        Command command = new CommandCreateJDBCResource(TestDomainV4Constants.JDBC_CONNECTION_POOL, JDBC_CONNECTION, null, null);
        try {
            Future<ResultString> future = ServerAdmin.<ResultString>
                    exec(server, command);
            try {
                ResultString result = future.get();
                //assertNotNull(result.getValue());
                assertEquals(result.getState(), TaskState.COMPLETED);
            } catch (    InterruptedException | ExecutionException ie) {
                fail("Version command execution failed: " + ie.getMessage());
            }
        } catch (PayaraIdeException gfie) {
            fail("Version command execution failed: " + gfie.getMessage());
        }
    }
    
    /**
     * Test GlasFissh administration REST command execution.
     */
    @Test(groups = {"rest-commands"}, dependsOnMethods = {"testCommandCreateJDBCResource"})
    public void testCommandListJdbcResources() {
        PayaraServer server = payaraServer();
        Command command = new CommandListResources(CommandListResources.command(
                "jdbc-resource"), null);
        try {
            Future<ResultList<String>> future = ServerAdmin.<ResultList<String>>
                    exec(server, command);
            try {
                ResultList<String> result = future.get();
                assertNotNull(result.getValue());
                assertFalse(result.getValue().isEmpty());
                assertTrue(result.getValue().contains(JDBC_CONNECTION));
            } catch ( InterruptedException | ExecutionException ie) {
                fail("Version command execution failed: " + ie.getMessage());
            }
        } catch (PayaraIdeException gfie) {
            fail("Version command execution failed: " + gfie.getMessage());
        }
    }
    
    /**
     * Test GlasFissh administration HTTP command execution.
     */
    //@Test(groups = {"rest-commands"}, dependsOnMethods = {"testCommandListJdbcResources"})
    public void testCommandDeleteResource() {
        PayaraServer server = payaraServer();
        Command command = new CommandDeleteResource(JDBC_CONNECTION, "jdbc-resource",
                "jdbc_resource_name", true);
        try {
            Future<ResultString> future = 
                    ServerAdmin.<ResultString>exec(server, command);
            try {
                ResultString result = future.get();
                //assertNotNull(result.getValue());
                assertTrue(result.getState() == TaskState.COMPLETED);
            } catch ( InterruptedException | ExecutionException ie) {
                fail("DeleteResource command execution failed: " + ie.getMessage());
            }
        } catch (PayaraIdeException gfie) {
            fail("DeleteResource command execution failed: " + gfie.getMessage());
        }
        
       command = new CommandDeleteResource(JMS_CONNECTOR_POOL, "connector-connection-pool",
                "poolname", true);
        try {
            Future<ResultString> future = 
                    ServerAdmin.<ResultString>exec(server, command);
            try {
                ResultString result = future.get();
                assertNotNull(result.getValue());
                assertTrue(result.getState() == TaskState.COMPLETED);
            } catch ( InterruptedException | ExecutionException ie) {
                fail("DeleteResource command execution failed: " + ie.getMessage());
            }
        } catch (PayaraIdeException gfie) {
            fail("DeleteResource command execution failed: " + gfie.getMessage());
        }
        
        command = new CommandDeleteResource(JDBC_CONNECTION_POOL, "jdbc-connection-pool",
                "poolname", true);
        try {
            Future<ResultString> future = 
                    ServerAdmin.<ResultString>exec(server, command);
            try {
                ResultString result = future.get();
                assertNotNull(result.getValue());
                assertTrue(result.getState() == TaskState.COMPLETED);
            } catch ( InterruptedException | ExecutionException ie) {
                fail("DeleteResource command execution failed: " + ie.getMessage());
            }
        } catch (PayaraIdeException gfie) {
            fail("DeleteResource command execution failed: " + gfie.getMessage());
        }
    }

}
