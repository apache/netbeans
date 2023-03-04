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
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.websvc.saas.model.Saas;
import org.netbeans.modules.websvc.saas.model.SaasServicesModel;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;

/**
 *
 * @author nam
 */
public abstract class SaasNodeChildren<T> extends Children.Keys<T> implements PropertyChangeListener {
    private Saas saas;
    
    public SaasNodeChildren(Saas saas) {
        this.saas = saas;
        SaasServicesModel model = SaasServicesModel.getInstance();
        model.addPropertyChangeListener(WeakListeners.propertyChange(this, model));
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getSource() == saas && evt.getPropertyName().equals(Saas.PROP_STATE)) {
            updateKeys();
        }
    }
    
    public Saas getSaas() {
        return saas;
    }
    
    @Override
    protected void addNotify() {
        saas.toStateReady(false);
        updateKeys();
        super.addNotify();
    }

    @Override
    protected void removeNotify() {
        List<T> emptyList = Collections.emptyList();
        setKeys(emptyList);
        super.removeNotify();
    }
    
    protected abstract void updateKeys();

    protected static final Object[] WAIT_HOLDER = new Object[] { new Object() };
    protected static Node[] getWaitNode() {
        AbstractNode wait = new AbstractNode(Children.LEAF);
        wait.setName(NbBundle.getMessage(WsdlSaasNodeChildren.class, "NODE_LOAD_MSG")); // NOI18N
        wait.setIconBaseWithExtension("org/netbeans/modules/websvc/saas/ui/resources/wait.gif"); // NOI18N
        return new Node[] { wait };
    }

}
