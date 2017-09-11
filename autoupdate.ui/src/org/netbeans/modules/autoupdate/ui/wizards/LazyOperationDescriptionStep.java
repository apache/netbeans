/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2013 Oracle and/or its affiliates. All rights reserved.
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

import java.awt.Component;
import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.autoupdate.OperationContainer;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.autoupdate.UpdateManager;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.netbeans.modules.autoupdate.ui.Utilities;
import org.netbeans.modules.autoupdate.ui.actions.AutoupdateCheckScheduler;
import org.netbeans.modules.autoupdate.ui.actions.Installer;
import org.netbeans.modules.autoupdate.ui.wizards.LazyInstallUnitWizardIterator.LazyUnit;
import org.netbeans.modules.autoupdate.ui.wizards.OperationWizardModel.OperationType;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;
import org.openide.util.TaskListener;

/**
 *
 * @author Jiri Rechtacek
 */
public class LazyOperationDescriptionStep implements WizardDescriptor.Panel<WizardDescriptor> {
    private static final String HEAD = "OperationDescriptionStep_Header_Head";
    private static final String CONTENT = "OperationDescriptionStep_Header_Content";
    private static final String TABLE_TITLE_INSTALL = "OperationDescriptionStep_TableInstall_Title";
    private static final String TABLE_TITLE_UPDATE = "OperationDescriptionStep_TableUpdate_Title";
    private PanelBodyContainer component;
    private Collection<LazyUnit> installModel = null;
    private boolean hasUpdates = false;
    private OperationType operationType = null;
    private final List<ChangeListener> listeners = new ArrayList<ChangeListener> ();
    private RequestProcessor.Task checkRealUpdatesTask = null;
    private WizardDescriptor wd = null;
    private boolean forceReload;
    private boolean canClose = false;

    
    /** Creates a new instance of OperationDescriptionStep */
    public LazyOperationDescriptionStep (Collection<LazyUnit> model, OperationType doOperation, boolean forceReload) {
        this.installModel = model;
        this.operationType = doOperation;
        this.hasUpdates = installModel != null && ! installModel.isEmpty ();
        this.forceReload = forceReload;
    }
    
    @Override
    public Component getComponent() {
        if (component == null) {
            JPanel body;
            String tableTitle;
            String head;
            String content;
            switch (operationType) {
            case INSTALL :
                tableTitle = getBundle (TABLE_TITLE_INSTALL);
                head = getBundle (HEAD);
                content = getBundle (CONTENT);
                break;
            case UPDATE :
                tableTitle = getBundle (TABLE_TITLE_UPDATE);
                head = getBundle (HEAD);
                content = getBundle (CONTENT);
                break;
            default:
                assert false : "Unexcepted operationType " + operationType;
                return null;
            }
            if (! hasUpdates) {
                tableTitle = getBundle ("LazyOperationDescriptionStep_FindUpdates_Title");
            }
            body = new OperationDescriptionPanel (tableTitle,
                    preparePluginsForShow (installModel, operationType),
                    "",
                    "",
                    false);
            component = new PanelBodyContainer (head, content, body);
            component.setPreferredSize (OperationWizardModel.PREFFERED_DIMENSION);
            long estimatedTime = Utilities.getTimeOfInitialization ();
            if (forceReload) {
                long refreshTime = Utilities.getTimeOfRefreshUpdateCenters ();
                estimatedTime = estimatedTime > 0 || refreshTime > 0 ? estimatedTime + refreshTime : 0;
            }
            component.setWaitingState (true, estimatedTime);
            checkRealUpdates ();
        }
        return component;
    }
    
