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

import java.awt.Image;
import javax.swing.Action;
import org.netbeans.api.annotations.common.StaticResource;

import org.netbeans.modules.maven.indexer.api.NBArtifactInfo;
import org.netbeans.modules.maven.indexer.api.NBVersionInfo;

import org.netbeans.modules.maven.indexer.api.RepositoryInfo;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;

/**
 *
 * @author mkleint
 * @author Anuradha G
 */
public class ArtifactNode extends AbstractNode {

    private static final @StaticResource String ARTIFACT_BADGE = "org/netbeans/modules/maven/repository/ArtifactBadge.png";

    public ArtifactNode(RepositoryInfo info,String groupId, String artifactId) {
        super(Children.create(new ArtifactChildren(info,groupId, artifactId), true));
        setName(artifactId);
        setDisplayName(artifactId);
    }

    public ArtifactNode(final RepositoryInfo info,final NBArtifactInfo artifactInfo) {
        super(new Children.Keys<NBVersionInfo>() {

            @Override
            protected Node[] createNodes(NBVersionInfo arg0) {


                return new Node[]{new VersionNode(info,arg0,arg0.isJavadocExists(),
                    arg0.isSourcesExists(), arg0.getGroupId() != null)
                };
            }

            @Override
            protected void addNotify() {
                super.addNotify();
                setKeys(artifactInfo.getVersionInfos());
            }
            });
        setName(artifactInfo.getName());
        setDisplayName(artifactInfo.getName());
    }

    @Override
    public Image getIcon(int arg0) {
        Image badge = ImageUtilities.loadImage(ARTIFACT_BADGE, true);
        return badge;
    }

    @Override
    public Image getOpenedIcon(int arg0) {
        return getIcon(arg0);
    }

    public @Override Action[] getActions(boolean context) {
        return new Action[0];
    }
}
