/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
