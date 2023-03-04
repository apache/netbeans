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
import org.netbeans.api.db.explorer.node.ChildNodeFactory;
import org.netbeans.api.db.explorer.node.NodeProvider;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author Rob Englander
 */
public class ForeignKeyListNode extends BaseNode {
    private static final String NAME = "Foreign Keys"; // NOI18N
    private static final String ICONBASE = "org/netbeans/modules/db/resources/folder.gif";
    private static final String FOLDER = "ForeignKeyList"; //NOI18N

    /**
     * Create an instance of ForeignKeyListNode.
     *
     * @param dataLookup the lookup to use when creating node providers
     * @return the ForeignKeyListNode instance
     */
    public static ForeignKeyListNode create(NodeDataLookup dataLookup, NodeProvider provider) {
        ForeignKeyListNode node = new ForeignKeyListNode(dataLookup, provider);
        node.setup();
        return node;
    }

    private ForeignKeyListNode(NodeDataLookup lookup, NodeProvider provider) {
        super(new ChildNodeFactory(lookup), lookup, FOLDER, provider);
    }

    protected void initialize() {
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage (ForeignKeyListNode.class, "ForeignKeyListNode_DISPLAYNAME"); // NOI18N
    }

    @Override
    public String getIconBase() {
        return ICONBASE;
    }

    @Override
    public String getShortDescription() {
        return NbBundle.getMessage (ForeignKeyListNode.class, "ND_ForeignKeyList"); //NOI18N
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(ForeignKeyListNode.class);
    }
}
