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

package org.netbeans.modules.payara.common.nodes;

import java.util.Set;
import java.util.Vector;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.netbeans.modules.payara.common.PayaraInstanceProvider;
import org.netbeans.modules.payara.spi.PayaraModule;

/**
 *
 * @author Peter Williams
 */
public class Hk2ResourceContainers extends Children.Keys<Object> implements Refreshable {

    private Lookup lookup;
    private final static Node WAIT_NODE = Hk2ItemNode.createWaitNode();
    
    Hk2ResourceContainers(Lookup lookup) {
        this.lookup = lookup;
    }

    @Override
    public void updateKeys() {
        Vector<Hk2ItemNode> keys = new Vector<Hk2ItemNode>();
        PayaraModule commonSupport = lookup.lookup(PayaraModule.class);
        if ((commonSupport != null)
                && (commonSupport.getInstanceProvider().equals(PayaraInstanceProvider.getProvider()))) {
            String[] childTypes = NodeTypes.getChildTypes(NodeTypes.RESOURCES);
            if (childTypes != null) {
                for (int i = 0; i < childTypes.length; i++) {
                    String type = childTypes[i];
                    keys.add(new Hk2ItemNode(lookup,
                            new Hk2ResourcesChildren(lookup, type),
                            NbBundle.getMessage(Hk2ResourceContainers.class, "LBL_" + type), // NOI18N
                            DecoratorManager.findDecorator(type, Hk2ItemNode.REFRESHABLE_FOLDER, true)));
                }
            }
        } else {
            String type = PayaraModule.JDBC;
            keys.add(new Hk2ItemNode(lookup,
                    new Hk2ResourcesChildren(lookup, type),
                    NbBundle.getMessage(Hk2ResourceContainers.class, "LBL_" + type), // NOI18N
                    DecoratorManager.findDecorator(type, Hk2ItemNode.REFRESHABLE_FOLDER, true)));
        }
        setKeys(keys);
    }

    @Override
    protected void addNotify() {
        updateKeys();
    }

    @Override
    protected void removeNotify() {
        setKeys((Set<? extends Object>) java.util.Collections.EMPTY_SET);
    }

    @Override
    protected org.openide.nodes.Node[] createNodes(Object key) {
        if (key instanceof Hk2ItemNode){
            return new Node [] { (Hk2ItemNode) key };
        }

        if (key instanceof String && key.equals(WAIT_NODE)){
            return new Node [] { WAIT_NODE };
        }

        return null;
    }

}
