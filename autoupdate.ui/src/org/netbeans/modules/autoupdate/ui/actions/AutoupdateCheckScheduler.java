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

package org.netbeans.modules.autoupdate.ui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.netbeans.api.autoupdate.InstallSupport;
import org.netbeans.api.autoupdate.OperationContainer;
import org.netbeans.api.autoupdate.OperationContainer.OperationInfo;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.autoupdate.UpdateManager;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.netbeans.api.autoupdate.UpdateUnitProvider;
import org.netbeans.api.autoupdate.UpdateUnitProviderFactory;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.autoupdate.ui.PluginManagerUI;
import org.netbeans.modules.autoupdate.ui.Unit;
import org.netbeans.modules.autoupdate.ui.UnitCategory;
import org.netbeans.modules.autoupdate.ui.Utilities;
import org.netbeans.modules.autoupdate.ui.wizards.InstallUnitWizard;
import org.netbeans.modules.autoupdate.ui.wizards.LazyInstallUnitWizardIterator.LazyUnit;
import org.netbeans.modules.autoupdate.ui.wizards.OperationWizardModel.OperationType;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.Notification;
import org.openide.awt.NotificationDisplayer;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;
import org.openide.util.TaskListener;
import org.openide.windows.WindowManager;

/**
 *
 * @author Jiri Rechtacek
 */
public class AutoupdateCheckScheduler {
    private static RequestProcessor.Task regularlyCheck = null;    
    private static final RequestProcessor REGULARLY_CHECK_TIMER = 
        new RequestProcessor("auto-checker-reqularly-timer", 1, true); // NOI18N
    private static final Logger err = Logger.getLogger (AutoupdateCheckScheduler.class.getName ());
    
    private static Notification updatesNotification = null;

    private AutoupdateCheckScheduler () {
    }
    
    public static void signOn () {
        // temprorary to investigate #203326
        NotificationDisplayer.getDefault();
        // end of investigation #203326
        
        AutoupdateSettings.generateIdentity ();
        
        if (timeToCheck ()) {
            // schedule refresh providers
            // install update checker when UI is ready (main window shown)
            WindowManager.getDefault().invokeWhenUIReady(new Runnable () {
                @Override
                public void run () {
                    Installer.RP.post (doCheck, 5000);
                }
            });
        } else {
            // install update checker when UI is ready (main window shown)
            WindowManager.getDefault().invokeWhenUIReady(new Runnable () {
                @Override
                public void run () {
                    Installer.RP.post (doCheckLazyUpdates, 11000);
                }
            });
        }
    }
    
    private static void scheduleRefreshProviders () {
        refreshUpdateCenters (null);
        final int delay = 500;
        final long startTime = System.currentTimeMillis ();
        RequestProcessor.Task t = Installer.RP.post (doCheckAvailableUpdates, delay);
        t.addTaskListener (new TaskListener () {
            @Override
            public void taskFinished (Task task) {
                task.removeTaskListener (this);
                long time = (System.currentTimeMillis () - startTime - delay) / 1000;
                if (time > 0) {
                    Utilities.putTimeOfInitialization (time);
                }
            }
        });
    }
    
    private static Runnable getRefresher (final UpdateUnitProvider p, final Collection<String> problems, final ProgressHandle progress) {
        return new Runnable () {
            @Override
            public void run () {
                try {
                    err.log (Level.FINE, "Start refresh " + p.getName () + "[" + p.getDisplayName () + "]");
                    p.refresh (progress, true);
                    PluginManagerUI pluginManagerUI = PluginManagerAction.getPluginManagerUI ();
                    if (pluginManagerUI != null) {
                        if (pluginManagerUI.initTask.isFinished ()) {
                            pluginManagerUI.updateUnitsChanged();
                        }
                    }
                    Utilities.showProviderNotification(p);
                } catch (IOException ioe) {
                    if (ioe instanceof UnknownHostException || ioe.getCause() instanceof UnknownHostException) {
                        // Most likely just offline. Do not print a stack trace. DownloadListener.notifyException already issuing warning.
                    } else {
                        err.log(Level.INFO, null, ioe);
                    }
                    if (problems != null) {
                        problems.add (ioe.getLocalizedMessage ());
                    }
                } finally {
                    err.log (Level.FINEST, "Refresh of " + p.getName () + "[" + p.getDisplayName () + "]" + " is finish.");
                }
            }
        };
    }
    
