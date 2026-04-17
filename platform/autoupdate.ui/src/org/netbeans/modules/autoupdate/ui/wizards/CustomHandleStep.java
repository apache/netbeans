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

/**
 *
 * @author Jiri Rechtacek
 */
public class CustomHandleStep implements WizardDescriptor.FinishablePanel<WizardDescriptor> {
    private OperationPanel panel;
    private PanelBodyContainer component;
    private OperationWizardModel model = null;
    private WizardDescriptor wd = null;
    private final Logger log = Logger.getLogger (this.getClass ().getName ());
    private final List<ChangeListener> listeners = new ArrayList<ChangeListener> ();
    
    private static final String HEAD_CUSTOM_INSTALL = "CustomHandleStep_Header_Install_Head";
    private static final String CONTENT_CUSTOM_INSTALL = "CustomHandleStep_Header_Install_Content";
    
    private static final String HEAD_CUSTOM_UNINSTALL = "CustomHandleStep_Header_Uninstall_Head";
    private static final String CONTENT_CUSTOM_UNINSTALL = "CustomHandleStep_Header_Uninstall_Content";
    
    private static final String HEAD_CUSTOM_INSTALL_DONE = "CustomHandleStep_Header_InstallDone_Head";
    private static final String CONTENT_CUSTOM_INSTALL_DONE = "CustomHandleStep_Header_InstallDone_Content";
    
    private static final String HEAD_CUSTOM_UNINSTALL_DONE = "CustomHandleStep_Header_UninstallDone_Head";
    private static final String CONTENT_CUSTOM_UNINSTALL_DONE = "CustomHandleStep_Header_UninstallDone_Content";
    
    private static final String HEAD_CUSTOM_INSTALL_FAIL = "CustomHandleStep_Header_InstallFail_Head";
    private static final String CONTENT_CUSTOM_INSTALL_FAIL = "CustomHandleStep_Header_InstallFail_Content";
    
    private static final String HEAD_CUSTOM_UNINSTALL_FAIL = "CustomHandleStep_Header_UninstallFail_Head";
    private static final String CONTENT_CUSTOM_UNINSTALL_FAIL = "CustomHandleStep_Header_UninstallFail_Content";

    private static final String HEAD_CUSTOM_INSTALL_RESTART = "CustomHandleStep_Header_Install_Restart_Head";
    private static final String CONTENT_CUSTOM_INSTALL_RESTART = "CustomHandleStep_Header_Install_Restart_Content";
    private static final String HEAD_CUSTOM_UNINSTALL_RESTART = "CustomHandleStep_Header_Uninstall_Restart_Head";
    private static final String CONTENT_CUSTOM_UNINSTALL_RESTART = "CustomHandleStep_Header_Uninstall_Restart_Content";

    private Restarter restarter = null;
    
    private boolean isInstall = false;
    
    /** Creates a new instance of OperationDescriptionStep */
    public CustomHandleStep (OperationWizardModel model) {
        this.model = model;
        this.isInstall = model instanceof InstallUnitWizardModel;
    }
    
    public boolean isFinishPanel() {
        return ! model.hasStandardComponents ();
    }

    public PanelBodyContainer getComponent() {
        if (component == null) {
            panel = new OperationPanel (false);
            panel.addPropertyChangeListener (new PropertyChangeListener () {
                    public void propertyChange (PropertyChangeEvent evt) {
                        if (OperationPanel.RUN_ACTION.equals (evt.getPropertyName ())) {
                            doHandleOperation ();
                        }
                    }
            });
            if (isInstall) {
                component = new PanelBodyContainer (getBundle (HEAD_CUSTOM_INSTALL), getBundle (CONTENT_CUSTOM_INSTALL), panel);
            } else {
                component = new PanelBodyContainer (getBundle (HEAD_CUSTOM_UNINSTALL), getBundle (CONTENT_CUSTOM_UNINSTALL), panel);
            }
            component.setPreferredSize (OperationWizardModel.PREFFERED_DIMENSION);
        }
        return component;
    }
    
    private void doHandleOperation () {
        // do operation
        restarter = null;
        if (handleOperation ()) {
            if (isInstall) {
                if(restarter!=null) {
                    presentInstallNeedsRestart();
                } else {
                    presentInstallDone ();
                }
            } else {
                if(restarter!=null) {
                    presentUninstallNeedsRestart();
                } else {
                    presentUninstallDone ();
                }
            }
        } else {
            if (isInstall) {
                presentInstallFail (errorMessage);
            } else {
                presentUninstallFail (errorMessage);
            }
        }
        done = true;
        fireChange ();
    }
    
    private boolean passed = false;
    private String errorMessage = null;
    private boolean done = false;
    private boolean wasStored = false;
    
    private boolean handleOperation () {
        final OperationSupport support = model.getCustomHandledContainer ().getSupport ();
        assert support != null;
        passed = false;
        
        Runnable performOperation = new Runnable () {
            public void run () {
                try {                    
                    final ProgressHandle handle = ProgressHandle.createHandle (isInstall ? getBundle ("CustomHandleStep_Install_InstallingPlugins") :
                                                    getBundle ("CustomHandleStep_Uninstall_UninstallingPlugins"));
                    JComponent progressComponent = ProgressHandleFactory.createProgressComponent (handle);
                    JLabel mainLabel = ProgressHandleFactory.createMainLabelComponent (handle);
                    JLabel detailLabel = ProgressHandleFactory.createDetailLabelComponent (handle);
                    
                    handle.setInitialDelay (0);
                    panel.waitAndSetProgressComponents (mainLabel, progressComponent, detailLabel);

                    restarter = support.doOperation (handle);
                    passed = true;
                    panel.waitAndSetProgressComponents (mainLabel, progressComponent, new JLabel (getBundle ("CustomHandleStep_Done")));
                } catch (OperationException ex) {
                    log.log (Level.INFO, ex.getMessage (), ex);
                    passed = false;
                    errorMessage = ex.getLocalizedMessage ();
                }
            }
        };
        performOperation.run ();
        
        return passed;
    }
    
