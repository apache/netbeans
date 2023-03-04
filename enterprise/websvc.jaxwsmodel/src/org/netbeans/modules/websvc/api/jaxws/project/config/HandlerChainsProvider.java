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
 * HandlerChainsProvider.java
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
public class HandlerChainsProvider {
    
    private static HandlerChainsProvider  provider;
    
    /** Creates a new instance of HandlerChainsProvider */
    private HandlerChainsProvider() {
    }
    
    public static synchronized HandlerChainsProvider getDefault() {
        if (provider==null) {
            provider = new HandlerChainsProvider();
        }
        return provider;
    }
    
    public HandlerChains getHandlerChains(InputStream is) throws IOException {
        org.netbeans.modules.websvc.jaxwsmodel.handler_config1_0.HandlerChains impl = 
                org.netbeans.modules.websvc.jaxwsmodel.handler_config1_0.HandlerChains.createGraph(is);
        return (impl==null?null:new HandlerChains(impl));
    }
    
    public HandlerChains getHandlerChains(FileObject fo) throws IOException {
        org.netbeans.modules.websvc.jaxwsmodel.handler_config1_0.HandlerChains impl = 
                org.netbeans.modules.websvc.jaxwsmodel.handler_config1_0.HandlerChains.createGraph(fo.getInputStream());
        return (impl==null?null:new HandlerChains(impl));
    }
}