    private static Runnable doCheckAvailableUpdates = new Runnable () {
        @Override
        public void run () {
            if (SwingUtilities.isEventDispatchThread ()) {
                Installer.RP.post (doCheckAvailableUpdates);
                return ;
            }
            boolean hasUpdates = false;
            if (Utilities.shouldCheckAvailableUpdates ()) {
                Collection<UpdateElement> updates = new HashSet<UpdateElement> ();
                checkUpdateElements(OperationType.UPDATE, null, false, updates);
                hasUpdates = updates != null && ! updates.isEmpty ();
                LazyUnit.storeUpdateElements (OperationType.UPDATE, updates);
                Utilities.storeAcceptedLicenseIDs();
            }
            if (! hasUpdates && Utilities.shouldCheckAvailableNewPlugins ()) {
                LazyUnit.storeUpdateElements (OperationType.INSTALL, checkUpdateElements(OperationType.INSTALL, null, false, null));
            }
            Installer.RP.post (doCheckLazyUpdates, 500);
        }
    };
    
    public static void runCheckAvailableUpdates (int delay) {
        Installer.RP.post (doCheckAvailableUpdates, delay);
    }

    public static Collection<UpdateElement> checkUpdateElements(OperationType type, Collection<String> problems,
            boolean forceReload, Collection<UpdateElement> visibleUpdateElement) {
        // check
        err.log (Level.FINEST, "Check UpdateElements for " + type);
        if (forceReload) {
            ProgressHandle dummyHandler = ProgressHandleFactory.createHandle ("dummy-check-for-updates"); // NOI18N
            ProgressHandleFactory.createProgressComponent(dummyHandler);
            dummyHandler.start();
            Collection <String> updateProblems=refreshUpdateCenters (dummyHandler);
            if (problems != null && updateProblems != null) {
                problems.addAll(updateProblems);
            }
        }
        List<UpdateUnit> units = UpdateManager.getDefault ().getUpdateUnits (UpdateManager.TYPE.MODULE);
        boolean handleUpdates = OperationType.UPDATE == type;
        Collection<UnitCategory> cats =  handleUpdates ?
            Utilities.makeUpdateCategories (units, false) :
            Utilities.makeAvailableCategories (units, false);
        if (cats == null || cats.isEmpty ()) {
            err.log (Level.FINE, "findUpdateElements(" + type + ") doesn't find any elements.");
            return null;
        }
        // 1. try to install all available updates
        Collection<UpdateElement> updates = new HashSet<UpdateElement> ();
        boolean somePendingElements = false;
        boolean someBrokenDependencies = false;
        OperationContainer<InstallSupport> container = handleUpdates ?
            OperationContainer.createForUpdate () :
            OperationContainer.createForInstall ();


        List<UpdateElement> elements = new ArrayList<UpdateElement>();

        for (UnitCategory cat : cats) {
            for (Unit u : cat.getUnits ()) {        
                if(u instanceof Unit.Available) {
                    elements.add(((Unit.Available) u).getRelevantElement ());
                } else if (u instanceof Unit.CompoundUpdate) {
                    for(UpdateUnit uu :((Unit.CompoundUpdate) u).getUpdateUnits()) {
                        elements.add(uu.getAvailableUpdates().get(0));
                    }
                    if (((Unit.CompoundUpdate) u).getRealUpdate() != null) {
                        elements.add(((Unit.CompoundUpdate) u).getRealUpdate());
                    }
                } else if (u instanceof Unit.Update) {
                    elements.add(((Unit.Update) u).getRelevantElement ());
                }

            }
        }
        for(UpdateElement element : elements) {
            if (! somePendingElements) {
                if(container.canBeAdded (element.getUpdateUnit (), element)) {
                    OperationInfo<InstallSupport> operationInfo = container.add (element);
                    if (operationInfo == null) {
                        updates.add (element);
                        continue;
                    }
                    Collection<UpdateElement> reqs = new HashSet<UpdateElement> (operationInfo.getRequiredElements ());
                    Collection<String> brokenDeps = operationInfo.getBrokenDependencies ();
                    if (! brokenDeps.isEmpty ()) {
                        err.log (Level.WARNING, "Plugin " + operationInfo + // NOI18N
                                " cannot be installed because some dependencies cannot be satisfied: " + brokenDeps); // NOI18N
                        someBrokenDependencies = true;
                        break;
                    }
                    for (UpdateElement tmpEl : reqs) {
                       if (tmpEl.getUpdateUnit ().isPending ()) {
                           err.log (Level.WARNING, "Plugin " + operationInfo.getUpdateElement () + // NOI18N
                                   " depends on " + tmpEl + " in pending state.");                           
                           somePendingElements = true;
                           updates = Collections.emptySet ();
                           break;
                       } 
                    }
                    if (! somePendingElements) {
                        container.add (reqs);
                        updates.add (element);
                    }
                }
            }
        }
        if (! somePendingElements && ! container.listInvalid ().isEmpty ()) {
            err.log (Level.WARNING, "Plugins " + updates + // NOI18N
                    " cannot be installed, Install Container contains invalid elements " + container.listInvalid ()); // NOI18N
        }
        if (! somePendingElements && someBrokenDependencies) {
            // 2. if some problem then try one by one
            updates = new HashSet<UpdateElement> ();
           for(UpdateElement element : elements) {
                    OperationContainer<InstallSupport> oc = handleUpdates ?
                        OperationContainer.createForUpdate () :
                        OperationContainer.createForInstall ();
                    
                    UpdateUnit unit = element.getUpdateUnit ();
                    if (oc.canBeAdded (unit, element)) {
                        OperationInfo<InstallSupport> operationInfo = oc.add (element);
                        if (operationInfo == null) {
                            updates.add (element);
                            continue;
                        }
                        boolean skip = false;
                        Collection<UpdateElement> reqs = new HashSet<UpdateElement> (operationInfo.getRequiredElements ());
                        for (UpdateElement tmpEl : reqs) {
                           if (tmpEl.getUpdateUnit ().isPending ()) {
                               err.log (Level.WARNING, "Plugin " + element + // NOI18N
                                       " depends on " + tmpEl + " in pending state.");                           
                               skip = true;
                           } 
                        }
                        if (skip) {
                            continue;
                        }
                        oc.add (reqs);
                        Collection<String> brokenDeps = new HashSet<String> ();
                        for (OperationInfo<InstallSupport> info : oc.listAll ()) {
                            brokenDeps.addAll (info.getBrokenDependencies ());
                        }
                        if (brokenDeps.isEmpty () && oc.listInvalid ().isEmpty ()) {
                            updates.add (element);
                        } else {
                            oc.removeAll ();
                            if (! brokenDeps.isEmpty ()) {
                                err.log (Level.WARNING, "Plugin " + element + // NOI18N
                                        " cannot be installed because some dependencies cannot be satisfied: " + brokenDeps); // NOI18N
                            } else {
                                err.log (Level.WARNING, "Plugin " + element + // NOI18N
                                        " cannot be installed, Install Container contains invalid elements " + oc.listInvalid ()); // NOI18N
                            }
                        }
                    }                
            }
        }

        // if any then notify updates
        if (visibleUpdateElement == null) {
            err.log (Level.FINE, "findUpdateElements(" + type + ") returns " + updates.size () + " elements.");
            return updates;
        } else {
            for (UnitCategory cat : cats) {
                for (Unit u : cat.getUnits()) {
                    assert u instanceof Unit.Update : u + " has to be instanceof Unit.Update";
                    if (u instanceof Unit.Update) {
                        visibleUpdateElement.add(((Unit.Update)u).getRelevantElement());
                    }
                }
            }
            err.log (Level.FINE, "findUpdateElements(" + type + ") returns " + visibleUpdateElement.size () +
                    " visible elements (" + updates.size() + " in all)");
            return updates;
        }
    }

