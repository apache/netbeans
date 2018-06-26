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

import java.awt.Image;
import java.io.File;
import java.util.Collections;
import org.netbeans.api.project.Project;
import org.netbeans.api.queries.VisibilityQuery;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import static org.netbeans.modules.maven.j2ee.ui.nodes.Bundle.*;


/**
 * filter node for display of web sources
 * @author  Milos Kleint 
 */
@NbBundle.Messages("LBL_Web_Pages=Web Pages")
class WebPagesNode extends FilterNode {
    private boolean isTopLevelNode = false;
    private Project project;
    private FileObject file;
    
    WebPagesNode(Project proj, Node orig, File root) {
        this(proj, orig, root, true);
    }
    
    private WebPagesNode(Project proj, Node orig, File root, boolean isTopLevel) {
        //#142744 if orig child is leaf, put leave as well.
        super(orig, orig.getChildren() == Children.LEAF ? Children.LEAF : new WebAppFilterChildren(proj, orig, root));
        this.project = proj;
        isTopLevelNode = isTopLevel;
        if (isTopLevel) {
            file = FileUtil.toFileObject(root);
        }
    }
    
    @Override
    public String getDisplayName() {
        if (isTopLevelNode) {
            String webRootPath = file.getPath();
            String displayName;

            // To preserve current behavior, don't show web root name in the node name for default "webapp"
            if (webRootPath.endsWith("src/main/webapp")) { // NOI18N
                displayName = LBL_Web_Pages();
            } else {
                // Remove project path from the display name --> In case of
                // /tmp/something/projectName/src/main/resources/deployment
                // we will have only src/main/resources/deployment
                String projectPath = project.getProjectDirectory().getPath();
                displayName = LBL_Web_Pages() + " (" + webRootPath.replaceAll(projectPath, "") + ")"; // NOI18N
            }

            try {
                displayName = file.getFileSystem().getDecorator().annotateName(displayName, Collections.singleton(file));
            } catch (FileStateInvalidException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
            
            return displayName;
        }
        return getOriginal().getDisplayName();
        
    }

    @Override
    public String getHtmlDisplayName() {
        if (!isTopLevelNode) {
            return getOriginal().getHtmlDisplayName();
        }
         try {
            String s = LBL_Web_Pages();
            String result = file.getFileSystem().getDecorator().annotateNameHtml (
                s, Collections.singleton(file));

            //Make sure the super string was really modified
            if (result != null && !s.equals(result)) {
                return result;
            }
         } catch (FileStateInvalidException e) {
             ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
         }
         return super.getHtmlDisplayName();
    }    
    
    @Override
    public Image getIcon(int param) {
        Image retValue = super.getIcon(param);
        if (isTopLevelNode) {
            retValue = ImageUtilities.mergeImages(retValue,
                                             ImageUtilities.loadImage("org/netbeans/modules/maven/j2ee/ui/resources/WebPagesBadge.png"), //NOI18N
                                             8, 8);
        } 
        return retValue;
    }

    @Override
    public Image getOpenedIcon(int param) {
        Image retValue = super.getOpenedIcon(param);
        if (isTopLevelNode) {
            retValue = ImageUtilities.mergeImages(retValue,
                                             ImageUtilities.loadImage("org/netbeans/modules/maven/j2ee/ui/resources/WebPagesBadge.png"), //NOI18N
                                             8, 8);
        } 
        return retValue;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + (this.isTopLevelNode ? 1 : 0);
        hash = 67 * hash + (this.file != null ? this.file.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final WebPagesNode other = (WebPagesNode) obj;
        if (this.isTopLevelNode != other.isTopLevelNode) {
            return false;
        }
        if (this.file != other.file && (this.file == null || !this.file.equals(other.file))) {
            return false;
        }
        return true;
    }

    private static class WebAppFilterChildren extends FilterNode.Children {

        private final File root;
        private final Project project;

        private WebAppFilterChildren(Project proj, Node original, File rootpath) {
            super(original);
            root = rootpath;
            project = proj;
        }
        
        @Override
        protected Node[] createNodes(Node obj) {
            FileObject fobj = obj.getLookup().lookup(FileObject.class);
        
            if (fobj != null) {
                if (!VisibilityQuery.getDefault().isVisible(fobj)) {
                    return new Node[0];
                }
                Node n = new WebPagesNode(project, obj, root, false);
                return new Node[] {n};
            }
            Node origos = obj;
            return new Node[] { origos.cloneNode() };
        }        
    }    
}

