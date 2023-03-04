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
 * WSConfigurationProviderRegistry.java
 *
 * Created on March 29, 2007, 11:14 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.websvc.design.configuration;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import org.openide.util.Lookup;


/**
 *
 * @author rico
 */
public class WSConfigurationProviderRegistry {
    static WSConfigurationProviderRegistry registry = new WSConfigurationProviderRegistry();
    
    private Set<WSConfigurationProvider> providers = new LinkedHashSet<WSConfigurationProvider>();
    
    
    /** Creates a new instance of WSConfigurationProviderRegistry */
    private WSConfigurationProviderRegistry() {
    }
    
    public static WSConfigurationProviderRegistry getDefault(){
        return registry;
    }
    
    public void register(WSConfigurationProvider provider){
        providers.add(provider);
    }
    
    public void unregister(WSConfigurationProvider provider){
        providers.remove(provider);
    }
    
    public Set<WSConfigurationProvider> getWSConfigurationProviders(){
        populateRegistry();
        return providers;
    }
    
    private void populateRegistry(){
        if(providers.isEmpty()){
            Lookup.Result<WSConfigurationProvider> results = Lookup.getDefault().
                    lookup(new Lookup.Template<WSConfigurationProvider>(WSConfigurationProvider.class));
            Collection<? extends WSConfigurationProvider> providers = results.allInstances();
            for(WSConfigurationProvider provider : providers){
                register(provider);
            }
        }
    }
    
}
