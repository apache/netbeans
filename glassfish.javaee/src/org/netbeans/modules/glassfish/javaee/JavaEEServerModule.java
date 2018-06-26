/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.glassfish.javaee;

import java.util.Collection;
import java.util.Collections;
import org.netbeans.modules.glassfish.eecommon.api.LogHyperLinkSupport;
import org.netbeans.modules.glassfish.spi.Recognizer;
import org.netbeans.modules.glassfish.spi.RecognizerCookie;
import org.netbeans.modules.glassfish.spi.RemoveCookie;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.InstanceRemovedException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.j2ee.deployment.devmodules.api.ServerInstance;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.openide.util.Exceptions;
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
