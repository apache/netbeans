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

package org.netbeans.modules.cnd.remote.projectui.actions;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.api.remote.ServerRecord;
import org.netbeans.modules.cnd.remote.actions.base.RemoteOpenActionBase;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;


@ActionID(id = "org.netbeans.modules.cnd.remote.actions.NewRemoteProjectAction", category = "Project")
@ActionRegistration(iconInMenu = true, displayName = "#NewRemoteProjectAction.submenu.title", lazy = false)
@ActionReferences({
    //@ActionReference(path = "Menu/File", position = 510),
    @ActionReference(path = "Toolbars/Remote", position = 1000)
})
public class NewRemoteProjectAction extends RemoteOpenActionBase {

    private ImageIcon icon;
    
    public NewRemoteProjectAction() {
        super(NbBundle.getMessage(NewRemoteProjectAction.class, "NewRemoteProjectAction.submenu.title"));
        icon = ImageUtilities.loadImageIcon("org/netbeans/modules/cnd/remote/resources/newProject.png", false); //NOI18N
        putValue("iconBase","org/netbeans/modules/cnd/remote/resources/newProject.png"); //NOI18N
    }

    @Override
    protected Icon getIcon() {
        return icon;
    }

    @Override
    protected void updateToolTip() {
        ServerRecord rec = ServerList.getDefaultRecord();
        putValue(Action.SHORT_DESCRIPTION, NbBundle.getMessage(NewRemoteProjectAction.class, "NewRemoteProjectAction.tooltip", 
                (rec == null/*paranoia*/) ? "?" : rec.getDisplayName())); //NOI18N
    }

    @Override
    protected String getSubmenuTitle() {
        return NbBundle.getMessage(NewRemoteProjectAction.class, "NewRemoteProjectAction.submenu.title"); // NOI18N
    }

    @Override
    protected String getItemTitle(String record) {
        return NbBundle.getMessage(NewRemoteProjectAction.class, "NewRemoteProjectAction.item.title", record); // NOI18N
    }        
        
    @Override
    protected String getPerformerID() {
        return "CND/Toobar/Services/NewRemoteProject"; // NOI18N
    }
}
