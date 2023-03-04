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

package org.netbeans.modules.websvc.core.wseditor.support;

import org.netbeans.modules.websvc.api.support.EditWSAttributesCookie;
import java.util.Collection;
import java.util.Set;
import org.netbeans.modules.websvc.api.support.LogUtils;
import org.netbeans.modules.websvc.spi.wseditor.WSEditorProvider;
import org.netbeans.modules.websvc.api.wseditor.WSEditorProviderRegistry;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

public final class WSEditAttributesAction extends NodeAction {
    
    protected void performAction(Node[] activatedNodes) {
        if (activatedNodes.length == 1) {
            final EditWSAttributesCookie cookie = activatedNodes[0].getLookup().lookup(EditWSAttributesCookie.class);
            if (cookie!=null) {
                cookie.openWSAttributesEditor();

                // logging usage of action
                Object[] params = new Object[2];
                params[0] = LogUtils.WS_STACK_JAXWS;
                params[1] = "EDIT WS ATTRIBUTES"; // NOI18N
                LogUtils.logWsAction(params);
            }
        }
    }
    
    public String getName() {
        return NbBundle.getMessage(WSEditAttributesAction.class, "CTL_WSEditAttributesAction");
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    @Override
    protected boolean asynchronous() {
        return true;
    }
    
    private WSEditorProviderRegistry populateWSEditorProviderRegistry(){
        WSEditorProviderRegistry registry = WSEditorProviderRegistry.getDefault();
        if(registry.getEditorProviders().isEmpty()){
            Lookup.Result<WSEditorProvider> results = Lookup.getDefault().
                    lookup(new Lookup.Template<WSEditorProvider>(WSEditorProvider.class));
            Collection<? extends WSEditorProvider> services = results.allInstances();
            for(WSEditorProvider provider : services){
                registry.register(provider);
            }
        }
        return registry;
    }
    
    protected boolean enable(Node[] activatedNodes) {
        
        if(activatedNodes.length == 1){
            WSEditorProviderRegistry registry =
                    populateWSEditorProviderRegistry();
            Set<WSEditorProvider> providers = registry.getEditorProviders();
            if(providers.size() == 0){
                return false;
            }
            Node node = activatedNodes[0];
            for(WSEditorProvider provider : providers){
                //look for the first one that is enabled and return true
                if(provider.enable(node)){
                    return true;
                }
            }
        }
        return false;
        
    }
}

