/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.cloud.oracle;

import org.netbeans.modules.cloud.oracle.items.OCIItem;
import java.awt.Image;
import java.util.List;
import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.cloud.oracle.items.CompartmentItem;
import org.openide.loaders.DataNode;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Jan Horvath
 */
public class TenancyNode extends AbstractNode {
    
    private static final String ORCL_ICON = "org/netbeans/modules/cloud/oracle/resources/tenancy.svg"; // NOI18N
    
    public TenancyNode(OCIItem tenancy) {
        super(Children.create(new TenancyChildFactory(tenancy), true), Lookups.fixed(tenancy));
        setName(tenancy.getName()); 
        setDisplayName(tenancy.getName());
        setIconBaseWithExtension(ORCL_ICON);
        setShortDescription(tenancy.getDescription());
    }
    
    @Override
    public Image getIcon(int type) {
        return badgeIcon(super.getIcon(type));
    }
    
    @Override
    public Image getOpenedIcon(int type) {
        return badgeIcon(super.getOpenedIcon(type));
    }
    
    private Image badgeIcon(Image origImg) {
        return origImg;
    }
    @Override
    public Action[] getActions(boolean context) {
        return new Action[] {
            
        };
    }
    
    public static class TenancyChildFactory extends org.openide.nodes.ChildFactory<CompartmentItem>
            implements ChangeListener {

        private final OCIItem tenancy;

        public TenancyChildFactory(OCIItem tenancy) {
            this.tenancy = tenancy;
        }

        @Override
        protected boolean createKeys(List<CompartmentItem> toPopulate) {
            toPopulate.addAll(OCIManager.getDefault().getCompartments(tenancy.getId()));
            return true;
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            
        }

        @Override
        protected Node createNodeForKey(CompartmentItem key) {
            CompartmentNode node = new CompartmentNode(key);
            return node;
        }
    }
    
}