    /**
     * @param progress
     * @return collection of strings with description of problems in update process
     */
    private static Collection<String> refreshUpdateCenters (ProgressHandle progress) {
        final long startTime = System.currentTimeMillis ();
        Collection<String> problems = new HashSet<String> ();
        assert ! SwingUtilities.isEventDispatchThread () : "Cannot run refreshProviders in EQ!";
        Collection<RequestProcessor.Task> refreshTasks = new HashSet<RequestProcessor.Task> ();
        List <UpdateUnitProvider> providers = UpdateUnitProviderFactory.getDefault ().getUpdateUnitProviders (true);
        RequestProcessor rp = new RequestProcessor("autoupdate-refresh-providers", providers.size(), false);
        for (UpdateUnitProvider p : providers) {
            RequestProcessor.Task t = rp.post (getRefresher (p, problems, progress));
            refreshTasks.add (t);
        }
        err.log (Level.FINEST, "Waiting for all refreshTasks...");
        for (RequestProcessor.Task t : refreshTasks) {
            t.waitFinished ();
        }
        err.log (Level.FINEST, "Waiting for all refreshTasks is done.");
        long time = (System.currentTimeMillis () - startTime) / 1000;
        if (time > 0) {
            Utilities.putTimeOfRefreshUpdateCenters (time);
        }
        return problems;
    }
    
