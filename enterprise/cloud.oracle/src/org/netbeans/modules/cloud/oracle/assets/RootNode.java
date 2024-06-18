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
package org.netbeans.modules.cloud.oracle.assets;

import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.cloud.oracle.NodeProvider;
import org.netbeans.modules.cloud.oracle.OCIManager;
import org.netbeans.modules.cloud.oracle.OCISessionInitiator;
import org.netbeans.modules.cloud.oracle.items.OCIItem;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Jan Horvath
 */
public class RootNode {

    static Node instance() {
        return new AbstractNode(
                Children.create(new AssetsChildren(OCIManager.getDefault().getActiveSession()), true));
    }
    
    static class AssetsChildren extends ChildFactory<OCIItem> implements ChangeListener {

        OCISessionInitiator session;

        public AssetsChildren(OCISessionInitiator session) {
            this.session = session;
            CloudAssets.getDefault().addChangeListener(this);

        }

        @Override
        protected boolean createKeys(List<OCIItem> toPopulate) {
            toPopulate.addAll(CloudAssets.getDefault().getItems());
            return true;
        }

        @Override
        protected Node[] createNodesForKey(OCIItem key) {
            NodeProvider nodeProvider = Lookups.forPath(
                    String.format("Cloud/Oracle/%s/Nodes", key.getKey().getPath()))
                    .lookup(NodeProvider.class);
            return new Node[]{nodeProvider.apply(key, session)};
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            refresh(false);
        }

    }
}
