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
package org.netbeans.modules.cordova.platforms.spi;

import java.net.MalformedURLException;
import java.net.URL;
import org.netbeans.modules.web.browser.spi.BrowserURLMapperImplementation;
import org.netbeans.modules.web.common.api.WebUtils;
import org.netbeans.modules.web.webkit.debugging.api.TransportStateException;
import org.netbeans.modules.web.webkit.debugging.spi.Command;
import org.netbeans.modules.web.webkit.debugging.spi.ResponseCallback;
import org.netbeans.modules.web.webkit.debugging.spi.TransportImplementation;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Jan Becicka
 */
public abstract class MobileDebugTransport implements TransportImplementation {

    protected ResponseCallback callBack;
    private String indexHtmlLocation;
    private String bundleId;
    private BrowserURLMapperImplementation.BrowserURLMapper mapper;
    private final RequestProcessor RP = new RequestProcessor(MobileDebugTransport.class);
    
    @Override
    public final void registerResponseCallback(ResponseCallback callback) {
        this.callBack = callback;
    }

    @Override
    public final void sendCommand(final Command command) throws TransportStateException {
        RP.post(new Runnable() {
            @Override
            public void run() {
                sendCommandImpl(command);
            }
        });
    }
    
    protected abstract void sendCommandImpl(Command command);
    
    @Override
    public final URL getConnectionURL() {
        try {
            if (indexHtmlLocation == null || indexHtmlLocation.isEmpty()) {
                return null;
            }
            return new URL(indexHtmlLocation);
        } catch (MalformedURLException ex) {
            throw new RuntimeException(ex);
        }
    }
 

    /**
     * TODO: hack to workaround #221791
     * @param toString
     * @return 
     */
    protected final String translate(String toString) {
        return toString.replace("localhost", WebUtils.getLocalhostInetAddress().getHostAddress()); // NOI18N
    }

    public final void setBaseUrl(String documentURL) {
        this.indexHtmlLocation = documentURL;
        if (mapper != null && documentURL != null) {
            int idx = documentURL.lastIndexOf("/www/");
            assert idx > -1 : "document url does not contain 'www' in path: " + documentURL;
            documentURL = documentURL.substring(0, idx + "/www/".length());
            documentURL = documentURL.replace("file:///", "file:/");
            documentURL = documentURL.replace("file:/", "file:///");
            try { 
                mapper.setBrowserURLRoot(WebUtils.urlToString(new URL(documentURL)));
            } catch (MalformedURLException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
    
    public final void setBundleIdentifier(String name) {
        this.bundleId=name;
    }
    
    protected final String getBundleIdentifier() {
        return this.bundleId;
    }
    
    public final void setBrowserURLMapper(BrowserURLMapperImplementation.BrowserURLMapper mapper) {
        this.mapper = mapper;
    }
    
    public void flush() {}
}
