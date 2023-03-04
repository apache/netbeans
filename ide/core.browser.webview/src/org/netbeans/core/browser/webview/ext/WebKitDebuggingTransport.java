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
package org.netbeans.core.browser.webview.ext;

import com.sun.javafx.scene.web.Debugger;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.scene.web.WebEngine;
import javafx.util.Callback;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;
import org.netbeans.core.browser.webview.TransportImplementationWithURLToLoad;
import org.netbeans.modules.web.webkit.debugging.spi.Command;
import org.netbeans.modules.web.webkit.debugging.spi.Response;
import org.netbeans.modules.web.webkit.debugging.spi.ResponseCallback;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

public class WebKitDebuggingTransport implements TransportImplementationWithURLToLoad {
    private static final Logger LOGGER = Logger.getLogger(WebKitDebuggingTransport.class.getName());
    private static final RequestProcessor RP = new RequestProcessor("JavaFX debugging callback");

    private WebBrowserImpl browserImpl;
    private Debugger debugger;
    private FXCallback fxCallback;
    private ResponseCallback callback;
    private volatile String urlToLoad; // The url to be loaded to the browser
    
    public WebKitDebuggingTransport(WebBrowserImpl browserImpl) {
        this.browserImpl = browserImpl;
    }
    
    @Override
    @SuppressWarnings("deprecation")
    public boolean attach() {
        this.debugger = getDebugger();
        this.fxCallback = new FXCallback(callback);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                debugger.setMessageCallback(fxCallback);
                debugger.setEnabled(true);
            }
        });
        return true;
    }

    private Debugger getDebugger() {
        Method getDebuggerImpl = null;
        try {
            getDebuggerImpl = WebEngine.class.getDeclaredMethod("impl_getDebugger");
        } catch (NoSuchMethodException | SecurityException ex) {
            LOGGER.log(Level.FINE, "Method impl_getDebugger not found on javafx.scene.web.WebEngine", ex);
        }
        try {
            getDebuggerImpl = WebEngine.class.getDeclaredMethod("getDebugger");
            getDebuggerImpl.setAccessible(true);
        } catch (NoSuchMethodException | SecurityException ex) {
            LOGGER.log(Level.FINE, "Method getDebugger not found on javafx.scene.web.WebEngine", ex);
        }

        if (getDebuggerImpl == null) {
            throw new UnsupportedOperationException("Failed to fetch debugger from WebEngine (unsupported JavaFX version?)");
        }

        try {
            return Debugger.class.cast(getDebuggerImpl.invoke(browserImpl.getEngine()));
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            throw new UnsupportedOperationException("Failed to invoke " + getDebuggerImpl.getName(), ex);
        }
    }

    @Override
    public boolean detach() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                if (debugger != null) {
                    debugger.setEnabled(false);
                }
            }
        });
        return true;
    }

    @Override
    public void sendCommand(final Command command) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                try {
                    if ((debugger != null) && debugger.isEnabled()) {
                        debugger.sendMessage(command.toString());
                    }
                } catch (Throwable t) {
                    LOGGER.log(Level.WARNING, "sending commend triggered exception. command="+command.toString(), t);
                }
            }
        });
    }

    @Override
    public void registerResponseCallback(ResponseCallback callback) {
        this.callback = callback;
    }

    @Override
    public String getConnectionName() {
        return browserImpl.getURL();
    }
    
    @Override
    public void setURLToLoad(String urlToLoad) {
        this.urlToLoad = urlToLoad;
        
    }
    
    @Override
    public URL getConnectionURL() {
        String urlStr = urlToLoad;
        if (urlStr == null) {
            urlStr = browserImpl.getURL();
        }
        if (urlStr != null) {
            try {
                return new URL(urlStr);
            } catch (MalformedURLException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return null;
    }

    @Override
    public String getVersion() {
        return VERSION_1;
    }
    
    private static class FXCallback implements Callback<String, Void> {

        private ResponseCallback responseCallback;

        public FXCallback(ResponseCallback responseCallback) {
            this.responseCallback = responseCallback;
        }
        
        @Override
        public Void call(final String p) {
            RP.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        JSONObject json = (JSONObject)JSONValue.parseWithException(p);
                        responseCallback.handleResponse(new Response(json));
                    } catch (ParseException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            });
            return null;
        }
        
    }
    
}
