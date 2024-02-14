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

package org.netbeans.modules.websvc.saas.ui.nodes;

import java.awt.datatransfer.Transferable;
import java.util.List;
import org.openide.util.ImageUtilities;
import org.openide.util.actions.SystemAction; 
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import javax.swing.Action;
import org.netbeans.modules.websvc.saas.model.SaasGroup;
import org.netbeans.modules.websvc.saas.model.SaasServicesModel;
import org.netbeans.modules.websvc.saas.ui.actions.AddGroupAction;
import org.netbeans.modules.websvc.saas.ui.actions.AddServiceAction;
import org.netbeans.modules.websvc.saas.ui.actions.DeleteGroupAction;
import org.netbeans.modules.websvc.saas.ui.actions.RenameGroupAction;
import org.netbeans.modules.websvc.saas.util.SaasUtil;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.nodes.AbstractNode;
import org.openide.util.datatransfer.PasteType;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 * Node representing Group of Web Services
 * @author nam
 */
public class SaasGroupNode extends AbstractNode {
    private final SaasGroup group;

    public SaasGroupNode(SaasGroup group) {
        this(group, new InstanceContent());
    }
    
    protected SaasGroupNode(SaasGroup group, InstanceContent content) {
        super(new SaasGroupNodeChildren(group), new AbstractLookup(content));
        this.group = group;
        content.add(group);
    }    

    @Override
    public String getName() {
        return group.getName();
    }
    
    @Override
    public void setName(String name){
        if (group.isUserDefined()) {
            super.setName(name);
            group.setName(name);
        }
    }
    
    @Override
    public boolean canRename() {
        return group.isUserDefined();
    }

    private Image getUserDirFolderImage(int type, boolean openIcon) {
        FileObject folder = FileUtil.toFileObject(new File(System.getProperty("netbeans.user"))); //NOI18N
        if (folder != null) {
            DataFolder df = DataFolder.findFolder(folder);
            if (df != null) {
                return (openIcon ? df.getNodeDelegate().getOpenedIcon(type) : df.getNodeDelegate().getIcon(type));
            }
        }
        return null;
    }

    private Image vendorIcon = null;
    private Image getVendorIcon(int type) {
        if (vendorIcon == null && group.getServices().size() > 0) {
            vendorIcon = SaasUtil.loadIcon(group, type);
        }
        return vendorIcon;
    }
    
    @Override
    public Image getIcon(int type){
        Image icon = getVendorIcon(type);
        if (icon != null) {
            return icon;
        }
        Image standardFolderImage = getUserDirFolderImage(type, false);
        if (standardFolderImage != null) {
            return standardFolderImage;
        }
        return ImageUtilities.loadImage("org/netbeans/modules/websvc/saas/resources/folder-closed.png"); // NOI18N
    }
    
    @Override
    public Image getOpenedIcon(int type){
        Image icon = getVendorIcon(type);
        if (icon != null) {
            return icon;
        }
        Image standardFolderImage = getUserDirFolderImage(type, true);
        if (standardFolderImage != null) {
            return standardFolderImage;
        }
        return ImageUtilities.loadImage("org/netbeans/modules/websvc/saas/resources/folder-open.png"); // NOI18N
    }

    @Override
    public Action[] getActions(boolean context) {
        List<Action> actions = SaasNode.getActions(getLookup());
        actions.add(SystemAction.get(AddServiceAction.class));
        actions.add(SystemAction.get(AddGroupAction.class));
        actions.add(SystemAction.get(DeleteGroupAction.class));
        actions.add(SystemAction.get(RenameGroupAction.class));
        return actions.toArray(new Action[0]);
    }
    
    @Override
    public boolean canDestroy() {
        return group.isUserDefined();
    }
    
    @Override
    public void destroy() throws IOException{
        SaasServicesModel.getInstance().removeGroup(group);
        super.destroy();
    }

    @Override
    protected void createPasteTypes(final Transferable t, List<PasteType> s) {
        //TODO review original
    }
    
    
}
