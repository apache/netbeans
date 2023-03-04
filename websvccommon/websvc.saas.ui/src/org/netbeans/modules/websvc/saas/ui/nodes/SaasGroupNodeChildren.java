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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.websvc.saas.model.CustomSaas;
import org.netbeans.modules.websvc.saas.model.Saas;
import org.netbeans.modules.websvc.saas.model.SaasGroup;
import org.netbeans.modules.websvc.saas.model.SaasServicesModel;
import org.netbeans.modules.websvc.saas.model.WadlSaas;
import org.netbeans.modules.websvc.saas.model.WsdlSaas;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.WeakListeners;

public class SaasGroupNodeChildren extends Children.Keys<Object> implements PropertyChangeListener {
    private SaasGroup group;

    public SaasGroupNodeChildren(SaasGroup group) {
        this.group = group;
        SaasServicesModel model = SaasServicesModel.getInstance();
        model.addPropertyChangeListener(WeakListeners.propertyChange(this, model));
    }

    protected void setGroup(SaasGroup g) {
        group = g;
    }

    @Override
    protected void addNotify() {
        updateKeys();
        super.addNotify();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getSource() == group) {
            updateKeys();
            if (evt.getNewValue() != null) {
                refreshKey(evt.getNewValue());
            } else if (evt.getOldValue() != null) {
                refreshKey(evt.getOldValue());
            }
        }
    }

    protected void updateKeys() {
        ArrayList<Object> keys = new ArrayList<Object>();
        List<SaasGroup> groups = group.getChildrenGroups();
        Collections.sort(groups);
        keys.addAll(groups);
        
        List<Saas> services = group.getServices();
        Collections.sort(services);
        keys.addAll(services);
        
        setKeys(keys);
    }

    @Override
    protected void removeNotify() {
        java.util.List<String> emptyList = Collections.emptyList();
        setKeys(emptyList);
        super.removeNotify();
    }

    @Override
    protected Node[] createNodes(Object key) {
        if (key instanceof SaasGroup) {
            SaasGroup g = (SaasGroup) key;
            SaasGroupNode node = new SaasGroupNode(g);
            return new Node[]{node};
        } else if (key instanceof WadlSaas) {
            return new Node[]{new WadlSaasNode((WadlSaas) key)};
        } else if (key instanceof WsdlSaas) {
            return new Node[]{new WsdlSaasNode((WsdlSaas) key)};
        } else if (key instanceof CustomSaas) {
            return new Node[]{new CustomSaasNode((CustomSaas) key)};
        }
        return new Node[0];
    }
}
