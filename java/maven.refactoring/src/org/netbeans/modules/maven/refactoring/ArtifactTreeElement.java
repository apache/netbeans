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

package org.netbeans.modules.maven.refactoring;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.maven.indexer.api.NBVersionInfo;
import org.netbeans.modules.maven.indexer.api.RepositoryInfo;
import org.netbeans.modules.maven.indexer.api.RepositoryPreferences;
import org.netbeans.modules.maven.indexer.api.RepositoryUtil;
import org.netbeans.modules.maven.spi.IconResources;
import org.netbeans.modules.maven.spi.nodes.NodeUtils;
import org.netbeans.modules.refactoring.spi.ui.TreeElement;
import org.netbeans.modules.refactoring.spi.ui.TreeElementFactory;
import org.openide.util.ImageUtilities;

class ArtifactTreeElement implements TreeElement {

    private static final Logger LOG = Logger.getLogger(ArtifactTreeElement.class.getName());

    private final NBVersionInfo art;

    ArtifactTreeElement(NBVersionInfo art) {
        this.art = art;
    }

    @Override public TreeElement getParent(boolean isLogical) {
        if (!art.getVersion().isEmpty()) {
            return TreeElementFactory.getTreeElement(new NBVersionInfo(art.getRepoId(), art.getGroupId(), art.getArtifactId(), "", null, null, null, null, null));
        } else if (!art.getArtifactId().isEmpty()) {
            return TreeElementFactory.getTreeElement(new NBVersionInfo(art.getRepoId(), art.getGroupId(), "", "", null, null, null, null, null));
        } else {
            RepositoryInfo repo = RepositoryPreferences.getInstance().getRepositoryInfoById(art.getRepoId());
            return repo != null ? TreeElementFactory.getTreeElement(repo) : null;
        }
    }

    @Override public Icon getIcon() {
        if (!art.getVersion().isEmpty()) {
            try {
                Project p = FileOwnerQuery.getOwner(RepositoryUtil.downloadArtifact(art).toURI());
                if (p != null) {
                    return ProjectUtils.getInformation(p).getIcon();
                }
            } catch (Exception x) {
                LOG.log(Level.FINE, null, "could not check project icon for " + art);
            }
            return ImageUtilities.loadImageIcon(IconResources.ICON_DEPENDENCY_JAR, true);
        } else if (!art.getArtifactId().isEmpty()) {
            // XXX should probably be moved into NodeUtils
            return ImageUtilities.loadImageIcon("org/netbeans/modules/maven/repository/ArtifactBadge.png", true);
        } else {
            return ImageUtilities.image2Icon(NodeUtils.getTreeFolderIcon(false));
        }
    }

    @Override public String getText(boolean isLogical) {
        if (!art.getVersion().isEmpty()) {
            return art.getVersion();
        } else if (!art.getArtifactId().isEmpty()) {
            return art.getArtifactId();
        } else {
            return art.getGroupId();
        }
    }

    @Override public Object getUserObject() {
        return art;
    }

}
