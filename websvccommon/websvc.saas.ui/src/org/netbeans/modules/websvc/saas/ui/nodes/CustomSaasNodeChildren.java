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
import org.netbeans.modules.websvc.saas.model.CustomSaas;
import org.netbeans.modules.websvc.saas.model.CustomSaasMethod;
import org.netbeans.modules.websvc.saas.model.Saas;
import org.netbeans.modules.websvc.saas.model.SaasMethod;
import org.openide.nodes.Node;

/**
 *
 * @author nam
 */
public class CustomSaasNodeChildren extends SaasNodeChildren<SaasMethod> {
    public CustomSaasNodeChildren(CustomSaas saas) {
        super(saas);
    }

    @Override
    public CustomSaas getSaas() {
        return (CustomSaas) super.getSaas();
    }
    
    @Override
    protected void updateKeys() {
        if (getSaas().getState() == Saas.State.READY) {
            List<SaasMethod> methods = getSaas().getMethods();
            Collections.sort(methods);
            setKeys(methods);
        } else {
            java.util.List<SaasMethod> emptyList = Collections.emptyList();
            setKeys(emptyList);
        }
    }
    
    @Override
    protected void addNotify() {
        super.addNotify();
        updateKeys();
    }

    @Override
    protected void removeNotify() {
        java.util.List<SaasMethod> emptyList = Collections.emptyList();
        setKeys(emptyList);
        super.removeNotify();
    }

    @Override
    protected Node[] createNodes(SaasMethod key) {
        return new Node[] { new CustomMethodNode((CustomSaasMethod) key) };
    }

}
