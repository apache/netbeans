/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */

package org.netbeans.lib.v8debug.server;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.netbeans.lib.v8debug.V8Body;
import org.netbeans.lib.v8debug.V8Command;
import org.netbeans.lib.v8debug.V8Event;
import org.netbeans.lib.v8debug.V8Request;
import org.netbeans.lib.v8debug.V8Response;
import org.netbeans.lib.v8debug.V8Script;
import org.netbeans.lib.v8debug.V8ScriptLocation;
import org.netbeans.lib.v8debug.commands.Evaluate;
import org.netbeans.lib.v8debug.connection.ClientConnection;
import org.netbeans.lib.v8debug.connection.ServerConnection;
import org.netbeans.lib.v8debug.events.AfterCompileEventBody;
import org.netbeans.lib.v8debug.events.BreakEventBody;
import org.netbeans.lib.v8debug.vars.ReferencedValue;
import org.netbeans.lib.v8debug.vars.V8Number;
import org.netbeans.lib.v8debug.vars.V8String;

/**
 * Stress test of a flood of debugger communication.
 *
 * @author Martin Entlicher
 */
public class StressTest {

    @Test
    public void stressTest() throws IOException, InterruptedException {
        final ServerConnection sn = new ServerConnection();
        int port = sn.getPort();
        ClientConnection cn = new ClientConnection(null, port);
        final StressTestListener listener = new StressTestListener(sn, cn);

        Thread st = new Thread() {
            @Override public void run() {
                try {
                    sn.runConnectionLoop(Collections.EMPTY_MAP, listener);
                } catch (IOException ex) {
                    Logger.getLogger(V8ServerConnectionTest.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
        st.start();
        try { Thread.sleep(500); } catch (InterruptedException iex) {} // Wait for the server to start up
        try {
            cn.runEventLoop(listener);
        } catch (IOException ioex) {
            ioex.printStackTrace();
        }
        st.join();
        Assert.assertTrue(listener.success());
        sn.closeServer();
    }

    private static class StressTestListener implements ClientConnection.Listener, ServerConnection.Listener {

        private static final int NUM_THREADS = 5;
        private static final int NUM_REQUESTS = 2000;

        private final ServerConnection sn;
        private final ClientConnection cn;
        private final Thread[] eventsThread = new Thread[NUM_THREADS];
        private Boolean success = null;

        private StressTestListener(ServerConnection sn, ClientConnection cn) throws IOException {
            this.sn = sn;
            this.cn = cn;
            for (int i = 0; i < NUM_THREADS; i++) {
                eventsThread[i] = new PushEventsThread();
                eventsThread[i].start();
            }
            cn.send(Evaluate.createRequest(0, "Test Eval"));
        }

        @Override
        public void header(Map<String, String> properties) {
        }

        @Override
        public void response(V8Response response) {
            long requestSequence = response.getRequestSequence();
            if (requestSequence < NUM_REQUESTS) {
                requestSequence++;
                try {
                    cn.send(Evaluate.createRequest(requestSequence, "Test Eval"));
                } catch (IOException ex) {
                    success = false;
                    Logger.getLogger(StressTest.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                try {
                    for (int i = 0; i < NUM_THREADS; i++) {
                        eventsThread[i].join();
                    }
                } catch (InterruptedException ex) {
                    Logger.getLogger(StressTest.class.getName()).log(Level.SEVERE, null, ex);
                }
                if (success == null) {
                    success = true;
                }
                try {
                    cn.close();
                    sn.closeCurrentConnection();
                } catch (IOException ex) {
                    Logger.getLogger(StressTest.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        @Override
        public void event(V8Event event) {
            if (!event.isRunning().getValue()) {
                success = false;
                throw new IllegalStateException();
            }
        }

        @Override
        public ServerConnection.ResponseProvider request(V8Request request) {
            if (!request.getCommand().equals(V8Command.Evaluate)) {
                success = false;
                throw new IllegalStateException();
            }
            V8Body eBody = new Evaluate.ResponseBody(new V8String(321, "Test value", "test value"));
            return ServerConnection.ResponseProvider.create(request.createSuccessResponse(10l, eBody, new ReferencedValue[0], true));
        }

        private boolean success() {
            return Boolean.TRUE.equals(success);
        }

        private class PushEventsThread extends Thread {

            private static final int NUM_EVENTS = 2000;

            private final ReferencedValue[] referencedValues = new ReferencedValue[] {
                new ReferencedValue(333, new V8Number(33, 33l, "33"))
            };

            @Override
            public void run() {
                try { Thread.sleep(500); } catch (InterruptedException iex) {} // Wait for the server to start up
                try {
                    for (int i = 0; i < NUM_EVENTS; i++) {
                        V8Event event;
                        if ((i & 0x1) == 0) {
                            V8Body acBody = new AfterCompileEventBody(new V8Script(NUM_EVENTS + i, "Test"+Integer.toBinaryString(i), i, 0, 0, 1000, null, null, "test", Long.MAX_VALUE, null, "test text", V8Script.Type.NORMAL, V8Script.CompilationType.API, null, null));
                            event = new V8Event(i, V8Event.Kind.AfterCompile, acBody, referencedValues, Boolean.TRUE, Boolean.TRUE, null);
                        } else {
                            V8Body bBody = new BreakEventBody("invocation", i, i, "invocation source", new V8ScriptLocation(i, "script.js", i, i, NUM_EVENTS), new long[] { i >> 1 });
                            event = new V8Event(i, V8Event.Kind.Break, bBody, referencedValues, Boolean.TRUE, Boolean.TRUE, null);
                        }
                        sn.send(event);
                    }
                } catch (IOException ex) {
                    Logger.getLogger(StressTest.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }
    }
}
