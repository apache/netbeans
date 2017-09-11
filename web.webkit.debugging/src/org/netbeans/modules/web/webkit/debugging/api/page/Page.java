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
package org.netbeans.modules.web.webkit.debugging.api.page;

import org.json.simple.JSONObject;
import org.netbeans.modules.web.webkit.debugging.TransportHelper;
import org.netbeans.modules.web.webkit.debugging.api.WebKitDebugging;
import org.netbeans.modules.web.webkit.debugging.spi.Command;
import org.netbeans.modules.web.webkit.debugging.spi.Response;
import org.netbeans.modules.web.webkit.debugging.spi.ResponseCallback;

/**
 * Java wrapper of the Page domain of WebKit Remote Debugging Protocol.
 * 
 * @author Jan Stola
 */
public class Page {
    /** Transport used by this instance. */
    private final TransportHelper transport;
    /** Determines if Page domain notifications are enabled. */
    private boolean enabled;
    /** Callback for Page event notifications. */
    private final Callback callback;
    /** Number of clients interested in Page domain notifications. */
    private int numberOfClients = 0;

    /**
     * Creates a new wrapper for the Page domain of WebKit Remote Debugging Protocol.
     * 
     * @param transport transport to use.
     * @param webKit WebKit remote debugging API wrapper to use.
     */
    public Page(TransportHelper transport, WebKitDebugging webKit) {
        this.transport = transport;
        this.callback = new Callback();
        this.transport.addListener(callback);
    }

    /**
     * Enables Page domain notifications.
     */
    public void enable() {
        numberOfClients++;
        if (!enabled) {
            enabled = true;
            transport.sendBlockingCommand(new Command("Page.enable")); // NOI18N
        }
    }

    /**
     * Disables Page domain notifications.
     */
    public void disable() {
        assert numberOfClients > 0;
        numberOfClients--;
        if (numberOfClients == 0) {
            transport.sendCommand(new Command("Page.disable")); // NOI18N
            enabled = false;
        }
    }

    /**
     * Reloads the page optionally ignoring the cache.
     * 
     * @param ignoreCache if true then the browser cache is ignored
     * (as if the user pressed Shift+refresh).
     * @param scriptToEvaluateOnLoad if non-null then the script will
     * be injected into all frames of the inspected page after reload.
     */
    public void reload(boolean ignoreCache, String scriptToEvaluateOnLoad) {
        reload(ignoreCache, scriptToEvaluateOnLoad, null);
    }

    /**
     * Reloads the page optionally ignoring the cache.
     * 
     * @param ignoreCache if true then the browser cache is ignored
     * (as if the user pressed Shift+refresh).
     * @param scriptToEvaluateOnLoad if non-null then the script will
     * be injected into all frames of the inspected page after reload.
     * @param scriptPreprocessor script body that should evaluate to function
     * that will preprocess all the scripts before their compilation.
     */
    public void reload(boolean ignoreCache, String scriptToEvaluateOnLoad, String scriptPreprocessor) {
        JSONObject pars = new JSONObject();
        pars.put("ignoreCache", ignoreCache); // NOI18N
        if (scriptToEvaluateOnLoad != null) {
            pars.put("scriptToEvaluateOnLoad", scriptToEvaluateOnLoad); // NOI18N
        }
        if (scriptPreprocessor != null) {
            pars.put("scriptPreprocessor", scriptPreprocessor); // NOI18N
        }
        transport.sendBlockingCommand(new Command("Page.reload", pars)); // NOI18N
    }

    /**
     * Navigates the page to the given URL.
     * 
     * @param url URL to navigate to.
     */
    public void navigate(String url) {
        JSONObject urlPar = new JSONObject();
        urlPar.put("url", url); // NOI18N
        transport.sendBlockingCommand(new Command("Page.navigate", urlPar)); // NOI18N
    }

    /**
     * Determines whether Page domain notifications are enabled.
     * 
     * @return {@code true} when Page domain notifications are enabled,
     * returns {@code false} otherwise.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Callback for Page domain notifications.
     */
    private static class Callback implements ResponseCallback {

        @Override
        public void handleResponse(Response response) {
//            if ("Page.loadEventFired".equals(response.getMethod())) {
//            } else if ("Page.domContentEventFired".equals(response.getMethod())) {
//            }
        }
        
    }
    
}
