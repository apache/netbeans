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


package org.netbeans.modules.derby;

import java.awt.Dialog;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.derby.ui.CreateDatabasePanel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;

/**
 *
 * @author   Petr Jiricka
 */
public class CreateDatabaseAction extends CallableSystemAction {

    /** Generated sreial version UID. */
    //static final long serialVersionUID =3322896507302889271L;

    public CreateDatabaseAction() {
        putValue("noIconInMenu", Boolean.TRUE);
    }    
    
    public void performAction() {
        if (!Util.checkInstallLocation()) {
            return;
        }
        if (!Util.ensureSystemHome()) {
            return;
        }
        
        String derbySystemHome = DerbyOptions.getDefault().getSystemHome();
        CreateDatabasePanel panel = new CreateDatabasePanel(derbySystemHome);
        DialogDescriptor desc = new DialogDescriptor(panel, NbBundle.getMessage(CreateDatabaseAction.class, "LBL_CreateDatabaseTitle"), true, null);
        desc.createNotificationLineSupport();
        panel.setDialogDescriptor(desc);
        Dialog dialog = DialogDisplayer.getDefault().createDialog(desc);
        panel.setIntroduction();
        String acsd = NbBundle.getMessage(CreateDatabaseAction.class, "ACSD_CreateDatabaseAction");
        dialog.getAccessibleContext().setAccessibleDescription(acsd);
        dialog.setVisible(true);
        dialog.dispose();
        
        if (!DialogDescriptor.OK_OPTION.equals(desc.getValue())) {
            return;
        }
        
        String databaseName = panel.getDatabaseName();
        String user = panel.getUser();
        String password = panel.getPassword();
        
        // if only the username or password is null, ensure they are both null
        if (user == null || password == null) {
            user = null;
            password = null;
        }
        
        try {
            makeDatabase(databaseName, user, password);
        } catch (Exception e) {
            Logger.getLogger("global").log(Level.WARNING, null, e);
        }
    }

    void makeDatabase(String dbname, String user, String password) throws Exception {
        RegisterDerby.getDefault().postCreateNewDatabase(dbname, user, password);
    }

    protected boolean asynchronous() {
        return false;
    }

    @Override
    public boolean isEnabled() {
        return Util.hasInstallLocation();
    }
    
    /** Gets localized name of action. Overrides superclass method. */
    public String getName() {
        return NbBundle.getBundle(CreateDatabaseAction.class).getString("LBL_CreateDBAction");
    }

    /** Gets the action's help context. Implemenst superclass abstract method. */
    public HelpCtx getHelpCtx() {
        return new HelpCtx(CreateDatabaseAction.class);
    }

}
