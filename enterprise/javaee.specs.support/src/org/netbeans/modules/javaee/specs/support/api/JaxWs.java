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

package org.netbeans.modules.javaee.specs.support.api;

import org.netbeans.modules.websvc.wsstack.api.WSStack;

/**
 *
 * @author mkuchtiak
 */
public class JaxWs {
    private UriDescriptor uriDescriptor;
    
    public JaxWs(UriDescriptor uriDescriptor) {
        this.uriDescriptor = uriDescriptor;
    }
    
    public UriDescriptor getWsUriDescriptor() {
        return uriDescriptor;
    }
            
    public static enum Tool implements WSStack.Tool {
        WSIMPORT,
        WSGEN;

        public String getName() {
            return name();
        }
    }
    
    public static enum Feature implements WSStack.Feature {
        JSR109,
        SERVICE_REF_INJECTION,
        TESTER_PAGE,
        WSIT;

        public String getName() {
            return name();
        }
    }
    
    public static interface UriDescriptor {
        /** Pattern for WSDL descriptor URI */
        public String getDescriptorUri (String applicationRoot, String serviceName, String portName, boolean isEjb);
        /** Pattern for Web Service URI */
        public String getServiceUri (String applicationRoot, String serviceName, String portName, boolean isEjb);
        /** Pattern for Tester Page URI */
        public String getTesterPageUri (String host, String port, String applicationRoot, String serviceName, String portName, boolean isEjb);
    }
    
}
