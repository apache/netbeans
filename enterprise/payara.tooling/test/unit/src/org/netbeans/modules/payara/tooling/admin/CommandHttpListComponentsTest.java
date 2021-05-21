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
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.netbeans.modules.payara.tooling.PayaraIdeException;
import org.netbeans.modules.payara.tooling.data.PayaraServer;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 * Payara list components HTTP command execution test.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
public class CommandHttpListComponentsTest extends CommandHttpTest {

    ////////////////////////////////////////////////////////////////////////////
    // Test methods                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Test GlasFissh administration HTTP command execution.
     */
    @Test(groups = {"http-commands"})
    public void testListComponents() {
        PayaraServer server = payaraServer();
        Command command = new CommandListComponents(null);
        try {
            Future<ResultMap<String, List<String>>> future = 
                    ServerAdmin.<ResultMap<String,
                    List<String>>>exec(server, command);
            try {
                ResultMap<String, List<String>> result = future.get();
                assertNotNull(result.getValue());
                assertTrue(result.getState() == TaskState.COMPLETED);
// This test requires some components to exist.
//                assertTrue(result.getValue().size() > 0);
            } catch (InterruptedException | ExecutionException ie) {
                fail("List components command execution failed: "
                        + ie.getMessage());
            }
        } catch (PayaraIdeException gfie) {
            fail("List components command execution failed: "
                    + gfie.getMessage());
        }
    }
}
