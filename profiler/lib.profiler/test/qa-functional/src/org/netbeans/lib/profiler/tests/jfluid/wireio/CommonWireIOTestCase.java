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

package org.netbeans.lib.profiler.tests.jfluid.wireio;

import org.netbeans.lib.profiler.tests.jfluid.CommonProfilerTestCase;
import org.netbeans.lib.profiler.wireprotocol.Command;
import org.netbeans.lib.profiler.wireprotocol.WireIO;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;


/**
 *
 * @author ehucka
 */
public class CommonWireIOTestCase extends CommonProfilerTestCase {
    //~ Inner Classes ------------------------------------------------------------------------------------------------------------

    protected class LoggingThread extends Thread {
        //~ Instance fields ------------------------------------------------------------------------------------------------------

        boolean prepared = false;
        private boolean running = true;

        //~ Methods --------------------------------------------------------------------------------------------------------------

        public boolean isPrepared() {
            return prepared;
        }

        public void setRunning(boolean running) {
            this.running = running;
        }

        public boolean isRunning() {
            return running;
        }

        @Override
        public void run() {
            try {
                ServerSocket serverSocket = new ServerSocket(PORT);
                ref("Server start to listen on port " + String.valueOf(PORT));
                prepared = true;

                Socket clientSocket = serverSocket.accept();

                WireIO wireIO = createWireIO(clientSocket);

                while (running) {
                    running &= simpleLogCommands(wireIO);
                }

                serverSocket.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    protected static int PORT = 5140;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    /** Creates a new instance of CommonWireIOTestCase */
    public CommonWireIOTestCase(String name) {
        super(name);
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public WireIO createWireIO(Socket clientSocket) {
        try {
            clientSocket.setTcpNoDelay(true); // Necessary at least on Solaris to avoid delays in e.g. readInt() etc.

            ObjectInputStream socketIn = new ObjectInputStream(clientSocket.getInputStream());
            ObjectOutputStream socketOut = new ObjectOutputStream(clientSocket.getOutputStream());
            WireIO wireIO = new WireIO(socketOut, socketIn);

            return wireIO;
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public WireIO createWireIOClient(Socket clientSocket) {
        try {
            clientSocket.setSoTimeout(0);
            clientSocket.setTcpNoDelay(true); // Necessary at least on Solaris to avoid delays in e.g. readInt() etc.

            ObjectOutputStream socketOut = new ObjectOutputStream(clientSocket.getOutputStream());
            ObjectInputStream socketIn = new ObjectInputStream(clientSocket.getInputStream());
            WireIO wireIO = new WireIO(socketOut, socketIn);

            return wireIO;
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public boolean simpleLogCommands(WireIO wireIO) {
        try {
            Object o = wireIO.receiveCommandOrResponse();

            if (o == null) {
                ref("Connection interrupted.");

                return false;
            } else {
                if (o instanceof Command) {
                    ref(" received command " + o.toString());
                } else {
                    ref(" received object " + o.getClass().getName() + " " + o.toString());
                }
            }
        } catch (IOException ex) {
            return false;
        }

        return true;
    }
}