    private static boolean timeToCheck () {
        if (getReqularlyTimerTask () != null) {
            // if time is off then is time to check
            if (getReqularlyTimerTask ().getDelay () <= 0 && getWaitPeriod () > 0) {
                return true;
            }
        }
        
        // If this is the first time always check
        if (AutoupdateSettings.getLastCheck () == null) {
            return true;
        }
        
        switch (AutoupdateSettings.getPeriod ()) {
            case AutoupdateSettings.EVERY_STARTUP:
                return true;
            case AutoupdateSettings.NEVER:
                return false;
            case AutoupdateSettings.CUSTOM_CHECK_INTERVAL:
                return AutoupdateSettings.getLastCheck ().getTime () + AutoupdateSettings.getCheckInterval () < new Date ().getTime ();
            default:
                Date lastCheck = AutoupdateSettings.getLastCheck();
                GregorianCalendar calendar = new GregorianCalendar();
                calendar.setTime (lastCheck);

                calendar.set (Calendar.HOUR, 0);
                calendar.set (Calendar.AM_PM, 0);
                calendar.set (Calendar.MINUTE, 0);
                calendar.set (Calendar.SECOND, 0);
                calendar.set (Calendar.MILLISECOND, 0);

                switch (AutoupdateSettings.getPeriod ()) {
                    case AutoupdateSettings.EVERY_DAY:
                        calendar.add (GregorianCalendar.DATE, 1);
                        break;
                    case AutoupdateSettings.EVERY_WEEK:
                        calendar.add (GregorianCalendar.WEEK_OF_YEAR, 1);
                        break;
                    case AutoupdateSettings.EVERY_2WEEKS:
                        calendar.add (GregorianCalendar.WEEK_OF_YEAR, 2);
                        break;
                    case AutoupdateSettings.EVERY_MONTH:
                        calendar.add (GregorianCalendar.MONTH, 1);
                        break;
                }
                return calendar.getTime ().before (new Date ());
        }
    }
    
    private static RequestProcessor.Task getReqularlyTimerTask () {
        if (regularlyCheck == null) {
            // only for ordinary periods
            if (getWaitPeriod () > 0) {
                int waitPeriod = getWaitPeriod ();
                int restTime = waitPeriod;
                // calculate rest time to check
                if (AutoupdateSettings.getLastCheck () != null) {
                    restTime = waitPeriod - (int)(System.currentTimeMillis () - AutoupdateSettings.getLastCheck ().getTime ());
                }
                
                // if restTime < 0 then schedule next round by given period
                if (restTime <= 0) {
                    restTime = waitPeriod;
                }
                
                regularlyCheck = REGULARLY_CHECK_TIMER.post (doCheck, restTime, Thread.MIN_PRIORITY);
                
            }
        }
        return regularlyCheck;
    }
    
    private static Runnable doCheck = new Runnable () {
        @Override
        public void run() {
            if (SwingUtilities.isEventDispatchThread ()) {
                Installer.RP.post (doCheck);
                return ;
            }
            if (timeToCheck ()) {
                scheduleRefreshProviders ();
                if (getWaitPeriod () > 0 && regularlyCheck != null && regularlyCheck.getDelay () <= 0) {
                    regularlyCheck = REGULARLY_CHECK_TIMER.post (doCheck, getWaitPeriod (), Thread.MIN_PRIORITY);
                }
            }
        }
    };
    
