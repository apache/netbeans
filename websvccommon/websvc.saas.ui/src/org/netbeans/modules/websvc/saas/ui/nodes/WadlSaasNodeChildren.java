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

package org.netbeans.modules.websvc.saas.ui.nodes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.websvc.saas.model.Saas;
import org.netbeans.modules.websvc.saas.model.Saas.State;
import org.netbeans.modules.websvc.saas.model.SaasMethod;
import org.netbeans.modules.websvc.saas.model.WadlSaas;
import org.netbeans.modules.websvc.saas.model.WadlSaasMethod;
import org.netbeans.modules.websvc.saas.model.WadlSaasResource;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;

/**
 *
 * @author nam
 */
public class WadlSaasNodeChildren extends SaasNodeChildren<Object> {
    
    public WadlSaasNodeChildren(WadlSaas wadlSaas) {
        super(wadlSaas);
    }
    
    @Override
    public WadlSaas getSaas() {
        return (WadlSaas) super.getSaas();
    }
    
    @Override
    protected void updateKeys() {
        State state = getSaas().getState();
        if (state == Saas.State.READY) {
            ArrayList<Object> keys = new ArrayList<Object>();
            List<WadlSaasResource> resources = getSaas().getResources();
            Collections.sort(resources);
            keys.addAll(resources);
            
            List<SaasMethod> methods = getSaas().getMethods();
            Collections.sort(methods);
            keys.addAll(methods);
            
            setKeys(keys);
        } else if (state == Saas.State.INITIALIZING) {
            setKeys(WAIT_HOLDER);
        } else {
            setKeys(Collections.emptyList());
        }
    }
    
    @Override
    protected Node[] createNodes(Object key) {
        if (key == WAIT_HOLDER[0]) {
            return getWaitNode();
        }
        try {
            if (key instanceof WadlSaasMethod) {
                WadlSaasMethod wsm = (WadlSaasMethod) key;
                return new Node[] { new WadlMethodNode(wsm) };
            } else if (key instanceof WadlSaasResource) {
                return new Node[] { new ResourceNode((WadlSaasResource)key) };
            }
        } catch(Exception ex) {
            Exceptions.printStackTrace(ex);
        }
        return new Node[0];
    }

}
