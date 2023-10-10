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

import java.awt.Component;
import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.autoupdate.OperationContainer;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.autoupdate.UpdateManager;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.netbeans.api.autoupdate.UpdateUnitProvider;
import org.netbeans.api.autoupdate.UpdateUnitProviderFactory;
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
        final Collection<String> problems = new ArrayList<>();
        final List<String> notifications = new ArrayList<>();
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
                } else {
                    for (UpdateUnitProvider p :
                            UpdateUnitProviderFactory.getDefault().getUpdateUnitProviders(true)) {
                        String desc = p.getDescription();
                        if (desc != null && desc.contains("<a name=\"autoupdate_catalog_parser\"")) {
                            notifications.add(desc);
                        }
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
                if (problems.isEmpty()) {
                    if (notifications.isEmpty()) {
                        body = new OperationDescriptionPanel(
                                getBundle("LazyOperationDescriptionStep_NoUpdates_Title"), // NOI18N
                                getBundle("LazyOperationDescriptionStep_NoUpdates"), // NOI18N
                                "", "",
                                false);
                    } else {
                        String content = notifications.stream().collect(Collectors.joining("<br><br>"));
                        body = new OperationDescriptionPanel(
                                getBundle("LazyOperationDescriptionStep_Notifications_Title"), // NOI18N
                                content, // NOI18N
                                "", "",
                                false);
                    }
                } else {
                    body = new OperationDescriptionPanel(
                            getBundle("LazyOperationDescriptionStep_NoUpdatesWithProblems_Title"), // NOI18N
                            getBundle("LazyOperationDescriptionStep_NoUpdatesWithProblems"), // NOI18N
                            "", "",
                            false);
                }
                component.setBody(body);
                component.setWaitingState(false);
                fireChange();
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
