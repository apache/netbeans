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
package org.netbeans.modules.maven.nodes;

import java.util.Collections;
import javax.swing.Action;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.queries.VisibilityQuery;
import org.netbeans.modules.maven.ActionProviderImpl;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.modules.maven.execute.model.NetbeansActionMapping;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.loaders.DataObject;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import static org.netbeans.modules.maven.nodes.Bundle.*;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;


/**
 * filter node for display of site sources
 * @author  Milos Kleint
 */
class SiteDocsNode extends FilterNode {
    private static final @StaticResource String PSITE_BADGE = "org/netbeans/modules/maven/projectsite-badge.png";
    
    private NbMavenProjectImpl project;
    private boolean isTopLevelNode = false;
    
    SiteDocsNode(NbMavenProjectImpl proj, Node orig) {
        this(proj, orig, true);
    }
    
    private SiteDocsNode(NbMavenProjectImpl proj, Node orig, boolean isTopLevel) {
        //#142744 if orig child is leaf, put leave as well.
        super(orig, orig.getChildren() == Children.LEAF ? Children.LEAF : new SiteFilterChildren(proj, orig));
        isTopLevelNode = isTopLevel;
        project = proj;
    }
    
   
    @Override
    @Messages("LBL_Site_Pages=Project Site")
    public String getDisplayName() {
        if (isTopLevelNode) {
            String s = LBL_Site_Pages();
            DataObject dob = getOriginal().getLookup().lookup(DataObject.class);
            FileObject file = dob.getPrimaryFile();
            try {
                s = file.getFileSystem().getDecorator().annotateName(s, Collections.singleton(file));
            } catch (FileStateInvalidException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
            
            return s;
        }
        return getOriginal().getDisplayName();
        
    }

    @Override
    public String getHtmlDisplayName() {
        if (!isTopLevelNode) {
            return getOriginal().getHtmlDisplayName();
        }
         try {
            DataObject dob = getOriginal().getLookup().lookup(DataObject.class);
            FileObject file = dob.getPrimaryFile();
             String s = LBL_Site_Pages();
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
    @Messages({"BTN_Generate_Site=Generate Site", "BTN_Deploy_Site=Deploy Site"})
    public javax.swing.Action[] getActions(boolean param) {
        if (isTopLevelNode) {
            Action[] toReturn = new Action[4];
            toReturn[0] = CommonProjectActions.newFileAction();
            toReturn[1] = null;
            NetbeansActionMapping mapp = new NetbeansActionMapping();
            mapp.addGoal("site"); //NOI18N
            toReturn[2] = ActionProviderImpl.createCustomMavenAction(BTN_Generate_Site(), mapp, true, Lookup.EMPTY, project);
            mapp = new NetbeansActionMapping();
            mapp.addGoal("site:deploy"); //NOI18N
            toReturn[3] = ActionProviderImpl.createCustomMavenAction(BTN_Deploy_Site(), mapp, false, Lookup.EMPTY, project);
            return toReturn;
        } else {
            return super.getActions(param);
        }
    }    

    @Override
    public java.awt.Image getIcon(int param) {
        java.awt.Image retValue = super.getIcon(param);
        if (isTopLevelNode) {
            retValue = ImageUtilities.mergeImages(retValue,
                                             ImageUtilities.loadImage(PSITE_BADGE), //NOI18N
                                             8, 8);
        } 
        return retValue;
    }

    @Override
    public java.awt.Image getOpenedIcon(int param) {
        java.awt.Image retValue = super.getOpenedIcon(param);
        if (isTopLevelNode) {
            retValue = ImageUtilities.mergeImages(retValue,
                                             ImageUtilities.loadImage(PSITE_BADGE), //NOI18N
                                             8, 8);
        } 
        return retValue;
    }
    
    static class SiteFilterChildren extends FilterNode.Children {
        private final NbMavenProjectImpl project;
        SiteFilterChildren(NbMavenProjectImpl proj, Node original) {
            super(original);
            project = proj;
        }
        
        @Override
        protected Node[] createNodes(Node obj) {
            DataObject dobj = (obj).getLookup().lookup(DataObject.class);
        
            if (dobj != null) {
                if (!VisibilityQuery.getDefault().isVisible(dobj.getPrimaryFile())) {
                    return new Node[0];
                }
                Node n = new SiteDocsNode(project, obj, false);
                return new Node[] {n};
            }
            Node origos = obj;
            return new Node[] { origos.cloneNode() };
        }        
    }    
}

