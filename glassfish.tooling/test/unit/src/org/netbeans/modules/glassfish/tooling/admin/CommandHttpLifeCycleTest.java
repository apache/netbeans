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
import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.netbeans.modules.glassfish.tooling.GlassFishIdeException;
import org.netbeans.modules.glassfish.tooling.data.GlassFishServer;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.fail;
import org.testng.annotations.Test;

/**
 * GlassFish domain life cycle HTTP command execution test.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
public class CommandHttpLifeCycleTest extends CommandHttpTest {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    private static final String CLUSTER_NAME = "ToolingCluster";
    private static final String INSTANCE_NAME = "ToolingInstance";

    ////////////////////////////////////////////////////////////////////////////
    // Test methods                                                           //
    ////////////////////////////////////////////////////////////////////////////
/*
 * TODO: Not yes implemented:
 * http://localhost:4848/__asadmin/list-nodes
 * http://localhost:4848/__asadmin/create-instance?DEFAULT=i&node=localhost-domain1&cluster=a
 */
    /**
     * Test GlasFissh create-cluster administration HTTP command execution.
     */
    @Test(groups = {"http-commands"})
    public void testCreateCluster() {
        GlassFishServer server = glassFishServer();
        Command command = new CommandCreateCluster(CLUSTER_NAME);
        ResultString result = null;
        String value = null;
        try {
            Future<ResultString> future = 
                    ServerAdmin.<ResultString>exec(server, command);
            try {
                result = future.get();
                value = result.getValue();
            } catch (InterruptedException | ExecutionException ie) {
                fail("FetchLogData command execution failed: " + ie.getMessage());
            }
        } catch (GlassFishIdeException gfie) {
            fail("FetchLogData command execution failed: " + gfie.getMessage());
        }
        if (result == null || result.getState() != TaskState.COMPLETED) {
            fail(value);
        }
        assertNotNull(value);
    }

    /**
     * Test GlasFissh create-cluster administration HTTP command execution.
     */
    @Test(groups = {"http-commands"}, dependsOnMethods = {"testCreateCluster"})
    public void testCreateClusterInstance() {
        GlassFishServer server = glassFishServer();
        Command command = new CommandCreateInstance(INSTANCE_NAME, CLUSTER_NAME, "localhost-" + server.getDomainName());
        ResultString result = null;
        String value = null;
        try {
            Future<ResultString> future = 
                    ServerAdmin.<ResultString>exec(server, command);
            try {
                result = future.get();
                value = result.getValue();
            } catch (InterruptedException | ExecutionException ie) {
                fail("FetchLogData command execution failed: " + ie.getMessage());
            }
        } catch (GlassFishIdeException gfie) {
            fail("FetchLogData command execution failed: " + gfie.getMessage());
        }
        if (result == null || result.getState() != TaskState.COMPLETED) {
            fail(value);
        }
        assertNotNull(value);
    }

    /**
     * Test GlasFissh deploy administration HTTP command execution.
     */
    @Test(groups = {"http-commands"}, dependsOnMethods = {"testCreateClusterInstance"})
    public void testDeployWebApplication() {
        GlassFishServer server = glassFishServer();
        Command command = new CommandDeploy("Test", CLUSTER_NAME, new File("target/test/simpleWeb.war"), "simpleWeb", null, null);
        ResultString result = null;
        String value = null;
        try {
            Future<ResultString> future = 
                    ServerAdmin.<ResultString>exec(server, command);
            try {
                result = future.get();
                value = result.getValue();
            } catch (InterruptedException | ExecutionException ie) {
                fail("FetchLogData command execution failed: " + ie.getMessage());
            }
        } catch (GlassFishIdeException gfie) {
            fail("FetchLogData command execution failed: " + gfie.getMessage());
        }
        if (result == null || result.getState() != TaskState.COMPLETED) {
            fail(value);
        }
        assertNotNull(value);
    }
    
    /**
     * Test GlasFissh deploy administration HTTP command execution.
     */
    @Test(groups = {"http-commands"}, dependsOnMethods = {"testDeployWebApplication"})
    public void testUndeployWebApplication() {
        GlassFishServer server = glassFishServer();
        Command command = new CommandUndeploy("Test", CLUSTER_NAME);
        ResultString result = null;
        String value = null;
        try {
            Future<ResultString> future = 
                    ServerAdmin.<ResultString>exec(server, command);
            try {
                result = future.get();
                value = result.getValue();
            } catch (InterruptedException | ExecutionException ie) {
                fail("FetchLogData command execution failed: " + ie.getMessage());
            }
        } catch (GlassFishIdeException gfie) {
            fail("FetchLogData command execution failed: " + gfie.getMessage());
        }
        if (result == null || result.getState() != TaskState.COMPLETED) {
            fail(value);
        }
        assertNotNull(value);
    }

    /**
     * Test GlasFissh create-cluster administration HTTP command execution.
     */
    @Test(groups = {"http-commands"}, alwaysRun = true, dependsOnMethods = {"testUndeployWebApplication"})
    public void testDeleteClusterInstance() {
        GlassFishServer server = glassFishServer();
        Command command = new CommandDeleteInstance(INSTANCE_NAME);
        ResultString result = null;
        String value = null;
        try {
            Future<ResultString> future = 
                    ServerAdmin.<ResultString>exec(server, command);
            try {
                result = future.get();
                value = result.getValue();
            } catch (InterruptedException | ExecutionException ie) {
                fail("FetchLogData command execution failed: " + ie.getMessage());
            }
        } catch (GlassFishIdeException gfie) {
            fail("FetchLogData command execution failed: " + gfie.getMessage());
        }
        if (result == null || result.getState() != TaskState.COMPLETED) {
            fail(value);
        }
        assertNotNull(value);
    }

    /**
     * Test GlasFissh create-cluster administration HTTP command execution.
     */
    @Test(groups = {"http-commands"}, alwaysRun = true, dependsOnMethods = {"testDeleteClusterInstance"})
    public void testDeleteCluster() {
        GlassFishServer server = glassFishServer();
        Command command = new CommandDeleteCluster(CLUSTER_NAME);
        ResultString result = null;
        String value = null;
        try {
            Future<ResultString> future = 
                    ServerAdmin.<ResultString>exec(server, command);
            try {
                result = future.get();
                value = result.getValue();
            } catch (InterruptedException | ExecutionException ie) {
                fail("FetchLogData command execution failed: " + ie.getMessage());
            }
        } catch (GlassFishIdeException gfie) {
            fail("FetchLogData command execution failed: " + gfie.getMessage());
        }
        if (result == null || result.getState() != TaskState.COMPLETED) {
            fail(value);
        }
        assertNotNull(value);
    }

}
