/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.payara.jakartaee;

import java.util.Collection;
import java.util.Collections;
import org.netbeans.modules.payara.eecommon.api.LogHyperLinkSupport;
import org.netbeans.modules.payara.spi.Recognizer;
import org.netbeans.modules.payara.spi.RecognizerCookie;
import org.netbeans.modules.payara.spi.RemoveCookie;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.InstanceRemovedException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.j2ee.deployment.devmodules.api.ServerInstance;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.openide.windows.OutputListener;


/**
 *
 * @author Peter Williams
 */
public class JavaEEServerModule implements Lookup.Provider, RemoveCookie, RecognizerCookie {

    private final InstanceProperties instanceProperties;
    private final LogHyperLinkSupport.AppServerLogSupport logSupport;

    private final Lookup lookup;

    JavaEEServerModule(Lookup instanceLookup, InstanceProperties ip) {
        instanceProperties = ip;
        logSupport = new LogHyperLinkSupport.AppServerLogSupport("", "/");

        // is this ok, can platform change ?
        ServerInstance inst = Deployment.getDefault().getServerInstance(
                instanceProperties.getProperty(InstanceProperties.URL_ATTR));
        J2eePlatform platform = null;
        try {
            platform = inst.getJ2eePlatform();
        } catch (InstanceRemovedException ex) {
        }
        lookup = platform != null
                ? new ProxyLookup(Lookups.fixed(platform, ip), Lookups.proxy(platform))
                : Lookup.EMPTY;
    }

    public InstanceProperties getInstanceProperties() {
        return instanceProperties;
    }

    @Override
    public Lookup getLookup() {
        return lookup;
    }

    // ------------------------------------------------------------------------
    // RemoveCookie support
    // ------------------------------------------------------------------------
    public void removeInstance(String serverUri) {
        InstanceProperties.removeInstance(serverUri);
    }

    // ------------------------------------------------------------------------
    // RecognizerCookie support
    // ------------------------------------------------------------------------
    public Collection<? extends Recognizer> getRecognizers() {
        return Collections.singleton(new Recognizer() {
            public OutputListener processLine(String text) {
                OutputListener result = null;
                if(text.length() > 0 && text.length() < 500 && !" ".equals(text)) {
                    LogHyperLinkSupport.AppServerLogSupport.LineInfo lineInfo = 
                            logSupport.analyzeLine(text);
                    if(lineInfo != null && lineInfo.isError() && lineInfo.isAccessible()) {
                        result = logSupport.getLink(lineInfo.message(), lineInfo.path(), lineInfo.line());
                    }
                }
                return result;
            }
        });
    }

}
