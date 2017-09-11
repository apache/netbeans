/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
 * Microsystems, Inc. All Rights Reserved.
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
                    final ProgressHandle handle = ProgressHandleFactory.createHandle (isInstall ? getBundle ("CustomHandleStep_Install_InstallingPlugins") :
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
