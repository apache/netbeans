/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.modules.j2ee.weblogic9.j2ee;

import org.netbeans.modules.j2ee.deployment.common.api.Version;
import org.netbeans.modules.javaee.specs.support.api.JaxWs;
import org.netbeans.modules.websvc.wsstack.api.WSStack.Feature;
import org.netbeans.modules.websvc.wsstack.api.WSStack.Tool;
import org.netbeans.modules.websvc.wsstack.api.WSStackVersion;
import org.netbeans.modules.websvc.wsstack.api.WSTool;
import org.netbeans.modules.websvc.wsstack.spi.WSStackFactory;
import org.netbeans.modules.websvc.wsstack.spi.WSStackImplementation;

/**
 *
 * @author mkuchtiak
 * @author ads
 */
public class WebLogicJaxWsStack implements WSStackImplementation<JaxWs> {

    private static final Version ALTERNATIVE_TESTER_URL_SERVER_VERSION = Version.fromJsr277NotationWithFallback("12.1.2"); // NOI18N

    private static final Version JAXWS_225_SUPPORTED_SERVER_VERSION = Version.fromJsr277NotationWithFallback("12.1.1"); // NOI18N
    
    private static final Version JAXWS_228_SUPPORTED_SERVER_VERSION = Version.fromJsr277NotationWithFallback("12.1.2"); // NOI18N
    
    private static final Version JAXWS_2210_SUPPORTED_SERVER_VERSION = Version.fromJsr277NotationWithFallback("12.1.3"); // NOI18N

    private static final Version JAXWS_2211_SUPPORTED_SERVER_VERSION = Version.fromJsr277NotationWithFallback("12.2.1"); // NOI18N
    
    private final Version serverVersion;
    
    private final String version;
    
    private final JaxWs jaxWs;
    
    public WebLogicJaxWsStack(Version serverVersion) {
        this.serverVersion = serverVersion;
        if (serverVersion != null && JAXWS_2211_SUPPORTED_SERVER_VERSION.isBelowOrEqual(serverVersion)) {
            version = "2.2.11"; // NOI18N
        } else if (serverVersion != null && JAXWS_2210_SUPPORTED_SERVER_VERSION.isBelowOrEqual(serverVersion)) {
            version = "2.2.10"; // NOI18N
        } else if (serverVersion != null && JAXWS_228_SUPPORTED_SERVER_VERSION.isBelowOrEqual(serverVersion)) {
            version = "2.2.8"; // NOI18N
        } else if (serverVersion != null && JAXWS_225_SUPPORTED_SERVER_VERSION.isBelowOrEqual(serverVersion)) {
            version = "2.2.5"; // NOI18N
        } else {
            version = "2.1.4"; // NOI18N
        }
        jaxWs = new JaxWs(getUriDescriptor());
    }

    @Override
    public JaxWs get() {
        return jaxWs;
    }

    @Override
    public WSStackVersion getVersion() {
        return WSStackFactory.createWSStackVersion(version);
    }

    @Override
    public WSTool getWSTool(Tool toolId) {
            return null;
    }

    @Override
    public boolean isFeatureSupported(Feature feature) {
        return feature == JaxWs.Feature.TESTER_PAGE || feature == JaxWs.Feature.JSR109;
    }
    
    private JaxWs.UriDescriptor getUriDescriptor() {
        return new JaxWs.UriDescriptor() {

            @Override
            public String getServiceUri(String applicationRoot, 
                    String serviceName, String portName, boolean isEjb) 
            {
                if (isEjb) {
                    return portName+"/"+serviceName;
                } else {
                    return (applicationRoot.length() >0 ? applicationRoot+"/":"")+
                        serviceName;
                }
            }

            @Override
            public String getDescriptorUri(String applicationRoot, 
                    String serviceName, String portName, boolean isEjb) 
            {
                return getServiceUri(applicationRoot, serviceName, portName, 
                        isEjb)+"?wsdl"; //NOI18N
            }
            
            @Override
            public String getTesterPageUri(String host, String port, 
                    String applicationRoot, String serviceName, String portName, 
                        boolean isEjb) 
            {
                String prefix;
                if (serverVersion != null && ALTERNATIVE_TESTER_URL_SERVER_VERSION.isBelowOrEqual(serverVersion)) {
                    prefix = "http://"+host+":"+port+"/ws_utc/begin.do?wsdlUrl="; //NOI18N
                } else {
                    prefix = "http://"+host+":"+port+"/wls_utc/begin.do?wsdlUrl="; //NOI18N
                }

                return prefix + "http://" + host + ":" + port + "/" + getServiceUri( // NOI18N
                        applicationRoot, serviceName, portName, isEjb) + "?wsdl"; // NOI18N
            }
            
        };
    }
    
}
