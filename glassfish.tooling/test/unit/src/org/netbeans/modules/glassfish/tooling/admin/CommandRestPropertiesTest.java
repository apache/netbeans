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
import static org.netbeans.modules.glassfish.tooling.admin.CommandRestTest.glassFishServer;
import org.netbeans.modules.glassfish.tooling.data.GlassFishServer;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 * GlassFish get property REST command execution test.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
public class CommandRestPropertiesTest extends CommandRestTest {

    ////////////////////////////////////////////////////////////////////////////
    // Test methods                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Test GlasFissh administration HTTP command execution.
     */
    @Test(groups = {"rest-commands"})
    public void testCommandGetProperty() {
        GlassFishServer server = glassFishServer();
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
        } catch (GlassFishIdeException gfie) {
            fail("get property command execution failed: " + gfie.getMessage());
        }
    }
    
    /**
     * Test GlasFissh administration HTTP command execution.
     */
    @Test(groups = {"rest-commands"})
    public void testCommandSetProperty() {
        GlassFishServer server = glassFishServer();
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
        } catch (GlassFishIdeException gfie) {
            fail("SetProperty command execution failed: " + gfie.getMessage());
        }
    }

}
