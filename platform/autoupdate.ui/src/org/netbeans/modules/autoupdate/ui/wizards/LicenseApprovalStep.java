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

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.autoupdate.OperationException;
import org.netbeans.modules.autoupdate.ui.Utilities;
import org.netbeans.modules.autoupdate.ui.actions.AutoupdateCheckScheduler;
import org.netbeans.modules.autoupdate.ui.actions.Installer;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Jiri Rechtacek
 */
class LicenseApprovalStep implements WizardDescriptor.FinishablePanel<WizardDescriptor> {
    private LicenseApprovalPanel panel;
    private PanelBodyContainer component;
    private InstallUnitWizardModel model = null;
    private boolean isApproved = false;
    private WizardDescriptor wd;
    private final List<ChangeListener> listeners = new ArrayList<ChangeListener> ();
    private static final String HEAD = "LicenseApprovalPanel_Header_Head";
    private static final String CONTENT = "LicenseApprovalPanel_Header_Content";
    private RequestProcessor.Task lazyLoadingTask = null;
    
    /** Creates a new instance of OperationDescriptionStep */
    public LicenseApprovalStep (InstallUnitWizardModel model) {
        this.model = model;
    }
    @Override
    public boolean isFinishPanel() {
        return false;
    }

    @Override
    public Component getComponent() {
        if (component == null) {
            JPanel tmp = new LicenseApprovalPanel (null, isApproved);
            component = new PanelBodyContainer (getBundle (HEAD), getBundle (CONTENT), tmp);
            component.setPreferredSize (OperationWizardModel.PREFFERED_DIMENSION);
            if (wd != null) {
                // have "Activate" text during 'enable' phase.
                model.modifyOptionsForDoOperation (wd, 2);
            }
            component.setWaitingState (true);
            appendLoadingLazy ();
        }
        return component;
    }

    private void appendLoadingLazy () {
        lazyLoadingTask = Installer.RP.post (new Runnable () {
            @Override
            public void run () {
                panel = new LicenseApprovalPanel (model, isApproved);
                panel.addPropertyChangeListener (LicenseApprovalPanel.LICENSE_APPROVED, new PropertyChangeListener () {
                    @Override
                        public void propertyChange (PropertyChangeEvent arg0) {
                            isApproved = panel.isApproved ();
                            fireChange ();
                        }
                });
                SwingUtilities.invokeLater (new Runnable () {
                    @Override
                    public void run () {
                        component.setBody (panel);
                        component.setWaitingState (false);
                        fireChange ();
                    }
                });
            }
        });
    }
    
    @Override
    public HelpCtx getHelp() {
        return null;
    }

    @Override
    public void readSettings (WizardDescriptor wd) {
        this.wd = wd;
        if (panel != null) {
            // have "Activate" text during 'enable' phase.
            model.modifyOptionsForDoOperation (wd, 2);
        }
    }

    @Override
    public void storeSettings (WizardDescriptor wd) {
        if (WizardDescriptor.NEXT_OPTION.equals (wd.getValue ())) {
            model.addApprovedLicenses (panel.getLicenses ());
            Installer.RP.post(new Runnable() {

                @Override
                public void run() {
                    Utilities.addAcceptedLicenseIDs(panel.getLicenseIds());
                    Utilities.storeAcceptedLicenseIDs();
                }
            });
        } else {
            model.modifyOptionsForStartWizard (wd);
        }
        if (WizardDescriptor.CANCEL_OPTION.equals (wd.getValue ()) || WizardDescriptor.CLOSED_OPTION.equals (wd.getValue ())) {
            try {
                if (lazyLoadingTask != null && ! lazyLoadingTask.isFinished ()) {
                    lazyLoadingTask.cancel ();
                }
                AutoupdateCheckScheduler.notifyAvailable(LazyInstallUnitWizardIterator.LazyUnit.loadLazyUnits (model.getOperation()), model.getOperation());
                model.doCleanup (true);
            } catch (OperationException x) {
                Logger.getLogger (InstallUnitWizardModel.class.getName ()).log (Level.INFO, x.getMessage (), x);
            }
        }
    }

    @Override
    public boolean isValid() {
        return isApproved;
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

    private String getBundle (String key) {
        return NbBundle.getMessage (LicenseApprovalStep.class, key);
    }
}
