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

import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.cloud.oracle.items.OCIItem;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Jan Horvath
 */
public class OCINode extends AbstractNode {
    private RefreshListener refreshListener;

    private final OCIItem item;
    private final CloudChildFactory factory;

    public OCINode(OCIItem item) {
        this(new CloudChildFactory(item), item, Lookups.fixed(item));
    }
    
    private OCINode(CloudChildFactory factory, OCIItem item, Lookup lookup) {
        super(Children.create(factory, true), lookup);
        setName(item.getName());
        this.item = item;
        this.factory = factory;
        refreshListener = new RefreshListener();
        item.addChangeListener(refreshListener);
    }
    
    public OCINode(OCIItem item, Children children) {
        super(children, Lookups.fixed(item));
        setName(item.getName());
        this.item = item;
        this.factory = null;
        refreshListener = new RefreshListener();
        item.addChangeListener(refreshListener);
    }

    @Override
    public Action[] getActions(boolean context) {
        List<Action> result = new ArrayList<>();
        
        String path = item.getKey().getPath();
        String provider = path.substring(0, path.indexOf("/"));
        
        
        List<? extends Action> commonActions = Utilities.actionsForPath(
                String.format("Cloud/%s/Common/Actions", provider));
        for (Action commonAction : commonActions) {
            if (commonAction.isEnabled()) {
                result.add(commonAction);
            }
        }
        
        result.addAll(Utilities.actionsForPath(
                String.format("Cloud/%s/Actions",
                        item.getKey().getPath())));

        return result.toArray(new Action[0]); // NOI18N
    }
    
    public void refresh() {
        factory.refreshKeys();
    }

    @Override
    public Node.Handle getHandle() {
        return super.getHandle();
    }

    private final class RefreshListener implements ChangeListener {
        @Override
        public void stateChanged(ChangeEvent e) {
            refresh();
        }
    }
    
}
