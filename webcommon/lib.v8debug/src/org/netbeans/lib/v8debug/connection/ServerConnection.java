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

package org.netbeans.lib.v8debug.connection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
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
import static org.netbeans.lib.v8debug.connection.DebuggerConnection.*;

/**
 * A debugger server connection. This is a main server debugger class.
 * Create an instance of this class to listen for incoming debugger connections.
 * <p>
 * The typical usage is:
 * <pre><code>
 *   final ServerConnection sn = new ServerConnection();
 *   final Map&lt;String, String&gt; properties = ... // See HeaderProperties.
 *   new Thread() {
 *       public void run() {
 *           try {
 *               sn.runConnectionLoop(map, new ServerConnection.Listener() {
 *                   public ServerConnection.ResponseProvider request(V8Request request) {
 *                       return ServerConnection.ResponseProvider.create(
 *                           request.createSuccessResponse(...));
 *                   }
 *               });
 *           } catch (IOException ex) {
 *               ...
 *           }
 *      }
 *   }.start();
 *   ...
 *   V8Event event = new V8Event(...);
 *   sn.send(event);
 * </code></pre>
 * 
 * @author Martin Entlicher
 */
public final class ServerConnection {
    
    private static final Logger LOG = Logger.getLogger(ServerConnection.class.getName());
    
    private static final String SERVER_PROTOCOL_VERSION = "1";
    
    private final ServerSocket server;
    private Socket currentSocket;
    private InputStream clientIn;
    private OutputStream clientOut;
    private final Object outLock = new Object();
    private final byte[] buffer = new byte[BUFFER_SIZE];
    private final ContainerFactory containerFactory = new LinkedJSONContainterFactory();
    private final Set<IOListener> ioListeners = new CopyOnWriteArraySet<>();
    
    /**
     * Create a new server listening connection with automatically selected port
     * number. The actual port number that was selected can be retrieved by
     * calling {@link #getPort()}.
     * @throws IOException when an IO problem occurs.
     */
    public ServerConnection() throws IOException {
        server = new ServerSocket(0);
    }
    
    /**
     * Create a new server listening connection on the specific port.
     * @param serverPort The port the connection is listening on.
     * @throws IOException when an IO problem occurs.
     * @throws IllegalArgumentException if the port parameter is outside the
     * specified range of valid port values, which is between 0 and 65535, inclusive.
     */
    public ServerConnection(int serverPort) throws IOException, IllegalArgumentException {
        server = new ServerSocket(serverPort);
    }
    
    /**
     * Execute the debugger events loop. Run this in an application thread, this
     * class does not provide any threading. This method waits until some client
     * debugger is connected, keeps processing the debugger requests and keeps
     * blocking until the connection is closed. Then the method can be called
     * again to accept another client debugger connection.
     * @param properties The map of properties to provide to the client as a header.
     * See {@link HeaderProperties}
     * @param listener The listener to receive the debugger events.
     * @throws IOException thrown when an IO problem occurs.
     */
    public void runConnectionLoop(Map<String, String> properties, Listener listener) throws IOException {
        Socket socket = server.accept();
        socket.setTcpNoDelay(true);
        currentSocket = socket;
        clientIn = socket.getInputStream();
        clientOut = socket.getOutputStream();
        sendProperties(properties);
        runEventLoop(listener);
    }
    
    /**
     * Get the port number this connection is listening on.
     * @return The port number.
     */
    public int getPort() {
        return server.getLocalPort();
    }
    
