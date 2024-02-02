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

package org.netbeans.modules.autoupdate.ui.wizards;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.netbeans.api.autoupdate.OperationException;
import org.netbeans.api.autoupdate.OperationSupport;
import org.netbeans.api.autoupdate.OperationSupport.Restarter;
import static org.netbeans.modules.autoupdate.ui.wizards.Bundle.*;

/**
 *
 * @author Jiri Rechtacek
 */
public class UninstallStep implements WizardDescriptor.FinishablePanel<WizardDescriptor> {
    private static final Logger err = Logger.getLogger("org.netbeans.modules.autoupdate.ui.wizards.UninstallStep");
    private OperationPanel panel;
    private PanelBodyContainer component;
    private UninstallUnitWizardModel model = null;
    private WizardDescriptor wd = null;
    private Restarter restarter = null;
    private final List<ChangeListener> listeners = new ArrayList<ChangeListener> ();
    
    private boolean wasStored = false;
    private OperationSupport support;
    
    /** Creates a new instance of OperationDescriptionStep */
    public UninstallStep (UninstallUnitWizardModel model) {
        this.model = model;
    }
    
    @Override
    public boolean isFinishPanel() {
        return true;
    }

    @NbBundle.Messages({
        "UninstallStep_Header_Uninstall_Head=Uninstallation",
        "UninstallStep_Header_Uninstall_Content=Please wait until the installer uninstalls all the selected plugins.",
        "UninstallStep_Header_Activate_Head=Activation",
        "UninstallStep_Header_Deactivate_Head=Deactivation",
        "UninstallStep_Header_Activate_Content=Please wait until the installer finishes activating all the selected plugins.",
        "UninstallStep_Header_Deactivate_Content=Please wait until the installer finishes deactivating all the selected plugins."})
    @Override
    public PanelBodyContainer getComponent() {
        if (component == null) {
            panel = new OperationPanel (false);
            panel.addPropertyChangeListener (new PropertyChangeListener () {
                    @Override
                    public void propertyChange (PropertyChangeEvent evt) {
                        if (OperationPanel.RUN_ACTION.equals (evt.getPropertyName ())) {
                            doAction ();
                        }
                    }
            });
            switch (model.getOperation ()) {
                case UNINSTALL :
                    component = new PanelBodyContainer(
                            UninstallStep_Header_Uninstall_Head(),
                            UninstallStep_Header_Uninstall_Content(), panel);
                    break;
                case ENABLE :
                    component = new PanelBodyContainer(
                            UninstallStep_Header_Activate_Head(),
                            UninstallStep_Header_Activate_Content(), panel);
                    break;
                case DISABLE :
                    component = new PanelBodyContainer(
                            UninstallStep_Header_Deactivate_Head(),
                            UninstallStep_Header_Deactivate_Content(), panel);
                    break;
                default:
                    assert false : "Unknown OperationType " + model.getOperation ();
            }
            component.setPreferredSize (OperationWizardModel.PREFFERED_DIMENSION);
        }
        return component;
    }
    
    private void doAction () {
        // proceed operation
        Restarter r;
        try {
            if ((r = handleAction ()) != null) {
                presentActionNeedsRestart (r);
            } else {
                presentActionDone ();
            }
        } catch (OperationException ex) {
            presentActionFailed (ex);
        }
        fireChange ();
    }
    
