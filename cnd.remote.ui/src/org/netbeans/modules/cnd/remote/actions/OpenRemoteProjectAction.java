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

package org.netbeans.modules.cnd.remote.actions;

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

/**
 *
 */
@ActionID(id = "org.netbeans.modules.cnd.remote.actions.OpenRemoteProjectAction", category = "Project")
@ActionRegistration(iconInMenu = true, 
        displayName = "#OpenRemoteProjectAction.submenu.title", //NOI18N
        iconBase="org/netbeans/modules/cnd/remote/ui/resources/openProject.png", //NOI18N
        lazy = false)
@ActionReferences({
    //@ActionReference(path = "Menu/File", position = 520),
    @ActionReference(path = "Toolbars/Remote", position = 2000)
})
public class OpenRemoteProjectAction extends RemoteOpenActionBase {

    private final ImageIcon icon;
    
    public OpenRemoteProjectAction() {
        super(NbBundle.getMessage(OpenRemoteProjectAction.class, "OpenRemoteProjectAction.submenu.title")); //NOI18N
        icon = ImageUtilities.loadImageIcon("org/netbeans/modules/cnd/remote/ui/resources/openProject.png", false); //NOI18N
        putValue("iconBase","org/netbeans/modules/cnd/remote/ui/resources/openProject.png"); //NOI18N
    }

    @Override
    protected Icon getIcon() {
        return icon;
    }

    @Override
    protected void updateToolTip() {
        ServerRecord rec = ServerList.getDefaultRecord();
        putValue(Action.SHORT_DESCRIPTION, NbBundle.getMessage(OpenRemoteFileAction.class, "OpenRemoteProjectAction.tooltip", 
                (rec == null/*paranoia*/) ? "?" : rec.getDisplayName())); //NOI18N
    }

    @Override
    protected String getSubmenuTitle() {
        return NbBundle.getMessage(OpenRemoteProjectAction.class, "OpenRemoteProjectAction.submenu.title"); //NOI18N
    }

    @Override
    protected String getItemTitle(String record) {
        return NbBundle.getMessage(OpenRemoteProjectAction.class, "OpenRemoteProjectAction.item.title", record); //NOI18N
    }        

    @Override
    protected String getPerformerID() {
        return "CND/Toobar/Services/OpenRemoteProject"; //NOI18N
    }
}
