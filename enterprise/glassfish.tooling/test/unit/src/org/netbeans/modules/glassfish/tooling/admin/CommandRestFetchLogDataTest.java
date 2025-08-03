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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.netbeans.modules.glassfish.tooling.GlassFishIdeException;
import org.netbeans.modules.glassfish.tooling.data.GlassFishServer;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.fail;
import org.testng.annotations.Test;

/**
 * GlassFish view-log HTTP command execution test.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
public class CommandRestFetchLogDataTest extends CommandRestTest {

    // Test methods                                                           //
    /**
     * Test GlasFissh view-log administration HTTP command execution.
     */
    @Test(groups = {"rest-commands"})
    public void testCommandFetchLogData() {
        GlassFishServer server = glassFishServer();
        Command command = new CommandFetchLogData();
        try {
            Future<ResultLog> future =
                    ServerAdmin.<ResultLog>exec(server, command);
            try {
                ResultLog result = future.get();
                assertNotNull(result.getValue());
            } catch (    InterruptedException | ExecutionException ie) {
                fail("FetchLogData command execution failed: " + ie.getMessage());
            }
        } catch (GlassFishIdeException gfie) {
            fail("FetchLogData command execution failed: " + gfie.getMessage());
        }
    }

}