     @NbBundle.Messages({
        "UninstallStep_NullSupport_NullElements=Not found any elements for that operation.",
        "# {0} - invalid elements",
        "UninstallStep_NullSupport_InvalidElements=Found invalid element(s) {0} for that operation.",
        "UninstallStep_ProgressName_Uninstall=Uninstalling plugins",
        "UninstallStep_ProgressName_Activate=Activating plugins",
        "UninstallStep_ProgressName_Deactivate=Deactivating plugins",
        "UninstallStep_Done=Done.",
        "# {0} - A error message",
        "UninstallStep_Failed=Failed. {0}"})
    private Restarter handleAction () throws OperationException {
        assert model.getBaseContainer () != null : "getBaseContainers() returns not null container.";
        support = (OperationSupport) model.getBaseContainer ().getSupport ();
        assert support != null : "OperationSupport cannot be null because OperationContainer " +
                "contains elements: " + model.getBaseContainer ().listAll () + " and invalid elements " + model.getBaseContainer ().listInvalid ();
        if (support == null) {
            err.log(Level.WARNING, "OperationSupport cannot be null because OperationContainer contains elements: "
                    + "{0} and invalid elements {1}", new Object[]{model.getBaseContainer().listAll(), model.getBaseContainer().listInvalid()});
            if (!model.getBaseContainer().listInvalid().isEmpty()) {
                // cannot continue if there are invalid elements
                throw new OperationException(OperationException.ERROR_TYPE.UNINSTALL,
                        UninstallStep_NullSupport_InvalidElements(model.getBaseContainer().listInvalid()));
            } else if (model.getBaseContainer().listAll().isEmpty()) {
                // it's weird, there must be any elemets for uninstall
                throw new OperationException(OperationException.ERROR_TYPE.UNINSTALL,
                        UninstallStep_NullSupport_NullElements());
            }
            throw new OperationException(OperationException.ERROR_TYPE.UNINSTALL,
                    "OperationSupport cannot be null because OperationContainer "
                    + "contains elements: " + model.getBaseContainer().listAll() + " and invalid elements " + model.getBaseContainer().listInvalid());
        }
        ProgressHandle handle = null;
        switch (model.getOperation ()) {
            case UNINSTALL :
                handle = ProgressHandleFactory.createHandle(UninstallStep_ProgressName_Uninstall());
                break;
            case ENABLE :
                handle = ProgressHandleFactory.createHandle(UninstallStep_ProgressName_Activate());
                break;
            case DISABLE :
                handle = ProgressHandleFactory.createHandle(UninstallStep_ProgressName_Deactivate());
                break;
            default:
                assert false : "Unknown OperationType " + model.getOperation ();
        }
        
        JComponent progressComponent = ProgressHandleFactory.createProgressComponent (handle);
        JLabel mainLabel = ProgressHandleFactory.createMainLabelComponent (handle);
        JLabel detailLabel = ProgressHandleFactory.createDetailLabelComponent (handle);
        model.modifyOptionsForDisabledCancel (wd);
        
        panel.waitAndSetProgressComponents (mainLabel, progressComponent, detailLabel);
        
        Restarter r = null;
        try {
            r = support.doOperation (handle);
            panel.waitAndSetProgressComponents(mainLabel, progressComponent, new JLabel(UninstallStep_Done()));
        } catch (OperationException ex) {
            err.log (Level.INFO, ex.getMessage (), ex);
            panel.waitAndSetProgressComponents(mainLabel, progressComponent,
                    new JLabel(UninstallStep_Failed(ex.getLocalizedMessage())));
            throw ex;
        }
        return r;
    }
    
    @NbBundle.Messages({"UninstallStep_Header_UninstallDone_Head=Uninstallation completed successfully",
        "UninstallStep_Header_UninstallDone_Content=Click Finish to quit the plugin installer.",
        "UninstallStep_Header_ActivateDone_Head=Activation completed successfully",
        "UninstallStep_Header_ActivateDone_Content=Click Finish to quit the plugin installer.",
        "UninstallStep_Header_DeactivateDone_Head=Deactivation completed successfully",
        "UninstallStep_Header_DeactivateDone_Content=Click Finish to quit the plugin installer.",
        "UninstallStep_UninstallDone_Text=The Plugin Installer has successfully uninstalled the following plugins:",
        "UninstallStep_ActivateDone_Text=The Plugin Installer has successfully activated the following plugins:",
        "UninstallStep_DeactivateDone_Text=The Plugin Installer has successfully deactivated the following plugins:"})
    private void presentActionDone () {
        switch (model.getOperation ()) {
            case UNINSTALL :
                component.setHeadAndContent (UninstallStep_Header_UninstallDone_Head(),
                        UninstallStep_Header_UninstallDone_Content());
                break;
            case ENABLE :
                component.setHeadAndContent (UninstallStep_Header_ActivateDone_Head(),
                        UninstallStep_Header_ActivateDone_Content());
                break;
            case DISABLE :
                component.setHeadAndContent (UninstallStep_Header_DeactivateDone_Head(),
                        UninstallStep_Header_DeactivateDone_Content());
                break;
            default:
                assert false : "Unknown OperationType " + model.getOperation ();
        }
        model.modifyOptionsForDoClose (wd);
        switch (model.getOperation ()) {
            case UNINSTALL :
                panel.setBody(UninstallStep_UninstallDone_Text(), model.getAllVisibleUpdateElements());
                break;
            case ENABLE :
                panel.setBody(UninstallStep_ActivateDone_Text(), model.getAllVisibleUpdateElements());
                break;
            case DISABLE :
                panel.setBody(UninstallStep_DeactivateDone_Text(), model.getAllVisibleUpdateElements());
                break;
            default:
                assert false : "Unknown OperationType " + model.getOperation ();
        }
    }
    
