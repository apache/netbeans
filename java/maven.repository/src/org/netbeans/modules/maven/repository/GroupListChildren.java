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
/*
 * Contributor(s): theanuradha@netbeans.org
 */
package org.netbeans.modules.maven.repository;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.List;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.maven.indexer.api.RepositoryInfo;
import org.netbeans.modules.maven.indexer.api.RepositoryQueries;
import org.netbeans.modules.maven.indexer.api.RepositoryQueries.Result;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle.Messages;
import org.openide.util.WeakListeners;
import static org.netbeans.modules.maven.repository.Bundle.*;

/**
 *
 * @author mkleint
 * @author Anuradha
 */
public class GroupListChildren extends ChildFactory.Detachable<String> implements PropertyChangeListener {

    private RepositoryInfo info;
    static final String KEY_PARTIAL = "____PARTIAL_RESULT";
    private boolean noIndex;

    public GroupListChildren(RepositoryInfo info) {
        this.info = info;
    }

    public void setInfo(RepositoryInfo info) {
        this.info = info;
        refresh(false);
    }
    
    @Override 
    @Messages("TXT_Partial_result=<No result, processing index...>")
    protected Node createNodeForKey(String key) {
        if (KEY_PARTIAL.equals(key)) {
            return createPartialNode();
        }
        return new GroupNode(info, key);
    }

    static Node createPartialNode() {
        AbstractNode node = new AbstractNode(Children.LEAF);
        node.setIconBaseWithExtension("org/netbeans/modules/maven/resources/wait.gif");
        node.setDisplayName(TXT_Partial_result());
        return node;
    }

    protected @Override boolean createKeys(List<String> toPopulate) {
        if(noIndex) {
            return true;
        }
        Result<String> result = RepositoryQueries.getGroupsResult(Collections.singletonList(info));
        toPopulate.addAll(result.getResults());
        if (result.isPartial()) {
            toPopulate.add(KEY_PARTIAL);
        }      
        return true;
    }
    
    protected @Override void addNotify() {
        info.addPropertyChangeListener(WeakListeners.propertyChange(this, info));
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if(RepositoryInfo.PROP_INDEX_CHANGE.equals(evt.getPropertyName())) {
            noIndex = false;
            refresh(false);
        } else if(RepositoryInfo.PROP_NO_REMOTE_INDEX.equals(evt.getPropertyName())) {
            noIndex = true;
            refresh(false);
        }
    }
}
