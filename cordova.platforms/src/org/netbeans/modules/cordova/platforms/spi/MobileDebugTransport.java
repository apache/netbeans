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
        return toString.replaceAll("localhost", WebUtils.getLocalhostInetAddress().getHostAddress()); // NOI18N
    }

    public final void setBaseUrl(String documentURL) {
        this.indexHtmlLocation = documentURL;
        if (mapper != null && documentURL != null) {
            int idx = documentURL.lastIndexOf("/www/");
            assert idx > -1 : "document url does not contain 'www' in path: " + documentURL;
            documentURL = documentURL.substring(0, idx + "/www/".length());
            documentURL = documentURL.replaceAll("file:///", "file:/");
            documentURL = documentURL.replaceAll("file:/", "file:///");
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