    @SuppressWarnings("unchecked")
    private void checkRealUpdates () {
        final Collection<String> problems=new ArrayList<String>();
        checkRealUpdatesTask = Installer.RP.post (new Runnable () {
            @Override
            public void run () {
                final Collection<UpdateElement> updateElementsForStore = new HashSet<UpdateElement> ();
                final Collection<UpdateElement> updates = AutoupdateCheckScheduler.checkUpdateElements(operationType, problems,
                        forceReload, updateElementsForStore);
                hasUpdates = updates != null && ! updates.isEmpty ();
                if (hasUpdates) {
                    assert wd != null : "WizardDescriptor must found!";
                    OperationContainer oc = OperationType.UPDATE == operationType ?
                        OperationContainer.createForUpdate() :
                        OperationContainer.createForInstall();
                    boolean allOk = true;
                    InstallUnitWizardModel model = new InstallUnitWizardModel (operationType, oc);
                    for (UpdateElement el : updates) {
                        UpdateUnit uu = el.getUpdateUnit ();
                        if (UpdateManager.TYPE.CUSTOM_HANDLED_COMPONENT == uu.getType ()) {
                            allOk &= model.getCustomHandledContainer ().canBeAdded (uu, el);
                        } else {
                            allOk &= oc.canBeAdded (uu, el);
                        }
                    }
                    hasUpdates = hasUpdates && allOk;
                    if (allOk) {
                        for (UpdateElement el : updates) {
                            UpdateUnit uu = el.getUpdateUnit ();
                            if (UpdateManager.TYPE.CUSTOM_HANDLED_COMPONENT == uu.getType ()) {
                                model.getCustomHandledContainer ().add (el);
                            } else {
                                oc.add (el);
                            }
                        }
                        model.getBaseContainer().listAll();
                        final WizardDescriptor.Iterator<WizardDescriptor> panels = new InstallUnitWizardIterator (model, true);
                        
                        // call InstallUnitWizardIterator.compactPanels outside AWT
                        ((InstallUnitWizardIterator) panels).hasNext();
                        
                        SwingUtilities.invokeLater (new Runnable () {
                            @Override
                            public void run () {
                                wd.setPanelsAndSettings (panels, wd);
                                fireChange ();
                                LazyUnit.storeUpdateElements (operationType, updateElementsForStore);
                            }
                        });
                    }
                }
            }
        });
        class TLAndR implements TaskListener, Runnable { 
            @Override
            public void taskFinished (Task task) {
                task.removeTaskListener (this);
                if (!hasUpdates) {
                    installModel = Collections.EMPTY_SET;
                    new InstallUnitWizardModel(null, null).modifyOptionsForDoClose(wd);
                    canClose = true;
                    LazyUnit.storeLazyUnits(operationType, installModel);
                    EventQueue.invokeLater(this);
                }
            }
            
            @Override
            public void run() {
                JPanel body;
                if(problems==null || problems.isEmpty())
                {
                    body = new OperationDescriptionPanel (
                        getBundle ("LazyOperationDescriptionStep_NoUpdates_Title"), // NOI18N
                        getBundle ("LazyOperationDescriptionStep_NoUpdates"), // NOI18N
                        "", "",
                        false);
                }
                else
                {
                    body = new OperationDescriptionPanel (
                        getBundle ("LazyOperationDescriptionStep_NoUpdatesWithProblems_Title"), // NOI18N
                        getBundle ("LazyOperationDescriptionStep_NoUpdatesWithProblems"), // NOI18N
                        "", "",
                        false);
                }
                component.setBody (body);
                component.setWaitingState (false);
                fireChange ();
            }
        }
        checkRealUpdatesTask.addTaskListener (new TLAndR());
    }
    
    private String preparePluginsForShow (Collection<LazyUnit> units, OperationType type) {
        String s = new String ();
        List<String> names = new ArrayList<String> ();
        if (units != null && ! units.isEmpty ()) {
            for (LazyUnit u : units) {
                String updatename;
                updatename = "<b>"  + u.getDisplayName () + "</b> "; // NOI18N
                if (OperationWizardModel.OperationType.UPDATE == type) {
                    updatename += getBundle ("OperationDescriptionStep_UpdatePluginVersionFormat", u.getOldVersion (), u.getNewVersion ());
                } else {
                    updatename += getBundle ("OperationDescriptionStep_PluginVersionFormat",  // NOI18N
                        u.getNewVersion ());
                }
                updatename += "<br>"; // NOI18N
                String notification = u.getNotification ();
                if (notification != null && notification.length () > 0) {
                    updatename += "<font color=\"red\">" + notification + "</font><br><br>";  // NOI18N
                }
                names.add (updatename);
            }
            Collections.sort (names);
            for (String name : names) {
                s += name;
            }
        }
        return s.trim ();
    }

    @Override
    public HelpCtx getHelp() {
        return null;
    }

    @Override
    public void readSettings (WizardDescriptor wd) {
        this.wd = wd;
        //new InstallUnitWizardModel (null, null).modifyOptionsForStartWizard (wd);
    }

    @Override
    public void storeSettings(WizardDescriptor wd) {
        if (WizardDescriptor.CANCEL_OPTION.equals (wd.getValue ()) || WizardDescriptor.CLOSED_OPTION.equals (wd.getValue ())) {
            if (checkRealUpdatesTask != null && ! checkRealUpdatesTask.isFinished ()) {
                checkRealUpdatesTask.cancel ();
            }
            AutoupdateCheckScheduler.notifyAvailable (installModel, operationType);
        }
    }

    @Override
    public boolean isValid () {
        return canClose;
    }

    @Override
    public synchronized void addChangeListener (ChangeListener l) {
        listeners.add (l);
    }

    @Override
    public synchronized void removeChangeListener (ChangeListener l) {
        listeners.remove (l);
    }

    private void fireChange () {
        ChangeEvent e = new ChangeEvent (this);
        List<ChangeListener> templist;
        synchronized (this) {
            templist = new ArrayList<ChangeListener> (listeners);
        }
        for (ChangeListener l : templist) {
            l.stateChanged (e);
        }
    }

    private String getBundle (String key, Object... params) {
        return NbBundle.getMessage (OperationDescriptionPanel.class, key, params);
    }

}
