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

package org.netbeans.modules.maven.jaxws.nodes;

import java.awt.Image;
import java.beans.BeanInfo;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.project.api.WebServiceData;
import org.netbeans.spi.project.ui.PrivilegedTemplates;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.openide.actions.FindAction;
import org.openide.actions.PasteAction;
import org.openide.actions.PropertiesAction;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;

public class JaxWsClientRootNode extends AbstractNode {
    private Project project;
    private static final String SERVICES_BADGE = "org/netbeans/modules/maven/jaxws/resources/webservicegroup.png"; // NOI18N
    private Icon folderIconCache;
    private Icon openedFolderIconCache;
    private java.awt.Image cachedServicesBadge;
    
    public JaxWsClientRootNode(Project project, WebServiceData wsData) {
        super(new JaxWsClientRootChildren(wsData), Lookups.fixed(project, new WsPrivilegedTemplates()));
        setDisplayName(NbBundle.getBundle(JaxWsClientRootNode.class).getString("LBL_ServiceReferences"));
    }
    
    @Override
    public Image getIcon( int type ) {
        return computeIcon( false );
    }
    
    @Override
    public Image getOpenedIcon( int type ) {
        return computeIcon( true );
    }
    
    private java.awt.Image getServicesImage() {
        if (cachedServicesBadge == null) {
            cachedServicesBadge = ImageUtilities.loadImage(SERVICES_BADGE);
        }            
        return cachedServicesBadge;        
    }
    
    /**
     * Returns Icon of folder on active platform
     * @param opened should the icon represent opened folder
     * @return the folder icon
     */
    private Icon getFolderIcon (boolean opened) {
        if (openedFolderIconCache == null) {
            Node n = DataFolder.findFolder(FileUtil.getConfigRoot()).getNodeDelegate();
            openedFolderIconCache = new ImageIcon(n.getOpenedIcon(BeanInfo.ICON_COLOR_16x16));
            folderIconCache = new ImageIcon(n.getIcon(BeanInfo.ICON_COLOR_16x16));
        }
        if (opened) {
            return openedFolderIconCache;
        }
        else {
            return folderIconCache;
        }
    }

    private Image computeIcon( boolean opened) {        
        Icon icon = getFolderIcon(opened);
        Image image = ((ImageIcon)icon).getImage();
        image = ImageUtilities.mergeImages(image, getServicesImage(), 7, 7 );
        return image;        
    }

    @Override
    public Action[] getActions(boolean context) {
        return new Action[]{
            CommonProjectActions.newFileAction(),
            null,
            SystemAction.get(FindAction.class),
            null,
            SystemAction.get(PasteAction.class),
            null,
            SystemAction.get(PropertiesAction.class)
        };
    }
    
    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    

    private static class WsPrivilegedTemplates implements PrivilegedTemplates {

        @Override
        public String[] getPrivilegedTemplates() {
            return new String[] {
                "Templates/WebServices/WebServiceClient", // NOI18N
                "Templates/WebServices/MessageHandler.java", // NOI18N
                "Templates/WebServices/LogicalHandler.java" // NOI18N
            };
        }
    }

}