    private void runEventLoop(Listener listener) throws IOException {
        int n;
        int contentLength = -1;
        int[] beginPos = new int[] { 0 };
        int[] fromPtr = new int[] { 0 };
        int readOffset = 0;
        String tools = null;
        byte[] emptyArray = new byte[] {};
        byte[] messageBytes = emptyArray;
        while ((n = clientIn.read(buffer, readOffset, BUFFER_SIZE - readOffset)) > 0) {
            n += readOffset;
            int from = 0;
            do {
                if (contentLength < 0) {
                    fromPtr[0] = from;
                    
                    contentLength = readContentLength(buffer, fromPtr, n, beginPos);
                    if (contentLength < 0) {
                        break;
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
                int length = Math.min(contentLength - messageBytes.length, n - from);
                messageBytes = Utils.joinArrays(messageBytes, buffer, from, length);
                from += length;
                if (messageBytes.length == contentLength) {
                    String message = new String(messageBytes, CHAR_SET);
                    try {
                        received(listener, tools, message);
                    } catch (ThreadDeath td) {
                        throw td;
                    } catch (ParseException pex) {
                        throw new IOException(pex.getLocalizedMessage(), pex);
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
            } else {
                readOffset = 0;
            }
        }
        
    }
    
    private void received(Listener listener, String tools, String message) throws ParseException, IOException {
        //System.out.println("RECEIVED: tools: '"+tools+"', message: '"+message+"'");
        fireReceived(message);
        LOG.log(Level.FINE, "RECEIVED: {0}, {1}", new Object[]{tools, message});
        if (message.isEmpty()) {
            return ;
        }
        JSONParser parser = new JSONParser();
        JSONObject obj = (JSONObject) parser.parse(message, containerFactory);
        V8Request request = JSONReader.getRequest(obj);
        ResponseProvider rp = listener.request(request);
        if (V8Command.Disconnect.equals(request.getCommand())) {
            try {
                closeCurrentConnection();
            } catch (IOException ioex) {}
        }
        if (rp != null) {
            rp.sendTo(this);
        }
    }
    
    private void sendProperties(Map<String, String> properties) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> prop : properties.entrySet()) {
            sb.append(prop.getKey());
            sb.append(": ");
            sb.append(prop.getValue());
            sb.append(EOL_STR);
        }
        if (!properties.containsKey(HeaderProperties.PROTOCOL_VERSION)) {
            sb.append(HeaderProperties.PROTOCOL_VERSION +
                      ": "+SERVER_PROTOCOL_VERSION +
                      EOL_STR);
        }
        sb.append(CONTENT_LENGTH_STR+"0" + EOL_STR + EOL_STR);
        byte[] bytes = sb.toString().getBytes(CHAR_SET);
        synchronized (outLock) {
            clientOut.write(bytes);
        }
    }
    
    private void send(V8Response response) throws IOException {
        JSONObject obj = JSONWriter.store(response);
        sendJSON(obj);
    }
    
    /**
     * Send a debugger event.
     * @param event An event to send to the client.
     * @throws IOException thrown when an IO problem occurs.
     */
    public void send(V8Event event) throws IOException {
        JSONObject obj = JSONWriter.store(event);
        sendJSON(obj);
    }
    
    private void sendJSON(JSONObject obj) throws IOException {
        String text = obj.toJSONString();
        text = text.replace("\\/", "/"); // Replace escaped slash "\/" with shash "/". Unescape slashes.
        //System.out.println("SEND: "+text);
        fireSent(text);
        LOG.log(Level.FINE, "SEND: {0}", text);
        byte[] bytes = text.getBytes(CHAR_SET);
        String contentLength = CONTENT_LENGTH_STR+bytes.length + EOL_STR + EOL_STR;
        synchronized (outLock) {
            if (clientOut == null) {
                throw new IOException("No client connection is opened.");
            }
            clientOut.write(contentLength.getBytes(CHAR_SET));
            clientOut.write(bytes);
        }
    }
    
    public boolean isConnected() {
        return currentSocket != null && clientOut != null;
    }
    
    /**
     * Close the currently established client-server connection, if any.
     * The {@link #runConnectionLoop(java.util.Map, org.netbeans.lib.v8debug.connection.ServerConnection.Listener)}
     * can then be executed again to accept another client connection.
     * @throws IOException thrown when an IO problem occurs.
     */
    public void closeCurrentConnection() throws IOException {
        if (currentSocket != null) {
            currentSocket.close();
            currentSocket = null;
        }
    }

    /**
     * Close the server connection. Stop accepting incoming connections.
     * @throws IOException thrown when an IO problem occurs.
     */
    public void closeServer() throws IOException {
        if (server != null) {
            server.close();
        }
        fireClosed();
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
    
    /**
     * Listener receiving debugger events.
     */
    public interface Listener {
        
        /**
         * Called when a request is received. The implementation should compose
         * a response and provide it either synchronously or asynchronously via
         * the returned {@link ResponseProvider}.
         * @param request The received request.
         * @return The response provider allowing either synchronous or asynchronous response.
         */
        ResponseProvider request(V8Request request);
        
    }
    
    /**
     * Debugger response provider. Allows synchronous or asynchronous responses.
     * Create the response by calling
     * {@link V8Request#createSuccessResponse(long, org.netbeans.lib.v8debug.V8Body, org.netbeans.lib.v8debug.vars.ReferencedValue[], boolean)}
     * or {@link V8Request#createErrorResponse(long, boolean, java.lang.String)}.
     */
    public static final class ResponseProvider {
        
        private V8Response response;
        private ServerConnection sc;
        
        private ResponseProvider(V8Response response) {
            this.response = response;
        }
        
        /**
         * Create a synchronous response to a debugger request.
         * @param response The response.
         * Use {@link V8Request#createSuccessResponse(long, org.netbeans.lib.v8debug.V8Body, org.netbeans.lib.v8debug.vars.ReferencedValue[], boolean)}
         * or {@link V8Request#createErrorResponse(long, boolean, java.lang.String)}
         * to create the response.
         * @return A synchronous response provider.
         */
        public static ResponseProvider create(V8Response response) {
            return new ResponseProvider(response);
        }
        
        /**
         * Create an ampty asynchronous response provider.
         * @return an asynchronous response, call
         * {@link #setResponse(org.netbeans.lib.v8debug.V8Response)} on the
         * returned object to set the response asynchronously.
         */
        public static ResponseProvider createLazy() {
            return new ResponseProvider(null);
        }
        
        /**
         * Set the asynchronous response.
         * @param response The response.
         * Use {@link V8Request#createSuccessResponse(long, org.netbeans.lib.v8debug.V8Body, org.netbeans.lib.v8debug.vars.ReferencedValue[], boolean)}
         * or {@link V8Request#createErrorResponse(long, boolean, java.lang.String)}
         * to create the response.
         * @throws IOException thrown when an IO problem occurs.
         */
        public void setResponse(V8Response response) throws IOException {
            ServerConnection sc;
            synchronized (this) {
                if (this.response != null) {
                    throw new IllegalStateException("Response has been set already.");
                }
                this.response = response;
                sc = this.sc;
            }
            if (sc != null) {
                sc.send(response);
            }
        }
        
        void sendTo(ServerConnection sc) throws IOException {
            V8Response response;
            synchronized (this) {
                response = this.response;
                this.sc = sc;
            }
            if (response != null) {
                sc.send(response);
            }
        }
    }
}
