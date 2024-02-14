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

package org.netbeans.modules.bugtracking.bridge.nodes;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.api.core.ide.ServicesTabNodeRegistration;
import org.netbeans.modules.bugtracking.api.Repository;
import org.netbeans.modules.bugtracking.api.RepositoryManager;
import org.netbeans.modules.bugtracking.api.Util;
import org.openide.nodes.*;
import org.openide.util.NbBundle;

/**
 * Root node representing Bugtracking in the Services window
 *
 * @author Tomas Stupka
 */
public class BugtrackingRootNode extends AbstractNode {
    
    private static final String BUGTRACKING_NODE_NAME = "bugtracking";                                       // NOI18N
    private static final String ICON_BASE = "org/netbeans/modules/bugtracking/ui/resources/bugtracking.png"; // NOI18N
    
    /** Init lock */
    private static final Object LOCK_INIT = new Object();

    /** The only instance of the BugtrackingRootNode in the system */
    private static BugtrackingRootNode defaultInstance;
    
    /** 
     * Creates a new instance of BugtrackingRootNode
     */
    private BugtrackingRootNode() {
        super(Children.create(new RootNodeChildren(), true));
        setName(BUGTRACKING_NODE_NAME); 
        setDisplayName(NbBundle.getMessage(BugtrackingRootNode.class, "LBL_BugtrackingNode")); // NOI18N
        setIconBaseWithExtension(ICON_BASE);
    }
    
    /**
     * Creates default instance of BugtrackingRootNode
     *
     * @return default instance of BugtrackingRootNode
     */
    @ServicesTabNodeRegistration(
        name="bugtracking",                                                                 // NOI18N
        displayName="org.netbeans.modules.bugtracking.bridge.nodes.Bundle#LBL_BugtrackingNode", // NOI18N
        iconResource="org/netbeans/modules/bugtracking/ui/resources/bugtracking.png",       // NOI18N
        position=588
    )
    public static BugtrackingRootNode getDefault() {
        synchronized(LOCK_INIT) {
            if (defaultInstance == null) {
                defaultInstance = new BugtrackingRootNode();
            }
            return defaultInstance;
        }
    }
    
    @Override
    public Action[] getActions(boolean context) {
        return new Action[] {
            new AbstractAction(NbBundle.getMessage(BugtrackingRootNode.class, "LBL_CreateRepository")) { // NOI18N
                @Override
                public void actionPerformed(ActionEvent e) {
                    Util.createRepository();
                }
            }
        };
    }
    
    private static class RootNodeChildren extends ChildFactory<Repository> implements PropertyChangeListener  {

        /**
         * Creates a new instance of RootNodeChildren
         */
        public RootNodeChildren() {
            RepositoryManager.getInstance().addPropertChangeListener(this);
        }

        @Override
        protected Node createNodeForKey(Repository key) {
            return new RepositoryNode(key);
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if(evt.getPropertyName().equals(RepositoryManager.EVENT_REPOSITORIES_CHANGED)) {
                refresh(false);
            }
        }

        @Override
        protected boolean createKeys(List<Repository> toPopulate) {
            Collection<Repository> repos = RepositoryManager.getInstance().getRepositories();
            
            // populate only mutable repositories -> those that the user can edit or delete
            Iterator<Repository> it = repos.iterator();
            while(it.hasNext()) {
                Repository repo = it.next();
                if(repo.isMutable()) {
                    toPopulate.add(repo);
                }
            }
            
            toPopulate.sort(new RepositoryComparator());
            return true;
        }
    }

    private static class RepositoryComparator implements Comparator<Repository> {
        @Override
        public int compare(Repository r1, Repository r2) {
            if(r1 == null && r2 == null) return 0;
            if(r1 == null) return -1;
            if(r2 == null) return 1;
            return r1.getDisplayName().compareTo(r2.getDisplayName());
        }
    }
}
