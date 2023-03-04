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

