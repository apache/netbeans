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
/*
 * EndpointsProvider.java
 *
 * Created on March 19, 2006, 8:44 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.websvc.api.jaxws.project.config;

import java.io.IOException;
import java.io.InputStream;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Roderico Cruz
 */
public class EndpointsProvider {
    
    private static EndpointsProvider  provider;
    
    /** Creates a new instance of HandlerChainsProvider */
    private EndpointsProvider() {
    }
    
    public static synchronized EndpointsProvider getDefault() {
        if (provider==null) {
            provider = new EndpointsProvider();
        }
        return provider;
    }
    
    public Endpoints getEndpoints(InputStream is) throws IOException {
        org.netbeans.modules.websvc.jaxwsmodel.endpoints_config1_0.Endpoints impl = 
                org.netbeans.modules.websvc.jaxwsmodel.endpoints_config1_0.Endpoints.createGraph(is);
        return (impl==null?null:new Endpoints(impl));
    }
    
    public Endpoints getEndpoints(FileObject fo) throws IOException {
        org.netbeans.modules.websvc.jaxwsmodel.endpoints_config1_0.Endpoints impl = 
                org.netbeans.modules.websvc.jaxwsmodel.endpoints_config1_0.Endpoints.createGraph(fo.getInputStream());
        return (impl==null?null:new Endpoints(impl));
    }
}
