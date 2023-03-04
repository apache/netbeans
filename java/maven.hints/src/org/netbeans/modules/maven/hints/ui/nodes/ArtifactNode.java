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
package org.netbeans.modules.maven.hints.ui.nodes;

import java.awt.Image;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.modules.maven.indexer.api.NBVersionInfo;

import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;

/**
 *
 * @author Anuradha G
 */
public class ArtifactNode extends AbstractNode {
    private static final @StaticResource String ARTIFACT_BADGE = "org/netbeans/modules/maven/hints/ArtifactBadge.png";

    private List<NBVersionInfo> versionInfos;
    private final ArtifactNodeChildren myChildren;
    
    public ArtifactNode(String name, List<NBVersionInfo> list) {
        super(new ArtifactNodeChildren(list));
        myChildren = (ArtifactNodeChildren)getChildren();
        this.versionInfos=list;
        setName(name);
        setDisplayName(name);
    }

    @Override
    public Image getIcon(int arg0) {
        return ImageUtilities.loadImage(ARTIFACT_BADGE, true); //NOI18N
    }

    @Override
    public Image getOpenedIcon(int arg0) {
        return getIcon(arg0);
    }

    public List<NBVersionInfo> getVersionInfos() {
        return new ArrayList<NBVersionInfo>(versionInfos);
    }
    
    public void setVersionInfos(List<NBVersionInfo> infos) {
        versionInfos = infos;
        myChildren.setNewKeys(infos);
    }

    static class ArtifactNodeChildren extends Children.Keys<NBVersionInfo> {

        private List<NBVersionInfo> keys;

        public ArtifactNodeChildren(List<NBVersionInfo> keys) {
            this.keys = keys;
        }

        @Override
        protected Node[] createNodes(NBVersionInfo arg0) {
            return new Node[]{new VersionNode(arg0, arg0.isJavadocExists(),
                        arg0.isSourcesExists())
                    };
        }

        @Override
        protected void addNotify() {
            setKeys(keys);
        }

        protected void setNewKeys(List<NBVersionInfo> keys) {
            this.keys = keys;
            setKeys(keys);
        }
    }
}
