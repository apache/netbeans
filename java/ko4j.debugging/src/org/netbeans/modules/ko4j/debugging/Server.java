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
package org.netbeans.modules.ko4j.debugging;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import org.netbeans.modules.web.browser.api.PageInspector;
import org.netbeans.modules.web.webkit.debugging.api.WebKitDebugging;
import org.netbeans.modules.web.webkit.debugging.spi.Factory;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 * 
 * @author Jan Stola
 */
final class Server {
    private static final Server INSTANCE = new Server();
    private ServerSocket socket;

    public static Server getInstance() {
        return INSTANCE;
    }
    
    public int acceptClient() {
        ensureStarted();
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Socket client = socket.accept();
                    MessageDispatcherImpl dispatcher = new MessageDispatcherImpl();
                    Transport transport = new Transport(client, dispatcher);
                    WebKitDebugging webKit = Factory.createWebKitDebugging(transport);
                    Lookup context = Lookups.fixed(transport, webKit, dispatcher);
                    PageInspector.getDefault().inspectPage(context);
                } catch (IOException ioex) {
                    Exceptions.printStackTrace(ioex);
                }
            }
        });
        t.start();
        return socket.getLocalPort();
    }

    private synchronized void ensureStarted() {
        if (socket != null) {
            return;
        }
        try {
            socket = new ServerSocket();
            socket.bind(null);
        } catch (IOException ioex) {
            Exceptions.printStackTrace(ioex);
        }
    }

}
