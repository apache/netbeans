/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.ko4j.debugging;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;
import org.netbeans.modules.web.browser.api.PageInspector;
import org.netbeans.modules.web.webkit.debugging.api.TransportStateException;
import org.netbeans.modules.web.webkit.debugging.spi.Command;
import org.netbeans.modules.web.webkit.debugging.spi.Response;
import org.netbeans.modules.web.webkit.debugging.spi.ResponseCallback;
import org.netbeans.modules.web.webkit.debugging.spi.TransportImplementation;
import org.openide.util.Exceptions;

/**
 *
 * @author Jan Stola
 */
public class Transport implements TransportImplementation {
    private ObjectInputStream input;
    private ObjectOutputStream output;
    private ResponseCallback callback;
    private MessageDispatcherImpl messageDispatcher;
    
    public Transport(Socket socket, MessageDispatcherImpl messageDispatcher) {
        try {
            this.messageDispatcher = messageDispatcher;
            input = new ObjectInputStream(socket.getInputStream());
            output = new ObjectOutputStream(socket.getOutputStream());
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        while (true) {
                            int length = input.readInt();
                            byte[] bytes = new byte[length];
                            input.readFully(bytes);
                            String message = new String(bytes, StandardCharsets.UTF_8);
                            JSONObject json = (JSONObject)JSONValue.parseWithException(message);
                            callback.handleResponse(new Response(json));
                        }
                    } catch (IOException ioex) {
                        ioex.printStackTrace();
                    } catch (ParseException pex) {
                        pex.printStackTrace();
                    }
                    Transport.this.messageDispatcher.dispatchMessage(PageInspector.MESSAGE_DISPATCHER_FEATURE_ID, null);
                }                
            });
            t.start();
        } catch (IOException ioex) {
            Exceptions.printStackTrace(ioex);
        }
    }

    @Override
    public boolean attach() {
        return true;
    }

    @Override
    public boolean detach() {
        return true;
    }

    @Override
    public synchronized void sendCommand(Command command) throws TransportStateException {
        try {
            String message = command.toString();
            byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
            output.writeInt(bytes.length);
            output.write(bytes);
            output.flush();
        } catch (IOException ioex) {
            ioex.printStackTrace();
        }
    }

    @Override
    public void registerResponseCallback(ResponseCallback callback) {
        this.callback = callback;
    }

    @Override
    public String getConnectionName() {
        return "";
    }

    @Override
    public URL getConnectionURL() {
        return null;
    }

    @Override
    public String getVersion() {
        return VERSION_1;
    }
    
}
