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
package org.netbeans.modules.acp.api;

import com.agentclientprotocol.sdk.error.AcpProtocolException;
import com.agentclientprotocol.sdk.spec.AcpSchema;
import java.util.List;
import org.netbeans.junit.NbTestCase;

public class AcpTerminalHandlerTest extends NbTestCase {
    public AcpTerminalHandlerTest(String name) {
        super(name);
    }

    public void testFailedLaunchReturnsError() {
        AcpTerminalHandler handler = new AcpTerminalHandler();
        try {
            handler.handleCreateTerminal(new AcpSchema.CreateTerminalRequest(
                    "session", "nonexistent-acp-terminal-command-42", List.of(), null, List.of(), null));
            fail("A failed launch must return a protocol error");
        } catch (AcpProtocolException expected) {
            // Expected.
        } finally {
            handler.releaseAll();
        }
    }

    public void testOutputReportsTruncation() {
        if (!new java.io.File("/bin/sh").canExecute()) {
            return;
        }
        AcpTerminalHandler handler = new AcpTerminalHandler();
        try {
            String id = handler.handleCreateTerminal(new AcpSchema.CreateTerminalRequest(
                    "session", "/bin/sh", List.of("-c", "yes x | head -c 1100000"), null, List.of(), null)).terminalId();
            handler.handleWaitForTerminalExit(new AcpSchema.WaitForTerminalExitRequest("session", id));
            assertTrue(handler.handleTerminalOutput(new AcpSchema.TerminalOutputRequest("session", id)).truncated());
        } finally {
            handler.releaseAll();
        }
    }

    public void testUtf8SplitAcrossWrites() {
        if (!new java.io.File("/bin/sh").canExecute()) {
            return;
        }
        AcpTerminalHandler handler = new AcpTerminalHandler();
        try {
            String id = handler.handleCreateTerminal(new AcpSchema.CreateTerminalRequest(
                    "session", "/bin/sh", List.of("-c", "printf '\\342'; sleep 0.2; printf '\\202\\254'"), null, List.of(), null)).terminalId();
            handler.handleWaitForTerminalExit(new AcpSchema.WaitForTerminalExitRequest("session", id));
            assertEquals("€", handler.handleTerminalOutput(new AcpSchema.TerminalOutputRequest("session", id)).output());
        } finally {
            handler.releaseAll();
        }
    }
}
