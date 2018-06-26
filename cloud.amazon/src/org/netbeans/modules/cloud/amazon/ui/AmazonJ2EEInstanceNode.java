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
package org.netbeans.modules.cloud.amazon.ui;

import java.awt.Image;
import javax.swing.Action;
import org.netbeans.modules.cloud.amazon.serverplugin.AmazonJ2EEInstance;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.ImageUtilities;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;

/**
 *
 */
public class AmazonJ2EEInstanceNode extends AbstractNode {
    
    public static final String TOMCAT_ICON = "org/netbeans/modules/cloud/amazon/ui/resources/tomcat.png"; // NOI18N
    
    private AmazonJ2EEInstance aij;
    
    public AmazonJ2EEInstanceNode(AmazonJ2EEInstance aij) {
        super(Children.LEAF, Lookups.fixed(aij));
        this.aij = aij;
        setName(""); // NOI18N
        setDisplayName(aij.getDisplayName());
        //setShortDescription(NbBundle.getMessage(RootNode.class, "Amazon_Node_Short_Description"));
        setIconBaseWithExtension(TOMCAT_ICON);
    }
    
    void showServerType() {
        setDisplayName(aij.getEnvironmentName()+" ["+aij.getApplicationName()+"]"+" on "+aij.getContainerType());
    }

    @Override
    public Image getIcon(int type) {
        return badgeIcon(super.getIcon(type));
    }
    
    @Override
    public Image getOpenedIcon(int type) {
        return badgeIcon(super.getOpenedIcon(type));
    }   
    
    private static final String RUNNING_ICON 
            = "org/netbeans/modules/cloud/amazon/ui/resources/running.png"; // NOI18N
    private static final String WAITING_ICON
            = "org/netbeans/modules/cloud/amazon/ui/resources/waiting.png"; // NOI18N
    private static final String TERMINATED_ICON
            = "org/netbeans/modules/cloud/amazon/ui/resources/terminated.png"; // NOI18N
    
    private Image badgeIcon(Image origImg) {
        Image badge = null;        
        switch (aij.getState()) {
            case UPDATING:
            case LAUNCHING:
            case TERMINATING:
                badge = ImageUtilities.loadImage(WAITING_ICON);
                break;
            case READY:
                badge = ImageUtilities.loadImage(RUNNING_ICON);
                break;
            case TERMINATED:
                badge = ImageUtilities.loadImage(TERMINATED_ICON);
                break;
        }
        return badge != null ? ImageUtilities.mergeImages(origImg, badge, 15, 8) : origImg;
    }

    @Override
    public Action[] getActions(boolean context) {
        return new Action[] {
            SystemAction.get(RemoteServerPropertiesAction.class)
        };
    }
    
}
