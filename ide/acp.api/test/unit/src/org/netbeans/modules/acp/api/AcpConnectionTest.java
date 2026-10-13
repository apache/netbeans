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

import java.util.List;
import org.netbeans.junit.NbTestCase;

public class AcpConnectionTest extends NbTestCase {

    public AcpConnectionTest(String name) {
        super(name);
    }

    private AcpAgentDescriptor createDescriptor() {
        return new AcpAgentDescriptor.Builder("test-agent",
                AcpAgentDescriptor.TransportType.STDIO)
                .command(List.of("echo", "hello"))
                .build();
    }

    public void testConstructorRejectsNull() {
        try {
            new AcpConnection(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException ex) {
            // expected
        }
    }

    public void testGetDescriptor() {
        AcpAgentDescriptor desc = createDescriptor();
        AcpConnection conn = new AcpConnection(desc);
        assertSame(desc, conn.getDescriptor());
    }

    public void testInitiallyNotConnected() {
        AcpConnection conn = new AcpConnection(createDescriptor());
        assertFalse(conn.isConnected());
    }

    public void testCloseOnDisconnectedIsHarmless() {
        AcpConnection conn = new AcpConnection(createDescriptor());
        conn.close();
        assertFalse(conn.isConnected());
    }

    public void testAddListenerRejectsNull() {
        AcpConnection conn = new AcpConnection(createDescriptor());
        try {
            conn.addSessionListener(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException ex) {
            // expected
        }
    }

    public void testRemoveListenerAcceptsUnknown() {
        AcpConnection conn = new AcpConnection(createDescriptor());
        AcpSessionListener listener = new AcpSessionListener() {};
        conn.removeSessionListener(listener);
    }

    public void testSendPromptWithoutConnectionThrows() {
        AcpConnection conn = new AcpConnection(createDescriptor());
        try {
            conn.sendPrompt("session-1", "hello");
            fail("Expected IllegalStateException");
        } catch (IllegalStateException ex) {
            assertTrue(ex.getMessage().contains("Not connected"));
        } catch (Exception ex) {
            fail("Unexpected exception: " + ex);
        }
    }

    public void testCancelWithoutConnectionThrows() {
        AcpConnection conn = new AcpConnection(createDescriptor());
        try {
            conn.cancel("session-1");
            fail("Expected IllegalStateException");
        } catch (IllegalStateException ex) {
            assertTrue(ex.getMessage().contains("Not connected"));
        } catch (Exception ex) {
            fail("Unexpected exception: " + ex);
        }
    }

    public void testNewSessionWithoutConnectionThrows() {
        AcpConnection conn = new AcpConnection(createDescriptor());
        try {
            conn.newSession("/tmp");
            fail("Expected IllegalStateException");
        } catch (IllegalStateException ex) {
            assertTrue(ex.getMessage().contains("Not connected"));
        } catch (Exception ex) {
            fail("Unexpected exception: " + ex);
        }
    }

    public void testAllowToolForSession() {
        AcpConnection conn = new AcpConnection(createDescriptor());
        conn.allowToolForSession("s1", "read_file");
        conn.allowToolForSession("s1", "write_to_file");
    }

    public void testAllowToolRejectsNullSession() {
        AcpConnection conn = new AcpConnection(createDescriptor());
        try {
            conn.allowToolForSession(null, "read_file");
            fail("Expected NullPointerException");
        } catch (NullPointerException ex) {
            // expected
        }
    }

    public void testSetPermissionHandler() {
        AcpConnection conn = new AcpConnection(createDescriptor());
        conn.setPermissionHandler(req -> null);
        conn.setPermissionHandler(null);
    }
}
