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
import org.netbeans.modules.glassfish.tooling.data.GlassFishServer;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 * This test file should contain all tests of commands that talk
 * to an instance.
 *
 * @author Tomas Kraus, Peter Benedikovic
 */
public class CommandRestInstanceTest extends CommandRestTest {

    private static final String INSTANCE_NAME = "instance1";
    private static final String STANDALONE_INSTANCE = "instance2";
    private static final String NODE_NAME = "localhost-domain1";
    private static final String CLUSTER_NAME = "cluster1";

    ////////////////////////////////////////////////////////////////////////////
    // Test methods                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Test GlasFish start instance command via REST.
     */
    @Test(groups = {"rest-commands"}, dependsOnMethods = {"createStandaloneInstanceTest"})
    public void startInstanceTest() {
        GlassFishServer server = glassFishServer();
        Command command = new CommandStartInstance(STANDALONE_INSTANCE);
        try {
            Future<ResultString> future =
                    ServerAdmin.<ResultString>exec(server, command);
            try {
                ResultString result = future.get();
                //assertNotNull(result.getValue());
                assertEquals(result.state, TaskState.COMPLETED);
            } catch (    InterruptedException | ExecutionException ie) {
                fail("CommandStartInstance command execution failed: " + ie.getMessage());
            }
        } catch (GlassFishIdeException gfie) {
            fail("CommandStartInstance command execution failed: " + gfie.getMessage());
        }
    }
    
    /**
     * Test GlasFish stop instance command via REST.
     */
    @Test(groups = {"rest-commands"}, dependsOnMethods = {"startInstanceTest"})
    public void stopInstanceTest() {
        GlassFishServer server = glassFishServer();
        Command command = new CommandStopInstance(STANDALONE_INSTANCE);
        try {
            Future<ResultString> future =
                    ServerAdmin.<ResultString>exec(server, command);
            try {
                ResultString result = future.get();
                //assertNotNull(result.getValue());
                assertEquals(result.state, TaskState.COMPLETED);
            } catch (    InterruptedException | ExecutionException ie) {
                fail("CommandStopInstance command execution failed: " + ie.getMessage());
            }
        } catch (GlassFishIdeException gfie) {
            fail("CommandStopInstance command execution failed: " + gfie.getMessage());
        }
    }
    
    /**
     * Test GlasFish create standalone instance command via REST.
     */
    @Test(groups = {"rest-commands"})
    public void createStandaloneInstanceTest() {
        GlassFishServer server = glassFishServer();
        Command command = new CommandCreateInstance(STANDALONE_INSTANCE, null,
                TestDomainV4Constants.NODE_NAME);
        try {
            Future<ResultString> future =
                    ServerAdmin.<ResultString>exec(server, command);
            try {
                ResultString result = future.get();
                //assertNotNull(result.getValue());
                assertEquals(result.state, TaskState.COMPLETED);
            } catch (    InterruptedException | ExecutionException ie) {
                fail("CommandCreateInstance command execution failed: " + ie.getMessage());
            }
        } catch (GlassFishIdeException gfie) {
            fail("CommandCreateInstance command execution failed: " + gfie.getMessage());
        }
    }
    
    /**
     * Test GlasFish create instance in a cluster command via REST.
     */
    @Test(groups = {"rest-commands"})
    public void createClusterInstanceTest() {
        GlassFishServer server = glassFishServer();
        Command command = new CommandCreateInstance(INSTANCE_NAME, TestDomainV4Constants.CLUSTER,
                TestDomainV4Constants.NODE_NAME);
        try {
            Future<ResultString> future =
                    ServerAdmin.<ResultString>exec(server, command);
            try {
                ResultString result = future.get();
                assertNotNull(result.getValue());
                assertEquals(result.state, TaskState.COMPLETED);
            } catch (    InterruptedException | ExecutionException ie) {
                fail("CommandCreateInstance command execution failed: " + ie.getMessage());
            }
        } catch (GlassFishIdeException gfie) {
            fail("CommandCreateInstance command execution failed: " + gfie.getMessage());
        }
    }
    
    /**
     * Test GlasFish create instance in a cluster command via REST.
     */
    @Test(groups = {"rest-commands"}, dependsOnMethods = {"stopInstanceTest"})
    public void deleteInstanceTest() {
        GlassFishServer server = glassFishServer();
        Command command = new CommandDeleteInstance(INSTANCE_NAME);
        try {
            Future<ResultString> future =
                    ServerAdmin.<ResultString>exec(server, command);
            try {
                ResultString result = future.get();
                assertNotNull(result.getValue());
                assertEquals(result.state, TaskState.COMPLETED);
            } catch (    InterruptedException | ExecutionException ie) {
                fail("CommandDeleteInstance command execution failed: " + ie.getMessage());
            }
        } catch (GlassFishIdeException gfie) {
            fail("CommandDeleteInstance command execution failed: " + gfie.getMessage());
        }
    }
}
