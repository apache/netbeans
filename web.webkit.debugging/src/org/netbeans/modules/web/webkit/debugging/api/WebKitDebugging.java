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
