/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.j2ee.earproject.ui;

import java.awt.Image;
import java.beans.BeanInfo;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.modules.java.api.common.classpath.ClassPathSupport;
import org.netbeans.modules.j2ee.earproject.EarProject;
import org.netbeans.modules.j2ee.earproject.ui.actions.AddModuleAction;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;

/**
 * Represents the <em>J2EE Modules</em> node in the EAR project's logical view.
 *
 * @author vkraemer
 * @author Ludovic Champenois
 */
public final class LogicalViewNode extends AbstractNode {
    
    static final String J2EE_MODULES_NAME = "j2ee.modules"; // NOI18N    
    private Image J2EE_MODULES_BADGE = ImageUtilities.loadImage( "org/netbeans/modules/j2ee/earproject/ui/resources/application_16.gif", true ); // NOI18N
    private static Icon folderIconCache;
    private static Icon openedFolderIconCache;	
    
    public LogicalViewNode(AntProjectHelper model, EarProject project, 
            UpdateHelper updateHelper, ClassPathSupport cs) {
        super(new LogicalViewChildren(model, project, updateHelper, cs), Lookups.fixed( new Object[] { project }));
        // Set FeatureDescriptor stuff:
        setName(J2EE_MODULES_NAME);
        setDisplayName(NbBundle.getMessage(LogicalViewNode.class, "LBL_LogicalViewNode"));
        setShortDescription(NbBundle.getMessage(LogicalViewNode.class, "HINT_LogicalViewNode"));
    }
    
    @Override
    public Image getIcon( int type ) {
        return computeIcon( false, type );
    }
    
    @Override
    public Image getOpenedIcon( int type ) {
        return computeIcon( true, type );
    }
    /**
     * Returns Icon of folder on active platform
     * @param opened should the icon represent opened folder
     * @return the folder icon
     */
    static synchronized Icon getFolderIcon (boolean opened) {
        if (openedFolderIconCache == null) {
            Node n = DataFolder.findFolder(FileUtil.getConfigRoot()).getNodeDelegate();
            openedFolderIconCache = ImageUtilities.image2Icon(n.getOpenedIcon(BeanInfo.ICON_COLOR_16x16));
            folderIconCache = ImageUtilities.image2Icon(n.getIcon(BeanInfo.ICON_COLOR_16x16));
        }
        if (opened) {
            return openedFolderIconCache;
        }
        else {
            return folderIconCache;
        }
    }

    private Image computeIcon( boolean opened, int type ) {        
        Icon icon = getFolderIcon(opened);
        Image image = ((ImageIcon)icon).getImage();
        image = ImageUtilities.mergeImages(image, J2EE_MODULES_BADGE, 7, 7 );
        return image;        
    }

    // Create the popup menu:
    @Override
    public Action[] getActions(boolean context) {
        return new Action[] {
            SystemAction.get(AddModuleAction.class),
        };
    }
    
    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
        // When you have help, change to:
        // return new HelpCtx(LogicalViewNode.class);
    }
    
    // Handle copying and cutting specially:
    @Override
    public boolean canCopy() {
        return false;
    }
    
}
