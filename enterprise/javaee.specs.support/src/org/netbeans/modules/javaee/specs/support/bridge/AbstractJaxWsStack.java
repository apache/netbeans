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

package org.netbeans.modules.javaee.specs.support.bridge;

import java.net.URL;

import org.netbeans.modules.javaee.specs.support.api.JaxWs;
import org.netbeans.modules.websvc.wsstack.api.WSStack;
import org.netbeans.modules.websvc.wsstack.api.WSTool;
import org.netbeans.modules.websvc.wsstack.spi.*;

/**
 *
 * @author mkuchtiak
 * @author ads
 */
public abstract class AbstractJaxWsStack implements WSStackImplementation<JaxWs> {  
    private JaxWs jaxWs;
    
    protected AbstractJaxWsStack() {
        jaxWs = new JaxWs(getUriDescriptor());
    }
    
    @Override
    public JaxWs get() {
        return jaxWs;
    }
    
    @Override
    public WSTool getWSTool(WSStack.Tool toolId) {
        if (toolId == JaxWs.Tool.WSIMPORT) {
            return WSStackFactory.createWSTool(new JaxWsTool(JaxWs.Tool.WSIMPORT));
        } else if (toolId == JaxWs.Tool.WSGEN) {
            return WSStackFactory.createWSTool(new JaxWsTool(JaxWs.Tool.WSGEN));
        } else {
            return null;
        }
    }

    public JaxWs.UriDescriptor getUriDescriptor() {
         return new JaxWs.UriDescriptor() {

            @Override
            public String getServiceUri(String applicationRoot, 
                    String serviceName, String portName, boolean isEjb) 
             {
                if (isEjb) {
                    return serviceName+"/"+portName; //NOI18N
                } else {
                    return (applicationRoot.length()>0 ? 
                            applicationRoot+"/" : "")+serviceName; //NOI18N
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
                    String applicationRoot, String serviceName, 
                    String portName, boolean isEjb) 
            {
                return "http://"+host+":"+port+"/"+getServiceUri(applicationRoot, //NOI18N
                        serviceName, portName, isEjb); 
            }
             
         };
    }
    
    @Override
    public boolean isFeatureSupported(WSStack.Feature feature) {
        if (feature == JaxWs.Feature.TESTER_PAGE) {
            return true;
        } else {
            return false;
        }
    }
    
    private class JaxWsTool implements WSToolImplementation {
        JaxWs.Tool tool;
        JaxWsTool(JaxWs.Tool tool) {
            this.tool = tool;
        }

        @Override
        public String getName() {
            return tool.getName();
        }

        @Override
        public URL[] getLibraries() {
            return new URL[0];
        }
        
    }

}
