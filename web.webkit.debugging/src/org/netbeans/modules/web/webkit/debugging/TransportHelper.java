/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.webkit.debugging;

import java.awt.EventQueue;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.web.webkit.debugging.api.TransportStateException;
import org.netbeans.modules.web.webkit.debugging.spi.Command;
import org.netbeans.modules.web.webkit.debugging.spi.Response;
import org.netbeans.modules.web.webkit.debugging.spi.ResponseCallback;
import org.netbeans.modules.web.webkit.debugging.spi.TransportImplementation;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

@NbBundle.Messages({"WebKitDebuggingProtocolPane=WebKit Protocol"})
public class TransportHelper {
    
    private TransportImplementation impl;
    private Callback callback;
    private Map<Integer, Handle> map = new HashMap<Integer, Handle>();
    private List<ResponseCallback> listeners = new CopyOnWriteArrayList<ResponseCallback>();

    public static final String OBJECT_GROUP_NAME = "netbeans-debugger-objects";
    
    static final boolean SHOW_WEBKIT_PROTOCOL = Boolean.getBoolean("show.webkit.protocol");
    
    private final RequestProcessor RP = new RequestProcessor();
    
    private boolean reset = false;

    public TransportHelper(TransportImplementation impl) {
        this.impl = impl;
        this.callback = new Callback();
        impl.registerResponseCallback(callback);
    }
    
    public String getConnectionName() {
        return impl.getConnectionName();
    }
    
    public URL getConnectionURL() {
        return impl.getConnectionURL();
    }
    
    public void sendCommand(Command command) {
        assert !EventQueue.isDispatchThread();
        log("send "+command.toString()); // NOI18N
        try {
            impl.sendCommand(command);
        } catch (TransportStateException tsex) {
            log("transport failed for "+command.toString()); // NOI18N
        }
    }

    public void reset() {
        if (SHOW_WEBKIT_PROTOCOL) {
            getOutputLogger().getOut().close();
            getOutputLogger().getErr().close();
        }
        reset = true;
    }
    
    public boolean isVersionUnknownBeforeRequestChildNodes() {
        return TransportImplementation.VERSION_UNKNOWN_BEFORE_requestChildNodes.equals(impl.getVersion());
    }
    
    public boolean isVersion1() {
        return TransportImplementation.VERSION_1.equals(impl.getVersion());
    }
    
    public Response sendBlockingCommand(Command command) {
        assert !EventQueue.isDispatchThread();
        log("blocking send "+command.toString()); // NOI18N
        Handle handle = createSynchronizationHandle(command);
        try {
            impl.sendCommand(command);
        } catch (TransportStateException tsex) {
            return new Response(tsex);
        }
        boolean res = handle.waitForResponse();
        if (res) {
            return handle.getResponse();
        } else {
            logError("no response for "+command.toString()); // NOI18N
            return null;
        }
    }

    public void sendCallbackCommand(Command command, 
            ResponseCallback callback) {
        assert !EventQueue.isDispatchThread();
        log("callback send "+command.toString()); // NOI18N
        createCallbackHandle(command, callback);
        try {
            impl.sendCommand(command);
        } catch (TransportStateException tsex) {
            callback.handleResponse(new Response(tsex));
        }
    }
    
    public void addListener(ResponseCallback l) {
        listeners.add(l);
    }

    public void removeListener(ResponseCallback l) {
        listeners.remove(l);
    }
    
    private void notifyListeners(Response response) {
        for (ResponseCallback l : listeners ) {
            l.handleResponse(response);
        }
    }
    private synchronized Handle createSynchronizationHandle(Command command) {
        Handle handle = new Handle();
        map.put(command.getID(), handle);
        return handle;
    }

    private synchronized void createCallbackHandle(Command command, 
            ResponseCallback callback) {
        map.put(command.getID(), new Handle(callback));
    }

    private synchronized Handle removeHandle(int id) {
        return map.remove(id);
    }

    public RequestProcessor getRequestProcessor() {
        return RP;
    }
    
    private static class Handle {
        private Response response;
        private Semaphore semaphore;
        private ResponseCallback callback;

        public Handle() {
            this.semaphore = new Semaphore(0);
        }

        public Handle(ResponseCallback callback) {
            this.callback = callback;
        }

        public void setResponse(Response response, TransportHelper transport) {
            this.response = response;
            if (semaphore != null) {
                semaphore.release();
            }
            if (callback != null) {
                TransportStateException transportException = response.getException();
                if (transportException != null) {
                    transport.log("response "+transportException.toString()); // NOI18N
                } else {
                    transport.log("response "+response.getResponse().toJSONString()); // NOI18N
                }
                callback.handleResponse(response);
            }
        }

        public Response getResponse() {
            return response;
        }
        
        public boolean waitForResponse() {
            assert semaphore != null;
            try {
                return semaphore.tryAcquire(10, TimeUnit.SECONDS);
            } catch (InterruptedException ex) {
                Logger.getLogger(TransportHelper.class.getName()).log(Level.INFO, null, ex);
                return false;
            }
        }
        
    }

    private InputOutput getOutputLogger() {
       return IOProvider.getDefault().getIO(Bundle.WebKitDebuggingProtocolPane(), false);
    }
    
    public static String getCurrentTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss:SSS");
        return formatter.format(new Date(System.currentTimeMillis()));
    }
    
    private void log(String s) {
        checkReset();
        if (SHOW_WEBKIT_PROTOCOL) {
            getOutputLogger().getOut().println(getCurrentTime() + " " + s);
        }
    }
    
    private void logError(String s) {
        checkReset();
        if (SHOW_WEBKIT_PROTOCOL) {
            getOutputLogger().getErr().println(getCurrentTime()+" "+s); 
        }
    }

    private void checkReset() {
        if (reset) {
            reset = false;
            if (SHOW_WEBKIT_PROTOCOL) {
                try {
                    getOutputLogger().getOut().reset();
                    getOutputLogger().getErr().reset();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }
    
    
    private class Callback implements ResponseCallback {

        public Callback() {
        }
        
        @Override
        public void handleResponse(Response response) {
            int id = response.getID();
            if (id != -1) {
                // handle result of a command we issued earlier privately and
                // do not propagate that event further
                Handle handle = removeHandle(id);
                if (handle == null) {
                    log("ignoring response "+response.toString()); // NOI18N
                    return;
                }
                log("response "+response.toString()); // NOI18N
                handle.setResponse(response, TransportHelper.this);
            } else {
                // this is a unrequested notification from webkit - pass it
                // to API layer to handle it:
                log("event "+response.toString()); // NOI18N
                notifyListeners(response);
            }
        }
    
    }
}
