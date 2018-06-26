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
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.j2ee.ui.nodes;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.spi.nodes.AbstractMavenNodeList;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.common.spi.ProjectWebRootProvider;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.nodes.Node;

/**
 * This class is <i>immutable</i> and thus <i>thread safe</i>.
 *
 * @author mkleint
 * @author Martin Janicek <mjanicek@netbeans.org>
 */
@NodeFactory.Registration(projectType="org-netbeans-modules-maven",position=50)
public class WebPagesNodeFactory implements NodeFactory {

    public WebPagesNodeFactory() {
    }

    @Override
    public NodeList createNodes(Project project) {
        return new WebRootNodeList(project);
    }


    /**
     * This class is <i>immutable</i> and thus <i>thread safe</i>.
     */
    private static class WebRootNodeList extends AbstractMavenNodeList<String> implements PropertyChangeListener{

        private final Project project;


        private WebRootNodeList(Project project) {
            this.project = project;
        }
        
        @Override
        public List<String> keys() {
            List<String> keys = new ArrayList<>();

            for (FileObject webRoot : getWebRoots()) {
                String webRootPath = webRoot.getPath();
                if (webRootPath.endsWith("/")) { // NOI18N
                    webRootPath = webRootPath.substring(0, webRootPath.length() - 1);
                }
                keys.add(webRootPath);
            }
            return keys;
        }
        
        @Override
        public Node node(String key) {
            for (FileObject webRoot : getWebRoots()) {
                if (key.equals(webRoot.getPath())) {
                    DataFolder fold = DataFolder.findFolder(webRoot);
                    File webAppFolder = FileUtil.toFile(webRoot);
                    if (fold != null) {
                        return new WebPagesNode(project, fold.getNodeDelegate().cloneNode(), webAppFolder);
                    }
                }
            }
            return null;
        }

        private Collection<FileObject> getWebRoots() {
            ProjectWebRootProvider webRootProvider = project.getLookup().lookup(ProjectWebRootProvider.class);
            if (webRootProvider != null) {
                return webRootProvider.getWebRoots();
            }
            return Collections.emptyList();
        }
        
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (NbMavenProject.PROP_PROJECT.equals(evt.getPropertyName())) {
                fireChange();
            }
        }
        
        @Override
        public void addNotify() {
            NbMavenProject.addPropertyChangeListener(project, this);
        }
        
        @Override
        public void removeNotify() {
            NbMavenProject.removePropertyChangeListener(project, this);
        }
    }
}
