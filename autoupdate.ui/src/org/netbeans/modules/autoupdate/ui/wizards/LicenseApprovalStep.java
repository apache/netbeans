/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2012 Oracle and/or its affiliates. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
public class LicenseApprovalStep implements WizardDescriptor.FinishablePanel<WizardDescriptor> {
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
                model.modifyOptionsForDoOperation (wd);
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
            model.modifyOptionsForDoOperation (wd);
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
