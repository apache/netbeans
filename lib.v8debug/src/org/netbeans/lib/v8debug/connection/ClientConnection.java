/* 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.lib.v8debug.connection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.ContainerFactory;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.netbeans.lib.v8debug.JSONReader;
import org.netbeans.lib.v8debug.JSONWriter;
import org.netbeans.lib.v8debug.V8Command;
import org.netbeans.lib.v8debug.V8Event;
import org.netbeans.lib.v8debug.V8Request;
import org.netbeans.lib.v8debug.V8Response;
import org.netbeans.lib.v8debug.V8Type;
import static org.netbeans.lib.v8debug.connection.DebuggerConnection.*;

/**
 * A debugger client connection. This is a main client debugger class.
 * Create an instance of this class to connect to a local or remote debugger.
 * <p>
 * The typical usage is:
 * <pre><tt>
 *   final ClientConnection connection = new ClientConnection(hostName, portNumber);
 *   new Thread() {
 *       void run() {
 *           connection.runEventLoop(new ClientConnection.Listener() {
 *               public void header(Map<String, String> properties) {
 *                   // header received
 *               }
 *               public void response(V8Response response) {
 *                   // response received
 *               }
 *               public void event(V8Event event) {
 *                   // event received
 *               }
 *           });
 *       }
 *   }.start();
 *   ...
 *   V8Request request = Continue.createRequest(...);
 *   connection.send(request);
 * </tt></pre>
 * 
 * @author Martin Entlicher
 */
public final class ClientConnection {
    
    private static final Logger LOG = Logger.getLogger(ClientConnection.class.getName());
    
    private final Socket server;
    private final InputStream serverIn;
    private final OutputStream serverOut;
    private final Object outLock = new Object();
    private final byte[] buffer = new byte[BUFFER_SIZE];
    private final ContainerFactory containerFactory = new LinkedJSONContainterFactory();
    private final Set<IOListener> ioListeners = new CopyOnWriteArraySet<>();
    
    /**
     * Create a new client connection.
     * @param serverName the debugger server name. Can be <code>null</code> to connect to localhost.
     * @param serverPort the debugger server port
     * @throws IOException when an IO problem occurs.
     */
    public ClientConnection(String serverName, int serverPort) throws IOException {
        server = new Socket(serverName, serverPort);
        serverIn = server.getInputStream();
        serverOut = server.getOutputStream();
    }

    // For tests
    ClientConnection(InputStream serverIn, OutputStream serverOut) throws IOException {
        this.server = null;
        this.serverIn = serverIn;
        this.serverOut = serverOut;
    }
    
    /**
     * Execute the debugger events loop. Run this in an application thread, this
     * class does not provide any threading. This method blocks until the connection
     * is closed and distributes the debugger events through the provided listener.
     * @param listener The listener to receive the debugger events.
     * @throws IOException thrown when an IO problem occurs.
     */
    public void runEventLoop(Listener listener) throws IOException {
        int n;
        int contentLength = -1;
        int[] beginPos = new int[] { 0 };
        int[] fromPtr = new int[] { 0 };
        int readOffset = 0;
        String tools = null;
        byte[] emptyArray = new byte[] {};
        byte[] messageBytes = emptyArray;
        Map<String, String> header = null;
        int from = 0;
        while ((n = serverIn.read(buffer, readOffset, BUFFER_SIZE - readOffset)) > 0) {
            n += readOffset;
            /*System.err.print("readOffset = "+readOffset+" => n = "+n+" : [");
            for (int ri = readOffset; ri < n; ri++) {
                System.err.print(Integer.toHexString(buffer[ri])+",");
            }
            System.err.println("\b]");*/
            do {
                if (contentLength < 0) {
                    fromPtr[0] = from;
                    
                    contentLength = readContentLength(buffer, fromPtr, n, beginPos);
                    if (contentLength < 0) {
                        break;
                    }
                    if (header == null) {
                        header = readProperties(new String(buffer, from, beginPos[0], CHAR_SET));
                        listener.header(header);
                    }
                    from = fromPtr[0];
                }
                if (tools == null) {
                    fromPtr[0] = from;
                    tools = readTools(buffer, fromPtr, n);
                    if (tools == null) {
                        break;
                    } else {
                        from = fromPtr[0];
                    }
                }
                if (from >= n) {
                    break;
                }
                int length = Math.min(contentLength - messageBytes.length, n - from);
                //System.err.println("buffer.length = "+buffer.length+", from = "+from+", length = "+length);
                //System.err.println("  appending: "+new String(buffer, from, length, CHAR_SET));
                messageBytes = Utils.joinArrays(messageBytes, buffer, from, length);
                from += length;
                if (messageBytes.length == contentLength) {
                    String message = new String(messageBytes, CHAR_SET);
                    try {
                        received(listener, tools, message);
                    } catch (ThreadDeath td) {
                        throw td;
                    } catch (ParseException pex) {
                        throw new IOException(pex.getLocalizedMessage()+" message = '"+message+"'", pex);
                    } catch (Throwable t) {
                        LOG.log(Level.SEVERE, message, t);
                    }
                    contentLength = -1;
                    tools = null;
                    messageBytes = emptyArray;
                }
            } while (from < n);
            if (from < n) {
                System.arraycopy(buffer, from, buffer, 0, n - from);
                readOffset = n - from;
                from = 0;
            } else {
                readOffset = 0;
                from -= n; // from might be > n when there's some overlap of e.g. \r\n
            }
        }
        
    }
    
