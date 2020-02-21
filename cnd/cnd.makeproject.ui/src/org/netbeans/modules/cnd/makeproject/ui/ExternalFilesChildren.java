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
package org.netbeans.modules.cnd.makeproject.ui;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.modules.cnd.makeproject.api.configurations.Folder;
import org.netbeans.modules.cnd.makeproject.api.ui.ItemEx;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;

/**
 *
 */
final class ExternalFilesChildren extends BaseMakeViewChildren {

    public ExternalFilesChildren(Folder folder, MakeLogicalViewProvider provider) {
        super(folder, provider);
    }

    @Override
    protected Node[] createNodes(Object key) {
        if (key instanceof LoadingNode) {
            return new Node[]{(Node) key};
        }
        if (!(key instanceof ItemEx)) {
            System.err.println("wrong item in external files folder " + key); // NOI18N
            return null;
        }
        ItemEx item = (ItemEx) key;
        DataObject fileDO = item.getDataObject();
        Node node;
        if (fileDO != null && fileDO.isValid()) {
            node = new ViewItemNode(this, getFolder(), item, fileDO, provider.getProject(), true);
        } else {
            node = new BrokenViewItemNode(this, getFolder(), item, provider.getProject());
        }
        return new Node[]{node};
    }

    @Override
    protected Collection<Object> getKeys(AtomicBoolean canceled) {
        return getFolder().getElements();
    }
}
