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
package org.netbeans.modules.web.webkit.debugging.api;

import org.netbeans.modules.web.webkit.debugging.APIFactory;
import org.netbeans.modules.web.webkit.debugging.TransportHelper;
import org.netbeans.modules.web.webkit.debugging.api.console.Console;
import org.netbeans.modules.web.webkit.debugging.api.css.CSS;
import org.netbeans.modules.web.webkit.debugging.api.dom.DOM;
import org.netbeans.modules.web.webkit.debugging.api.network.Network;
import org.netbeans.modules.web.webkit.debugging.api.page.Page;

/**
 * Main API entry point for Remote WebKit Debugging support. Instance of this
 * class will be available in browser's lookup if browser supports Remote WebKit 
 * Debugging.
 */
public class WebKitDebugging {

    static {
        APIFactory.Accessor.DEFAULT = new APIFactory.Accessor() {
            
            @Override
            public WebKitDebugging createWebKitDebugging(TransportHelper transport) {
                return new WebKitDebugging(transport);
            }
        };
    }
    
    private final TransportHelper transport;
    private Debugger debugger;
    private Runtime runtime;
    private DOM dom;
    private CSS css;
    private Page page;
    private Network network;
    private Console console;

    private WebKitDebugging(TransportHelper transport) {
        this.transport = transport;
    }

    public String getConnectionName() {
        return transport.getConnectionName();
    }

    /**
     * Get debugger part of Remote WebKit Debugging.
     * 
     * @return debugger part of Remote WebKit Debugging.
     */
    public synchronized Debugger getDebugger() {
        if (debugger == null) {
            debugger = new Debugger(transport, this);
        }
        return debugger;
    }
    
    /**
     * Get runtime part of Remote WebKit Debugging.
     * 
     * @return runtime part of Remote WebKit Debugging.
     */
    public synchronized Runtime getRuntime() {
        if (runtime == null) {
            runtime = new Runtime(transport, this);
        }
        return runtime;
    }

    /**
     * Returns DOM part of Remote WebKit Debugging.
     * 
     * @return DOM part of Remote WebKit Debugging.
     */
    public synchronized DOM getDOM() {
        if (dom == null) {
            dom = new DOM(transport, this);
        }
        return dom;
    }

    /**
     * Returns CSS part of Remote WebKit Debugging.
     *
     * @return CSS part of Remote WebKit Debugging.
     */
    public synchronized CSS getCSS() {
        if (css == null) {
            css = new CSS(transport);
        }
        return css;
    }

    /**
     * Get Page part of Remote WebKit Debugging.
     * 
     * @return Page part of Remote WebKit Debugging.
     */
    public synchronized Page getPage() {
        if (page == null) {
            page = new Page(transport, this);
        }
        return page;
    }

    /**
     * Get Network part of Remote WebKit Debugging.
     * 
     * @return Network part of Remote WebKit Debugging.
     */
    public synchronized Network getNetwork() {
        if (network == null) {
            network = new Network(transport, this);
        }
        return network;
    }

    public synchronized Console getConsole() {
        if (console == null) {
            console = new Console(transport, this);
        }
        return console;
    }
    
    /**
     * Resets cached data.
     */
    public synchronized void reset() {
        if (dom != null) {
            dom.reset();
        }
        if (css != null) {
            css.reset();
        }
        if (console != null) {
            console.reset();
        }
        transport.reset();
    }
    
    // other parts of Remote WebKit Debugging like CSS, DOMDebugger, 
    // Inspector will be introduced here in time
    
}
