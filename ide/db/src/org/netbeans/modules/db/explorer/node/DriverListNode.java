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
