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
