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
 * WSEditorProviderRegistry.java
 *
 * Created on February 17, 2006, 10:31 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.websvc.api.wseditor;

import org.netbeans.modules.websvc.spi.wseditor.WSEditorProvider;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Roderico Cruz
 */
public class WSEditorProviderRegistry {
    
    static WSEditorProviderRegistry registry = new WSEditorProviderRegistry();
    
    private Set<WSEditorProvider> editors = new HashSet<WSEditorProvider>();
    
    /**
     * Creates a new instance of WSEditorProviderRegistry
     */
    private WSEditorProviderRegistry() {
    }
    
    public static WSEditorProviderRegistry getDefault(){
        return registry;
    }
    
    public void register(WSEditorProvider provider){
        editors.add(provider);
    }
    
    public void unregister(WSEditorProvider provider){
        editors.remove(provider);
    }
    
    public Set<WSEditorProvider> getEditorProviders(){
        return editors;
    }
}
