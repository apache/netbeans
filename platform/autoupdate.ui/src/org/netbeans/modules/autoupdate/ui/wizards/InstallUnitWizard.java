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

package org.netbeans.modules.autoupdate.ui.wizards;

import org.netbeans.modules.autoupdate.ui.*;
import java.awt.Dialog;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.autoupdate.ui.wizards.LazyInstallUnitWizardIterator.LazyUnit;
import org.netbeans.modules.autoupdate.ui.wizards.OperationWizardModel.OperationType;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author Jiri Rechtacek
 */
public class InstallUnitWizard {
    
    private static final Logger log = Logger.getLogger (InstallUnitWizard.class .getName());
    
    /** Creates a new instance of InstallUnitWizard */
    public InstallUnitWizard () {}
    
    public boolean invokeWizard (OperationType doOperation, PluginManagerUI manager) {
        InstallUnitWizardModel model = new InstallUnitWizardModel (doOperation);
        model.setPluginManager (manager);
        return invokeWizard (model);
    }
    
    public boolean invokeWizard (InstallUnitWizardModel model) {
        return invokeWizard(model, true);
    }

    public boolean invokeWizard (InstallUnitWizardModel model, boolean allowRunInBackground) {
        WizardDescriptor.Iterator<WizardDescriptor> iterator;
        iterator = new InstallUnitWizardIterator (model, true, allowRunInBackground);
        return implInvokeWizard (iterator);
    }
    
    public boolean invokeWizard(InstallUnitWizardModel model, boolean allowRunInBackground, boolean runInBackground) {
        WizardDescriptor.Iterator<WizardDescriptor> iterator;
        iterator = new InstallUnitWizardIterator(model, true, allowRunInBackground, runInBackground);
        return implInvokeWizard(iterator);
    }

    public boolean invokeLazyWizard (Collection<LazyUnit> units, OperationType doOperation, boolean forceReload) {
        return implInvokeWizard (new LazyInstallUnitWizardIterator (units, doOperation, forceReload));
    }
    
    private boolean implInvokeWizard (WizardDescriptor.Iterator<WizardDescriptor> iterator) {
        WizardDescriptor wizardDescriptor = new WizardDescriptor (iterator);
        wizardDescriptor.setModal (true);
        
        wizardDescriptor.setTitleFormat (new MessageFormat(NbBundle.getMessage (InstallUnitWizard.class, "InstallUnitWizard_MessageFormat")));
        wizardDescriptor.setTitle (NbBundle.getMessage (InstallUnitWizard.class, "InstallUnitWizard_Title"));
        
        Dialog dialog = DialogDisplayer.getDefault ().createDialog (wizardDescriptor);
        dialog.setVisible (true);
        dialog.toFront ();
        boolean cancelled = wizardDescriptor.getValue() != WizardDescriptor.FINISH_OPTION;
        log.log (Level.FINE, "InstallUnitWizard returns with value " + wizardDescriptor.getValue ());
        return !cancelled;
    }
    
}
