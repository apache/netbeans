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
package org.netbeans.modules.css.lib.api.properties;

import java.util.ArrayList;
import java.util.Collection;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 * Instance is supposed to be registered in the global lookup
 *
 * @author marekfukala
 */
public interface PropertyDefinitionProvider {
    
    public Collection<String> getPropertyNames(FileObject context);
    
    public PropertyDefinition getPropertyDefinition(String propertyName);
    
    
    public static class Query {
        
        public static Collection<String> getPropertyNames(FileObject context) {
            Collection<String> all = new ArrayList<>();
            Collection<? extends PropertyDefinitionProvider> providers = Lookup.getDefault().lookupAll(PropertyDefinitionProvider.class);
            for(PropertyDefinitionProvider provider : providers) {
                all.addAll(provider.getPropertyNames(context));
            }
            return all;
        }
        
        public static PropertyDefinition getPropertyDefinition(String propertyName) {
            Collection<? extends PropertyDefinitionProvider> providers = Lookup.getDefault().lookupAll(PropertyDefinitionProvider.class);
            for(PropertyDefinitionProvider provider : providers) {
                PropertyDefinition def = provider.getPropertyDefinition(propertyName);
                if(def != null) {
                    return def;
                }
            }
            return null;
        }
        
    }
}
