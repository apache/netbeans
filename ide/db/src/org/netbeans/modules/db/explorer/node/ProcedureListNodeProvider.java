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

package org.netbeans.modules.db.explorer.node;

import org.netbeans.api.db.explorer.node.BaseNode;
import org.netbeans.api.db.explorer.node.NodeProviderFactory;
import org.openide.util.Lookup;

/**
 *
 * @author Rob Englander
 */
public class ProcedureListNodeProvider extends ConnectedNodeProvider {

    // lazy initialization holder class idiom for static fields is used
    // for retrieving the factory
    public static NodeProviderFactory getFactory() {
        return FactoryHolder.FACTORY;
    }

    private static class FactoryHolder {
        static final NodeProviderFactory FACTORY = new NodeProviderFactory() {
            public ProcedureListNodeProvider createInstance(Lookup lookup) {
                ProcedureListNodeProvider provider = new ProcedureListNodeProvider(lookup);
                return provider;
            }
        };
    }

    private ProcedureListNodeProvider(Lookup lookup) {
        super(lookup);
    }

    @Override
    protected BaseNode createNode(NodeDataLookup lookup) {
        return ProcedureListNode.create(lookup, this);
    }
    
}
