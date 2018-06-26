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
