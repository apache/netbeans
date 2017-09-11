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
package org.netbeans.core.browser.webview.ext;

import com.sun.javafx.scene.web.Debugger;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
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

    private WebBrowserImpl browserImpl;
    private Debugger debugger;
    private FXCallback fxCallback;
    private ResponseCallback callback;
    private volatile String urlToLoad; // The url to be loaded to the browser
    
    private static RequestProcessor RP = new RequestProcessor("JavaFX debugging callback");
    
    private static final Logger LOGGER = Logger.getLogger(WebKitDebuggingTransport.class.getName());

    public WebKitDebuggingTransport(WebBrowserImpl browserImpl) {
        this.browserImpl = browserImpl;
    }
    
    @Override
    @SuppressWarnings("deprecation")
    public boolean attach() {
        this.debugger = browserImpl.getEngine().impl_getDebugger();
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
