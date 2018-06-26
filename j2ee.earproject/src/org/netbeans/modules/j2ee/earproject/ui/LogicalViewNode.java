/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
