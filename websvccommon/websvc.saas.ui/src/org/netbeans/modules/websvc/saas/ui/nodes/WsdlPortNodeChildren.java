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

import java.util.Collections;
import java.util.List;
import org.netbeans.modules.websvc.saas.model.WsdlSaasMethod;
import org.netbeans.modules.websvc.saas.model.WsdlSaasPort;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 *
 * @author nam
 */
public class WsdlPortNodeChildren extends Children.Keys<WsdlSaasMethod>{
    private final WsdlSaasPort port;

    public WsdlPortNodeChildren(WsdlSaasPort port) {
        this.port = port;
    }

    @Override
    protected void addNotify() {
        List<WsdlSaasMethod> methods = port.getWsdlMethods();
        Collections.sort(methods);
        setKeys(methods);
        
        super.addNotify();
    }

    @Override
    protected void removeNotify() {
        setKeys(new WsdlSaasMethod[0]);
        super.removeNotify();
    }

    @Override
    protected Node[] createNodes(WsdlSaasMethod key) {
        return new Node[] { new WsdlMethodNode(key) };
    }
}
