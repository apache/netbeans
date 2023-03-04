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
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.autoupdate.OperationContainer;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author Jiri Rechtacek
 */
public class UninstallUnitWizard {
    
    private final Logger log = Logger.getLogger (this.getClass ().getName ());
    
    /** Creates a new instance of InstallUnitWizard */
    public UninstallUnitWizard () {}
    
    public boolean invokeWizard () {
        return invokeWizardImpl (true, null, null);
    }
    
    public boolean invokeWizard (boolean doEnable) {
        return invokeWizardImpl (null, doEnable ? Boolean.TRUE : Boolean.FALSE, null);
    }
    
    /**
     * Invokes the wizard with the ability to call back and refresh the model and UI
     * 
     * @param doEnable if true will represent enable operation
     * @param refresher non-{@code null} will be provided to the model to refresh.
     * @return whether the wizard was cancelled
     */
    public boolean invokeWizard (boolean doEnable, Callable<OperationContainer> refresher) {
        return invokeWizardImpl (null, doEnable ? Boolean.TRUE : Boolean.FALSE, refresher);
    }
    
    private boolean invokeWizardImpl (Boolean doUninstall, Boolean doEnable, Callable<OperationContainer> refresher) {
        assert doUninstall != null || doEnable != null : "At least one action is enabled";
        assert ! (doUninstall != null && doEnable != null) : "Only once action is enabled";
        assert doUninstall == null || Containers.forUninstall () != null : "The OperationContainer<OperationSupport> forUninstall must exist!";
        assert doUninstall != null || !doEnable || (doEnable && Containers.forEnable () != null) : "The OperationContainer<OperationSupport> forEnable must exist!";
        assert doUninstall != null || doEnable || (! doEnable && Containers.forDisable () != null) : "The OperationContainer<OperationSupport> forDisable must exist!";
        
        UninstallUnitWizardModel model = new UninstallUnitWizardModel (doUninstall != null
                ? OperationWizardModel.OperationType.UNINSTALL : doEnable ? OperationWizardModel.OperationType.ENABLE : OperationWizardModel.OperationType.DISABLE);
        model.setRefreshCallable(refresher);
        WizardDescriptor.Iterator<WizardDescriptor> iterator = new UninstallUnitWizardIterator (model);
        WizardDescriptor wizardDescriptor = new WizardDescriptor (iterator);
        wizardDescriptor.setModal (true);
        
        // {0} will be replaced by WizardDescriptor.Panel.getComponent().getName()
        // {1} will be replaced by WizardDescriptor.Iterator.name()
        wizardDescriptor.setTitleFormat (new MessageFormat("{1}"));
        wizardDescriptor.setTitle (NbBundle.getMessage (UninstallUnitWizard.class, "UninstallUnitWizard_Title"));
        
        Dialog dialog = DialogDisplayer.getDefault ().createDialog (wizardDescriptor);
        dialog.setVisible (true);
        dialog.toFront ();
        boolean cancelled = wizardDescriptor.getValue() != WizardDescriptor.FINISH_OPTION;
        log.log (Level.FINE, "InstallUnitWizard returns with value " + wizardDescriptor.getValue ());
        return !cancelled;
    }
    
}
