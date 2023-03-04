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
package org.netbeans.modules.mercurial.ui.log;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

import java.util.*;
import org.openide.nodes.AbstractNode;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;

/**
 * Represents children of a Revision Node in Search history results table.
 *
 * @author Maros Sandor
 */
class RevisionNodeChildren extends Children.Keys<Object> implements PropertyChangeListener {

    private RepositoryRevision container;
    private SearchHistoryPanel master;
    private boolean nodesCreated;
    private final PropertyChangeListener list;

    public RevisionNodeChildren(RepositoryRevision container, SearchHistoryPanel master) {
        this.container = container;
        this.master = master;
        container.addPropertyChangeListener(RepositoryRevision.PROP_EVENTS_CHANGED, list = WeakListeners.propertyChange(this, container));
    }

    @Override
    protected void addNotify() {
        refreshKeys();
    }

    @Override
    protected void removeNotify() {
        setKeys (Collections.<Object>emptySet());
    }
    
    private void refreshKeys() {
        if (container.expandEvents()) {
            setKeys(new Object[] { new Object() });
        } else {
            setKeys(container.getEvents());
        }
    }
    
    @Override
    public void propertyChange (PropertyChangeEvent evt) {
        if (nodesCreated && RepositoryRevision.PROP_EVENTS_CHANGED.equals(evt.getPropertyName()) && evt.getSource() == container) {
            refreshKeys();
        }
    }

    @Override
    protected Node[] createNodes (Object fn) {
        nodesCreated = true;
        Node node;
        if (fn instanceof RepositoryRevision.Event) {
            node = new RevisionNode((RepositoryRevision.Event) fn, master);
        } else {
            node = new AbstractNode(Children.LEAF) {

                @Override
                public String getName () {
                    return NbBundle.getMessage(RevisionNodeChildren.class, "MSG_RevisionNodeChildren.Loading"); //NOI18N
                }
                
            };
        }
        return new Node[] { node };
    }
}

