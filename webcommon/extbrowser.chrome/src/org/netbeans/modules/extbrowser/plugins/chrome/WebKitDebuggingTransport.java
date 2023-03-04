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
package org.netbeans.modules.extbrowser.plugins.chrome;

import java.net.URL;
import org.netbeans.modules.extbrowser.chrome.ChromeBrowserImpl;
import org.netbeans.modules.extbrowser.plugins.ExternalBrowserPlugin;
import org.netbeans.modules.web.webkit.debugging.api.TransportStateException;
import org.netbeans.modules.web.webkit.debugging.spi.Command;
import org.netbeans.modules.web.webkit.debugging.spi.ResponseCallback;
import org.netbeans.modules.web.webkit.debugging.spi.TransportImplementation;

public class WebKitDebuggingTransport implements TransportImplementation {

    private final ChromeBrowserImpl impl;
    private ResponseCallback callback;

    public WebKitDebuggingTransport(ChromeBrowserImpl impl) {
        this.impl = impl;
    }
    
    @Override
    public void sendCommand(Command command) throws TransportStateException {
        if (impl.getBrowserTabDescriptor() == null) {
            throw new TransportStateException();
        }
        ExternalBrowserPlugin.getInstance().sendWebKitDebuggerCommand(impl.getBrowserTabDescriptor(), command.getCommand());
    }

    @Override
    public void registerResponseCallback(ResponseCallback callback) {
        this.callback = callback;
    }

    @Override
    public boolean attach() {
        ExternalBrowserPlugin.BrowserTabDescriptor tab = impl.getBrowserTabDescriptor();
        if (tab == null) {
            // Issue 226812
            return false;
        } else {
            ExternalBrowserPlugin.getInstance().attachWebKitDebugger(tab);
            tab.setCallback(callback);
            return true;
        }
    }

    @Override
    public boolean  detach() {
        ExternalBrowserPlugin.getInstance().detachWebKitDebugger(impl.getBrowserTabDescriptor());
        return true;
        // XXX: anything else to cleanup?? unregister callback from tab?
    }

    @Override
    public String getConnectionName() {
        if (impl.getURL() != null) {
            return impl.getURL().toExternalForm();
        } else {
            return "...";
        }
    }
    
    @Override
    public URL getConnectionURL() {
        if (impl.getURL() != null) {
            return impl.getURL();
        } else {
            return null;
        }
    }
    
    @Override
    public String getVersion() {
        return VERSION_1;
    }
    
}
