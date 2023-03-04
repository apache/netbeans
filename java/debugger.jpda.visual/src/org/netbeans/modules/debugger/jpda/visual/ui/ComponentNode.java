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
package org.netbeans.modules.debugger.jpda.visual.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.Action;
import org.netbeans.modules.debugger.jpda.visual.spi.ComponentInfo;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.lookup.Lookups;

/**
 * The node representation of a remote component.
 * 
 * @author Martin Entlicher
 */
public class ComponentNode extends AbstractNode {
    
    private ComponentInfo ci;
    
    public ComponentNode(ComponentInfo ci) {
        super(ci.getSubComponents().length > 0 ? new ComponentChildren(ci) : Children.LEAF,
              Lookups.singleton(ci));
        this.ci = ci;
        ci.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
                if (Node.PROP_PROPERTY_SETS.equals(evt.getPropertyName())) {
                    firePropertySetsChange(null, null);
                }
            }
        });
        
    }

    @Override
    public String getDisplayName() {
        return ci.getDisplayName();
    }
    
    @Override
    public String getHtmlDisplayName() {
        return ci.getHtmlDisplayName();
    }

    @Override
    public Action[] getActions(boolean context) {
        return ci.getActions(context);
    }
    
    @Override
    public PropertySet[] getPropertySets() {
        return ci.getPropertySets();
    }
    
    ComponentNode findNodeFor(ComponentInfo ci) {
        if (ci.equals(this.ci)) {
            return this;
        }
        Children ch = getChildren();
        Node[] subNodes = ch.getNodes();
        for (Node n : subNodes) {
            ComponentNode cn = (ComponentNode) n;
            ComponentNode node = cn.findNodeFor(ci);
            if (node != null) {
                return node;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return super.toString()+", node's ci = "+ci;
    }
    
    private static class ComponentChildren extends Children.Keys<ComponentInfo> {
        
        private ComponentInfo ci;
        
        public ComponentChildren(ComponentInfo ci) {
            this.ci = ci;
        }

        @Override
        protected void addNotify() {
            super.addNotify();
            setKeys(ci.getSubComponents());
        }
        
        @Override
        protected Node[] createNodes(ComponentInfo key) {
            return new Node[] { new ComponentNode(key) };
        }
    }
}
