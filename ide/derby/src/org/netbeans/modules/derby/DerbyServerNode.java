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

package org.netbeans.modules.derby;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.WeakListeners;
import org.openide.util.actions.SystemAction;

/**
 * Provides a node for the local Java DB server under the Databases node
 * in the Database Explorer 
 * 
 * @author David Van Couvering
 */
public class DerbyServerNode extends AbstractNode implements Comparable {
    private static final DerbyDatabasesImpl DATABASES_IMPL = DerbyDatabasesImpl.getDefault();
    private static final ChildFactory FACTORY = new ChildFactory(DATABASES_IMPL);
    private static final DerbyServerNode DEFAULT = new DerbyServerNode(FACTORY);

    private SystemAction[] actions = new SystemAction[] {
            SystemAction.get(StartAction.class),
            SystemAction.get(StopAction.class),
            SystemAction.get(CreateDatabaseAction.class),
            SystemAction.get(CreateSampleDBAction.class),
            SystemAction.get(DerbyPropertiesAction.class)
        };
    
    // I'd like a less generic icon, but this is what we have for now...
    private static final String ICON_BASE = "org/netbeans/modules/derby/resources/catalog.gif";
    
    public static DerbyServerNode getDefault() {
        return DEFAULT;
    }
    
    private DerbyServerNode(ChildFactory f) {
        super(Children.create(f, true));
        this.setIconBaseWithExtension(ICON_BASE);
    }
    
    @Override
    public String getDisplayName() {
       // Product name - no need to internationalize
       return "Java DB"; // NOI18N
    }
   
    @Override
    public SystemAction[] getActions(boolean b) {
        return actions;
    }
    
    @Override
    public boolean canCopy() {
        return false;
    }
    
    @Override
    public boolean canCut() {
        return false;
    }
    
    @Override
    public boolean canRename() {
        return false;
    }
    
    @Override
    public boolean canDestroy() {
        return true;
    }
    
    @Override
    public SystemAction getPreferredAction() {
        return null;
    }

    @Override
    public int compareTo(Object other) {
        Node otherNode = (Node)other;
        return this.getDisplayName().compareTo(otherNode.getDisplayName());
    }

    private static class ChildFactory
            extends org.openide.nodes.ChildFactory<String> implements ChangeListener {

        private DerbyDatabasesImpl databasesImpl;

        @SuppressWarnings("LeakingThisInConstructor")
        public ChildFactory(DerbyDatabasesImpl impl) {
            this.databasesImpl = impl;
            impl.addChangeListener(
                WeakListeners.create(ChangeListener.class, this, impl));
        }

        @Override
        protected Node createNodeForKey(String db) {
            return new DerbyDatabaseNode(db, databasesImpl);
        }

        @Override
        protected boolean createKeys(List<String> toPopulate) {
            List<String> fresh = new ArrayList<String>();

            fresh.addAll(databasesImpl.getDatabases());

            Collections.sort(fresh);
            toPopulate.addAll(fresh);

            return true;
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            refresh(false);
        }

    }

}