    @NbBundle.Messages({"UninstallStep_Header_UninstallFailed_Head=Uninstallation failed",
        "UninstallStep_Header_UninstallFailed_Content=Click Cancel to quit the plugin installer.",
        "UninstallStep_Header_ActivateFailed_Head=Activation failed",
        "UninstallStep_Header_ActivateFailed_Content=Click Cancel to quit the plugin installer.",
        "UninstallStep_Header_DeactivateFailed_Head=Deactivation failed",
        "UninstallStep_Header_DeactivateFailed_Content=Click Cancel to quit the plugin installer.",
        "# {0} - An error message",
        "UninstallStep_UninstallFailed_Text=Uninstallation failed: {0}",
        "# {0} - An error message",
        "UninstallStep_ActivateFailed_Text=Activation failed: {0}",
        "# {0} - An error message",
        "UninstallStep_DeactivateFailed_Text=Deactivation failed: {0}"})
    private void presentActionFailed (OperationException ex) {
        switch (model.getOperation ()) {
            case UNINSTALL :
                component.setHeadAndContent(UninstallStep_Header_UninstallFailed_Head(), UninstallStep_Header_UninstallFailed_Content());
                break;
            case ENABLE :
                component.setHeadAndContent(UninstallStep_Header_ActivateFailed_Head(),
                        UninstallStep_Header_ActivateFailed_Content());
                break;
            case DISABLE :
                component.setHeadAndContent(UninstallStep_Header_DeactivateFailed_Head(),
                        UninstallStep_Header_DeactivateFailed_Content());
                break;
            default:
                assert false : "Unknown OperationType " + model.getOperation ();
        }
        model.modifyOptionsForFailed (wd);
        switch (model.getOperation ()) {
            case UNINSTALL :
                panel.setBody("", UninstallStep_UninstallFailed_Text(ex.getLocalizedMessage())); // NOI18N
                break;
            case ENABLE :
                panel.setBody("", UninstallStep_ActivateFailed_Text(ex.getLocalizedMessage()));
                break;
            case DISABLE :
                panel.setBody ("", UninstallStep_DeactivateFailed_Text(ex.getLocalizedMessage ()));
                break;
            default:
                assert false : "Unknown OperationType " + model.getOperation ();
        }
    }
    
