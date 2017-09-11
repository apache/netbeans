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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.db.explorer.node;

import org.netbeans.api.db.explorer.node.BaseNode;
import org.netbeans.api.db.explorer.node.ChildNodeFactory;
import java.awt.Image;
import java.beans.BeanInfo;
import javax.swing.Icon;
import javax.swing.UIManager;
import org.netbeans.api.db.explorer.node.NodeProvider;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 *
 * @author Rob Englander
 */
public class DriverListNode extends BaseNode {
    private static final String NAME = "Drivers";
    private static final String ICONBASE = "org/netbeans/modules/db/resources/defaultFolder.gif";
    private static final String FOLDER = "DriverList"; //NOI18N

    /** 
     * Create an instance of DriverListNode.
     * 
     * @param dataLookup the lookup to use when creating node providers
     * @return the DriverListNode instance
     */
    public static DriverListNode create(NodeDataLookup dataLookup, NodeProvider provider) {
        DriverListNode node = new DriverListNode(dataLookup, provider);
        node.setup();
        return node;
    }

    private DriverListNode(NodeDataLookup lookup, NodeProvider provider) {
        super(new ChildNodeFactory(lookup), lookup, FOLDER, provider);
    }
        
    protected void initialize() {
    }
    
    @Override
    public Image getIcon(int type) {
        Image result = null;
        if (type == BeanInfo.ICON_COLOR_16x16) {
            result = icon2Image("Nb.Explorer.Folder.icon"); // NOI18N
        }
        if (result == null) {
            result = icon2Image("Tree.closedIcon"); // NOI18N
        }
        if (result == null) {
            result = super.getIcon(type);
        }
        return result;
    }

    @Override
    public Image getOpenedIcon(int type) {
        Image result = null;
        if (type == BeanInfo.ICON_COLOR_16x16) {
            result = icon2Image("Nb.Explorer.Folder.openedIcon"); // NOI18N
        }
        if (result == null) {
            result = icon2Image("Tree.openIcon"); // NOI18N
        }
        if (result == null) {
            result = super.getOpenedIcon(type);
        }
        return result;
    }

    private static Image icon2Image(String key) {
        Object obj = UIManager.get(key);
        if (obj instanceof Image) {
            return (Image)obj;
        }

        if (obj instanceof Icon) {
            Icon icon = (Icon)obj;
            return ImageUtilities.icon2Image(icon);
        }

        return null;
    }  

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage (DriverListNode.class, "DriverListNode_DISPLAYNAME"); // NOI18N
    }

    @Override
    public String getIconBase() {
        return ICONBASE;
    }

    @Override
    public String getShortDescription() {
        return NbBundle.getMessage (DriverListNode.class, "ND_DriverList"); //NOI18N
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(DriverListNode.class);
    }
}
