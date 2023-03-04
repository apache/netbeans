/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.lib.v8debug.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.netbeans.lib.v8debug.PropertyLong;
import org.netbeans.lib.v8debug.V8Arguments;
import org.netbeans.lib.v8debug.V8Body;
import org.netbeans.lib.v8debug.V8Command;
import org.netbeans.lib.v8debug.V8Event;
import org.netbeans.lib.v8debug.V8Frame;
import org.netbeans.lib.v8debug.V8Request;
import org.netbeans.lib.v8debug.V8Response;
import org.netbeans.lib.v8debug.V8Scope;
import org.netbeans.lib.v8debug.commands.Backtrace;
import org.netbeans.lib.v8debug.commands.ChangeBreakpoint;
import org.netbeans.lib.v8debug.commands.ChangeLive;
import org.netbeans.lib.v8debug.commands.ClearBreakpoint;
import org.netbeans.lib.v8debug.connection.ClientConnection;
import org.netbeans.lib.v8debug.connection.ServerConnection;
import org.netbeans.lib.v8debug.vars.ReferencedValue;
import org.netbeans.lib.v8debug.vars.V8Function;
import org.netbeans.lib.v8debug.vars.V8Number;
import org.netbeans.lib.v8debug.vars.V8String;
import org.netbeans.lib.v8debug.vars.V8Value;

/**
 *
 * @author Martin Entlicher
 */
public class V8ServerCommandsTest {
    
    private List<CMD> commands;
    
    private void fillCommands() {
        commands = new ArrayList<>();
        PropertyLong pln = new PropertyLong(null);
        ReferencedValue<V8Function> fnc = new ReferencedValue<>(333, new V8Function(111, pln, pln, pln, "testFunction", null, true, "src", pln, 22l, pln, pln, pln, null, null, null));
        commands.add(new CMD(V8Command.Backtrace,
                     new Backtrace.Arguments(null, null, null, null),
                     new Backtrace.ResponseBody(0l, 2l, 3l, new V8Frame[] {
                         new V8Frame(0l, new ReferencedValue(22, new V8Number(22, 10l, "10")), fnc, 2l, false, true, true, null, null, 30, 5, 3, " source line;", new V8Scope[0], null),
                         new V8Frame(1l, new ReferencedValue(23, new V8String(23, "10s", "10s")), fnc, 2l, false, true, true, null, null, 30, 5, 3, " source line;", new V8Scope[0], null),
                         new V8Frame(2l, new ReferencedValue(24, new V8Value(24, V8Value.Type.Null, null)), fnc, 2l, false, true, true, null, null, 30, 5, 3, " source line;", new V8Scope[0], null)
                     })));
        commands.add(new CMD(V8Command.Changebreakpoint,
                             new ChangeBreakpoint.Arguments(5, Boolean.TRUE, null, 0l),
                             null
                     ));
        /*
        commands.add(new CMD(V8Command.Changelive,
                             new ChangeLive.Arguments(44, "ghjkasjdhfg", Boolean.TRUE),
                             new ChangeLive.ResponseBody(new ChangeLive.ChangeLog, null, Boolean.TRUE)
                     ));
                */
        commands.add(new CMD(V8Command.Clearbreakpoint,
                             new ClearBreakpoint.Arguments(5),
                             new ClearBreakpoint.ResponseBody(5)
                     ));
    }
    
    @Test
    public void testCommands() throws IOException, InterruptedException {
        fillCommands();
        final ServerConnection sn = new ServerConnection();
        int port = sn.getPort();
        ClientConnection cn = new ClientConnection(null, port);
        final CommandsListener listener = new CommandsListener(sn, cn);
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
        Assert.assertTrue(listener.getError(), listener.success());
        sn.closeServer();
        Assert.assertTrue(commands.toString(), commands.isEmpty());
    }
    
    private class CommandsListener implements ServerConnection.Listener, ClientConnection.Listener {

        private final ServerConnection sn;
        private final ClientConnection cn;

        private long reqSeq = 0;
        private long respSeq = 0;
        private boolean success = true;
        private String error = "";
        
        private CommandsListener(ServerConnection sn, ClientConnection cn) {
            this.sn = sn;
            this.cn = cn;
        }
        
        @Override
        public ServerConnection.ResponseProvider request(V8Request request) {
            CMD cmd = commands.get(commands.size() - 1);
            if (reqSeq != request.getSequence()) {
                success = false;
                error = "Server: Sequence mismatch";
            }
            V8Response response = request.createSuccessResponse(++respSeq, cmd.resp, null, true);
            return ServerConnection.ResponseProvider.create(response);
        }

        @Override
        public void header(Map<String, String> properties) {
            sendNextRequest();
        }
        
        private void sendNextRequest() {
            CMD cmd = commands.get(commands.size() - 1);
            try {
                cn.send(new V8Request(++reqSeq, cmd.cmd, cmd.args));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        @Override
        public void response(V8Response response) {
            if (response.getRequestSequence() != reqSeq) {
                success = false;
                error = "Client: Request Sequence mismatch";
            }
            if (response.getSequence() != respSeq) {
                success = false;
                error = "Client: Response Sequence mismatch";
            }
            CMD cmd = commands.remove(commands.size() - 1);
            if (!cmd.cmd.equals(response.getCommand())) {
                success = false;
                error = "Command mismatch";
            }
            if (cmd.resp == null && response.getBody() != null) {
                success = false;
                error = "Body not null mismatch";
            }
            if (cmd.resp != null) {
                checkBodies(cmd.resp, response.getBody());
            }
            if (commands.isEmpty() || !success) {
                try {
                    cn.close();
                    sn.closeCurrentConnection();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            } else {
                sendNextRequest();
            }
        }

        private void checkBodies(V8Body b1, V8Body b2) {
            if (!b1.getClass().equals(b2.getClass())) {
                success = false;
                error = "Different body classes; expecting: "+b1.getClass()+", got: "+b2.getClass();
            }
        }
        
        @Override
        public void event(V8Event event) {
            
        }
        
        public boolean success() {
            return success;
        }
        
        public String getError() {
            return error;
        }

    }
    
    private static class CMD {
        
        public final V8Command cmd;
        public final V8Arguments args;
        public final V8Body resp;
        
        public CMD(V8Command cmd, V8Arguments args, V8Body resp) {
            this.cmd = cmd;
            this.args = args;
            this.resp = resp;
        }
        
    }
}