    @NbBundle.Messages({
        "UninstallStep_Header_Restart_Head=Restart application to complete deactivation",
        "UninstallStep_Header_Restart_Content=Restart application to finish plugin deactivation.",
        "UninstallStep_Activate_Header_Restart_Head=Restart application to complete activation",
        "UninstallStep_Activate_Header_Restart_Content=Restart application to finish plugin activation.",
        "UninstallStep_ActivateLater_Text=The Plugin Installer has successfully activated the following plugins:",
        "UninstallStep_UninstallLater_Text=The Plugin Installer has successfully uninstalled the following plugins:",
        "UninstallStep_DeactivateLater_Text=The Plugin Installer has successfully deactivated the following plugins:"})
    private void presentActionNeedsRestart (Restarter r) {
        if (model.getOperation() == OperationWizardModel.OperationType.ENABLE) {
            component.setHeadAndContent (UninstallStep_Activate_Header_Restart_Head(), UninstallStep_Activate_Header_Restart_Content());
        } else {
            component.setHeadAndContent (UninstallStep_Header_Restart_Head(), UninstallStep_Header_Restart_Content());
        }
        model.modifyOptionsForDoClose (wd, true);
        restarter = r;
        panel.setRestartButtonsVisible (true);
        switch (model.getOperation ()) {
            case UNINSTALL :
                panel.setBody(UninstallStep_UninstallLater_Text(), model.getAllVisibleUpdateElements());
                break;
            case ENABLE :
                panel.setBody (UninstallStep_ActivateLater_Text(), model.getAllVisibleUpdateElements ());
                break;
            case DISABLE :
                panel.setBody(UninstallStep_UninstallLater_Text(), model.getAllVisibleUpdateElements());
                break;
            default:
                assert false : "Unknown OperationType " + model.getOperation ();
        }
    }
    
    @Override
    public HelpCtx getHelp() {
        return null;
    }

    @Override
    public void readSettings (WizardDescriptor wd) {
        this.wd = wd;
        this.wasStored = false;
    }

    @Override
    @NbBundle.Messages("UninstallSupport_RestartNeeded=Restart the application to complete the deactivation.")
    public void storeSettings (WizardDescriptor wd) {
        assert ! WizardDescriptor.PREVIOUS_OPTION.equals (wd.getValue ()) : "Cannot invoke Back in this case.";
        if (wasStored) {
            return ;
        }
        this.wasStored = true;
        if (WizardDescriptor.CANCEL_OPTION.equals (wd.getValue ()) || WizardDescriptor.CLOSED_OPTION.equals (wd.getValue ())) {
            try {
                model.doCleanup (true);
            } catch (OperationException x) {
                Logger.getLogger (UninstallStep.class.getName ()).log (Level.INFO, x.getMessage (), x);
            }
        } else if (restarter != null) {
            assert support != null : "OperationSupport cannot be null because OperationContainer " +
                    "contains elements: " + model.getBaseContainer ().listAll () + " and invalid elements " + model.getBaseContainer ().listInvalid ();
            if (support == null) {
                err.log(Level.INFO, "OperationSupport cannot be null because OperationContainer contains elements: "
                        + "{0} and invalid elements {1}", new Object[]{model.getBaseContainer().listAll(), model.getBaseContainer().listInvalid()});
                try {
                    model.doCleanup(true);
                } catch (OperationException x) {
                    Logger.getLogger(UninstallStep.class.getName()).log(Level.INFO, x.getMessage(), x);
                }
            }
            if (panel.restartNow ()) {
                try {
                    support.doRestart (restarter, null);
                } catch (OperationException x) {
                    err.log (Level.INFO, x.getMessage (), x);
                }
                
            } else {
                support.doRestartLater (restarter);
                try {
                    model.doCleanup (false);
                } catch (OperationException x) {
                    err.log (Level.INFO, x.getMessage (), x);
                }
                final Runnable onMouseClick = new Runnable () {
                    @Override
                    public void run () {
                        try {
                            support.doRestart (restarter, null);
                        } catch (OperationException x) {
                            err.log (Level.INFO, x.getMessage (), x);
                        }
                    }
                };
                InstallStep.notifyRestartNeeded (onMouseClick, UninstallSupport_RestartNeeded());
            }
        } else {
            try {
                model.doCleanup (! WizardDescriptor.FINISH_OPTION.equals (wd.getValue ()));
            } catch (OperationException x) {
                err.log (Level.INFO, x.getMessage (), x);
            }
        }
    }
    
    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public synchronized void addChangeListener(ChangeListener l) {
        listeners.add(l);
    }

    @Override
    public synchronized void removeChangeListener(ChangeListener l) {
        listeners.remove(l);
    }

    private void fireChange() {
        ChangeEvent e = new ChangeEvent(this);
        List<ChangeListener> templist;
        synchronized (this) {
            templist = new ArrayList<ChangeListener> (listeners);
        }
	for (ChangeListener l: templist) {
            l.stateChanged(e);
        }
    }

}
