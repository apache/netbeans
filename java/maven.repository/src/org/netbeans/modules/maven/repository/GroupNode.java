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
package org.netbeans.modules.maven.repository;

import java.awt.Image;
import java.util.Collections;
import java.util.List;
import javax.swing.Action;

import org.netbeans.modules.maven.indexer.api.RepositoryInfo;
import org.netbeans.modules.maven.indexer.api.RepositoryQueries;
import org.netbeans.modules.maven.spi.nodes.NodeUtils;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 *
 * @author mkleint
 * @author Anuradha
 */
public class GroupNode extends AbstractNode {
    public GroupNode(RepositoryInfo info,String id) {
        super(Children.create(new GroupChildren(info, id), true));
        setName(id);
        setDisplayName(id);
    }

    static class GroupChildren extends ChildFactory<String> {

        private String id;
        private RepositoryInfo info;
        public GroupChildren(RepositoryInfo info,String group) {
            this.info = info;
            id = group;
        }

        protected @Override Node createNodeForKey(String key) {
            if (GroupListChildren.KEY_PARTIAL.equals(key)) {
                return GroupListChildren.createPartialNode();
            }
            return new ArtifactNode(info, id, key);
        }

        protected @Override boolean createKeys(List<String> toPopulate) {
            RepositoryQueries.Result<String> result = RepositoryQueries.getArtifactsResult(id, Collections.singletonList(info));
            toPopulate.addAll(result.getResults());
            if (result.isPartial()) {
                toPopulate.add(GroupListChildren.KEY_PARTIAL);
            }
            return true;
        }
    }

    @Override
    public Image getIcon(int arg0) {
        return NodeUtils.getTreeFolderIcon(false);
    }

    @Override
    public Image getOpenedIcon(int arg0) {
        return NodeUtils.getTreeFolderIcon(true);
    }

    public @Override Action[] getActions(boolean context) {
        return new Action[0];
    }
}
