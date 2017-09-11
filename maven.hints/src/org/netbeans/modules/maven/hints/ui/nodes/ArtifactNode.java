/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