    private static Map<String, String> readPropertiesScan(String properties) {
        Scanner sp = new Scanner(properties);
        Map<String, String> map = new HashMap<>();
        try {
            while (sp.hasNext()) {
                String key = sp.next(": ");
                String value = sp.next("\r\n");
                map.put(key, value);
            }
        } catch (NoSuchElementException ex) {}
        return map;
    }
    
    private static Map<String, String> readProperties(String properties) {
        Map<String, String> map = new HashMap<>();
        int l = properties.length();
        int pos = 0;
        while (pos < l) {
            int pos2 = properties.indexOf(": ", pos);
            if (pos2 < 0) {
                break;
            }
            String key = properties.substring(pos, pos2).trim();
            pos = pos2 + 2;
            pos2 = properties.indexOf("\r\n", pos);
            if (pos2 < 0) {
                break;
            }
            String value = properties.substring(pos, pos2).trim();
            pos = pos2 + 2;
            map.put(key, value);
        }
        return map;
    }
    
    /**
     * Send a request to the debugger server. The implementation synchronizes the
     * requests, no additional synchronization is necessary.
     * @param request The request to be sent to the debugger server.
     * @throws IOException thrown when an IO problem occurs.
     */
    public void send(V8Request request) throws IOException {
        JSONObject obj = JSONWriter.store(request);
        String text = obj.toJSONString();
        //System.out.println("SEND: "+text);
        fireSent(text);
        LOG.log(Level.FINE, "SEND: {0}", text);
        byte[] bytes = text.getBytes(CHAR_SET);
        String contentLength = CONTENT_LENGTH_STR+bytes.length + "\r\n\r\n";
        synchronized (outLock) {
            serverOut.write(contentLength.getBytes(CHAR_SET));
            serverOut.write(bytes);
        }
    }
    
    /**
     * Close the connection.
     * @throws IOException thrown when an IO problem occurs.
     */
    public void close() throws IOException {
        if (server != null) {
            server.close();
        }
        fireClosed();
    }
    
    /**
     * Test whether the connection is closed.
     * @return <code>true</code> when the connection is already closed,
     * <code>false</code> otherwise.
     */
    public boolean isClosed() {
        if (server != null) {
            return server.isClosed();
        } else {
            return false;
        }
    }
    
    /**
     * Add an I/O listener to monitor the debugger communication.
     * @param iol an IOListener
     */
    public void addIOListener(IOListener iol) {
        ioListeners.add(iol);
    }
    
    /**
     * Remove an I/O listener monitoring the communication.
     * @param iol an IOListener
     */
    public void removeIOListener(IOListener iol) {
        ioListeners.remove(iol);
    }
    
    private void fireSent(String str) {
        for (IOListener iol : ioListeners) {
            iol.sent(str);
        }
    }
    
    private void fireReceived(String str) {
        for (IOListener iol : ioListeners) {
            iol.received(str);
        }
    }
    
    private void fireClosed() {
        for (IOListener iol : ioListeners) {
            iol.closed();
        }
    }
    
    private void received(Listener listener, String tools, String message) throws ParseException {
        //System.out.println("RECEIVED: tools: '"+tools+"', message: '"+message+"'");
        fireReceived(message);
        LOG.log(Level.FINE, "RECEIVED: {0}, {1}", new Object[]{tools, message});
        if (message.isEmpty()) {
            return ;
        }
        JSONParser parser = new JSONParser();
        JSONObject obj = (JSONObject) parser.parse(message, containerFactory);
        //V8Packet packet = V8Packet.get(obj);
        V8Type type = JSONReader.getType(obj);
        switch (type) {
            case event:     V8Event event = JSONReader.getEvent(obj);
                            //System.out.println("event: "+event);
                            listener.event(event);
                            break;
            case response:  V8Response response = JSONReader.getResponse(obj);
                            //System.out.println("response: "+response);
                            listener.response(response);
                            if (V8Command.Disconnect.equals(response.getCommand())) {
                                try {
                                    close();
                                } catch (IOException ioex) {}
                            }
                            break;
            default: throw new IllegalStateException("Wrong type: "+type);
        }
    }
    
    /**
     * Listener receiving debugger events.
     */
    public static interface Listener {
        
        /**
         * Called when the initial header is received.
         * @param properties Properties containing the header information.
         * @see HeaderProperties class.
         */
        void header(Map<String, String> properties);
        
        /**
         * Called when a response is received.
         * @param response The received response.
         */
        void response(V8Response response);
        
        /**
         * Called when an event is received.
         * @param event The received event.
         */
        void event(V8Event event);
    }
    
}
