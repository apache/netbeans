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

/**
 * Payara list web services REST command execution test.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
public class CommandRestListWebServicesTest extends CommandRestTest {

    ////////////////////////////////////////////////////////////////////////////
    // Test methods                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Test GlasFissh administration REST command execution.
     */
    @Test(groups = {"rest-commands"})
    public void testListWebServices() {
        PayaraServer server = payaraServer();
        Command command = new CommandListWebServices();
        try {
            Future<ResultList<String>> future = 
                    ServerAdmin.<ResultList<String>>exec(server, command);
            try {
                ResultList<String> result = future.get();
                //assertNotNull(result.getValue());
                assertEquals(result.getState(), TaskState.COMPLETED);
//                assertTrue(result.getValue().size() > 0);
            } catch (    InterruptedException | ExecutionException ie) {
                fail("List resources command execution failed: "
                        + ie.getMessage());
            }
        } catch (PayaraIdeException gfie) {
            fail("List resources command execution failed: "
                    + gfie.getMessage());
        }
    }
}
