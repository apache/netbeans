/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2015, 2016 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 */
package org.netbeans.modules.glassfish.tooling.admin;

import org.netbeans.modules.glassfish.tooling.TaskState;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.netbeans.modules.glassfish.tooling.GlassFishIdeException;
import org.netbeans.modules.glassfish.tooling.TestDomainV4Constants;
import static org.netbeans.modules.glassfish.tooling.admin.CommandRestTest.glassFishServer;
import org.netbeans.modules.glassfish.tooling.data.GlassFishServer;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

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
        GlassFishServer server = glassFishServer();
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
        } catch (GlassFishIdeException gfie) {
            fail("Version command execution failed: " + gfie.getMessage());
        }
    }
    
    /**
     * Test GlasFissh administration REST command execution.
     */
    @Test(groups = {"rest-commands"})
    public void testCommandCreateConnector() {
        GlassFishServer server = glassFishServer();
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
        } catch (GlassFishIdeException gfie) {
            fail("Version command execution failed: " + gfie.getMessage());
        }
    }
    
    /**
     * Test GlasFissh administration REST command execution.
     */
    @Test(groups = {"rest-commands"})
    public void testCommandCreateJDBCConnectionPool() {
        GlassFishServer server = glassFishServer();
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
        } catch (GlassFishIdeException gfie) {
            fail("Version command execution failed: " + gfie.getMessage());
        }
    }
    
    /**
     * Test GlasFissh administration REST command execution.
     */
    @Test(groups = {"rest-commands"})
    public void testCommandCreateJDBCResource() {
        GlassFishServer server = glassFishServer();
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
        } catch (GlassFishIdeException gfie) {
            fail("Version command execution failed: " + gfie.getMessage());
        }
    }
    
    /**
     * Test GlasFissh administration REST command execution.
     */
    @Test(groups = {"rest-commands"}, dependsOnMethods = {"testCommandCreateJDBCResource"})
    public void testCommandListJdbcResources() {
        GlassFishServer server = glassFishServer();
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
        } catch (GlassFishIdeException gfie) {
            fail("Version command execution failed: " + gfie.getMessage());
        }
    }
    
    /**
     * Test GlasFissh administration HTTP command execution.
     */
    //@Test(groups = {"rest-commands"}, dependsOnMethods = {"testCommandListJdbcResources"})
    public void testCommandDeleteResource() {
        GlassFishServer server = glassFishServer();
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
        } catch (GlassFishIdeException gfie) {
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
        } catch (GlassFishIdeException gfie) {
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
        } catch (GlassFishIdeException gfie) {
            fail("DeleteResource command execution failed: " + gfie.getMessage());
        }
    }
    
//    @AfterClass(alwaysRun = true)
//    public void cleanResources() throws InterruptedException, ExecutionException {
//        GlassFishServer server = glassFishServer();
//        Command command = new CommandDeleteResource("testConnectionPool", "connector-connection-pool",
//                "poolname", true);
//        ServerAdmin.<ResultString>exec(server, command, ideCtx).get();
//        command = new CommandDeleteResource("myJdbcConnPool", "jdbc-connection-pool",
//                "poolname", true);
//        ServerAdmin.<ResultString>exec(server, command, ideCtx).get();
//    }
}