    private void presentInstallDone () {
        component.setHeadAndContent (getBundle (HEAD_CUSTOM_INSTALL_DONE), getBundle (CONTENT_CUSTOM_INSTALL_DONE));
        model.modifyOptionsForContinue (wd, isFinishPanel ());
        panel.setBody (getBundle ("CustomHandleStep_InstallDone_Text"), model.getCustomHandledComponents ());
    }
    
    private void presentInstallFail (String msg) {
        component.setHeadAndContent (getBundle (HEAD_CUSTOM_INSTALL_FAIL), getBundle (CONTENT_CUSTOM_INSTALL_FAIL));
        model.modifyOptionsForDoClose (wd);
        panel.setBody (getBundle ("CustomHandleStep_InstallFail_Text", msg), model.getCustomHandledComponents ());
    }
    
    private void presentUninstallDone () {
        component.setHeadAndContent (getBundle (HEAD_CUSTOM_UNINSTALL_DONE), getBundle (CONTENT_CUSTOM_UNINSTALL_DONE));
        model.modifyOptionsForContinue (wd, isFinishPanel ());
        panel.setBody (getBundle ("CustomHandleStep_UninstallDone_Text"), model.getCustomHandledComponents ());
    }
    
    private void presentUninstallFail (String msg) {
        component.setHeadAndContent (getBundle (HEAD_CUSTOM_UNINSTALL_FAIL), getBundle (CONTENT_CUSTOM_UNINSTALL_FAIL));
        model.modifyOptionsForDoClose (wd);
        panel.setBody (getBundle ("CustomHandleStep_UninstallFail_Text", msg), model.getCustomHandledComponents ());
    }

    private void presentInstallNeedsRestart () {
        component.setHeadAndContent (getBundle (HEAD_CUSTOM_INSTALL_RESTART), getBundle (CONTENT_CUSTOM_INSTALL_RESTART));
        model.modifyOptionsForContinue(wd, isFinishPanel ());
        if(isFinishPanel()) {
            panel.setRestartButtonsVisible (true);
        }
        panel.setBody (getBundle ("CustomHandleStep_InstallDone_Text"), model.getCustomHandledComponents ());
    }
    
    private void presentUninstallNeedsRestart () {
        component.setHeadAndContent (getBundle (HEAD_CUSTOM_UNINSTALL_RESTART), getBundle (CONTENT_CUSTOM_UNINSTALL_RESTART));
        model.modifyOptionsForContinue(wd, isFinishPanel());
        if(isFinishPanel()) {
            panel.setRestartButtonsVisible (true);
        }
        panel.setBody (getBundle ("CustomHandleStep_UninstallDone_Text"), model.getCustomHandledComponents ());
    }

    public HelpCtx getHelp() {
        return null;
    }

    public void readSettings (WizardDescriptor wd) {
        this.wd = wd;
        this.done = false;
        this.wasStored = false;
    }

    public void storeSettings (WizardDescriptor wd) {
        assert !WizardDescriptor.PREVIOUS_OPTION.equals(wd.getValue()) : "Cannot invoke Back in this case.";
        if (wasStored) {
            return;
        }
        this.wasStored = true;
        if (WizardDescriptor.CANCEL_OPTION.equals(wd.getValue()) ||
                WizardDescriptor.CLOSED_OPTION.equals(wd.getValue()) ||
                WizardDescriptor.NEXT_OPTION.equals(wd.getValue())) {
            model.getCustomHandledContainer().removeAll();
        } else if (restarter != null) {
            final OperationSupport support = model.getCustomHandledContainer().getSupport();
            assert support != null : "OperationSupport cannot be null because OperationContainer " +
                    "contains elements: " + model.getCustomHandledContainer().listAll() + " and invalid elements " + model.getCustomHandledContainer().listInvalid();
            if (panel.restartNow()) {
                try {
                    support.doRestart(restarter, null);
                } catch (OperationException x) {
                    log.log(Level.INFO, x.getMessage(), x);
                }

            } else {
                support.doRestartLater(restarter);
                model.getCustomHandledContainer().removeAll();

                final Runnable onMouseClick = new Runnable() {

                    public void run() {
                        try {
                            support.doRestart(restarter, null);
                        } catch (OperationException x) {
                            log.log(Level.INFO, x.getMessage(), x);
                        }
                    }
                };
                InstallStep.notifyRestartNeeded(onMouseClick,
                        getBundle(isInstall ? "CustomHandleStep_Install_RestartNeeded" : "CustomHandleStep_Uninstall_RestartNeeded"));
                return;
            }
        } else {
            if (WizardDescriptor.FINISH_OPTION.equals(wd.getValue())) {
                model.getCustomHandledContainer().removeAll();
            }

        }
    }

    public boolean isValid() {
        return done;
    }

    public synchronized void addChangeListener(ChangeListener l) {
        listeners.add(l);
    }

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

    private String getBundle (String key, String... params) {
        return NbBundle.getMessage (CustomHandleStep.class, key, params);
    }
}
