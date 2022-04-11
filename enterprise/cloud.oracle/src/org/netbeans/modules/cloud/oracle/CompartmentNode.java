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
import java.util.List;
import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.cloud.oracle.items.CompartmentItem;
import org.netbeans.modules.cloud.oracle.items.DatabaseItem;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Jan Horvath
 */
public class CompartmentNode extends AbstractNode {
    
    private static final String COMPARTMENT_ICON = "org/netbeans/modules/cloud/oracle/resources/compartment.svg"; // NOI18N
    
    public CompartmentNode(CompartmentItem compartment) {
        super(Children.create(
                new CompartmentNode.CompartmentChildFactory(compartment), true), 
                Lookups.fixed(compartment));
        setName(compartment.getName()); 
        setDisplayName(compartment.getName());
        setIconBaseWithExtension(COMPARTMENT_ICON);
    }
    
    @Override
    public Action[] getActions(boolean context) {
        return Utilities.actionsForPath("Cloud/Oracle/Compartment/Actions").toArray(new Action[0]); // NOI18N
    }
    
    public static class CompartmentChildFactory extends org.openide.nodes.ChildFactory<DatabaseItem>
            implements ChangeListener {

        private final String compartmentId;

        public CompartmentChildFactory(CompartmentItem compartment) {
            this.compartmentId = compartment.getId();
            compartment.addChangeListener(this);
        }

        @Override
        protected boolean createKeys(List<DatabaseItem> toPopulate) {
            toPopulate.addAll(OCIManager.getDefault().getDatabases(compartmentId));
            return true;
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            refresh(false);
        }

        @Override
        protected Node createNodeForKey(DatabaseItem key) {
            AbstractNode node = new DatabaseNode(key);
            return node;
        }
    }
}