    private static Runnable doCheckLazyUpdates = new Runnable () {
        @Override
        public void run () {
            if (SwingUtilities.isEventDispatchThread ()) {
                Installer.RP.post (doCheckLazyUpdates);
                return ;
            }
            Collection<LazyUnit> updates = LazyUnit.loadLazyUnits (OperationType.UPDATE);
            boolean hasUpdates = updates != null && ! updates.isEmpty ();
            if (Utilities.shouldCheckAvailableUpdates ()) {
                notifyAvailable (updates, OperationType.UPDATE);
            }
            if (! hasUpdates && Utilities.shouldCheckAvailableNewPlugins ()) {
                Collection<LazyUnit> newUnits = LazyUnit.loadLazyUnits (OperationType.INSTALL);
                notifyAvailable (newUnits, OperationType.INSTALL);
            }
            for(UpdateUnitProvider p : UpdateUnitProviderFactory.getDefault().getUpdateUnitProviders(true)) {
                Utilities.showProviderNotification(p);
            }
        }
    };
    
    private static int getWaitPeriod () {
        switch (AutoupdateSettings.getPeriod ()) {
            case AutoupdateSettings.NEVER:
                return 0;
            case AutoupdateSettings.EVERY_STARTUP:
                return 0;
            case AutoupdateSettings.EVERY_DAY:
                return 1000 * 3600 * 24;
            case AutoupdateSettings.EVERY_WEEK:
                return 1000 * 3600 * 24 * 7;
            case AutoupdateSettings.EVERY_2WEEKS:
                return 1000 * 3600 * 24 * 14;
            case AutoupdateSettings.EVERY_MONTH:
                return Integer.MAX_VALUE; // 1000 * 3600 * 24 * 28 is close but too big 
            case AutoupdateSettings.CUSTOM_CHECK_INTERVAL:
                return AutoupdateSettings.getCheckInterval ();
            default:
                return 0;
        }
    }
    
    public static void notifyAvailable (final Collection<LazyUnit> units, final OperationType type) {

        if (units == null || units.isEmpty ()) {
            if (updatesNotification != null) {
                updatesNotification.clear();
                updatesNotification = null;
            }
            return ;
        }
        
        // Some modules found
        ActionListener onMouseClickAction = new ActionListener () {
            @SuppressWarnings("unchecked")
            @Override
            public void actionPerformed ( ActionEvent ae ) {
                boolean wizardFinished = false;
                RequestProcessor.Task t = PluginManagerUI.getRunningTask ();
                if (t != null && ! t.isFinished ()) {
                    DialogDisplayer.getDefault ().notifyLater (
                            new NotifyDescriptor.Message (
                                NbBundle.getMessage (AutoupdateCheckScheduler.class,
                                    "AutoupdateCheckScheduler_InstallInProgress"), // NOI18N
                                NotifyDescriptor.WARNING_MESSAGE));
                    return ;
                }
                try {
                    wizardFinished = new InstallUnitWizard ().invokeLazyWizard (units, type, false);
                } finally {
                    if (wizardFinished) {
                        PluginManagerUI pluginManagerUI = PluginManagerAction.getPluginManagerUI ();
                        if (pluginManagerUI != null) {
                            pluginManagerUI.updateUnitsChanged();
                        }
                        Installer.RP.post (doCheckAvailableUpdates);
                    }
                }
            }
        };
        int updateCount = units.size();
        String title = updateCount == 1
            ? NbBundle.getMessage(AutoupdateCheckScheduler.class, "AutoupdateCheckScheduler_UpdateFound_ToolTip", updateCount) // NOI18N
            : NbBundle.getMessage(AutoupdateCheckScheduler.class, "AutoupdateCheckScheduler_UpdatesFound_ToolTip", updateCount); // NOI18N
        
        synchronized (AutoupdateCheckScheduler.class) {
            if (updatesNotification != null) {
                updatesNotification.clear();
                updatesNotification = null;
            }
            updatesNotification = NotificationDisplayer.getDefault().notify(title,
                    ImageUtilities.loadImageIcon("org/netbeans/modules/autoupdate/ui/resources/newUpdates.png", false),
                    NbBundle.getMessage(AutoupdateCheckScheduler.class, "AutoupdateCheckScheduler_UpdateFound_Hint"),
                    onMouseClickAction, NotificationDisplayer.Priority.HIGH);
        }
    }

    public static void notifyAvailableUpdates(Collection<UpdateElement> updates) {
        assert updates != null && ! updates.isEmpty() : "Some updates found.";
        LazyUnit.storeUpdateElements(OperationType.UPDATE, updates);
        notifyAvailable(LazyUnit.loadLazyUnits (OperationType.UPDATE), OperationType.UPDATE);
    }

}
