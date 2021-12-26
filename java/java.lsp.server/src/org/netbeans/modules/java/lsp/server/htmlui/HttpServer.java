/**
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
package org.netbeans.modules.java.lsp.server.htmlui;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import org.netbeans.html.boot.spi.Fn;

abstract class HttpServer<Request, Response, WebSocket, Runner> {
    abstract void init(int from, int to) throws IOException;
    abstract void start() throws IOException;
    abstract void shutdownNow();
    abstract void addHttpHandler(Handler h, String path);
    abstract int getPort();

    abstract String getRequestURI(Request r);
    abstract String getServerName(Request r);
    abstract int getServerPort(Request r);
    abstract String getParameter(Request r, String id);
    abstract String getMethod(Request r);
    abstract String getBody(Request r) throws IOException;
    abstract String getHeader(Request r, String substring);

    abstract Writer getWriter(Response r);
    abstract void setContentType(Response r, String texthtml);
    abstract void setStatus(Response r, int i);
    abstract OutputStream getOutputStream(Response r);
    abstract void suspend(Response r);
    abstract void resume(Response r, Runnable runWhenResponseIsReady);
    abstract void setCharacterEncoding(Response r, String utF8);
    abstract void addHeader(Response r, String accessControlAllowOrigin, String string);

    abstract <WebSocket> void send(WebSocket socket, String s);

    abstract Runner initializeRunner(String id);
    abstract void runSafe(Runner runner, Runnable code, Fn.Presenter presenter);

    static abstract class Handler {
        abstract <Request, Response> void service(HttpServer<Request, Response, ?, ?> server, Request rqst, Response rspns) throws IOException;
    }

    static abstract class WebSocketApplication {
        abstract <WebSocket> void onMessage(HttpServer<?, ?, WebSocket, ?> server, WebSocket socket, String text);
    }
}
